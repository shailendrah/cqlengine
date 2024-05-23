/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/OrderByExprInterp.java /main/11 2012/05/02 03:06:03 pkali Exp $ */

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
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    04/01/11 - store name of attr
    skmishra    06/04/09 - adding xmltype check
    sbishnoi    03/17/09 - removing the check that order by expr should be in
                           project list
    sbishnoi    02/10/09 - code restructring
    skmishra    09/09/08 - throw semantic exception for order by column inside
                           xmlagg
    parujain    09/08/08 - support offset
    udeshmuk    06/03/08 - support for xmlagg
    udeshmuk    04/26/08 - parametrize error.
    parujain    11/09/07 - external source
    parujain    10/25/07 - if using ondemand
    parujain    06/26/07 - order by expression interpreter
    parujain    06/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/OrderByExprInterp.java /main/11 2012/05/02 03:06:03 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPOrderByExprNode;
import oracle.cep.parser.CEPParseTreeNode;

class OrderByExprInterp extends NodeInterpreter{
 
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    
    assert node instanceof CEPOrderByExprNode;
    super.interpretNode(node, ctx);
    
    CEPOrderByExprNode orderNode = (CEPOrderByExprNode)node;
    CEPExprNode expr = orderNode.getOrderByExpr() ;
    OrderByExpr orderByExpr = null;
   
    // Condition:
    //  Expressions inside order-by clause are not supported
    //  Order-By Clause should be either an integer pointing to project list
    //   or
    //  Order-By Clause should be an attribute
    if(expr instanceof CEPIntConstExprNode)
    {
      orderByExpr = interpretOrderByNode((CEPIntConstExprNode)expr,
                                         ctx,
                                         orderNode.getIsNullsFirst(),
                                         orderNode.isAscending());
    }
    else if(expr instanceof CEPAttrNode)
    {
      orderByExpr = interpretOrderByNode((CEPAttrNode)expr,
                                         ctx,
                                         orderNode.getIsNullsFirst(),
                                         orderNode.isAscending());
    }
    else
    {
      throw new SemanticException(
        SemanticError.ORDER_BY_EXPRESSION_NOT_AN_ATTRIBUTE,
        orderNode.getStartOffset(), 
        orderNode.getEndOffset()
        ); 
    }  
    ctx.setExpr(orderByExpr);
  }
  
  private OrderByExpr interpretOrderByNode(CEPIntConstExprNode orderByExprNode,
      SemContext ctx,
      boolean isNullsFirst,
      boolean isAscending) 
  throws CEPException
  {
    // Condition:
    // 1) Integer order by expressions are not allowed for order-by in XMLAGG   
    if(ctx.isOrderByInsideXMLAgg())
      throw new CEPException(SemanticError.INVALID_ORDER_BY_IN_XMLAGG,
                              orderByExprNode.getStartOffset(),
                              orderByExprNode.getEndOffset()                              
                             );
    
    
    int pos           = orderByExprNode.getValue();
    Expr selectList[] = ctx.getSelectList();
    
    // Condition:
    // 1) Integer order by expression must be smaller than select list size
    // 2) Integer order by expression must refer to a select list attribute;
    //    Only ORDER BY ATTRIBUTE is supported
    if(selectList.length < pos)
    {
      throw new SemanticException(
        SemanticError.ORDER_BY_POSITION_NOT_A_VALID_POSITION,
        orderByExprNode.getStartOffset(),
        orderByExprNode.getEndOffset(),
        new Object[]{pos});
    }
    else if(!(selectList[pos-1] instanceof AttrExpr))
    {
      throw new SemanticException(
        SemanticError.ORDER_BY_EXPRESSION_NOT_AN_ATTRIBUTE,
        orderByExprNode.getStartOffset(), 
        orderByExprNode.getEndOffset(),
        new Object[]{pos});
    }
    else if(selectList[pos -1].getReturnType() == Datatype.XMLTYPE)
    {
      throw new SemanticException(
          SemanticError.INVALID_XMLTYPE_USAGE,
          orderByExprNode.getStartOffset(), 
          orderByExprNode.getEndOffset(),
          new Object[]{pos});
    }
    else
    {
      AttrExpr attr = (AttrExpr)selectList[pos - 1];
      Expr attrExpr = new AttrExpr(
      new Attr(attr.getAttr().getVarId(), 
               attr.getAttr().getAttrId(),
               attr.getAttr().getActualName(), attr.dt), 
               attr.dt);
      StringBuffer orderName = new StringBuffer("(order by");
      String attrName = attr.getName();
      attrExpr.setName(attrName, attr.isUserSpecifiedName(), attr.isExternal);
      ((AttrExpr)attrExpr).setActualName(attr.getActualName());
      
      OrderByExpr orderByExpr 
        = new OrderByExpr(attrExpr, isNullsFirst,isAscending);
      
      orderName.append(attrName);
      orderName.append(")");
      orderByExpr.setName(orderName.toString(), false);      
      return orderByExpr;
    }
  }
  
  private OrderByExpr interpretOrderByNode(CEPAttrNode orderByExprNode,
      SemContext ctx,
      boolean isNullsFirst,
      boolean isAscending) 
  throws CEPException
  {
    NodeInterpreter orderByInterp
    = InterpreterFactory.getInterpreter(orderByExprNode);
    orderByInterp.interpretNode(orderByExprNode, ctx);

    if(ctx.getExpr().getReturnType() ==  Datatype.XMLTYPE)
    {
      throw new SemanticException(
          SemanticError.INVALID_XMLTYPE_USAGE,
          orderByExprNode.getStartOffset(), 
          orderByExprNode.getEndOffset(),
          new Object[]{ctx.getExpr().getName()});
    }
    OrderByExpr orderByExpr 
      = new OrderByExpr(ctx.getExpr(), isNullsFirst, isAscending);
    StringBuffer orderName = new StringBuffer("(order by");
    String attrExprName    = ctx.getExpr().getName();
    orderName.append(attrExprName);
    orderName.append(")");
    orderByExpr.setName(orderName.toString(), false);
    return orderByExpr;
  }
}
