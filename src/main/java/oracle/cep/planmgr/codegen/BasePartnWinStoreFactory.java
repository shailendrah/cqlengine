/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BasePartnWinStoreFactory.java /main/4 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/21/11 - remove eval.setContext
    sborah      10/14/09 - support for bigdecimal
    parujain    03/20/09 - stateless server
    udeshmuk    11/07/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BasePartnWinStoreFactory.java /main/3 2009/11/09 10:10:59 sborah Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.BasePartnWindowStoreImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public abstract class BasePartnWinStoreFactory extends ExecStoreFactory
{ 
  abstract String getStoreClassName();
  
  @Override
  ExecStore newExecStore(StoreGenContext ctx) throws CEPException
  {
    PhyStore              phyStore;
    BasePartnWindowStoreImpl pwStore;
    TupleSpec             partTuple; //Tuple for partition key
    int                   numPartAttrs;
    Attr[]                partnAttrs;
    Attr                  phyAttr;
    int                   windowListPos;
    IEvalContext          evalContext;
    IAEval                copyEval;
    IAllocator<ITuplePtr> dataTsFac;
    IAllocator<ITuplePtr> hdrTsFac;
    TupleSpec             inpTuple;
    TupleSpec             dataTuple;
    
    ExecContext ec = ctx.getExecContext();
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();

    numPartAttrs = ctx.getNumPartAttrs();
    partnAttrs   = ctx.getPartnAttrs();
    phyStore     = ctx.getPhyStore();
    inpTuple     = ctx.getInpTuple();
    dataTuple    = ctx.getDataTuple();
    
    assert numPartAttrs > 0;
    assert partnAttrs.length > 0;
    
    String storeClassName = getStoreClassName();

    // build the tuple layout for the partition key
    partTuple = new TupleSpec(factoryMgr.getNextId());
    // partition tuple has one attribute for each partitioning attr
    for (int pa = 0; pa < numPartAttrs; pa++)
    {
        phyAttr = partnAttrs[pa];

        // assert that there is a valid attribute at the given attr position
        assert inpTuple.getNumAttrs() > phyAttr.getPos();

        partTuple.addAttr(inpTuple.getAttrMetadata((phyAttr.getPos())));
    }

    // in addition to the partition key, the partition tuple store a pointer
    // to the oldest row in the partition, recent row in the partition and the
    // the exact count of rows in the partition
    windowListPos = partTuple.addAttr(Datatype.OBJECT);
    
    // generate data tuple layout: a datatuple consists of the actual data
    // attributes
/*
    dataTuple = new TupleSpec();
    for (int aid = 0; aid < inpTuple.getNumAttrs(); aid++)
    {
        dataTuple.addAttr(inpTuple.getAttrType(aid), inpTuple.getAttrLen(aid));
    }
*/
    
    // data tuple will have an extra -
    // OBJECT attribute to keep reference to doublyList node
    Column refCol = new Column(dataTuple.addAttr(Datatype.OBJECT));
    
    // shared evaluation context between the store, its index, and its various
    // evaluators.
    evalContext = EvalContextFactory.create(ec);
    
    // copy evaluator to copy partition attributes from a data row into the
    // partition key or header
    copyEval = AEvalFactory.create(ec);
    
    for (int pa = 0; pa < numPartAttrs; pa++)
    {
        AInstr instr = new AInstr();

        instr.op = ExprHelper.getCopyOp(partTuple.getAttrType(pa));
        instr.r1 = IEvalContext.INPUT_ROLE; // role for the input data tuple
        phyAttr  = partnAttrs[pa];

        // assert that there is a valid attribute at the given attr position
        assert inpTuple.getNumAttrs() > phyAttr.getPos();
        instr.c1 = phyAttr.getPos();
        instr.r2 = 0;
        instr.c2 = 0;
        instr.dr = IEvalContext.SYN_ROLE; // header tuple in synopsis
        instr.dc = pa;

        copyEval.addInstr(instr);
    }
    
    copyEval.compile();
    
    dataTsFac = factoryMgr.get(dataTuple);
    hdrTsFac  = factoryMgr.get(partTuple);

    StoreFactoryContext objCtx = new StoreFactoryContext(ec, storeClassName);
    objCtx.setFactory(dataTsFac);
    objCtx.setSecFactory(hdrTsFac); // set the header tuple factory in objctx
    objCtx.setOpt(phyStore.getOwnOp());

    pwStore = (BasePartnWindowStoreImpl)ObjectManager.allocate(objCtx);
    phyStore.setInstStore(pwStore);
    
    pwStore.setHeaderColPositions(windowListPos);
    // Set the column number of the attribute in the tuple used for
    // keeping reference to DoublyList node
    pwStore.setColRef(refCol);
    
    pwStore.setEvalContext(evalContext);
    pwStore.setHdrCopyEval(copyEval);

    pwStore.setHeaderIndex(getPartnKeyIndex(ec, inpTuple, 
        evalContext, partTuple, numPartAttrs, pwStore.getHdrTupFactory(), partnAttrs));
    
    ctx.setPartnTupleFac(dataTsFac);
    
    return pwStore;
  }
  
  /**
   * Setup and return the index for searching on the synopsis for a
   * particular partitioning key. The input DATA tuple on which the
   * search is performed is give the INPUT role.
   * @param ec TODO
   * @param    evalCtx     evaluation context for the operator
   * @param    tupSpec     tuple specification
   * @param    numPAttrs   number of attributes for the hash
   * @param    partnAttrs  partition attributes
   *
   * @return  an instance of the HashIndex set up to search the synopsis
   *
   * @throws  CEPException when not able create instructions for the index
   */
  protected HashIndex getPartnKeyIndex(ExecContext ec,
                                     TupleSpec inpTuple, 
                                     IEvalContext evalCtx,
                                     TupleSpec tupSpec, 
                                     int numPAttrs,
                                     IAllocator<ITuplePtr> factory, Attr[] partnAttrs) 
      throws CEPException
  {
    HashIndex synIndex = new HashIndex(ec); // hash index on synopsis
    HInstr hInstr;
    IHEval  updHash, scanHash;
    IBEval  keyEqual;
    BInstr bInstr;
    Attr   phyAttr;

    updHash = HEvalFactory.create(ec, numPAttrs);
   
    for (int attr = 0; attr < numPAttrs; attr++)
    {
      hInstr = new HInstr(tupSpec.getAttrType(attr), 
                          IEvalContext.UPDATE_ROLE, 
                          new Column(attr));

      updHash.addInstr(hInstr);
    }
    
    updHash.compile();

    scanHash = HEvalFactory.create(ec, numPAttrs);
    
    for (int attr = 0; attr < numPAttrs; attr++)
    {
      phyAttr = partnAttrs[attr];
      int pos = phyAttr.getPos();

      hInstr = new HInstr(inpTuple.getAttrType(pos), 
                           IEvalContext.INPUT_ROLE, 
                           new Column(pos));
      scanHash.addInstr(hInstr);
    }
    
    scanHash.compile();

    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numPAttrs; attr++)
    {
      bInstr = new BInstr();
      // compare the attributes in the INPUT tuple with the tuple
      // in the scan role
      phyAttr = partnAttrs[attr];
      int pos = phyAttr.getPos();
      bInstr.op = ExprHelper.getEqOp(tupSpec.getAttrType(attr));
      bInstr.r1 = IEvalContext.INPUT_ROLE;
      bInstr.c1 = new Column(pos);

      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(attr);

      keyEqual.addInstr(bInstr);
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