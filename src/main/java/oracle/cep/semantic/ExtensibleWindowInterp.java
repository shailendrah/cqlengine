/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ExtensibleWindowInterp.java /main/7 2009/11/23 21:21:22 parujain Exp $ */

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
    parujain    09/25/09 - dependency support
    hopark      11/04/08 - fix schema
    hopark      10/09/08 - remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    03/19/07 - support drop window
    parujain    03/06/07 - Extensible Window Interpreter
    parujain    03/06/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ExtensibleWindowInterp.java /main/7 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.Window;
import oracle.cep.metadata.WindowManager;
import oracle.cep.metadata.DependencyType;
import oracle.cep.parser.CEPConstExprNode;
import oracle.cep.parser.CEPExtensibleWindowExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.service.ExecContext;

/**
 * The interpreter that is specific to the CEPExtensibleWindowExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 */

class ExtensibleWindowInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {
    
    assert node instanceof CEPExtensibleWindowExprNode;
    CEPExtensibleWindowExprNode winNode = (CEPExtensibleWindowExprNode)node;
    
    super.interpretNode(node, ctx);
    
    ExtensibleWindowSpec winSpec = new ExtensibleWindowSpec();
    validateAndInstantiateWindow(winNode, ctx, winSpec);
    ctx.setWindowSpec(winSpec); 
  }
  
  private void validateAndInstantiateWindow(CEPExtensibleWindowExprNode winNode, 
      SemContext ctx, ExtensibleWindowSpec winSpec) throws CEPException
  {
    String winName = winNode.getName();
    CEPConstExprNode[] params = winNode.getParams();
    Datatype[] types = new Datatype[params.length];
    Expr[] exprs = null;
    if(params.length > 0)
      exprs = new Expr[params.length];
    
    for( int i = 0; i < params.length; i++) {
      NodeInterpreter interp = InterpreterFactory.getInterpreter( params[i]);
      interp.interpretNode( params[i], ctx);
      exprs[i] = ctx.getExpr();
      
      // the expression should always have the constant
      assert exprs[i].getExprType() == ExprType.E_CONST_VAL;
      
      types[i] = exprs[i].getReturnType();
    }
    
    try{
    ExecContext ec = ctx.getExecContext();
    Window window = ec.getWindowMgr().getValidWindow(ec, winName,ctx.getSchema(), types);
    
    //add the queryid of the query referencing this window
    // Query is dependent of the Window so Window is the master
    ec.getDependencyMgr().addDependency(window.getId(), ctx.getQueryObj().getId(),
    	DependencyType.WINDOW, DependencyType.QUERY, window.getSchema());
   // window.addDestQuery(ctx.getQueryObj().getId());
    
    // add the window id to the query
  //  ctx.getQueryObj().addRefWindow(window.getId());
    
    winSpec.setId(window.getId());
    }catch(CEPException e)
    {
      e.setStartOffset(winNode.getStartOffset());
      e.setEndOffset(winNode.getEndOffset());
      throw e;
    }
    winSpec.setParams(exprs);
    return;
  }
}
