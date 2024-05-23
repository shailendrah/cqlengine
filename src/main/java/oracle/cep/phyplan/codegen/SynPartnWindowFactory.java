/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynPartnWindowFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    ayalaman    08/01/06 - Partition window implementation
    ayalaman    08/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynPartnWindowFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPrtnWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * SynPartnWindowFactory
 *
 * @author ayalaman
 */
public class SynPartnWindowFactory extends SynGenFactory {
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptPrtnWin;
    PhyOptPrtnWin opPrtnWin = (PhyOptPrtnWin) op;
    PhySynopsis partSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.PARTN_WIN_SYN);

    opPrtnWin.setWinSyn(partSyn);
    partSyn.setOwnOp(opPrtnWin);
  }
}

