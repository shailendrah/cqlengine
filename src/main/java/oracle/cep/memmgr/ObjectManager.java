/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/ObjectManager.java /main/8 2008/11/13 22:18:50 anasrini Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    11/10/08 - support for DirectInteropQueue
    udeshmuk    11/05/08 - rename pattern store to private store.
    udeshmuk    10/10/08 - entry for pattern specific partition.
    hopark      02/22/08 - support pagedWinStore
    parujain    11/16/07 - external synopsis
    rkomurav    05/15/07 - add Binding store and syn
    ayalaman    07/30/06 - add partition window synopsis
    parujain    07/21/06 - Generic LinkedList 
    najain      07/19/06 - ref-count tuples 
    najain      06/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/ObjectManager.java /main/8 2008/11/13 22:18:50 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import java.util.HashMap;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.SharedQueueReader;
import oracle.cep.execution.queues.SharedQueueWriter;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.execution.stores.BindStoreImpl;
import oracle.cep.execution.stores.LineageStoreImpl;
import oracle.cep.execution.stores.PrivatePartnWindowStoreImpl;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.stores.WinStoreImpl;
import oracle.cep.execution.stores.PartnWindowStoreImpl;
import oracle.cep.execution.synopses.BindingSynopsisImpl;
import oracle.cep.execution.synopses.ExternalSynopsisImpl;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.PrivatePartnWindowSynopsisImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.execution.synopses.WindowSynopsisImpl;
import oracle.cep.execution.synopses.PartnWindowSynopsisImpl;
import oracle.cep.memmgr.factory.BindStoreImplFactory;
import oracle.cep.memmgr.factory.BindSynopsisImplFactory;
import oracle.cep.memmgr.factory.ExtSynopsisImplFactory;
import oracle.cep.memmgr.factory.LinStoreImplFactory;
import oracle.cep.memmgr.factory.LinSynopsisImplFactory;
import oracle.cep.memmgr.factory.PartnWinStoreImplFactory;
import oracle.cep.memmgr.factory.PrivatePartnWinStoreImplFactory;
import oracle.cep.memmgr.factory.PartnWinSynImplFactory;
import oracle.cep.memmgr.factory.PrivatePartnWinSynImplFactory;
import oracle.cep.memmgr.factory.RelStoreImplFactory;
import oracle.cep.memmgr.factory.RelSynopsisImplFactory;
import oracle.cep.memmgr.factory.SharedQueueReaderFactory;
import oracle.cep.memmgr.factory.SharedQueueWriterFactory;
import oracle.cep.memmgr.factory.DirectInteropQueueFactory;
import oracle.cep.memmgr.factory.WinStoreImplFactory;
import oracle.cep.memmgr.factory.WinSynopsisImplFactory;
import oracle.cep.phyplan.PhyOpt;

/**
 * Manages allocation of all kinds of objects
 * 
 * @author najain
 * @since 1.0
 */

public class ObjectManager {

  private static HashMap<String, ObjectFactory> map;

  static {
    populateMap();
  }

  private static void populateMap() {
    map = new HashMap<String, ObjectFactory>();

    map.put(DirectInteropQueue.class.getName(),
            new DirectInteropQueueFactory());
    map.put(SharedQueueWriter.class.getName(), new SharedQueueWriterFactory());
    map.put(oracle.cep.execution.queues.stored.SharedQueueWriter.class.getName(), new SharedQueueWriterFactory());
    map.put(SharedQueueReader.class.getName(), new SharedQueueReaderFactory());

    map.put(WinStoreImpl.class.getName(), new WinStoreImplFactory());
    map.put(oracle.cep.execution.stores.stored.WinStoreImpl.class.getName(), new WinStoreImplFactory());
    map.put(RelStoreImpl.class.getName(), new RelStoreImplFactory());
    map.put(LineageStoreImpl.class.getName(), new LinStoreImplFactory());
    map.put(PartnWindowStoreImpl.class.getName(), new PartnWinStoreImplFactory());
    map.put(PrivatePartnWindowStoreImpl.class.getName(), new PrivatePartnWinStoreImplFactory());
    map.put(BindStoreImpl.class.getName(), new BindStoreImplFactory());

    map.put(WindowSynopsisImpl.class.getName(), new WinSynopsisImplFactory());
    map.put(RelationSynopsisImpl.class.getName(), new RelSynopsisImplFactory());
    map.put(LineageSynopsisImpl.class.getName(), new LinSynopsisImplFactory());
    map.put(PartnWindowSynopsisImpl.class.getName(), new PartnWinSynImplFactory());
    map.put(PrivatePartnWindowSynopsisImpl.class.getName(), new PrivatePartnWinSynImplFactory());
    map.put(BindingSynopsisImpl.class.getName(), new BindSynopsisImplFactory());
    map.put(ExternalSynopsisImpl.class.getName(), new ExtSynopsisImplFactory());
    
  }

  public static Object allocate(ObjectFactoryContext ctx) throws CEPException {
    ObjectFactory    f = map.get(ctx.getObjectType());
    assert f != null;
    
    // The context should contain the phyopt which is allocating the memory.
    // Here we can add it to a list of allocated objects for the phyopt
    PhyOpt opt = ctx.getOpt();

    // Also, we need to make that no-one can accidently allocate any of these
    // objects by mistake. The only way I can think is to make them in the
    // same directory as the object itself (SimpleStoreFactory is in the same
    // directory as SimpleStore) -- this was as long as we manually control
    // that directory, no one else can allocate them
    
    Object obj = f.allocate(ctx);
    opt.addAllObj(obj);
    return obj;
  }

  public static void   free(ObjectFactoryContext ctx) throws CEPException {
    ObjectFactory    f = map.get(ctx.getObjectType());
    assert f != null;
    
    f.free(ctx);
  }

  public static boolean  isPrimary(ObjectFactoryContext ctx) throws CEPException {
    ObjectFactory    f = map.get(ctx.getObjectType());
    assert f != null;
    
    return f.isPrimary();
  }
 
}
