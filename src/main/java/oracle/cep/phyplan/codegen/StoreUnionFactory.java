/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreUnionFactory.java /main/3 2008/10/24 15:50:17 hopark Exp $ */

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
    sbishnoi    04/12/07 - countsyn code cleanup
    sbishnoi    04/06/07 - support for union all
    najain      08/14/06 - fix union
    dlenkov     07/05/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreUnionFactory.java /main/3 2008/10/24 15:50:17 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptUnion;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StoreUnionFactory
 *
 * @author dlenkov
 */
public class StoreUnionFactory extends StoreGenFactory {

  public PhyStore addStoreOpt( Object ctx) {

    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;

    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptUnion;
    PhyOptUnion opu = (PhyOptUnion)op;

    PhyStore store;
    PhyStoreKind storeKind;
    
    if(opu.isUnionAll())
    {
      if(op.getIsStream()) 
      {
        storeKind = PhyStoreKind.PHY_WIN_STORE;
        store     = new PhyStore(ec, storeKind);
      }
      else 
      {
        PhyLinStore linStore = new PhyLinStore(ec);

        // Either we query based on the left or the right tuple
        linStore.setNumLineages(1);
        store = linStore;
        opu.getOutSynopsis().makeStub(store);
      }
    }
    else
    {
      storeKind = PhyStoreKind.PHY_REL_STORE;
      store = new PhyStore(ec, storeKind);
      opu.getOutSynopsis().makeStub(store);
      store.setOwnOp(op);  
    }
    
    return store;
  }
}
