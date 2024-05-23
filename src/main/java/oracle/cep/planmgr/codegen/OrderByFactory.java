/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/OrderByFactory.java /main/4 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/19/09 - stateless server
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      03/04/08 - fix release from wrong factory
    parujain    06/28/07 - Orderby factory
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/OrderByFactory.java /main/4 2009/03/30 14:46:02 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.OrderBy;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOrderBy;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.service.ExecContext;

public class OrderByFactory extends ExecOptFactory {

  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException {
    return new OrderBy(ctx.getExecContext());
  }

  void setupExecOpt(CodeGenContext ctx) throws CEPException {
    assert ctx.getExecOpt() instanceof OrderBy;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptOrderBy;
    
    OrderBy execOper = (OrderBy)ctx.getExecOpt();
    ExecContext ec = ctx.getExecContext();
    PhyOptOrderBy phyOrder = (PhyOptOrderBy)op;
    TupleSpec            ts;
    ComparatorSpecs[]    specs = new ComparatorSpecs[phyOrder.getExprs().length];
    Expr[] phyOrderExprs = phyOrder.getExprs();
    
    for(int i=0; i<phyOrder.getExprs().length; i++)
    {
      assert phyOrderExprs[i] instanceof ExprOrderBy;
      ExprOrderBy expr = (ExprOrderBy)phyOrderExprs[i];
      int pos = PositionHelper.getExprPos(expr.getOrderbyExpr());
      specs[i] = new ComparatorSpecs(pos, expr.isNullsFirst(), expr.isAscending());
    }
    
    ts = CodeGenHelper.getTupleSpec(ec, op.getInputs()[0]);
    execOper.initialize(specs, ts);
  }
  
  @Override
  protected ExecStore instStore(CodeGenContext ctx) throws CEPException
  {
      assert ctx != null;
      PhyOpt op = ctx.getPhyopt();
      assert op != null;

      ExecContext ec = ctx.getExecContext();
      ExecStore inStore = getInputStore(op, ec, 0);
      ctx.setTupleStorage(inStore);
      return inStore;
  }
}
