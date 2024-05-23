/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynRStreamFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    Synopsis requirements for an RSTREAM operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    anasrini    04/10/06 - Creation
    anasrini    04/10/06 - Creation
    anasrini    04/10/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynRStreamFactory.java /main/2 2008/10/24 15:50:13 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRStrm;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * Synopsis requirements for an RSTREAM operator
 *
 * @since 1.0
 */

class SynRStreamFactory extends SynGenFactory {

  public void addSynOpt(Object ctx) {

    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptRStrm;
    PhyOptRStrm rstream = (PhyOptRStrm) op;

    PhySynopsis inSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
    rstream.setSynopsis(inSyn);
    inSyn.setOwnOp(rstream);
  }
}

