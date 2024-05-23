/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/LinStoreFactory.java /main/14 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory for creation of a lineage store

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    12/19/10 - remove eval.setEvalContext
    sborah      10/14/09 - support for bigdecimal
    hopark      10/10/08 - remove statics
    hopark      12/07/07 - cleanup spill
    hopark      09/04/07 - eval optimize
    sbishnoi    09/04/07 - add ColRef
    najain      04/11/07 - bug fix
    hopark      04/06/07 - mark static pin for const tupleptr
    najain      03/14/07 - cleanup
    hopark      03/06/07 - use ITuplePtr
    hopark      01/12/07 - uses long for tuple id in lineage
    najain      12/04/06 - stores are not storage allocators
    najain      06/18/06 - cleanup
    najain      06/16/06 - cleanup
    najain      06/16/06 - bug fix 
    najain      06/13/06 - bug fix 
    anasrini    06/03/06 - do not set numStubs in exec store 
    anasrini    04/10/06 - bug fix 
    anasrini    03/22/06 - setupIndex should use linCols 
    anasrini    03/22/06 - set the column numbers for the special attributes 
    anasrini    03/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/LinStoreFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:45 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.util.ArrayList;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStoreImpl;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.BOp;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.service.ExecContext;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;

/**
 * Factory for creation of a lineage store
 *
 * @since 1.0
 */

class LinStoreFactory extends ExecStoreFactory {

  protected ExecStore newExecStore(StoreGenContext ctx)
    throws CEPException {

    LineageStoreImpl  linStore;
    PhyLinStore       store    = (PhyLinStore)ctx.getPhyStore();
    ExecContext ec = ctx.getExecContext();
    IAllocator<ITuplePtr> factory;
    IAllocator      stf;
    TupleSpec         ts;
    ITuplePtr  linTuple;
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex         index    = new HashIndex(ec);
    IEvalContext       evalContext;
    int               numLins  = store.getNumLineages();
    ArrayList<Column> linCols = new ArrayList<Column>(numLins);
    int               linCol;

    ts = getTupleSpec(ctx);
    // A Lineage store uses the following extra attributes per tuple
    // internally - 
    //  a byte attribute for managing multiple readers (tracking inserts)
    //  a byte attribute for managing multiple readers (tracking deletes)
    //    Each reader will use one bit, so the length of each of these byte
    //    arrays will be the least number of bytes to accomadate all the
    //    readers.We start with arbitrary number, and increase on demand
    //  numLins int attributes for lineage 
    //  an object attribute for keeping reference to DoubleList
    // These special attributes should be added only after the attributes of
    // corresponding to the schema of the operator
    Column insCol = new Column(ts.addAttr(
        new AttributeMetadata(Datatype.BYTE, BYTES_INITIAL_STUBS, 0, 0)));
    
    Column refCol = new Column(ts.addAttr(Datatype.OBJECT));

    for (int i=0; i<numLins; i++) {
      linCol = ts.addAttr(Datatype.BIGINT);
      linCols.add(new Column(linCol));
    }
    
    factory = factoryMgr.get(ts);

    StoreFactoryContext objCtx = new StoreFactoryContext(ec, LineageStoreImpl.class.getName());
    objCtx.setFactory(factory);
    objCtx.setOpt(store.getOwnOp());
    linStore = (LineageStoreImpl)ObjectManager.allocate(objCtx);

    // Set the number of items in the lineage 
    // (the number of attributes in the composite lineage key)
    linStore.setNumLins(numLins);
    
    // Set the column number of the attribute in the tuple used for 
    // tracking reader inserts
    linStore.setColIns(insCol);

    // Set the column number of the attribute in the tuple used for
    // keeping reference to DoublyList node
    linStore.setColRef(refCol);
    
    // Set the column numbers of the attributes in the tuple used for
    // storing its lineage
    linStore.setColLineage(linCols);

    // Setup the index and set the index in the lineage store
    evalContext = EvalContextFactory.create(ec);
    setupIndex(ec, evalContext, numLins, index, linCols, factory);
    linStore.setIndex(index);

    // Sepcify the tuple that will hold the key for lookup to be
    // passed onto the underlying index
    stf = factoryMgr.get(ts);
    linTuple = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
    linStore.setTupleBuf(linTuple);

    // Bind this tuple to the evalContext
    evalContext.bind(linTuple, IEvalContext.LIN_ROLE);

    store.setInstStore(linStore);

    return linStore;
  }

  private void setupIndex(ExecContext ec, IEvalContext evalContext, 
                          int numLins, HashIndex index,
			  ArrayList<Column> linCols, IAllocator<ITuplePtr> factory) 
    throws CEPException {

    IHEval  updateHash;
    IHEval  scanHash;
    IBEval  keyEqual;
    HInstr hinstr;
    BInstr binstr;

    // Setup update hash
    updateHash = HEvalFactory.create(ec, numLins);
    for (int  i=0; i<numLins; i++) {
      hinstr = new HInstr(Datatype.BIGINT, IEvalContext.UPDATE_ROLE, 
                          linCols.get(i));
      updateHash.addInstr(hinstr);
    }
    updateHash.compile();

    // Setup scan hash
    scanHash = HEvalFactory.create(ec, numLins);
    for (int  i=0; i<numLins; i++) {
      hinstr = new HInstr(Datatype.BIGINT, IEvalContext.LIN_ROLE, 
                          linCols.get(i));
      scanHash.addInstr(hinstr);
    }
    scanHash.compile();

    // Setup key equal
    keyEqual = BEvalFactory.create(ec, numLins);
    for (int  i=0; i<numLins; i++) {
      binstr = new BInstr();
      binstr.op = BOp.BIGINT_EQ;
      binstr.r1 = IEvalContext.SCAN_ROLE;
      binstr.c1 = linCols.get(i);
      binstr.e1 = null;
      binstr.r2 = IEvalContext.LIN_ROLE;
      binstr.c2 = linCols.get(i);
      binstr.e2 = null;
        
      keyEqual.addInstr(binstr);
    }
    keyEqual.compile();

    index.setEvalContext(evalContext);
    index.setUpdateHashEval(updateHash);
    index.setScanHashEval(scanHash);
    index.setKeyEqual(keyEqual);
    index.setFactory(factory);
    index.initialize();
  }

}
