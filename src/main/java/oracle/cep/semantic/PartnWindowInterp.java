/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PartnWindowInterp.java /main/10 2013/05/13 06:00:34 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/12/13 - bug 16791571
    sbishnoi    02/20/13 - bug 16094448
    vikshukl    05/31/12 - fix subquery and partition window
    sbishnoi    12/03/11 - support of variable duration partition window
    parujain    03/12/09 - make interpreters stateless
    hopark      02/04/09 - use local scope
    parujain    09/08/08 - support offset
    sbishnoi    07/25/08 - support for nanosecond; changing variable names
    hopark      12/15/06 - add range
    ayalaman    07/31/06 - partition window interpreter
    ayalaman    07/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PartnWindowInterp.java /main/10 2013/05/13 06:00:34 sbishnoi Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.metadata.UserFunction;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPPartnWindowExprNode;
import oracle.cep.common.ArithOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.RangeConverter;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.extensibility.type.IType.Kind;
import oracle.cep.parser.CEPAttrNode;

/**
 *  The interpreter for the partition window specification 
 */
class PartnWindowInterp extends NodeInterpreter
{
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  /**
   *  Interpret the current node using the semantic context passed in
   *
   *  @param   node    current node to be interpreted
   *  @param   ctx     semantic context 
   * 
   *  @throws CEPException  for any semantic errors in partition window spec. 
   */
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException 
  {
    PartnWindowSpec         pwinSpec; 
    CEPPartnWindowExprNode  pwinNode; 
    SymbolTableAttrEntry   attrs[] = null;

    assert node instanceof CEPPartnWindowExprNode; 

    pwinNode = (CEPPartnWindowExprNode)node; 

    // Create a new symbol table for this new scope
    SymbolTable oldSymbolTable = ctx.getSymbolTable();
    String winName = ctx.getWindowRelName();
    String srcName = ctx.getWindowRelAlias();
    if (srcName == null)
      srcName = winName;
    SymbolTableSourceEntry entry = oldSymbolTable.lookupSource(srcName);
    // make sure that the local varId is same as global varId 
    // Otherwise, the attribute will not match.
    SymbolTable newSymbolTable = new SymbolTable(entry.getVarId());

    if (entry.getSourceType() != SymbolTableSourceType.PERSISTENT) {
      // we need to copy all attributes that belong to the subquery / view
      // in the new symbol table
      attrs = ctx.getSymbolTable().getAllAttrs(ctx.getExecContext(), 
                                               entry.getVarId());      
    }

    ctx.setSymbolTable(newSymbolTable);
    
    try 
    {
      // unaliased subqueries are not allowed so this assert is fine for now.
      assert (winName != null);

      //S [partition by X.c1] X is not allowed.
      if (entry.getSourceType() == SymbolTableSourceType.PERSISTENT ) 
      {
          newSymbolTable.addPersistentSourceEntry(ctx.getExecContext(), 
                                                  winName, 
                                                  winName, 
                                                  ctx.getSchema());
      }
      else
      {
        newSymbolTable.addInlineSourceEntry(winName, winName, false);
        assert attrs != null;
        for (int i = 0; i < attrs.length; i++) 
        {
          newSymbolTable.addAttrEntry(attrs[i].getVarName(), winName, 
                                      attrs[i].getAttrId(), 
                                      attrs[i].getAttrType(), 
                                      attrs[i].getAttrLen());
        }
      }
    }
    catch(CEPException ce)
    {
      ce.setStartOffset(pwinNode.getStartOffset());
      ce.setEndOffset(pwinNode.getEndOffset());
      throw ce;
    }

    // a partition window node has the partition by specification and the rows spec
    pwinSpec = new PartnWindowSpec(); 

    super.interpretNode(node, ctx);

    // process the PARTITION BY specification 
    interpretPartnByClause (pwinNode, ctx, pwinSpec);
    
    long slideNumNanoSeconds 
      = pwinNode.hasRange() ? 
        oracle.cep.common.RangeConverter.interpRange(
          pwinNode.getSlideAmount(), pwinNode.getSlideUnit())
          : 0;
    pwinSpec.setSlideUnits(slideNumNanoSeconds);                                              
    pwinSpec.setNumRows(pwinNode.getNumRows()); 
  
    // Calculate Range Expression or Constant Value
    if(pwinNode.isVariableDuration())
    {
      NodeInterpreter rangeExprInterp 
        = InterpreterFactory.getInterpreter(pwinNode.getRangeExpr());
    
      rangeExprInterp.interpretNode(pwinNode.getRangeExpr(), ctx);
      Expr rangeExpr = ctx.getExpr();
      Expr finalRangeExpr = null;
      
      /**
       * Bug 16791571
       * If the range expression is built using extensible type attribute then
       * we need to convert the original expression to native cql type expression
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
            pwinNode.getRangeExpr().getStartOffset(),
            pwinNode.getRangeExpr().getEndOffset());
        }
      }
      
      // Range Expression should either evaluate to INTEGER or BIGINT
      if(rangeExpr.getReturnType() != Datatype.INT &&
         rangeExpr.getReturnType() != Datatype.BIGINT &&
         !rangeExpr.isNull())
      {
        throw new CEPException(
          SemanticError.RANGE_EXPR_DOES_NOT_EVALUATE_TO_INT_BIGINT,
          pwinNode.getRangeExpr().getStartOffset(),
          pwinNode.getRangeExpr().getEndOffset());
      }
      
      /*
       * Bug 16773962
       * If range expression is of type INT then we should convert it to
       * BIGINT using to_bigint function.
       */
      if(rangeExpr.getReturnType() == Datatype.INT)
      {
        ValidFunc vfn = null;
        vfn = 
          TypeCheckHelper.getTypeCheckHelper().validateExpr("to_bigint", 
            new CEPExprNode[]{pwinNode.getRangeExpr()}, ctx, false);
                  
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
      TimeUnit rangeUnit = pwinNode.getRangeUnit();
      
      if(isConversionRequired(rangeUnit))
      {
        Long multiplicationFactor = getMultiplicationFactor(rangeUnit);

        // Long multiple which will convert the range expression value
        // to nanoseconds granularity
        Expr multiplicationExpr 
            = new ConstBigintExpr(multiplicationFactor.longValue());

        finalRangeExpr = new ComplexExpr(ArithOp.MUL,
                                         finalRangeExpr, 
                                         multiplicationExpr, 
                                         finalRangeExpr.getReturnType());
      }
      else
      {
        // Nothing to do if their is no conversion required;
        finalRangeExpr = rangeExpr;
      }

      pwinSpec.setRangeExpr(finalRangeExpr);
      pwinSpec.setRangeUnit(pwinNode.getRangeUnit());
    }
    else
    {
      long rangeNumNanoSeconds = pwinNode.hasRange() ?
        oracle.cep.common.RangeConverter.interpRange(pwinNode.getRangeAmount(), 
                                                   pwinNode.getRangeUnit()): 0;          
      pwinSpec.setRangeUnits(rangeNumNanoSeconds);
      pwinSpec.setRangeUnit(pwinNode.getRangeUnit());
      
      // Semantic Check: Slide shouldn't be higher than range value
      if(slideNumNanoSeconds > rangeNumNanoSeconds)
      {
        throw new SemanticException(SemanticError.SLIDE_GREATER_THAN_RANGE,
            new Object[]{slideNumNanoSeconds, rangeNumNanoSeconds});
      }
    }
    pwinSpec.setVariableDurationWindow(pwinNode.isVariableDuration());
    
    
    ctx.setWindowSpec(pwinSpec); 

    // Now restore the old symbol table
    ctx.setSymbolTable(oldSymbolTable);
    ctx.getSemQuery().symTable = oldSymbolTable;

  }

  /**
   * Interpret the partition by clause of a query
   * <p>
   * Converts the list of partition by attributes in the clause to their
   * semantic forms
   *
   * @param   pwinNode   parse tree node for the partition window spec. 
   * @param   ctx        semantic context 
   * @param   pwinSpec   partition window spec that will be updated with 
   *                     the PARTITION BY attribute specification
   */
  private void interpretPartnByClause(CEPPartnWindowExprNode pwinNode,
                                      SemContext ctx,
                                      PartnWindowSpec   pwinSpec)
       throws CEPException 
  {
    CEPAttrNode[]   pbyAttrs; 
    NodeInterpreter attrInterp;
    AttrExpr        attrExpr;

    pbyAttrs = pwinNode.getPartByClause(); 

    if (pbyAttrs != null)
    {
      int  pbyLen = pbyAttrs.length; 

      for (int ai=0; ai < pbyLen; ai++)
      {
        attrInterp = InterpreterFactory.getInterpreter(pbyAttrs[ai]); 
        attrInterp.interpretNode(pbyAttrs[ai], ctx); 
  
        attrExpr = (AttrExpr)ctx.getExpr();

        // update the partition window specification with the attr info
        try{
        pwinSpec.addPartnByAttr(attrExpr.getAttr());
        }catch(CEPException e)
        {
          e.setStartOffset(pbyAttrs[ai].getStartOffset());
          e.setEndOffset(pbyAttrs[ai].getEndOffset());
          throw e;
        }
      }
    }

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
