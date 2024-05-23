/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/StoreInst.java /main/4 2008/11/13 21:59:39 udeshmuk Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Factory for instantiating an execution representation of a store

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/05/08 - rename pattern store to private store.
    udeshmuk    10/10/08 - add entry for pattern specific store.
    rkomurav    05/15/07 - add bind store
    ayalaman    08/03/06 - partition store implementation
    najain      07/19/06 - ref-count tuples 
    najain      07/05/06 - add shared store 
    najain      03/31/06 - set exec store in phyStore 
    anasrini    03/24/06 - uncomment other store types 
    anasrini    03/12/06 - Creation
    anasrini    03/12/06 - Creation
    anasrini    03/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/StoreInst.java /main/4 2008/11/13 21:59:39 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.util.HashMap;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.exceptions.CEPException;

/**
 * Instantiates an execution store corresponding to a physical store
 * 
 * @author anasrini
 * @since 1.0
 */

public class StoreInst {

  private static final int NUM_STORES = 10;
  private static HashMap<PhyStoreKind, ExecStoreFactory> execMap;

  static {
    populateExecMap();
  }

  private static void populateExecMap() {
    execMap = new HashMap<PhyStoreKind, ExecStoreFactory>(NUM_STORES);

    execMap.put(PhyStoreKind.PHY_WIN_STORE, new WinStoreFactory());
    execMap.put(PhyStoreKind.PHY_REL_STORE, new RelStoreFactory());
    execMap.put(PhyStoreKind.PHY_LIN_STORE, new LinStoreFactory());
    execMap.put(PhyStoreKind.PHY_PARTN_WIN_STORE, new PartnWinStoreFactory());
    execMap.put(PhyStoreKind.PHY_PVT_PARTN_STORE, new PrivatePartnWinStoreFactory());
    execMap.put(PhyStoreKind.PHY_BIND_STORE, new BindStoreFactory());
  }

  public static ExecStore instStore(StoreGenContext ctx) throws CEPException {
    ExecStore        execStore;
    ExecStoreFactory factory;
    PhyStoreKind     kind;

    kind    = ctx.getPhyStore().getStoreKind();
    factory = execMap.get(kind);
    assert factory != null : kind;

    execStore = factory.newExecStore(ctx);
    ctx.getPhyStore().setInstStore(execStore);
    return execStore;
  }
  

}
