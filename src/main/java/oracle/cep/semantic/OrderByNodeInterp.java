/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/OrderByNodeInterp.java /main/1 2009/03/31 02:50:09 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/17/09 - Creation
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError; 
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPOrderByExprNode;
import oracle.cep.parser.CEPOrderByNode;
import oracle.cep.parser.CEPOrderByTopExprNode;
import oracle.cep.parser.CEPParseTreeNode;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/OrderByNodeInterp.java /main/1 2009/03/31 02:50:09 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class OrderByNodeInterp extends NodeInterpreter
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException 
  {
    assert node instanceof CEPOrderByNode : node;
    CEPOrderByNode        orderByNode = (CEPOrderByNode)node;

    CEPOrderByExprNode[]  orderNodes       = null;
    CEPOrderByTopExprNode orderByTopExpr   = null;
    CEPAttrNode[]         partitionByAttrs = null;
        
    // Get array of order-by expressions
    orderNodes = orderByNode.getOrderByClause();
    
    // Get order-by top expression
    orderByTopExpr = orderByNode.getOrderByTopExpr();
    
    // Get array of order-by partition attributes
    if(orderByTopExpr != null && 
         orderByTopExpr.isPartitionByClauseExist())
    {
      partitionByAttrs = orderByTopExpr.getPartitionByClause();
    }
    
    // interpret order-by expressions
    Expr tempOrderByExpr;    
    for(int i=0; i < orderNodes.length; i++)
    {
      NodeInterpreter interp = InterpreterFactory.getInterpreter(orderNodes[i]);
      interp.interpretNode(orderNodes[i], ctx);
      
      tempOrderByExpr = ctx.getExpr();
      ctx.addOrderByExprs(tempOrderByExpr);
      
      if(tempOrderByExpr.getReturnType() == Datatype.XMLTYPE)
        throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
            orderNodes[i].getStartOffset(), orderNodes[i].getEndOffset(),
                           new Object[]{tempOrderByExpr.getName()});
    }
    
    // interpret order by top expressions
    if(orderByTopExpr != null)
    {
      if(orderByTopExpr.getNumRows() <= 0)
        throw new CEPException(SemanticError.INVALID_ORDER_BY_USAGE_1,
                             orderNodes[0].getStartOffset(),
                             orderByTopExpr.getEndOffset());
      else
        ctx.setNumOrderByRows(orderByTopExpr.getNumRows());
    }
    
    // interpret partition by expressions
    if(partitionByAttrs != null)
    {
      for(int i=0; i < partitionByAttrs.length; i++)
      {
        NodeInterpreter attrInterp 
          = InterpreterFactory.getInterpreter(partitionByAttrs[i]); 
        attrInterp.interpretNode(partitionByAttrs[i], ctx); 

        AttrExpr attrExpr = (AttrExpr)ctx.getExpr();
        ctx.addPartitionByAttrs(attrExpr);       
      }
    }
    
  }
  
  
}
