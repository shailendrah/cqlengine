/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynMinusFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $ */

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
    hopark      10/07/08 - use execContext to remove statics
    sbishnoi    09/26/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynMinusFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptMinus;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

public class SynMinusFactory extends SynGenFactory {

  public void addSynOpt( Object ctx) {

    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptMinus;
    PhyOptMinus opMinus = (PhyOptMinus) op;

    PhySynopsis rel = new PhySynopsis(ec, SynopsisKind.LIN_SYN);
    opMinus.setOutSynopsis( rel);
    rel.setOwnOp( opMinus);

    rel = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    opMinus.setLeftInputSynopsis(rel);
    rel.setOwnOp(opMinus);
    
    rel = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    opMinus.setRightInputSynopsis(rel);
    rel.setOwnOp(opMinus);
    
    
  }
}
