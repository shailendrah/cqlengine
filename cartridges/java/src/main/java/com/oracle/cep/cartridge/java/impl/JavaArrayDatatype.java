/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaArrayDatatype.java /main/5 2012/06/28 05:37:49 alealves Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      alealves  11/27/09 - Data cartridge context, default package support
    alealves    Sep 3, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaArrayDatatype.java /main/5 2012/06/28 05:37:49 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.type.IArray;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IType;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

public class JavaArrayDatatype extends JavaDatatype implements IArrayType, IArray
{
  private static final long serialVersionUID = 2660891228539554531L;
  
  private final IType componentType;

  public JavaArrayDatatype(JavaTypeSystem javaTypeSystem,
      Class<?> componentJavaClass)
  {
    // We have to do instantiate an zero-length array to be able to get to the Array class,
    //  the loadClass() option won't work.
    super(javaTypeSystem, Array.newInstance(componentJavaClass, 0).getClass());
    
    componentType = javaTypeSystem.getCQLType(componentJavaClass);
  }

  @Override
  public IType getComponentType()
  {
    return componentType;
  }

  @Override
  public Object get(Object obj, int index) throws RuntimeInvocationException, 
  ArrayIndexOutOfBoundsException
  {
    try 
    {
      return Array.get(obj, index);
    } catch (IllegalArgumentException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Array get invocation failed", e);

      throw new RuntimeInvocationException("java", getUnderlyingClass().getName(), e);
    }
  }

  @Override
  public Object instantiate(IType componentType, int length)
      throws RuntimeInvocationException
  {
    try 
    {
      return Array.newInstance(getUnderlyingClass(), length);
    } catch (RuntimeException e)
    {
      if (logger.isDebugEnabled())
        logger.debug("Array instantiation failed", e);

      throw new RuntimeInvocationException("java", getUnderlyingClass().getName(), e);
    }

  }

  @Override
  public IArray getArrayImplementation()
  {
    return this;
  }

  @Override
  public Iterator<Object> iterator(Object obj)
  {
    return new IteratorImpl(obj);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Iterator<Object> iterator(Object obj, IType componentType)
  {
    // We want to certify that the components of the array can be assigned to the type
    //  'componentType' given as an argument.
    if (!componentType.isAssignableFrom(getComponentType())) 
      throw new ClassCastException("Type " + componentType.name()
          + " is not assignable from type " + getComponentType().name());
        
    return new IteratorImpl(obj);
  }
  
  private class IteratorImpl implements Iterator<Object> 
  {
    private Object targetArrayObject;
    private int index;
    private int length;
    
    public IteratorImpl(Object targetArrayObject)
    {
      this.targetArrayObject = targetArrayObject;
      index = 0;
      length = Array.getLength(targetArrayObject);
    }

    @Override
    public boolean hasNext()
    {
      return index < length;
    }

    @Override
    public Object next()
    {
      try {
        return Array.get(targetArrayObject, index++);
      } 
      catch (ArrayIndexOutOfBoundsException e) 
      {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException("remove");
    }
  }
  
  @Override
  public int hashCode()
  {
    return getUnderlyingClass().hashCode() ^ componentType.hashCode();
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
      return true;
    
    if (!(obj instanceof JavaArrayDatatype))
      return false;
    
    return getUnderlyingClass().equals(((JavaArrayDatatype) obj).getUnderlyingClass()) && 
      componentType.equals(((JavaArrayDatatype) obj).componentType);
  }
}
