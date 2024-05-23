/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreExchangeFactory.java /main/1 2011/03/24 11:35:08 anasrini Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    03/20/11 - Creation
 */

/**
 *  @version $Header: StoreExchangeFactory.java 20-mar-2011.04:00:29 anasrini Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptExchange;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;


/**
 * StoreExchangeFactory
 */
public class StoreExchangeFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptExchange;
    PhyStore store = new PhyStore(ec, PhyStoreKind.PHY_WIN_STORE);
    return store;
  }
}

