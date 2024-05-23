/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynGroupAggrFactory.java /main/4 2009/02/01 23:27:27 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Synopsis requirements for a GROUP/AGGREGATION operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/29/09 - fix for bug 8208755
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    sbishnoi    09/25/07 - support for dirtySyn
    rkomurav    09/28/06 - expression support for aggregations
    anasrini    07/12/06 - support for user defined aggregations 
    anasrini    05/30/06 - Creation
    anasrini    05/30/06 - Creation
    anasrini    05/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynGroupAggrFactory.java /main/4 2009/02/01 23:27:27 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptGroupAggr;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;
import oracle.cep.common.BaseAggrFn;

/**
 * Synopsis requirements for a GROUP/AGGREGATION operator
 *
 * @since 1.0
 */

class SynGroupAggrFactory extends SynGenFactory {

  public void addSynOpt(Object ctx) {

    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptGroupAggr;
    PhyOptGroupAggr phyGroupAggr = (PhyOptGroupAggr) op;

    // Output Synopsis
    PhySynopsis outSyn = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    phyGroupAggr.setOutSyn(outSyn);
    outSyn.setOwnOp(phyGroupAggr);

    // Input synopsis 
    if (isInpSynRequired(phyGroupAggr)) {
      PhySynopsis inSyn = new PhySynopsis(ec, SynopsisKind.REL_SYN);
      phyGroupAggr.setInSyn(inSyn);
      inSyn.setOwnOp(phyGroupAggr);
    }
  
    // Dirty Synopsis
    PhySynopsis dirtySyn = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    phyGroupAggr.setDirtySyn(dirtySyn);
    dirtySyn.setOwnOp(phyGroupAggr);
    
  }

  private boolean isInpSynRequired(PhyOptGroupAggr phyGroupAggr) 
  {

    // This is needed only if input is a relation and non-incremental
    // aggregation functions MAX or MIN are present
    // In case of unbounded streams where the behavior of 
    // tuples is only incremental , no input synopsis is required 
    // for both incremental and non-incremental aggregation functions.

    if (phyGroupAggr.getInputs()[0].getIsStream())
      return false;

    int          numAggrAttrs = phyGroupAggr.getNumAggrParamExprs();
    BaseAggrFn[] fns          = phyGroupAggr.getAggrFunctions();

    for (int i = 0; i< numAggrAttrs; i++) 
    {
      if (!(fns[i].supportsIncremental()))
        return true;
    }

    return false;
  }
}
