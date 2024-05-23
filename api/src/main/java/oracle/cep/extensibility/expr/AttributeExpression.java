/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

package oracle.cep.extensibility.expr;

/**
 * Interface representing an expression which is a reference to an attribute.
 */
public interface AttributeExpression
    extends Expression
{
  public String getAttributeName();
}
