/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynViewRelnSrcFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $ */

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
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynViewRelnSrcFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptViewRelnSrc;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * SynViewRelnSrcFactory
 *
 * @author najain
 */
public class SynViewRelnSrcFactory extends SynGenFactory {
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptViewRelnSrc;
    PhyOptViewRelnSrc opReln = (PhyOptViewRelnSrc) op;

    PhySynopsis outSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
    
    opReln.setOutSyn(outSyn);
    outSyn.setOwnOp(opReln);
  }
}

