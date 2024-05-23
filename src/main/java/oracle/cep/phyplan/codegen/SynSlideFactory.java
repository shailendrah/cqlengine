/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynSlideFactory.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/29/12 - Creation
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSlide;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynSlideFactory.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class SynSlideFactory extends SynGenFactory
{

  @Override
  public void addSynOpt(Object ctx)
  {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptSlide;
    PhyOptSlide opSlide = (PhyOptSlide) op;
    
    // Construct Output Synopsis for Slide operator
    PhySynopsis relSyn = new PhySynopsis(lpctx.getExecContext(), 
                                         SynopsisKind.REL_SYN);
    
    // Set Output Synopsis inside the physical operator
    opSlide.setOutputSyn(relSyn);
    
    // Set Synopsis's owner operator
    relSyn.setOwnOp(opSlide);
  }
  
}
