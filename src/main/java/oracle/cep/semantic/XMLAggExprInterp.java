/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/XMLAggExprInterp.java /main/3 2009/03/19 20:24:41 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain   03/13/09 - stateless interp
    hopark     11/11/08 - use aggrFn.getFuncName
    udeshmuk   05/30/08 - xmlagg support
    mthatte    05/21/08 - Creation
 */

package oracle.cep.semantic;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.BuiltInAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPAggrExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXMLAggNode;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/XMLAggExprInterp.java /main/3 2009/03/19 20:24:41 parujain Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class XMLAggExprInterp extends AggrExprInterp
{
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    assert node instanceof CEPXMLAggNode;
    super.interpretNode(node, ctx);
    
    CEPExprNode exprNodes[] = new CEPExprNode[1];
    CEPAggrExprNode aggrNode = (CEPAggrExprNode)node;
    AggrFunction  aggrFn = aggrNode.getAggrFunction();
    exprNodes[0]  = aggrNode.getExprNode();
    //Validate the function call
    ValidFunc vfn = TypeCheckHelper.getTypeCheckHelper().validateExpr(
                                    aggrFn.getFuncName(), exprNodes, ctx, false);
    Expr[] params = vfn.getExprs();
    
    Expr expr     = params[0];
    Datatype paramType = expr.getReturnType();
    String argName     = expr.getName();
    
    //Interpret the order by clause
    CEPXMLAggNode xmlnode = (CEPXMLAggNode) node;
    OrderByExpr[] orderByExprs = null;
    if(xmlnode.getOrderByExprs() != null)
    {
      // indicate that order by exists inside xmlagg
      ctx.setOrderByInsideXMLAgg(true);
      boolean oldIsAggrAllowed = ctx.isAggrAllowed();
      ctx.setIsAggrAllowed(false);
      orderByExprs = new OrderByExpr[xmlnode.getOrderByExprs().length];
      for(int i=0; i < xmlnode.getOrderByExprs().length; i++)
      {
        NodeInterpreter interp = InterpreterFactory.getInterpreter(
                                                  xmlnode.getOrderByExprs()[i]);
        interp.interpretNode(xmlnode.getOrderByExprs()[i], ctx);
        orderByExprs[i] = (OrderByExpr)ctx.getExpr();
      }
      //all orderby exprs are processed so reset.
      ctx.setIsAggrAllowed(oldIsAggrAllowed);
      ctx.setOrderByInsideXMLAgg(false);
    }
    buildAggrExpr(ctx, expr, paramType, aggrFn, argName, orderByExprs);
  }
	
  protected void buildAggrExpr(SemContext ctx, Expr expr, Datatype paramType,
		  AggrFunction aggrFn, String argName, OrderByExpr[] orderByExprs)
  {
    String      exprName;
    XMLAggExpr  xmlAggExpr;
    
    Datatype dt = aggrFn.getReturnType(paramType);
    if(orderByExprs!=null)
    {
      exprName = new String(aggrFn + "(" + argName + "order by ");
      for(int i=0; i < orderByExprs.length; i++)
      {
        exprName += orderByExprs[i].getName()+" ";
      }
      exprName += ")";
    }
    else
      exprName = new String(aggrFn + "(" + argName + ")");
    
    xmlAggExpr = new XMLAggExpr(BuiltInAggrFn.get(aggrFn), dt, expr, orderByExprs);
    boolean isExternal = expr.isExternal;
    xmlAggExpr.setName(exprName, false, isExternal);
    ctx.setExpr(xmlAggExpr);
  }
}
