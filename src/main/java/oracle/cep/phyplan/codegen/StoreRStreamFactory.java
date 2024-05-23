/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreRStreamFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    Store requirements for RSTREAM operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    najain      07/13/06 - always use win store 
    najain      07/05/06 - cleaunup
    anasrini    04/10/06 - Creation
    anasrini    04/10/06 - Creation
    anasrini    04/10/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreRStreamFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRStrm;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;


/**
 * Store requirements for RSTREAM operator
 *
 * @since 1.0
 */

class StoreRStreamFactory extends StoreGenFactory {

  public PhyStore addStoreOpt(Object ctx) {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptRStrm;
    PhyStoreKind  storeKind;
    PhyStore      store;

    storeKind = PhyStoreKind.PHY_WIN_STORE;
    store = new PhyStore(ec, storeKind);
    return store;
  }
}
