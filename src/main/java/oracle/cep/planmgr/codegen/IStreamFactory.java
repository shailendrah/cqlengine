/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/IStreamFactory.java /main/15 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares IStreamFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah    01/21/11 - remove eval.setContext
 vikshukl  09/08/09 - add support for ISTREAM (R) DIFFERENCE USING (...)
 parujain  03/19/09 - stateless server
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 najain    04/03/08 - silent relns support
 hopark    12/07/07 - cleanup spill
 najain    04/11/07 - bug fix
 hopark    04/06/07 - mark static pin for const tupleptr
 hopark    04/05/07 - memmgr reorg
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
 anasrini  06/03/06 - do not get stubId from physical synopsis
 najain    05/05/06 - sharing support
 ayalaman  04/28/06 - relational store use
 ayalaman  04/26/06 - extend the XStreamFactory
 ayalaman  04/24/06 - set up storage for operators
 ayalaman  04/20/06 - Implementation
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/IStreamFactory.java /main/14 2009/12/24 20:10:21 vikshukl Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;


import java.util.Vector;
import java.util.LinkedList;
import java.util.Iterator;

import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptIStrm;
import oracle.cep.common.Datatype;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.IStream;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.memmgr.IAllocator;

/**
 * IStreamFactory
 *
 * The IStream Factory manipulates tuples in four roles - Input, Synopsis,
 * Constant, and Output. The Tuple in Synopsis has is similar to the Input tuple
 * except that it has one additional Count attribute that stores the count of
 * the tuple. While preparing output, only the data portion of the tuple in the
 * synopsis is copied to the tuple in output role. The tuple in constant role is
 * used to make adjustments to the count attribute of the tuple in synopsis. It
 * has Read-only constants that are used as the arguments to various operators
 * (Increment, decrement etc.,) setup by the IStreamFactory.
 *
 */
public class IStreamFactory extends XStreamFactory
{

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   *      Create a new IStream execution operator
   */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    return new IStream(ctx.getExecContext());
  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx.getExecOpt() instanceof IStream;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptIStrm;

    ExecContext ec = ctx.getExecContext();
    IStream iStream = (IStream) ctx.getExecOpt();
    PhyOptIStrm phyIStream = (PhyOptIStrm) op;

    IEvalContext evalContext = EvalContextFactory.create(ec);
    TupleSpec tupSpec;
    ConstTupleSpec constTupSpec;
    int                  countCol;
    IAEval               incrEval, decrEval, initEval, outEval;
    IBEval               zeroEval, posEval;
    PhySynopsis          nowPhySyn, inPhySyn;
    RelationSynopsisImpl nowExecSyn, inExecSyn;
    HashIndex            synIndex;
    ITuplePtr            constTuple;
    RelStoreImpl         relStore;
    ExecStore            inStore;
    boolean              istream2 = false;  // ISTREAM operator with NOT IN semantics?
    Vector<Integer>      selectIStreamCols = null; // position of SELECT expr(s) on
                                                   // which ISTREAM operator is defined

    if (phyIStream.getUsingExprListMap() != null) { 
      /* we're dealing with ISTREAM with NOT IN semantics. */
      istream2 = true;
      iStream.setIstreamSem(true); // we're not doing regular istream
      Integer[]  usingExprListMap = phyIStream.getUsingExprListMap();
      selectIStreamCols = new Vector<Integer>();  

      for (int i=0; i < usingExprListMap.length; i++) {
        selectIStreamCols.add(usingExprListMap[i]);
      }
    }
       
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());

    /* instantiate the "now" relation synopsis. this is used by regular
     * istream to store all tuples that arrive at the same time t.  renamed it
     * to "now" syn, now that we also have "in" synopsis in case where we do
     * ISTREAM (not in) to capture relation as of (t-1).
     */
    nowPhySyn = phyIStream.getSynopsis();
    nowExecSyn = (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
    nowExecSyn.setEvalContext(evalContext);

    /* set the out evaluator for the IStream using the unaltered tuple (tuple
     * without the added count column)
     */
    tupSpec = CodeGenHelper.getTupleSpec(ec, op);
    outEval = getOutEval(ec, tupSpec);
    
    iStream.setOutEval(outEval);

    /* save TupleSpec before we add ref count column */
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec oldSpec = new TupleSpec(factoryMgr.getNextId(), 
                                      tupSpec.getNumAttrs());
    oldSpec.copy(tupSpec);    

    /* add a ref-count attribute to the original tuple spec */
    countCol = tupSpec.addAttr(Datatype.INT);
    relStore = (RelStoreImpl) 
        StoreInst.instStore(new StoreGenContext(ctx.getExecContext(),
                                          nowPhySyn.getStwstore(), tupSpec));

    /* "now" synopsis will be same for ISTREAM(regular) and ISTREAM(not in) */
    synIndex = super.getSynCountIndex(ec, evalContext, oldSpec, 
                                      relStore.getFactory());
    nowExecSyn.setStore(relStore);
    nowExecSyn.setStubId(relStore.addStub());
    iStream.setSynStoreTupleFactory(relStore.getFactory());
    iStream.setCountScan(nowExecSyn.setIndexScan(null, synIndex)); // index scan
    iStream.setFullScan(nowExecSyn.setFullScan());                 // full scan 
    nowExecSyn.initialize();    
    nowPhySyn.setSyn(nowExecSyn);    // record execution synopsis in physical syn
    iStream.setSynopsis(nowExecSyn);   // "now" synopsis for execution
    nowExecSyn.initialize();

    /* if we're implementing ISTREAM (not in), also instantiate input relation
     * synopsis (to capture relation as of T-1)
     */
    if (istream2) {
      inPhySyn = phyIStream.getInSynopsis();

      allCtx.setOpt(op);
      allCtx.setObjectType(RelationSynopsisImpl.class.getName());
      
      inExecSyn = (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
      /* use underlying store instead of allocating a new one */
      inStore = inPhySyn.getStwstore().getInstStore(); 
      inExecSyn.setStore((RelStore)inStore);
      inExecSyn.setStubId(inStore.addStub());

      // setup an index on a subset of expressions for faster lookup.
      synIndex = this.getInputSynIndex(ec, evalContext, oldSpec, 
                                       selectIStreamCols,
                                       inStore.getFactory());

      inExecSyn.setEvalContext(evalContext); 
      inPhySyn.setSyn(inExecSyn);

      /* now set up relavant synopsis bits in execution operator */
      iStream.setInSynopsis(inExecSyn);
      iStream.setInScanId(inExecSyn.setIndexScan(null, synIndex));
      iStream.setFullInScanId(inExecSyn.setFullScan());

      inExecSyn.initialize();

      /* set up nowList (in-memory) too */
      iStream.setNowList(new LinkedList<QueueElement>());
    }

    iStream.setEvalContext(evalContext); // eval context for execution operator

    constTupSpec = new ConstTupleSpec(factoryMgr);

    // generate the required eval operators for the iStream
    // count incrementor
    incrEval = getIncrEval(ec, constTupSpec, countCol);
    iStream.setIncrEval(incrEval);

    // count decrementor
    decrEval = getDecrEval(ec, constTupSpec, countCol);
    iStream.setDecrEval(decrEval);

    // count and synopsis tuple initializer
    initEval = getInitEval(ec, phyIStream, constTupSpec, countCol);
    iStream.setInitEval(initEval);

    // 'count is positive' evaluator
    posEval = getPosEval(ec, constTupSpec, countCol);
    iStream.setPosEval(posEval);

    // 'count is zero' evaluator
    zeroEval = getZeroEval(ec, constTupSpec, countCol);
    iStream.setZeroEval(zeroEval);

    // above evaluators extend the constTupSpec to accommodate
    // some integer values. Using the extended constTupSpec, generate
    // a tuple with all the const values.
    IAllocator ctf = factoryMgr.get(constTupSpec.getTupleSpec());
    constTuple = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
    constTupSpec.populateTuple(ec, constTuple);

    // bind the constant tuple to the evaluation context
    evalContext.bind(constTuple, IEvalContext.CONST_ROLE);

    boolean silentRelns = op.getInputs()[0].isSilentRelnDep();

    iStream.setSilentRelns(silentRelns);

    if (silentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        iStream.addInputRelns((RelSource) opDep.getInstOp());
      }
    }
  }

  /**
   * Setup and return the index for searching on the synopsis
   * @param    ec          execution context
   * @param    evalCtx     evaluation context for the operator 
   * @param    tupSpec     tuple specification 
   * @param    posMap      SELECT positions on which ISTREAM is defined.
   * 
   * @return  an instance of the HashIndex set up to search the synopsis
   *
   * @throws  CEPException when not able create instructions for the index
   */
  private HashIndex getInputSynIndex(ExecContext ec, IEvalContext evalCtx, 
                                     TupleSpec tupSpec, 
                                     Vector<Integer> posMap,
                                     IAllocator<ITuplePtr> factory)
      throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex  synIndex = new HashIndex(ec); // hash index on synopsis
    HInstr     hInstr; 
    IHEval      updHash, scanHash; 
    IBEval      keyEqual; 
    BInstr     bInstr; 
    int        numAttrs = tupSpec.getNumAttrs();

    assert posMap != null;

    updHash = HEvalFactory.create(ec, numAttrs); 

    for (int i = 0; i < numAttrs; i++)
    {
      if (posMap.contains(i)) {
        hInstr = new HInstr(tupSpec.getAttrType(i),
                            IEvalContext.UPDATE_ROLE,
                            new Column(i));

        updHash.addInstr(hInstr); 
      }
    }
    updHash.compile();

    scanHash = HEvalFactory.create(ec, numAttrs); 
    for (int i = 0; i < numAttrs; i++)
    {
        if (posMap.contains(i)) {
            hInstr = new HInstr(tupSpec.getAttrType(i),
                                IEvalContext.INPUT_ROLE,
                                new Column(i)); 
     
            scanHash.addInstr(hInstr); 
        }
    }
    scanHash.compile();

    keyEqual = BEvalFactory.create(ec); 
    for (int i = 0; i < numAttrs; i++)
    {
      if (posMap.contains(i)) {
        bInstr = new BInstr(); 
        // compare the attributes in the INPUT tuple with the tuple 
        // in the scan role 
        bInstr.op = ExprHelper.getEqOp(tupSpec.getAttrType(i));
     
        bInstr.r1 = IEvalContext.INPUT_ROLE; 
        bInstr.c1 = new Column(i); 
        
        bInstr.r2 = IEvalContext.SCAN_ROLE; 
        bInstr.c2 = new Column(i); 
        
        keyEqual.addInstr(bInstr); 
      }
    }
    keyEqual.compile();

    synIndex.setUpdateHashEval(updHash); 
    synIndex.setScanHashEval(scanHash); 
    synIndex.setKeyEqual(keyEqual); 
    synIndex.setEvalContext(evalCtx);
    synIndex.setFactory(factory);
    synIndex.initialize(); 
    
    return synIndex; 
  }

}
