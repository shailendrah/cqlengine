/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/DStreamFactory.java /main/14 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares DStreamFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah    01/21/11 - remove eval.setContext
 parujain  03/19/09 - stateless server
 parujain  03/19/09 - stateless server
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 najain    04/04/08 - silent reln support
 hopark    12/07/07 - cleanup spill
 najain    04/11/07 - bug fix
 hopark    04/06/07 - mark static pin for const tupleptr
 najain    03/14/07 - cleanup
 hopark    03/06/07 - use ITuplePtr
 najain    12/04/06 - stores are not storage allocators
 najain    11/14/06 - set synStoreFactory
 hopark    11/07/06 - bug 5465978 : refactor newExecOpt
 najain    07/19/06 - ref-count tuples
 najain    07/05/06 - cleanup
 najain    06/29/06 - factory allocation cleanup
 najain    06/18/06 - cleanup
 najain    06/15/06 - query cleanup
 najain    06/13/06 - bug fix
 najain    06/13/06 - bug fix
 anasrini  06/03/06 - do not get stubId from physical synopsis
 najain    05/05/06 - sharing support
 ayalaman  04/28/06 - use relational store for count tuples
 ayalaman  04/26/06 - extend the XStreamFactory
 ayalaman  04/20/06 - Implementation
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/DStreamFactory.java /main/13 2009/03/30 14:46:02 parujain Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Iterator;

import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptDStrm;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.DStream;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.IAllocator;

/**
 * DStreamFactory
 *
 * The DStream Factory manipulates tuples in four roles - Input, Synopsis,
 * Constant, and Output. The Tuple in Synopsis is similar to the Input tuple
 * except that it has one additional Count attribute that stores the count of
 * the tuple. While preparing output, only the data portion of the tuple in the
 * synopsis is copied to the tuple in output role. The tuple in constant role is
 * used to make adjustments to the count attribute of the tuple in synopsis. It
 * has Read-only constants that are used as the arguments to various operators
 * (Increment, decrement etc.,) setup by the DStreamFactory.
 *
 * @author skaluska
 */
public class DStreamFactory extends XStreamFactory
{

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    return new DStream(ctx.getExecContext());
  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx.getExecOpt() instanceof DStream;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptDStrm;

    DStream dStream = (DStream) ctx.getExecOpt();
    PhyOptDStrm phyDStream = (PhyOptDStrm) op;
    ExecContext ec = ctx.getExecContext();
    
    IEvalContext evalContext = EvalContextFactory.create(ec);
    TupleSpec tupSpec;
    ConstTupleSpec constTupSpec;
    int countCol;
    IAEval incrEval, decrEval, initEval, outEval;
    IBEval zeroEval, negEval;
    PhySynopsis nowSyn;
    HashIndex synIndex;
    ITuplePtr constTuple;
    RelationSynopsisImpl delSyn;
    RelStoreImpl relStore;

    // instantiate the relation synopsis
    nowSyn = phyDStream.getSynopsis(); // get the physical synopsis
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    delSyn = (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
    delSyn.setEvalContext(evalContext);

    // Index for looking up in synopsis
    tupSpec = CodeGenHelper.getTupleSpec(ec, op);

    // set the out evaluator for the IStream using the unaltered
    // tuple (tuple with added count column);
    outEval = getOutEval(ec, tupSpec);
    
    dStream.setOutEval(outEval);

    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec oldSpec = new TupleSpec(factoryMgr.getNextId(), tupSpec.getNumAttrs());
    oldSpec.copy(tupSpec);

    // add a count attribute to the original tuple spec
    countCol = tupSpec.addAttr(Datatype.INT);

    relStore = (RelStoreImpl) StoreInst.instStore(new StoreGenContext(ec, nowSyn
        .getStwstore(), tupSpec));

    synIndex = getSynCountIndex(ec, evalContext, oldSpec, relStore.getFactory());

    delSyn.setStore(relStore);
    delSyn.setStubId(relStore.addStub());
    dStream.setSynStoreTupleFactory(relStore.getFactory());

    // set the count scan id to perform hash index based lookup while
    // searching for a specific tuple.
    dStream.setCountScan(delSyn.setIndexScan(null, synIndex));
    // set the full scan id to perform full scan of the synopsis.
    dStream.setFullScan(delSyn.setFullScan());
    delSyn.initialize();
    // set the execution synopsis for the physical synopsis
    nowSyn.setSyn(delSyn);
    // set the synopsis for the DStream operator
    dStream.setSynopsis(delSyn);
    delSyn.initialize();

    // Set the evaluation context for the dStream
    dStream.setEvalContext(evalContext);

    constTupSpec = new ConstTupleSpec(factoryMgr);

    // generate the required eval operators for the dStream
    // count incrementor
    incrEval = getIncrEval(ec, constTupSpec, countCol);
  
    dStream.setIncrEval(incrEval);

    // count decrementor
    decrEval = getDecrEval(ec, constTupSpec, countCol);
   
    dStream.setDecrEval(decrEval);

    // count and synopsis tuple initializer
    initEval = getInitEval(ec, phyDStream, constTupSpec, countCol);
   
    dStream.setInitEval(initEval);

    // 'count is negative' evaluator
    negEval = getNegEval(ec, constTupSpec, countCol);
    
    dStream.setNegEval(negEval);

    // 'count is zero' evaluator
    zeroEval = getZeroEval(ec, constTupSpec, countCol);
  
    dStream.setZeroEval(zeroEval);

    // above evaluators extend the constTupSpec to accommodate
    // some integer values. Using the extended constTupSpec, generate
    // a tuple with all the const values.
    IAllocator ctf = factoryMgr.get(constTupSpec.getTupleSpec());
    constTuple = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
    constTupSpec.populateTuple(ec, constTuple);
    // bind the constant tuple to the evaluation context
    evalContext.bind(constTuple, IEvalContext.CONST_ROLE);

    boolean silentRelns = op.getInputs()[0].isSilentRelnDep();

    dStream.setSilentRelns(silentRelns);

    if (silentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        dStream.addInputRelns((RelSource) opDep.getInstOp());
      }
    }
  }

}
