/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynDistinctFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    sbishnoi    05/11/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynDistinctFactory.java /main/2 2008/10/24 15:50:14 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptDistinct;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

public class SynDistinctFactory extends SynGenFactory {
  
  public void addSynOpt(Object ctx){
  
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;

    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptDistinct;
    PhyOptDistinct opu = (PhyOptDistinct) op;
    
    PhySynopsis rel = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
    opu.setOutputSyn(rel);
    rel.setOwnOp(opu);
  }
  
}


