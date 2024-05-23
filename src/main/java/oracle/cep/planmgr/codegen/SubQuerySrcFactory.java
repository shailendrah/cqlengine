/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SubQuerySrcFactory.java /main/1 2013/11/27 21:53:24 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/20/13 - Creation
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.OrderingKind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.operators.ConcurrentViewStrmSrc;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ViewStrmSrc;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSubquerySrc;
import oracle.cep.phyplan.PhyOptViewStrmSrc;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SubQuerySrcFactory.java /main/1 2013/11/27 21:53:24 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

class SubQuerySrcFactory extends ExecOptFactory
{

    /*
   * (non-Javadoc)
   *
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.planmgr.codegen.CodeGenContext)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op = ctx.getPhyopt();
      
      if (op.getOrderingConstraint() == OrderingKind.UNORDERED)
      {
        LogUtil.fine(LoggerType.TRACE, 
            "Instantiating a ConcurrentViewStrmSrc for " 
            + op.getId() + " with ordering kind as "
            + op.getOrderingConstraint());
        return new ConcurrentViewStrmSrc(ctx.getExecContext());
      }
      else
        return new ViewStrmSrc(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof ViewStrmSrc;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptSubquerySrc;

        ExecContext ec = ctx.getExecContext();
        ViewStrmSrc sourceOp = (ViewStrmSrc) ctx.getExecOpt();
        PhyOptSubquerySrc vsop = (PhyOptSubquerySrc) op;

        //int strId = vsop.getStrId();
        //String optName = ctx.getExecContext().getViewMgr().getView(strId).getName();
        sourceOp.setOptName("Subquery Root Operator");

        // Set the position of the ELEMENT_TIME attr
        sourceOp.setElemTimePos(op.getNumAttrs()-1);

        IEvalContext evalContext = EvalContextFactory.create(ec);
        IAEval       copyEval    = getCopyEval(ec, op);
        sourceOp.setCopyEval(copyEval);
        sourceOp.setEvalContext(evalContext);
    }

    private IAEval getCopyEval(ExecContext ec, PhyOpt op) throws CEPException
    {
      IAEval     eval     = AEvalFactory.create(ec);
      AInstr    instr;
      int       numAttrs = op.getNumAttrs();

      // copy all the columns except the last one - the ELEMENT_TIME col
      for (int attr = 0; attr < numAttrs-1; attr++)
      {
        instr = new AInstr();
        
        instr.op = ExprHelper.getCopyOp(op.getAttrTypes(attr));
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = attr;
        instr.r2 = 0;
        instr.c2 = 0;
        instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
        instr.dc = attr;
        
        eval.addInstr(instr);
      }
      eval.compile();

      return eval;
    }


}