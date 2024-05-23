/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreValueWindowFactory.java /main/2 2011/03/15 08:30:48 sbishnoi Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/01/11 - initialize lineage store if window over relation
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/phyplan/codegen/StoreValueWindowFactory.java /main/1 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptValueWin;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;

/**
 * StoreRangeWindowFactory
 *
 * @author parujain
 */
public class StoreValueWindowFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {    
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptValueWin;
    PhyOptValueWin phyOp = (PhyOptValueWin)op;
    
    assert phyOp.isWindowOverRelation();
    
    PhyStore store;
    PhyLinStore linStore = new PhyLinStore(ec);
    linStore.setNumLineages(op.getNumInputs());

    store = (PhyStore) linStore;
    phyOp.getOutputSyn().makeStub(store);
    store.setOwnOp(op);    
    return store;
  }
}
