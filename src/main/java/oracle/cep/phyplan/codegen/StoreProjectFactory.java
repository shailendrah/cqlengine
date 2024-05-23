/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreProjectFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $ */

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
 najain      07/19/06 - ref-count tuples 
 najain      07/05/06 - cleaunup
 najain      04/06/06 - cleanup
 skaluska    04/05/06 - compile
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreProjectFactory.java /main/6 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptProject;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StoreProjectFactory
 * 
 * @author najain
 */
public class StoreProjectFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptProject;
    PhyOptProject opProj = (PhyOptProject) op;
    PhyStoreKind storeKind;
    PhyStore store;
    
    if((opProj.getIsStream() == true) || 
       ((!opProj.isStateInitializationDone()) || opProj.isQueryOperator())) 
    {
      storeKind = PhyStoreKind.PHY_WIN_STORE;
      store = new PhyStore(ec, storeKind);
    }
    else {
      PhyLinStore linStore = new PhyLinStore(ec);
      linStore.setNumLineages(op.getNumInputs());

      store = (PhyStore) linStore;
      opProj.getOutSyn().makeStub(store);
    }

    return store;
  }
}
