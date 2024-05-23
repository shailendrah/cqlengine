/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

package oracle.cep.extensibility.expr;

/**
 * Interface representing a boolean expression.
 */
public interface BooleanExpression
    extends Expression
{
    public Expression getLeft();

    public Expression getRight();
}
