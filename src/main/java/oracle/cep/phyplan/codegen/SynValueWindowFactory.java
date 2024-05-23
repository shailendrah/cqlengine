/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynValueWindowFactory.java /main/3 2011/03/15 08:30:48 sbishnoi Exp $ */

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
    sbishnoi    03/01/11 - initializing lineage synopsis if relation over
                           window
    hopark      10/07/08 - use execContext to remove statics
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynValueWindowFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptValueWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * SynValueWindowFactory
 *
 * @author parujain
 */
public class SynValueWindowFactory extends SynGenFactory {
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptValueWin;
    PhyOptValueWin opValWin = (PhyOptValueWin) op;
    if(opValWin.isWindowOverRelation())
    {
      PhySynopsis linSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.LIN_SYN);
      opValWin.setOutputSyn(linSyn);
      linSyn.setOwnOp(opValWin);
    }
    else
    {
      PhySynopsis winSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.WIN_SYN);
      opValWin.setWinSyn(winSyn);
      winSyn.setOwnOp(opValWin);
    }
  }
}
