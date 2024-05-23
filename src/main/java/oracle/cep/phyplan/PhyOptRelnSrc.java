/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRelnSrc.java /main/30 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Relation Source Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    08/31/15 - set extTableSource
 vikshukl    06/17/13 - fix sharing
 udeshmuk    08/13/12 - implement canConstructQuery()
 udeshmuk    08/08/12 - bypass reference to worker and txn id columns
 vikshukl    08/07/12 - archived dimension
 udeshmuk    04/16/12 - set worker id and txn id
 udeshmuk    08/25/11 - propagate event identifier col name for archived rel
 anasrini    07/19/11 - XbranchMerge anasrini_bug-12752107_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    07/15/11 - set pullOperator=true
 udeshmuk    06/29/11 - support for archived relation
 udeshmuk    06/20/11 - reflect the changed method names of archived relation
 udeshmuk    03/28/11 - archive relation support
 sbishnoi    05/21/10 - adding timeoutList
 sbishnoi    12/14/09 - adding review comments for table function
 sbishnoi    09/29/09 - support for table function
 sborah      10/07/09 - bigdecimal support
 sborah      07/15/09 - support for bigdecimal
 sborah      06/12/09 - Memory Optimization
 sbishnoi    05/16/09 - adding complete lineage of input source
 anasrini    05/07/09 - system timestamped source lineage
 sborah      04/20/09 - reorganize sharing hash
 sborah      03/18/09 - define sharingHash
 hopark      10/09/08 - remove statics
 parujain    05/09/08 - fix viewrelnsrc drop
 parujain    11/09/07 - external source
 parujain    11/05/07 - 
 mthatte     10/24/07 - adding isOnDemand
 hopark      10/25/07 - set synopsis
 parujain    06/21/07 - fix delete
 parujain    12/18/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 anasrini    08/03/06 - remove shareRelStore
 najain      05/15/06 - relation support 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 najain      02/23/06 - add createPhysicalOperator
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRelnSrc.java /main/30 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import oracle.cep.interfaces.input.ExtSource;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRelnSrc;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.PhysicalPlanError;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.service.ExecContext;

/**
 * Relation Source Physical Operator
 */
public class PhyOptRelnSrc extends PhyOpt {
  /** Id assigned by tableMgr */
  int         relId;

  /** Id of the tableSource operator */
  int         srcId;

  /** Is this for a system timestamped stream */
  private boolean isSystemTimestamped;

  /** Is this an ondemand relation? */
  boolean isExternal;
  
  /** IS this an archived relation? */
  boolean isArchivedDim;
  
  /** List of operators which wants this source to send a timeout heartbeat*/ 
  private LinkedHashSet<Integer> timeOutOpList;

  private String[] aliases = null;
  
  private ExtSource externalTableSource = null;
  
  public ExtSource getExternalTableSource() {
	return externalTableSource;
}


public void setExternalTableSource(ExtSource externalTableSource) {
	this.externalTableSource = externalTableSource;
}


public PhyOptRelnSrc(ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException, MetadataException {
    super(ec, PhyOptKind.PO_RELN_SOURCE);

    assert logPlan != null;
    assert logPlan.getNumInputs() == 0;
    assert logPlan instanceof LogOptRelnSrc;
    LogOptRelnSrc opRelnSrc = (LogOptRelnSrc) logPlan;

    int tableId = opRelnSrc.getRelationId();
    setRelId(tableId);
    setSrcId(opRelnSrc.getVarId());

    // output is not a stream
    setIsStream(false);
    
    try 
    {
      // Output schema of the operator = schema of the stream
      setNumAttrs(ec.getTableMgr().getNumAttrs(tableId));
      isSystemTimestamped = ec.getTableMgr().isSystemTimestamped(tableId);
      
      for (int a = 0; a < getNumAttrs(); a++) 
      {
        setAttrMetadata(a, ec.getTableMgr().getAttrMetadata(tableId, a));
      }
    } 
    catch (MetadataException ex) 
    {
      throw new PhysicalPlanException(PhysicalPlanError.DUMMY_ERROR_PHY);
    }
    
    //Set External flag
    this.setExternal(opRelnSrc.isExternal());

    // Set pull operator flag
    this.setPullOperator(opRelnSrc.isPullOperator());

    // set the archived dimension flag
    this.setArchivedDim(opRelnSrc.isArchivedDim());
    
    setupLineages(isSystemTimestamped);
    
    // Setup the attribute types and attribute length
    
    for (int a = 0; a < getNumAttrs(); a++) 
    {
      setAttrTypes(a, ec.getTableMgr().getAttrType(tableId, a));
      setAttrLen(a, ec.getTableMgr().getAttrLen(tableId, a));
    }
        
    //Set External flag
    this.setExternal(opRelnSrc.isExternal());       
  }

   
  public void setupLineages(boolean isSystemTimestamped)
  { 
    // If system timestamped add self as the system timestamped source lineage
    if (isSystemTimestamped)
    {
      if(systsSourceLineage == null)
        systsSourceLineage = new LinkedHashSet<PhyOpt>();
      
      systsSourceLineage.add(this);
      
      // Initialize an empty timeout list
      timeOutOpList = new LinkedHashSet<Integer>();
    }
    
    if(fullSourceLineage == null)
      fullSourceLineage = new LinkedHashSet<PhyOpt>();
    
    fullSourceLineage.add(this);
  }
    
  public boolean isExternal() 
  {
  return isExternal;
  }

  public void setExternal(boolean isExternal) 
  {
  this.isExternal = isExternal;
  }


  public boolean isArchivedDim() {
    return isArchivedDim;
  }


  public void setArchivedDim(boolean isArchivedDim) {
    this.isArchivedDim = isArchivedDim;
  }


  public PhySynopsis getOutSyn() {
    return getSynopsis(0);
  }

  public void setOutSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }

  public int getRelId() 
  {
    return relId;
  }

  public void setRelId(int relId) 
  {
    this.relId = relId;
  }

  public int getSrcId() 
  {
    return srcId;
  }

  public void setSrcId(int srcId) 
  {
    this.srcId = srcId;
  }

//  public boolean delete(QueryDeletionContext ctx) throws CEPException
//  {
//    // isdeleted will be false when queryid has already been deleted
//    // ex: select * from v1,v1; where v1 will be attempted to be dropped twice
//    boolean isdeleted = super.delete(ctx);
//
//    // remove it from the list, if no other query references this operator
//    if (qryIds.size() == 0 && isdeleted)
//      PlanManager.getPlanManager().dropSourceOp(relId, this);
//    
//    return isdeleted;
//  }

  // toString method override
  public String toString() 
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorRelationSource>");
    sb.append(super.toString());

    sb.append("<RelationId relId=\"" + relId + "\" />");
    sb.append("<SourceId srcId=\"" + srcId + "\" />");

    sb.append("<PhysicalSynopsis>");
    PhySynopsis outSyn = getOutSyn();
    sb.append(outSyn.toString());
    sb.append("</PhysicalSynopsis>");

    sb.append("</PhysicalOperatorRelationSource>");
    return sb.toString();
  }

  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException 
  {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> RelSrc </name>\n");
    xml.append("<lname> Relation Source </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Relation\" value = \"");
    xml.append(execContext.getTableMgr().getTable(relId).getName());
    xml.append("\"/>\n");
    xml.append("<property name = \"isHbtTimeoutRequired\" value = \"");
    xml.append(this.isHbtTimeoutRequired());
    xml.append("\"/>\n");
    return xml.toString();
  }
  
  // get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getOutSyn() == syn);
      return PhySynPos.OUTPUT.getName();
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptRelnSrc))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptRelnSrc relOpt = (PhyOptRelnSrc)opt;
  
    assert relOpt.getOperatorKind() == PhyOptKind.PO_RELN_SOURCE;
  
    if(relOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(this.relId != relOpt.relId)
      return false;
       
    if(this.isExternal || opt.isExternal)
      return false;
    
    // don't share archived dimension, because it behaves differently
    // depending on whether it is used in an archived view or not.
    if (this.isArchivedDim() || opt.isArchivedDim())
      return false;    
    
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
  
  //archived relation support related
  public boolean isStateFul()
  {
    // This will be called only in case of archived relation.
    return false; 
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    if(!execContext.getTableMgr().getTable(relId).isArchived())
      return false;
    
    aliases  = new String[execContext.getTableMgr().getAttrNames(relId).length];
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
    StringBuffer temp = null;
    String entityName = null;
    String archName = null;
    String eventIdColName = null;
    String workerIdColName = null;
    String txnIdColName = null;
    String[] attrNames = null; 
    
    entityName = execContext.getTableMgr().getTable(relId).getEntityName();
    archName = execContext.getTableMgr().getTable(relId).getArchiverName();
    eventIdColName = execContext.getTableMgr().getTable(relId).getEventIdColName();
    workerIdColName = execContext.getTableMgr().getTable(relId).getWorkerIdColName();
    txnIdColName = execContext.getTableMgr().getTable(relId).getTxnIdColName();
    attrNames = execContext.getTableMgr().getAttrNames(relId);
            
    this.setArchiverName(archName);
    if(!this.isView)
      this.setEventIdColName(eventIdColName);
    this.setWorkerIdColName(workerIdColName);
    this.setTxnIdColName(txnIdColName);
    
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
        
        if(attrName.equalsIgnoreCase(eventIdColName))
        {
          if(!this.isView)
            this.setEventIdColNum(i);
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
        { //use view aliases
          temp.append(name+ " as "+aliases[i]);
          projEntries.add(name+" as "+aliases[i]);
        }
        else
        {
          temp.append(name+ " as "+attrName);
          projEntries.add(name+" as "+attrName); 
        }
        projTypes.add(execContext.getTableMgr().getAttrType(relId,i));   
        commaRequired = true;
        i++;
      }
    }
    this.setOutputSQL("select "+ temp.toString()+" from "+entityName);
  }
}