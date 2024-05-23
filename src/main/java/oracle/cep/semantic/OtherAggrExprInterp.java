/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/OtherAggrExprInterp.java /main/6 2009/04/06 23:26:52 sborah Exp $ */

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
    parujain    03/13/09 - stateless interp
    sborah      03/10/09 - set exprName to actual name.
    hopark      11/11/08 - use getFuncName instead of AggrFunc.name
    udeshmuk    04/16/08 - support for aggr distinct
    udeshmuk    03/12/08 - remove returntype in staticmetadata.
    udeshmuk    02/18/08 - support for all nulls in function arguments.
    udeshmuk    09/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/OtherAggrExprInterp.java /main/6 2009/04/06 23:26:52 sborah Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPAggrExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPOtherAggrExprNode;
import oracle.cep.parser.CEPParseTreeNode;
/**
 * The interpreter that is specific to the CEPOtherAggrExprNode parse tree 
 * node. 
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 
 */

public class OtherAggrExprInterp extends AggrExprInterp {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
    throws CEPException {

    super.interpretNode(node, ctx);
    CEPAggrExprNode aggrNode = (CEPAggrExprNode)node;
    AggrFunction  aggrFn = aggrNode.getAggrFunction();
    CEPExprNode exprNodes[] = new CEPExprNode[1];
    exprNodes[0]  = aggrNode.getExprNode();
    ValidFunc vfn = TypeCheckHelper.getTypeCheckHelper().validateExpr(aggrFn.getFuncName(), exprNodes, ctx, false);
    Expr[] params = vfn.getExprs();
    if ((vfn.getIsResultNull()) && (params[0] == null))
    {
      Expr expr = Expr.getExpectedExpr(vfn.getFn().getReturnType());
      expr.setName("null", false, false);
      ctx.setExpr(expr);
      return;
    }
    Expr expr          = params[0];
    Datatype paramType = expr.getReturnType();
    String argName;
    
    if(expr instanceof AttrExpr)
      argName       = ((AttrExpr)expr).getActualName();
    else
      argName       = expr.getName();
    
    buildAggrExpr(ctx, expr, ((CEPOtherAggrExprNode)aggrNode).getIsDistinct(),
                  paramType, argName, aggrFn);
  }
  
}   
