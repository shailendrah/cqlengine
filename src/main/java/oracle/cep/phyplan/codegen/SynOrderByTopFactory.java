/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynOrderByTopFactory.java /main/1 2009/03/02 23:20:27 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/10/09 - Creation
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOrderByTop;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynOrderByTopFactory.java /main/1 2009/03/02 23:20:27 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SynOrderByTopFactory extends SynGenFactory
{
  public void addSynOpt(Object ctx) 
  {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;

    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptOrderByTop;
    PhyOptOrderByTop phyOpt = (PhyOptOrderByTop) op;
    
    PhySynopsis linSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.LIN_SYN);
    phyOpt.setOutputSyn(linSyn);
    linSyn.setOwnOp(phyOpt);
  } 
}
