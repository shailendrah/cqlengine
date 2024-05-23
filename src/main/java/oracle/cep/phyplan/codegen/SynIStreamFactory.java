/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynIStreamFactory.java /main/3 2009/12/24 20:10:22 vikshukl Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    ayalaman    04/23/06 - synopsis requirements for IStream 
    ayalaman    04/23/06 - synopsis requirements for IStream 
    ayalaman    04/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynIStreamFactory.java /main/3 2009/12/24 20:10:22 vikshukl Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptIStrm;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 * Synopsis requirements for an IStream operator
 *
 * @since 1.0
 */

class SynIStreamFactory extends SynGenFactory {

  public void addSynOpt(Object ctx) {

    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptIStrm;
    PhyOptIStrm istream = (PhyOptIStrm) op;

    /* ISTREAM uses relational synopsis for the case where multiple tuples
     * arrive at time t
     */
    PhySynopsis nowSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
    istream.setSynopsis(nowSyn);
    nowSyn.setOwnOp(istream);

    /* For ISTREAM(not in), we need a relational synopsis to capture r(t-1) */
    if (istream.getUsingExprListMap() != null) 
    {
      PhySynopsis inSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
      istream.setInSynopsis(inSyn);
      inSyn.setOwnOp(istream);
    }
  }
}
