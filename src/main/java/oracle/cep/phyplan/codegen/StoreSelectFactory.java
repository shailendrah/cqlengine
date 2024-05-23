/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreSelectFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/13/12 - use stateinitializationDone flag instead of
                           getOutputSQL
    udeshmuk    07/07/12 - changes after introduction of buffer operator
    udeshmuk    10/21/11 - check if outputs use the syn/store
    udeshmuk    09/02/11 - prevent creation of synopsis in the context of
                           archived reln
    hopark      10/09/08 - remove statics
    anasrini    08/03/06 - support an out store
    najain      03/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreSelectFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSelect;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StoreSelectFactory
 *
 * @author najain
 */
public class StoreSelectFactory extends StoreGenFactory {

  public PhyStore addStoreOpt(Object ctx) {

    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptSelect;
    PhyOptSelect opSel = (PhyOptSelect) op;
    PhyStoreKind storeKind;
    PhyStore store;

    if((opSel.getIsStream() == true) ||
       ((!opSel.isStateInitializationDone()) || opSel.isQueryOperator())) 
    {
      storeKind = PhyStoreKind.PHY_WIN_STORE;
      store = new PhyStore(ec, storeKind);
    }
    else {
      PhyStore relStore = new PhyStore(ec, PhyStoreKind.PHY_REL_STORE);
      store = relStore;
      opSel.getOutSyn().makeStub(store);
    }

    return store;
  }
}
