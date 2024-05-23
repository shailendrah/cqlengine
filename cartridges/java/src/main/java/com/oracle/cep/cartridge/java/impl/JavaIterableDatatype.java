/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaIterableDatatype.java /main/4 2011/05/17 12:08:21 alealves Exp $ */

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
      sbishnoi  03/17/10 - implmenting new IIterabletype method
    alealves    Sep 8, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaIterableDatatype.java /main/3 2010/03/20 08:53:21 sbishnoi Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import java.util.Iterator;

import oracle.cep.extensibility.type.IIterableType;
import oracle.cep.extensibility.type.IType;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

public class JavaIterableDatatype 
  extends JavaDatatype implements IIterableType
{
  private static final long serialVersionUID = -2759343294340692395L;
  
  public <T extends Iterable<?>> JavaIterableDatatype(JavaTypeSystem javaTypeSystem,
      Class<T> iterableClass)
  {
    super(javaTypeSystem, iterableClass);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Iterable<?>> Class<T> getIterableClass()
  {
    return (Class<T>) super.getUnderlyingClass();
  }

  @Override
  public Iterator<Object> iterator(Object obj)
  {
    Iterator<?> iter = (getIterableClass().cast(obj)).iterator();
    return new IteratorImpl<Object>(iter, Object.class);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <C> Iterator<C> iterator(Object obj, IType componentType)
  {
    Class<C> componentClass =
      (Class<C>) getTypeSystem().getJavaType(componentType);
    
    Iterator<?> iter = (getIterableClass().cast(obj)).iterator();
    return new IteratorImpl<C>(iter, componentClass);
  }

  @SuppressWarnings("unchecked")
  @Override
  public IType getComponentType()
  {
    return null;
  }


  private class IteratorImpl<T> implements Iterator<T> 
  {
    private Iterator<?> delegate;
    private Class<T> componentClass;
    
    public IteratorImpl(Iterator<?> delegate, Class<T> componentClass)
    {
      this.delegate = delegate;
      this.componentClass = componentClass;
    }

    @Override
    public boolean hasNext()
    {
      return delegate.hasNext();
    }

    @Override
    public T next()
    {
      Object component = delegate.next();
      return componentClass.cast(component);
    }

    @Override
    public void remove()
    {
      delegate.remove();
    }
  }
}
