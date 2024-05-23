/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

package oracle.cep.extensibility.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.LogicalOp;

/**
 * Interface representing a complex boolean expression that is
 * composed of other boolean expressions.
 * For example - "attr1=100 AND attr2&gt;250"
 */
public interface ComplexBooleanExpression
    extends BooleanExpression {

  /**
   * Get the Logical operator
   * 
   * @return the logical operator
   */
  public LogicalOp getOperator();
}


