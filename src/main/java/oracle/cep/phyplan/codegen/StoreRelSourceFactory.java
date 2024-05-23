/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreRelSourceFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $ */

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
    udeshmuk    09/02/11 - prevent creation of store in the context of archived
                           reln
    hopark      10/09/08 - remove statics
    najain      05/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreRelSourceFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRelnSrc;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StoreRelSourceFactory
 *
 * @author najain
 */

public class StoreRelSourceFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptRelnSrc;
    PhyOptRelnSrc opReln = (PhyOptRelnSrc)op;

    if((!opReln.isStateInitializationDone()) || opReln.isQueryOperator())
      return new PhyStore(ec,PhyStoreKind.PHY_WIN_STORE);
      
    PhyStore store = new PhyStore(ec, PhyStoreKind.PHY_REL_STORE);

    opReln.getOutSyn().makeStub(store);

    return store;
  }
}

