/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

package oracle.cep.extensibility.expr;

import oracle.cep.common.Datatype;

/**
 * Interface defining a condition expression in a CQL statement
 */
public interface Expression
{
  public ExprKind getKind();

  public Datatype getType();
}
