/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SimpleCaseInterp.java /main/7 2012/06/04 12:17:09 sbishnoi Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/31/12 - fix bug 14050762
    parujain    08/26/08 - semantic exception offset
    udeshmuk    03/12/08 - check for UNKNOWN datatype instead of bNull flag.
    udeshmuk    02/20/08 - handle nulls.
    udeshmuk    02/05/08 - parameterize errors.
    parujain    11/09/07 - external source
    parujain    10/26/07 - is expr ondemand
    parujain    04/02/07 - Simple Case Expression Interpreter
    parujain    04/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SimpleCaseInterp.java /main/7 2012/06/04 12:17:09 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.metadata.UserFunction;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.parser.CEPCaseComparisonExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSimpleCaseExprNode;
import oracle.cep.service.ExecContext;

public class SimpleCaseInterp extends NodeInterpreter{
  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    SimpleCaseExpr simpleExpr;
    String             exprName;
    // This flag keeps track of whether all results are null or not
    // In Case not all results and else can be null
    boolean            isResultNull = true;
    // Currently all the return types of the result should be same
    Datatype   returnType = null;
    boolean    isExternal = false;
    
    assert node instanceof CEPSimpleCaseExprNode;
    CEPSimpleCaseExprNode simpleNode = (CEPSimpleCaseExprNode)node;
  
    super.interpretNode(node, ctx);
    
    exprName = new String("(" + "Case");

    // handling of common comparison expression CASE 'a' WHEN....
    //ctx.setCompExpr(simpleNode.getComparisonExpr());
    NodeInterpreter interp = InterpreterFactory.getInterpreter(simpleNode.getComparisonExpr());
    interp.interpretNode(simpleNode.getComparisonExpr(), ctx);
    Expr comp = ctx.getExpr();
    isExternal =  isExternal || ctx.getExpr().isExternal;
    exprName = exprName + comp.getName();
    //initial data type of common expression ..it might change if the comp expr is literal NULL.
    Datatype initialCommdt = comp.getReturnType();
    //data type of the common expression
    Datatype commdt = initialCommdt;
    boolean commdtChanged =false; //boolean to keep track of the commdt change event
    
    // Determine return type on the basis of expression node
    returnType = determineReturnType(simpleNode,ctx);
    
    // handling of else clause
    if(!simpleNode.isElse())
      simpleExpr = new SimpleCaseExpr(comp);
    else
    {
      NodeInterpreter interpreter = InterpreterFactory.getInterpreter(simpleNode.getElseExpr());
      interpreter.interpretNode(simpleNode.getElseExpr(), ctx);
      isExternal =  isExternal || ctx.getExpr().isExternal;
      simpleExpr = new SimpleCaseExpr(comp, ctx.getExpr());
      isResultNull = false;
    }
    
    boolean allCompExprsNull = true; //true if all comp exprs are null
    CEPCaseComparisonExprNode[] caseExprs = simpleNode.getCaseExprs();
    for(int i=0; i<caseExprs.length; i++)
    {
      NodeInterpreter interpreter = InterpreterFactory.getInterpreter(caseExprs[i]);
      interpreter.interpretNode(caseExprs[i], ctx);
      assert ctx.getExpr() instanceof CaseComparisonExpr;
      isExternal =  isExternal || ctx.getExpr().isExternal;
      CaseComparisonExpr expr = (CaseComparisonExpr)ctx.getExpr();
      simpleExpr.addComparisonExpr(expr);
      if (initialCommdt == Datatype.UNKNOWN)
      {//initial common expression is literal NULL
        if (expr.getComparisonExpr().getReturnType() != Datatype.UNKNOWN)
        { // the recent expression is not literal NULL  so see if it is the first non-null expr
          allCompExprsNull = false;
          if (!commdtChanged)
          { // it is indeed first non-null expr.
            commdt = expr.getComparisonExpr().getReturnType();
            comp   = Expr.getExpectedExpr(commdt);
            //change the common comparison expr and the earlier null valued when clause expressions
            simpleExpr.setCompExpr(comp);
            for (int j=0; j<i; j++)
              simpleExpr.setComparisonExpr(j, new CaseComparisonExpr(
                                                  Expr.getExpectedExpr(commdt)));
            commdtChanged = true;
          }
          else if (expr.getComparisonExpr().getReturnType() != commdt)
            throw new SemanticException(SemanticError.INCONSISTENT_DATATYPES_IN_CASE,
              caseExprs[i].getStartOffset(), caseExprs[i].getEndOffset(),
              new Object[]{commdt, expr.getComparisonExpr().getReturnType()});
          else ; // do nothing, everything is correct till now
        }
        else
        { //current expr is also literal NULL
          if (commdtChanged)
          { //some non-null expr has occurred earlier so convert this
            //literal NULL expr to the same type
            simpleExpr.setComparisonExpr(i, new CaseComparisonExpr(
                                                Expr.getExpectedExpr(commdt))); 
          }
          else ; //do nothing, everything is correct till now
        }
      }
      else
      { // initial common expression is not literal NULL
        allCompExprsNull = false;
        if (expr.getComparisonExpr().getReturnType() != commdt)
        { // current expression type does not match with the expected
          if (expr.getComparisonExpr().getReturnType() == Datatype.UNKNOWN)
          { //convert to a const. expr. of commdt type and set bnull to true
            simpleExpr.setComparisonExpr(i, new CaseComparisonExpr(
                                                Expr.getExpectedExpr(commdt)));
          }
          else
          {
            Datatype fromdt = expr.getComparisonExpr().getReturnType();
            Datatype todt = commdt;
            Expr fexpr = wrapConversionExpr(ctx, expr.getComparisonExpr(), fromdt, todt);
            
            if(fexpr == null)
            {
              //ORA-00932 error
              throw new SemanticException(
                SemanticError.INCONSISTENT_DATATYPES_IN_CASE,
                caseExprs[i].getStartOffset(), caseExprs[i].getEndOffset(),
                new Object[]{commdt, expr.getComparisonExpr().getReturnType()});
            }
            expr.setComparisonExpr(fexpr);
          }
        }
        else ; //do nothing, everything is correct till now
      }
      
      if(expr.getResultExpr() != null)
      {
        exprName = exprName + "(" + "WHEN" + expr.comparisonExpr.getName() + "THEN" + expr.getResultExpr().getName() + ")";
        isResultNull = false;
        Datatype exprReturnType = expr.getReturnType();
        
        // Note: The following condition can only be true if return type of
        // all case comparison expressions are numeric but not matching.
        // In all other cases, determineReturnType performs the validation
        // and throw appropriate error message.
        // Please note that all the expression whose return type isn't
        // returnType variable, they should use implicit conversion function
        // to change the type.
        if((returnType != null) && (!returnType.equals(exprReturnType)))
        {
          // Apply a conversion function
          Expr newReturnExpr = wrapConversionExpr(ctx, expr.getResultExpr(), expr.getReturnType(), returnType);
          expr.setResultExpr(newReturnExpr);
        }
      }
      else
        exprName = exprName + "(" + "WHEN" + expr.comparisonExpr.getName() + "THEN null" + ")";
    }
    
    if(simpleNode.isElse())
      exprName = exprName + "ELSE" + simpleExpr.getElseExpr().getName() + ")";
    else
      exprName = exprName + "ELSE Null" + ")";
    
    //  End of Case statement
    exprName = exprName + ")";
    
    if(isResultNull)
      throw new SemanticException(SemanticError.NOT_ALL_RESULTS_CAN_BE_NULL_IN_CASE,
    		  simpleNode.getStartOffset(), simpleNode.getEndOffset());
    
    if(allCompExprsNull)
    { //all comp exprs including the common expr are of type Unknown, so convert to 
      //some type say INT 
      simpleExpr.setCompExpr(Expr.getExpectedExpr(Datatype.INT));
      for (int i=0; i<caseExprs.length; i++)
      {
        simpleExpr.setComparisonExpr(i, new CaseComparisonExpr(
                                            Expr.getExpectedExpr(Datatype.INT)));
      }
    }
    simpleExpr.setName(exprName, false, isExternal);
    simpleExpr.setReturnType(returnType);
    ctx.setExpr(simpleExpr);
  }
  
  /**
   * Get the implicit conversion function which can convert the datatype of an expression
   * @param ctx    semantic context
   * @param fromdt source datatype
   * @param todt   target datatype
   * @return       Function object
   * @throws CEPException
   */
  private Expr wrapConversionExpr(SemContext ctx, Expr sourceExpr, Datatype fromdt, Datatype todt) throws CEPException
  {
    String implicitConversionFunc 
      = TypeConverter.getTypeConverter().TransOp(fromdt, todt);
  
    // Conversion from <fromdt> to <todt> is not possible
    if(implicitConversionFunc == null)
      return null;
  
    ExecContext ec = ctx.getExecContext();
    UserFunctionManager builtinUserFnMgr = ec.getUserFnMgr();
    Datatype[] types = new Datatype[1];
    types[0] = fromdt;
  
    // Here we should use the default schema name as this is default schema
    UserFunction func = getFunction(builtinUserFnMgr, 
                                  implicitConversionFunc, 
                                  types, 
                                  ec.getDefaultSchema());
    if (func == null)
    {
      boolean b 
        = ec.getBuiltinFuncInstaller().installFuncs(ec, 
                                              implicitConversionFunc,
                                              types);
      if (!b)
        b = ec.getColtInstaller().installFuncs(
            ec, implicitConversionFunc, types);
      if (!b)
        b = ec.getColtAggrInstaller().installFuncs(
          ec, implicitConversionFunc, types);
      if (b)
      {
        func = getFunction(builtinUserFnMgr, 
                         implicitConversionFunc, 
                         types, 
                         ec.getDefaultSchema());
      }
    }
    
    // Create a Wrapper Expression using the conversion function
    Expr[] paramExpr = new Expr[1];
    paramExpr[0] = sourceExpr;
    Expr fexpr 
      = new FuncExpr(func.getId(), paramExpr, func.getReturnType());
    
    String fullName 
      = UserFunctionManager.getUniqueFunctionName(
          implicitConversionFunc, types);
    fexpr.setName(fullName, false, sourceExpr.isExternal);
    return fexpr;
  }
  
  
  /**
   * Returns the datatype for the expression node.
   * @param node
   * @param ctx
   * @return
   * @throws CEPException
   */
  private Datatype determineReturnType(CEPSimpleCaseExprNode node, SemContext ctx) throws CEPException
  {
    Datatype returnType = null;
    CEPCaseComparisonExprNode[] caseExprs = node.getCaseExprs();
    /** A Flag to check if all datatype of all case expressions is numeric */
    boolean isNumeric = true;
    ArrayList<Datatype> exprTypes = new ArrayList<Datatype>();

    // Collect datatype of all case expressions in a list
    for(int i =0; i < caseExprs.length; i++)
    {
      NodeInterpreter interpreter = InterpreterFactory.getInterpreter(caseExprs[i]);
      interpreter.interpretNode(caseExprs[i], ctx);
      assert ctx.getExpr() instanceof CaseComparisonExpr;
      Datatype caseReturnType = ((CaseComparisonExpr)ctx.getExpr()).getReturnType();
      if(caseReturnType != null)
        exprTypes.add(caseReturnType);
    }
    if(node.isElse())
    {
      NodeInterpreter interpreter = InterpreterFactory.getInterpreter(node.getElseExpr());
      interpreter.interpretNode(node.getElseExpr(), ctx);
      if(ctx.getExpr() != null && ctx.getExpr().getReturnType() != null)
        exprTypes.add(ctx.getExpr().getReturnType());
    }
    // Traverse the list and apply the datatype rule
    for(Datatype next: exprTypes)
    {
      if(returnType != null)
      {
        if(isNumeric)
        {
          // All expressions should be either numeric or non-numeric.
          if(!Datatype.isNumeric(next))
            throw new SemanticException(SemanticError.RETURN_TYPE_MISMATCH_IN_CASE,
                new Object[]{"integer,float,bigint,double or number", next});

          // Determine the return type having high precedence
          boolean updateReturnType = TypeConverter.getTypeConverter().Trans(returnType, next) < TypeConverter.INFEASIBLE;
          if(updateReturnType)
            returnType = next;
        }
        else
        {
          if(!returnType.equals(next))
            throw new SemanticException(SemanticError.RETURN_TYPE_MISMATCH_IN_CASE,
              new Object[]{"integer,float,bigint,double or number", next});
        }
      }
      else
      {
        returnType = next;
        isNumeric = Datatype.isNumeric(returnType);
      }
    }
    return returnType;
  }


  
  //This method is for internal type conversions by getting functions like to_timestamp
  private UserFunction getFunction(UserFunctionManager ufnmgr, 
     String funcName, Datatype[] dt, 
     String schema) throws CEPException
  {
     String name = UserFunctionManager.getUniqueFunctionName(funcName, dt);
     return ufnmgr.getFunction(name, schema);
  }
}
