/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

package oracle.cep.extensibility.expr;

import oracle.cep.common.CompOp;
import oracle.cep.common.UnaryOp;

/**
 * Interface representing a basic boolean expression only composed of 
 * simple comparison operators (example: "attr = 100").
 */
public interface BaseBooleanExpression
    extends BooleanExpression
{

  /**
   * Get the comparison operator
   * 
   * @return the comparison operator
   */
  public CompOp getOperator();

  /**
   * Get the unary operator 
   * 
   * @return the unary operator
   */
  public UnaryOp getUnaryOperator();
  
  public Expression getUnaryExpression();
  
}
