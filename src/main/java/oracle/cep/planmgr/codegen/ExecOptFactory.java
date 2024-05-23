/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExecOptFactory.java /main/32 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Abstract Factory for instantiating the execution operators

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk  09/10/12 - set eventid column position
 udeshmuk  05/09/12 - set snapshotId in Queue reader context
 sbishnoi  09/29/11 - multiplying snapshot by 1000000 to convert to nanos
 udeshmuk  08/28/11 - pass eventIdColName while calling getTuplesFromResultSet
 udeshmuk  06/28/11 - move common code to initializeExecOptState from other
                      operator factories
 udeshmuk  06/20/11 - stabilise archived relation framework
 udeshmuk  04/05/11 - archived relation support
 anasrini  04/05/11 - Partition parallelism for n-ary ops
 anasrini  03/28/11 - support for reInstantiation
 anasrini  03/20/11 - partition parallelism
 sbishnoi  05/15/09 - optimization to keep unique source objects in operator
                      lineage
 parujain  05/04/09 - lifecycle management
 parujain  04/21/09 - remove store list from execMgr
 anasrini  05/07/09 - system timestamped source lineage
 parujain  03/19/09 - stateless server
 anasrini  11/08/08 - set execOp.requiresBufferedInput
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 parujain  08/01/08 - 
 najain    04/25/08 - add addStore
 hopark    02/25/08 - support paged queue
 sbishnoi  11/26/07 - support for update semantics
 najain    07/10/07 - 
 rkomurav  06/07/07 - fix warnings
 parujain  05/08/07 - maintain isStream flag
 najain    03/14/07 - cleanup
 najain    12/04/06 - stores are not storage allocators
 hopark    11/17/06 - bug 5583899 : removed input/outputs from ExecOpt
 hopark    11/07/06 - bug 5465978 : refactor newExecOpt
 najain    10/28/06 - initialize op in common
 najain    10/17/06 - queues are stored in ExecManager
 anasrini  09/22/06 - set name for execution operator
 anasrini  08/24/06 - set phyOptId
 najain    07/19/06 - ref-count tuples
 najain    07/10/06 - set TupleFactory in outQueue
 najain    06/29/06 - factory allocation cleanup
 najain    06/07/06 - add processExecOpt
 anasrini  03/22/06 - remove references to planMgr
 anasrini  03/21/06 - getInStore
 anasrini  03/16/06 - processing queues
 anasrini  03/11/06 - newExecOpt should throw CEPException
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExecOptFactory.java /main/32 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhyQueueKind;
import oracle.cep.phyplan.PhySharedQueueReader;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IArchiverFinder;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecOptType;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.QueueReaderContext;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.metadata.Query;

/**
 * ExecOptFactory
 *
 * @author skaluska
 */
abstract class ExecOptFactory
{

    protected void initialize(CodeGenContext ctx)
    {
    }
    
    // Factories who need additional information in context can creat their own
    protected CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                                 PhyOpt phyopt)
    {
      return new CodeGenContext(ec, query, phyopt);
    }

    abstract ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException;

    abstract void setupExecOpt(CodeGenContext ctx) throws CEPException;

    /**
     * Instantiate the store.
     * The default implementation is instantiating with the physical operator's
     * store.
     *
     * @param ctx
     * @return
     * @throws CEPException
     */
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        assert ctx.getPhyopt() != null;

        ExecStore outStore = null;
        if (ctx.getPhyopt().getStore() != null)
        {
            outStore = StoreInst.instStore(
                              new StoreGenContext(ctx.getExecContext(), 
                                                  ctx.getPhyopt().getStore()));
            ctx.setTupleStorage(outStore);
            
        }
        return outStore;
    }

    /**
     * Instantiate the execution operator.
     * The operator specific behavior is provided by overriding
     * newExecOpt, instStore, and setupExecOpt.
     *
     * @param ctx
     * @return
     * @throws CEPException
     */
    public ExecOpt instantiate(CodeGenContext ctx) throws CEPException
    {
        // Initialize context
        initialize(ctx);

        ExecContext ec = ctx.getExecContext();
        // Construct an operator
        ExecOpt execOp = newExecOpt(ctx);
        execOp.setRequiresBufferedInput(ctx.getPhyopt().requiresBufferedInput());

        ConfigManager cfgMgr = ec.getServiceManager().getConfigMgr();
        execOp.setTsSlice(cfgMgr.getSchedTimeSlice());
        ctx.setExecOpt(execOp);

        // Set the execution operator in the physical operator
        ctx.getPhyopt().setInstOp(execOp);

        // Set the phyOptId
        execOp.setPhyOptId(ctx.getPhyopt().getId());
        
        // Set whether stream is produced or not
        execOp.setIsStream(ctx.getPhyopt().getIsStream());

        // Set the name of the execution operator
        execOp.setOptName(ctx.getPhyopt().getOperatorKind() + "#" + 
                          ctx.getPhyopt().getId());

        // instStore sets TupleStorage as a side effect.
        // It should be called before invoking getTupleStorageAlloc
        ExecStore outStore = instStore(ctx);
        
        execOp.setTupleStorageAlloc(ctx.getTupleStorage().getFactory());

        // Assumption: There will be no outQueue for an operator of kind OUTPUT
        // and for an operator of kind EXCHANGE
        if(
           (execOp.opttyp != ExecOptType.EXEC_OUTPUT) &&
           (execOp.opttyp != ExecOptType.EXEC_EXCHANGE)
          )
        {         
          // Instantiate the out queue
          ISharedQueueWriter outQueue = instOutQueue(ctx.getPhyopt(), ec);
          assert outQueue != null;
          execOp.setOutputQueue((Queue)outQueue);
          IAllocator<ITuplePtr> factory = outStore.getFactory();
          FactoryManager factoryMgr =
            ec.getServiceManager().getFactoryManager();
          outQueue.setTupleFactory(factoryMgr, factory);
        }
        

        // Set the in store
        if (ctx.getPhyopt().getNumInputs() == 1)
        {
            execOp.setInTupleStorageAlloc(
                        getInputStore(ctx.getPhyopt(), ec, 0).getFactory());

            execOp.setInputQueue(getInputQueue(ctx.getPhyopt(), ec, 0));
        }

        // Setup the system timestamped source lineage
        setupSystsLineage(ctx);
        
        //if event id column name is set in phyopt, set it in execopt.
        if(ctx.getPhyopt().getIsView())
          execOp.setUseEventIdVal(true);
        if(ctx.getPhyopt().getEventIdColNum() != -1)
          execOp.setEventIdColNum(ctx.getPhyopt().getEventIdColNum());

        // Operator specific instantiation
        setupExecOpt(ctx);
        
        // Check if the operator needs to initialize state from archiver.
	/* This won't be needed now as we use a centralized approach
	 * to execute queries. Can be removed in next txn if things work 
	 * without issues */
	/*
        if(ctx.getPhyopt().isQueryOperator())
        {
          initializeExecOptState(ctx);
        }
        */
        // Add the operator to the ExecManager's list
        // This will also add the source operators to the scheduler's list
        execOp.addExecOp();

        return execOp;
    }
    
    protected IArchiver findArchiver(CodeGenContext ctx) throws CEPException
    {
      // Get the Archiver
      ConfigManager cm = ctx.getExecContext().getServiceManager().getConfigMgr();
      
      IArchiverFinder service = cm.getArchiverFinder();
      
      IArchiver archiver = null;
      if(service == null)
      { 
        LogUtil.info(LoggerType.TRACE, "IArchiverFinder service not available!");
        throw new CEPException(CodeGenError.ARCHIVER_FINDER_SERVICE_NOT_AVAILABLE);
      }
      else
      {

        if(ctx.getPhyopt().getArchiverName() != null)
        {
          LogUtil.info(LoggerType.TRACE, "Looking up in IArchiverFinder with key "
                       + ctx.getPhyopt().getArchiverName());
          archiver = service.findArchiver(ctx.getPhyopt().getArchiverName());
          if(archiver == null)
            throw new CEPException(CodeGenError.ARCHIVER_NOT_FOUND, 
                                   new Object[]{ctx.getPhyopt().getArchiverName()});
        }
        else
        {
          LogUtil.info(LoggerType.TRACE, "Specified archiver name is null!");
          throw new CEPException(CodeGenError.ARCHIVERNAME_NOT_SPECIFIED);
        }
      }
      
      return archiver;
      
    }
    
    protected IArchiverQueryResult executeArchiverQuery(CodeGenContext ctx, 
                                                        String archiverQuery, 
                                                        IArchiver archiver)
							throws CEPException
    {
      // Get the current system time/query start time to be set as snapShotTime    
      Long currentTime = System.currentTimeMillis(); //default
      
      // Convert the snapShotTime to nanos
      currentTime = currentTime * 1000000l;
      
      //Check if the start time value is set
      if(ctx.getQuery().getQueryStartTime() != Long.MIN_VALUE)
      {
        currentTime = ctx.getQuery().getQueryStartTime(); 
      }
      
      ctx.getExecOpt().setSnapShotTime(currentTime);
      
      //Prepare query requests and give params if any
      QueryRequest[] requests = 
        new QueryRequest[]{
          new QueryRequest(archiverQuery, null)
        };
      
      // Execute query
      LogUtil.info(LoggerType.TRACE, "About to execute the archiver query for "
                                     +ctx.getPhyopt().getOptName());      
      IArchiverQueryResult results = archiver.execute(requests);
      assert results.getResultCount() == requests.length ;
      return results;
      
    }
    
    protected TupleSpec getArchiverTupleSpec(CodeGenContext ctx) throws CEPException
    {
      //default is to call CodeGenHelper's getTupleSpec.
      return CodeGenHelper.getTupleSpec(ctx.getExecContext(), ctx.getPhyopt()); 
    }
    
    /**
     * Initialize the exec operator state by consulting the archiver
     */
    protected void initializeExecOptState(CodeGenContext ctx) throws CEPException
    {
      LogUtil.info(LoggerType.TRACE, "Initializing state for "
                   +ctx.getPhyopt().getOptName());
      // Get the query
      String archiverQuery = ctx.getPhyopt().getOutputSQL();
      
      //Get the archiver
      IArchiver archiver = findArchiver(ctx);
      
      //execute query
      IArchiverQueryResult results = 
        executeArchiverQuery(ctx, archiverQuery, archiver);
      
      // Get the result set
      ResultSet resultSet = null;
      if(results != null)
      {
        resultSet = results.getResult(0);
      
        // Get the tuple spec
        TupleSpec tupSpec = getArchiverTupleSpec(ctx);
        
        // Form tuple from resultSet
        List<ITuplePtr> tuples = CodeGenHelper.getTuplesFromResultSet(
          resultSet, tupSpec, ctx.getTupleStorage().getFactory(),
          ctx.getPhyopt().getEventIdColName(),
          ctx.getPhyopt().getEventIdColNum(),
          ctx.getPhyopt().isEventIdColAddedToProjClause());
       
        archiver.closeResult(results);
        ctx.getExecOpt().setArchivedRelationTuples(tuples);
        if(tuples != null)
          LogUtil.info(LoggerType.TRACE,
                       "Archiver query for "+ ctx.getPhyopt().getOptName()
                       +" returned "+tuples.size()+" rows");
        else
          LogUtil.info(LoggerType.TRACE, 
              "Archiver query for "+ ctx.getPhyopt().getOptName()
              +"returned null results!");
      }
      else
      {
        ctx.getExecOpt().setArchivedRelationTuples(null);
        LogUtil.info(LoggerType.TRACE, 
                     "Archiver query for "+ ctx.getPhyopt().getOptName()
                     +"returned null results!");
      }
    }

    private void setupSystsLineage(CodeGenContext ctx)
    {
      ExecOpt execOp = ctx.getExecOpt();
      PhyOpt  phyOp  = ctx.getPhyopt();

      // Setup the complete systs source lineage
      Set<PhyOpt>  systsSourceLineagePhy;
      Set<ExecOpt> systsSourceLineageExec = null;
      systsSourceLineagePhy = phyOp.getSystsSourceLineage();
      
      if(systsSourceLineagePhy != null)
      {
        // lazy initialization
        systsSourceLineageExec = new HashSet<ExecOpt>();

        convertPhyToExec(systsSourceLineagePhy, systsSourceLineageExec);
        execOp.setSystsSourceLineage(systsSourceLineageExec);
      }
      
      // Setup the outer (left) input source lineage
      Set<PhyOpt>  outerSystsSourceLineagePhy;
      Set<ExecOpt> outerSystsSourceLineageExec = null ;
      outerSystsSourceLineagePhy = phyOp.getOuterSystsSourceLineage();
      
      if(outerSystsSourceLineagePhy != null)
      {
        // lazy initialization
        outerSystsSourceLineageExec = new HashSet<ExecOpt>();

        convertPhyToExec(outerSystsSourceLineagePhy, 
            outerSystsSourceLineageExec);
        execOp.setAllOuterSystsSourceLineage(outerSystsSourceLineageExec);
      }

      // Setup the inner (right) input source lineage
      Set<PhyOpt>  innerSystsSourceLineagePhy;
      Set<ExecOpt> innerSystsSourceLineageExec = null;
      innerSystsSourceLineagePhy = phyOp.getInnerSystsSourceLineage();
      
      if(innerSystsSourceLineagePhy != null)
      {
        // lazy initialization
        innerSystsSourceLineageExec = new HashSet<ExecOpt>();

        convertPhyToExec(innerSystsSourceLineagePhy, 
            innerSystsSourceLineageExec);
        execOp.setAllInnerSystsSourceLineage(innerSystsSourceLineageExec);
      }
      // Setup the full input source lineage
      Set<PhyOpt>  fullSourceLineagePhy;
      Set<ExecOpt> fullSourceLineageExec = null;
      fullSourceLineagePhy = phyOp.getFullSourceLineage();
      
      if(fullSourceLineagePhy != null)
      {
        // lazy initialization
        fullSourceLineageExec = new HashSet<ExecOpt>();

        convertPhyToExec(fullSourceLineagePhy, fullSourceLineageExec);
        execOp.setFullSourceLineage(fullSourceLineageExec);
      }
      
      // Compute finalOuter = allOuter - allInner 
      // and     finalInner = allInner - allOuter
      execOp.computeEffectiveSystsSourceLineage();
    }  

    private void convertPhyToExec(Set<PhyOpt> phyList, Set<ExecOpt> execList)
    {
      if (phyList == null)
        return;

      Iterator<PhyOpt> phyIter = phyList.iterator();
      ExecOpt e;
      PhyOpt  p;
      while (phyIter.hasNext())
      {
        p = phyIter.next();
        e = p.getInstOp();
        assert e != null : p;
        execList.add(e);
      }
    }

    /**
     * This method is invoked on an already instantiated physical operator 
     * that has changed since its last (re)instantiation call.
     * This can happen as a result of operator sharing in the global plan
     * An example is the EXCHANGE operator.
     *
     * @param ctx Code Generation context
     * @throws CEPException
     */
    public void reInstantiate(CodeGenContext ctx) throws CEPException
    {
      return;
    }


    public void processExecOpt(CodeGenContext ctx)
    {
        // should be never called
        // If you hit here, it means you added an instance of execoptfactory
        // which does not override processExecOpt in OptInst.processMap.
        assert false;
    }

    private ISharedQueueWriter instOutQueue(PhyOpt op, ExecContext ec) 
      throws CEPException
    {
        PhyQueue phyOutQueue = op.getOutQueue();
        if (phyOutQueue != null)
        {
            Queue outQ = 
              QueueInst.instQueue(new QueueGenContext(ec, phyOutQueue, null));
            assert outQ instanceof ISharedQueueWriter;
            ec.getPlanMgr().addPhyQueueWriter(op.getOutQueue());
            return (ISharedQueueWriter) outQ;
        }

        // No OUT queue for this operator
        return null;
    }

    protected void instInQueues(PhyOpt op, ExecContext ec) 
      throws CEPException
    {
      int numInputs = op.getNumInputs();

      for(int i=0; i<numInputs; i++)
      {
        getInputQueue(op, ec, i);
      }
    }

    protected Queue getInputQueue(PhyOpt op, ExecContext ec, int index) 
      throws CEPException
    {
        PhyQueue[] inpQs = op.getInQueues();
        PhyQueue inpQ;
        PhyQueueKind kind;
        Queue execQueue;

        assert inpQs != null;
        assert (index < inpQs.length) : inpQs.length;
        inpQ = inpQs[index];
        assert inpQ != null;

        kind = inpQ.getQueueKind();
        assert kind == PhyQueueKind.READER_Q;

        // Instantiate a ReaderContext for this reader
        QueueReaderContext readerCtx = getReaderContext(ec, op, inpQ, index);
        execQueue = QueueInst.instQueue(new QueueGenContext(ec, inpQ,
                                                            readerCtx));
        ec.getPlanMgr().addPhyQueueReader((PhySharedQueueReader)inpQ);
        return execQueue;
    }


    /**
     * Instantiate a reader context for this Reader
     * @param ec the ExecContext for this CEP Service
     * @param op the physical layer operator in question
     * @param inpQ the input queue for this physical operator corresponding
     *             to input "inputNo"
     * @param inputNo the input number of this queue for this operator
     * @return the ReaderContext for this input of this operator
     */
    protected QueueReaderContext getReaderContext(ExecContext ec, PhyOpt op,
                                                  PhyQueue inpQ,
                                                  int inputNo)
    {
      QueueReaderContext queueCtx = new QueueReaderContext(inputNo);
      return queueCtx;
    }

    protected ExecStore getInputStore(PhyOpt op, ExecContext ec, int index)
    throws CEPException
    {
        PhyStore[] inStores = op.getInStores();
        PhyStore inStore;

        assert inStores != null;
        assert index < inStores.length : inStores.length;
        inStore = inStores[index];
        assert inStore != null;

        ExecStore st = inStore.getInstStore();
        return st;
    }

   

}
