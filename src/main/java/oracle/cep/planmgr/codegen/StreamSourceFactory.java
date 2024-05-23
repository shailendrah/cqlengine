/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/StreamSourceFactory.java /main/20 2013/04/30 11:44:35 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Factory for the Stream Source Execution operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi  04/07/13 - XbranchMerge sbishnoi_bug-15962405_ps6 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  02/18/13 - allow disabling timeout heartbeat
 udeshmuk  06/03/12 - create archiver tuple comparator and set in exec opt
 udeshmuk  04/23/12 - set workerId, txnId column num
 udeshmuk  03/19/12 - set numRows in execopt for archived stream
 udeshmuk  01/13/12 - set tsColNum in execopt
 anasrini  04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 anasrini  03/20/11 - partition parallelism
 anasrini  12/20/10 - remove eval.setEvalContext
 sbishnoi  04/26/10 - process requireHbtTimeOut flag
 sborah    10/14/09 - support for bigdecimal
 sborah    07/15/09 - support for bigdecimal
 parujain  03/19/09 - stateless server
 sborah    11/24/08 - support for altering base timeline
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 mthatte   04/03/08 - adding setDerivedTS()
 udeshmuk  12/17/07 - set isSystemtimestamped and timeoutDuration fields.
 anasrini  08/27/07 - support for ELEMENT_TIME
 parujain  05/10/07 - Stats enabled flag
 parujain  04/26/07 - store stream id in execution operator
 najain    12/04/06 - stores are not storage allocators
 hopark    11/09/06 - bug 5465978 : refactor newExecOpt
 anasrini  09/22/06 - set exec opt name
 anasrini  09/13/06 - get attribute names
 najain    07/19/06 - ref-count tuples
 najain    06/29/06 - factory allocation cleanup
 najain    06/18/06 - cleanup
 najain    05/04/06 - sharing support
 najain    04/18/06 - time is a part of tuple
 najain    04/06/06 - cleanup
 skaluska  04/04/06 - add tsStorageAlloc
 najain    03/30/06 - bug fixes
 anasrini  03/20/06 - fix up stores related
 najain    03/17/06 - fix problems
 anasrini  03/16/06 - process queues
 anasrini  03/15/06 - get tableSource from tableMgr
 anasrini  03/10/06 - Implementation
 anasrini  03/07/06 - implementation
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/StreamSourceFactory.java /main/20 2013/04/30 11:44:35 sbishnoi Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.sql.Timestamp;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.OrderingKind;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.comparator.ArchiverTupleComparator;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.operators.ConcurrentStreamSource;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.StreamSource;
import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.TableManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptStrmSrc;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;


/**
 * StreamSourceFactory
 *
 * @author skaluska
 * @since 1.0
 */
class StreamSourceFactory extends ExecOptFactory
{
    /*
   * (non-Javadoc)
   *
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.planmgr.codegen.CodeGenContext)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op = ctx.getPhyopt();
      assert op instanceof PhyOptStrmSrc;

      int numAttrs = op.getNumAttrs();

      if (op.getOrderingConstraint() != OrderingKind.TOTAL_ORDER)
      {
        LogUtil.fine(LoggerType.TRACE, 
                     "Instantiating a ConcurrentStreamSource for " 
                     + op.getId() + " with ordering kind as "
                     + op.getOrderingConstraint());
        return new ConcurrentStreamSource(ctx.getExecContext(), numAttrs);
      }
      else
        return new StreamSource(ctx.getExecContext(), numAttrs);
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof StreamSource;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptStrmSrc;

        ExecContext ec = ctx.getExecContext();
        StreamSource sourceOp = (StreamSource) ctx.getExecOpt();
        PhyOptStrmSrc ssop = (PhyOptStrmSrc) op;
        int numAttrs;
        String[] attrNames;
        int strId;
        TableSource source = null;
        
        // Create the execution operator
        strId               = ssop.getStrId();
        numAttrs            = op.getNumAttrs();
        TableManager tblMgr = ec.getTableMgr();
        attrNames           = tblMgr.getAttrNames(strId);

        // TODO: move this approprately later
        FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
        
        // Handle ELEMENT_TIME outside the loop
        for (int i = 0; i < numAttrs - 1; i++)
        {
          sourceOp.addAttr(attrNames[i], op.getAttrMetadata(i), new Column(i));
        }
        // Handle ELEMENT_TIME here
        int pc = numAttrs - 1;
        sourceOp.addAttr(StreamPseudoColumn.ELEMENT_TIME.getColumnName(),
                         new AttributeMetadata(op.getAttrMetadata(pc).getDatatype(), 
                         op.getAttrMetadata(pc).getLength(),
                         op.getAttrMetadata(pc).getDatatype().getPrecision(), 
                         0), new Column(pc));

        // Set the table source
        source = ec.getExecMgr().getTableSource(strId);
        sourceOp.setSource(source);

        if (ssop.getOrderingConstraint() == OrderingKind.TOTAL_ORDER)
          source.setExecOp(sourceOp);
        else 
        {
          source.setUnorderedExecOp(sourceOp);
          
          if (ssop.getOrderingConstraint() == OrderingKind.PARTITION_ORDERED)
            source.setPropagateHeartbeatforUnordered(true);
        }

        String optName = tblMgr.getTable(strId).getName();
        sourceOp.setOptName(optName + "#" + ssop.getId());
        
        sourceOp.setStreamId(strId);
        
        //enable stats
        sourceOp.setIsStatsEnabled(tblMgr.getTable(strId).getIsStatsEnabled(),
                                    tblMgr.getTable(strId).getIsBaseTimelineMillisecond());
        
        // set isSystemTimestamped
        boolean isSysTs = tblMgr.isSystemTimestamped(strId);
        sourceOp.setIsSystemTimestamped(isSysTs);
        
        if(ctx.getPhyopt().getWorkerIdColNum() != -1)
          sourceOp.setWorkerIdColNum(ctx.getPhyopt().getWorkerIdColNum());
        if(ctx.getPhyopt().getTxnIdColNum() != -1)
          sourceOp.setTxnIdColNum(ctx.getPhyopt().getTxnIdColNum());
        // set tsColNum and tsColType - used only for archived stream
        sourceOp.setTsColNum(ssop.getTsColNum());
        sourceOp.setTsColType(ssop.getTsColType());
        
        //With the combined union query approach (for all query operators in the plan)
        //we cannot have order by based on timestamp column.
        //This is because ORDER BY is not allowed within a sub-query of the 
        //UNION query. So we have to do the sorting ourselves.
        sourceOp.setIsReplayRange(tblMgr.getTable(strId).isReplayRange());
        sourceOp.setAscArchiverTupleComparator(
          new ArchiverTupleComparator(ssop.getTsColNum(), ssop.getTsColType(), true));
        sourceOp.setDescArchiverTupleComparator(
          new ArchiverTupleComparator(ssop.getTsColNum(), ssop.getTsColType(), false));
        sourceOp.setNumRows(tblMgr.getTable(strId).getReplayRows());
	        
        // set heartbeat timeout duration
        long timeout = tblMgr.getTable(strId).getTimeout();
        if(timeout == -1)
        {
          boolean isExplicitTimeout = tblMgr.getTable(strId).isExplicitTimeout();
          if(ssop.isHbtTimeoutRequired() && !isExplicitTimeout)
          {
            // Set the nanosecond timeout
            timeout = Constants.DEFAULT_HBT_TIMEOUT_NANOS;
          }
        }
        sourceOp.setTimeoutDuration(timeout);
        
        //If this stream has a derived timestamp, set the timestamp expr in sourceOp.
        boolean isDerivedTS = tblMgr.getTable(strId).isDerivedTs();
        sourceOp.setDerivedTS(isDerivedTS);
       
        //If it has a derived Timestamp, initialize all that is required to evaluate it.
        if(isDerivedTS)
        {
          IAEval               outEval;
          IEvalContext         evalContext;
          EvalContextInfo      evalCtxInfo;
          TupleSpec                     st;
          ConstTupleSpec                ct;
          IAllocator<Object>            stf;
          IAllocator<Object>            ctf;
          ITuplePtr                       t;
          Expr                       tsExpr;
          
          // Create the evaluation context and instantiate the expressions
          outEval = AEvalFactory.create(ec);
          evalContext = EvalContextFactory.create(ec);
          evalCtxInfo = new EvalContextInfo(factoryMgr);
          tsExpr = ssop.getDerivedTs();
          sourceOp.setDtsType(tsExpr.getType());
          assert tsExpr != null;

          int[] inpRoles = new int[1];
          inpRoles[0] = IEvalContext.INPUT_ROLE;
          ExprHelper.instExprDest(ec, tsExpr, outEval, evalCtxInfo,
                                  IEvalContext.NEW_OUTPUT_ROLE, 0, 
                                  inpRoles);

          outEval.compile();
          // Scratch Tuple
          st = evalCtxInfo.st;
          if (st != null) {
              stf = factoryMgr.get(st);

              t = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
              evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
          }

          // Constant Tuple
          ct = evalCtxInfo.ct;
          if (ct != null) {
              ctf = factoryMgr.get(ct.getTupleSpec());

              t = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
              ct.populateTuple(ec, t);
              evalContext.bind(t, IEvalContext.CONST_ROLE);
          }
          
          //Sets the evaluator and evaluation context for the derived timestamp.
          sourceOp.setEvalContext(evalContext);
          sourceOp.setDerivedTSEvaluator(outEval);
        }
        
        // Initialize the execution operator
        sourceOp.initialize();
    }
    
    protected IArchiverQueryResult executeArchiverQuery(CodeGenContext cgctx,
      String archiverQuery,
      IArchiver archiver) throws CEPException
    {
      PhyOptStrmSrc pStrmSrc = (PhyOptStrmSrc) cgctx.getPhyopt();
      boolean isReplayRange = false;
     
      isReplayRange = 
          cgctx.getExecContext().getTableMgr().getTable(pStrmSrc.getStrId())
          .isReplayRange();
      
      QueryRequest[] requests = null;
       
      if(isReplayRange)
      { //range case : need to send parameter
        //             no need to set snapshottime in exec
        // Get the current system time/query start time to be set as snapShotTime
        // Multiply it with 10^6 to make it in nanosecond unit
        Long currentTime = System.currentTimeMillis() * 1000000l ; //default

        //Check if the start time value is set
        if(cgctx.getQuery().getQueryStartTime() != Long.MIN_VALUE)
        {
          LogUtil.info(LoggerType.TRACE, 
            "Using user specified query start time as snapshot time");
          currentTime = cgctx.getQuery().getQueryStartTime(); 
        }
        
        //find out the type of the TIMESTAMP column : ts or bigint
        if(pStrmSrc.getTsColType() == Datatype.BIGINT)
        {
          //bigint - so no need to convert currenttime to nanos
	  //         since the value would already be in nanos
          requests = new QueryRequest[] {
            new QueryRequest(archiverQuery,
                             new Object[]{currentTime})
          };
          LogUtil.info(LoggerType.TRACE,
              "Snapshot time sent as parameter "+currentTime);
        }
        else
        {
          //timestamp - create a java.sql.timestamp instance out of currenttime
          java.sql.Timestamp ts = new java.sql.Timestamp(currentTime/1000000l);
          requests = new QueryRequest[] {
            new QueryRequest(
                  archiverQuery,
                  new Object[]{ts})
          };
          LogUtil.info(LoggerType.TRACE,
              "Snapshot time sent as parameter "+ts);
        }
        
      }
      else
      { // rows case : no parameter needed, no need to set snapshottime in exec
        requests = 
          new QueryRequest[]{
            new QueryRequest(archiverQuery, null)
          };
      }
      
      // Execute query
      LogUtil.info(LoggerType.TRACE, "About to execute the archiver query for "
        +cgctx.getPhyopt().getOptName());      
      IArchiverQueryResult results = archiver.execute(requests);
      assert results.getResultCount() == requests.length ;
      return results;
    }

}
