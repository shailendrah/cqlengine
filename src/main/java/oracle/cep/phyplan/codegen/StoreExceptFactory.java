/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreExceptFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

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
    dlenkov     07/05/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreExceptFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptExcept;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StoreExceptFactory
 *
 * @author dlenkov
 */
public class StoreExceptFactory extends StoreGenFactory {

  public PhyStore addStoreOpt( Object ctx) {

    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;

    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptExcept;
    PhyOptExcept ope = (PhyOptExcept)op;

    PhyStore store = new PhyStore( ec, PhyStoreKind.PHY_REL_STORE);    
    ope.getOutSynopsis().makeStub( store);
    
    return store;
  }
}
