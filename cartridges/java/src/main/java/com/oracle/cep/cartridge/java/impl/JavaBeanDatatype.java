/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaBeanDatatype.java /main/5 2012/08/01 19:00:10 alealves Exp $ */

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
    alealves    Jan 24, 2011 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaBeanDatatype.java /main/5 2012/08/01 19:00:10 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import static com.oracle.cep.cartridge.java.impl.JavaCartridge.JAVA_CARTRIDGE_NAME;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.type.IField;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IType;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

public class JavaBeanDatatype extends JavaDatatype
{
  private static final long serialVersionUID = -7436987242224894876L;
  
  // Cache of all JavaBean-style properties and public fields, where the former
  //  have preference over the latter.
  // Check JavaBeanTest for the details.
  private Map<String, IFieldMetadata> 
    propertiesAndFields;

  public JavaBeanDatatype(JavaTypeSystem javaTypeSystem, Class<?> javaClass)
  {
    super(javaTypeSystem, javaClass);
  }
  
  synchronized void initialize() throws MetadataNotFoundException 
  {
    if (propertiesAndFields == null)
    {
      propertiesAndFields = new HashMap<String, IFieldMetadata>();
      
      IFieldMetadata [] fieldsMetadata =
        super.getFields();
      
      for (IFieldMetadata fieldMetadata : fieldsMetadata) 
      {
        propertiesAndFields.put(fieldMetadata.getName(), fieldMetadata);
      }
      
      // We do not support BeanInfo's today.
      BeanInfo beanInfo;
      try
      {
        beanInfo = Introspector.getBeanInfo(getUnderlyingClass(), Introspector.IGNORE_ALL_BEANINFO);
      } catch (IntrospectionException e)
      {
        throw new MetadataNotFoundException(JAVA_CARTRIDGE_NAME, "<fields>", e);
      }

      PropertyDescriptor [] propDescrs =
        beanInfo.getPropertyDescriptors();

      for (PropertyDescriptor propDescr : propDescrs)
      {
        if (logger.isDebugEnabled())
          logger.debug("Introspected property descriptor = " + propDescr);
        
        // We may be dealing with either LHS or RHS, hence either get or write methods may be present.
        // Also, do not override the class literal T.class
        if ((propDescr.getReadMethod() != null || propDescr.getWriteMethod() != null) && 
            !propDescr.getName().equals(JavaClassLiteralMetadata.TOKEN))
        {
          if (logger.isInfoEnabled())
          {
            if (propertiesAndFields.containsKey(propDescr.getName()))
              logger.info("Property '" + propDescr.getName() + "' of class '" + getUnderlyingClass() 
                  + "' is overriding underlying field.");
          }
          
          propertiesAndFields.put(propDescr.getName(), new JavaBeanPropertyMetadata(propDescr.getName(), 
              propDescr.getReadMethod(), propDescr.getWriteMethod(), propDescr.getPropertyType()));
        }
        else
        {
          // REVIEW Note that we are not doing any checks to see if the property is bound, or constrained.
          
          if (logger.isDebugEnabled())
          {
            logger.debug("No support for indexed properties, hence property " + propDescr.getName() 
                + "' of class '" + getUnderlyingClass() + "' is being discarded.");
          }
        }
      }

      Introspector.flushFromCaches(getUnderlyingClass());
    }
  }

  @Override
  public IFieldMetadata getField(String fieldName)
      throws MetadataNotFoundException
  {
    initialize();
    
    IFieldMetadata metadata =
      propertiesAndFields.get(fieldName);
    
    if (metadata == null)
    {
      throw new MetadataNotFoundException(JAVA_CARTRIDGE_NAME, fieldName);
    } 
    
    return metadata;
  }

  @Override
  public IFieldMetadata[] getFields() throws MetadataNotFoundException
  {
    initialize();

    return propertiesAndFields.values().toArray(new IFieldMetadata[0]);
  }

  class JavaBeanPropertyMetadata implements IFieldMetadata 
  {
    private String name;
    private IType type;
    private boolean isGetStatic;
    private boolean isSetStatic;
    private PropertyInvoker invocable;

    public JavaBeanPropertyMetadata(String propertyName, Method getterMethod, Method setterMethod, 
        Class<?> type)
    {
      name = propertyName;
      
      setType(type);
      
      isGetStatic = getterMethod != null ? Modifier.isStatic(getterMethod.getModifiers()) : false;
      
      isSetStatic = setterMethod != null ? Modifier.isStatic(setterMethod.getModifiers()) : false;
      
      invocable = new PropertyInvoker(getterMethod, setterMethod);
    }

    @Override
    public IField getFieldImplementation()
    {
      return invocable;
    }
    
    void setType(Class<?> clazz)
    {
      type = getTypeSystem().getCQLType(clazz);
    }

    @Override
    public IType getType()
    {
      return type;
    }

    @Override
    public String getName()
    {
      return name;
    }

    @Override
    public String getSchema()
    {
      return JAVA_CARTRIDGE_NAME;
    }

    @Override
    public boolean hasGet()
    {
      return invocable.hasGet();
    }

    @Override
    public boolean hasSet()
    {
      return invocable.hasSet();
    }

    @Override
    public boolean isGetStatic()
    {
      return isGetStatic;
    }

    @Override
    public boolean isSetStatic()
    {
      return isSetStatic;
    }
  }
  
  static class PropertyInvoker implements IField 
  {
    private Method getter;
    private Method setter;

    PropertyInvoker(Method getter, Method setter)
    {
      this.getter = getter;
      this.setter = setter;
    }
    
    boolean hasGet() 
    {
      return getter != null;
    }
    
    boolean hasSet()
    {
      return setter != null;
    }

    @Override
    public Object get(Object obj, ICartridgeContext context) 
    throws RuntimeInvocationException
    {
      try
      {
        CartridgeContextHolderImpl.set(context);
        return getter.invoke(obj, new Object[]{});
        
      } catch (Exception e)
      {
        if (logger.isDebugEnabled())
          logger.debug("Getter method invocation failed for property", e);
        
        throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, getter.getName(), e);
        
      } finally {
        CartridgeContextHolderImpl.set(null);
      }
    }

    @Override
    public void set(Object obj, Object arg, ICartridgeContext context) 
    throws RuntimeInvocationException
    {
      try
      {
        CartridgeContextHolderImpl.set(context);
        setter.invoke(obj, arg);
      } catch (Exception e)
      {
        if (logger.isDebugEnabled())
          logger.debug("Setter method invocation failed", e);
        
        throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, setter.getName(), e);
        
      } finally {
        CartridgeContextHolderImpl.set(null);
      }
    }
  }
}
