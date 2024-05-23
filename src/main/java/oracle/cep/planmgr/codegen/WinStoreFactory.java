/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/WinStoreFactory.java /main/8 2008/10/24 15:50:21 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    Factory for creation a window store

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      02/21/08 - stored WinStore
    najain      03/14/07 - cleanup
    najain      12/04/06 - stores are not storage allocators
    najain      06/18/06 - cleanup
    najain      06/16/06 - cleanup
    najain      06/14/06 - query deletion support 
    najain      06/13/06 - bug fix 
    najain      05/04/06 - sharing support 
    najain      04/19/06 - winstore implements relstore 
    najain      03/17/06 - fix problems 
    anasrini    03/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/WinStoreFactory.java /main/8 2008/10/24 15:50:21 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.WinStoreImpl;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.IAllocator;

/**
 * Factory for creation of a window store
 *
 * @since 1.0
 */

class WinStoreFactory extends ExecStoreFactory {

  protected ExecStore newExecStore(StoreGenContext ctx)
    throws CEPException {
    PhyStore        store = ctx.getPhyStore();
    TupleSpec       ts = getTupleSpec(ctx);
    IAllocator<ITuplePtr>  factory;
    ExecStore    winStore;
    
    ExecContext ec = ctx.getExecContext();
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    factory = factoryMgr.get(ts);
    
    StoreFactoryContext objCtx = new StoreFactoryContext(ec, WinStoreImpl.class.getName());
    objCtx.setFactory(factory);
    objCtx.setOpt(store.getOwnOp());
    winStore = (ExecStore)ObjectManager.allocate(objCtx);

    store.setInstStore(winStore);

    return winStore;
  }
}
