/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptExchange.java /main/5 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/14 - setisDependentOnPartnStream
    anasrini    07/19/11 - override requiresBufferedInput=false
    sborah      04/15/11 - drop query dynamically
    anasrini    03/30/11 - use a LinkedHashSet for the ddls
    anasrini    03/29/11 - use CQL_RESERVED_PREFIX in partitionSchemaPrefix
    anasrini    03/28/11 - operator sharing
    anasrini    03/19/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptExchange.java /main/5 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.Constants;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.QueryDeletionContext;
import oracle.cep.service.ExecContext;

/**
 * The EXCHANGE physical operartor.
 *
 * This operator is related to patition parallelism. It is uniquely determined
 * by the set of base streams/relations that this serves and their
 * corresponding partitioning expressions. 
 * 
 * Its function is to assist in appropriately routing the partitions of these
 * streams / relations.
 */

public class PhyOptExchange extends PhyOpt 
{
  /** The partitioning expressions for each entity */
  private List<Expr> partitioningExprList;

  /** The DOP for this exchange operator */
  private int dop;

  /** 
   * The names of the entities involved in this Exchange.
   * The order of the names in this list exactly matches the order
   * of the inputs. That is, entityNameList.get(i) is the entityName
   * asocited with the i^th input
   */
  private List<String> entityNameList;

  /** The DDL commands to create the entity objects */
  private LinkedHashSet<String> ddls;

  /** 
   * Related to operator sharing
   * The newly added DDLs to be run due to the merge into the global plan
   * of a local plan operator
   */
  private List<String> newDDLs;

  /**
   * There will be "dop" number of schemas used by this Exchnage operator.
   * This is the prefix for each of the schema names
   */
  private String partitionSchemaPrefix;
  
  /**
   * True if the exchange operator's input is partition stream, false
   * otherwise.
   */
  private boolean isDependentOnPartnStream = false;

  // CONSTRUCTOR

  public PhyOptExchange(ExecContext ec, PhyOpt[] phyChildPlans, 
                        List<Expr> partitioningExprList,
                        Collection<String> objDDLs,
                        List<String> entityNameList, int dop,
                        boolean isDependentOnPartnStream)
    throws PhysicalPlanException 
  {
    
    super(ec, PhyOptKind.PO_EXCHANGE);
    this.partitioningExprList = partitioningExprList;
    this.ddls                 = new LinkedHashSet<String>();
    this.newDDLs              = new LinkedList<String>();
    this.entityNameList       = entityNameList;
    this.dop                  = dop;
    this.isDependentOnPartnStream = isDependentOnPartnStream;
    
    // output schema
    int numExprs = partitioningExprList == null ? 0 :
      partitioningExprList.size();

    setNumAttrs(numExprs);
    //FIXME: what is going to be the output schema in case of partition stream
    if(!isDependentOnPartnStream)
    {
      int a = 0;
      for (Expr e : partitioningExprList ) 
      {
        setAttrMetadata(a++,
                        new AttributeMetadata(e.getType(), e.getLength(), 
                                              e.getType().getPrecision(),
                                              0));
      }
    }

    // Process the inputs
    setNumInputs(phyChildPlans.length);
    int i=0;
    for (PhyOpt input : phyChildPlans)
    {
      input.addOutput(this);
      getInputs()[i] = input;
      i++;
    }

    // set the ddls
    ddls.addAll(objDDLs);

    // Set the partitioningSchemaPrefix
    StringBuilder sb = new StringBuilder();
    sb.append(Constants.CQL_RESERVED_PREFIX + ec.getSchemaName() + "_" 
              + getId() + "_");
    partitionSchemaPrefix = sb.toString();

  }

  public boolean isDependentOnPartnStream() {
    return isDependentOnPartnStream;
  }

  public void setDependentOnPartnStream(boolean isDependentOnPartnStream) {
    this.isDependentOnPartnStream = isDependentOnPartnStream;
  }

  public boolean delete(QueryDeletionContext ctx) throws CEPException
  {
    String queryName = ctx.getQuery().getName();
    if (!queryName.startsWith(Constants.CQL_RESERVED_PREFIX))
      executeDropDDLs(queryName);

    return super.delete(ctx);
  }

  private void executeDropDDLs(String queryName) throws CEPException
  {
    String partitionSchemaName;
    int    dop                   = this.getDOP();
    String partitionSchemaPrefix = this.getPartitionSchemaPrefix();
    String schemaName            = this.execContext.getSchemaName();
    String ddl                   = "drop query " + queryName;

    for(int j=0; j<dop; j++)
    {
      StringBuilder sb = new StringBuilder();
      sb.append(partitionSchemaPrefix + j);
      partitionSchemaName = sb.toString();
      
      // Now create all these objects in a schema just for this "bucket"
      this.execContext.setSchema(partitionSchemaName);
      
      LogUtil.fine(LoggerType.TRACE,
          "Running " + ddl + " in " + partitionSchemaName);
      this.execContext.executeDDL(ddl, true);
      
      this.execContext.setSchema(schemaName);
    }
    
  }

  // GETTER METHODS

  public List<Expr> getPartitioningExprList()
  {
    return partitioningExprList;
  }

  public Collection<String> getDDLs()
  {
    return ddls;
  }

  public Collection<String> getNewDDLs()
  {
    return newDDLs;
  }

  public List<String> getEntityNames()
  {
    return entityNameList;
  }

  public int getDOP()
  {
    return dop;
  }

  public String getPartitionSchemaPrefix()
  {
    return partitionSchemaPrefix;
  }

  // RELATED TO OPERATOR SHARING

  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    if(!this.isDependentOnPartnStream)
      return (this.getOperatorKind() + "#"
              + getExpressionList(partitioningExprList.toArray(new Expr[0])));
    else
    {//FIXME : is this correct?
      return this.getOperatorKind()+"#"+this.getEntityNames().toString();
    }
  }

  /**
   * This method tells whether the two operators are partially equivalent or
   * not
   */
  public boolean isPartialEquivalent(PhyOpt opt) {
    if (!(opt instanceof PhyOptExchange))
      return false;
    
    // this is to avoid finding the same operator in PlanManager list
    if (opt.getId() == this.getId())
      return false;
    
    PhyOptExchange exchangeOpt = (PhyOptExchange) opt;
    
    assert exchangeOpt.getOperatorKind() == PhyOptKind.PO_EXCHANGE;
    
    if (exchangeOpt.getNumInputs() != this.getNumInputs())
      return false;
    
    if (exchangeOpt.getNumAttrs() != this.getNumAttrs())
      return false;
        
    return comparePartitioningExprs(exchangeOpt);
  }
  
  private boolean comparePartitioningExprs(PhyOptExchange opt) 
  {
    // If both operator depends on partition stream, then consider them equivalent
    if(this.isDependentOnPartnStream != opt.isDependentOnPartnStream)
      return false;
    
    if(!this.isDependentOnPartnStream)
    {
      List<Expr> exchangeOptExprs = opt.getPartitioningExprList();

      if (exchangeOptExprs == null)
        return false;

      if (exchangeOptExprs.size() != partitioningExprList.size())
        return false;
  
      int i=0;
      for (Expr e : partitioningExprList)
      {
        Expr f = exchangeOptExprs.get(i);
        if (f.getKind() != e.getKind())
          return false;
        
        if (f.getType() != e.getType())
          return false;
        
        if (getAttrLen(i) != opt.getAttrLen(i))
          return false;
        
        if (!e.equals(f))
          return false;
  
        i++;
      }
    }
    return true;
  }

  @Override
  public boolean mergeIntoGlobalPlan(PhyOpt localOpt)  
  {
    assert localOpt instanceof PhyOptExchange;
    PhyOptExchange local = (PhyOptExchange)localOpt;

    // 2 Exchange operators can be shared only when they have the 
    // same number of inputs, the same base entities and the same
    // partitioning expressio for each of those base entities.
    // (see isPartailEquivalent above)
    // Hence, their set of entityDDLs will be identical, only difference
    // will be in the queryDDLs
    Collection<String> localDDLs = local.getDDLs();
    for(String lddl : localDDLs)
    {
      if (ddls.add(lddl))
        newDDLs.add(lddl);
    }

    for(String ddl : newDDLs)
    {
      LogUtil.fine(LoggerType.TRACE,
                   "Adding a new DDL into " + getId() + " " + ddl);
    }

    return (newDDLs.size() > 0);
  }

  // RELATED TO Buffered Queue requirements
  // Generally, non unary operators require queue buffering since they
  // have to coordinate multiple inputs over time.
  //
  // The EXCHANGE operator is an exception in the sense that while being
  // an n-ary operator, it does not have to coordinate its inputs over time, it
  // merely routes them to the appropriate partition for further processing.
  //
  // Hence, unlike other non unary operators, the EXCHANGE operator does not
  // require queue buffering as it can deal with the input event immediately.
  @Override
  public boolean requiresBufferedInput()
  {
    return false;
  }

  // TOSTRING METHOD OVERRIDE
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<PhysicalOperatorExchange dop=" + dop + ">");
    sb.append(super.toString());

    if (!isDependentOnPartnStream && partitioningExprList.size() != 0) 
    {
      sb.append("<NumofExpressions numExpr=\"" + partitioningExprList.size()
                + "\" />");
      for (Expr e : partitioningExprList)
        sb.append(e.toString());

      sb.append("<DDLs>");
      for (String ddl : ddls)
      {
        sb.append("<DDL>" + ddl + "<DDL/>");
      }

    }
    
    sb.append("</PhysicalOperatorExchange>");
    return sb.toString();
  }

  // QUERY PLAN RELATED
  // Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    int i;
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Exchange </name>\n");
    xml.append("<lname> Parallel Exchange </lname>\n");
    xml.append(super.getXMLPlan2());

    xml.append("<property name = \"PartitioningExpr List\" value = \"");
    if(!this.isDependentOnPartnStream)
    {
      for (Expr e : partitioningExprList)
      {
        xml.append(e.getXMLPlan2());
        xml.append(",");
      }
    }
    else
      xml.append("null");
    xml.append("\"/>");
    
    xml.append("<property name = \"DOP\" value = \"" + dop + "\"/>");
    xml.append("<property name = \"isDependentOnPartnStream\" value = \"" + isDependentOnPartnStream + "\"/>");
    return xml.toString();
  }

  // get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Exchange has no Relation Synopsis
    assert (false);
    return null;
  }



}
