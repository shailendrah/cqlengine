/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/QueryManager.java /main/95 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares QueryManager in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  09/23/14 - support for partitioned stream
 udeshmuk  07/08/13 - add stats and fix logging for ARF - stress testing plan
 vikshukl  06/13/13 - clear dim flag if not a view query
 vikshukl  02/19/13 - make getArchiverResultReaders public
 udeshmuk  01/24/13 - set archived_sia_started flag
 vikshukl  01/22/13 - remove unused imports
 udeshmuk  01/14/13 - call closeResult on IArchiver
 udeshmuk  11/07/12 - send viewids to findBufferOpRequirements
 vikshukl  10/10/12 - clear dimension if not view
 pkali     08/15/12 - released the created DynamicTypes
 udeshmuk  08/08/12 - enable config params use for BI mode as well
 udeshmuk  08/08/12 - set snapshottime for stream source case
 udeshmuk  08/03/12 - enable state initialization for a view query as well
 udeshmuk  07/17/12 - use configmanager api for beam_transaction_context
 udeshmuk  07/17/12 - add params for setting the DO name corresponding to
                      BEAM_TRANSACTION_CONTEXT and for transaction_cid and
                      transaction_tid columns
 udeshmuk  07/06/12 - call a routine to identify the operators in the local plan
                      where buffer op should be added
 udeshmuk  06/25/12 - use wlevs.home as property
 udeshmuk  06/23/12 - use WLEVS_HOME instead of ADE_VIEW_ROOT
 udeshmuk  06/21/12 - fix for the decimal column in archived relation
 udeshmuk  05/21/12 - restructure archiver querying framework
 udeshmuk  05/16/12 - add query metadata as arg to shareOperators
 udeshmuk  04/16/12 - allow sharing
 sbishnoi  03/01/12 - fix 13774367
 vikshukl  09/27/11 - remove unused imports
 vikshukl  08/24/11 - subquery support
 udeshmuk  06/30/11 - enable sharing only if the query is NOT dependent on
                      archived relation
 anasrini  07/01/11 - add onlyValidate flag to compile
 udeshmuk  05/12/11 - support for alter query start time DDL
 udeshmuk  03/23/11 - archived reln - state initialization algo
 anasrini  04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 anasrini  03/19/11 - partition parallel
 sborah    03/17/11 - set ordering constraint expr
 anasrini  03/16/11 - partition parallelism
 sborah    03/15/11 - add query ordering constraint
 udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  09/01/10 - add propagateHeartbeat while adding named query
                      destination
 sborah    06/16/10 - ordering constraint
 sbishnoi  04/26/10 - generate auto heartbeat timeout
 parujain  09/24/09 - dependency support
 sbishnoi  08/25/09 - support for batch output syntax
 sborah    03/26/09 - remove garbage code in instantiate()
 parujain  02/09/09 - drop output operator
 parujain  01/28/09 - transaction mgmt
 parujain  01/07/09 - redesign force stop/drop
 hopark    12/30/08 - change getOutTypes to take null context
 parujain  12/30/08 - validateQuery handle references
 skmishra  12/26/08 - adding validate
 sborah    12/30/08 - add outQueryExprs()
 parujain  11/24/08 - redesign drop
 hopark    12/02/08 - move LogLevelManager to ExecContext
 sborah    11/24/08 - support for altering base timeline
 parujain  11/21/08 - handle constants
 sborah    11/13/08 - fix stopQueryForce()
 hopark    11/06/08 - lazy seeding
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 parujain  09/12/08 - multiple schema support
 hopark    09/17/08 - support schema
 parujain  09/04/08 - offset info
 sbishnoi  07/23/08 - modify validateReturnTs to allow TIMESTAMP as valid
                      datatype
 hopark    06/18/08 - logging refactor
 parujain  04/24/08 - fix lock problem
 hopark    03/17/08 - config reorg
 parujain  04/24/08 - fix lock problem
 hopark    03/17/08 - config reorg
 parujain  04/24/08 - fix lock problem
 parujain  05/08/08 - fix lock problem when exception is thrown
 parujain  05/07/08 - fix problems
 parujain  04/24/08 - fix lock problem
 mthatte   04/08/08 - validate ts return type
 parujain  04/01/08 - get all functionids
 mthatte   02/26/08 - parametrizing metadata errors
 parujain  02/07/08 - parameterizing error
 hopark    02/05/08 - fix dump level
 hopark    01/07/08 - add logging
 parujain  01/02/08 - fix bug
 parujain  11/09/07 - external source
 sbishnoi  11/05/07 - support for update semantics
 mthatte   10/26/07 - adding boolean flag for onDemand reln
 mthatte   09/10/07 - 
 najain    07/09/07 - cleanup
 hopark    06/27/07 - add query change notifier
 parujain  06/21/07 - fix drop query
 sbishnoi  06/07/07 - fix xlint warning
 parujain  05/18/07 - remove queryid from View
 parujain  05/09/07 - enable/disable stats monitoring
 parujain  04/27/07 - get Query stats
 parujain  04/13/07 - runtime exception handling
 parujain  04/09/07 - get PlanManager lock
 hopark    03/21/07 - storage re-org
 parujain  03/19/07 - drop window
 parujain  02/28/07 - stop query
 parujain  02/08/07 - system startup
 parujain  01/31/07 - drop function
 parujain  02/01/07 - startup with BDB
 parujain  01/11/07 - BDB integration
 rkomurav  01/13/07 - call logopt.tostring
 parujain  12/15/06 - operator sharing
 parujain  12/13/06 - instantiation of output
 parujain  12/06/06 - propagating relation
 hopark    12/06/06 - move logplan, phyplan in addQuery
 parujain  11/27/06 - Locks mgmt during drop Query
 hopark    11/17/06 - bug 5583899 : removed input/outputs from ExecOpt
 rkomurav  11/15/06 - fix to catch the return value from optimizePlan
 dlenkov   11/16/06 - removed println (somebody forgot to remove it)
 najain    11/10/06 - merge root if possible
 parujain  10/26/06 - Startup initialization
 najain    10/25/06 - integrate mds
 najain    10/24/06 - integrate with mds
 parujain  10/24/06 - startup handling
 parujain  10/23/06 - DDL support in MDS
 parujain  09/13/06 - MDS Integration
 dlenkov   09/07/06 - fix query references
 parujain  09/05/06 - bug 5461058
 najain    08/31/06 - add name
 rkomurav  08/23/06 - add getXMLPlan2
 dlenkov   08/18/06 - support for named queries
 parujain  08/18/06 - support views with same query
 rkomurav  08/18/06 - xmldump
 parujain  08/16/06 - set semquery null after compile
 parujain  07/14/06 - check locks 
 parujain  06/27/06 - metadata cleanup 
 najain    06/20/06 - add dropQuery 
 najain    06/10/06 - remove stopQuery 
 najain    06/06/06 - pass Query in addQueryOutputs 
 najain    06/04/06 - add list of relations 
 najain    05/31/06 - read/write locks 
 najain    05/17/06 - view support
 najain    05/09/06 - compile breakup 
 ayalaman  04/02/06 - use QueryOutput for query output 
 ayalaman  03/31/06 - server implementation to meet OC4J needs 
 najain    04/06/06 - cleanup
 najain    04/03/06 - cleanup
 najain    03/30/06 - add addDestination 
 najain    03/22/06 - misc
 skaluska  03/16/06 - implementation
 skaluska  03/15/06 - Creation
 skaluska  03/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/QueryManager.java /main/95 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.OrderingKind;
import oracle.cep.common.SQLType;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.Distinct;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogPlanGen;
import oracle.cep.logplan.LogPlanParallelismHelper;
import oracle.cep.logplan.LogicalPlanException;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheKey;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.Parser;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptBuffer;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptOutput;
import oracle.cep.phyplan.PhyOptOutputIter;
import oracle.cep.phyplan.PhyOptStrmSrc;
import oracle.cep.phyplan.PhyOptValueWin;
import oracle.cep.phyplan.PhyPlanGen;
import oracle.cep.phyplan.PhyPlanGenContext;
import oracle.cep.planmgr.Snapshot;
import oracle.cep.planmgr.codegen.CodeGenHelper;
import oracle.cep.planmgr.optimizer.OptTransform;
import oracle.cep.semantic.Expr;
import oracle.cep.semantic.SemQuery;
import oracle.cep.semantic.SemanticInterpreter;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IArchiverFinder;
import oracle.cep.service.IQueryChgListener;
import oracle.cep.storage.IStorageContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.util.DAGHelper;
import oracle.cep.util.DAGNode;

/**
 * QueryManager is the central point for execution of CEP commands.
 *
 * @author skaluska
 */
@DumpDesc(evPinLevel=LogLevel.MQUERY_ARG,
          dumpLevel=LogLevel.MQUERY_INFO,
          verboseDumpLevel=LogLevel.MQUERY_LOCKINFO)
public class QueryManager extends CacheObjectManager
  implements ILoggable
{
  ExecContext execContext;
  
  /**
   * Constructor for QueryManager. The constructor has been kept private
   * intentionally so that no-one can create a new instance of a QueryManager.
   * This way, only a single instance of the QueryManager is present, and it can
   * be accessed globally via QueryManager.getQueryManager().
   */
  public QueryManager(ExecContext ec, Cache cache)
  {
    super(ec.getServiceManager(), cache);
    execContext = ec;
    parser = new Parser();
    sem = new SemanticInterpreter();
    log = new LogPlanGen();
    phyplan = new PhyPlanGen();
    queryChgListeners = new CopyOnWriteArrayList<IQueryChgListener>();
  }

  public void init(ConfigManager cfg)
  {
  }

  /** parser */
  private Parser              parser;

  /** semantic interpreter */
  private SemanticInterpreter sem;

  /** logical plan generator */
  private LogPlanGen          log;

  /** physical plan generator */
  private PhyPlanGen          phyplan;

  /** 
   *  Multiple notifiers are supported, one always being the PlanManager.
   */
  private List<IQueryChgListener>   queryChgListeners;

  /**
   * Object to collect statistics associated with starting of query
   */
  private ArchiverStats archiverStats = null;
  
  /**
   * Getter for parser in QueryManager
   * 
   * @return The parser.
   */
  public Parser getParser()
  {
    return parser;
  }
  
  /** 
   * returns a handle to logical plan generator. 
   * Currently used in LogPlanFromHelper for subquery plan generation
   * @return LogPlanGen
   */
  public LogPlanGen getLog() {
    return log;
  }

  private int[] getReferencedQueryIds(Query q) throws MetadataException
  {
    Integer[] refViewIds;
  //  Iterator<Integer> iter;
    int[] qids;
    int vid;
    int i = 0;
    View view;

    // Get the referenced views
    refViewIds = execContext.getDependencyMgr().
                             getMasters(q.getId(), DependencyType.VIEW);
  
    if(refViewIds != null)
    {
      qids = new int[refViewIds.length];
      for(int j=0; j<refViewIds.length; j++)
      {
        vid = refViewIds[j].intValue();
        // Get the view object
        view = execContext.getViewMgr().getView(vid);
        qids[i++] = view.getQueryId();
      }
      return qids;
    }
    return (new int[0]);
  }

  public void startNamedQuery( String qName, String schema) throws CEPException
  {
    int qId;

    qId = findQuery(qName, schema);
    
    startQuery(qId, false);
   
    notifyStartQuery(qName, qId);
  }

  public void startQuery( int id) throws CEPException
  {
    startQuery(id, false);
   
    notifyStartQuery(null, id);
  }
  
 
  /**
   * This method is currently in QueryManager but later on 
   * will be moved from this place. This method is doing the
   * startup when the system goes down and then comes up again
   * When the system comes up again and the queries are already
   * registered, we don't need to re-register them. Simply restart
   * the queries by calling restartQuery()
   * We are basically looping around all the existing queries in MDS
   * and restart them.
   *
   */
  public void startup(String schemaName) throws CEPException
  {
    if (storage == null)
      return;

    IStorageContext cursor = intialize(schemaName);
    //load the cache with all the queries and make state as CREATED
    loadCache(cursor);
    
    cursor = intialize(schemaName);
    CacheKey qKey = getNextQuery(cursor);
    // No need to do anything if list is empty
    if(qKey == null)
      return;
  
    ITransaction txn ;
  
    while( qKey != null)
    {
      
      // Extract schema from qname
      String schema = null;
      String qname = null;
      txn = execContext.getTransactionMgr().begin();
      execContext.setTransaction(txn);
      try {
          // Extract the schema and qname
          qname = qKey.getObjectName().toString();
          schema = qKey.getSchema();
          restartQuery(txn, qname, schema);
          qKey = getNextQuery(cursor);
          txn.commit(execContext);
      }
      catch(CEPException e)
      {
        txn.rollback(execContext);
        throw e;
      }
      finally
      {
        execContext.setTransaction(null);
      }
    }
  }
  
  /**
   * Validates that this query returns an int OR a long OR a timestamp value
   */
  public boolean validateReturnsTS(int qryId) throws MetadataException 
  {
    //This can only be called for a derived timestamp dummy query
    Query q;
    try
    {
      q = getQuery(qryId);
    }
    catch(MetadataException m)
    {
      throw new MetadataException(MetadataError.INVALID_DERIVED_TIMESTAMP);
    }
    
    // Expr arraylist to collect select list expression of dummy query
    ArrayList<Expr> e;
    e = q.getSemQuery().getSelectListExprs();
    
    /* there should be only one expression to evaluate timestamp */     
    assert q.getSemQuery().getSelectListSize() == 1;

    
    Expr exp = e.get(0);
    if(exp.getReturnType()==Datatype.BIGINT 
      || exp.getReturnType() == Datatype.INT
      || exp.getReturnType() == Datatype.TIMESTAMP)
    {
      return true;
    }
    
    return false;
  }
  
  private void loadCache(IStorageContext cursor) throws MetadataException
  {// Get the key
    CacheKey qkey = getNextQuery(cursor);
    CacheLock l = null;
    
    if(qkey == null)
      return;
    
    ITransaction txn = null;
    try {
      while(qkey != null)
      {
        
        txn = execContext.getTransactionMgr().begin();
        execContext.setTransaction(txn);
        String qname = null;
        String schema = null;
        try 
        {
          // Extract the schema and qname
          
          qname = qkey.getObjectName().toString();
          schema = qkey.getSchema();
          l = findCache(txn, new Descriptor(qname,
                        CacheObjectType.QUERY, schema, null),true);
      
          if (l == null)
          {
            throw new MetadataException(MetadataError.INVALID_QUERY_OPERATION, new Object[]{"load query"});
          }
          
          // Initialize
          Query query = (Query) l.getObj();
          query.setState(QueryState.CREATE);
          execContext.getTransactionMgr().commit(txn);
        }
        catch(MetadataException e)
        {
          execContext.getTransactionMgr().rollback(txn);
          
          throw e;
        }
        finally{
          execContext.setTransaction(null);
        }
        
        qkey = getNextQuery(cursor);
      }
    }
    catch (MetadataException e)
    {
      throw (e);
    }
    
  }
  
  /**
   * Start running the query
   * @param id
   *          Query id
   * @param force
   *          start the query always
   * 
   * @throws CEPException
   */
  public void startQuery(int id, boolean force) 
  throws CEPException
  {
    long tempTime = System.currentTimeMillis();
    archiverStats  = new ArchiverStats();
    
    LogLevelManager.trace(LogArea.METADATA_QUERY, LogEvent.MQUERY_START, this, id, force);
    
    CacheLock l = null;
    Query q;
    ITransaction m_txn = execContext.getTransaction();

    try
    {
      execContext.getPlanMgr().getLock().writeLock().lock();

      l = findCache(m_txn, id, true, CacheObjectType.QUERY);
      
      if(l == null)
        throw new MetadataException(MetadataError.INVALID_QUERY_IDENTIFIER,new Object[]{id});
 
      
      q = (Query) l.getObj();
      q.setArchiverStats(archiverStats);
      
      if (!force && (q.getExtDests().size() == 0))
        throw new MetadataException(MetadataError.NO_QUERY_DESTINATION, new Object[]{q.getName()});

      // Is the query already running ?
      if (q.getState() != QueryState.RUN)
      {
        // Get all masters of type VIEW whom query is dependent
        Integer[] refViewIds = execContext.getDependencyMgr().
                                   getMasters(q.getId(), DependencyType.VIEW);
        if(refViewIds != null)
        {
          // start all the referenced views(queries)
          for(int k=0; k<refViewIds.length; k++)
          {
            // Start view only when -
            // (1) This is an internal DDL e.g. Exchange operator DDL
            // OR
            // (2) Query is not dependent on partition stream
            // OR
            // (3) Query is dependent on partn stream but the view on which it is
            //  based is dependent on partn stream
            if((execContext.isInternalDDL()) || (!q.isDependentOnPartnStream()) 
               || (execContext.getViewMgr().getView(refViewIds[k]).isPartitioned()))
            {
              execContext.getViewMgr().startView(refViewIds[k].intValue(), true);
            }
          }
          
          // A view's system ordering constraint may have changed, there we need to reset
          //  the plan and make sure the ordering constraint is validated considering the view.
          q.setSemQuery(null);
          q.setLogPlan(null);
        }

        // instantiate the query in plan manager.
        instantiate(q);

        // If the system determined ordering constraint is PARTITION_ORDER
        // then skip processing the query destination and relation
        // propagation here
        if(q.getSystemOrderingConstraint() == OrderingKind.PARTITION_ORDERED) 
        {
          // Set new state
          q.setState(QueryState.RUN);
          q.setDesiredState(QueryState.RUN);
          return;
        }
        
        // Add the outputs
        if (q.getExtDests().size() != 0)
          execContext.getPlanMgr().addQueryOutputs(q, q.getExtDests());
        
        //initialize the query operator states if query is based on archived 
        //relation. In this case we don't need relation propagation.
        if(q.isDependentOnArchivedRelation())
        {
          initializeOperatorStates(q);
          // After propagating the archived relation tuples
          // also set the propagation state to SIA_DONE.
          execContext.getPlanMgr().propagateArchivedRelationTuples(q);
        }
        else
          // Propogate all the relations as of this time
          execContext.getPlanMgr().propRelns(q);

        // Set new state
        q.setState(QueryState.RUN);
        q.setDesiredState(QueryState.RUN);
      }
     
      long totalTime = System.currentTimeMillis() - tempTime;
      q.getArchiverStats().setTotalStartTime(totalTime);
    }
    finally
    {
      execContext.getPlanMgr().getLock().writeLock().unlock();
    }
  }

  /**
   * Initialize the state of the operators by querying the archiver.
   * The operators are identified by PlanManager.findQueryOperators()
   * call made during QueryManager.instantiate().
   * @param q Query
   */
  private void initializeOperatorStates(Query q) throws CEPException
  {
    Map<String, List<PhyOpt>> archiverToPhyOptMap =
      new HashMap<String, List<PhyOpt>>();
    List<PhyOpt> connectorOpList = new LinkedList<PhyOpt>();
    
    LogUtil.fine(LoggerType.TRACE, "ARF# Starting state initialization process" +
      " for CQL query "+q.getName());
    
    // 1. First segregate the operators based on the 'archiver' they 
    //    want to issue their query against.
    //    Also create a list of 'connector' operators (those joining
    //    the global plan with the local plan) in the query.
    //    Also populate certain details about the archiver query of
    //    each phyopt
    
    PhyOpt root = execContext.getPlanMgr().getQueryRootOpt(q.getId());
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);

    for (DAGNode op : nodes) {
      PhyOpt opt = (PhyOpt) op;
      
      if(opt.isQueryOperator())
      {
        List<PhyOpt> phyOpList = 
          archiverToPhyOptMap.get(opt.getArchiverName());
        if(phyOpList == null)
        {
          phyOpList = new LinkedList<PhyOpt>();
          archiverToPhyOptMap.put(opt.getArchiverName(),
                                  phyOpList);
        }
        phyOpList.add(opt);
      }
      if(opt.isLHSConnector() || opt.isRHSConnector()) 
      {
        connectorOpList.add(opt);
      }
    }
    
    LogUtil.finer(LoggerType.TRACE, "ARF# Separated query operators based on" +
      " archiver and collected "+ connectorOpList.size()+ " connector " +
      "operators for query "+q.getName());
    
    // Get the current system time/query start time to be set as snapShotTime
    // Multiply it with 10^6 to make it in nanosecond unit.
    // The current system time is obtained here so that all archiver queries
    // corresponding to a single CQL query use the same value of 'currentTime'.
    Long currentTime = System.currentTimeMillis()  * 1000000l ; //default
    
    //Check if the start time value is set
    if(q.getQueryStartTime() != Long.MIN_VALUE)
    {
      currentTime = q.getQueryStartTime(); 
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
        "Using user specified query start time as snapshot time...");
    }
    
    //2. One combined query (using UNION) for all operators 
    //   associated with one archiver.
    //   So all operators that query one archiver will have their
    //   queries clubbed together and hence they execute against
    //   a single snapshot.
    //   This guarantee is not provided when queries are executed 
    //   against different archivers.
    Set<String> keyValues = archiverToPhyOptMap.keySet();
    Iterator<String> keyValuesIter = keyValues.iterator();
    while(keyValuesIter.hasNext())
    {
      String archName = keyValuesIter.next();

      List<PhyOpt> phyList = archiverToPhyOptMap.get(archName);
      LogUtil.finer(LoggerType.TRACE, "ARF# "+phyList.size()+" query operators " +
        "would query the archiver "+archName+". Combining their queries now!");
      //number of entries in the project clause of the resultant query
      int numEntriesInProjectClause = 0;
      //array to keep track of the first column index of every phyopt's project
      //entries. will be used to lookup that operator's records in resultset.
      //1 extra entry in the array is to mark the snapshot info start index
      int[] phyOptColStartIdx = new int[phyList.size()+1];
      
      //A list that would contain the datatype of each project clause entry
      //of the generated archiver query. This is needed in case of BI to
      //cast the nulls to appropriate type. Length of the list will be equal
      //to the length of the project clause of the combined union query.
      List<Datatype> projTypesList = new LinkedList<Datatype>();
      
      //2a. populate data structures
      int k = 0;
      for(PhyOpt phyOp : phyList)
      {
        //1 is added since resultset entries start from 1
        phyOptColStartIdx[k] = numEntriesInProjectClause+1;
        numEntriesInProjectClause += phyOp.getProjectClauseLength();
        projTypesList.addAll(phyOp.getArchiverProjTypes());
        k++;
      }
      phyOptColStartIdx[k] = numEntriesInProjectClause+1;
      
      //added 3 for taking into account the three additional columns
      //apart from the columns in the archiver queries of phyopts.
      //Three additional columns are - BEAM_ID, TRANSACTION_ID and type(used
      //for ordering the query results)
      numEntriesInProjectClause += 3;
      projTypesList.add(Datatype.BIGINT); // worker id
      projTypesList.add(Datatype.BIGINT); // txn id
      projTypesList.add(Datatype.INT); // order by column
      
      assert projTypesList.size() == numEntriesInProjectClause :
        "projTypesList size="+projTypesList.size()+ 
        " numEntriesInProjectClause="+numEntriesInProjectClause;
      
      //2b. construct the query 
      String qString = constructUnionBasedQuery(phyList,
                                                phyOptColStartIdx, 
                                                numEntriesInProjectClause,
                                                projTypesList,
                                                q.getName()
                                                );
      
      //2c. Obtain the archiver instance from the archiver name
      IArchiver archiver = findArchiver(archName);

      //2d. execute the query
      IArchiverQueryResult qryResult 
        = executeArchiverQuery(qString, archiver, q, phyList, currentTime);
      
      //2e. use the query results and populate tuples in execopts and snapshot
      //    object construction.
      Snapshot s = convertResultToTuples(qryResult, phyList, phyOptColStartIdx,
                                         numEntriesInProjectClause, q,
                                         archiver);   
      
      //2f. Call the cloesResult() method to close the archiver resultsets
      archiver.closeResult(qryResult);
      LogUtil.finer(LoggerType.TRACE, "ARF# Closed the "+archName+" resultsets..");
      
      //2g. once all operators have the archiver tuples set and snapshot object
      //    is created, we should request a new snapshotid and use it */
      long snapshotId = execContext.getPlanMgr().getNextSnapshotId();
      s.setSnapshotId(snapshotId);
      execContext.getPlanMgr().addSnapshot(s);
      
      //2h. set the snapshot id in the queue for 'connector' operators
      //FIXME: currently we set the current snapshotid for ALL connector 
      //operators. This might need to change.
      //e.g. select * from R1, R2 where R1.c1 = 3 and R2.c2=4
      //Assume R1 and R2 have different archivers.
      //Here if the SELECT operators below the join are connectors, then
      //select above R1 should have snapshot id 0 and that above R2 should 
      //have snapshot id 1.
      setSnapshotIdForConnectors(connectorOpList, snapshotId);

      LogUtil.info(LoggerType.TRACE, "ARF# State initialization process" +
          " for CQL query "+q.getName()+" completed ...");
    }
  }

  private String constructUnionBasedQuery(List<PhyOpt> phyList,
                                         int[] phyOptColStartIdx,
                                         int totalProjEntries,
                                         List<Datatype> projTypesList,
                                         String queryName)
  {
    SQLType sqlType = 
      execContext.getServiceManager().getConfigMgr().getTargetSQLType();
    LogUtil.fine(LoggerType.TRACE, "ARF# Target SQL TYPE = "+sqlType);
    boolean useMillisTs =
      execContext.getServiceManager().getConfigMgr().getUseMillisTs();
    LogUtil.fine(LoggerType.TRACE,"ARF# UseMilisTs = "+useMillisTs);
      
    StringBuffer qString = new StringBuffer();
    String nullEntries = null;
    //ordering column value.
    //There is an implicit mapping between entry in phyList and orderVal
    //i.e. the first entry in phylist (index 0) would have order value 0.
    //So based on the order value we would be able to locate the operator
    //wherever needed.
    //similar implicit 1:1 correspondence exists between phyOptColStartIdx
    //entry and phyList entry.
    int orderVal = 0;
    //loop index
    int optIdx = 0;
    //ordering column alias
    String alias = ((PhyOpt)phyList.get(0)).getOptName()+"_type ";
    
    for(PhyOpt opt : phyList)
    {
      //get the operator's existing output sql
      String opSql = opt.getOutputSQL();
      LogUtil.fine(LoggerType.TRACE, "ARF# "+
                   "Processing query operator "+opt.getOptName()+" ...");
      LogUtil.finer(LoggerType.TRACE,"ARF# Existing SQL: "+opSql);
      StringBuffer opTransformedSql = new StringBuffer(opSql);
          
      //insert nulls if needed after the existing project clause in opSql
      //also insert the ordering column
      //This should be done before adding nulls at the beginning so that
      //opt.getProjectClauseEndIdx() is valid.
      
      nullEntries = getNullBlock(totalProjEntries - phyOptColStartIdx[optIdx+1],
                                 sqlType,
                                 projTypesList.subList(phyOptColStartIdx[optIdx+1]-1, totalProjEntries)
                                 );
      String nullAndOrderBy = 
        ", "+nullEntries + " "+orderVal+" as "+alias;
      opTransformedSql.insert(opt.getProjectClauseEndIdx(), nullAndOrderBy);
      orderVal++;
      
      //insert nulls if needed before the existing project clause in opSql
      nullEntries = getNullBlock(phyOptColStartIdx[optIdx] - 1,
                                 sqlType,
                                 projTypesList.subList(0, phyOptColStartIdx[optIdx] - 1)
                                 );
      opTransformedSql.insert(opt.getProjectClauseStartIdx(), nullEntries);
      
      //enclose the sql in bracket and add UNION ALL at the end
      StringBuffer enclosedSQL = 
        new StringBuffer(" ( "+ opTransformedSql+" ) ");
      enclosedSQL.append(" UNION ALL ");
      //LogUtil.finer(LoggerType.TRACE, "ARF# Transformed SQL: "+enclosedSQL);
      
      //append to qString
      qString.append(enclosedSQL.toString());
      
      //increment loop index
      optIdx++;
    }
    
    //All operator sql are combined now. So add the snapshot sql.
    //insert appropriate number of nulls after "( select "
    qString.append("( select ");
    nullEntries = getNullBlock(phyOptColStartIdx[optIdx]-1,
                               sqlType,
                               projTypesList.subList(0,phyOptColStartIdx[optIdx] - 1));
    qString.append(nullEntries);
    
    //insert the snapshot columns and order by
    //In production setup the name of the table containing snapshot info 
    //would be BEAM_TRANSACTION_CONTEXT.
    //But for allowing regressions to run in parallel (by different users)
    //we will need a unique table name.
    String snapShotTableName =
      execContext.getServiceManager().getConfigMgr().getBeamTxnCtxName();
    if(execContext.getServiceManager().getConfigMgr().isJDBCTest())
    {
      String suffix = System.getProperty("wlevs.home");
      LogUtil.finest(LoggerType.TRACE, "ARF# wlevs.home in server ="+suffix);
      //we get back wlevs.home in this form: /ade/udeshmuk_cep3/cep/wlevs_cql
      //and we are interested in only the name 'udeshmuk_cep3'
      if(suffix != null)
      {
        String[] tmp = suffix.split("/");
        suffix = tmp[tmp.length - 3];
	      suffix = suffix.replace('-','_');
        snapShotTableName = "BTC_"+suffix;
        if(snapShotTableName.length() > 30)
          snapShotTableName = snapShotTableName.substring(0,30);
      }
    }
    
    String contextColName = 
      execContext.getServiceManager().getConfigMgr().getContextColName();
    String txnColName = 
      execContext.getServiceManager().getConfigMgr().getTxnColName();
    
    qString.append(" "+contextColName+", "+txnColName+", "+orderVal+" as "+
                   alias+"from "+snapShotTableName+")");

    qString.append(" order by "+alias+" asc");
    
    //replace # by _ since # is commnet in BI Logical SQL
    String finalQuery = qString.toString().replace('#', '_');
    
    LogUtil.fine(LoggerType.TRACE, 
                 "ARF# FINAL COMBINED ARCHIVER QUERY for CQL query "+queryName+ " : "+finalQuery);
    return finalQuery;
  }
  
  private String getNullBlock(int size, SQLType targetSQLType, List<Datatype> dtList)
  {
    StringBuffer nullBlock = new StringBuffer("");
    for(int i=0; i < size; i++)
    {
      if(targetSQLType == SQLType.ORACLE)
        nullBlock.append("null, ");
      else
      {
        if(getBITypeName(dtList.get(i)) != null)
          nullBlock.append("CAST(null as "+getBITypeName(dtList.get(i))+"), ");
        else//FIXME : may be we can throw error
          nullBlock.append("null, ");
      }
    }
    
    return nullBlock.toString();
  }
  
  private String getBITypeName(Datatype dt)
  {
    switch(dt.kind)
    {
      case INT:return "INTEGER";
      case BIGINT: return "INTEGER";
      case FLOAT: return "FLOAT";
      case DOUBLE: return "DOUBLE PRECISION";
      case BIGDECIMAL: return "DOUBLE PRECISION";
      case CHAR : return "CHAR";
      case TIMESTAMP : return "TIMESTAMP";
      default : return null;
    }
  }
  
  private IArchiverQueryResult executeArchiverQuery(String qString,
                                                    IArchiver archiver,
                                                    Query q,
                                                    List<PhyOpt> phyList,
                                                    Long currentTime)
                                                    throws CEPException
  {
    //1. Process parameters, if any.
    assert (archiver != null) : "Archiver instance is NULL !";
    Object[] params = processParamsToArchiverQuery(phyList, q, currentTime);
    
    //2. Prepare query requests 
    QueryRequest[] requests = 
      new QueryRequest[]{
        new QueryRequest(qString, params)
      };
    
    //3. Execute query
    LogUtil.finer(LoggerType.TRACE, "ARF# About to execute the archiver query for "
                                   +"CQL query "+q.getName()); 
    long tempTime = System.currentTimeMillis();
    IArchiverQueryResult results = archiver.execute(requests);
    long execTime = System.currentTimeMillis() - tempTime;
    q.getArchiverStats().setArchiverQueryExecTime(execTime);
    
    assert results.getResultCount() == requests.length;
    
    return results;
  }
  
  private IArchiver findArchiver(String archName) throws CEPException
  {
    // Get the Archiver
    ConfigManager cm = execContext.getServiceManager().getConfigMgr();
    
    IArchiverFinder service = cm.getArchiverFinder();
    
    IArchiver archiver = null;
    if(service == null)
    { 
      LogUtil.warning(LoggerType.TRACE, "ARF# IArchiverFinder service not available!");
      throw new CEPException(CodeGenError.ARCHIVER_FINDER_SERVICE_NOT_AVAILABLE);
    }
    else
    {

      if(archName != null)
      {
        LogUtil.finer(LoggerType.TRACE, "ARF# Looking up in IArchiverFinder with key "
                     + archName);
        archiver = service.findArchiver(archName);
        if(archiver == null)
          throw new CEPException(CodeGenError.ARCHIVER_NOT_FOUND, 
                                 new Object[]{archName});
      }
      else
      {
        LogUtil.warning(LoggerType.TRACE, "ARF# Specified archiver name is null!");
        throw new CEPException(CodeGenError.ARCHIVERNAME_NOT_SPECIFIED);
      }
    }
    
    return archiver;
  }
  
  private Object[] processParamsToArchiverQuery(List<PhyOpt> phyList,
                                                Query query,
                                                Long currentTime)
                                                throws CEPException
  {
    /*
     * The parameters (if any) are processed and provided in the order
     * of phyList. It is the same order in which queries of these phyopts
     * were combined. So no need to have a separate mapping of parameter
     * values to the position of the parameter in the combined archiver 
     * query.
     */
    List<Object> paramValList = new LinkedList<Object>();


    for(PhyOpt op : phyList)
    {
      paramValList.addAll(processParams(op, query, currentTime));
    }
    
    if(paramValList.size() == 0)
      return null;
    else
      return paramValList.toArray();
  }
  
  private List<Object> processParams(PhyOpt op, Query q, Long currentTime) 
    throws CEPException
  {
    LogUtil.finer(LoggerType.TRACE, "ARF# "+
      "Processing parameter(s) of "+op.getOptName()+" for the archiver query");
    /*
     * Even though currently we have only PhyOptValueWindow and PhyOptStrmSrc
     * sending parameter to the archiver query and that too only one parameter
     * we are returning a list of parameter values to keep the code general
     * enough to handle multiple parameters, if needed, in future.
     */
    List<Object> paramVals = new LinkedList<Object>();
    
    switch(op.getOperatorKind())
    {
      case PO_VALUE_WIN:
        // Determine the snapshot time on the basis of value window type
        // Snapshot time will be nanosecond unit
        PhyOptValueWin phyWin = (PhyOptValueWin)op;
        
        long snapShotTime 
          = phyWin.getSnapShotTime(currentTime);
        
        op.getInstOp().setSnapShotTime(currentTime);
        
        if(phyWin.getColType() == Datatype.TIMESTAMP)
        {  
          // Timestamp constructor requires parameter timestamp in millisecond unit
          Timestamp ts = new Timestamp(snapShotTime / 1000000l);
          paramVals.add(ts);  
          LogUtil.finer(LoggerType.TRACE, "ARF# "+
            "Snapshot time sent as parameter "+ts);
        }
        else
        {
          paramVals.add(snapShotTime);
          LogUtil.finer(LoggerType.TRACE, "ARF# "+
            "Snapshot time sent as parameter "+snapShotTime);
        }
        break;
      case PO_STREAM_SOURCE:
        PhyOptStrmSrc pStrmSrc = (PhyOptStrmSrc) op;
        
        if(execContext.getTableMgr().getTable(pStrmSrc.getStrId())
                                    .isReplayRange())
        {
          // find out the type of the TIMESTAMP column : ts or bigint
          if(pStrmSrc.getTsColType() == Datatype.BIGINT)
          {
            //bigint - so NO need to convert currenttime to nanos
            //since the value would already be in nanos
            paramVals.add(currentTime);
            LogUtil.finer(LoggerType.TRACE, "ARF# "+
                "Snapshot time sent as parameter "+currentTime);
          }
          else
          {
            //timestamp - create a timestamp instance out of currenttime
            Timestamp ts = new Timestamp(currentTime/1000000l);
            paramVals.add(ts);
            LogUtil.finer(LoggerType.TRACE, "ARF# "+
                "Snapshot time sent as parameter "+ts);
          }
          
	  op.getInstOp().setSnapShotTime(currentTime);
        }
        else
        {
          //no parameter needed for rows case
          op.getInstOp().setSnapShotTime(currentTime);
        }
        break;
      default:
        //set the snapshot time in the operator
        op.getInstOp().setSnapShotTime(currentTime);
    }
    
    return paramVals;
  }
  
  private Snapshot convertResultToTuples(IArchiverQueryResult result,
                                         List<PhyOpt> phyList,
                                         int[] phyOptColStartIdx,
                                         int totalProjEntries,
                                         Query q,
					 IArchiver archiver)
                                         throws CEPException
  {
    long tempTime = System.currentTimeMillis();
    /*
     * For processing of every record, check the type field value.
     * It will give the index in the phyList array. From there 
     * we will get phyopt object and then use it to get the relevant
     * information needed to convert the record to a tuple.
     */
    List<List<ITuplePtr>> phyOptArchiverTuples = 
      new LinkedList<List<ITuplePtr>>();
    assert result != null : "Archiver query returned null";
    ResultSet res = result.getResult(0);
    Snapshot s = new Snapshot();
    
    for(int i=0; i < phyList.size(); i++)
    {
      List<ITuplePtr> tupList = new LinkedList<ITuplePtr>();
      phyOptArchiverTuples.add(tupList);
    }
    
    try
    {
      ResultSetMetaData resMeta = res.getMetaData();
      int colCount = resMeta.getColumnCount();
      long numRecords = 0;
      
      while(res.next())
      {
        numRecords++;
        //1. Look at the ordering column value to identify the phyopt/execopt
        //   to whom the record belongs.
        int optIdx = res.getInt(colCount);
        
        //2. If the index is greater than phyList.size() then it means this is
        //   a snapshot tuple.
        if(optIdx == phyList.size())
        {
          //get the worker id / BEAM_ID information
          long workerId = res.getLong(colCount - 2);
          //get the txn id / TRANSACTION_ID information
          long txnId = res.getLong(colCount - 1);
          /*LogUtil.finest(LoggerType.TRACE, 
            "Adding snapshot info : CONTEXT_ID="+workerId+" TXN_ID="+txnId);*/
          s.addSnapshotInfo(workerId, txnId);
        }
        else
        {
          int startColIdx = phyOptColStartIdx[optIdx];
          int endColIdx = phyOptColStartIdx[optIdx + 1];
          ITuplePtr tuple = null;
          //construct the tuple
          tuple = getArchivedTuple(startColIdx, endColIdx,
                                   phyList.get(optIdx), res, resMeta);
          
          //add the tuple to tuplelist for that operator
          List<ITuplePtr> tupList = 
            (List<ITuplePtr>) phyOptArchiverTuples.get(optIdx);
          tupList.add(tuple);
        }
      }
      
      long conversionTime = System.currentTimeMillis() - tempTime;
      q.getArchiverStats().setConversionOfResultSetToTuplesTime(conversionTime);
      q.getArchiverStats().setNumRecordsReturned(numRecords);
      
      //Since we have processed the entire result set now, we have the
      //list of archiver tuples ready for each of the 'query' operators.
      //So we will set them in the execution operators.
      //We will also set the readerIds on which these tuples should be sent. 
      int idx = 0; 
      for(PhyOpt p : phyList)
      {
        p.getInstOp().setArchiverReaders(getArchiverResultReaders(p, q.getId()));
        p.getInstOp().setArchivedRelationTuples(phyOptArchiverTuples.get(idx));
        
        //reset the query operator flag. 
        //necessary so that some query that may get added in future and share
        //this operator and could incorrectly assume this to be query operator
        //e.g. q1: Istream(select c2 from R)
        //     q2: select distinct c2 from R 
        //Here q1 is started first and then q2. project for c2 is shared.
        //If we don't reset the query operator flag then project for c2 and 
        //distinct both would be assumed as query operators for q2.
        p.setIsQueryOperator(false);
        LogUtil.fine(LoggerType.TRACE, "ARF# "+ 
                     "Set "+phyOptArchiverTuples.get(idx).size()+
                     " archiver query result tuples in "+
                     p.getInstOp().getOptName());
        idx++;
      }
    }
    catch(SQLException se)
    {
      LogUtil.warning(LoggerType.TRACE, "ARF# Got SQLException, closing the resultset..");
      archiver.closeResult(result);
      if(se.getCause() != null)
        throw new ExecException(ExecutionError.ARCHIVER_QUERY_RESULTSET_ACCESS_ERROR,
                                se.getCause(),
                                new Object[]{se.getMessage()});
      else
        throw new ExecException(ExecutionError.ARCHIVER_QUERY_RESULTSET_ACCESS_ERROR, 
                                new Object[]{se.getMessage()});
    }
    
    //return the snapshot object
    return s;
  }
  
  public BitSet getArchiverResultReaders(PhyOpt p, int qId) 
    throws CEPException
  {
    PhyOptOutputIter iter = p.getOutputsIter();
    BitSet archiverReaders = new BitSet();
    if(iter != null)
    {
      PhyOpt out = null;
      while((out=iter.getNext()) != null)
      {
        
        //check if the output has the current queryid 
        if(out.getQryIds().contains(qId))
        {
          //get the input reader queue(s) of the operator
          if(out.getInputs().length == 2)
          {
            //binary
            if(out.getInputs()[0] == p)
            {
              archiverReaders.set(((ISharedQueueReader)out.getInstOp().
                                    getInputQueue(0)).getReaderId());
            }
            if(out.getInputs()[1] == p)
            {
              archiverReaders.set(((ISharedQueueReader)out.getInstOp().
                  getInputQueue(1)).getReaderId());
            }
          }
          else if(out.getInputs().length == 1)
          {
            //unary
            archiverReaders.set(((ISharedQueueReader)out.getInstOp().
                getInputQueue(0)).getReaderId());
          }
        }
      }
    }

    return archiverReaders;
  }
  
  /**
   * Converts a record in the resultset into a tuple. Only the columns
   * from startColIdx to endColIdx are considered while constructing the tuple.
   * Functions called on resultset to retrive values in a record are as 
   * per the recommendations in
   * http://docs.oracle.com/javase/6/docs/technotes/guides/jdbc/getstart/mapping.html
   * @param startColIdx - index of the first column for that operator
   * @param endColIdx - index of the last column for that operator
   * @param op - the operator being processed
   * @param resultSet - result set 
   * @param resMeta - metadata of the resultset
   * @return a tuple as per the operator (op) tuplespec
   * @throws SQLException
   * @throws CEPException
   */
  private ITuplePtr getArchivedTuple(int startColIdx, int endColIdx,
                                     PhyOpt op, ResultSet resultSet,
                                     ResultSetMetaData resMeta)
                                     throws SQLException, CEPException
  {
    boolean eventIdColAdded = op.isEventIdColAddedToProjClause();
    int eventIdColNum = op.getEventIdColNum();
    IAllocator<ITuplePtr> factory = op.getInstOp().getTupleStorageAlloc();
    //For distinct we have to call a separate method
    //For all other possible query operators (RelSrc,StrmSrc,Project,Select,
    //GroupAggr,ValueWindow) we can use the CodeGenHelper method
    TupleSpec tupSpec = null;
    if(op.getOperatorKind() == PhyOptKind.PO_DISTINCT)
      tupSpec = ((Distinct)op.getInstOp()).getTupleSpec();
    else
      tupSpec = CodeGenHelper.getTupleSpec(execContext, op);
    
    ITuplePtr tPtr = (ITuplePtr)factory.allocate(); 
    
    if(tPtr != null)
    {
      ITuple t = tPtr.pinTuple(IPinnable.WRITE);
      
      int idx = startColIdx;
      if(eventIdColAdded)
      {
        t.setId(resultSet.getLong(idx));
        idx++;
      }
      else
      {
        if(eventIdColNum != -1)
        {
          t.setId(resultSet.getLong(startColIdx+eventIdColNum));
        }
        idx = startColIdx;
      }
    
      ResultSetMetaData rsMeta = resultSet.getMetaData(); 
      int tupIdx = 0;
      //resultSetIdx should be strictly less than endColIdx since 
      //endColIdx is actually the startColIdx of next operator.
      for(int resultSetIdx=idx; resultSetIdx < endColIdx; resultSetIdx++)
      {
        switch(rsMeta.getColumnType(resultSetIdx))
        {
          case java.sql.Types.BOOLEAN:
          case java.sql.Types.BIT:
            boolean bv = resultSet.getBoolean(resultSetIdx);

            if(resultSet.wasNull())
              t.setAttrNull(tupIdx);
            else
              t.boolValueSet(tupIdx, bv);
            break;
          case java.sql.Types.CHAR:
          case java.sql.Types.VARCHAR:
          case java.sql.Types.LONGNVARCHAR:
            String s = resultSet.getString(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            {
              if(tupSpec.getAttrType(tupIdx) == Datatype.CHAR)
              {
                t.cValueSet(tupIdx, s.toCharArray(), s.length());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INTERVAL)
              {
                IntervalFormat format = tupSpec.getAttrMetadata(tupIdx).getIntervalFormat();
                t.vValueSet(tupIdx, 
                  IntervalConverter.parseDToSIntervalString(s,format),
                  format);                
              }
              else if((tupSpec.getAttrType(tupIdx) == Datatype.INTERVALYM))
              {
                IntervalFormat fmt = tupSpec.getAttrMetadata(tupIdx).getIntervalFormat();
                t.vymValueSet(tupIdx, 
                    IntervalConverter.parseYToMIntervalString(s,fmt),
                    fmt);
              }
            }
            break;
          case java.sql.Types.BINARY:
          case java.sql.Types.VARBINARY:
          case java.sql.Types.LONGVARBINARY:
            byte[] b = resultSet.getBytes(resultSetIdx);

            if(resultSet.wasNull())
              t.setAttrNull(tupIdx);
            else
              t.bValueSet(tupIdx, b, b.length);
            break;
          case java.sql.Types.DECIMAL:
          case java.sql.Types.NUMERIC:
            BigDecimal bdVal = resultSet.getBigDecimal(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            { //based on cql column type convert the value and call the setter
              if(tupSpec.getAttrType(tupIdx) == Datatype.BIGDECIMAL)
              {
                t.nValueSet(tupIdx, bdVal, bdVal.precision(), bdVal.scale());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.DOUBLE)
              {
                t.dValueSet(tupIdx, bdVal.doubleValue());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.FLOAT)
              {
                t.fValueSet(tupIdx, bdVal.floatValue());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.BIGINT)
              {
                t.lValueSet(tupIdx, bdVal.longValueExact());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INT)
              {
                t.iValueSet(tupIdx, bdVal.intValueExact());
              } 
            }
            break;
          case java.sql.Types.DOUBLE:
          case java.sql.Types.FLOAT:
            Double dVal = resultSet.getDouble(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            { //based on cql column type convert the value and call the setter
              if(tupSpec.getAttrType(tupIdx) == Datatype.BIGDECIMAL)
              {
                BigDecimal bd = new BigDecimal(dVal);
                t.nValueSet(tupIdx, bd, bd.precision(), bd.scale());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.DOUBLE)
              {
                t.dValueSet(tupIdx, dVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.FLOAT)
              {
                t.fValueSet(tupIdx, dVal.floatValue());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.BIGINT)
              {
                t.lValueSet(tupIdx, dVal.longValue());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INT)
              {
                t.iValueSet(tupIdx, dVal.intValue());
              } 
            }
            break;
          case java.sql.Types.REAL:
            Float fVal = resultSet.getFloat(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            { //based on cql column type convert the value and call the setter
              if(tupSpec.getAttrType(tupIdx) == Datatype.BIGDECIMAL)
              {
                BigDecimal bd = new BigDecimal(fVal);
                t.nValueSet(tupIdx, bd, bd.precision(), bd.scale());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.DOUBLE)
              {
                t.dValueSet(tupIdx, fVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.FLOAT)
              {
                t.fValueSet(tupIdx, fVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.BIGINT)
              {
                t.lValueSet(tupIdx, fVal.longValue());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INT)
              {
                t.iValueSet(tupIdx, fVal.intValue());
              } 
            }
            break;
          case java.sql.Types.BIGINT:
            long lVal = resultSet.getLong(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            { //based on cql column type convert the value and call the setter
              if(tupSpec.getAttrType(tupIdx) == Datatype.BIGDECIMAL)
              {
                BigDecimal bd = new BigDecimal(lVal);
                t.nValueSet(tupIdx, bd, bd.precision(), bd.scale());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.DOUBLE)
              {
                t.dValueSet(tupIdx, lVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.FLOAT)
              {
                t.fValueSet(tupIdx, lVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.BIGINT)
              {
                t.lValueSet(tupIdx, lVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INT)
              {
                t.iValueSet(tupIdx, (int)lVal);
              } 
            } 
            break;
          case java.sql.Types.INTEGER:
            int iVal = resultSet.getInt(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            { //based on cql column type convert the value and call the setter
              if(tupSpec.getAttrType(tupIdx) == Datatype.BIGDECIMAL)
              {
                BigDecimal bd = new BigDecimal(iVal);
                t.nValueSet(tupIdx, bd, bd.precision(), bd.scale());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.DOUBLE)
              {
                t.dValueSet(tupIdx, iVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.FLOAT)
              {
                t.fValueSet(tupIdx, iVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.BIGINT)
              {
                t.lValueSet(tupIdx, iVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INT)
              {
                t.iValueSet(tupIdx, iVal);
              } 
            }
            break;
          case java.sql.Types.SMALLINT:
          case java.sql.Types.TINYINT:
            short sVal = resultSet.getShort(resultSetIdx);
            if(resultSet.wasNull())
            {
              t.setAttrNull(tupIdx);
            }
            else
            { //based on cql column type convert the value and call the setter
              if(tupSpec.getAttrType(tupIdx) == Datatype.BIGDECIMAL)
              {
                BigDecimal bd = new BigDecimal(sVal);
                t.nValueSet(tupIdx, bd, bd.precision(), bd.scale());
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.DOUBLE)
              {
                t.dValueSet(tupIdx, sVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.FLOAT)
              {
                t.fValueSet(tupIdx, sVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.BIGINT)
              {
                t.lValueSet(tupIdx, sVal);
              }
              else if(tupSpec.getAttrType(tupIdx) == Datatype.INT)
              {
                t.iValueSet(tupIdx, sVal);
              } 
            }
            break;
          case java.sql.Types.TIMESTAMP:
            Timestamp ti = resultSet.getTimestamp(resultSetIdx);

            if(resultSet.wasNull())
              t.setAttrNull(tupIdx);
            else
            {
              t.tValueSet(tupIdx, ti);
              t.tFormatSet(tupIdx, 
                           tupSpec.getAttrMetadata(tupIdx).getTimestampFormat());
            }
            break;
          default: 
            throw new SQLException("Unsupported column type "+
                                   resMeta.getColumnTypeName(resultSetIdx)+
                                   " for column name "+resMeta.getColumnName(resultSetIdx));
        }
        
        //increment the tuple column index
        tupIdx++;   
      }//end for 
      
      tPtr.unpinTuple(); 
    }
    
    return tPtr;
  }
  
  private void setSnapshotIdForConnectors(List<PhyOpt> connectorOpList,
                                         long snapshotId)
  {
    for(PhyOpt connOp : connectorOpList)
    {
      ExecOpt connExecOp = connOp.getInstOp();
      ISharedQueueReader queue = 
        (ISharedQueueReader) connExecOp.getInputQueue(0);
      if((connOp.isLHSConnector()) && (queue != null))
      {
        queue.getReaderContext().setSnapshotId(snapshotId);
        connOp.setLHSConnector(false);
        connOp.setCanBeConnectorOperator(true);
        LogUtil.finer(LoggerType.TRACE, "ARF# "+
          "Snapshot id is set to "+snapshotId+ " in "+connOp.getOptName()+" input queue.");
      }
      if(connExecOp.getInputQueue(1) != null)
      {
        //binary op
        queue = (ISharedQueueReader) connExecOp.getInputQueue(1);
        if(connOp.isRHSConnector())
        {
          queue.getReaderContext().setSnapshotId(snapshotId);
          connOp.setRHSConnector(false);
          connOp.setCanBeConnectorOperator(true);
          LogUtil.finer(LoggerType.TRACE, "ARF# "+
            "Snapshot id is set to "+snapshotId+ " in "+connOp.getOptName()+" right input queue.");
        }
      }
      
    }
  }
  
  /**
   * Restart an already created query. This also compiles the query.
   * The query will be automatically started.
   * @param cql
   *          Query text
   * 
   * @throws CEPException
   */
  private void restartQuery(ITransaction m_txn, String qname, String schema)
      throws CEPException
  {
    CacheLock l = null;
    Query query;
    int queryId;
    
    assert (qname != null);
     
    LogLevelManager.trace(LogArea.METADATA_QUERY, LogEvent.MQUERY_START, this, qname);
    // Check for duplicate query name and create
    // qname here contains both schema and query name
    l = findCache(m_txn, 
                  new Descriptor(qname,CacheObjectType.QUERY, schema, null),
                  true);
    
    if (l == null)
    {
      throw new MetadataException(MetadataError.INVALID_QUERY_OPERATION, new Object[]{"restart query"});
    }

    // Initialize
    query = (Query) l.getObj();

    // If any query is already restarted then we don't want to restart it again
    // This can happen as a result of query on top of view which will restart query
    // on which view was created.
    if(query.getState().equals(QueryState.RUN))
      return;
      
    queryId = l.getObj().getId();

    // Compile query
    SemQuery sQuery = typecheck(query, null);
    query.setSemQuery(sQuery);
    query.setNames();
    query.setState(QueryState.TYPECHECKED);

    compile(query, false);
      
    // If any query has just a view on top of it then we don't need to start that query
    // Also start the query only when we want it to RUN else not.
   // if(query.getDestViews().isEmpty() || (!query.getExtDests().isEmpty()))
    if((!execContext.getDependencyMgr().
         isAnyDependentPresent(query.getId(), DependencyType.VIEW)) ||
       (!query.getExtDests().isEmpty()))
    {
      if(query.getDesiredState() == QueryState.RUN)
        startQuery(queryId, false);
    }

 }


  /**
   * Finds the existing query in the cache or MDS.
   * @param cql 
   *          Query text
   * @return Query id
   * @throws CEPException
   */
  public int findQuery(String cql, String schema)
      throws CEPException
  {
    CacheLock l = null;
    int queryId;
    ITransaction txn = execContext.getTransaction();
    
     l = findCache(txn, new Descriptor(cql,CacheObjectType.QUERY, schema,null),false);
     
     if(l== null)
       throw new MetadataException(MetadataError.QUERY_NOT_FOUND, new Object[]{cql});
     
    queryId = l.getObj().getId();
    
      // Release
      if (l != null)
        release(txn, l);
    
    return queryId;
  }


  /**
   * Add a new query. This also compiles the query.
   * The query must be explicitly started.
   * @param name
   *          Query name
   * @param cql
   *          Query text
   * @param parseTree
   *          Parse tree for the query, if available
   * 
   * @return Query id
   * @throws CEPException
   */
  public int addNamedQuery(String name, String cql, String schema,
                           CEPParseTreeNode parseTree)
      throws CEPException
  {
    int qid;
    try{
      qid = addQuery(name, cql, schema, parseTree);
    }catch(MetadataException me)
    {
      if (me.getStartOffset() == 0 || me.getEndOffset() == 0) 
      {
        me.setStartOffset(parseTree.getStartOffset());
        me.setEndOffset(parseTree.getEndOffset());
      }
      throw me;
    }
    catch(LogicalPlanException e)
    {
      e.setStartOffset(parseTree.getStartOffset());
      e.setEndOffset(parseTree.getEndOffset());
      throw e;
    }
    
    notifyAddQuery(name, qid);
    return qid;
  }
    
  /**
   * Add a new query. This also compiles the query.
   * The query must be explicitly started.
   * @param name
   *          Query name
   * @param cql
   *          Query text
   * @param parseTree
   *          Parse tree for the query, if available
   * @return Query id
   * @throws CEPException
   */
  private int addQuery( String name, String cql, String schema, 
                        CEPParseTreeNode parseTree)
      throws CEPException
  {
    CacheLock l = null;
    Query query;
    int queryId = -1;
    String qName;
    boolean isNamed = false;
    ArrayList<Integer> tblIds = null;
    int tblId;
    int num;
    boolean isView;
    ITransaction txn = execContext.getTransaction();
        
    assert (cql != null);
    assert (name != null) : cql;
    qName = name;

    if (!qName.startsWith(Constants.CQL_RESERVED_PREFIX))
      isNamed = true;
    
    LogLevelManager.trace(LogArea.METADATA_QUERY, LogEvent.MQUERY_CREATE, this,
                          qName);
    // Check for duplicate query name and create
    l = createObject( txn, qName, schema, CacheObjectType.QUERY, this);
    
    if (l == null)
    {
      throw new MetadataException(MetadataError.QUERY_ALREADY_EXISTS, 
                          new Object[]{qName});
    }

    // Initialize
    query = (Query) l.getObj();
    queryId = l.getObj().getId();
    query.setState(QueryState.CREATE);
    query.setDesiredState(QueryState.CREATE);
    query.setText( cql);
    query.setIsNamed( isNamed);
      
    // Compile query
    SemQuery sQuery = typecheck(query, parseTree);
    query.setSemQuery(sQuery);
    query.setNames();
    query.setState(QueryState.TYPECHECKED);
    query.setDesiredState(QueryState.TYPECHECKED);
      
    // Set Primary key flag and primary key attributes
    query.setIsPrimaryKeyExist(sQuery.getIsPrimaryKeyExists());
    if(sQuery.getIsPrimaryKeyExists())
      query.setOutputConstraintAttrs(sQuery.getOutputConstraintAttrs());
      
      // Add all referenced views
      tblIds = sQuery.getReferencedTables();
      num = sQuery.getNumRefTables();
      
      for(int i = 0; i < num ; i++)
      {
        tblId = tblIds.get(i);
        isView = false;

      // Is it a view
      try
      {
        View v = execContext.getViewMgr().getView(tblId);
        // View is master, Query is dependent
        execContext.getDependencyMgr().addDependency(tblId, query.getId(), 
                            DependencyType.VIEW, DependencyType.QUERY, schema);
   
        isView = true;
        query.setRefExternal(query.isRefExternal || v.isExternal()); //does this query reference an External view?
      }
      catch (MetadataException e)
      {
       if (e.getErrorCode() != MetadataError.INVALID_VIEW_IDENTIFIER)  
         throw e;
      }
      if (!isView)
      {
         
        // Query depends on table so Table-Master, Query-Dependent
        execContext.getDependencyMgr().addDependency(tblId, queryId,
                    DependencyType.TABLE, DependencyType.QUERY, schema);
       
        Table tbl = execContext.getTableMgr().getTable(tblId);
        query.setRefExternal(query.isRefExternal || tbl.isExternal()); //does this query reference an External table?
         
      }
    }

    compile(query, true);
    
    // If Query is based on a partitioned source(stream), then set ordering
    // constraint to PARTITION ORDERED implicitly.
    // Please make sure that this should be executed if the query is registed
    // by user and not by any internal generated DDL from parallelism helper.
    if(query.isDependentOnPartnStream() && !execContext.isInternalDDL())
    {
      alterOrderingConstraint(queryId, OrderingKind.PARTITION_ORDERED,null);
    }      
   
    return queryId;
  }


  public SemQuery typecheck(Query q, CEPParseTreeNode parseTree)
      throws CEPException
  {
    // Parse the command
    if (parseTree == null)
      parseTree = parser.parseCommand(execContext, q.getText());
      
    // Peform semantic analysis
    return sem.interpretQuery(execContext, q, parseTree);
  }

  /**
   * Compile the query
   * @param q
   *          Query
   * @param onlyValidate
   *           Generate logical plan only for validation. For example
   *           parallelism analysis is not required
   * 
   * @throws CEPException
   */
  public void compile(Query q, boolean onlyValidate) throws CEPException
  {
    SemQuery semQuery = q.getSemQuery();

    if (semQuery == null)
    {
      semQuery = typecheck(q, null);
    }
    // Generate logical plan
    LogOpt logOp = log.genLogPlan(execContext, semQuery, q, onlyValidate);

    q.setLogPlan(logOp);
  }


  /**
   * Get the logical plan for this query
   * @return The logical plan corresponding to this query
   */
  public LogOpt getLogPlan(int qid) throws CEPException
  {
    Query q = getQuery(qid);
    if (q.getLogPlan() == null)
      compile(q, false);
    return q.getLogPlan();
  }

  /**
   * Instantiate the query
   * @param q
   *          Query
   * 
   * @throws CEPException
   */
  public void instantiate(Query q) throws CEPException
  {
    LogOpt logOp = q.getLogPlan();

    if (logOp == null)
    {
      compile(q, false);
      logOp = q.getLogPlan();
    }

    // Set the system determined ordering constraint
    q.setSystemOrderingConstraint(logOp.getOrderingConstraint());

    LogUtil.fine(LoggerType.TRACE,
        "System determined ordering constraint for " + q.getName()
        + " is "  + q.getSystemOrderingConstraint());

    if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
    {
      if (q.getUserOrderingConstraint() != q.getSystemOrderingConstraint())
      {
        LogUtil.warning(LoggerType.CUSTOMER,
            "User specified ordering constraint for '" + q.getName() +
            "' is " + q.getUserOrderingConstraint() + ", however system determined that query " +
            " can only be executed as = " + q.getSystemOrderingConstraint());
      }
    }

    // Generate physical plan
    PhyOpt phyOp = phyplan.genPhysicalPlan(logOp,
                                           new PhyPlanGenContext(execContext,
                                                                 q));
   
    // Optimize
    OptTransform optx = new OptTransform();
    phyOp = optx.optimizePlan(execContext, phyOp);
    
    // If query is dependent on archived relation then 
    // identify the operators in the local plan which would act
    // as 'query' operators to initialize states of 
    // stateful operators based on the archived data.
    
    if(q.isDependentOnArchivedRelation()) 
    {
      long tempTime = System.currentTimeMillis();
      //set view root 
      if(q.isViewQuery())
      {
        LogUtil.finest(LoggerType.TRACE, "ARF# "+
	             "Viewquery "+q.getName()+" has root "+phyOp.getOptName());
        phyOp.setIsView(true);
      }
      
      //check if this query is dependent on a view
      Integer[] refViewIds = execContext.getDependencyMgr()
                                        .getMasters(q.getId(), 
					            DependencyType.VIEW);
      
      // if it is not a view query clear all flags.
      execContext.getPlanMgr().clearDimensionflags(q, phyOp, refViewIds);
      
      // identify the projects, selects and relnsrc in local query plan 
      // where buffer operator should be added.
      execContext.getPlanMgr().findBufferOpRequirements(q, phyOp, refViewIds);
      
      //find query operators. This may mark some buffer ops unnecessary,
      //since there is no need to maintain buffer for operators that are
      //downstream to a query operator.
      boolean isQueryFlagOverwritten = 
        execContext.getPlanMgr().findQueryOperators(q, phyOp, refViewIds);
      
      //create those buffer operators now.
      boolean atLeastOneBufferOpAdded = 
        execContext.getPlanMgr().createBufferOperators(q, phyOp, 
                                                       isQueryFlagOverwritten);
      
      //check if a buffer operator is added on top of phyOp(existing root)
      //if so, update phyOp.
      PhyOptOutputIter iter = phyOp.getOutputsIter();
      if(iter != null)
      {
        PhyOpt ot = null;
        do
        {
           ot = iter.getNext();
           if(ot != null)
           {
             //if the output for current view root is buffer operator
             //and the buffer belonging to query being instantiated then
             //set it as new query root.
             if((ot instanceof PhyOptBuffer) 
                && (ot.getQryIds().contains(q.getId())))
               phyOp = ot;
           }
        } while (ot != null);
      }
      
      //call findQueryOperators again if needed
      if(isQueryFlagOverwritten && atLeastOneBufferOpAdded)
      {
        LogUtil.finer(LoggerType.TRACE, "ARF# "+
                     "Finding query operators again after adding " +
                     "buffer operators");
        execContext.getPlanMgr().findQueryOperators(q, phyOp, refViewIds);
      }
      
      //log the phyplan top-down - no need to append ARF# prefix.
      LogUtil.finer(LoggerType.TRACE, 
                   execContext.getPlanMgr().getPlanAsString(phyOp, null));
      
      long opIdentificationAndQueryConstructionTime = 
        System.currentTimeMillis() - tempTime;
      q.getArchiverStats().
        setQueryOpsIdentificationAndConstructionTime(
          opIdentificationAndQueryConstructionTime);
      
    }
    
    phyOp = execContext.getPlanMgr().shareOperators(q, phyOp);

    // Process the heart beat timeout flags
    execContext.getPlanMgr().processRequireHbtTimeOut(phyOp);

    // Add to the list of roots
    execContext.getPlanMgr().setQueryRoot(q.getId(), phyOp);
      
    // Semantic query made null
    q.setSemQuery(null);

    // Add auxillary structures needed for instantiation
    execContext.getPlanMgr().add_aux_structures(q, phyOp);

    // Generate execution plan
    execContext.getPlanMgr().instantiate(q, phyOp);
    
    // Set the propagation state to ARCHIVED_SIA_STARTED if query is dependent 
    // on archived relation. This helps in identifying the case when we are 
    // producing output based on history data and accordingly avoid heartbeat 
    // requests from join.
    if(q.isDependentOnArchivedRelation())
    {
      ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(phyOp);
      for(DAGNode node : nodes)
      {
        PhyOpt opt = (PhyOpt) node;
        //Set propState only for the operators that are above views
        if(!opt.isBelowViewRootInclusive())
          opt.getInstOp().setPropStateToSIAStart();
      }
      
      LogUtil.finer(LoggerType.TRACE, "ARF# Set the propState to ARCHIVED_SIA_START");
    }
    
    // We are done with LogPlan.
    // Set it to null unless if this is a query defining a view and the
    // ordering determined is PARTITION_ORDERED. This is because the
    // LogOptExchange for this query will be required by other queries
    // on top of this view
    if(!(
       (q.getSystemOrderingConstraint() == OrderingKind.PARTITION_ORDERED) &&
       (q.getExtDests().size() == 0)
        )
      )
    {
      q.setLogPlan(null);
    }
  }

  /**
   * Add a destinaton for a query
   * @param qName
   *          Query name
   * @param destination
   *          EPR for destination
   * 
   * @throws MetadataException
   */
  public void addNamedQueryDestination( String qName, String destination, 
                                        String schema,
                                        boolean isUpdateSemantics,
                                        boolean isBatchOutputTuple,
					boolean propagateHeartbeat)
    throws MetadataException, CEPException
  {
    int qId;

    qId = findQuery(qName, schema);
    Destination destObj  = new Destination(destination, isUpdateSemantics,
                                           isBatchOutputTuple, 
					   propagateHeartbeat);
    addQueryDestination(qId, destObj);
   
  }

  /**
   * Add a destinaton for a query
   * @param id
   *          Query identifier
   * @param destination
   *          EPR for destination
   * 
   * @throws MetadataException
   */
  public void addQueryDestination(int id, String destination)
      throws MetadataException, CEPException
  {
    addQueryDestination(id, new Destination(destination));
  }
  
  /**
   * Add a destinaton for a query
   * @param id
   *          Query identifier
   * @param destination
   *          EPR for destination
   * 
   * @throws MetadataException
   */
  public void addQueryDestination(int id, 
                                   Destination destination)
      throws MetadataException, CEPException
  {
    CacheLock l = null;
    Query q;
    ITransaction txn = execContext.getTransaction();

    try
    {
      execContext.getPlanMgr().getLock().writeLock().lock();
      
      // Get name
      l = findCache(txn, id,true,CacheObjectType.QUERY);
    
      if(l == null)
        throw new MetadataException(MetadataError.INVALID_QUERY_IDENTIFIER, new Object[]{id});
    
      q = (Query) l.getObj();

      q.addQueryDestination(destination);
      
      // If Query has already been started then instantiate and propagate output to it
      if(q.getState() == QueryState.RUN)
      {
        execContext.getPlanMgr().addQueryNewOutput(q, destination);
        execContext.getPlanMgr().propRelns(q);
      }
    }
    finally 
    {
      execContext.getPlanMgr().getLock().writeLock().unlock();
    }
  }

  // This is required when exception in output op occurs.
  public void dropQueryDestination(PhyOpt op) throws CEPException
  {
    ITransaction txn = execContext.getTransaction();
    assert op instanceof PhyOptOutput;
    
    PhyOptOutput output = (PhyOptOutput)op;
    CacheLock l = null;
    Query q;
    try
    {
      execContext.getPlanMgr().getLock().writeLock().lock();
      int qid = output.getQueryId();
      l = findCache(txn, qid,true,CacheObjectType.QUERY);
      
      if(l == null)
        throw new MetadataException(MetadataError.INVALID_QUERY_IDENTIFIER, 
                                    new Object[]{qid});
    
      q = (Query) l.getObj();
      Destination dest = output.getDestination();
      q.removeDestination(dest);
      QueryDeletionContext ctx = new QueryDeletionContext(q);
      output.delete(ctx);
    }
    finally
    {
      execContext.getPlanMgr().getLock().writeLock().unlock();	
    }
  }

  /**
   * Drops an existing query given its name
   * @param qName
   *          The query name
   * 
   * @throws CEPException
   */
  public void dropNamedQuery( String qName, String schema) throws CEPException
  {
    int qId;

    qId = findQuery(qName, schema);
    
    notifyBeforeDropQuery(qName, qId);
    dropQuery(qId, true);
    notifyAfterDropQuery(qName, qId);
  }

  /**
   * Drops an existing query.
   * @param b 
   * @param qName 
   * @param queryId
   *          The query identifier
   * 
   * @throws CEPException
   */
  public void dropQuery(int qryId) throws CEPException
  {
    dropQuery(qryId, true);
  }
  
 
  /**
   * Drops an existing query.
   * @param userOp
   *          Is it an User operation
   * @param queryId
   *          The query identifier
   * 
   * @throws CEPException
   */
  private void dropQuery(int qryId, boolean userOp)
      throws CEPException
  {
    LogLevelManager.trace(LogArea.METADATA_QUERY, LogEvent.MQUERY_DELETE, this, qryId, userOp);
    
    CacheLock l = null;
    Query query = null;
    ITransaction txn = execContext.getTransaction();

    try
    {
      l = findCache(txn, qryId, false, CacheObjectType.QUERY);
      if(l == null)
         throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER, new Object[]{qryId});

      query = (Query) l.getObj(); 
      // Currently only view destination can exist later on we can modify the msg
        if(userOp && execContext.getDependencyMgr().
                     areDependentsPresent(query.getId()))
        throw new MetadataException( MetadataError.VIEW_DESTINATION_EXISTS, new Object[]{query.getName()}); 
    }
    finally
    {
      // Release
      if (l != null)
       release(txn, l);
    }

    l = null;
    query = null;
    Locks locks = null;

    stopQuery(qryId);
        
    locks = deleteCache(txn, qryId);
    if(locks == null)
       throw  new MetadataException(MetadataError.INVALID_QUERY_IDENTIFIER, 
                                    new Object[]{qryId});
    
    l = locks.objLock;
    
    query = (Query) l.getObj();
    
    // Remove all dependencies
    execContext.getDependencyMgr().removeAllDependencies(query.getId(), 
                                                         DependencyType.QUERY,
                                                         query.getSchema());
    
    //Release the created dynamic types (as part of internal cartrige)
    query.releaseDynamicTypes();
  }
  
  public void alterOrderingConstraint(int queryId, 
                                      OrderingKind orderingConstraint,
                                      CEPExprNode parallelPartioningExpr) 
  throws CEPException
  {
    CacheLock l = null;
    Query query = null;
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, queryId, true, CacheObjectType.QUERY);
    if(l == null)
      throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER,
          new Object[]{queryId});
    
    query = (Query) l.getObj(); 

    // Since the Ordering Constraint is changing, the query should be 
    // recompiled the next time around. Hence, set the cached SemQuery
    // and LogPlan to null
    if (query.getUserOrderingConstraint() != orderingConstraint)
    {
      query.setSemQuery(null);
      query.setLogPlan(null);
    }

    //set the user specified orderingConstraint.
    LogUtil.fine(LoggerType.TRACE,
                 "Setting user ordering constraint for query "
                 + query.getName() + " to " + orderingConstraint);

    // set the user specified orderingConstraint.
    query.setUserOrderingConstraint(orderingConstraint);
    
    // save the parallelPartioningExpr also to the query metadata
    if (orderingConstraint == OrderingKind.PARTITION_ORDERED)
      query.setPartitionParallelExprNode(parallelPartioningExpr);
    else
      query.setPartitionParallelExprNode(null);
  }
  
  /**
   * Alter the ordering constraint for the given named query
   * 
   * @param qName the name of the query
   * @param schema the schema 
   * @param orderingConstraint the value of the Ordering constraint to be set
   * @throws CEPException
   */
  public void alterOrderingConstraint(String qName, String schema,
                                      OrderingKind orderingConstraint, 
                                      CEPExprNode parallelPartioningExpr) 
    throws CEPException
  {
    CacheLock l = null;
    Query query;
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, 
                  new Descriptor(qName ,CacheObjectType.QUERY, schema, null), 
                  true);
    
    if(l == null)
      throw new MetadataException( MetadataError.QUERY_NOT_FOUND,
          new Object[]{qName});
    
    query = (Query) l.getObj();

    // If the query is dependent on a partitioned stream source, then user is 
    // not allowed to alter the ordering constraint
    if(query.isDependentOnPartnStream())
    {
      throw new CEPException(MetadataError.CANNOT_ALTER_ORDERING_CONSTRAINT, 
                             qName, orderingConstraint) ;
    }
    
    // Since the Ordering Constraint is changing, the query should be 
    // recompiled the next time around. Hence, set the cached SemQuery
    // and LogPlan to null
    if (query.getUserOrderingConstraint() != orderingConstraint)
    {
      query.setSemQuery(null);
      query.setLogPlan(null);
    }

    //set the user specified orderingConstraint.
    LogUtil.fine(LoggerType.TRACE,
                 "Setting user ordering constraint for query "
                 + qName + " to " + orderingConstraint);
    query.setUserOrderingConstraint(orderingConstraint);
    
    // save the parallelPartioningExpr also to the query metadata
    if (orderingConstraint == OrderingKind.PARTITION_ORDERED)
      query.setPartitionParallelExprNode(parallelPartioningExpr);
    else
      query.setPartitionParallelExprNode(null);

  }
  
  /**
   * Set the start time value for the given named query
   * 
   * @param qName the name of the query
   * @param qId id of the query
   * @param schema the schema 
   * @param startTime the value of the start time for the query
   * @throws CEPException
   */
  public void setQueryStartTime(String qName, int qId, String schema, 
                                long startTime, ExecContext ec)
    throws CEPException
  {
    CacheLock l = null;
    Query query;
    ITransaction txn = execContext.getTransaction();
    
    if(qName != null)
    {
      l = findCache(txn, 
                    new Descriptor(qName ,CacheObjectType.QUERY, schema, null), 
                    true);
    
      if(l == null)
        throw new MetadataException(MetadataError.QUERY_NOT_FOUND,
                                    new Object[]{qName});
    }
    else
    {
      l = findCache(txn, qId, true, CacheObjectType.QUERY);
      
      if(l == null)
        throw new MetadataException(MetadataError.QUERY_NOT_FOUND,
                                    new Object[]{qId});
    }
    
    query = (Query) l.getObj();
    
    //set the provided value as query start time
    query.setQueryStartTime(startTime);
    
    //get views which are referred by the query.
    if(ec.getDependencyMgr().isAnyMasterPresent(query.getId(),
                                                DependencyType.VIEW))
    {
      Integer[] viewIds = 
        ec.getDependencyMgr().getMasters(query.getId(), DependencyType.VIEW);
      
      for(int i=0; i < viewIds.length; i++)
      {
        l = findCache(txn, viewIds[i], false, CacheObjectType.VIEW);
        
        if(l == null)
          throw new MetadataException(MetadataError.VIEW_NOT_FOUND,
                                      new Object[]{viewIds[i]});
        View v = (View) l.getObj();
        int queryId = v.getQueryId();
        release(txn, l);
        setQueryStartTime(null, queryId, schema, startTime, ec);
      }
    } 
    
  }
  
  /**
   * Stops the already running query given its name
   * @param qName
   *          The query name
   * 
   * @throws CEPException
   */
  public void stopNamedQuery( String qName, String schema) throws CEPException
  {
    int qId;

    CacheLock l = null;
    Query query;
    ITransaction txn = execContext.getTransaction();
    
    try{
    	l = findCache(txn, new Descriptor(qName ,CacheObjectType.QUERY, 
                                              schema,null), false);
        if(l == null)
           throw new MetadataException( MetadataError.QUERY_NOT_FOUND,
                                        new Object[]{qName});

        query = (Query) l.getObj();
 
        if(query.getState() != QueryState.RUN)
          return;
    
         //Iterate through all the dest views and verify if they all are 
         // stopped or not. If not then we cannot stop the query
        Integer[] destViews = execContext.getDependencyMgr().
                              getDependents(query.getId(), DependencyType.VIEW);
        if(destViews != null)
        {
          for(int i=0; i<destViews.length; i++)
          {
            View v = execContext.getViewMgr().getView(destViews[i]);
            if(v.getViewState() == ViewState.STARTED)
            {
       	      throw new MetadataException(
                    MetadataError.CANNOT_STOP_QUERY_VIEW_DESTINATION_EXISTS,
                                     new Object[]{query.getName()});
       	    }
          }
        }
        qId = query.getId();
        
     }
     finally
     {
       // Release
       if (l != null)
         release(txn, l);
     }
    
    stopQuery(qId);
  
  }
  
  /**
   * Stops the already running query given its name
   * Today this is referred only when Runtime exception occurs
   * @param qId
   *         Query Id
   * 
   * @throws CEPException
   */
  public void stopQuery(int qId) throws CEPException
  {
    CacheLock l = null;
    Query query;
    
    ITransaction txn = execContext.getTransaction();
    
    try{
      	l = findCache(txn, qId, false, CacheObjectType.QUERY);
          if(l == null)
             throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER,
                                          new Object[]{qId});

          query = (Query) l.getObj();
   
          if(query.getState() != QueryState.RUN)
            return;
      
           //Iterate through all the dest views and verify if they all are 
           // stopped or not. If not then we cannot stop the query
          Integer[] destViews = execContext.getDependencyMgr().
                                getDependents(query.getId(), 
                                              DependencyType.VIEW);
          if(destViews != null)
          {
            for(int i=0; i<destViews.length; i++)
            {
              View v = execContext.getViewMgr().getView(destViews[i]);
              if(v.getViewState() == ViewState.STARTED)
              {
         	    return;
         	  }
            }
          }
          
       }
       finally
       {
         // Release
         if (l != null)
           release(txn, l);
       }
       
     stopQueryInternal(qId);
   
  }
  
  // Called by either query manager or view manager
  // when we have already checked whether the query is running or not
  // whether the query can be stopped or not
  public void stopQueryInternal(int qryId) throws CEPException
  {
    LogLevelManager.trace(LogArea.METADATA_QUERY, LogEvent.MQUERY_STOP,
                          this, qryId);

    CacheLock l = null;
    Query query = null;

    ITransaction txn = execContext.getTransaction();

       try{
         execContext.getPlanMgr().getLock().writeLock().lock();
  
         l = findCache(txn, qryId, true, CacheObjectType.QUERY);
  
         if(l == null)
           throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER, 
                                        new Object[]{qryId});
         
         query = (Query) l.getObj();
         query.setState(QueryState.STOPPED);
         
         notifyStopQuery(query.getName(), qryId);
   
         QueryDeletionContext ctx = new QueryDeletionContext(query);
  
         // First remove all execution operators
         PhyOpt output;

         // delete all query outputs (non-recursively)
         while (true)
         {
           output = execContext.getPlanMgr().getNextQueryOutput(qryId);
           if (output == null)
             break;

           output.delete(ctx);
          }

          PhyOpt root = execContext.getPlanMgr().getQueryRootOpt(qryId);
          // root will be null when query was never started, only compiled
          if(root != null)
          {
            root.delete(ctx);
           // remove it from the list of query outputs
            execContext.getPlanMgr().removeQueryRoot(qryId, root);
          }
         
          // Get all referenced Views for this query
          Integer[] refViews = execContext.getDependencyMgr().
                                   getMasters(query.getId(), DependencyType.VIEW);
      
          if(refViews != null)
          {
            for(int i=0; i<refViews.length; i++)
            {
              int vid = refViews[i].intValue();
              View v = execContext.getViewMgr().getView(vid);
              if(v.getViewState() == ViewState.STARTED)
              {
                Integer[] destQueries = execContext.getDependencyMgr().
                                            getDependents(v.getId(), DependencyType.QUERY);
                boolean canViewStop = true;
                if(destQueries != null)
                {
                  for(int j=0; j<destQueries.length; j++)
                  {
                    int query_id = destQueries[j].intValue();
                    if(getQuery(query_id).getState() == QueryState.RUN)
                    {
                      canViewStop = false;
                      break;
                    }
                  }
                }
                if(canViewStop)
                  execContext.getViewMgr().stopView(vid);
              }
            }
          }
      }
      finally 
      {
        execContext.getPlanMgr().getLock().writeLock().unlock();
      }
  }
  
  public void enableNamedQueryStats(String qName, String schema, 
                                    boolean isBaseTimelineMillisecond) 
  throws CEPException
  {
    int qId;

    qId = findQuery(qName, schema);
    enableQueryStats(qId, isBaseTimelineMillisecond);
  
  }
  
  private void enableQueryStats( int qryId,
                                  boolean isBaseTimelineMillisecond) 
  throws CEPException
  {
    CacheLock l = null;
    Query query = null;
    ITransaction txn = execContext.getTransaction();
 
    l = findCache(txn, qryId, true , CacheObjectType.QUERY);
    if(l == null)
       throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER,
                                    new Object[]{qryId});
      
    query = (Query) l.getObj();
    if(query.getState() != QueryState.RUN)
     throw new MetadataException(
                               MetadataError.CANNOT_ENABLE_OR_DISABLE_STATS, 
                               new Object[]{query.getName()});
      
    PhyOpt root = execContext.getPlanMgr().getQueryRootOpt(qryId);
    boolean enabled = isSourceEnabled(root);
      
    if(!enabled)
      throw new MetadataException(MetadataError.CANNOT_ENABLE_QUERY_STATS,
                                  new Object[]{query.getName()});
      
    query.setIsStatsEnabled(true,isBaseTimelineMillisecond);
        
    execContext.getPlanMgr().setQueryStats(true,
                                    isBaseTimelineMillisecond, qryId);
     
  }
  
  private boolean isSourceEnabled(PhyOpt opt)
  {
    int count = opt.getNumInputs(); 
    if(count == 0)
    {
      // check if the source has enabled Stats flag or not
      if(opt instanceof oracle.cep.phyplan.PhyOptStrmSrc)
      {
         oracle.cep.phyplan.PhyOptStrmSrc src = (oracle.cep.phyplan.PhyOptStrmSrc)opt;
         try {
         Table table = execContext.getTableMgr().getTable(src.getStrId());
         return table.getIsStatsEnabled();
         }catch(MetadataException me)
         {
           return false;
         }
      }
      return true;
    }
   
    boolean enabled = true;
    for(int i=0; i<opt.getNumInputs(); i++)
    {
      if(!isSourceEnabled(opt.getInputs()[i]))
        return false;
    }
    return enabled;
  }
  
  public void disableNamedQueryStats(String qName, String schema) throws CEPException
  {
    int qId;

    qId = findQuery(qName, schema);
    disableQueryStats( qId);
 
  }
  
  private void disableQueryStats(int qryId)
  throws CEPException
  {
   CacheLock l = null;
   Query query = null;
   ITransaction txn = execContext.getTransaction();
   
   l = findCache(txn, qryId, true, CacheObjectType.QUERY);
   if(l == null)
      throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER, new Object[]{qryId});

   query = (Query) l.getObj();
   if(query.getState() != QueryState.RUN)
     throw new MetadataException(MetadataError.CANNOT_ENABLE_OR_DISABLE_STATS,
                                 new Object[]{query.getName()});
   
   query.setIsStatsEnabled(false);
    
   execContext.getPlanMgr().setQueryStats(false, false, query.getId());
      
  }
  
 
 
 // Get all the query ids which are referencing the function id
 // Recursively find out all the queries directly or indirectly referencing the func.
 // Ex: create query q1 as select * from v1; where v1 uses function f1
 // so q1 indirectly references f1.
  public List<Integer> getAllFunctionIdQueries(int fnId)
  {
    List<Integer> QueryList = new LinkedList<Integer>();
    // directly referencing queries
    Integer[] fnDestQr = execContext.getDependencyMgr().
                         getDependents(fnId, DependencyType.QUERY) ;
    if(fnDestQr != null)
    {
      for(int i=0; i<fnDestQr.length; i++)
        QueryList.add(fnDestQr[i]);
                                 
      for(int j=0; j<fnDestQr.length; j++)
      {
        try {
          int qid = fnDestQr[j].intValue();
          Query query = getQuery(qid);
          Integer[] destViews = execContext.getDependencyMgr().
                              getDependents(query.getId(),DependencyType.VIEW);
          if((destViews != null) && (destViews.length > 0))
            getAllDestQueries(destViews, QueryList);
        }catch(MetadataException me)
        {
        
        }
      }
    }
    return QueryList;
  }
  
  private void getAllDestQueries(Integer[] destViews, List<Integer> qList)
  {
    for(int i=0; i<destViews.length; i++)
    {
      int vid = destViews[i].intValue();
      getDestViewQueries(vid, qList);
    }
  }
  
  // get dest queries for this view
  private void getDestViewQueries(int vid, List<Integer> qList) 
  {
      Integer[] destQueries = execContext.getDependencyMgr().
                                  getDependents(vid, DependencyType.QUERY);

      if(destQueries != null)
      {
        for(int i=0; i<destQueries.length; i++)
        {
          int qid = destQueries[i].intValue();
          if(!ifPresentInList(qid, qList))
            qList.add(new Integer(qid));

          Integer[] destViews = execContext.getDependencyMgr().
                                getDependents(qid, DependencyType.VIEW);

          if((destViews != null) && (destViews.length > 0))
            getAllDestQueries(destViews, qList);
        }
      }
  }
  
 // If an id is already present in the list
  private boolean ifPresentInList(int id, List<Integer> list)
  {
    Iterator<Integer> iter = list.iterator();
    while(iter.hasNext())
    {
      int qid = iter.next().intValue();
      if(qid == id)
        return true;
    }
    return false;
  }
  
  /**
   * get all the function ids referenced by query or the view referenced by the query
   */
  public List<Integer> getAllFunctionIds(int qid)
  {
    
    List<Integer> list = new LinkedList<Integer>();
    try {
      Query query = getQuery(qid);
      Integer[] refFuncs = execContext.getDependencyMgr().
                           getMasters(qid, DependencyType.FUNCTION);
      if(refFuncs != null)
      {
        for(int i=0; i<refFuncs.length; i++)
          list.add(refFuncs[i]);
      }
      int[] qIds = getReferencedQueryIds(query);
      if(qIds.length > 0)
      {
        for(int i=0; i<qIds.length; i++)
        {
          List<Integer> list1 = getAllFunctionIds(qIds[i]);
          Iterator<Integer> iter = list1.iterator();
          while(iter.hasNext())
          {
            Integer fnid = iter.next();
            if(!ifPresentInList(fnid.intValue(), list))
              list.add(fnid);
          }
        }
      }
    }
    catch(MetadataException me)
    {
   
    }
    return list;
  }
  
  /**
   * Get the plan for execution. Should be called after endAppSpec() method is
   * called
   * 
   * @return xml representation of the plan
   */
  public String getXMLPlan()
  {
    return (execContext.getPlanMgr().getXMLPlan());
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    return execContext.getPlanMgr().getXMLPlan2();
  }

  /**
   * Get the output schema of a given query. The output schema contains
   * information about:
   * 
   * 1. Whether the output of the query is a stream or relation 2. The type of
   * each attribute of the output query, where the attribute positions are fixed
   * by teh query semantics. (The schema is therefore unnamed)
   * 
   * The schema is encoded as an XML string. For example:
   * 
   * <schema query = "1" type = "stream"> <column type = "int"/> <column type =
   * "float/> <column type = "char" len = "10"/> <column type = "byte"/>
   * </schema>
   * @param queryId
   *          query identifier
   * 
   * @return Schema encoded as XML text
   */
  public String getQuerySchema(int queryId)
  {
    return (execContext.getPlanMgr().getQuerySchema(queryId));
  }
  
  public MetadataStats getQueryStats(int id)
  {
    try {
      Query query = getQuery(id);
      MetadataStats stat = new MetadataStats();
      stat.setName(query.getName());
      if(!query.isNamed && (execContext.getDependencyMgr().
    	                    isAnyDependentPresent(query.getId(), 
    	                                          DependencyType.VIEW)))
      {
        Integer[] destViews = execContext.getDependencyMgr().
                              getDependents(query.getId(), DependencyType.VIEW);
        View view = execContext.getViewMgr().getView(destViews[0].intValue());
        stat.setName(view.getName());
      }
      stat.setText(query.getText());
      stat.setIsMetadata(!(query.isNamed));
      
      // Check if query's schema is marked with CQL_RESERVED_PREFIX
      String serviceName = execContext.getServiceName();
      stat.setInternal(query.getSchema().substring(serviceName != null ? serviceName.length() + 1 : 0).startsWith(
              Constants.CQL_RESERVED_PREFIX));
      
      //set the archiver stats
      stat.setArchiverStats(query.getArchiverStats());
      
      return stat;
    }catch(MetadataException me)
    {
      me.printStackTrace();
    }
    return null;
  }
  
  /**
   * Get information of all registered queries
   * @return a map of <query-name, QueryInfo>
   * @throws CEPException 
   */
  public HashMap<String, QueryInfo> getQueryInfo() throws CEPException
  {
    HashMap<String, QueryInfo> map=
                                 new HashMap<String, QueryInfo>();
    IStorageContext storageCtx = cache.initQuery(NameSpace.QUERY.toString(), 
                                                 execContext.getSchema());
    CacheObject record = null;
    while(true)
    {
      record = (CacheObject)storage.getNextRecord(storageCtx);
      if(record == null)
        break;
      if(record.getType() == CacheObjectType.QUERY)
      {
        Query nextQuery = (Query)record;
        QueryInfo qInfo = new QueryInfo();
        qInfo.setRunning(nextQuery.getState() == QueryState.RUN);
        
        // Determine Ordering Constraints of Query
        qInfo.setOrderingConstraint(LogPlanParallelismHelper.deriveParallelism(nextQuery, getLogPlan(nextQuery.getId())));
        String[] attrs = nextQuery.getNames();
        Datatype[] types = nextQuery.getTypes();
        
        HashMap<String,Datatype> projAttrs = new LinkedHashMap<String,Datatype>();
        for(int i=0; i< attrs.length; i++)
        {
          int indexOfDotOperator = attrs[i].indexOf('.');
          if(indexOfDotOperator != -1)
          {
            projAttrs.put(attrs[i].substring(indexOfDotOperator+1), types[i]);
          }
          else
          {
            projAttrs.put(attrs[i], types[i]);
          }
        }
        qInfo.setOutputAttributes(projAttrs);
        
        map.put(nextQuery.getName(), qInfo);
      }
    }
    return map;
  }
 

  
  public Query getQuery(int id) throws MetadataException
  {
    CacheLock l = null;
    Query query = null;
    ITransaction m_txn = execContext.getTransaction();

    try
    {
      // Get name
      l = findCache(m_txn, id, false,CacheObjectType.QUERY);
    
      if(l == null)
        throw new MetadataException(MetadataError.INVALID_QUERY_IDENTIFIER, new Object[]{id});
    
      query = (Query) l.getObj();
    }
    finally
    {
      // Release
      if (l != null)
        release(m_txn, l);
    }

    return query;
  }
  
  /**
   * Acquires either a read or write lock for query object.
   * @param context Storage Context
   * @param id Query Id of the query
   * @param lockFlag true for WRITE lock and false for READ lock
   * @throws MetadataException Throws the exception if the query id does not 
   *                           corresponds to the query
   * @return Acquires the lock and return 
   */
  public CacheLock getQueryLock(ITransaction txn, int id, boolean lockFlag) 
     throws MetadataException
  {
    CacheLock l = null;
    
    l = findCache(txn, id, lockFlag, CacheObjectType.QUERY);

    if(l == null)
      throw new MetadataException(MetadataError.INVALID_QUERY_IDENTIFIER, new Object[]{id});
  
    return l;    
  }


  /**
   * Returns a list of exprs of the query
   */
  public ArrayList<Expr> getQueryExprs( int qid)
  throws CEPException 
  {
	ITransaction txn = execContext.getTransaction();
    CacheLock l = getQueryLock(txn, qid, true);
    Query query = (Query)l.getObj();
    SemQuery semQuery = query.getSemQuery();
    //  If the query has not been type checked
    if (semQuery == null)
    {
      semQuery = typecheck(query, null);
      if (query.getState() == QueryState.CREATE)
      {
        query.setSemQuery(semQuery);
        query.setState(QueryState.TYPECHECKED);
        query.setDesiredState(QueryState.TYPECHECKED);
      }
    }

    return semQuery.getSelectListExprs();
  }  
  
 
  
  public Datatype[] getOutTypes(int qid) throws CEPException
  { // take write lock
    ITransaction txn = execContext.getTransaction();
    CacheLock querylock = getQueryLock(txn, qid, true);
    Query query =  (Query)querylock.getObj();
    SemQuery semQuery = query.getSemQuery();
    ArrayList<Expr> selExprs;
    Datatype[] types;

    // If the query has not been type checked
    if (semQuery == null)
    {
      semQuery = typecheck(query, null);
      if (query.getState() == QueryState.CREATE)
      {
        query.setSemQuery(semQuery);
        query.setState(QueryState.TYPECHECKED);
        query.setDesiredState(QueryState.TYPECHECKED);
      }
    }
    selExprs = semQuery.getSelectListExprs();
    types = new Datatype[selExprs.size()];
    for (int i = 0; i < selExprs.size(); i++)
    {
      types[i] = selExprs.get(i).getReturnType();
    }
    return types;
  }
  
 
  public String[] getOutNames(int qid) throws CEPException
  {
    Query query = getQuery( qid);
    String names[] = query.getNames();
    assert names != null;
    return names;
  }

  public boolean isStreamQuery(int qid) throws CEPException
  {
    ITransaction txn = execContext.getTransaction();
    CacheLock l = getQueryLock(txn, qid, true);
    Query query = (Query)l.getObj();
    SemQuery semQuery = query.getSemQuery();

    // If the query has not been type checked
    if (semQuery == null)
    {
      semQuery = typecheck(query, null);
      if (query.getState() == QueryState.CREATE)
      {
        query.setSemQuery(semQuery);
        query.setState(QueryState.TYPECHECKED);
        query.setDesiredState(QueryState.TYPECHECKED);
      }
    }

    return semQuery.isStreamQuery();
  }

  public IStorageContext intialize(String schema) 
  {
    return storage.initQuery(NameSpace.QUERY.toString(), schema);
  }

  public CacheKey getNextQuery(IStorageContext cursor) throws MetadataException 
  {
    if (cursor == null)
      throw new MetadataException(MetadataError.QUERY_FETCH_ITERATOR_NOT_INITIALIZED);
    return (CacheKey)storage.getNextKey(cursor);
  }

  /** ChgNotifier stuff */
  public void addQueryChgListener(IQueryChgListener notifier)
  {
    queryChgListeners.add(notifier);
  }
  
  public void removeQueryChgListener(IQueryChgListener notifier)
  {
    queryChgListeners.remove(notifier);
  }
  
  private void notifyAddQuery(String qryName, int qryId)
  {
    for (IQueryChgListener notifier : queryChgListeners)
    {
      try 
      {
        notifier.onQueryAdded(qryName, qryId, execContext.getSchemaName(), execContext);
      } 
      catch(Exception e)
      {
        // eats up any exception, and try next one.
      }
    }
  }

  private void notifyStartQuery(String qryName, int qryId)
  {
    for (IQueryChgListener notifier : queryChgListeners)
    {
      try 
      {
        notifier.onQueryStarted(qryName, qryId,execContext.getSchemaName(), execContext);
      } 
      catch(Exception e)
      {
        // eats up any exception, and try next one.
      }
    }
  }
  
  private void notifyStopQuery(String qryName, int qryId)
  {
    for (IQueryChgListener notifier : queryChgListeners)
    {
      try 
      {
        notifier.onQueryStopped(qryName, qryId, execContext.getSchemaName(), execContext);
      } 
      catch(Exception e)
      {
        // eats up any exception, and try next one.
      }
    }
  }
  
  private void notifyBeforeDropQuery(String qryName, int qryId)
  {
    for (IQueryChgListener notifier : queryChgListeners)
    {
      try 
      {
        notifier.onBeforeQueryDrop(qryName, qryId, execContext.getSchemaName(), execContext);
      } 
      catch(Exception e)
      {
        // eats up any exception, and try next one.
      }
    }
  }

  private void notifyAfterDropQuery(String qryName, int qryId)
  {
    for (IQueryChgListener notifier : queryChgListeners)
    {
      try 
      {
        notifier.onAfterQueryDrop(qryName, qryId, execContext.getSchemaName(), execContext);
      } 
      catch(Exception e)
      {
        // eats up any exception, and try next one.
      }
    }
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetId()
   */
  public int getTargetId()
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetName()
   */
  public String getTargetName()
  {
    return "QueryManager";
  }

  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetType()
   */
  public int getTargetType()
  {
    return 0;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    // All levels are handled by the default implementation.
    // MQUERY_INFO - dumps using the fields specified in DumpDesc annotation
    // MQUERY_LOCKINFO - handled by overriden dump method in this class
  }

  public void dump(IDumpContext dumper) 
  {
    super.dump(dumper, LogTags.TAG_QUERIES, CacheObjectType.QUERY);
  }
  
}
