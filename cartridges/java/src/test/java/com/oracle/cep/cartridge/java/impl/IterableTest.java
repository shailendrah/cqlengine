/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/IterableTest.java /main/1 2009/09/19 05:25:32 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    Sep 9, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/IterableTest.java /main/1 2009/09/19 05:25:32 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.type.IArray;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IIterableType;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

public class IterableTest extends TestCase
{
  private JavaTypeSystem javaTypeSystem;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    
    javaTypeSystem = new JavaTypeSystemImpl();
  }
  
  public void testTypedArrayIteration() throws MetadataNotFoundException 
  {
    Integer [] array = new Integer [] {1, 2};
    
    IArrayType type = new JavaArrayDatatype(javaTypeSystem, Integer.class);
    Iterator<Object> cartridgeIter = null;
    
    cartridgeIter =
      type.iterator(array, javaTypeSystem.getCQLType(Object.class));
    
    Object object = cartridgeIter.next();
    assertNotNull(object);
    
    Iterator<Number> cartridgeIter2 = null;
    
    cartridgeIter2 =
      type.iterator(array, javaTypeSystem.getCQLType(Number.class));
    
    Number number =
      cartridgeIter2.next();
    assertNotNull(number);
    
    Exception expected = null;
    try 
    {
      Iterator<Short> cartridgeIter3 = null;
      
      cartridgeIter3 =
        type.iterator(array, javaTypeSystem.getCQLType(Short.class));
      
      Short sh = 
        cartridgeIter3.next();
      assertNotNull(sh);
    } catch (ClassCastException e) 
    {
      expected = e;
    }
    assertNotNull(expected);
    
  }
  
  public void testTypedCollectionIteration() throws MetadataNotFoundException 
  {
    List<Integer> list = new ArrayList<Integer>();
    
    IIterableType type = new JavaIterableDatatype(javaTypeSystem, list.getClass());
    
    list.add(1);
    list.add(2);
    
    Iterator<Object> cartridgeIter =
      type.iterator(list, javaTypeSystem.getCQLType(Object.class));
    
    Object object = cartridgeIter.next();
    assertNotNull(object);
    
    Iterator<Number> cartridgeIter2 =
      type.iterator(list, javaTypeSystem.getCQLType(Number.class));
    
    Number number =
      cartridgeIter2.next();
    assertNotNull(number);
    
    Exception expected = null;
    try 
    {
      Iterator<Short> cartridgeIter3 =
        type.iterator(list, javaTypeSystem.getCQLType(Short.class));
      
      Short sh = 
        cartridgeIter3.next();
      assertNotNull(sh);
    } catch (ClassCastException e) 
    {
      expected = e;
    }
    assertNotNull(expected);
  }  

  public void testCollection() throws MetadataNotFoundException 
  {
    List<Integer> list = new ArrayList<Integer>();
        
    IIterableType type = new JavaIterableDatatype(javaTypeSystem, list.getClass());
    
    list.add(1);
    list.add(2);
    
    Iterator<?> cartridgeIter =
      type.iterator(list);
    
    Iterator<?> javaIter =
      list.iterator();
    
    //
    // Non-empty list
    //
    
    while (javaIter.hasNext()) 
    {
      assertTrue(cartridgeIter.hasNext());
      assertTrue(javaIter.next().equals(cartridgeIter.next()));
    }
    
    assertFalse(cartridgeIter.hasNext());

    Exception expected = null;
    try 
    {
      cartridgeIter.next();
    } catch (NoSuchElementException e) 
    {
      expected = e;
    }
    
    assertNotNull(expected);
    
    //
    // Empty list
    //
    list.clear();
    
    cartridgeIter = 
      type.iterator(list);
    
    assertFalse(cartridgeIter.hasNext());

    expected = null;
    try 
    {
      cartridgeIter.next();
    } catch (NoSuchElementException e) 
    {
      expected = e;
    }
    
    assertNotNull(expected);
  }
  
  public void testArray() throws MetadataNotFoundException, RuntimeInvocationException 
  {
    Integer [] array = new Integer [] {1, 2};
        
    IArrayType type = new JavaArrayDatatype(javaTypeSystem, Integer.class);
    
    Iterator<?> cartridgeIter =
      type.iterator(array);
    
    //
    // Non-empty array
    //
    int i = 0;
    IArray arrayImpl = type.getArrayImplementation();
    
    while (cartridgeIter.hasNext())
    {
      assertEquals(cartridgeIter.next(), array[i]);
      assertEquals(arrayImpl.get(array, i), array[i++]);
    }
    
    Exception expected = null;
    try 
    {
      cartridgeIter.next();
    } catch (NoSuchElementException e) 
    {
      expected = e;
    }
    
    assertNotNull(expected);
    
    expected = null;
    try 
    {
      arrayImpl.get(array, i);
    } catch (ArrayIndexOutOfBoundsException e) 
    {
      expected = e;
    }
    
    assertNotNull(expected);
    
    //
    // Test empty array
    //
    cartridgeIter =
      type.iterator(new Integer[]{});
    
    assertFalse(cartridgeIter.hasNext());
    
    expected = null;
    try 
    {
      cartridgeIter.next();
    } catch (NoSuchElementException e) 
    {
      expected = e;
    }
    
    assertNotNull(expected);
    
    expected = null;
    try 
    {
      arrayImpl.get(new Integer[]{}, 0);
    } catch (ArrayIndexOutOfBoundsException e) 
    {
      expected = e;
    }
    
    assertNotNull(expected);
  }
}
