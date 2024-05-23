/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynBufferFactory.java /main/1 2012/07/16 08:14:06 udeshmuk Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/07/12 - add factory for buffer operator
    udeshmuk    07/07/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynBufferFactory.java /main/1 2012/07/16 08:14:06 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptBuffer;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;

public class SynBufferFactory extends SynGenFactory
{

  @Override
  public void addSynOpt(Object ctx)
  {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptBuffer;
    
    assert op.getOutputSQL() != null : 
      "outputSQL for buffer operator cannot be null!";
    
    PhyOptBuffer bufferOp = (PhyOptBuffer) op;
    PhySynopsis outSyn = null;
    if(bufferOp.isProjectInput())
      outSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.LIN_SYN);
    else
      outSyn = new PhySynopsis(lpctx.getExecContext(), SynopsisKind.REL_SYN);
    
    bufferOp.setOutSyn(outSyn);
    outSyn.setOwnOp(bufferOp);
  }
  
}