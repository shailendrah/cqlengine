/* $Header: pcbpel/cep/common/src/oracle/cep/common/UnaryOp.java /main/3 2009/02/16 19:12:41 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      02/10/09 - support for is_not_null
    parujain    12/04/07 - modify symbol
    parujain    10/12/06 - built-in operator functions
    parujain    09/28/06 - Is null implemenation
    parujain    09/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/common/UnaryOp.java /main/3 2009/02/16 19:12:41 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.common;

/**
 * Enumeration of the Unary operators supported
 *
 * @since 1.0
 */

public enum UnaryOp implements BooleanOp, Cloneable{
  IS_NULL("is null","is_null"),
  IS_NOT_NULL("is not null","is_not_null");
  
  private String symbol;
  private String funcName;
  
  UnaryOp(String symbol, String funcName)
  {
  	this.symbol = symbol;
  	this.funcName = funcName;
  }

  /**
   * Get the symbolic representation of this operator 
   * @return the symbolic representation of this operator
   */
  public String getSymbol()
  {
    return symbol;
  }
  
  /**
   * Get the function name corresponding to the arithmetic operator
   * @return the function name of this operator
   */
  public String getFuncName()
  {
  	return funcName;
  }
  
  public UnaryOp clonedummy() throws CloneNotSupportedException {
    UnaryOp op = (UnaryOp) super.clone();
    return op;
  }
}
