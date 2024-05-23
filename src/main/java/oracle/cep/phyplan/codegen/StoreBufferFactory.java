/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreBufferFactory.java /main/1 2012/07/16 08:14:06 udeshmuk Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/07/12 - add factory for buffer operator
    udeshmuk    07/07/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreBufferFactory.java /main/1 2012/07/16 08:14:06 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptBuffer;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

public class StoreBufferFactory extends StoreGenFactory
{

  @Override
  public PhyStore addStoreOpt(Object ctx)
  {
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptBuffer;
    
    PhyOptBuffer bufferOp = (PhyOptBuffer) op;
    PhyStore store;
    
    assert bufferOp.getOutputSQL() != null : 
      "outputSQL for buffer operator cannot be null!";
    
    if(bufferOp.isProjectInput())
    {
      //create lineage store
      PhyLinStore linStore = new PhyLinStore(ec);
      linStore.setNumLineages(op.getNumInputs());

      store = (PhyStore) linStore;
      bufferOp.getOutSyn().makeStub(store);
    }
    else
    {
      //create rel store
      store = new PhyStore(ec, PhyStoreKind.PHY_REL_STORE);
      bufferOp.getOutSyn().makeStub(store);
    }
    
    return store;
  }
  
}