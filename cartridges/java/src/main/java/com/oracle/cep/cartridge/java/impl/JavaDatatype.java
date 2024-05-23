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
    alealves    Jun 24, 2010 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaDatatype.java /main/13 2012/06/28 05:37:49 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import com.oracle.cep.cartridge.java.JavaTypeSystem;
import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.type.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.oracle.cep.cartridge.java.impl.JavaCartridge.JAVA_CARTRIDGE_NAME;

/**
 * JavaDatatype is a wrapper of Java class into a CQL data-type.
 * 
 * FIXME eventually we need to remove the reference to Datatype and just use IType.
 * 
 * @author Alex Alves
 */
public class JavaDatatype extends Datatype implements IComplexType
{
  private static final long serialVersionUID = 3527174714833530198L;
  
  protected static final Log logger = 
    LogFactory.getLog("JavaCartridge");
  
  private final Class<?> clazz;

  private final JavaTypeSystem typeSystem;
  private final MemberFinder finder = new MemberFinder();
  
  public JavaDatatype(JavaTypeSystem javaTypeSystem, Class<?> javaClass) 
  {
    super(javaClass.getName(), javaClass);
    typeSystem = javaTypeSystem;
    clazz = javaClass;
  }
  
  Class<?> getUnderlyingClass()
  {
    return clazz;
  }
  
  @Override
  public IMethodMetadata getMethod(String memberName, IType... parameters) 
    throws MetadataNotFoundException
  {
    Class<?> [] paramTypes = null;
    
    try
    {
      paramTypes = typeSystem.getJavaTypes(parameters);
      return new JavaMemberMetadata(memberName, paramTypes);
    } catch (MetadataNotFoundException e) {
      throw e;
    } catch (Exception e)
    {
      String signature = MemberFinder.formatMethodName(memberName, paramTypes);
      throw new MetadataNotFoundException(JAVA_CARTRIDGE_NAME, signature, e);
    }
  }

  @Override
  public IConstructorMetadata getConstructor(IType... parameters) 
  throws MetadataNotFoundException
  {
    Class<?> [] paramTypes = null;
    
    try
    {
      paramTypes = typeSystem.getJavaTypes(parameters);
      return new JavaMemberMetadata(paramTypes);
    } catch (MetadataNotFoundException e) {
      throw e;
    } catch (Exception e)
    {
      String signature = MemberFinder.formatMethodName(clazz.getName(), paramTypes);
      throw new MetadataNotFoundException(JAVA_CARTRIDGE_NAME, signature, e);
    }
  }

  @Override
  public IFieldMetadata getField(String fieldName) 
    throws MetadataNotFoundException
  {
    // Java does not allow two fields with the same name, even if one is static and the other is not.
    // Therefore, we can return the first match. 
    if (fieldName.equals(JavaClassLiteralMetadata.TOKEN))
    {
      // We support the class literal T.class as a static field metadata.
      // Therefore we need to add it to all JavaDatatypes explicitly.
      return new JavaClassLiteralMetadata(clazz);
    }
    else
    {
      try
      {
        return new JavaMemberMetadata(fieldName);
      } catch (MetadataNotFoundException e) {
        throw e;
      } catch (Exception e)
      {
        throw new MetadataNotFoundException(JAVA_CARTRIDGE_NAME, fieldName, e);
      }
    }
  }
  
  /**
   * We don't cache the metadata for two reasons:
   * - the underlying Java entity is already being cached (classes' field, method, etc)
   * - the metadata is short-lived and only used during compilation, hence preferable to save on
   *  memory.
   */
  @Override
  public IFieldMetadata [] getFields() throws MetadataNotFoundException
  {
    final Field[] fields = getCandidateFields();
    final IFieldMetadata [] metadataArray = new IFieldMetadata [fields.length + 1];
    
    for (int i = 0; i < fields.length; i++)
    {
      metadataArray[i] = new 
        JavaMemberMetadata(fields[i]);
    }
    
    // We support the class literal T.class as a static field metadata.
    // Therefore we need to add it to all JavaDatatypes explicitly.
    metadataArray[fields.length] = new JavaClassLiteralMetadata(clazz);
    
    return metadataArray;
  }

  private Field[] getCandidateFields()
  {
    // getFields() returns all fields, including superclasses and interfaces, in a unordered form.
    //  Instead we don't want duplicates, and we want starting at the sub-class, the interfaces, and then
    //  the super-class.
    Map<String, Field> uniqueOrderedFields = new HashMap<String, Field>();
    
    List<Class<?>> candidates = new LinkedList<Class<?>>(); 
    candidates.add(clazz);
    
    while (!candidates.isEmpty())
    {
      // Remove from front.
      Class<?> target = candidates.remove(0);
      
      // Returns all declared fields of a class, including private ones.
      Field [] declFields = target.getDeclaredFields();
      
      for (Field declField : declFields)
      {
        if (Modifier.isPublic(declField.getModifiers()) &&
            !uniqueOrderedFields.containsKey(declField.getName()))
        {
          uniqueOrderedFields.put(declField.getName(), declField);
        }
        else
        {
          if (logger.isDebugEnabled())
            logger.debug("Ignoring field '" + declField.getName() + "' of class '" 
                + declField.getClass().getName() + ", in favor of sub-class.");
        }
      }
      

      // Add to end, first the interfaces, then the super class.
      candidates.addAll(Arrays.asList(target.getInterfaces()));
      if (target.getSuperclass() != null)
        candidates.add(target.getSuperclass());
    }
    
    Field [] fields = uniqueOrderedFields.values().toArray(new Field[0]);
    return fields;
  }
  
  @Override
  public boolean isAssignableFrom(IType fromDatatype)
  {
    // If both are Java types, then use Java's isAssignableFrom().
    if (fromDatatype instanceof JavaDatatype)
    {
      JavaDatatype fromJavaType = (JavaDatatype) fromDatatype;
      return this.clazz.isAssignableFrom(fromJavaType.clazz);
    } 
    else 
    {
      return super.isAssignableFrom(fromDatatype);
    }
  }
  
  static class MethodInvoker implements IMethod 
  {
    private final Method method;
    
    MethodInvoker(Method method)
    {
      this.method = method;
    }

    @Override
    public Object invoke(Object obj, Object[] args, ICartridgeContext context)
        throws RuntimeInvocationException
    {
      try
      {
        CartridgeContextHolderImpl.set(context);
        return method.invoke(obj, args);
      } catch (Exception e)
      {
        if (logger.isDebugEnabled())
          logger.debug("Method invocation failed", e);
        
        throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, method.getName(), e);
      } finally {
        CartridgeContextHolderImpl.set(null);
      }
    }
  }
  
  static class FieldInvoker implements IField 
  {
    private final Field field;
    
    FieldInvoker(Field field)
    {
      this.field = field;
    }

    @Override
    public Object get(Object obj, ICartridgeContext context) 
    throws RuntimeInvocationException
    {
      try
      {
        CartridgeContextHolderImpl.set(context);
        return field.get(obj);
      } catch (Exception e)
      {
        if (logger.isDebugEnabled())
          logger.debug("Field get invocation failed", e);
        
        throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, field.getName(), e);
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
        field.set(obj, arg);
      } catch (Exception e)
      {
        if (logger.isDebugEnabled())
          logger.debug("Field set invocation failed", e);
        
        throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, field.getName(), e);
      } finally {
        CartridgeContextHolderImpl.set(null);
      }
    }
  }
  
  class ConstructorInvoker implements IConstructor 
  {
    private Constructor<?> constructor;
    
    ConstructorInvoker(Constructor<?> constructor)
    {
      this.constructor = constructor;
    }

    @Override
    public Object instantiate(Object[] args, ICartridgeContext context) 
    throws RuntimeInvocationException
    {
      try
      {
        CartridgeContextHolderImpl.set(context);
        return constructor.newInstance(args);
      } catch (Exception e)
      {
        if (logger.isDebugEnabled())
          logger.debug("Constructor invocation failed", e);
        
        throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, constructor.getName(), e);
      } finally {
        CartridgeContextHolderImpl.set(null);
      }
    }
  }
  
  class JavaMemberMetadata implements IMethodMetadata, IFieldMetadata, 
    IConstructorMetadata 
  {
    private Method method;
    private Constructor<?> constructor;
    private Field field;
    private String name;
    private IType returnType;
    private IType[] paramTypes;
    private IRuntimeInvocable invocable;
    private boolean isStatic;
    private IType rawReturnType;
    
    /**
     * Method access
     * @throws MetadataNotFoundException 
     * @throws ClassNotFoundException 
     */
    public JavaMemberMetadata(String methodName, Class<?>[] paramTypes) 
      throws SecurityException, NoSuchMethodException, MetadataNotFoundException, ClassNotFoundException
    {
      this.name = methodName;
      loadMethod(clazz, methodName, paramTypes);
      
      setReturnType(method.getReturnType());
      setParamTypes(method.getParameterTypes());
      setImplementation(new MethodInvoker(method));
      setRawReturnType(method.getGenericReturnType());
      isStatic = Modifier.isStatic(method.getModifiers());
    }
    
    /**
     * Constructor invocation
     * @throws MetadataNotFoundException 
     */
    public JavaMemberMetadata(Class<?>[] paramTypes) 
      throws SecurityException, NoSuchMethodException, ClassNotFoundException, MetadataNotFoundException
    {
      this.name = clazz.getSimpleName();
      loadConstructor(clazz, paramTypes);
      
      setReturnType(clazz);
      setParamTypes(constructor.getParameterTypes());
      setImplementation(new ConstructorInvoker(constructor));
    }

    /**
     * Field access
     * @throws MetadataNotFoundException 
     */
    public JavaMemberMetadata(String fieldName) throws SecurityException, NoSuchFieldException, 
      MetadataNotFoundException
    {
      this.name = fieldName;
      loadField(clazz, fieldName);
      
      setReturnType(field.getType());
      setParamTypes(null);
      setImplementation(new FieldInvoker(field));
      isStatic = Modifier.isStatic(field.getModifiers());
    }

    public JavaMemberMetadata(Field field) throws MetadataNotFoundException
    {
      this.field = field;
      this.name = field.getName();
      
      setReturnType(field.getType());
      setParamTypes(null);
      setImplementation(new FieldInvoker(field));
      isStatic = Modifier.isStatic(field.getModifiers());
    }

    private void loadField(Class<?> clazz, String fieldName) 
    throws SecurityException, NoSuchFieldException
    {
      field = 
        clazz.getField(fieldName);
    }

    private void loadConstructor(Class<?> clazz, Class<?>[] paramTypes) 
    throws SecurityException, NoSuchMethodException
    {
      constructor =
        finder.findConstructor(clazz, paramTypes);
    }

    private void loadMethod(Class<?> clazz, String memberName, Class<?>[] paramTypes) 
      throws NoSuchMethodException, ClassNotFoundException
    {
      method =
        finder.findMethod(clazz, memberName, paramTypes);
    }

     void setImplementation(IRuntimeInvocable function) 
    {
      this.invocable = function;
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

    void setParamTypes(Class<?> [] paramClasses) throws MetadataNotFoundException 
    {
      if (paramClasses != null) 
      {
        this.paramTypes = typeSystem.getCQLTypes(paramClasses);
      }
    }

    @Override
    public IType getReturnType()
    {
      return returnType;
    }
    
    void setReturnType(Class<?> clazz) throws MetadataNotFoundException 
    {
      returnType = typeSystem.getCQLType(clazz);
    }
    
    void setRawReturnType(java.lang.reflect.Type type)
      throws MetadataNotFoundException
    {
      try
      {
        if(type instanceof java.lang.reflect.ParameterizedType)
        {
          java.lang.reflect.Type innerType = 
            ((java.lang.reflect.ParameterizedType)type).getActualTypeArguments()[0]; 
          String typeName = ((Class<?>)innerType).getName();
          this.rawReturnType = typeSystem.getCQLType(Class.forName(typeName));
        }
      }
      catch(Exception e)
      {//eat the exception
      }      
    }

    @Override
    public IType[] getParameterTypes()
    {
      return paramTypes;
    }
    
    @Override
    public boolean isStatic()
    {
      return isStatic;
    }

    @Override
    public boolean isGetStatic()
    {
      return isStatic;
    }

    @Override
    public IType getType()
    {
      return returnType; 
    }

    @Override
    public IType getInstanceType()
    {
      return returnType;
    }
    
    public IMethod getMethodImplementation() 
    {
      return (IMethod) invocable;
    }

    @Override
    public IField getFieldImplementation()
    {
      return (IField) invocable;
    }

    @Override
    public IConstructor getConstructorImplementation()
    {
      return (IConstructor) invocable;
    }

    @Override
    public boolean hasGet()
    {
      return true; // always true for class fields (as opposed to properties)
    }

    @Override
    public boolean hasSet()
    {
      return true; // always true for class fields (as opposed to properties)
    }

    @Override
    public boolean isSetStatic()
    {
      return isStatic;
    }
  }

  /**
   * Java supports the idea of class literals. These are expressions such as <code>T.class</code>.
   * For example: java.lang.String.class. Note that this is different than invoking "string".getClass().
   * The former is a static reference that returns Class<T>, where as the latter is non-static.
   * Also, note that it is different than invoking T.class.getClass(), which returns Class instead of Class<T>.
   * 
   * To support class literals, we make use an internal static field meta-data implementation.
   * 
   */
  class JavaClassLiteralMetadata implements IFieldMetadata 
  {
    // In Java, the literal <code>class</code> is considered a token.
    static final String TOKEN = "class";
    
    private final String name = TOKEN;
    private final IType returnType;

    JavaClassLiteralMetadata(Class<?> clazz)
    {
      returnType = typeSystem.getCQLType(clazz);
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
    public IType getType()
    {
      return returnType;
    }

    @Override
    public boolean isGetStatic()
    {
      return true;
    }

    @Override
    public boolean isSetStatic()
    {
      return false;
    }

    @Override
    public boolean hasSet()
    {
      return false;
    }

    @Override
    public boolean hasGet()
    {
      return true;
    }

    @Override
    public IField getFieldImplementation()
    {
      return new IField() {
        @Override
        public void set(Object obj, Object arg, ICartridgeContext context)
            throws RuntimeInvocationException
        {
          throw new RuntimeInvocationException(JAVA_CARTRIDGE_NAME, name);
        }

        @Override
        public Object get(Object obj, ICartridgeContext context)
            throws RuntimeInvocationException
        {
          return returnType;
        }};
    }
  }
  
  protected JavaTypeSystem getTypeSystem()
  {
    return typeSystem;
  }

  @Override
  public String toString()
  {
    // We include the '@java' to diferantiate from CQL native types
    return clazz.getName() + "@java";
  }
  
  @Override
  public int hashCode()
  {
    return clazz == null ? 0 : clazz.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
      return true;
    
    // A Java-cartridge datatype is only equal with other Java-cartridge type.
    // In other words, a Long@java is considered different than a Long@jdbc.
    //
    if (!(obj instanceof JavaDatatype))
      return false;
    
    return clazz.equals(((JavaDatatype) obj).clazz);
  }

  MemberFinder getFinder()
  {
    return finder;
  }

}
