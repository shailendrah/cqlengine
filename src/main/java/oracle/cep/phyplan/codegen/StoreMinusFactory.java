/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreMinusFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    sbishnoi    09/26/07 - Creation
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyLinStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptMinus;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;



/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/StoreMinusFactory.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class StoreMinusFactory extends StoreGenFactory {

  public PhyStore addStoreOpt( Object ctx) {

    assert ctx instanceof StoreGenFactoryContext;
    StoreGenFactoryContext lpctx = (StoreGenFactoryContext) ctx;

    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptMinus;
    PhyOptMinus opMinus = (PhyOptMinus)op;

    PhyLinStore linStore = new PhyLinStore(ec);
    linStore.setNumLineages(1);
    opMinus.getOutSynopsis().makeStub( linStore);
    
    return linStore;
  }
}
