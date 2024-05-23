/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/JavaBeanTest.java /main/4 2013/05/23 18:54:09 alealves Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    Mar 8, 2011 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/JavaBeanTest.java /main/4 2013/05/23 18:54:09 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.type.IFieldMetadata;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

/**
 * Semantic:
 *  - Implicit 'class' property.
 *  - Property does not need to have both getter and setter.
 *  - isProp() can be used as getter for a boolean property.
 *  - properties have precedence over fields.
 *  - inheritance supported.
 *  - no support for bound, constrained, and indexed properties.
 *  
 * @author Alex Alves
 *
 */
public class JavaBeanTest extends TestCase
{
  private JavaTypeSystem javaTypeSystem;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    
    javaTypeSystem = new JavaTypeSystemImpl();
  }
  
  public void testProperties() throws MetadataNotFoundException, RuntimeInvocationException 
  {
    JavaBeanDatatype jbdt = new JavaBeanDatatype(javaTypeSystem, FooClass.class);
    
    IFieldMetadata[] fields =
      jbdt.getFields();
    
    assertEquals(7, fields.length);
    // Map Entry {fieldName, [hasSet,hasGet]}
    HashMap<String,Object> expectedFields = new HashMap<String,Object>();
    expectedFields.put("myProp1", new boolean[]{true,true});  
    expectedFields.put("myProp2", new boolean[]{true,true});  
    expectedFields.put("myProp3", new boolean[]{false,true});  
    expectedFields.put("myProp6", new boolean[]{true,true});  
    expectedFields.put("myProp7", new boolean[]{true,false});  
    expectedFields.put("myProp8", new boolean[]{false,true});  
    expectedFields.put("class",   new boolean[]{false,true});  
 
    boolean allFieldsMatched = true;
    boolean allSetterMatched = true;
    boolean allGetterMatched = true;
    
    for(int i=0; i<fields.length; i++)
    {
      allFieldsMatched = allFieldsMatched && expectedFields.containsKey(fields[i].getName());
      boolean[] fmd = (boolean[])(expectedFields.get(fields[i].getName()));
      allSetterMatched = allSetterMatched && (fields[i].hasSet() == fmd[0]);
      allGetterMatched = allGetterMatched && (fields[i].hasGet() == fmd[1]);
    }
   
    assertTrue(allFieldsMatched);
    assertTrue(allGetterMatched);
    assertTrue(allSetterMatched);
  }
  
  public void testOverloadingProperties() throws MetadataNotFoundException, RuntimeInvocationException 
  {
    JavaBeanDatatype jbdt = new JavaBeanDatatype(javaTypeSystem, FooClass.class);
    
    IFieldMetadata[] fields =
      jbdt.getFields();
    
    FooClass foo = new FooClass();
    for(int i=0; i<fields.length;i++)
    {
      if(fields[i].getName().equals("myProp6"))
         fields[i].getFieldImplementation().set(foo, "set!", null);
    }
    assertEquals("set!", foo.myProp5);
  }
  
  public void testOverridingProperties() throws MetadataNotFoundException
  {
    JavaBeanDatatype jbdt = new JavaBeanDatatype(javaTypeSystem, BarClass.class);
    
    IFieldMetadata[] fields =
      jbdt.getFields();
    
    assertEquals(5, fields.length);
    
    HashSet<String> expectedFields = new HashSet<String>();
    expectedFields.add("childProp1"); 
    expectedFields.add("childProp2"); 
    expectedFields.add("class"); 
    expectedFields.add("parentProp1"); 
    expectedFields.add("parentProp2"); 
    for(int i=0; i<fields.length; i++)
      assertTrue(expectedFields.contains(fields[i].getName()));
  }
  
  public void testProperty() throws MetadataNotFoundException, RuntimeInvocationException
  {
    JavaBeanDatatype jbdt = new JavaBeanDatatype(javaTypeSystem, BarClass.class);
    
    BarClass bar = new BarClass();
    
    IFieldMetadata metadata =
      jbdt.getField("parentProp1");
    
    metadata.getFieldImplementation().set(bar, "set!", null);
    assertEquals("set!", bar.getParentProp1());
    
    metadata =
      jbdt.getField("childProp2");
    
    bar.childProp2 = "set!";
    metadata.getFieldImplementation().set(bar, "set!", null);
    assertEquals("set!", metadata.getFieldImplementation().get(bar, null));
  }
  
  public void testStaticProperty() throws MetadataNotFoundException, RuntimeInvocationException
  {
    JavaBeanDatatype jbdt = new JavaBeanDatatype(javaTypeSystem, StaticClass.class);
    
    MetadataNotFoundException e = null;
    try 
    {
      IFieldMetadata metadata =
        jbdt.getField("myProp1");
      
      assertNull(metadata);
    } 
    catch (MetadataNotFoundException e1) 
    {
      e = e1;
    }
    
    assertNotNull(e);
    
    IFieldMetadata metadata =
      jbdt.getField("staticProp");
    
    assertTrue(metadata.isSetStatic());
    
    metadata.getFieldImplementation().set(null, "set!", null);
    assertEquals("set!", metadata.getFieldImplementation().get(null, null));
  }
  
  public void testFieldInheritance() throws MetadataNotFoundException, RuntimeInvocationException 
  {
    JavaDatatype jbdt = new JavaDatatype(javaTypeSystem, A.class);
    
    IFieldMetadata[] fields =
      jbdt.getFields();
    
    assertEquals(3, fields.length);
    
    A a = new A();
    
    assertEquals("attr2", fields[0].getName());
    assertEquals(2, fields[0].getFieldImplementation().get(a, null));
    
    assertEquals("attr1", fields[1].getName());
    assertEquals(1, fields[1].getFieldImplementation().get(a, null));
    
    assertEquals("class", fields[2].getName());
    assertEquals(jbdt, fields[2].getFieldImplementation().get(null, null));
   
  }
}
