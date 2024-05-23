/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynDStreamFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $ */

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
    ayalaman    04/23/06 - synopsis requirements for DStream 
    ayalaman    04/23/06 - synopsis requirements for DStream 
    ayalaman    04/23/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynDStreamFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptDStrm;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * Synopsis requirements for an IStream operator
 *
 * @since 1.0
 */

class SynDStreamFactory extends SynGenFactory {

  public void addSynOpt(Object ctx) {

    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptDStrm;
    PhyOptDStrm dstream = (PhyOptDStrm) op;

    // IStream also uses Relational Synopsis. 
    PhySynopsis inSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
    dstream.setSynopsis(inSyn);
    inSyn.setOwnOp(dstream);
  }
}
