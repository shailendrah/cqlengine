/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynBinJoinFactory.java /main/3 2008/10/24 15:50:14 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

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
    parujain    12/13/07 - External relation
    najain      04/03/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/codegen/SynBinJoinFactory.java /main/3 2008/10/24 15:50:14 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptJoin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * SynBinJoinFactory
 *
 * @author najain
 */
public class SynBinJoinFactory extends SynGenFactory {
  public void addSynOpt(Object ctx) {
    assert ctx instanceof SynGenFactoryContext;
    SynGenFactoryContext lpctx = (SynGenFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();
    PhyOpt op = lpctx.getPhyPlan();
    assert op instanceof PhyOptJoin;
    PhyOptJoin opJoin = (PhyOptJoin) op;
    PhySynopsis inner = null;
    if(op.isExternal())
      inner = new PhySynopsis(ec, SynopsisKind.EXT_SYN);
    else
      inner = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    PhySynopsis outer = new PhySynopsis(ec, SynopsisKind.REL_SYN);
    
    /* A join needs a relation synopsis for its outer and inner inputs */
    opJoin.setInnerSyn(inner);
    inner.setOwnOp(opJoin);
    
    opJoin.setOuterSyn(outer);
    outer.setOwnOp(opJoin);
    
    /**
     * If the join produces a relation (i.e., its output can contain MINUS
     * tuples) then it also needs a join synopsis for its output. The join
     * synopsis is required to produce MINUS tuples.
     */ 
    if (opJoin.getIsStream() == false) {
      PhySynopsis join = new PhySynopsis(ec, SynopsisKind.LIN_SYN);
      opJoin.setJoinSyn(join);
      join.setOwnOp(opJoin);
    }
    else 
      opJoin.setJoinSyn(null);
    
  }
}