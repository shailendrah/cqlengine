/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreRangeWindowFactory.java /main/2 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    03/16/11 - support for variable duration range window
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: StoreRangeWindowFactory.java 20-mar-2006.21:44:20 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRngWin;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;

/**
 * StoreRangeWindowFactory
 *
 * @author najain
 */
public class StoreRangeWindowFactory extends StoreGenFactory 
{
  public PhyStore addStoreOpt(Object ctx) 
  {
    //Note: This should be called only if the window is variable duration
    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;
    
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptRngWin;
    PhyOptRngWin phyOp = (PhyOptRngWin)op;
    
    assert phyOp.isVariableDurationWindow();
    
    PhyStore store;
    PhyLinStore linStore = new PhyLinStore(ec);
    linStore.setNumLineages(op.getNumInputs());

    store = (PhyStore) linStore;
    phyOp.getOutputSyn().makeStub(store);
    store.setOwnOp(op);    
    return store;
  }
}
