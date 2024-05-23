/* $Header: LogicalOp.java 20-nov-2006.12:19:31 parujain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Enumeration of the logical operators supported

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/20/06 - XOR implementation
    parujain    11/16/06 - OR/NOT implementation
    parujain    11/03/06 - And Function
    parujain    10/31/06 - clonedummy for Logical oper
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: LogicalOp.java 20-nov-2006.12:19:31 parujain Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.common;

/**
 * Enumeration of the logical operators supported
 *
 * @since 1.0
 */

public enum LogicalOp implements BooleanOp{
  AND("and","and","log_and"), OR("or","or","log_or"), NOT("not","not","log_not"), 
  XOR("xor","xor","log_xor");
  
  private String symbol;
  private String htmlExpr;
  private String funcName;
  
  /**
   * Constructor
   * @param symbol the popular symbolic representation of this operator
   * @param htmlExpr the html expression of the operator
   */
  LogicalOp(String symbol, String htmlExpr, String funcName) {
    this.symbol = symbol;
    this.htmlExpr = htmlExpr;
    this.funcName = funcName;
  }
  
  /**
   * Get the symbolic representation of this operator 
   * @return the symbolic representation of this operator
   */

  public String getSymbol() {
    return symbol;
  }

  
  /**
   * Get the function name correspoding to the arithmetic operator
   * @return the function name of this operator
   */
  public String getFuncName()
  {
    return funcName;
  }

  public LogicalOp clonedummy() throws CloneNotSupportedException {
    LogicalOp op = (LogicalOp) super.clone();
    return op;
  }
  
  //get html expression of the operator
  public String getHtmlExpression() {
    StringBuilder html = new StringBuilder();
    html.append(" ");
    html.append(htmlExpr);
    html.append(" ");
    return html.toString();
  }
}
