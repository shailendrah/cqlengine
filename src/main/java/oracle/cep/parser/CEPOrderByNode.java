/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPOrderByNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sbishnoi    03/05/09 - adding support to process partition by clause
    sbishnoi    02/09/09 - Creation
 */

package oracle.cep.parser;

import java.util.List;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPOrderByNode.java /main/2 2009/03/16 08:27:28 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPOrderByNode implements CEPParseTreeNode
{
  
  /** Array of order-by expressions*/
  protected CEPOrderByExprNode[] orderByClause;
  
  /** Order-By Top expression 
   *  e.g. select c1 from S[range 1 hour] order by c1 ASC rows 100
   *  So here "rows 100" is orderByTopExpr
   */
  protected CEPOrderByTopExprNode orderByTopExpr;
  
  /** start offset for this parse tree node*/
  protected int startOffset = 0;
  
  /** end offset for this parse tree node*/
  protected int endOffset = 0;
  
  public CEPOrderByNode(List<CEPOrderByExprNode> orderByClause , 
                        CEPOrderByTopExprNode paramOrderByTopExpr)
  {
    int orderbyLength = orderByClause.size();
    this.orderByClause     
      = (CEPOrderByExprNode[])(orderByClause.toArray(
          new CEPOrderByExprNode[orderbyLength]));
    
    this.orderByTopExpr = paramOrderByTopExpr;
    
    //Sets the offsets for this parser node
    setStartOffset(orderByClause.get(0).getStartOffset());
    if(orderByTopExpr == null)
      setEndOffset(orderByClause.get(orderbyLength-1).getEndOffset());
    else
      setEndOffset(orderByTopExpr.getEndOffset());
  }
  
  public CEPOrderByExprNode[] getOrderByClause()
  {
    return orderByClause;
  }
  
  public CEPOrderByTopExprNode getOrderByTopExpr()
  {
     return orderByTopExpr;
  }
  
  /**
   * Setter for startOffset
   */
  public void setStartOffset(int paramStartOffset)
  {
    startOffset = paramStartOffset;
  }
  
  /**
   * Setter for endOffset
   */
  public void setEndOffset(int paramEndOffset)
  {
    endOffset = paramEndOffset;
  }
  
  /**
   * Getter for startOffset
   * @return startOffset for this parse tree node
   */
  public int getStartOffset()
  {
    return startOffset;
  }
  
  /**
   * Getter for end Offset
   * @return endOffset for this parse tree node
   */
  public int getEndOffset()
  {
    return endOffset;
  }
}
