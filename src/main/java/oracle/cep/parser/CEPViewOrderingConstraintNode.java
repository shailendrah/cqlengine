/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPViewOrderingConstraintNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sborah      03/17/11 - add arith_expr to ordering constraint
    sborah      03/15/11 - view ordering constraint
    sborah      03/15/11 - Creation
 */

/**
 *  @version $Header: CEPViewOrderingConstraintNode.java 15-mar-2011.02:59:07 sborah   Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.OrderingKind;


public class CEPViewOrderingConstraintNode implements CEPParseTreeNode
{
  protected String name;
  
  protected int startOffset;
  
  protected int endOffset;
  /**
   * Holds the query ordering constraint 
   */
  private OrderingKind orderingConstraint;
  
  private CEPExprNode parallelPartioningExpr; 
  
  public CEPViewOrderingConstraintNode(CEPStringTokenNode nameToken, 
      OrderingKind orderingConstraint)
  {
    this.name                   = nameToken.getValue();
    this.parallelPartioningExpr = null;
    
    setOrderingConstraint(orderingConstraint);
    
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  public CEPViewOrderingConstraintNode(CEPStringTokenNode nameToken, 
      OrderingKind orderingConstraint, CEPExprNode parallelPartioningExpr)
  {
    this.name                   = nameToken.getValue();
    this.parallelPartioningExpr = parallelPartioningExpr;
    
    setOrderingConstraint(orderingConstraint);
    
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(parallelPartioningExpr.getEndOffset());
  }
  
  public OrderingKind getOrderingConstraint()
  {
    return orderingConstraint;
  }

  public void setOrderingConstraint(OrderingKind orderingConstraint)
  {
    this.orderingConstraint = orderingConstraint;
  }


  public String getName()
  {
    return name;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public int getStartOffset()
  {
    return startOffset;
  }


  public void setStartOffset(int startOffset)
  {
    this.startOffset = startOffset;
  }


  public int getEndOffset()
  {
    return endOffset;
  }


  public void setEndOffset(int endOffset)
  {
    this.endOffset = endOffset;
  }


  public CEPExprNode getParallelPartioningExpr()
  {
    return parallelPartioningExpr;
  }
}

