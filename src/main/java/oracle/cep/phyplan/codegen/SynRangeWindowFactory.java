/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynRangeWindowFactory.java /main/3 2011/05/09 23:12:07 sbishnoi Exp $ */

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
 hopark      10/07/08 - use execContext to remove statics
 najain      04/06/06 - cleanup
 skaluska    04/05/06 - pass planmanager in physynopsis
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynRangeWindowFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRngWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * SynRangeWindowFactory
 *
 * @author najain
 */
public class SynRangeWindowFactory extends SynGenFactory {
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptRngWin;
    PhyOptRngWin opRngWin = (PhyOptRngWin) op;
    
    if(opRngWin.isVariableDurationWindow())
    {
      PhySynopsis linSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.LIN_SYN);
      opRngWin.setOutputSyn(linSyn);
      linSyn.setOwnOp(opRngWin);
    }
    else
    {
      PhySynopsis winSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.WIN_SYN);
      opRngWin.setWinSyn(winSyn);
      winSyn.setOwnOp(opRngWin);
    }
  }
}