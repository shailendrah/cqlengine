/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved. */
package com.oracle.cep.cartridge.java.impl;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import junit.framework.TestCase;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

public class CastTest extends TestCase
{
  private JavaTypeSystem javaTypeSystem;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    
    javaTypeSystem = new JavaTypeSystemImpl();
  }
  
  public void testWideningConversion() throws MetadataNotFoundException, AmbiguousMetadataException, RuntimeInvocationException 
  {
    JavaDatatype integerDt = new JavaDatatype(javaTypeSystem, int.class);
    JavaDatatype longDt = new JavaDatatype(javaTypeSystem, long.class);
    
    JavaFunctors functors = new JavaFunctors(javaTypeSystem);
    
    IUserFunctionMetadata function = 
      functors.getFunction("cast", new Datatype[] {integerDt, longDt}, null);

    assertNotNull(function);
    
    Long longValue = 
      (Long) ((ISimpleFunction) function).execute(new Object[] {new Integer(10)}, null);
    
    assertEquals(new Long(10l), longValue);
    
    JavaDatatype charDt = new JavaDatatype(javaTypeSystem, char.class);
    JavaDatatype IntegerDt = new JavaDatatype(javaTypeSystem, Integer.class);
    
    function = 
      functors.getFunction("cast", new Datatype[] {charDt, IntegerDt}, null);

    assertNotNull(function);
    
    int intValue = 
      (Integer) ((ISimpleFunction) function).execute(new Object[] {new Character('a')}, null);
    
    assertEquals(97, intValue);
    
    JavaDatatype FloatDt = new JavaDatatype(javaTypeSystem, Float.class);
    JavaDatatype DoubleDt = new JavaDatatype(javaTypeSystem, Double.class);
    
    function = 
      functors.getFunction("cast", new Datatype[] {FloatDt, DoubleDt}, null);

    assertNotNull(function);
    
    Double doubleValue = 
      (Double) ((ISimpleFunction) function).execute(new Object[] {new Float(100.0)}, null);
    
    assertEquals(100.0, doubleValue);
  }
  
  public void testNarrowingConversion() throws MetadataNotFoundException, AmbiguousMetadataException, RuntimeInvocationException 
  {
    JavaDatatype longDt = new JavaDatatype(javaTypeSystem, long.class);
    JavaDatatype integerDt = new JavaDatatype(javaTypeSystem, int.class);
    
    JavaFunctors functors = new JavaFunctors(javaTypeSystem);
    
    IUserFunctionMetadata function = 
      functors.getFunction("cast", new Datatype[] {longDt, integerDt}, null);

    assertNotNull(function);
    
    int intValue = 
      (Integer) ((ISimpleFunction) function).execute(new Object[] {new Long(10)}, null);
    
    assertEquals(10, intValue);
    
    JavaDatatype IntegerDt = new JavaDatatype(javaTypeSystem, Integer.class);
    JavaDatatype charDt = new JavaDatatype(javaTypeSystem, char.class);
    
    function = 
      functors.getFunction("cast", new Datatype[] {IntegerDt, charDt}, null);

    assertNotNull(function);
    
    char charValue = 
      (Character) ((ISimpleFunction) function).execute(new Object[] {97}, null);
    
    assertEquals('a', charValue);
    
    JavaDatatype DoubleDt = new JavaDatatype(javaTypeSystem, Double.class);
    JavaDatatype FloatDt = new JavaDatatype(javaTypeSystem, Float.class);
    
    function = 
      functors.getFunction("cast", new Datatype[] {DoubleDt, FloatDt}, null);

    assertNotNull(function);
    
    Float floatValue = 
      (Float) ((ISimpleFunction) function).execute(new Object[] {new Double(100.0)}, null);
    
    assertEquals(100.0f, floatValue);
  }
  
  public void testIdentifyAndStringConversion() throws MetadataNotFoundException, AmbiguousMetadataException, RuntimeInvocationException 
  {
    JavaDatatype longDt = new JavaDatatype(javaTypeSystem, long.class);
    
    JavaFunctors functors = new JavaFunctors(javaTypeSystem);
    
    IUserFunctionMetadata function = 
      functors.getFunction("cast", new Datatype[] {longDt, longDt}, null);

    assertNotNull(function);
    
    long longValue = 
      (Long) ((ISimpleFunction) function).execute(new Object[] {new Long(10)}, null);
    
    assertEquals(10, longValue);
    
    JavaDatatype stringDt = new JavaDatatype(javaTypeSystem, String.class);
    
    function = 
      functors.getFunction("cast", new Datatype[] {longDt, stringDt}, null);

    assertNotNull(function);
    
    String stringValue = 
      (String) ((ISimpleFunction) function).execute(new Object[] {new Long(10)}, null);
    
    assertEquals("10", stringValue);
  }
  
  public void testReferenceConversion() throws MetadataNotFoundException, AmbiguousMetadataException, RuntimeInvocationException 
  {
    JavaDatatype aDt = new JavaDatatype(javaTypeSystem, A.class);
    JavaDatatype bDt = new JavaDatatype(javaTypeSystem, B.class);
    JavaFunctors functors = new JavaFunctors(javaTypeSystem);
    
    A a = new A();
    
    IUserFunctionMetadata function = 
      functors.getFunction("cast", new Datatype[] {aDt, bDt}, null);

    assertNotNull(function);
    
    B b1 = 
      (B) ((ISimpleFunction) function).execute(new Object[] {a}, null);
    
    assertNotNull(b1);
    
    try
    {
      B b2 = 
        (B) ((ISimpleFunction) function).execute(new Object[] {"foo"}, null);
      
      assertTrue(false);
    }
    catch (RuntimeInvocationException e)
    {
      // nop
    }
    
    function =
      functors.getFunction("cast", new Datatype[] {aDt, Datatype.CHAR}, null);
    
    assertNull(function);
  }
}
