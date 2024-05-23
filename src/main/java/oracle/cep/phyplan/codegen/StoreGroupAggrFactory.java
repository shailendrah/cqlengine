/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreGroupAggrFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    Store requirements for GROUP/AGGREGATION operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    anasrini    05/30/06 - Creation
    anasrini    05/30/06 - Creation
    anasrini    05/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreGroupAggrFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptGroupAggr;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;


/**
 * Store requirements for GROUP/AGGREGATION operator
 *
 * @since 1.0
 */

class StoreGroupAggrFactory extends StoreGenFactory {

  public PhyStore addStoreOpt(Object ctx) {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptGroupAggr;

    PhyOptGroupAggr groupAggr = (PhyOptGroupAggr)op;
    PhyStoreKind    storeKind;
    PhyStore        store;

    storeKind = PhyStoreKind.PHY_REL_STORE;
    store     = new PhyStore(ec, storeKind);

    groupAggr.getOutSyn().makeStub(store);
    return store;
  }
}
