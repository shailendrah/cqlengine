/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TimeWindowInterp.java /main/8 2013/05/13 06:00:34 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPTimeWindowExprNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/12/13 - bug 16791848
    sbishnoi    02/21/13 - bug 16188514
    udeshmuk    02/20/13 - for variable range window add to_bigint if the expr
                           is returning INT
    sbishnoi    03/16/11 - support for variable duration range window
    parujain    09/08/08 - support offset
    parujain    08/26/08 - semantic exception offset
    sbishnoi    07/25/08 - support for NANOSECOND granularity; correcting
                           conversions between various timeunits
    hopark      12/15/06 - add interpRange
    rkomurav    08/09/06 - slide and cleanup
    najain      04/06/06 - cleanup
    skaluska    04/04/06 - time is in msec 
    anasrini    02/21/06 - fix compile problem 
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TimeWindowInterp.java /main/8 2013/05/13 06:00:34 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.metadata.UserFunction;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPTimeWindowExprNode;
import oracle.cep.common.ArithOp;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.RangeConverter;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.SplRangeType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.extensibility.type.IType.Kind;


/**
 * The interpreter that is specific to the CEPTimeWindowExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class TimeWindowInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    /** Input Range and Slide Amount */
    long                  rangeAmount;
    long                  slideAmount;
    
    /** Input Range and Slide Unit */
    TimeUnit              rangeUnit;
    TimeUnit              slideUnit;

    /** Range and Slide Amount in nanosecond unit */
    long                  rangeNumNanoSeconds = 0;
    long                  slideNumNanoSeconds = 0;
    
    /** Expression which wil evaluate to range amount */
    CEPExprNode           rangeExprNode;
    boolean               isVariableDurationWindow;

    TimeWindowSpec        winspec;
    CEPTimeWindowExprNode winNode;

    assert node instanceof CEPTimeWindowExprNode;
    winNode = (CEPTimeWindowExprNode)node;

    super.interpretNode(node, ctx);

    // Check if the window is a variable duration window
    isVariableDurationWindow = winNode.isVariableDurationWindow();
        
    rangeUnit   = winNode.getRangeUnit();    
    slideUnit   = winNode.getSlideUnit();
    slideAmount = winNode.getSlideAmount();

    /** Convert Input Slide Amount to nanosecond timeunit */
    slideNumNanoSeconds 
      = oracle.cep.common.RangeConverter.interpRange(slideAmount, slideUnit);

    /** Convert Input Range Amount to nanosecond timeunit */
    if(isVariableDurationWindow)
    {
      rangeExprNode = winNode.getRangeExpr();
      try
      {
        NodeInterpreter rangeExprInterp 
        = InterpreterFactory.getInterpreter(rangeExprNode);
        
        rangeExprInterp.interpretNode(rangeExprNode, ctx);
        Expr rangeExpr = ctx.getExpr();
        Expr finalRangeExpr = null;
        
        /**
         * Bug: 16188514 
         * If the range expression is built using extensible type attributes,then
         * we need to convert the original function to native type expression.
         */
        if(rangeExpr.getReturnType().getKind() == Kind.OBJECT)
        {
          rangeExpr =
            TypeCheckHelper.getTypeCheckHelper().buildExtensibleOperatorExpression
            (ctx.getExecContext(), "to_cql", rangeExpr.getReturnType(), rangeExpr);
          
          if(rangeExpr == null)
          {
            throw new CEPException(
              SemanticError.EXTENSIBLE_RANGE_EXPR_DOES_NOT_EVALUATE_TO_INT_BIGINT,
              rangeExprNode.getStartOffset(),
              rangeExprNode.getEndOffset());
          }
        }
        
        // Range Expression should either evaluate to INTEGER or BIGINT
        if(rangeExpr.getReturnType() != Datatype.INT &&
           rangeExpr.getReturnType() != Datatype.BIGINT &&
           !rangeExpr.isNull())
        {
          throw new CEPException(
            SemanticError.RANGE_EXPR_DOES_NOT_EVALUATE_TO_INT_BIGINT,
            rangeExprNode.getStartOffset(),
            rangeExprNode.getEndOffset());
        }
       
        /*
         * Bug 16188386
         * If range expression is of type INT then we should convert it to
         * BIGINT using to_bigint function.
         */
        if(rangeExpr.getReturnType() == Datatype.INT)
        {
          ValidFunc vfn = null;
          vfn = 
            TypeCheckHelper.getTypeCheckHelper().validateExpr("to_bigint", 
              new CEPExprNode[]{rangeExprNode}, ctx, false);
                    
          assert vfn != null;
          finalRangeExpr = new FuncExpr(((UserFunction) vfn.getFn()).getId(), 
                                       new Expr[]{rangeExpr}, Datatype.BIGINT);
          
        }
        else
          finalRangeExpr = rangeExpr;
        
        //Note: It is not possible to determine the range value on the compile
        // time. 
        // On runtime, we have to obtain an expression which can compute the
        // range expression in NANOSECOND unit.
        if(isConversionRequired(rangeUnit))
        {
          Long multiplicationFactor = getMultiplicationFactor(rangeUnit);
          Expr multiplicationExpr;
          
          // Long multiple which will convert the range expression value
          // to nanoseconds granularity
          multiplicationExpr 
            = new ConstBigintExpr(multiplicationFactor.longValue());
          finalRangeExpr = new ComplexExpr(ArithOp.MUL,
                                           finalRangeExpr, 
                                           multiplicationExpr, 
                                           finalRangeExpr.getReturnType());
          
        }
        
        winspec = new TimeWindowSpec(finalRangeExpr, slideNumNanoSeconds);
        winspec.setRangeUnit(rangeUnit);
      }
      catch(CEPException ce)
      {
        ce.setStartOffset(rangeExprNode.getStartOffset());
        ce.setEndOffset(rangeExprNode.getEndOffset());
        throw ce;
      }
    }
    else
    {
      rangeAmount = winNode.getRangeAmount();
      if (rangeAmount == Constants.INFINITE) {
        winspec = new TimeWindowSpec(SplRangeType.UNBOUNDED);
      }
      else {
        rangeNumNanoSeconds 
          = oracle.cep.common.RangeConverter.interpRange(rangeAmount, rangeUnit);
        try{
        winspec = new TimeWindowSpec(rangeNumNanoSeconds, slideNumNanoSeconds);
        }catch(CEPException ce)
        {
          ce.setStartOffset(winNode.getStartOffset());
          ce.setEndOffset(winNode.getEndOffset());
          throw ce;
        }
      }
    }
    
    ctx.setWindowSpec(winspec);
  }  
  
  /**
   * Helper method to check if we need to convert the range expression value
   * to nanoseconds or not
   * @param rangeUnit
   * @return
   */
  private boolean isConversionRequired(TimeUnit rangeUnit)
  {
    return !rangeUnit.equals(TimeUnit.NANOSECOND);
  }
  
  /**
   * Amount which will be required to multiply with range expression value
   * to convert the value into nanosecond granularity
   * @param rangeUnit
   * @return
   */
  private long getMultiplicationFactor(TimeUnit rangeUnit)
  {
    return RangeConverter.interpRange(new Long(1), rangeUnit);
  }
}
