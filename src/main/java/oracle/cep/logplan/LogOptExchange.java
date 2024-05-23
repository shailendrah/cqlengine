/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptExchange.java /main/6 2015/02/06 15:09:31 sbishnoi Exp $ */

/* Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/14/15 - bug 20241138
    sbishnoi    09/23/14 - support for partitioned stream
    anasrini    07/19/11 - XbranchMerge anasrini_bug-12752107_ps5 from
                           st_pcbpel_11.1.1.4.0
    sborah      04/24/11 - support for external relation
    anasrini    03/29/11 - partition parallelism for views
    anasrini    03/18/11 - partition parallelism
    anasrini    03/18/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptExchange.java /main/6 2015/02/06 15:09:31 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.expr.Expr;

/**
 * The EXCHANGE logical operartor.
 *
 * This operator is related to patition parallelism. It is uniquely determined
 * by the set of base streams/relations that this serves and their
 * corresponding partitioning expressions. 
 * 
 * Its function is to assist in appropriately routing the partitions of these
 * streams / relations.
 */
public class LogOptExchange extends LogOpt
{
  /** The metadata identifiers of the entities that this serves */
  private List<Integer> entityIdList;

  /** The names of the entities that this serves */
  private List<String> entityNameList;

  /** The partitioning expressions for each entity */
  private List<Expr> partitioningExprList;

  /** The degree of parallelism (DOP) setting for each entity */
  private List<Integer> entityDOPList;  

  /** The DOP for this exchange operator */
  private int dop;

  /** The DDL commands to create the objects */
  private LinkedHashSet<String> ddls;

  private boolean isDependentOnPartnStream;

  public LogOptExchange()
  {
    super(LogOptKind.LO_EXCHANGE);

    // Allocate the memory for the lists
    entityIdList         = new LinkedList<Integer>();
    entityNameList       = new LinkedList<String>();
    partitioningExprList = new LinkedList<Expr>();
    entityDOPList        = new LinkedList<Integer>();
    ddls                 = new LinkedHashSet<String>(); 

    inputs = new ExpandableArray<LogOpt>(Constants.MAX_INPUT_OPS);
  }

  public void addInput(LogOptSource inp)
  {
    int    inpDOP;
    int    inpEntityId;
    String inpEntityName;
    Expr   inpPartitioningExpr;
    String createDDL;
    String alterDDL;
    String alterStream = "alter stream ";
    String alterRelation = "alter relation ";

    inputs.add(inp);
    numInputs++;

    inp.setOutput(this);

    // Set the entity dop. 
    // Determine the exchange dop - set as maximum of the constituent
    //                              entity DOPs
    inpDOP = inp.getDegreeOfParallelism(); 
    entityDOPList.add(inpDOP);

    if (numInputs == 1)
      dop = inpDOP;
    else if (inpDOP > dop)
      dop = inpDOP;

    // Set the entityid
    inpEntityId = inp.getEntityId();
    entityIdList.add(inpEntityId);

    // Set the entityName
    inpEntityName = inp.getEntityName();
    entityNameList.add(inpEntityName);    

    // Set the partitioningExpr
    inpPartitioningExpr = inp.getPartitionParallelExpr();
    if(!this.isDependentOnPartnStream)
    {
      partitioningExprList.add(inpPartitioningExpr);
      // Also, create an output attribute corresponding to this
      setOutAttr(numOutAttrs, inpPartitioningExpr.getAttr());
      numOutAttrs++;
    }

    // Set the entity related DDLs
    createDDL = inp.getCreateDDL();
    ddls.add(createDDL);
    if (inp.isSourceStream())
      alterDDL = alterStream + inpEntityName + " add source push";
    else
      alterDDL = alterRelation + inpEntityName + " add source push";
    ddls.add(alterDDL);

    LogUtil.fine(LoggerType.TRACE, "Added input to LogOptExchange with " 
                 + "entityId=" + inpEntityId 
                 + " entityName=" + inpEntityName
                 + " entityDOP=" + inpDOP
                 + " partitioningExpr=" + inpPartitioningExpr);


  }

  public void addExernalRelationInput(LogOptSource inp)
  {
    int    inpEntityId;
    String inpEntityName;
    String createDDL;
    String alterDDL;
    String alterRelation = "alter relation ";

    // Set the entityid
    inpEntityId = inp.getEntityId();

    // Set the entityName
    inpEntityName = inp.getEntityName();

    // NOTE - Do not add this external relation input as an input to
    // LogOptExchange operator. This is because we only have
    // push sources as input to the LogOptExchange operator
    //
    // Also, do not add inpEntityId and inpEntityName to the corresponding
    // lists

    // Set the entity related DDLs
    createDDL = inp.getCreateDDL();
    ddls.add(createDDL);
   
    alterDDL = alterRelation + inpEntityName + " add source \" " 
      +inp.getSource() + " \"";

    ddls.add(alterDDL);

    LogUtil.fine(LoggerType.TRACE, "Added External Relation input to " +
                 "LogOptExchange with entityId=" + inpEntityId 
                 + " entityName=" + inpEntityName
                );
  }
  
  public void addNonPartitionOrderedSource(LogOptSource inp)
  {
    int    inpEntityId;
    String inpEntityName;
    String createDDL;
    String alterDDL;
    String alterRelation = "alter relation ";
    String alterStream = "alter stream ";
    boolean isStream = inp.isSourceStream();
    
    // Set the entityid
    inpEntityId = inp.getEntityId();

    // Set the entityName
    inpEntityName = inp.getEntityName();

    // NOTE - Do not add this external relation input as an input to
    // LogOptExchange operator. This is because we only have
    // push sources as input to the LogOptExchange operator
    //
    // Also, do not add inpEntityId and inpEntityName to the corresponding
    // lists

    // Set the entity related DDLs
    createDDL = inp.getCreateDDL();
    ddls.add(createDDL);
    boolean isPushSource = inp.getSource()==null; 
    if(isStream)
    {
      if(isPushSource)
        alterDDL = alterStream + inpEntityName + " add source push";
      else
        alterDDL = alterStream + inpEntityName + " add source \" "
          +inp.getSource() + " \"";
    }
    else
    {
      if(isPushSource)
        alterDDL = alterRelation + inpEntityName + " add source push";
      else
        alterDDL = alterRelation + inpEntityName + " add source \" " 
          +inp.getSource() + " \"";
    }
    ddls.add(alterDDL);
    LogUtil.info(LoggerType.TRACE, "Added Non Partitioned input to " +
                 "LogOptExchange with entityId=" + inpEntityId 
                 + " entityName=" + inpEntityName
                );

    LogUtil.info(LoggerType.TRACE, "Added Input Source for Non Partitioned"+
      " input for input entity. entityId:=" + inpEntityName + " source:" + 
      (isPushSource ? "Push Source" : inp.getSource()));
  }
  
  public void addDDL(String ddl)
  {
    ddls.add(ddl);
  }

  // Getter methods

  public List<Expr> getPartitioningExprList()
  {
    return partitioningExprList;
  }

  public Collection<String> getDDLs()
  {
    return ddls;
  }

  public List<String> getEntityNameList()
  {
    return entityNameList;
  }

  public int getDOP()
  {
    return dop;
  }

  public boolean isDependentOnPartnStream() {
    return isDependentOnPartnStream;
  }

  public void setDependentOnPartnStream(boolean isDependentOnPartnStream) {
    this.isDependentOnPartnStream = isDependentOnPartnStream;
  }

  public String toString()
  {
    String s = super.toString();

    LogUtil.fine(LoggerType.TRACE, "LogOptExchange dop= " + dop);

    for(String ddl : ddls)
    {
      LogUtil.fine(LoggerType.TRACE, "LogOptExchange ddl " + ddl);
    }

    return s;
  }

}
