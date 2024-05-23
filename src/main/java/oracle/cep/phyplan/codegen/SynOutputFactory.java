/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynOutputFactory.java /main/4 2012/04/02 03:50:32 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    02/16/12 - create synopsis when batching is enabled
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 sbishnoi    11/23/07 - adding synopses for output to support update semantics
 najain      03/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynOutputFactory.java /main/4 2012/04/02 03:50:32 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOutput;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * SynOutputFactory
 *
 * @author najain
 */
public class SynOutputFactory extends SynGenFactory {
  
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptOutput;
    PhyOptOutput opu = (PhyOptOutput) op;
    
    // Add Synopsis only if Primary key exists for query or batching enabled
    if(opu.getIsPrimaryKeyExists() || opu.isBatchOutputTuples())
    {
      PhySynopsis outSyn = new PhySynopsis(ec, SynopsisKind.REL_SYN);
      opu.setOutputSyn(outSyn);
      outSyn.setOwnOp(opu);
      
      PhySynopsis plusSyn = new PhySynopsis(ec, SynopsisKind.REL_SYN);
      opu.setPlusSyn(plusSyn);
      plusSyn.setOwnOp(opu);
      
      PhySynopsis minusSyn = new PhySynopsis(ec, SynopsisKind.REL_SYN);
      opu.setMinusSyn(minusSyn);
      minusSyn.setOwnOp(opu);
      
    }
    return;
  }
}
