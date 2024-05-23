/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynPatternStrmClassBFactory.java /main/5 2008/11/13 21:59:39 udeshmuk Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/05/08 - rename pattern store to private store
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    udeshmuk    10/10/08 - use pattern specific synopsis type.
    rkomurav    09/12/07 - add prtnwinstore
    rkomurav    05/14/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynPatternStrmClassBFactory.java /main/5 2008/11/13 21:59:39 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPatternStrmClassB;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

public class SynPatternStrmClassBFactory extends SynGenFactory
{
  public void addSynOpt(Object ctx)
  {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptPatternStrmClassB;
    PhyOptPatternStrmClassB opPattern = (PhyOptPatternStrmClassB) op;
    PhySynopsis syn = new PhySynopsis(ec, SynopsisKind.BIND_SYN);
    
    syn.setOwnOp(opPattern);
    opPattern.setBindSyn(syn);
    
    if((opPattern.getMaxPrevIndex() > 0) && (opPattern.hasPartnAttrs()))
    {
      PhySynopsis pwSyn;
      pwSyn = new PhySynopsis(ec, SynopsisKind.PRIVATE_PARTN_WIN_SYN);
      pwSyn.setOwnOp(opPattern);
      opPattern.setPartnSyn(pwSyn);
    }
  }
}

