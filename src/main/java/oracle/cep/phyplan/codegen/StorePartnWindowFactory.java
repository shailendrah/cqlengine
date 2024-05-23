/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StorePartnWindowFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

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
    ayalaman    08/02/06 - storage for partition window
    ayalaman    08/01/06 - Partition window store factory
    ayalaman    08/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StorePartnWindowFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPrtnWin;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.service.ExecContext;

/**
 * StorePartnWindowFactory
 *
 * @author ayalaman
 */
public class StorePartnWindowFactory extends StoreGenFactory {

  public PhyStore addStoreOpt(Object ctx) 
  {
    PhyStore               store;
    StoreGenFactoryContext sgfctx;
    PhyOpt                 phyOp;

    assert ctx instanceof StoreGenFactoryContext;
    sgfctx = (StoreGenFactoryContext) ctx;
    ExecContext ec = sgfctx.getExecContext();
    phyOp = sgfctx.getPhyPlan();

    assert phyOp instanceof PhyOptPrtnWin;

    store = new PhyStore(ec, PhyStoreKind.PHY_PARTN_WIN_STORE);

    ((PhyOptPrtnWin)phyOp).getSynopsis().makeStub(store); 

    return store;
  }
}

