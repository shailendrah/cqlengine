/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreOrderByTopFactory.java /main/1 2009/03/02 23:20:27 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/10/09 - Creation
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOrderByTop;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreOrderByTopFactory.java /main/1 2009/03/02 23:20:27 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class StoreOrderByTopFactory extends StoreGenFactory
{
  public PhyStore addStoreOpt(Object ctx) {
   
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptOrderByTop;
    PhyOptOrderByTop phyOp = (PhyOptOrderByTop)op;
    
    PhyStore store;
    PhyLinStore linStore = new PhyLinStore(ec);
    linStore.setNumLineages(op.getNumInputs());

    store = (PhyStore) linStore;
    phyOp.getOutputSyn().makeStub(store);
    store.setOwnOp(op);    
    return store;
  } 
}
