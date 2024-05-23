/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptStrmSrc.java /main/23 2012/09/25 06:20:28 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Stream Source Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    08/13/12 - implement canConstructQuery()
 udeshmuk    08/08/12 - bypass reference to worker and txn id columns
 udeshmuk    05/29/12 - instead of * repeat attr names in archiver queries
 udeshmuk    04/19/12 - add worker id and txn id
 sbishnoi    04/05/12 - use targetsqltype config property
 sbishnoi    04/05/12 - use targetsqltype config property
 udeshmuk    03/13/12 - change the generated query to include alias for
                        sub-query
 sbishnoi    05/19/10 - adding isRequireHbtTimeout in visualizer xml plan
 sborah      07/15/09 - support for bigdecimal
 sborah      06/12/09 - Memory Optimization
 sbishnoi    05/16/09 - adding complete lineage of input source
 anasrini    05/07/09 - system timestamped source lineage
 sborah      04/20/09 - reorganize sharing hash
 sborah      03/18/09 - define sharingHash
 sborah      11/27/08 - getSharingHash()
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    05/09/08 - fix viewrelnsrc drop
 parujain    03/11/08 - derived timestamp
 hopark      10/25/07 - set synopsis
 anasrini    08/27/07 - support for ELEMENT_TIME
 parujain    06/21/07 - fix delete
 parujain    12/18/06 - operator sharing
 rkomurav    09/13/06 - PhySynPos OO restructuring
 rkomurav    08/23/06 - add getXMLPlan2
 anasrini    08/03/06 - remove shareRelStore
 najain      06/20/06 - query deletion 
 najain      05/05/06 - sharing support 
 najain      04/06/06 - cleanup
 najain      03/24/06 - cleanup
 najain      02/27/06 - add createPhysicalOperator
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptStrmSrc.java /main/23 2012/09/25 06:20:28 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.common.SQLType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptStrmSrc;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.PhysicalPlanError;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;

/**
 * Stream Source Physical Operator 
 */
public class PhyOptStrmSrc extends PhyOpt {
  /** Id assigned by tableMgr */
  int strId;

  /** Id of the tableSource operator */
  int srcId;
 
  /** Derived timestamp expr */
  Expr derivedTs;

  /** Is this for a system timestamped stream */
  private boolean isSystemTimestamped;
  
  /** List of operators which wants this streamsource to send a hbt timeout*/
  private LinkedHashSet<Integer> timeOutOpList;

  /** Column number of the timestamp column in the schema.
   *  Applicable for archived stream only.
   */
  private int tsColNum = -1;

  private Datatype tsColType = null;

  private String[] aliases = null;

  public int getSrcId() {
    return srcId;
  }

  public void setSrcId(int srcId) {
    this.srcId = srcId;
  }

  public int getStrId() {
    return strId;
  }

  public void setStrId(int strId) {
    this.strId = strId;
  }
  
  public void setDerivedTs(Expr expr)
  {
    this.derivedTs = expr;
  }
  
  public Expr getDerivedTs()
  {
    return this.derivedTs;
  }

  public PhyOptStrmSrc(ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException, MetadataException
  {

    super(ec, PhyOptKind.PO_STREAM_SOURCE);

    int numOutAttrs;

    assert logPlan != null;
    assert logPlan.getNumInputs() == 0;
    assert logPlan instanceof LogOptStrmSrc;
    LogOptStrmSrc opStrmSrc = (LogOptStrmSrc) logPlan;

    int tableId = opStrmSrc.getStreamId();
    setStore(null);
    setInstOp(null);
    setStrId(tableId);
    setSrcId(opStrmSrc.getVarId());
    // output is a stream
    setIsStream(true);

    try 
    {
      // Output schema of the operator = schema of the stream + pseudo columns
      numOutAttrs = ec.getTableMgr().getNumAttrs(tableId) + 1;
      setNumAttrs(numOutAttrs);
    }
    catch (MetadataException ex) 
    {
      throw new PhysicalPlanException(PhysicalPlanError.DUMMY_ERROR_PHY);
    }

    isSystemTimestamped = ec.getTableMgr().isSystemTimestamped(tableId);
    
    // If system timestamped add self as the system timestamped source lineage
    if (isSystemTimestamped)
    {
      if(systsSourceLineage == null)
        systsSourceLineage = new LinkedHashSet<PhyOpt>();
      
      systsSourceLineage.add(this);
      
      timeOutOpList = new LinkedHashSet<Integer>();
    }
    
    if(fullSourceLineage == null)
      fullSourceLineage = new LinkedHashSet<PhyOpt>();
    
    fullSourceLineage.add(this);
    

    for (int a = 0; a < numOutAttrs - 1; a++) 
    {
      setAttrMetadata(a, ec.getTableMgr().getAttrMetadata(tableId, a));
    }

    // Set datatype and length for ELEMENT_TIME pseudo column
    StreamPseudoColumn elemTime       = StreamPseudoColumn.ELEMENT_TIME;
    setAttrMetadata(numOutAttrs-1, new AttributeMetadata(elemTime.getColumnType(), 
                                         elemTime.getColumnLen(),
                                         elemTime.getColumnType().getPrecision(),
                                         0));    
    this.derivedTs = null;
  }

  
//  public boolean delete(QueryDeletionContext ctx) throws CEPException
//  {
//    //  isdeleted will be false when queryid has already been deleted
//    // ex: select * from v1,v1; where v1 will be attempted to be dropped twice
//    boolean isdeleted = super.delete(ctx);
//
//    // remove it from the list, if no other query references this operator
//    if (qryIds.size() == 0 && isdeleted)
//      PlanManager.getPlanManager().dropSourceOp(strId, this);
//    
//    return isdeleted;
//  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorStreamSource>");
    sb.append(super.toString());

    sb.append("<StreamId strId=\"" + strId + "\" />");
    sb.append("<SourceId srcId=\"" + srcId + "\" />");

    sb.append("</PhysicalOperatorStreamSource>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> StrSrc </name>\n");
    xml.append("<lname> Stream Source </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Stream\" value = \"");
    xml.append(execContext.getTableMgr().getTable(strId).getName());
    xml.append("\"/>\n");
    xml.append("<property name = \"isHbtTimeOutRequired\" value = \"");
    xml.append(this.isHbtTimeoutRequired());
    xml.append("\"/>\n");
    return xml.toString();
  }

  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Stream Source has no Relation Synopsis
    assert(false);
    return null;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptStrmSrc))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
   
    PhyOptStrmSrc strmOpt = (PhyOptStrmSrc)opt;
  
    assert strmOpt.getOperatorKind() == PhyOptKind.PO_STREAM_SOURCE;
  
    if(strmOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(this.strId != strmOpt.strId)
      return false;
    if(this.derivedTs != null)
    {
      if(strmOpt.derivedTs == null)
        return false;
      if(!this.derivedTs.equals(strmOpt.derivedTs))
        return false;
    }
    else
    {
      if(strmOpt.derivedTs != null)
        return false;
    }
    
    return true;
  }
  
  /**
   * Add the given operator into the list of operators which
   * needs heartbeat from this stream source
   * @param phyOptId
   */
  public void addToTimeOutOpList(PhyOpt phyOp)
  {
    if(timeOutOpList != null && !timeOutOpList.contains(phyOp.getId()))
    {
      timeOutOpList.add(phyOp.getId());
      execContext.getPlanMgr().updateHbtTimeoutList(this, true);
    }
  }
  
  /**
   * Remove the given operator from the list of operators which
   * needs heartbeat from this stream source
   * @param phyOptId
   */
  public void removeFromTimeOutOpList(PhyOpt phyOp)
  {
    if(phyOp != null && timeOutOpList != null)
    {
      if(timeOutOpList.contains(phyOp.getId()))
        timeOutOpList.remove(phyOp.getId());
      if(timeOutOpList.isEmpty())
      {
        this.setHbtTimeoutRequired(false);
        // Remove the execution operator from heartbeat timeout list
        execContext.getPlanMgr().updateHbtTimeoutList(this, false);
      }
    }
  }
  
  //archived stream related
  public boolean isStateFul()
  {
    //This will be called only in case of archived stream.
    //we need to return true here as if a stream is archived then  
    //we always want it to be the query operator
    return true;
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    if(!execContext.getTableMgr().getTable(strId).isArchived())
    {
      return false;
    }
    
    aliases   = new String[execContext.getTableMgr().getAttrNames(strId).length];
    if(this.isView)
    {
      Integer[] depViewIds =
        execContext.getDependencyMgr().getDependents(q.getId(), 
                                                     DependencyType.VIEW);
      
      assert depViewIds.length == 1 :"More than one views dependent on query "+
                                     q.getName();
      
      String[] attrNames = 
        execContext.getViewMgr().getAttrNames(depViewIds[0]);
      String eventIdColNm = 
        execContext.getViewMgr().getView(depViewIds[0]).getEventIdColName();
      
      assert eventIdColNm != null : "eventId column name cannot be null"; 
      assert aliases.length == attrNames.length;
      
      this.setEventIdColName(eventIdColNm);
  
      int i=0;
      for(String aName : attrNames)
      {
        if(aName.equalsIgnoreCase(eventIdColNm))
          this.setEventIdColNum(i);
        aliases[i++] = aName;
      }
      //we are not explicitly adding the event id here.
      this.setEventIdColAddedToProjClause(false);
    }
    return true; 
  }
  
  public boolean canBeQueryOperator() throws CEPException
  {
    return true;
  }
  
  public void updateArchiverQuery() throws CEPException
  {
    this.projEntries = new LinkedList<String>();
    this.projTypes = new LinkedList<Datatype>();
    
    SQLType targetSQLType = 
        execContext.getServiceManager().getConfigMgr().getTargetSQLType();

    StringBuffer temp = null;
    String entityName = null;
    String archName = null;
    String tsColName = null;
    String workerIdColName = null;
    String txnIdColName = null;
    String[] attrNames = null;
    
    entityName = execContext.getTableMgr().getTable(strId).getEntityName();
    archName  = execContext.getTableMgr().getTable(strId).getArchiverName();
    tsColName = execContext.getTableMgr().getTable(strId).getTimestampColName();
    workerIdColName = execContext.getTableMgr().getTable(strId).getWorkerIdColName();
    txnIdColName = execContext.getTableMgr().getTable(strId).getTxnIdColName();
    attrNames = execContext.getTableMgr().getAttrNames(strId);
        
    this.setArchiverName(archName);
   
    if(attrNames != null)
    {
      temp = new StringBuffer();
      boolean commaRequired = false;
      int i = 0;
      for(String attrName : attrNames)
      {
        if(commaRequired)
          temp.append(", ");
        String name = attrName; 
        if(attrName.equalsIgnoreCase(tsColName))
        {
          this.setTsColNum(i);
          this.tsColType = execContext.getTableMgr().getTable(strId)
                           .getAttribute(i).getType();           
        }
        else if(attrName.equalsIgnoreCase(workerIdColName))
	{
          this.setWorkerIdColNum(i);
	  name = "null";
	}
        else if(attrName.equalsIgnoreCase(txnIdColName))
	{
          this.setTxnIdColNum(i);
	  name = "null";
	}
        if(this.isView)
        { //use aliases
          projEntries.add(name+" as "+aliases[i]);
          temp.append(name+ " as "+aliases[i]); 
        }
        else
        {
          projEntries.add(name+" as "+attrName);
          temp.append(name+ " as "+attrName);
        }
        
        projTypes.add(execContext.getTableMgr().getAttrType(strId,i));
        commaRequired = true;
        i++;
      }
    }
   
    /* Owing to bug 13973535 we cannot be assured that the BI server
     * would return the records sorted in ascending order of timestamp value.
     * Also using ORDER BY is problematic when constructing the UNION based
     * query as sub-queries are not allowed to use ORDER BY clause.
     * In view of the above, removing the ORDER BY clause from the generated
     * queries. The ExecOpt for StrmSrc would sort the records returned by
     * the archiver query in desired order */
    if(temp != null)
    {
      //Construct query here.
      if(execContext.getTableMgr().getTable(strId).isReplayRange())
      {
        //append where clause corresponding to range
        //range value is in nanoseconds 
        long range = execContext.getTableMgr().getTable(strId).getReplayRange();
                
        if(tsColType == Datatype.TIMESTAMP)
        {
          //convert range to seconds
          range /= 1000000000l;
          //use numtodsintervals if querying database, timestampadd otherwise
          if(targetSQLType == SQLType.ORACLE)
            this.setOutputSQL("select "+temp.toString()+" from "+ entityName +
                              " where ("+tsColName+" >= ? - numtodsinterval("+
                              range+",'SECOND'))");
                              //" order by "+ tsColName +" asc nulls last");
          else if(targetSQLType == SQLType.BI)
            this.setOutputSQL("select "+temp.toString()+" from "+entityName +
                              " where ("+tsColName+" >= timestampadd(" +
                              "SQL_TSI_SECOND,-"+range+",?))");
                              //" order by "+tsColName+" asc nulls last");
        }
        else 
        {
          assert tsColType == Datatype.BIGINT :"invalid timestamp column type";
          this.setOutputSQL("select "+temp.toString()+" from "+ entityName +
                            " where "+tsColName+" >= ? - "+ range );
			    //" order by "+tsColName+ " asc nulls last");
        }
      }
      else
      {
        //append the order by and rownum condition
        int numRows = execContext.getTableMgr().getTable(strId).getReplayRows();
        if(targetSQLType == SQLType.ORACLE)
          this.setOutputSQL("select "+temp.toString()+" from (select "+temp.toString()+
	                          " from (select "+temp.toString()+" from "+
                            entityName+ " order by "+tsColName+" desc nulls last) "+
                            this.getOptName()+"_1"+
                            " where rownum <= "+numRows+") "+this.getOptName()+"_2");
		            //" order by "+tsColName+ " asc nulls last");
        else if(targetSQLType == SQLType.BI)
          /* this.setOutputSQL("select "+temp.toString()+" from (select "+temp.toString()+" from "+
                          entityName+ " order by "+tsColName+" desc nulls last FETCH FIRST "+
			  numRows + " ROWS ONLY) "+ this.getOptName()+"_1"+
			  " order by "+tsColName+ " asc nulls last");*/
          //This is done temporarily till hoyong moves to BI PS6.
	  //Idea is to fetch all the data and then select only the top numRows tuples

          this.setOutputSQL("select "+temp.toString()+" from "+entityName);
                           // + " order by "+tsColName+" desc nulls last");

      }
    }
  }

  private void setTsColNum(int i)
  {
    this.tsColNum = i;
  }
  
  public int getTsColNum()
  {
    return tsColNum;
  }
  
  public Datatype getTsColType()
  {
    return tsColType;
  }
}
