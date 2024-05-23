/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RelSourceFactory.java /main/28 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
   Declares RelSourceFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    udeshmuk  08/31/15 - set extSource in phyoptrelnsrc
    pkali     10/07/13 - Refactored for primary key scenario to set both
                         keyscanId and scanId (bug 17363573)
    vikshukl  10/04/12 - archived dimension
    udeshmuk  04/16/12 - set worker id and txn id
    udeshmuk  09/07/11 - synopsis may not be present
    udeshmuk  08/29/11 - set eventIdColNum in RelSource execopt
    udeshmuk  06/29/11 - support for archived relation
    anasrini  06/14/11 - XbranchMerge anasrini_bug-12654099_ps5 from
                         st_pcbpel_11.1.1.4.0
    anasrini  06/13/11 - handle setPropagateHeartbeatforUnordered
    anasrini  04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
    anasrini  03/24/11 - support for PARTITION parallelism
    anasrini  12/20/10 - remove eval.setEvalContext
    sbishnoi  04/26/10 - process requireHbtTimeOut flag
    sborah    10/14/09 - support for bigdecimal
    sbishnoi  10/01/09 - table function support
    sborah    07/16/09 - support for bigdecimal
    parujain  03/19/09 - stateless server
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    hopark    10/07/08 - use execContext to remove statics
    hopark    02/25/08 - support paged queue
    sbishnoi  12/18/07 - added isPrimaryKeyAttrNullEval 
    udeshmuk  12/17/07 - setting isSystemTimestamped and timeoutDuration
                         fields.
    parujain  11/09/07 - external source
    sbishnoi  10/29/07 - support for primary key
    hopark    09/04/07 - eval optimize
    parujain  04/26/07 - store relation id in execution operator
    najain    04/11/07 - bug fix
    najain    12/04/06 - stores are not storage allocators
    hopark    11/09/06 - bug 5465978 : refactor newExecOpt
    anasrini  09/22/06 - set name for exec opt
    anasrini  09/13/06 - get attribute names
    parujain  08/11/06 - cleanup planmgr
    parujain  08/04/06 - Timestamp datastructure
    najain    07/19/06 - ref-count tuples
    najain    07/05/06 - cleanup
    najain    06/29/06 - factory allocation cleanup
    najain    06/18/06 - cleanup
    najain    06/16/06 - bug fix
    najain    06/13/06 - bug fix
    najain    06/09/06 - query addition support
    najain    06/07/06 - add processExecOpt
    najain    06/04/06 - add full scan
    najain    05/15/06 - implementation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RelSourceFactory.java /main/28 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import java.util.ArrayList;
import java.util.BitSet;

import oracle.cep.common.Constants;
import oracle.cep.common.OrderingKind;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ConcurrentRelSource;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.interfaces.input.ExtSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.TableManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRelnSrc;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * RelSourceFactory
 *
 * @author najain
 */
public class RelSourceFactory extends ExecOptFactory
{

    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.planmgr.codegen.CodeGenContext)
   */

    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op = ctx.getPhyopt();
      int numAttrs = op.getNumAttrs();

      // We only support concurrent relation source for partition ordered.
      if (op.getOrderingConstraint() == OrderingKind.PARTITION_ORDERED)
      {
        LogUtil.fine(LoggerType.TRACE, 
                     "Instantiating a ConcurrentRelSource for " 
                     + op.getId() + " with ordering kind as "
                     + op.getOrderingConstraint());
        return new ConcurrentRelSource(ctx.getExecContext(), numAttrs);
      }
      else
        return new RelSource(ctx.getExecContext(), numAttrs);

    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
      boolean isPrimaryKeyExist;
      ArrayList<oracle.cep.metadata.Attribute> primaryKeyAttrList;
      ArrayList<Integer> primaryKeyAttrPos = new ArrayList<Integer>();
      
      ExecContext ec = ctx.getExecContext();
      assert ctx.getExecOpt() instanceof RelSource;
      PhyOpt op = ctx.getPhyopt();
      assert op instanceof PhyOptRelnSrc;

      RelSource relSource = (RelSource) ctx.getExecOpt();
      PhyOptRelnSrc rsop  = (PhyOptRelnSrc) op;

      // Create the execution operator
      int relId           = rsop.getRelId();
      int numAttrs        = op.getNumAttrs();
      
      TableManager tblMgr =ec.getTableMgr();
      String[] attrNames = tblMgr.getAttrNames(relId);
      
      isPrimaryKeyExist = 
        tblMgr.getTable(relId).getIsPrimaryKeyExist();
      primaryKeyAttrList = 
        tblMgr.getTable(relId).getPrimaryKeyAttrList();

      for (int i = 0; i < numAttrs; i++)
      {
          relSource.addAttr(attrNames[i], op.getAttrMetadata(i));
          if(!isPrimaryKeyExist)
            continue;
          // Obtain primary key attribute's position list
          for(int j=0; j < primaryKeyAttrList.size(); j++)
          {
            if(primaryKeyAttrList.get(j).getName().equalsIgnoreCase
                (attrNames[i]))
              primaryKeyAttrPos.add(i);
          }
      }
      
      relSource.setIsPrimarKeyExist(isPrimaryKeyExist);
      if(ctx.getPhyopt().getEventIdColNum() != -1)
        relSource.setEventIdColNum(ctx.getPhyopt().getEventIdColNum());
      if(ctx.getPhyopt().getWorkerIdColNum() != -1)
        relSource.setWorkerIdColNum(ctx.getPhyopt().getWorkerIdColNum());
      if(ctx.getPhyopt().getTxnIdColNum() != -1)
        relSource.setTxnIdColNum(ctx.getPhyopt().getTxnIdColNum());

      if(!rsop.isExternal())
      {
         // Shared Evaluation context
        IEvalContext evalContext = EvalContextFactory.create(ec);

        // Create the output synopsis
        PhySynopsis p_outSyn = rsop.getOutSyn();
        RelationSynopsisImpl e_outSyn = null;
        if(p_outSyn != null)
        {
          assert p_outSyn.getKind() == SynopsisKind.REL_SYN : p_outSyn.getKind();
          ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
          allCtx.setOpt(op);
          allCtx.setObjectType(RelationSynopsisImpl.class.getName());
          e_outSyn = (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
          p_outSyn.setSyn(e_outSyn);

          HashIndex keyScanIdx = new HashIndex(ec);
          HashIndex scanIdx    = new HashIndex(ec);
      
          // Initialize the index
          if(isPrimaryKeyExist)
          {
            this.initIndex(ec, rsop, evalContext, keyScanIdx, 
                           ctx.getTupleStorage().getFactory(), primaryKeyAttrPos);
            relSource.setPrimaryKeyAttrPos(primaryKeyAttrPos);

            int keyScanId     = e_outSyn.setIndexScan(null, keyScanIdx);
            relSource.setKeyScanId(keyScanId);
            
            // Check If Primary Key attributes are not null
            IBEval isPrimaryKeyAttrNullEval
              = getIsPrimaryKeyNullEval(ec, op, primaryKeyAttrPos);
            relSource.setIsPrimaryKeyAttrNull(isPrimaryKeyAttrNullEval);
            
          }
          
          //always calculate the scanId for all the columns
          //because for handling minus tuple in the case primary key 
          //we need to compare all the columns
          initIndex(ec, rsop, evalContext, scanIdx, ctx.getTupleStorage().getFactory());
          int scanId = e_outSyn.setIndexScan(null, scanIdx);
          relSource.setScanId(scanId);
        
         // Equality scan
          int fullScanId = e_outSyn.setFullScan();
  
          e_outSyn.setEvalContext(evalContext);
          e_outSyn.initialize();
        
          relSource.setFullScanId(fullScanId);
        }
        
        relSource.setSynopsis(e_outSyn);
        relSource.setEvalContext(evalContext);
        
        if(e_outSyn != null)
        {
          ExecStore outStore = ctx.getTupleStorage();
          assert (outStore != null);
          assert (outStore instanceof RelStoreImpl);
          relSource.setRelStore((RelStoreImpl) outStore);
          e_outSyn.setStore((RelStore)outStore);
          int id = outStore.addStub();
          e_outSyn.setStubId(id);
        }
      }
      
      // Set the table source
      TableSource source = ec.getExecMgr().getTableSource(relId);
      relSource.setSource(source);

      // We only support concurrent relation source for partition ordered.
      if (rsop.getOrderingConstraint() == OrderingKind.PARTITION_ORDERED)
      {
        source.setUnorderedExecOp(relSource);
        source.setPropagateHeartbeatforUnordered(true);
      }
      else
        source.setExecOp(relSource);

      relSource.setRelnId(relId);
      
      String optName = tblMgr.getTable(relId).getName();
      relSource.setOptName(optName + "#" + rsop.getId());
      
      relSource.setStreamId(relId);

      // check if external source
      relSource.setIsExternal(rsop.isExternal());
      
      // check if archived dimension
      if (rsop.isArchivedDim())
        relSource.setArchivedDim(true);
      
      // set isSystemTimestamped and heartbeat timeout duration
      boolean isSysTs = tblMgr.isSystemTimestamped(relId);
      relSource.setIsSystemTimestamped(isSysTs);
      long timeout = tblMgr.getTable(relId).getTimeout();
      if(timeout == -1)
      {
        if(rsop.isHbtTimeoutRequired())
        {
          // Set the heartbeat timeout reminder interval to default value
          timeout = Constants.DEFAULT_HBT_TIMEOUT_NANOS;
        }
      }
      relSource.setTimeoutDuration(timeout);
      
      // Initialize the execution operator
      relSource.initialize();
        
      if(rsop.isExternal())
      {
    	rsop.setExternalTableSource((ExtSource)relSource.getSource());
        rsop.setExtConnection(relSource.getExtConnection());
      }
    }
    
    /**
     * Get the evaluator instance to check whether primary key attributes
     * inside Tuple contains null or not.
     * @param ec Execution Context
     * @param op Physical operator representation for this operator
     * @param primaryKeyAttrPos
     * @return
     * @throws ExecException
     */
    private IBEval getIsPrimaryKeyNullEval(ExecContext ec, 
                                          PhyOpt op, ArrayList<Integer> primaryKeyAttrPos)
      throws ExecException
    {
      PhyOptRelnSrc relOp = (PhyOptRelnSrc)op;
      BInstr instr; 
      IBEval bEval  = BEvalFactory.create(ec);
      IAEval aEval  = AEvalFactory.create(ec);
      for (int i =0; i < primaryKeyAttrPos.size(); i++)
      {
        instr = new BInstr();
        instr.op = ExprHelper.getNullOp(relOp.getAttrTypes(i), aEval);
        instr.r1 = IEvalContext.UPDATE_ROLE;
        instr.c1 = new Column(primaryKeyAttrPos.get(i));
        bEval.addInstr(instr);
      }
      aEval.compile();
      bEval.compile();
      return bEval;
    }
    
    /**
     * Build Indexes of all Attributes of a Tuple
     * @param ec TODO
     * @param op
     * @param evalCtx
     * @param idx
     * @param factory
     * @throws ExecException
     */
    private void initIndex(ExecContext ec, PhyOptRelnSrc op,
                           IEvalContext evalCtx,    HashIndex idx, IAllocator<ITuplePtr> factory)
      throws ExecException
    {
      int numComparisonAttrs;
      ArrayList<Integer> comparisonAttrPos;
      numComparisonAttrs = op.getNumAttrs();
      comparisonAttrPos  = new ArrayList<Integer>();
      for(int i = 0; i < numComparisonAttrs; i++)
        comparisonAttrPos.add(i);
      initIndex(ec, op, evalCtx, idx, factory, comparisonAttrPos);
      
    }
    
    /**
     * Build Index of explicitly mentioned Attributes of a Tuple
     * @param ec TODO
     * @param op
     * @param evalCtx
     * @param idx
     * @param factory
     * @param comparisonAttrPos an Explicit list of Attributes on which index
     *   need to be constructed
     * @throws ExecException
     */
    private void initIndex(ExecContext ec, PhyOptRelnSrc op,
                           IEvalContext evalCtx,    HashIndex idx,
                           IAllocator<ITuplePtr> factory, ArrayList<Integer> comparisonAttrPos) 
      throws ExecException
    {
      int numComparisonAttrs;
      numComparisonAttrs = comparisonAttrPos.size();
      
      int attrPos = 0;
      IHEval updateHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
      
      for (int a = 0; a < numComparisonAttrs; a++) 
      {
        attrPos = comparisonAttrPos.get(a);
        HInstr hinstr = new HInstr(op.getAttrTypes(attrPos), 
        IEvalContext.UPDATE_ROLE, new Column(attrPos));
        
        updateHash.addInstr(hinstr);
      }
      updateHash.compile();
      
      IHEval scanHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
      for (int a = 0; a < numComparisonAttrs; a++)
      {
        attrPos = comparisonAttrPos.get(a);
        HInstr hinstr = new HInstr(op.getAttrTypes(attrPos),
        IEvalContext.UPDATE_ROLE, new Column(attrPos));
        
        scanHash.addInstr(hinstr);
      }
      scanHash.compile();
      
      IBEval keyEqual = BEvalFactory.create(ec);
      for (int a = 0; a < numComparisonAttrs; a++)
      {
        attrPos = comparisonAttrPos.get(a);
        BInstr binstr = new BInstr();
        
        binstr.op = ExprHelper.getEqOp(op.getAttrTypes(attrPos));
        
        // lhs
        binstr.r1 = IEvalContext.UPDATE_ROLE;
        binstr.c1 = new Column(attrPos);
        binstr.e1 = null;
        
        // rhs
        binstr.r2 = IEvalContext.SCAN_ROLE;
        binstr.c2 = new Column(attrPos);
        binstr.e2 = null;
        
        keyEqual.addInstr(binstr);
      }
      
      keyEqual.compile();

      idx.setUpdateHashEval(updateHash);
      idx.setScanHashEval(scanHash);
      idx.setKeyEqual(keyEqual);
      idx.setEvalContext(evalCtx);
      idx.setFactory(factory);
      idx.initialize();
    }

    public void processExecOpt(CodeGenContext ctx)
    {
      Query query = ctx.getQuery();
      PhyOpt op = ctx.getPhyopt();

      assert op instanceof PhyOptRelnSrc;
      int relnId = ((PhyOptRelnSrc)op).getRelId();

      // Add the relation to the query, if not already done so
      if (!query.isRefRelnPresent(relnId))
      {
          ExecOpt execOp = op.getInstOp();
          assert execOp != null;
          assert (execOp instanceof RelSource);

          Queue q = ((RelSource) execOp).getOutputQueue();
          assert q != null;
          assert q instanceof ISharedQueueWriter;
          BitSet readers = (BitSet)(((ISharedQueueWriter)q).getReaders().clone());

          query.addRefReln(relnId, readers);
      }

    }
}
