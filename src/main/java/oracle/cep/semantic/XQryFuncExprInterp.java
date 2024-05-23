/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XQryFuncExprInterp.java /main/11 2009/11/23 21:21:22 parujain Exp $ */

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
    parujain    10/02/09 - Dependency map
    parujain    03/16/09 - stateless interp
    parujain    01/28/09 - transaction mgmt
    hopark      12/03/08 - keep installer in ExecContext
    hopark      11/20/08 - support lazy seeding
    sbishnoi    11/19/08 - fix bug 7580692
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    najain      02/08/08 - 
    mthatte     12/26/07 - 
    najain      10/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XQryFuncExprInterp.java /main/11 2009/11/23 21:21:22 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXQryFunctionExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.service.ExecContext;
import oracle.cep.install.Install;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.UserFunction;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;


/**
 * The interpreter that is specific to the CEPXQryFunctionExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class XQryFuncExprInterp extends NodeInterpreter
{
  
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    assert node instanceof CEPXQryFunctionExprNode;
    CEPXQryFunctionExprNode fexprNode = (CEPXQryFunctionExprNode)node;

    super.interpretNode( node, ctx);
    
    boolean xmlQuery = fexprNode.getXmlQuery();

    String name = fexprNode.getName();
    CEPExprNode[] tnodes = fexprNode.getParams();

    Datatype[] dts = new Datatype[1];
    dts[0] = Datatype.CHAR;

    ExecContext ec = ctx.getExecContext();
    try{
    String uniqName = ec.getUserFnMgr().
                                 getUniqueFunctionName(name, dts);

    // As this is built-in function; Use default schema to get function
    UserFunction fn = ec.getUserFnMgr().
                                 getFunction(uniqName, ec.getDefaultSchema());
    if (fn == null)
    {
      
      boolean b = ec.getBuiltinFuncInstaller().installFuncs(ec, name, dts);
      if (b) 
      {
        fn = ec.getUserFnMgr().
          getFunction(uniqName, ec.getDefaultSchema());
      }
    }
    Expr[] params = new Expr[tnodes.length];

    for( int i = 0; i < tnodes.length; i++) {
      NodeInterpreter interp = InterpreterFactory.getInterpreter( tnodes[i]);
      interp.interpretNode( tnodes[i], ctx);
      params[i] = ctx.getExpr();
    }

    ValidFunc vfn = new ValidFunc(fn, params);

    //add the query id of the query referencing the function
   // fn.addDestQuery(ctx.getQueryObj().getId());
    
    //add the function id to the corresponding query
 //   ctx.getQueryObj().addRefFunction(fn.getId());
    // Master - fn, Dependent - Query
    ctx.getExecContext().getDependencyMgr().
                         addDependency(fn.getId(), 
                         ctx.getQueryObj().getId(), DependencyType.FUNCTION,
                         DependencyType.QUERY, ctx.getQueryObj().getSchema());
   
    XQryFuncExpr fexpr = new XQryFuncExpr(fn.getId(), fexprNode.getXQryStr(), params, fexprNode.getParamNames(), fn.getReturnType(), 0, xmlQuery ? XQryFuncExprKind.EX_EXPR_XQRY : XQryFuncExprKind.EX_EXPR_XEXTS);
    fexpr.setName( uniqName, false);
    ctx.setExpr( fexpr);
    }catch(MetadataException me)
    {
      me.setStartOffset(fexprNode.getStartOffset());
      me.setEndOffset(fexprNode.getEndOffset());
      throw me;
    }
  }
}
