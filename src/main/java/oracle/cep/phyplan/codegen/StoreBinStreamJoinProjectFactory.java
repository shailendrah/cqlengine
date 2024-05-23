/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreBinStreamJoinProjectFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    najain      07/19/06 - ref-count tuples 
    najain      07/05/06 - cleaunup
    najain      05/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreBinStreamJoinProjectFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptStrJoinProject;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StoreBinStreamJoinProjectFactory
 *
 * @author najain
 */
public class StoreBinStreamJoinProjectFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptStrJoinProject;
    PhyStore store;
    PhyStoreKind storeKind;
    
    if (op.getIsStream()) {
      storeKind = PhyStoreKind.PHY_WIN_STORE;     
      store = new PhyStore(ec, storeKind);
    }
    else {
      PhyLinStore linStore = new PhyLinStore(ec);
      linStore.setNumLineages(op.getNumInputs());
      store = linStore;
    }
    
    return store;
  }
}

