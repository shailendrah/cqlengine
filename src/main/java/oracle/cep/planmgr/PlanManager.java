/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/PlanManager.java /main/66 2015/04/14 02:49:38 udeshmuk Exp $ */
/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    04/08/15 - invoke initializeState even when archived tuples are
                        zero
 udeshmuk    07/08/13 - add stats and fix logging for ARF - stress testing plan
 vikshukl    06/12/13 - miscl typos
 udeshmuk    05/06/13 - send heartbeat from in special join only while starting
                        view query
 sbishnoi    04/07/13 - XbranchMerge sbishnoi_bug-15962405_ps6 from
                        st_pcbpel_11.1.1.4.0
 vikshukl    02/11/13 - don't reset for join and/or join project
 udeshmuk    01/24/13 - set propstate to SIA_DONE after propagating state
 vikshukl    01/22/13 - skip the fact branch for finding the query operator if
                        special join
 udeshmuk    11/07/12 - introduce buffer operator on top of view root wherever
                        needed
 vikshukl    10/05/12 - don't allocate buffer operator on fact side if view is
                        a join between a fact and a slow dimension
 vikshukl    09/10/12 - prevent buffer op from initializing
 udeshmuk    08/03/12 - find connector operator even for a view query
 udeshmuk    07/06/12 - add a routine to identify the operators in the local
                        plan where buffer op should be added
 udeshmuk    04/13/12 - double counting and sharing support for archived rel
 anasrini    02/27/12 - bug 13739177, fix mem leak in queryOutputs
 udeshmuk    07/12/11 - update state initialization algo to incorporate binary
                        operators
 udeshmuk    06/19/11 - stabilize State initialization framewwork
 anasrini    03/28/11 - operator sharing related to EXCHANGE
 anasrini    03/20/11 - partition parallelism
 sbishnoi    03/17/11 - rangeWin will need lineage store and synopsis in case
                        of variable duration
 udeshmuk    03/07/11 - archived relations - state initialization algorithm
 sbishnoi    03/01/11 - initialize lineage store if window over relation
 sbishnoi    04/26/10 - calculate requireHbtTimeOut flag
 sbishnoi    03/11/10 - sharing of tablefunction phyopt didnt update the
                        operator list
 sbishnoi    01/21/10 - sharing table function operators
 parujain    05/29/09 - fix ids
 sbishnoi    05/15/09 - calling setSysTsSourceLineage
 parujain    05/04/09 - lifecycle management
 parujain    04/16/09 - lists maintenance
 sborah      03/29/09 - performance improvements
 hopark      02/14/09 - remove static lock
 parujain    02/09/09 - execution error
 sborah      12/19/08 - enable local sharing of ops
 parujain    12/12/08 - drop/stop query redesign
 udeshmuk    12/03/08 - optimize list operations.
 sborah      11/29/08 - hashmap for shared ops
 sborah      11/25/08 - support for altering base timeline
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    07/07/08 - value based windows
 parujain    05/08/08 - fix drop objects problem
 najain      12/13/07 - xmltable support
 sbishnoi    11/23/07 - support for update semantics
 parujain    11/01/07 - unsubscribe output
 anasrini    09/03/07 - PhyOptViewStrmSrc will now have own store
 najain      07/09/07 - cleanup
 skmishra    07/02/07 - fix getXMLPlan
 parujain    06/28/07 - orderby store
 hopark      06/17/07 - suppress unchecked warnings
 hopark      06/14/07 - 
 parujain    05/07/07 - fix bug
 parujain    04/27/07 - get QueryIds having root operator
 parujain    04/09/07 - interface for QueryIds for PhyOpt
 rkomurav    03/15/07 - add store related changes for patternstrm
 rkomurav    03/01/07 - add pattern related code
 parujain    02/12/07 - addToSched Sched not instantiated
 parujain    02/06/07 - fix getXMLPlan2
 rkomurav    01/24/07 - add getters
 parujain    01/17/07 - bug fix
 parujain    01/10/07 - Ref counting of PhyOpt
 parujain    01/08/07 - fix csfb test case errors
 najain      01/09/07 - bugs
 parujain    12/28/06 - count Physical Operators
 parujain    12/15/06 - operator sharing
 parujain    12/06/06 - propagating relation
 parujain    12/01/06 - remove Objects from PlanManager
 hopark      11/17/06 - bug 5583899 : removed input/outputs from ExecOpt
 najain      11/13/06 - bug fix
 najain      10/17/06 - add getPhyOpt
 rkomurav    09/11/06 - cleanup of xmldump
 anasrini    09/07/06 - In add_aux, visit DAG in topologically sorted order
 rkomurav    08/30/06 - check for null PhyOpts
 rkomurav    08/22/06 - XML_visualiser
 rkomurav    08/17/06 - genXMLPlan Implementaion
 najain      08/03/06 - view strm/reln use underlying store
 anasrini    08/03/06 - bReqStore is always true
 najain      08/03/06 - view strm/reln use underlying store
 anasrini    08/03/06 - bReqStore is always true
 najain      06/20/06 - add getNextQueryOutput 
 najain      06/21/06 - query deletion support 
 najain      06/10/06 - remove operator disable/enable 
 najain      06/04/06 - add list of relations 
 najain      05/31/06 - read/write locks 
 najain      05/04/06 - sharing support 
 najain      04/06/06 - cleanup
 najain      04/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/PlanManager.java /main/66 2015/04/14 02:49:38 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecSourceOpt;
import oracle.cep.execution.operators.Output;
import oracle.cep.execution.operators.StreamSource;
import oracle.cep.execution.scheduler.Scheduler;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.metadata.Destination;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptBuffer;
import oracle.cep.phyplan.PhyOptJoin;
import oracle.cep.phyplan.PhyOptJoinProject;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptOutput;
import oracle.cep.phyplan.PhyOptProject;
import oracle.cep.phyplan.PhyOptRelnSrc;
import oracle.cep.phyplan.PhyOptRngWin;
import oracle.cep.phyplan.PhyOptOutputIter;
import oracle.cep.phyplan.PhyOptSelect;
import oracle.cep.phyplan.PhyOptState;
import oracle.cep.phyplan.PhyOptTableFunctionRelnSrc;
import oracle.cep.phyplan.PhyOptValueWin;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhySharedQueueReader;
import oracle.cep.phyplan.PhySharedQueueWriter;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.phyplan.codegen.StoreGenFactoryContext;
import oracle.cep.phyplan.codegen.StoreInst;
import oracle.cep.phyplan.codegen.SynGenFactoryContext;
import oracle.cep.phyplan.codegen.SynInst;
import oracle.cep.planmgr.codegen.CodeGenHelper;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DAGHelper;
import oracle.cep.util.DAGNode;

/**
 * PlanManager
 * 
 * @author skaluska
 */
public class PlanManager
{
  ExecContext execContext;
  
  /**
   * Constructor for PlanManager.
   */
  public PlanManager(ExecContext ec)
  {
    execContext = ec;
    activePhyOpt     = new HashMap<Integer, PhyOpt>();
    
    activePhySyn     = new HashMap<Integer, PhySynopsis>();  
    
    activePhyStore = new HashMap<Integer, PhyStore>();

    sourceOps     = new HashMap<Integer, PhyOpt>();
    unorderedSourceOps = new HashMap<Integer, PhyOpt>();
    queryRootsMap = new HashMap<Integer, PhyOpt>();
    queryOutputs  = new HashMap<Integer, ArrayList<PhyOpt>>();
    
    activePhyReaderQueue = new HashMap<Integer, PhyQueue>();
    activePhyWriterQueue = new HashMap<Integer, PhyQueue>();
    
    nextPhyOptId = new AtomicInteger(0);
    nextPhyStoreId = new AtomicInteger(0);
    nextPhySynId = new AtomicInteger(0);
    nextPhyQueueId = new AtomicInteger(0);
    
    nextSnapshotId = new AtomicLong(0);
    snapshotList = new ArrayList<Snapshot>();
    
    chgNotifier = null;
  }

  static
  {
  }

  private AtomicInteger nextPhyStoreId;
  private AtomicInteger nextPhyOptId;
  private AtomicInteger nextPhySynId;
  private AtomicInteger nextPhyQueueId;
  // ----------------------------------------------------------------------
  // System Execution state: operators, queues, plan ...
  // ----------------------------------------------------------------------

  // Physical operators in the system.
  /* Currently we are not using the freelist and size variables for phyopt.
   * This is because their use is giving rise to a peculiar situation in 
   * operator sharing algorithm implementation that cause NPE while running
   * tkdata2 as a part of srg. The exact problem has been identified and 
   * documented and needs to be discussed further.
   */
  private HashMap<Integer, PhyOpt>   activePhyOpt; 

  private HashMap<Integer, PhySynopsis> activePhySyn;

  private HashMap<Integer,PhyStore> activePhyStore;
  // ----------------------------------------------------------------------
  // "Naming" related:
  // 
  // The output of some subqueries form the input of other queries.
  // A tableId that occurs in a plan could refer to a base
  // stream/relation or to the result of another subquery. We
  // handle these two cases uniformly by storing a "source" operator
  // for each tableId that produces the tuples of the
  // stream/relation corresponding to the table.
  // ----------------------------------------------------------------------

  // Mapping from tableId/viewId to the corresponding physical source operator
  private HashMap<Integer, PhyOpt>      sourceOps;

  // Mapping from queryId to the corresponding root physical operator
  private HashMap<Integer, PhyOpt>      queryRootsMap; //used for O(1) lookups

  // Mapping from queryId to the corresponding output physical operators.
  // Note that there may be more than one output for a given query so using a list
  private HashMap<Integer, ArrayList<PhyOpt>> queryOutputs;

  /** global lock for exclusion during compilation */
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  /** Currently only one subscriber(LogManager) is supported.
   *  
   */
  private IPlanChgNotifier              chgNotifier;  
  
  private HashMap<Integer, PhyQueue> activePhyReaderQueue;
  
  private HashMap<Integer, PhyQueue> activePhyWriterQueue;
  
  private HashMap<Integer, LinkedList<PhyOpt>> tableFunctionPhyOptList;
  
  private HashMap<Integer, PhyOpt> unorderedSourceOps;
  
  //Id of the next snapshot
  private AtomicLong nextSnapshotId;
  
  private List<Snapshot> snapshotList;
  
  /**
   * Getter for lock in PlanManager
   * 
   * @return Returns the lock
   */
  public ReentrantReadWriteLock getLock()
  {
    return lock;
  }

  public PhyOpt getPhyOpt(int id)
  {
    return activePhyOpt.get(id);
  }
  
  public long getNextSnapshotId()
  {
    return nextSnapshotId.getAndIncrement();
  }
  
  public long getCurrentSnapshotId()
  {
    return nextSnapshotId.get();
  }
  
  public int getNextPhyOptId()
  {
    return nextPhyOptId.getAndIncrement();
  }
  
  public int getNextPhySynId()
  {
    return nextPhySynId.getAndIncrement();
  }
  
  public int getNextPhyStoreId()
  {
    return nextPhyStoreId.getAndIncrement();
  }
  
  public int getNextPhyQueueId()
  {
    return nextPhyQueueId.getAndIncrement();
  }
  
  /**
   * Gets the list of QueryIds for a particular Physical Operator
   * 
   * @param id
   *        Physical Operator Id
   * @return Returns the List of Query Ids
   */
  public List<Integer> getQueryIds(int id)
  {
    PhyOpt op = getPhyOpt(id);
    return op.getQryIds();
  }
  
  public Collection<PhyOpt> getActivePhyOpt()
  {
    return activePhyOpt.values();
  }
  
  public Collection<PhySynopsis> getActivePhySyn()
  {
    return activePhySyn.values();
  }
  
  public Collection<PhyStore> getActivePhyStore()
  {
    return activePhyStore.values();
  }
  
  public Iterator<PhyStore> getStoreListIterator()
  {
    return activePhyStore.values().iterator();
  }
  
  public void addPhyQueueReader(PhySharedQueueReader reader)
  {
    activePhyReaderQueue.put(reader.getId(), reader);
  }
  
  public void removePhyQueueReader(PhyQueue reader)
  {
    activePhyReaderQueue.remove(reader.getId());
  }
  
  public Iterator<PhyQueue> getSharedQueueReaderIterator()
  { 
    return activePhyReaderQueue.values().iterator();
  }
  
  public void addPhyQueueWriter(PhySharedQueueWriter writer)
  {
    activePhyWriterQueue.put(writer.getId(), writer);
  }
  
  public void removePhyQueueWriter(PhyQueue writer)
  {
    activePhyWriterQueue.remove(writer.getId());
  }
  
  public Iterator<PhyQueue> getSharedQueueWriterIterator()
  {
    return activePhyWriterQueue.values().iterator();
  }
  
  /**
   * Gets List of all output PhyOpt corresponding to this query
   * 
   * @param qryId
   *          Query identifier
   * @return List of query's output phyOpts. If none is associated
   *         with this query return empty (non-null) list.
   */
  public List<PhyOpt> getAllQueryOutputs(int qryId)
  {
    ArrayList<PhyOpt> phyList = queryOutputs.get(qryId);
    if(phyList==null)
      return null;
    else
      return phyList;
  }
  
  /**
   *  Gets whether the output operator's query has multiple 
   *  destinations or not.
   *   
   * @param phyid
   *              Physical Operator id
   * @return  True if query has multiple output destinations 
   */
  public boolean hasQueryMultipleOutputs(int phyid)
  {
    PhyOptOutput op = (PhyOptOutput)getPhyOpt(phyid);
    if(queryOutputs.get(op.getQueryId()).size() > 1)
      return true;
    return false;
  }
  
  /**
   * get the Physical Operator for the specified identifier
   * 
   * @param tableId
   *          id of the source
   * @return physical operator of the source
   */
  public PhyOpt getSourceOpt(int tableId)
  {
    return sourceOps.get(tableId);
  }


  /**
   * set the operator for the given source table
   * 
   * @param tableId
   *          identifier of the source table
   * @param op
   *          physical operator for the source table
   */
  public void setSourceOpt(int tableId, PhyOpt op)
  {
    if(getSourceOpt(tableId) == null)
      sourceOps.put(tableId, op);
  }
  
  /**
   * We keep a separate list for unordered source ops, as they
   *  have separate execution operators.
   *  
   * @param streamId
   * @return
   */
  public PhyOpt getUnorderedSourceOpt(int streamId)
  {
    return unorderedSourceOps.get(streamId);
  }
  
  public void setUnorderedSourceOpt(int tableId, PhyOpt op)
  {
    if(getUnorderedSourceOpt(tableId) == null)
      unorderedSourceOps.put(tableId, op);
  }



  /**
   * Gets the list of all query ids having a root operator
   * 
   * @return List of Query ids
   */
  public ArrayList<Integer> getRootQueryIds()
  {
    ArrayList<Integer> queryRoots = new ArrayList<Integer>();
    if(queryRootsMap.isEmpty()) 
      return queryRoots;
    Set<Integer> queryIds = queryRootsMap.keySet();
    Iterator<Integer> iter = queryIds.iterator();
    while(iter.hasNext())
    {
      queryRoots.add(iter.next());
    }
    return queryRoots;
  }
  
  /**
   * get the Query Root Operator for the specified identifier
   * 
   * @param qryId
   *          id of the query
   * @return physical operator of the query root
   */
  public PhyOpt getQueryRootOpt(int qryId)
  {
    return queryRootsMap.get(qryId);
  }

  /**
   * get the next Query Output Operator for the specified identifier
   * 
   * @param qryId
   *          id of the query
   * @return physical operator of the query output
   */
  public PhyOpt getNextQueryOutput(int qryId)
  {
    ArrayList<PhyOpt> phyList = queryOutputs.get(qryId);
    if(phyList!=null && phyList.size()!=0)
      return phyList.get(0);
    else // The operator for the given id. has not been added
      return null;
  }

  public boolean isQueryOutputPresent(int qryId, PhyOpt output)
  {
    ArrayList<PhyOpt> phyList = queryOutputs.get(qryId);
    if(phyList!=null)
    {
      Iterator<PhyOpt> iter = phyList.iterator();
      while(iter.hasNext())
      {
        if(iter.next() == output)
          return true;
      }
    }
    // The operator for the given id. has not been added
    return false;
  }

  private boolean isSourceOperatorPresent(int id, PhyOpt op)
  {
    PhyOpt val;
    if(((val=sourceOps.get(id))!=null) || ((val=unorderedSourceOps.get(id))!=null))
    {
      if(val==op)
        return true;
      else
        return false;
    }
    else// The operator for the given id. has not been added
      return false;
  }

  /**
   * set the root operator for the given query
   * 
   * @param qryId
   *          identifier of the source query
   * @param op
   *          physical operator for the query root
   */
  public void setQueryRoot(int qryId, PhyOpt op)
  {
    // make sure that the operator is not already present
    assert getQueryRootOpt(qryId) == null : getQueryRootOpt(qryId);

    queryRootsMap.put(qryId, op);
    notifyAddQuery(qryId, op);
  }

  public void removeQueryRoot(int qryId, PhyOpt op)
  {
    // make sure that the operator is already present
    assert getQueryRootOpt(qryId) == op;

    queryRootsMap.remove(qryId);
    notifyRemoveQuery(qryId, op);
  }

 
  /**
   * @param o
   *          physical operator to be added in the global list
   */
/*  public synchronized void addPhyOpt(PhyOpt o)
  {
    int j = addObject(listPhyOpt, freeListPhyOpt, o);
    sizeListPhyOpt++; //increment count of non-null elements
    o.setId(j);
  } 
*/
  public synchronized void addPhyOpt(PhyOpt o)
  {
    activePhyOpt.put(o.getId(), o);
  } 
  
  

  public synchronized void removePhyOpt(PhyOpt o)
  {
    if (o.getInstOp() != null)
      execContext.getExecMgr().removeOp(o.getInstOp());
    activePhyOpt.remove(o.getId());
  }

  /**
   * @param o
   *          physical synopsis to be added in the global list
   */
  public synchronized void addPhySyn(PhySynopsis o)
  {
    activePhySyn.put(o.getId(), o); 
  }
  
  /**
   * If physical synopsis belong to this phyOpt then remove it
   * @param opt PhyOpt getting deleted
   */
  public synchronized void removePhysicalSynopsis(PhyOpt opt)
  {
    Collection<PhySynopsis> syn = activePhySyn.values();
    // Required to collect all synids to be removed otherwise we will
    // have concurrentmodification exception
    ArrayList<Integer> idlist = new ArrayList<Integer>();
    for(PhySynopsis phySyn: syn)
    {
      
      if(phySyn != null)
      {
        if(phySyn.getOwnOp().equals(opt))
        {
          idlist.add(phySyn.getId());
        }
      }
    }
    for(int i=0; i<idlist.size(); i++)
      activePhySyn.remove(idlist.get(i));
    
  } 

  /**
   * @param o
   *          physical store to be added in the global list
   */
  public synchronized void addPhyStore(PhyStore o)
  {
    activePhyStore.put(o.getId(), o);  
  }

  
  public synchronized void removePhyStore(Integer pos, PhyStore o, PhyOpt opt)
  {
     PhyStore phyStore = activePhyStore.get(pos);
     if(phyStore != null)
     {
       assert phyStore.getId() == o.getId();
       if(phyStore.getOwnOp().equals(opt))
         activePhyStore.remove(pos);
     }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#addBaseTable(int,
   *      oracle.cep.interfaces.input.TableSource)
   */
  public void addBaseTable(int tableId, TableSource source)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#addLogicalQueryPlan(int,
   *      oracle.cep.logplan.LogOpt, oracle.cep.interfaces.output.QueryOutput)
   */
  public void addLogicalQueryPlan(int queryId, LogOpt logPlan,
      QueryOutput output)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#getQuerySchema(int)
   */
  public String getQuerySchema(int queryId)
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#map(int, int)
   */
  public void map(int queryId, int tableId)
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#optimize_plan(oracle.cep.phyplan.PhyOpt)
   */
  public void optimize_plan(PhyOpt root)
  {

  }
  
  public PhyOpt shareOperators(Query query, PhyOpt root)
    throws PhysicalPlanException
  { 
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
    Iterator<PhyOpt> iter;
    List<PhyOpt> opList = null;
    boolean flag;
     
    if(query.isDependentOnArchivedRelation())
    {
      for(DAGNode op : nodes)
      {
        PhyOpt phyOp = (PhyOpt) op;
        phyOp.setCanBeConnectorOperator(true);
        phyOp.setLHSConnector(false);
        phyOp.setRHSConnector(false);
      }
    }
    
    for (DAGNode op : nodes) 
    {
      PhyOpt phyopt = (PhyOpt)op;
  
      flag = false;
      PhyOpt opt = null;
  
      /**
       * If the operator is a source,
       * then just set the global equivalent to itself
       */      
      if(phyopt.getIsSource())
      {
        if(phyopt.getGlobalEquiv() != phyopt)
        {
          assert phyopt.getGlobalEquiv() == null;
          phyopt.setGlobalEquiv(phyopt);
        }
        
        if(phyopt instanceof PhyOptTableFunctionRelnSrc)
        {
          // List of Table function operator in global plan
          if(tableFunctionPhyOptList != null)
            opList = tableFunctionPhyOptList.get(phyopt.getSharingHash());
        }
        else
        {
          phyopt.setSourceLineages();
          continue;
        }
      }
      else
      {
       /**
        * Get a list of possible
        * output matches from one of its inputs. 
        */
        opList = phyopt.getInputs()[0].getPossibleOutputMatches(phyopt);
      }
      
      //if there are possible matches and if the operator
      //can be shared (used in archived relation dependent query)
      //then find the equivalents.
      if((opList != null) && (phyopt.canBeShared()))
      {
        iter = opList.iterator();
  
        // find out the operator which is equivalent
        while(iter.hasNext() && (!flag))
        {
          opt = (PhyOpt)iter.next();
        
          flag = phyopt.isCompleteEquiv(opt) 
                 && phyopt.isPartialEquivalent(opt);
       
        }
      }//end of if(opList != null)
     
      // If flag is false this shows that there is no shareable operator for
      // this operator. So set inputs and return the root
      if (flag == false)
      {
        // update the global equivalent pointer 
        // An operator which is admitted to the global
        // plan has its global equivalent set to itself.
        phyopt.setGlobalEquiv(phyopt);
        phyopt.fixInputs();
        
        if(query.isDependentOnArchivedRelation() && 
          (!phyopt.isBelowViewRootInclusive()))
        {
          /* 
           * We want to know the operator(s) at which the local plan joins the 
           * global query plan. We refer to such an operator as 'connector'
           * operator. 
           * 
           * A binary operator can be connector for left or right or both sides. 
           * isLHSConnector and isRHSConnector are the two booleans in PhyOpt
           * that keep track of this.
           * For unary there is only one input and so we make use of only the
           * isLHSConnector variable.
           * One more variable canBeConnectorOperator is used to decide whether
           * a particular operator can be connector or not. 
           * 
           * Invariant: If an operator is marked as a connector (either LHS or
           * RHS or both) then no other UNARY operator in its reachable set
           * can be connector.
           * 
           * However, a binary operator can be connector and that needs special
           * handling.
           * 
           * 1.Initially all the operators in the local plan can be connectors.
           * (canBeConnectorOperator = TRUE)
           * 2.Once we get an UNARY operator that is to be added to global plan,
           * If its canBeConnectorOperator is TRUE (it would be true iff no other 
           * operator below it has been connector) then we set
           * - the connector flags in it appropriately. 
           * - canBeConnectorOperator flag to FALSE for all operators
           * (unary as well binary) in its reachable set. 
           * Doing so establishes the invariant.
           * 3. Special handling for binary :
           * If the operator to be added to global plan is binary then 
           * if its canBeConnectorOperator flag is TRUE, it means it is 
           * connector for both sides.
           * else
           * we have to check the canBeConnectorOperator flags for children
           * to decide whether this is the connetor operator or not.
           */
          
          if(phyopt.canBeConnectorOperator())
          {
            if(phyopt.getInputs() != null)
            {  
              if(phyopt.getInputs().length == 1)
              { 
                // unary
                phyopt.setLHSConnector(true);
                LogUtil.finer(LoggerType.TRACE, "ARF# "+ 
                             phyopt.getOptName()+" marked as connector operator for query "+query.getName());
                setConnectorFlagForDownstreamOps(phyopt);
              }
              else
              {
                // binary
                assert phyopt.getInputs().length == 2;
                phyopt.setLHSConnector(true);
                phyopt.setRHSConnector(true);
                LogUtil.finer(LoggerType.TRACE, "ARF# "+
                  phyopt.getOptName()+" marked as connector operator for query "+query.getName());
                setConnectorFlagForDownstreamOps(phyopt);
              }
            }
          }
          else
          {
            //if it is a binary then we have to check even if the flag is false.
            if(phyopt.getInputs().length == 2)
            {
              //if a child operator has canBeConnectorOperator() returning TRUE
              //and none of isLHSConnector and isRHSConnector is set for it then
              //this binary op is a connector
              if((phyopt.getInputs()[0].canBeConnectorOperator())
                 &&((!phyopt.getInputs()[0].isLHSConnector()) && (!phyopt.getInputs()[0].isRHSConnector()))
                )
                phyopt.setLHSConnector(true);
              if((phyopt.getInputs()[1].canBeConnectorOperator())
                 &&((!phyopt.getInputs()[1].isLHSConnector()) && (!phyopt.getInputs()[1].isRHSConnector())) 
                ) 
                phyopt.setRHSConnector(true);
              
              //if it is a connector for at least one side then set the flag for
              //downstream operators
              if(phyopt.isLHSConnector() || phyopt.isRHSConnector())
              {
                setConnectorFlagForDownstreamOps(phyopt);
                LogUtil.finer(LoggerType.TRACE, "ARF# "+
                    phyopt.getOptName()+" marked as connector operator for query "+query.getName());
              }                
            }
          }
        }
        
        if(phyopt instanceof PhyOptTableFunctionRelnSrc)
        {
          // 2 Cases:
          // Case 1: There were no operators having possible match with current
          //         operator
          // Case 2: There were few potential matches but nobody was found 
          //         equivalent to the current operator
          if(opList == null)
          {
            if(tableFunctionPhyOptList == null)
              tableFunctionPhyOptList = new HashMap<Integer, LinkedList<PhyOpt>>();
            LinkedList<PhyOpt> newList = new LinkedList<PhyOpt>();
            newList.add(phyopt);
            tableFunctionPhyOptList.put(phyopt.getSharingHash(), newList);
          }
          else
            opList.add(phyopt);              
        }
      }
      else 
      {
        LogUtil.fine(LoggerType.TRACE,
                     "Merging  " + phyopt.getOptName() 
                     + " into " + opt.getOptName());

        // Here, we found a match in the global plan for this operator
        // Now, merge the contents of this operator into its global equivalent
        // Call globalEquiv.merge(phyOpt)
        boolean reinstantiate = opt.mergeIntoGlobalPlan(phyopt);
        
        //if this is a query operator in local plan then mark it as so 
        //in global plan operator as well. 
        //can happen only for relsrc, strmsrc, project and select that
        //do not maintain synopsis
        if(phyopt.isQueryOperator())
        {
          opt.setIsQueryOperator(true);
        }
        opt.setOutputSQL(phyopt.getOutputSQL());
        if(phyopt.getIsView())
        {
          opt.setIsView(true);
        }
          
        // if there is a change in the global operator due to the merge that
        // requires a reinstantiation, then we need to handle that here
        if (reinstantiate)
          opt.setState(PhyOptState.REINST);
      }

   }
   //  if Root itself is shared i.e. same query already exists
   if(root.getGlobalEquiv() != root)
   {
     root = root.fixRoot();
   }
   
   // remove all the operators in the local plan which are 
   // being shared with operators in the global plan.
   for(DAGNode n : nodes)
   {
     PhyOpt phyopt = (PhyOpt)n;
     /**
      * An operator is shared iff its global equivalent is 
      * different from itself.
      * Hence it is not part of the global plan so remove it.
      */
     if(phyopt.getGlobalEquiv() != phyopt)
     {
       phyopt.removePhyOp();
     }
   }  
   
    return root;
  }
  
  public void setConnectorFlagForDownstreamOps(PhyOpt op)
  {
    List<PhyOpt> listPhyOpts = new LinkedList<PhyOpt>();
    listPhyOpts.add(op);
    
    while(!listPhyOpts.isEmpty())
    {
      PhyOpt out = (PhyOpt)listPhyOpts.remove(0);
      if(out != op)
        out.setCanBeConnectorOperator(false);
      PhyOptOutputIter outputs = out.getOutputsIter();
      if(outputs != null)
      {
        try
        {
          while((out = outputs.getNext()) != null)
            listPhyOpts.add(out);
        }
        catch(PhysicalPlanException pe)
        {
          // TODO: add handling
        }
      }
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#add_aux_structures(oracle.cep.phyplan.PhyOpt)
   */
  public void add_aux_structures(Query query, PhyOpt root)
      throws PhysicalPlanException, MetadataException
  {
    // TODO: currently, we are assuming that only a single thread is compiling
    // the queries. Otherwise, this method should be synchronized. Operator
    // level
    // synchronization can be postponed.

    // The auxillary structures can be only added in a certain order.
    // For example, the synopsis requirements need to be addressed before
    // determining the store requirements. This is because store requirements
    // depend on the synopsis requirements.

    // Thus, we need to iterate through the DAG of nodes for this query in
    // topologically sorted order, for each phase separately.

    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
     
    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      add_syn(query, opt);
    }

    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      add_store(query, opt);
    }

    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      add_queues(query, opt);
    }

    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      set_in_stores(query, opt);
    }


    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      set_op_state(query, opt);
    }
  }

  private void set_op_state(Query query, PhyOpt op)
  {
    if (op.getState() == PhyOptState.INIT)
      op.setState(PhyOptState.ADDAUX);
  }

  private void add_syn(Query query, PhyOpt op)
  {
    if (
        (op.getOperatorKind() == PhyOptKind.PO_PATTERN_STRM) || 
        (op.getOperatorKind() == PhyOptKind.PO_XMLTABLE) ||
        (op.getOperatorKind() == PhyOptKind.PO_TABLE_FUNCTION) ||
        (op.getOperatorKind() == PhyOptKind.PO_EXCHANGE)
       )
      return;

    if (op.getState() == PhyOptState.INIT)
      SynInst.addSyn(new SynGenFactoryContext(execContext, op));
  }

  private void add_store(Query query, PhyOpt op) throws PhysicalPlanException
  {
    if (op.getState() != PhyOptState.INIT)
      return;

    PhyStore store;

    // These operators do not have any output, hence no output store required
    if (op.getOperatorKind() == PhyOptKind.PO_SINK)
    {
      return;
    }

    // The RANGE_WIN and ROW_WIN operators do not own a store. However,
    // they have outputs. Their outputStore is the same as the outputStore
    // of their input
    if (op.getOperatorKind() == PhyOptKind.PO_ROW_WIN
        || (op.getOperatorKind() == PhyOptKind.PO_RANGE_WIN
            && !((PhyOptRngWin)op).isVariableDurationWindow())
        || (op.getOperatorKind() == PhyOptKind.PO_VALUE_WIN
            && !((PhyOptValueWin)op).isWindowOverRelation())
        || op.getOperatorKind() == PhyOptKind.PO_VIEW_RELN_SRC
        || op.getOperatorKind() == PhyOptKind.PO_ORDER_BY
        || op.getOperatorKind() == PhyOptKind.PO_OUTPUT
        || op.getOperatorKind() == PhyOptKind.PO_SLIDE)
    {
      PhyStore outInpStore = op.getInputs()[0].getSharedRelStore();
      assert outInpStore != null;
      assert outInpStore == op.getInputs()[0].getStore();
      op.setStore(outInpStore);
      op.linkSynStore();
      return;
    }

    // For sys stream gen operator we generate a store only
    // when we come here through a monitor query [[ Explanation ]]
    if (op.getOperatorKind() == PhyOptKind.PO_SS_GEN &&
        op.getNumOutputs() == 0)
      return;

    store = StoreInst.addStore(new StoreGenFactoryContext(execContext, op));

    // add the store to the operator
    op.setStore(store);
    store.setOwnOp(op);

    // Store requirements for the synopsis owned by each operator
    op.synStoreReq();
  }

  private void add_queues(Query query, PhyOpt op) throws PhysicalPlanException,
      MetadataException
  {

    if (op.getState() != PhyOptState.INIT)
      return;

    // The kind of queue to used depends on the number of outputs, so for
    // simplification, the code is left in this shape.

    if (
        (op.getOperatorKind() != PhyOptKind.PO_OUTPUT) &&
        (op.getOperatorKind() != PhyOptKind.PO_EXCHANGE)
       )
    {
      PhySharedQueueWriter writer =
        new PhySharedQueueWriter(getNextPhyQueueId());
      writer.setSource(op);
      op.setOutQueue(writer);
    }

    // Create the readers for my inputs
    for (int i = 0; i < op.getNumInputs(); i++)
    {
      PhySharedQueueReader reader =
        new PhySharedQueueReader(getNextPhyQueueId());
      PhySharedQueueWriter inpWriter;
      PhyOpt inpOp = op.getInputs()[i];

      inpWriter = inpOp.getOutQueue();
      reader.setWriter(inpWriter);
      reader.setDest(op);

      op.getInQueues()[i] = reader;
    }
  }

  private void set_in_stores(Query query, PhyOpt op)
  {
    if (op.getState() != PhyOptState.INIT)
      return;

    assert op != null;
    PhyOpt child;

    for (int i = 0; i < op.getNumInputs(); i++)
    {

      // Stores already set (called recursively by one of op's parents)
      if (op.getInStores()[i] != null)
      {
        assert i == 0;
        break;
      }

      child = op.getInputs()[i];
      assert child != null;

      if (child.getStore() != null)
        op.getInStores()[i] = child.getStore();
      else
      {
        assert (child.getOperatorKind() == PhyOptKind.PO_RANGE_WIN
            || child.getOperatorKind() == PhyOptKind.PO_ROW_WIN
            || child.getOperatorKind() == PhyOptKind.PO_VALUE_WIN
            || child.getOperatorKind() == PhyOptKind.PO_VIEW_RELN_SRC
            || child.getOperatorKind() == PhyOptKind.PO_VIEW_STRM_SRC
            || child.getOperatorKind() == PhyOptKind.PO_PATTERN_STRM
            || child.getOperatorKind() == PhyOptKind.PO_XMLTABLE
            || child.getOperatorKind() == PhyOptKind.PO_ORDER_BY);

        assert (child.getNumInputs() == 1);

        if (child.getInStores()[0] == null)
          set_in_stores(query, child);

        assert (child.getInStores()[0] != null);

        op.getInStores()[i] = child.getInStores()[0];
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#getXMLPlan(oracle.cep.phyplan.PhyOpt)
   */
  public String getXMLPlan()
  {
    execContext.getPlanMgr().getLock().readLock().lock();
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalPlan>");
    for ( PhyOpt p : activePhyOpt.values())
    {
      if(p == null)
        continue;
      if (p.getOperatorKind() == PhyOptKind.PO_OUTPUT)
      {
        sb.append(p.toString());
      }
    }
    sb.append("</PhysicalPlan>");
    execContext.getPlanMgr().getLock().readLock().unlock();
    return sb.toString();
  }
  
  /**
   * Get the number of Physical operators in the Global Plan
   * @return
   *       Total no of Physical Operators
   */
  public synchronized int getNumOperators()
  {
    return activePhyOpt.size();
  }
  
  // Visualiser Compatible XML plan
  public String getXMLPlan2() throws CEPException
  {
    try {
    execContext.getPlanMgr().getLock().readLock().lock();
    StringBuilder sb = new StringBuilder();
    sb.append("<plan>\n\n");
    for ( PhyOpt p : activePhyOpt.values())
    {
      if(p == null)
        continue;
      sb.append(getXMLPlanOperator(p));
      sb.append("\n");
    }
    
    for ( PhyStore store : activePhyStore.values()) 
    {
      if(store == null)
        continue;
      sb.append(getXMLPlanStore(store));
      sb.append("\n");
    }
    
    for ( PhySynopsis syn : activePhySyn.values()) 
    {
      if(syn == null)
        continue;
      sb.append(syn.getXMLPlan2());
      sb.append("\n");
    }
    sb.append("</plan>");
    return sb.toString();
    }
    catch(CEPException ce)
    {
      throw(ce);
    }
    finally
    {
      execContext.getPlanMgr().getLock().readLock().unlock();
    }
  }
  
  //get the XML Plan for operator
  private String getXMLPlanOperator(PhyOpt op) throws CEPException{
    StringBuilder opXML = new StringBuilder();
    opXML.append("<operator id = \"");
    opXML.append(op.getId());
    opXML.append("\"");
    if (op.getIsStream()) {
      opXML.append(" stream = \"1\"");
    }
    else {
      opXML.append(" stream = \"0\"");
    }
    opXML.append(">\n");
    opXML.append(op.getXMLPlan2());
    opXML.append("</operator>\n");
    return opXML.toString();
  }
  
  //get the XML Plan for store
  private String getXMLPlanStore(PhyStore store)
  {
    //currently not ignoring any store!
    StringBuilder stXML = new StringBuilder();
    stXML.append("<store id = \"");
    stXML.append(store.getId());
    stXML.append("\">\n");
    stXML.append("<owner> ");
    stXML.append(store.getOwnOp().getId());
    stXML.append(" </owner>\n");
    stXML.append("<name> ");
    stXML.append(store.getStoreKind().getName());
    stXML.append(" </name>\n");
    stXML.append("</store>\n");
    return stXML.toString();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#instantiate(oracle.cep.phyplan.PhyOpt)
   */
  public ExecOpt instantiate(Query query, PhyOpt rootOp) throws CEPException
  {
    return CodeGenHelper.instantiate(execContext, query, rootOp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#printPlan()
   */
  public String getPlanAsString(PhyOpt opt, PhyOpt root)
  {
    StringBuffer plan = new StringBuffer();
     
    plan.append((root == null)? "\nARF# Root = "+ opt.getOptName() +" belowViewRoot="+opt.isBelowViewRootInclusive() : 
    		                        root.getOptName()+" ----> "+opt.getOptName()+" belowViewRoot="+opt.isBelowViewRootInclusive());
    if(opt.getInputs() != null)
    {
      plan.append("\nARF# ");
      if(opt.getInputs().length == 1)
        plan.append(getPlanAsString(opt.getInputs()[0], opt));
      else if(opt.getInputs().length == 2)
      {
        plan.append(getPlanAsString(opt.getInputs()[0], opt));
        plan.append("\nARF# ");
        plan.append(getPlanAsString(opt.getInputs()[1], opt));
      }
    }
    
    return plan.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#printStat()
   */
  public void printStat()
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.PlanManager#addMonitorPlan(int,
   *      oracle.cep.logplan.LogOpt, oracle.cep.interfaces.output.QueryOutput,
   *      oracle.cep.execution.scheduler.Scheduler)
   */
  public void addMonitorPlan(int monitorId, LogOpt logPlan, QueryOutput output,
      Scheduler sched)
  {

  }
  
  public List<PhyOpt> areOutputsDependentOnSynStore(PhyOpt op)
  {
    //Iterate through the outputs and check if any of them use this op's 
    //synopsis/store.
    PhyOptOutputIter iter = op.getOutputsIter();
    List<PhyOpt> outputExpectingBuffer = new LinkedList<PhyOpt>();
    if(iter != null)
    {
      PhyOpt output = null;
      do
      {
        try
        {
          output = iter.getNext();
          if((output != null) && (output.isDependentOnChildSynAndStore(op)))
          {
            // if this operator is part of a view and is the fact branch of a 
            // join don't allocate a buffer (defeats the purpose of 
            // saving memory)
            // NOTE: for special join we want to allocate buffer only on the 
            // dimension side of the join.
           if (op.isParentJoinView(op) && !op.isArchivedDim())                 
              ;
            else                 
              outputExpectingBuffer.add(output);
          }
        }
        catch(PhysicalPlanException pe){
          output = null;
        }
        
      }while(output != null);
    }
    return outputExpectingBuffer;
  }
  
  /**
   * Identify the places in phyplan which will need a buffer operator.
   * The method would be called only when q is archived relation dependent.
   * It will set a flag for those project/select/relnsrc operators
   * at least one of whose output expects a synopsis/store or when any of these
   * is a root of the query plan. 
   * In such a case the buffer operator would maintain the synopsis/store and 
   * this will ensure that project, select and relnsrc are always stateless
   * in archived relation based queries.
   * Once the query operator(s) are found, buffer operators might not be
   * needed for some of the operators. The 'isBufferingOpNeeded' flag for 
   * such operators will be reset correctly to false value during 
   * 'findQueryOperators' processing.
   * @param q Query metadata object
   * @param root Root of the local query phyplan for q.
   */
  public void findBufferOpRequirements(Query q, PhyOpt root, Integer[] refViewIds)
    throws CEPException
  {
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
    List<PhyOpt> refViewRoots= new LinkedList<PhyOpt>();
    
    //construct a list of opts which are view roots referred by this query 'q'
    if(refViewIds != null)
    {
      for(int vNum=0; vNum < refViewIds.length; vNum++)
      {
        int qid = 
          execContext.getViewMgr().getView(refViewIds[vNum]).getQueryId();
        PhyOpt vRoot = this.queryRootsMap.get(qid);
        refViewRoots.add(vRoot);
      }
    }
    
    for(DAGNode node : nodes)
    {
      PhyOpt opt = (PhyOpt) node;
      
      if(opt instanceof PhyOptRelnSrc)
      { 
        // FIXME: make sure that the relnsrc is archived
        // How to check if project or select has archived source in lineage?
        // Lineage won't be set at this time.
        PhyOptRelnSrc rel = (PhyOptRelnSrc) opt;
        try
        {
          if(!execContext.getTableMgr().getTable(rel.getRelId()).isArchived())
          {
            continue;
          }
        }
        catch(MetadataException me)
        {
          System.out.println(me.getMessage());
        }
      }
      
      //If the current operator is a relation source or project or select
      //or it is a view root (in this case it will be archived view root)
      //then we should be inserting buffer operator if the operator immediately
      //above it expects the child to maintain synopsis.
      if ((opt instanceof PhyOptRelnSrc) || (opt instanceof PhyOptProject) ||
          (opt instanceof PhyOptSelect) ||
          ((refViewRoots.size() > 0) && (refViewRoots.contains(opt))))
      {
        //The special processing is needed only in case of three operators 
        //mentioned in the 'if' condition.
        List<PhyOpt> l = areOutputsDependentOnSynStore(opt);
        //if opt is root of the query and q is not view defn query then
        //we can add buffer on top.
        //For a view defn query there is no need to add buffer on top.
        if((l.size() > 0) || ((opt == root) && (!q.isViewQuery())))
        {
          if(l.size() == 0)
          {
            //possible only when opt == root
            //add null as the only entry into the list.
            //This will be handled by createBufferOperators appropriately
            l.add(null);
          }
          LogUtil.finest(LoggerType.TRACE, "ARF# "+
                         "outputs expecting buffer set for "+opt.getOptName());
          opt.setOutputsExpectingBuffer(l);
        }
        else
          opt.setOutputsExpectingBuffer(null);
      }
      else
        opt.setOutputsExpectingBuffer(null);
    }
  }
  
  /*
   * Method sets the 'isBelowViewRootInclusive' flag correctly.
   * Initially it is false for all the operators in the local query plan.
   * Then it is set to true for all operators in the view query plan.
   */
  public void initializeFlagForOpsUpstreamToViewRoot(PhyOpt vRoot)
  {
    if(vRoot != null)
    {
      vRoot.setIsBelowViewRootInclusive(true);
      PhyOpt[] inputs = vRoot.getInputs();
      if(inputs != null)
      {
        for(int i = 0; i < inputs.length; i++)
        {
          initializeFlagForOpsUpstreamToViewRoot(inputs[i]);
        }
      }
    }
  }
  

  /**
   * State Initialization Algorithm. 
   * For every stateful operator in the query plan
   * - construct the queries to be issued against the archiver
   * - set them in the physical operator
   * @param Query q - metadata object representing the query
   * @param Phyopt root - the root of the physical plan of the query
   * @param refViewIds - the view ids on which the query q is dependent on
   * @return true if isQueryOperator flag was overwritten(from true to false) 
   * FIXME: the comment below is incomplete?
   * for some operator during the process of finding query operators. In such 
   * cases, after adding the buffer operator(s) in the query plan we need to 
   * rerun findQueryOperators. 
   */  
  public boolean findQueryOperators(Query q, PhyOpt root, Integer[] refViewIds) 
    throws CEPException
  {
    // Topologically sort the query plan
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
    boolean isQueryFlagOverwritten = false;

    // If the query is dependent on a view then we need to start the search
    // for query operator above the view root. We should not go below the view
    // root.
    if(refViewIds != null)
    {
      //Initially set the isBelowViewRootInclusive flag to false,
      //then for the operators upstream to view root operator it will
      //be set to true.
      //So only the operators upstream to the view root inlusive (those in the 
      //view query plan) would have the flag as TRUE and hence won't be 
      //considered for state initialization.
      for(DAGNode node : nodes)
      {
        PhyOpt opt = (PhyOpt) node;
        opt.setIsBelowViewRootInclusive(false);
        opt.setStateInitializationDone(false);
        opt.setCanBeShared(true);
        opt.setIsQueryOperator(false);
      }
      
      for(int vNum=0; vNum < refViewIds.length; vNum++)
      {
        int qid = 
          execContext.getViewMgr().getView(refViewIds[vNum]).getQueryId();
        PhyOpt vRoot = this.queryRootsMap.get(qid);
        initializeFlagForOpsUpstreamToViewRoot(vRoot);
      }      
    }
    else
    {
      for(DAGNode node : nodes)
      {
        // Cast to PhyOpt and set 'stateInitializationProcessingDone' flag to
        // false initially. This is needed so as to avoid doing the processing
        // for an operator for which one of the downstream operators will 
        // already be propagating tuples (using which state can be constructed)       
        PhyOpt opt = (PhyOpt) node;
        opt.setStateInitializationDone(false);
        // query is not dependent on views so set the flag to false
        opt.setIsBelowViewRootInclusive(false);
        opt.setCanBeShared(true);
        opt.setIsQueryOperator(false);
      }
    }

    /*
     * Terms:
     * 1. Stateful operator - An operator which maintains state
     *                        (i.e. maintains some sort of information derived
     *                         from the previously processed tuples)
     * 2. Query operator - An operator which queries the archiver to get state
     *                   - It may be stateful or stateless
     *                   - If stateful, it will populate it's state from the
     *                     returned results.
     *                   - It propagates the output after receiving the 
     *                     returned results to downstream operator(s)
     *
     * Invariants/Correctness arguments:
     * Assuming the query plan o be a DAG the following would hold after 
     * State Initialization Algorithm (SIA) runs-
     * 1. If an operator 'v' is identified as a Query operator by SIA, then no
     *    operator 'w' belonging to reachable set of 'v', will be a Query 
     *    operator, except when v=w.
     *    This ensures that a Query operator does not supply state to another
     *    Query operator. This is ensured by explicitly marking all operators
     *    in the reachable set of a currently identified Query operator 'v' as
     *    non-query operator irrepsective of their earlier markings.
     * 2. Every stateful operator will be covered by SIA 
     *    This can be seen from as follows -
     *    a. Either a stateful operator will itself be Query operator
     *    b. Or some operator on the path from that operator to the source 
     *       operator will be a Query operator.
     *    (b) holds because in SIA, when a stateful operator cannot become
     *    Query operator then we traverse the path mentioned in (b) to identify
     *    a Query operator.
     *    (b) holds even in case of an already identified Query operator getting
     *    unmarked due to some operator further upstream getting marked as Query
     *    operator. This is because that operator is also in the path from the
     *    stateful operator to source operator.
     */
    
    /*
     * We CANNOT quit as soon as we get the first operator which is stateful. 
     * This is because there can be another branch in the query plan for which 
     * there will be another lowest stateful operator. So we have to iterate 
     * through all operators.
     * e.g. select sum(distinct c1), sum(c3) from Sales where region='Apac' 
     *      group by prod_id
     * Here 'distinct' is the lowest stateful operator for branch 1 and 
     * 'groupaggr' for the aggregation sum(c3) is the lowest stateful operator
     * for branch 2.
     */
    boolean queryOperatorFound = false;
    for(DAGNode node : nodes)
    {
      PhyOpt opt = (PhyOpt) node;
      /*
       * 1. If the operator is above view root and state initialization is not
       *    done for it then process it.
       * 2. Else If the operator is below view root, nothing needs to be done
       * 3. Else if state initialization is done (and op is above view root)
       *    - do the processing for binary operators
       *    - also construct query for the operator wherever possible w/o
       *      marking it as a query operator.
       */      
      if((!opt.isBelowViewRootInclusive())&&(!opt.isStateInitializationDone()))
      {
        // state initialization processing is not done already
        if((opt.canConstructQuery(q)) && (opt.canBeQueryOperator()))
        {
          // The operator can construct the query and can also query
          // the archiver, so construct it's outputSQL. 
          // e.g. Aggr operator with avg can construct a query and can also
          // query the archiver.
          opt.updateArchiverQuery();
          if(opt.isStateFul())
          {
            // operator maintains state so mark it as query operator.
       	    // set isQueryOperator and stateInitializationProcessing flags for
      	    // downstream operators including itself
            isQueryFlagOverwritten = 
              isQueryFlagOverwritten 
              || setStateInitializationFlagsForDownstreamOps(opt, true);
            queryOperatorFound = true;
          }
          
          if (opt.isParentJoinView(opt) && !opt.isArchivedDim())
          {
            // On the fact branch
            //  -- disable sharing
            //  -- don't set state init flag to true (as it is never done)
            //  -- none of the operators is a query operator.
            //  -- no buffering is already taken care of in 
            //     findBufferOpRequirements
            opt.setStateInitializationDone(false);
            opt.setIsQueryOperator(false);
            // TODO: sharing is yet to be tested. But since state is going 
            // to be incomplete, most likely we cannot share
            // opt.setCanBeShared(false);
          }   
        }
        else
        { 
          if(!opt.canBeQueryOperator())
          {
            // Operator can construct query but cannot query the archiver
            // e.g. Aggr operator with max can construct a query but cannot 
            // query the archiver since it expects the child synopsis to be 
            // populated.
            // In such a case, we should construct the operator's SQL query
            // if the CQL query is a view defn query so that we get the SQL
            // of the view root. In case of logical do, the view root will
            // almost always be Join or JoinProject.
            if (q.isViewQuery())
              opt.updateArchiverQuery();
          }
          
          /*
           * Invariant:
           * By the flow of our algorithm all operators upstream to 'opt' will 
           * return TRUE for canBeQueryOperator() call on them (hence there is
           * no need to check that again)
           * AND
           * none of them will be stateful operator.
           * We don't have to go further than the child(ren) of opt as those 
           * themselves will be query operators.
           * This holds true even in the case of join optimization, because
           * we don't allocate any buffer operator on the fact side.
           */
          PhyOpt[] children = opt.getInputs();
          if(children == null)
          {  
            //A query is marked as dependent on archived relation if any one 
            //of its input sources is an archived relation.
            //If the source operator is archived then it can always construct
            //its query so code will come here only in case of non-archived
            //source operator.
            //In this case, since this cannot be query operator, no operator
            //in its reachable set could be query operator, so mark the flags
            //accordingly so that the invariant is maintained.
            isQueryFlagOverwritten = 
              isQueryFlagOverwritten ||
	            setStateInitializationFlagsForDownstreamOps(opt, false);
          }
          else if(children.length == 1)
          {
	          //Set isQueryOperator and stateInitializationProcessing flags for
            //downstream operators including itself
            isQueryFlagOverwritten = 
              isQueryFlagOverwritten ||
              setStateInitializationFlagsForDownstreamOps(children[0], true);
            queryOperatorFound = true;
          }
          else if(children.length == 2)
          {
            // June 12, 2013: added a check for isParentJoinView()
            if (opt.isArchivedDim() && opt.isParentJoinView(opt))
            {
              // This is a special join operator. For the join of a fact table
              // with the dimension table, we need to skip finding the query
              // operators on the fact side altogether.
              // Set isQueryOperator and stateInitializationProcessing flags for
              // upstream operators including itself (done above)
              // Feb 15, 2013:
              //   We need to skip this code altogether is special join
              //   Because in the case of multi-join, anything above the join
              //   will not have the state initialization done.
              //   Think
              //          |x|
              //         /   \
              //       |x|    D2
              //      /   \
              //     F     D
              //
              if (children[0].isArchivedDim() && 
                  !(children[0] instanceof PhyOptJoin || 
                    children[0] instanceof PhyOptJoinProject)) 
              {
                // don't bother if it is a "special" join
                isQueryFlagOverwritten = isQueryFlagOverwritten
                    || setStateInitializationFlagsForDownstreamOps(children[0],
                        true);
              }
              if (children[1].isArchivedDim() &&
                  !(children[1] instanceof PhyOptJoin || 
                    children[1] instanceof PhyOptJoinProject))
              {
                isQueryFlagOverwritten = isQueryFlagOverwritten
                    || setStateInitializationFlagsForDownstreamOps(children[1],
                        true);
              }              
            }
            else 
            {
              isQueryFlagOverwritten = 
                isQueryFlagOverwritten ||
                setStateInitializationFlagsForDownstreamOps(children[0], true);
              
              isQueryFlagOverwritten = 
                isQueryFlagOverwritten ||
                setStateInitializationFlagsForDownstreamOps(children[1], true);
            }
            queryOperatorFound = true;
          }
        }      
      }
      else if(!opt.isBelowViewRootInclusive())
      {
        // state initialization done flag is true for the operator and
        // it is above view root.
        // 1. If the operator is binary then 
        //    we should check if we need to set the query operator flag for 
        //    either of the sides. 
        //    If this is a view defn query then if possible we should 
        //    construct the SQL query for that operator
        // 2. If the operator is unary then
        //    If this is a view defn query then if possible we should 
        //    construct the SQL query for that operator.
        PhyOpt[] children = opt.getInputs();
        if(children != null)
        {
          if(children.length == 1)
          {
            if(q.isViewQuery())
              if(opt.canConstructQuery(q))
                opt.updateArchiverQuery();
          }
          if(children.length == 2)
          {
            // If the children has a valid sql and it can also be query
            // operator and if it has not been marked so earlier then mark it
            // as query op.
            if (((children[0].getOutputSQL() != null) && 
                (children[0].canBeQueryOperator())) && 
               (!children[0].isQueryOperator())) 
            {
              isQueryFlagOverwritten =
                isQueryFlagOverwritten ||
                setStateInitializationFlagsForDownstreamOps(children[0], true);
              queryOperatorFound = true;
            }
            if (((children[1].getOutputSQL() != null) && 
                 (children[1].canBeQueryOperator())) && 
                (!children[1].isQueryOperator()))
            {
              isQueryFlagOverwritten =
                isQueryFlagOverwritten ||
                setStateInitializationFlagsForDownstreamOps(children[1], true);
              queryOperatorFound = true;
            }
            if(q.isViewQuery())
            {
              if(opt.canConstructQuery(q))
                opt.updateArchiverQuery();
            }
          }
        }
      }
    } 
    
    // For a view defn query there is no need to mark the root as query
    // operator if none is found earlier.
    if ((!queryOperatorFound))
    {
      /* If the control reaches here, it means -
       * 1. We have gone through all operators in the DAG and none of them 
       *    could be identified as query operator.
       * 2. canBeQueryOperator() is true for all of the operators. Because
       *    if it were false for any operator, that operator's child(ren)
       *    operator(s) would have been marked as query operator.
       *    
       * So we can mark the root as query operator.
       */
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   "Setting the root "+root.getOptName() + " as query "+
                   "operator since no other query operator found ...");
      root.setIsQueryOperator(true);
      root.setStateInitializationDone(true);
    }
    
    return isQueryFlagOverwritten;
  }
  
  /**
   * Sets the initialization flag, canBeShared and isQueryOperator flags 
   * for all the downstream operators of the param operator including itself.
   * @param opt - physical operator
   * @return true - if this call resulted in overwriting an already set (true)
   * isQueryOperator flag for some operator to false.
   * e.g. distinct aggr based query can have such case.
   * select sum(distinct c1), min(c2) from R
   */
  public boolean setStateInitializationFlagsForDownstreamOps(PhyOpt opt, 
                                                             boolean flag)
    throws CEPException
  {
    boolean isQueryFlagOverwritten = false;
    //For setting the flag, do a breadth-first traversal with opt as the source
    List<PhyOpt> phyOpts = new LinkedList<PhyOpt>();
    phyOpts.add(opt);
    while(!phyOpts.isEmpty())
    {
      PhyOpt phyOp = (PhyOpt)phyOpts.remove(0);

      // FIXME: put a detailed comment here explaining the logic why the second
      // join was marked as SIA done.
      // April 19, 2013: Damn, don't remember the original reason! 
      if ((phyOp instanceof PhyOptJoin || phyOp instanceof PhyOptJoinProject) &&
          phyOp.isArchivedDim())
        phyOp.setStateInitializationDone(false);
      else
        phyOp.setStateInitializationDone(true);
      
      if(phyOp == opt) //this is the argument operator
      {          
        if (flag)
        { 
          //this is a query operator, (not some non-archived source calling 
          //this function) and if it is stateful then it cannot be shared.
          if(phyOp.isStateFul())
            phyOp.setCanBeShared(false);
        }
        else 
          //for a non-archived source we do not need a buffer operator.
          phyOp.setOutputsExpectingBuffer(null);
      }
      else 
      {
        //any operator above(downstream) query operator cannot be shared
        phyOp.setCanBeShared(false);
        //select, project or relnsrc appearing above query operator do not need
        //buffer operator as they would anyway maintain synopsis/store as
        //needed. So reset the list to null.
        phyOp.setOutputsExpectingBuffer(null);
      }
      
      //opt is the new query operator or opt is non-archived source
      //So any operator reachable from 'opt',
      //except itself, cannot be a query operator
      if(phyOp != opt)
      {
        if(phyOp.isQueryOperator())
        {
          //this was earlier marked as query operator but now will be 
          //overwritten
          assert flag : "input isQueryOperator flag should be true";
          isQueryFlagOverwritten = true;
          LogUtil.finest(LoggerType.TRACE, "ARF# "+
                       "Query operator flag overwritten to false for "
                       +phyOp.getOptName());
        }
        phyOp.setIsQueryOperator(false);
      }
      else
        phyOp.setIsQueryOperator(flag);

      PhyOptOutputIter iter = phyOp.getOutputsIter();
      if (iter != null)
      {
        PhyOpt child;  // 'child' seems like a misnomer here.
        do
        {
          child = iter.getNext();
          if (child != null)
          {
            phyOpts.add(child);
          }
        } while(child != null);
      }
    }    
    return isQueryFlagOverwritten;
  }
  
  public boolean createBufferOperators(Query q, PhyOpt root, 
                                       boolean isQueryFlagOverwritten) 
    throws CEPException
  {
    boolean atLeastOneBufferOpAdded = false;
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
    for(DAGNode node : nodes)
    {
      PhyOpt opt = (PhyOpt) node;
      if(opt.isBufferOpNeeded())
      {
        List<PhyOpt> l = opt.getOutputsExpectingBuffer();
        PhyOptOutputIter iter = opt.getOutputsIter();
	
        if(iter != null)
        {
          PhyOpt currOutput = null;
          //when opt is root of the query plan then the loop won't do anything
          do
          {
            currOutput = iter.getNext();
            //remove the output expecting buffer operator below it from the
            //outputlist of 'opt'.
            if((currOutput != null) && (l.contains(currOutput)))
	    {
              iter.remove();
	    }
          } while(currOutput != null);
        }
        
        for(PhyOpt out : l)
        {
          //insert a buffer operator between 'opt' and 'out'.
          //The constructor will set 'opt' as input of 'bufferOp' and also add
          //'bufferOp' in the output list of 'opt'.
          //it will also add 'out' to the output list of 'bufferOp' and also set
          //'bufferOp' as input for 'out'.
          //If isQueryFlagOverwritten is true then we will be calling 
          //findQueryOperators again after this function. So we do not need
          //to initialize the archiver related fields in PhyOptBuffer
          PhyOptBuffer bufferOp = new PhyOptBuffer(opt, out, q.getId(),
                                                   !isQueryFlagOverwritten);
          atLeastOneBufferOpAdded = true;
          
          LogUtil.finer(LoggerType.TRACE, "ARF# "+
                       "Buffer operator "+bufferOp.getOptName()+" added in "+
                       "between input="+opt.getOptName()+" and output="+
                       ((out == null) ? "root": out.getOptName()));
        }
        //since buffer operators will query, input should not
        //also maintain the invariant that the operators below query operator
        //would have stateInitializationDone flag set to false.
        if(opt.isQueryOperator())
        {
          opt.setIsQueryOperator(false);
          opt.setStateInitializationDone(false);
        }
      }
    }

    return atLeastOneBufferOpAdded;
  }
  
  /**
   * Propagate All The Relations to the newly added Query
   * 
   * @param query
   *             New added Query
   */
  public void propRelns(Query query)
  {
    int queryId = query.getId();
    PhyOpt root = getQueryRootOpt(queryId);
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);

    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      ExecOpt execOp = opt.getInstOp();
      if(execOp.propagationReqd())
      {
        assert execOp != null;

        // Before doing anything else, the relation operator needs to propagate
        // the current messages
        execOp.propagateOldData();
      }
    }
  }

  /**
   * Appends a query output dynamically to the existing running query
   * 
   * @param qry 
   *          Query
   * @param output
   *           New Output to be added to the query
   */
  public void addQueryNewOutput(Query qry, Destination output)
     throws CEPException
  {
    int qryId = qry.getId();

    // First find the query root operator
    PhyOpt phyOp = getQueryRootOpt(qryId);
    
    PhyOpt outOp = new PhyOptOutput(execContext, phyOp, qryId, 
                                    output, qry.getIsPrimaryKeyExist());

    // Perform add_aux_structures
    add_aux_structures(qry, outOp);
    // Perform instantiate
    instantiate(qry, outOp);

    // Add the output to the scheduler and ExecManager's list
    outOp.addOp();

    // add queryid to output mapping
    ArrayList<PhyOpt> phyList = queryOutputs.get(qryId);
    if(phyList == null)
    {
      phyList = new ArrayList<PhyOpt>();
      queryOutputs.put(qryId, phyList);
    }
    phyList.add(outOp);
  }
  

  public void addQueryOutputs(Query qry, List<Destination> outputs)
      throws CEPException
  {
    int qryId = qry.getId();

    // First find the query root operator
    PhyOpt phyOp = getQueryRootOpt(qryId);
    
    ArrayList<PhyOpt> phyList = queryOutputs.get(qryId);
    if(phyList == null)
    {
      phyList = new ArrayList<PhyOpt>();
      queryOutputs.put(qryId, phyList);
    }
    Iterator<Destination> iter = outputs.iterator();

    while (iter.hasNext())
    {
      // Create the physical representation of the Output operator
      Destination output = iter.next();
      PhyOpt outOp = new PhyOptOutput(execContext, phyOp, qryId,
                                      output, qry.getIsPrimaryKeyExist());

      // Perform add_aux_structures
      add_aux_structures(qry, outOp);
      // Perform instantiate
      instantiate(qry, outOp);

      // Add the output to the scheduler and ExecManager's list
      outOp.addOp();

      // add queryid to output mapping
      phyList.add(outOp);
    }
    
  }

  public void dropQueryOutput(int qryId, PhyOpt output) throws CEPException
  {
    assert isQueryOutputPresent(qryId, output);
    // Remove queryoutput subscribed
    assert output instanceof PhyOptOutput;
    execContext.getExecMgr().removeQueryOutput(qryId, ((PhyOptOutput)output).getEpr() );

    List<PhyOpt> outputs  = queryOutputs.get(qryId);
    Iterator<PhyOpt> iter = outputs.iterator();
    while (iter.hasNext())
    {
      PhyOpt nxt = iter.next();
      if(nxt == output)
      {
        iter.remove();
        if (outputs.size() == 0)
          queryOutputs.remove(qryId);

        return;
      }
    }
  }

  public void dropSourceOp(int id, PhyOpt op) throws CEPException
  {
    assert isSourceOperatorPresent(id, op);
    if (sourceOps.remove(id) == null)
      unorderedSourceOps.remove(id);
  }

  /** Vistitor stuff */
  public void accept(IPlanVisitor visitor)
  {
    for ( PhyOpt p : activePhyOpt.values())
    {
      if (p == null) continue;
      p.accept(visitor);
    }    
  }
  
  /** ChgNotifier stuff */
  public void addNotifier(IPlanChgNotifier notifier)
  {
    chgNotifier = notifier;
  }
  
  private void notifyAddQuery(int qryId, PhyOpt opt)
  {
    try {
      if (chgNotifier != null)
        chgNotifier.addQueryRoot(qryId, opt);
    } catch(Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      // eats up any exception
    }
  }

  private void notifyRemoveQuery(int qryId, PhyOpt opt)
  {
    try {
      if (chgNotifier != null)
        chgNotifier.removeQueryRoot(qryId, opt);
    } catch(Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      // eats up any exception
    }
  }
  
  /**
   * @param queryStatsEnabled
   *                  whether query stats are enabled/disabled
   * @param isBaseTimelineMillisecond
   *                  whether millisecond/nanosecond
   * @param queryId
   *              the Id of the query
   */
  public void setQueryStats(boolean queryStatsEnabled, 
                    boolean isBaseTimelineMillisecond, int queryId)
  {
    List<PhyOpt> list = getAllQueryOutputs(queryId);
    if(list != null && !list.isEmpty())
      {
        Iterator<PhyOpt> iter = list.iterator();
        Output opt;
        while(iter.hasNext())
        {
          opt = (Output)iter.next().getInstOp();
          opt.setIsStatsEnabled(queryStatsEnabled, isBaseTimelineMillisecond);
        }
      }
  }//end of setQueryStats()
  
  /**
   *
   * @param tableStatsEnabled
   *            whether query stats are enabled/disabled
   * @param isBaseTimelineMillisecond
   *              whether millisecond/nanosecond
   * @param tableId
   *              the id of the table
   */
  public void setTableStats(boolean tableStatsEnabled, 
                  boolean isBaseTimelineMillisecond, int tableId)
  {
    // set the flag for execution operator
    PhyOpt op = getSourceOpt(tableId);
    if (op != null && op.getInstOp() != null)
    {
      ((StreamSource)op.getInstOp()).setIsStatsEnabled(tableStatsEnabled, isBaseTimelineMillisecond);
    }
    
    op = getUnorderedSourceOpt(tableId);
    if (op != null && op.getInstOp() != null)
    {
      ((StreamSource)op.getInstOp()).setIsStatsEnabled(tableStatsEnabled, isBaseTimelineMillisecond);
    }
  }//end of setTableStats()
   
  public void dropTableFunctionOperator(PhyOpt phyopt)
  {
    if(phyopt!= null)
    {
      if(tableFunctionPhyOptList != null)
      {
        List<PhyOpt> phyOptList 
          = tableFunctionPhyOptList.get(phyopt.getSharingHash());
        if(phyOptList != null)
        {
          phyOptList.remove(phyopt);
        }
      }
    }
  }
  
  /** 
   * Parse the operator tree to process heartbeat timeout flag
   * @param phyopt
   */
  public void processRequireHbtTimeOut(PhyOpt phyopt)
  {
    if(phyopt.getIsSource())
      return;
    
    Collection<PhyOpt> sysTsSources = phyopt.getSystsSourceLineage();
    // There are no system timestamped input sources
    if(sysTsSources == null || sysTsSources.isEmpty())
      return;
    
    // If current operator needs heartbeat timeout, then set their input
    // operator's flag to true
    // Otherwise go to child operators and check
    if(phyopt.isHbtTimeoutRequired())
    {
      Iterator<PhyOpt> iter = sysTsSources.iterator();
      while(iter.hasNext())
      {
        PhyOpt op = iter.next();
        op.setHbtTimeoutRequired(true);
        op.addToTimeOutOpList(phyopt);
      }
    }
    
    for(int i = 0; i < phyopt.getNumInputs(); i++)
      processRequireHbtTimeOut(phyopt.getInputs()[i]);    
  }
  
  /**
   * Update the hbtTimeoutList of Scheduler manager
   * @param op
   * @param isAdd
   */
  public void updateHbtTimeoutList(PhyOpt op, boolean isAdd)
  {
    ExecOpt e = op.getInstOp();
    ExecSourceOpt sop = (ExecSourceOpt)e;
    
    if(e != null)
    {
      boolean isExplicitTimeOut = false;
      try
      {
        isExplicitTimeOut = 
          execContext.getTableMgr().getTable(sop.getStreamId()).isExplicitTimeout();
      }
      catch(MetadataException me)
      {
         LogUtil.fine(LoggerType.TRACE,"Unable to get timeout while updating "+
           " timeout list of scheduler because "+ sop.getStreamId()+ 
             " is not a valid table "); 
      }
      
      if(!isExplicitTimeOut)
      {
        long newTimeOutDuration = -1;
        if(isAdd)
        {
          newTimeOutDuration 
            = oracle.cep.common.Constants.DEFAULT_HBT_TIMEOUT_NANOS;
        }
        sop.setTimeoutDuration(newTimeOutDuration);
        LogUtil.fine(LoggerType.TRACE, "Setting new timeout of operator " + 
            e.getOptName() + " by new value " + newTimeOutDuration);
        execContext.getSchedMgr().updateHbtTimeOutList(e, isAdd);
      }
    }
  }

  /**
   * Go through the query plan and Propagate tuples obtained from archiver
   * as applicable.
   * @param query
   * @throws CEPException
   */
  public void propagateArchivedRelationTuples(Query query) throws CEPException
  {
    long tempTime = System.currentTimeMillis();
    int queryId = query.getId();
    PhyOpt root = getQueryRootOpt(queryId);
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);

    for (DAGNode op : nodes) 
    {
      PhyOpt opt = (PhyOpt) op;
      ExecOpt execOp = opt.getInstOp();
           
      if(execOp != null)
      {
        if (execOp.getArchivedRelationTuples() != null) 
        {
          execOp.initializeState();
        }
      }
    }
    
    // #16196625: 
    // We are done propagating the archiver tuples so our purpose of setting
    // the state to ARCHIVED_SIA_STARTED (to avoid request for heartbeats by
    // binary operators during query start) is served.
    // So now we set the state to ARCHIVED_SIA_DONE so that heartbeat requests
    // can be sent while processing streaming data. (data after query start).
    // Operators below and including the view root have already undergone SIA
    // (when view query started) so their propState is set already correctly
    // set. So we just have to look for operators downstream.
    for (DAGNode op : nodes)
    {
      PhyOpt opt = (PhyOpt) op;
      if(!opt.isBelowViewRootInclusive())
        opt.getInstOp().setPropStateToSIADone();
    }
    
    //
    // There was a problem with heartbeat and system timestamp that was
    // fixed as part of 16196625.
    // However in the special join case we don't have any initialization
    // happening on the "fact" side of the join. But we still need to 
    // enqueue the same heartbeat as the one on the dimension side (which
    // does do the initialization.
    // Not doing so will trigger timestamp out-of-order exceptions.
    // 
    // TODO: add why it is not a problem with application timestamp?
    
    // Bug# 16765352, 16766272: 
    // This processing should be called only when starting the view query.
    // Otherwise it can cause OOT exception when a query over already
    // started view is started as hb of old time (when view query is started)
    // is sent on the FACT side..
   
    if(query.isViewQuery())
    {
      for (DAGNode op : nodes)     
      {
        PhyOpt opt = (PhyOpt) op;
        if (opt.isParentJoinView(opt) && !opt.isArchivedDim())
        {
          // fact side of the branch, see if immediate parent is a join
          // if yes, then get the heartbeat ts from the other side (must be
          // dimension side)
          PhyOptOutputIter iter = opt.getOutputsIter();      
          for (int i=0; i < opt.getNumOutputs(); i++)
          {
            PhyOpt op2 = iter.getNext();
            if ((op2 instanceof PhyOptJoin || op2 instanceof PhyOptJoinProject))
            {
              // do it only for dimension join
              if (op2.isArchivedDim())
              {
                // as this point the queues are not set up, set them up now.
                // This seems like a HACK, but let's see if it works or not.            
                opt.getInstOp().setArchiverReaders(
                    execContext.getQueryMgr().getArchiverResultReaders(opt, 
                        queryId));
                PhyOpt[] children = op2.getInputs();
                if (children[0] != opt) // the OTHER side
                {
                  opt.getInstOp().enqueueHeartbeat(
                      children[0].getInstOp().getHeartbeatTime());
                }
                else if (children[1] != opt)
                {
                  opt.getInstOp().enqueueHeartbeat(
                      children[1].getInstOp().getHeartbeatTime());             
                }           
              }
            }
          }     
        }
      }
    }
    
    LogUtil.finer(LoggerType.TRACE, "ARF# Set the propState to ARCHIVED_SIA_DONE");
    long propagationTime = System.currentTimeMillis() - tempTime;
    query.getArchiverStats().setSnapshotPropagationTime(propagationTime);
  }
  
  synchronized public void addSnapshot(Snapshot s)
  {
    snapshotList.add(s);
  }
  
  synchronized public long findSnapshotId(long workerId, long txnId)
  {
    for(ListIterator<Snapshot> itr = snapshotList.listIterator(); itr.hasNext();)
    {
      Snapshot s = itr.next();
      if(s.hasSnapshotInfo())
      {
        if(s.isAccountedForInSnapshot(workerId, txnId))
          return s.getSnapshotId();
        //the above stmt could result in deletion of workerid,txnid mapping
        //so if there is no further snapshot information then remove 's'
        //from snapshotList
        if(!s.hasSnapshotInfo())
          itr.remove();
      }
    }
    //we didn't get a snapshot that accounts for <workerId, txnId>
    //so it means the tuple should be forwarded to the entire query plan
    //hence return currentsnapshotid (currentsnapshotid would be last 
    //assigned snapshotid + 1).
    return getCurrentSnapshotId();
  }
    
  // Before we do the stuff related to state initialization like finding query
  // operators, inserting buffer operators it is useful to detect certain
  // cases for which the join should not kick in.
  //
  // Specifically:
  //   1. Standalone queries
  //   2. View definition which does not comply with supported join order.
  //      (see more comments inside the function)
  public void clearDimensionflags(Query q, PhyOpt root, Integer[] refViewIds) 
         throws CEPException
  {
    if (!q.isDependentOnArchivedRelation())
      return;
    
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
    
    // check if the query itself is view query
    if (q.isViewQuery())
    {
      boolean enabledimjoin = false;
      //
      // We support only the following topology (join order)
      //    f x d1 x d2 x d3 (where di is a dimension table)
      //
      // This is checked in LogplanTransforHelper. However there we don't
      // check for whether the plan is part of the view definition or not.
      // Also make a check here that the joins we encounter after the first
      // one which is a special join are all special joins.
      //          |x|
      //         /   \
      //      |x|     D2 (if not dim, reset the whole thing)
      //     /   \
      //    F     D1
      // 
      // Why:
      // Case #1:
      //    F X D X ND, where ND is a non-"dimension" (i.e. volatile) table
      // Now here F X D is a special join, hence it will not maintain its 
      // output synopsis. This implies that the for the join (F X D) X ND 
      // the left side has incomplete state. Now suppose say that a change 
      // comes to ND. At this point there could be matching rows on the left 
      // but since we did not maintan the state, the query cannot progress. 
      // The options are to either do nothing (incorrect results) or throw an
      // exception (but the user did not mark it as "dimension" and ND is a
      // volatile table so throwing exception does not make sense either)
      // 
      // Case #2:
      //    F X ND X D
      // Here the first join is not a special join and will be fully 
      // materialized. Now when (F X ND) X D is detected, the left side is a 
      // fact and right side is a dimension, so left should not be materialized,
      // but it already is.
      // 
      // In a nutshell it is not possible to support arbitrary combination 
      // of dimension and non-dimension tables.
      // 
      // Conclusion: 
      // We cannot do special processing in a join where there are more than
      // on volatile tables.
      //
      // IMPORTANT: Topological sort ensures that we will visit joins in the order
      // they were specificed in the query.      
      for (DAGNode op : nodes) 
      {        
        PhyOpt opt = (PhyOpt) op;
        if (opt instanceof PhyOptJoin || opt instanceof PhyOptJoinProject) {
          if (opt.isArchivedDim())
          {           
            enabledimjoin = true;
            LogUtil.fine(LoggerType.TRACE, 
                         opt.getOptName() + " is a special join operator");   
          }
          else
          {
            enabledimjoin = false; // this is necessary and sufficient.
            LogUtil.fine(LoggerType.TRACE, 
                         opt.getOptName() + " is not a special join operator");   
          }
        }
      }
      
      if (!enabledimjoin)
      {
        // clear all flags
        for (DAGNode op : nodes)
        {
          PhyOpt opt = (PhyOpt) op;         
          opt.setArchivedDim(false);  
        }
        
        LogUtil.fine(LoggerType.TRACE, 
                     "Archived view definition does not comply with special" + 
                     " join topology. Special join processing is disabled.");        
      }     
    }
    else 
    {
      if (refViewIds != null)
      {
        // not a view query but may have dependent views, i.e. query on top 
        // of the logical data object.
        // Nothing to do here about clearing the flags.
      }
      else
      {
        // free standing query, clear all flags
        // we should NOT do the optimization in that case.
        for (DAGNode op : nodes) 
        {
          PhyOpt opt = (PhyOpt) op;
          opt.setArchivedDim(false);
        }
        LogUtil.fine(LoggerType.TRACE, 
                     q.getName() + " is not a (archived) view query." + 
                     " Special join processing is disabled.");        

      }
    }
  }
  
  
  
}
