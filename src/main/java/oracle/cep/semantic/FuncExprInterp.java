/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/FuncExprInterp.java /main/23 2010/03/29 12:29:13 alealves Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPFunctionExprNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/12/09 - dependency mgmt
    parujain    10/02/09 - Dependency map
    udeshmuk    09/09/09 - propagate func and link name.
    sborah      06/04/09 - allow aggregation fn as param to aggregation fn
    parujain    03/13/09 - stateless interp
    parujain    02/16/09 - fix offset
    parujain    02/16/09 - fix offset
    parujain    02/16/09 - fix offset
    udeshmuk    12/31/08 - ensure that format argument is always of character
                           type for to_timestamp
    hopark      10/09/08 - remove statics
    parujain    09/25/08 - fix bug
    parujain    09/04/08 - maintain offsets
    skmishra    06/19/08 - adding checks for xmlcomment, xmlcdata
    udeshmuk    04/24/08 - support for aggr distinct.
    udeshmuk    03/12/08 - remove returntype in staticmetadata.
    udeshmuk    02/18/08 - support for all nulls in function arguments.
    parujain    10/25/07 - is expr ondemand
    rkomurav    11/27/07 - share params
    sbishnoi    06/04/07 - support for multiple argument UDAs
    rkomurav    05/28/07 - restructure func expr aggr creation
    parujain    01/31/07 - drop function
    rkomurav    11/26/06 - fix aggr allowed flag
    dlenkov     11/03/06 - overloaded functions
    parujain    10/12/06 - built-in operator functions
    rkomurav    10/09/06 - support expressions as argument
    parujain    10/05/06 - Generic timestamp datatype
    parujain    09/21/06 - To_timestamp builtin function
    najain      09/21/06 - function overloading
    anasrini    08/30/06 - set expr name
    anasrini    07/10/06 - support for user defined aggregations 
    anasrini    06/13/06 - Creation
    anasrini    06/13/06 - Creation
    anasrini    06/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/FuncExprInterp.java /main/23 2010/03/29 12:29:13 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.IAggrFunctionMetadata;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.metadata.AggFunction;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.ObjectId;
import oracle.cep.metadata.UserFunction;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPFunctionExprNode;
import oracle.cep.parser.CEPParseTreeNode;

/**
 * The interpreter that is specific to the CEPFunctionExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class FuncExprInterp extends NodeInterpreter {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    boolean isExternal = false;

    assert node instanceof CEPFunctionExprNode;
    CEPFunctionExprNode fexprNode = (CEPFunctionExprNode)node;

    super.interpretNode( node, ctx);

    String name = fexprNode.getName();
    CEPExprNode[] tnodes = fexprNode.getParams();
    
    if(name.equals("to_timestamp") && (tnodes.length==2))
    {
      // format argument ..there should not be implicit conversion here
      NodeInterpreter interp = InterpreterFactory.getInterpreter(tnodes[1]);
      interp.interpretNode(tnodes[1], ctx);
      Expr format = ctx.getExpr();
      if((format.getReturnType() != Datatype.CHAR) && 
         (format.getReturnType() != Datatype.UNKNOWN))
        throw new SemanticException(SemanticError.WRONG_NUMBER_OR_TYPES_OF_ARGUMENTS, 
                          tnodes[1].getStartOffset(), tnodes[1].getEndOffset(),
                                    new Object[]{name});
    }

    ValidFunc vfn = null;
    try
    {
      // first assume that function is not an aggregate function
      // and hence allow its child functions to be aggregate in nature
      // as well.
      vfn = TypeCheckHelper.getTypeCheckHelper().
        validateExpr(name, tnodes, ctx, true, fexprNode.getLink());
      
    }
    catch(CEPException ce)
    {
      // When we call validateExpr, we don't pass along the node, hence 
      // we need to catch and set error offset here if not set.
      if (ce.getStartOffset() == 0)
      {
        ce.setStartOffset(fexprNode.getStartOffset());
        ce.setEndOffset(fexprNode.getEndOffset());
      }
      
      if(ce.getErrorCode() instanceof SemanticError)
        ce.setStartOffset(fexprNode.getStartOffset());
      throw ce;
    }

    IUserFunctionMetadata fn = vfn.getFn();
    
    // We had originally assumed it as a single row function , 
    // now we will confirm that this is is the case.
    
    if(fn instanceof IAggrFunctionMetadata && fn instanceof AggFunction) {
      try
      {
        // Since our assumption that it is a single row function is wrong 
        // we try to re-validate the type of the function with the knowledge 
        // that it is an aggregate function and hence, its child functions 
        // cannot be aggregate in nature.
        vfn = TypeCheckHelper.getTypeCheckHelper().
                                        validateExpr( name, tnodes, ctx, false);
      }
      catch(CEPException ce)
      {
        if(ce.getErrorCode() instanceof SemanticError)
          ce.setStartOffset(fexprNode.getStartOffset());
        throw ce;
      }
    } 
    
    Expr[] params = vfn.getExprs();
    ctx.setParams(params);
    
    
    //Special handling for xml pub funcs 
    try{
    if(name.equals("xmlcomment") || name.equals("xmlcdata"))
      validateArguments(name, params);
    }catch(SemanticException e)
    {
      e.setStartOffset(fexprNode.getStartOffset());
      e.setEndOffset(fexprNode.getEndOffset());
      throw e;
    }
    
    boolean allParamsNull = true;
    for (int i=0; i < params.length; i++)
    {
      if (!params[i].bNull) allParamsNull = false;
    }
    
    if ((vfn.getIsResultNull()) && allParamsNull)
    {
      // all parameters are null and the function evaluates to null as per
      // the StaticMetadata..so replace by a constExpr of appropriate type.
      Expr expr = Expr.getExpectedExpr(fn.getReturnType());
      expr.setName("null", false, false);
      ctx.setExpr(expr);
      return;
    }
    Datatype[] dts = new Datatype[params.length];
    for (int i = 0; i < params.length; i++)
    {
      dts[i] = params[i].getReturnType();
      isExternal = isExternal || params[i].isExternal;
    }
    
    String fullName = UserFunctionManager.getUniqueFunctionName( name, dts);
    
    int id = 0;
    if (fn instanceof UserFunction) 
    {
      id = ((UserFunction) fn).getId();
    }
    else
    {
      // This function is owned and managed by an external system (i.e. cartridge),
      //  hence we don't have an ID from our cache. However, we would still like to
      //  associate an ID so that is easier to identify the function. Therefore
      //  we will rely on ObjectId, as our common ID factory.
      id = ObjectId.getNextId();
    }
    // Built-in aggregated function
    if (fn instanceof IAggrFunctionMetadata && fn instanceof AggFunction) {
      assert fn instanceof AggFunction;
      AggFunction aggFn = (AggFunction) fn;
      UserDefAggrFn userAggr = new UserDefAggrFn(id, fn.getReturnType(),
          aggFn.supportsIncremental());
      AggrExpr aggrExpr = new AggrExpr(userAggr, fn.getReturnType(), params);
      aggrExpr.setName(fullName, false, isExternal);
      aggrExpr.setIsDistinctAggr(fexprNode.getIsDistinctAggr());
      ctx.setExpr(aggrExpr);
    }
    // Cartridge based aggregated function
    else if (fn instanceof IAggrFunctionMetadata) {
      IAggrFunctionMetadata afmd = (IAggrFunctionMetadata) fn;
      UserDefAggrFn userAggr = new UserDefAggrFn(id, fn.getReturnType(),
          afmd.supportsIncremental(), name, fexprNode.getLink().getValue());
      AggrExpr aggrExpr = new AggrExpr(userAggr, fn.getReturnType(), params);
      aggrExpr.setName(fullName, false, isExternal);
      aggrExpr.setIsDistinctAggr(fexprNode.getIsDistinctAggr());
      ctx.setExpr(aggrExpr);
    }
    else{
      if (fexprNode.getIsDistinctAggr())
        throw new SemanticException(SemanticError.DISTINCT_NOT_ALLOWED_HERE,
                          fexprNode.getStartOffset(), fexprNode.getEndOffset(),
                          new Object[] {name});
      
      FuncExpr fexpr;
      if(fn instanceof UserFunction)
        fexpr = new FuncExpr(id, params, fn.getReturnType());
      else //external (cartridge) function
        fexpr = new FuncExpr(id, params, fn.getReturnType(), name, fexprNode
                            .getLink().getValue());
      
      if (fn instanceof ISimpleFunctionMetadata) 
      {
        ISimpleFunctionMetadata sfm = 
          (ISimpleFunctionMetadata) vfn.getFn();
        
        // INVARIANT: built-in functions do not have a impl class, hence the reason
        //  code-gen checks for them later, so that it can set its own code.
        fexpr.setFuncImpl(sfm.getImplClass());
      }
      
      fexpr.setName( fullName, false, isExternal);
      ctx.setExpr( fexpr);
    }
    
    // ADD ONLY WHEN SUCCESS
    
    // REVIEW alealves
    // Manage only dependencies for objects that the engine owns. 
    // For example, we need to know which queries use a function, so that we can
    //  determine if a function can be dropped. A function retrieved from a cartridge
    //  cannot be dropped, hence we don't need to manage its dependency.
    if (fn instanceof UserFunction)
    {
      UserFunction uf = (UserFunction) fn;
      //add the query id of the query referencing the function
    //  manageable.addDestQuery(ctx.getQueryObj().getId());
      
      //add the function id to the corresponding query
     // ctx.getQueryObj().addRefFunction(manageable.getId());
      ctx.getExecContext().getDependencyMgr().
                           addDependency(uf.getId(), 
                           ctx.getQueryObj().getId(), DependencyType.FUNCTION,
                           DependencyType.QUERY, ctx.getQueryObj().getSchema());
    }
    
  }
  
  private void validateArguments(String name, Expr[] params) throws SemanticException
  {
    Expr arg;  
    
    if(name.equals("xmlcomment"))
    {
      assert params.length == 1;
      arg = params[0];
      if(arg.getExprType() == ExprType.E_CONST_VAL)
      {
        String argString = ((ConstCharExpr)arg).getValue();
        if(argString.contains("--"))
          throw new SemanticException(SemanticError.INVALID_CHARS_IN_XMLCOMMENT_ARG, new Object[]{argString});
      }
    }
    else if(name.equals("xmlcdata"))
    {
      assert params.length == 1;
      arg = params[0];
      if(arg.getExprType() == ExprType.E_CONST_VAL)
      {
        String argString = ((ConstCharExpr)arg).getValue();
        if(argString.contains("]]>"))
          throw new SemanticException(SemanticError.INVALID_CHARS_IN_XMLCDATA_ARG, new Object[]{argString});
      }
    }
  }
}
