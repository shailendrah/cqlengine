/* $Header: CompOp.java 13-feb-2008.06:25:35 udeshmuk Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Enumeration of the comparison operators supported

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    02/13/08 - correct the existing wrong function name for enum entry
                        LE.
 parujain    10/12/06 - built-in operator functions
 parujain    10/02/06 - Support for LIKE
 rkomurav    09/11/06 - cleanup for xmldump
 anasrini    08/30/06 - add method getSymbol
 rkomurav    08/22/06 - add getHtmlExpression
 najain      02/20/06 - add Clonedummy
 anasrini    02/08/06 - Creation
 anasrini    02/08/06 - Creation
 anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: CompOp.java 13-feb-2008.06:25:35 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of the comparison operators supported
 *
 * @since 1.0
 */

public enum CompOp implements BooleanOp, Cloneable {
  LT("<","&lt;","lt"), LE("<=","&lt;=","ltet"), GT(">","&gt;","gt"), GE(">=","&gt;=","gtet"),
  EQ("=","=","et"), NE("!=","!=","net"), LIKE("like","like","lk");

  private String symbol;
  private String htmlExpr;
  private String funcName;

  /**
   * Constructor
   * @param symbol the popular symbolic representation of this operator
   * @param htmlExpr the html expression of the operator
   */
  CompOp(String symbol, String htmlExpr, String funcName) {
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

  // TODO: a temporary hack for now. 
  // clone() in java.lang.Enum is final, so create a dummy for now.
  public CompOp clonedummy() throws CloneNotSupportedException {
    CompOp op = (CompOp) super.clone();
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
