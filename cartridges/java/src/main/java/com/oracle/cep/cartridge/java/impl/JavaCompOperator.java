/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaCompOperator.java /main/1 2011/03/02 16:46:14 alealves Exp $ */

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
    alealves    Feb 14, 2011 - Creation
 */

/**
 *  @version $Header$
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import oracle.cep.common.CompOp;
import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

class JavaCompOperator implements ISimpleFunctionMetadata, ISimpleFunction
{
  private CompOp op;
  private IAttribute[] attrs;
  
  public JavaCompOperator(CompOp op, Datatype[] paramTypes)
  {
    this.op = op;
    this.attrs = new IAttribute [paramTypes.length];
    
    for (int i = 0; i < paramTypes.length; i++)
      attrs[i] = new Attribute("attr" + i, paramTypes[i], 0);
  }

  @Override
  public int getNumParams()
  {
    return attrs.length;
  }

  @Override
  public IAttribute getParam(int pos) throws MetadataException
  {
    return attrs[pos];
  }

  @Override
  public Datatype getReturnType()
  {
    return Datatype.BOOLEAN;
  }

  @Override
  public String getName()
  {
    return op.getFuncName();
  }

  @Override
  public String getSchema()
  {
    return null; // REVIEW
  }

  @Override
  public UserDefinedFunction getImplClass()
  {
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object execute(Object[] args, ICartridgeContext context)
      throws RuntimeInvocationException
  {
    switch (op) 
    {
    case LT:
      return (((Comparable) args[0]).compareTo(args[1]) < 0);
    case LE:
      return (((Comparable) args[0]).compareTo(args[1]) <= 0);
    case EQ:
      return args[0].equals(args[1]);
    case NE:
      return !args[0].equals(args[1]);
    case GE:
      return (((Comparable) args[0]).compareTo(args[1]) >= 0);
    case GT:
      return (((Comparable) args[0]).compareTo(args[1]) > 0);
    case LIKE:
      return ((String) args[0]).matches((String) args[1]);
    default:
      throw new IllegalStateException("Unsupported operation: " + op);
    }
  }
  
}