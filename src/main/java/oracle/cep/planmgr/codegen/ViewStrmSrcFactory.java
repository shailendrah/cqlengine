/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ViewStrmSrcFactory.java /main/10 2011/06/17 11:31:28 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    12/20/10 - remove eval.setEvalContext
    sborah      10/14/09 - support for bigdecimal
    sborah      03/19/09 - cleanup
    parujain    03/19/09 - stateless server
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    hopark      09/06/07 - use IAEval factory
    anasrini    09/04/07 - ELEMENT_TIME support
    najain      12/04/06 - stores are not storage allocators
    hopark      11/07/06 - bug 5465978 : refactor newExecOpt
    anasrini    09/22/06 - set name for execution operator
    najain      08/03/06 - view strm share underlying store
    najain      07/19/06 - ref-count tuples
    najain      07/05/06 - cleanup
    najain      06/29/06 - factory allocation cleanup
    najain      06/18/06 - cleanup
    najain      06/16/06 - cleanup
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ViewStrmSrcFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:46 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
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
import oracle.cep.phyplan.PhyOptViewStrmSrc;
import oracle.cep.service.ExecContext;

/**
 * ViewStrmSrcFactory
 *
 * @author najain
 * @since 1.0
 */
class ViewStrmSrcFactory extends ExecOptFactory
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
        assert op instanceof PhyOptViewStrmSrc;

        ExecContext ec = ctx.getExecContext();
        ViewStrmSrc sourceOp = (ViewStrmSrc) ctx.getExecOpt();
        PhyOptViewStrmSrc vsop = (PhyOptViewStrmSrc) op;

        int strId = vsop.getStrId();
        String optName = ctx.getExecContext().getViewMgr().getView(strId).getName();
        sourceOp.setOptName(optName + "#" + vsop.getId());

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

