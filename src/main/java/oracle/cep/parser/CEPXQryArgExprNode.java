/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXQryArgExprNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/11/08 - error offset
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      10/25/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXQryArgExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for a xquery argument expression
 *
 * @since 1.0
 */

public class CEPXQryArgExprNode implements CEPParseTreeNode {
  protected CEPExprNode expr;
  protected String      name;
  protected int         startOffset;
  protected int         endOffset;

  public CEPXQryArgExprNode(CEPExprNode expr, CEPStringTokenNode nameToken)
  {
    this.expr = expr;
    this.name = nameToken.getValue();
    setStartOffset(expr.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }
  
  public CEPExprNode getExpr()
  {
    return expr;
  }

  public String getName()
  {
    return name;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

}
