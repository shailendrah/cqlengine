/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StorePatternStrmFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    rkomurav    03/15/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StorePatternStrmFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPatternStrm;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

class StorePatternStrmFactory extends StoreGenFactory {

  public PhyStore addStoreOpt(Object ctx)
  {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptPatternStrm;
    PhyStoreKind  storeKind;
    PhyStore      store;

    storeKind = PhyStoreKind.PHY_WIN_STORE;
    store = new PhyStore(ec, storeKind);
    return store;
  }
}

