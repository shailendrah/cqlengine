/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynRowWindowFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $ */

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
    dlenkov     05/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynRowWindowFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRowWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * SynRangeWindowFactory
 *
 * @author dlenkov
 */
public class SynRowWindowFactory extends SynGenFactory {

  public void addSynOpt(Object ctx) {

    assert ctx instanceof SynGenFactoryContext;

    SynGenFactoryContext lpctx = (SynGenFactoryContext)ctx;
    PhyOpt op = lpctx.getPhyPlan();

    assert op instanceof PhyOptRowWin;

    PhyOptRowWin rowWin = (PhyOptRowWin) op;
    PhySynopsis winSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.WIN_SYN);

    rowWin.setWinSyn( winSyn);
    winSyn.setOwnOp( rowWin);
  }
}
