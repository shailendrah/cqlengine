/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/OutputFactory.java /main/16 2012/04/02 03:50:32 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
  Factory for the Output Execution operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    sbishnoi  02/16/12 - create synopsis when batching is enabled
    sbishnoi  02/02/12 - support of batching and update semantics together
    anasrini  12/20/10 - remove eval.setEvalContext
    udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                         st_pcbpel_11.1.1.4.0
    udeshmuk  09/01/10 - add propagateHeartbeat
    sbishnoi  12/09/09 - output batching
    sborah    07/16/09 - support for bigdecimal
    sbishnoi  06/24/09 - support to process new EPR flag accumulateOutputTuples
    parujain  03/19/09 - stateless server
    sborah    11/24/08 - support for altering base timeline
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    anasrini  09/14/08 - temp fix on number of attrs
    sbishnoi  11/26/07 - support for update semantics
    parujain  05/10/07 - enable stats for new outputs for already running query
    najain    12/04/06 - stores are not storage allocators
    rkomurav  11/29/06 - remove the workaround for bug 5407652
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    najain    10/29/06 - set various fields
    najain    08/31/06 - add name
    najain    06/18/06 - cleanup
    najain    05/11/06 - set output driver
    najain    03/31/06 - fix bugs
    anasrini  03/24/06 - issue related to inStorageAlloc
    anasrini  03/20/06 - fix up stores related
    najain    03/17/06 - Fix addAttr invocation
    anasrini  03/16/06 - process queues
    anasrini  03/15/06 - implementation
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */
 
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/OutputFactory.java /main/16 2012/04/02 03:50:32 sbishnoi Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.util.ArrayList;
import java.util.LinkedList;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.OrderingKind;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.ConcurrentOutput;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.Output;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOutput;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * OutputFactory - Factory for the Output Execution operator
 *
 * @author skaluska
 */
public class OutputFactory extends ExecOptFactory
{

    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.planmgr.codegen.CodeGenContext)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException {
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptOutput;

        int numAttrs = op.getNumAttrs();

        if (op.getOrderingConstraint() == OrderingKind.UNORDERED)
          return new ConcurrentOutput(ctx.getExecContext(), numAttrs);
        else
          return new Output(ctx.getExecContext(), numAttrs);
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof Output;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptOutput;

        ExecContext ec = ctx.getExecContext();
        Output outExecOp = (Output) ctx.getExecOpt();
        PhyOptOutput outPhyOp = (PhyOptOutput) op;
        int          numAttrs;
       /* Datatype[]   dts;
        int[]        attrLen;
        int[]        attrPrecision;
        int[]        attrScale;*/
        AttributeMetadata[] attrMetadata;
        int                 queryId;
        QueryOutput         dest = null;
        
        Query             query;
        
        /** Check whether primary key exists*/
        boolean           isPrimaryKeyExist;
        
        /** Check whether Update tuples allowed in output*/
        boolean           isUpdateSemantics;
        
        LinkedList<String> outputConstraintAttrs;
        ArrayList<Integer> primaryKeyAttrPos = new ArrayList<Integer>();
        
        String[] attrNames = ctx.getQuery().getNames(); 
        numAttrs      = attrNames.length;
        /*dts           = op.getAttrTypes();
        attrLen       = op.getAttrLen();
        attrPrecision = op.getAttrPrecision();
        attrScale     = op.getAttrScale();*/
        attrMetadata  = op.getAttrMetadata();
        queryId       = outPhyOp.getQueryId();
        
        query = ec.getQueryMgr().getQuery(queryId);
        
        outputConstraintAttrs = query.getOutputConstraintAttrs();
        isPrimaryKeyExist     = outPhyOp.getIsPrimaryKeyExists();
        isUpdateSemantics     = outPhyOp.getIsUpdateSemantics();
        
        // Add Attribute to ExecOp
        // Also determine primary key attribute's position
        for (int i=0; i < numAttrs; i++)
        {
          outExecOp.addAttr(attrMetadata[i], i, attrNames[i] );
          if(!isPrimaryKeyExist)
            continue;
          for(int j=0; j < outputConstraintAttrs.size(); j++)
          {
            if(outputConstraintAttrs.get(j).equalsIgnoreCase(attrNames[i]))
              primaryKeyAttrPos.add(i);
          }
        }
        
        outExecOp.setIsPrimaryKeyExist(isPrimaryKeyExist);
        outExecOp.setIsUpdateSemantics(isUpdateSemantics);
        outExecOp.setPrimaryKeyAttrPos(primaryKeyAttrPos);
        
        // check whether batching is enabled for output tuples
        boolean isBatchOutputTuples = outPhyOp.isBatchOutputTuples();
        
        // Initialize the index if primary key Exist
        if(isPrimaryKeyExist || isBatchOutputTuples)
        {
          // Shared Evaluation context
          IEvalContext evalContext = EvalContextFactory.create(ec);

          // Create the output synopsis
          PhySynopsis p_outSyn = outPhyOp.getOutputSyn();

          assert p_outSyn != null;
          assert p_outSyn.getKind() == SynopsisKind.REL_SYN : p_outSyn.getKind();

          ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
          allCtx.setOpt(op);
          allCtx.setObjectType(RelationSynopsisImpl.class.getName());
          RelationSynopsisImpl e_outSyn =
              (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
          p_outSyn.setSyn(e_outSyn);
          
          // Create the plus input Synopsis
          PhySynopsis p_plusSyn = outPhyOp.getPlusSyn();

          assert p_plusSyn != null;
          assert p_plusSyn.getKind() == SynopsisKind.REL_SYN : p_plusSyn.getKind();

          allCtx.setOpt(op);
          allCtx.setObjectType(RelationSynopsisImpl.class.getName());
          RelationSynopsisImpl e_plusSyn =
              (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
          p_plusSyn.setSyn(e_plusSyn);
          
          // Create the minus input Synopsis
          PhySynopsis p_minusSyn = outPhyOp.getMinusSyn();

          assert p_minusSyn != null;
          assert p_minusSyn.getKind() == SynopsisKind.REL_SYN : p_minusSyn.getKind();

          allCtx.setOpt(op);
          allCtx.setObjectType(RelationSynopsisImpl.class.getName());
          RelationSynopsisImpl e_minusSyn =
              (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
          p_minusSyn.setSyn(e_minusSyn);
          
          // Create indexes and scans over output synopsis
          //FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
          HashIndex idx = new HashIndex(ec);
          initIndex(ec, outPhyOp, evalContext, 
                    idx, ctx.getTupleStorage().getFactory(), primaryKeyAttrPos);
                    
          HashIndex inpIdx = new HashIndex(ec);
          initIndex(ec, outPhyOp, evalContext,
                    inpIdx, ctx.getTupleStorage().getFactory(), null);
          
          int scanId     = e_outSyn.setIndexScan(null, idx);
          int fullScanId = e_outSyn.setIndexScan(null, inpIdx);
          
          // Create index and scans over plus synopsis
          HashIndex plusIdx = new HashIndex(ec);
          initIndex(ec, outPhyOp, evalContext,
                    plusIdx, ctx.getTupleStorage().getFactory(), null);
          
          int plusFullScanId = e_plusSyn.setFullScan();
          int plusScanId     = e_plusSyn.setIndexScan(null, plusIdx);
          
          // Create index and scans over minus synopsis
          HashIndex minusIdx = new HashIndex(ec);
          initIndex(ec, outPhyOp, evalContext,
                    minusIdx, ctx.getTupleStorage().getFactory(), null);
          int minusScanId    = e_minusSyn.setIndexScan(null, minusIdx);
          int minusFullScanId = e_minusSyn.setFullScan();
          
          
          // Configure Output Operator
          outExecOp.setKeyScanId(scanId);
          outExecOp.setFullScanId(fullScanId);
          outExecOp.setPlusFullScanId(plusFullScanId);
          outExecOp.setPlusScanId(plusScanId);
          outExecOp.setMinusScanId(minusScanId);
          outExecOp.setMinusFullScanId(minusFullScanId);
          
          outExecOp.setOutSyn(e_outSyn);
          outExecOp.setPlusSyn(e_plusSyn);
          outExecOp.setMinusSyn(e_minusSyn);
          outExecOp.setEvalContext(evalContext);
          
          ExecStore outStore = ctx.getTupleStorage();
          assert (outStore != null);
          assert (outStore instanceof RelStore);

          // Configure output Synopsis
          e_outSyn.setStore((RelStore)outStore);
          int outStubId = outStore.addStub();
          e_outSyn.setStubId(outStubId);
          e_outSyn.setEvalContext(evalContext);
          e_outSyn.initialize();
          
          // Configure plus synopsis
          e_plusSyn.setStore((RelStore)outStore);
          int plusStubId = outStore.addStub();
          e_plusSyn.setStubId(plusStubId);
          e_plusSyn.setEvalContext(evalContext);
          e_plusSyn.initialize();
          
         // Configure minus input synopsis
          e_minusSyn.setStore((RelStore)outStore);
          int minusStubId = outStore.addStub();
          e_minusSyn.setStubId(minusStubId);
          e_minusSyn.setEvalContext(evalContext);
          e_minusSyn.initialize();
          
        }
       
        // check whether the propagateHeartbeat flag is enabled
        boolean propagateHeartbeat = outPhyOp.getPropagateHeartbeat();

        // Set the output driver
        dest = ec.getExecMgr().
               getQueryOutput(queryId, outPhyOp.getEpr(), isBatchOutputTuples,
                              propagateHeartbeat);
        
        //set the batching flag for QueryOutput object
        dest.setBatchOutputTuples(isBatchOutputTuples);

        outExecOp.setOutput(dest);
        
        // set the batchOutputFlag
        outExecOp.setBatchOutputTuples(isBatchOutputTuples);

        //set the propagateHeartbeat flag
        outExecOp.setPropagateHeartbeat(propagateHeartbeat);
        
        // set IsStatsEnabled flag
        outExecOp.setIsStatsEnabled(query.getIsStatsEnabled(), 
                                      query.getIsBaseTimelineMillisecond());
    
        // Initialize the execution operator
        outExecOp.initialize();
    }
    
    //Initialize Index
    private void initIndex(ExecContext ec, PhyOptOutput op, 
                           IEvalContext evalCtx,   HashIndex idx,
                           IAllocator<ITuplePtr> factory, ArrayList<Integer> comparisonAttrPos) 
    throws ExecException
    {
      if(comparisonAttrPos == null)
      {
        comparisonAttrPos = new ArrayList<Integer>();
        for(int i = 0; i < op.getNumAttrs(); i++)
          comparisonAttrPos.add(i);
      }
      int numComparisonAttrs = comparisonAttrPos.size();
      
      int attrPos = 0;
      IHEval updateHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
      
      for (int a = 0; a < numComparisonAttrs; a++) 
      {
        attrPos = comparisonAttrPos.get(a);
        HInstr hinstr =
          new HInstr(op.getAttrMetadata()[attrPos].getDatatype(), 
                     IEvalContext.UPDATE_ROLE, new Column(attrPos));
        
        updateHash.addInstr(hinstr);
      }
      updateHash.compile();
      
      IHEval scanHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
      
      for (int a = 0; a < numComparisonAttrs; a++)
      {
        attrPos = comparisonAttrPos.get(a);
        HInstr hinstr = new HInstr(op.getAttrMetadata()[attrPos].getDatatype(),
                                   IEvalContext.UPDATE_ROLE,
                                   new Column(attrPos));
        
        scanHash.addInstr(hinstr);
      }
      scanHash.compile();
      
      IBEval keyEqual = BEvalFactory.create(ec);
      for (int a = 0; a < numComparisonAttrs; a++)
      {
        attrPos = comparisonAttrPos.get(a);
        BInstr binstr = new BInstr();
        
        binstr.op =
          ExprHelper.getEqOp(op.getAttrMetadata()[attrPos].getDatatype());
        
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
    
    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
      assert ctx != null;
      PhyOpt op = ctx.getPhyopt();
      assert op != null;

      ExecContext ec = ctx.getExecContext();
      ExecStore outStore = getInputStore(op, ec, 0);
      assert outStore instanceof RelStore;

      ctx.setTupleStorage(outStore);
      return outStore;
    }

}
