/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RelStoreFactory.java /main/9 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory for creation of a relational store

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/14/09 - support for bigdecimal
    hopark      10/10/08 - remove statics
    sbishnoi    08/27/07 - add ColRef
    najain      03/14/07 - cleanup
    najain      12/04/06 - stores are not storage allocators
    najain      06/18/06 - cleanup
    najain      06/16/06 - cleanup
    najain      06/15/06 - bug fix 
    najain      06/15/06 - query deletion support 
    najain      06/13/06 - bug fix 
    anasrini    06/03/06 - do not set numStubs in exec store 
    anasrini    05/31/06 - bug fix 
    anasrini    03/22/06 - colIns and colDel 
    anasrini    03/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RelStoreFactory.java /main/9 2009/11/09 10:10:59 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.Column;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;

/**
 * Factory for creation of a relational store
 *
 * @since 1.0
 */

class RelStoreFactory extends ExecStoreFactory {

  protected ExecStore newExecStore(StoreGenContext ctx)
    throws CEPException {

    RelStoreImpl relStore;
    PhyStore     store   = ctx.getPhyStore();
    ExecContext ec = ctx.getExecContext();
    IAllocator<ITuplePtr> factory;
    TupleSpec    ts;

    ts = ctx.getTupleSpec();
    if (ts == null)
      ts = getTupleSpec(ctx);

    // A Relational store uses the following extra attributes per tuple
    // internally - 
    //  a byte attribute for managing multiple readers (tracking inserts)
    //  a byte attribute for managing multiple readers (tracking deletes)
    //    Each reader will use one bit, so the length of each of these byte
    //    arrays will be the least number of bytes to accomadate all the
    //    readers
    //  an OBJECT attribute for keeping reference to DoubleList node
    
    Column insCol = new Column(ts.addAttr(
           new AttributeMetadata(Datatype.BYTE, BYTES_INITIAL_STUBS, 0 , 0)));

    Column refCol = new Column(ts.addAttr(Datatype.OBJECT));
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    factory = factoryMgr.get(ts);

    StoreFactoryContext objCtx = new StoreFactoryContext(ec, RelStoreImpl.class.getName());
    objCtx.setFactory(factory);
    objCtx.setOpt(store.getOwnOp());
    relStore = (RelStoreImpl)ObjectManager.allocate(objCtx);

    // Set the column number of the attribute in the tuple used for 
    // tracking reader inserts
    relStore.setColIns(insCol);
    
    // Set the column number of the attribute in the tuple used for
    // keeping reference to DoublyList node
    relStore.setColRef(refCol);

    store.setInstStore(relStore);

    return relStore;
  }
}
