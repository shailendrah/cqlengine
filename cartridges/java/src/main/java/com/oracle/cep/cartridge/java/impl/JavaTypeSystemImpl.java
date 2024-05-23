package com.oracle.cep.cartridge.java.impl;

import java.util.Hashtable;
import java.util.Map;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oracle.cep.cartridge.java.JavaCartridgeClassLoader;
import com.oracle.cep.cartridge.java.JavaTypeSystem;
import com.oracle.cep.cartridge.java.MultipleClassesFoundException;

/**
 * This class represents the Java type system from the perspective of the CQL 
 *  meta-type model.
 *  
 * @author Alex Alves
 *
 */
public class JavaTypeSystemImpl implements ITypeLocator, JavaTypeSystem
{
  private static final Log logger = 
    LogFactory.getLog(JavaCartridge.JAVA_CARTRIDGE_LOGGER);
  
  private static Datatype [] missingPrimitives = 
    {BytePrimitive.TYPE, CharPrimitive.TYPE, ShortPrimitive.TYPE};
  
  private static Datatype [] nonpublicNatives =
    {Datatype.VOID};
  
  private static Map<Class<?>, Datatype> specialTypeMap = new Hashtable<Class<?>, Datatype>();
  static 
  {
    specialTypeMap.put(java.util.Date.class, Datatype.TIMESTAMP);
    specialTypeMap.put(java.sql.Date.class, Datatype.TIMESTAMP);
    specialTypeMap.put(java.sql.Time.class, Datatype.TIMESTAMP);
    specialTypeMap.put(java.sql.Timestamp.class, Datatype.TIMESTAMP);
    specialTypeMap.put(java.math.BigDecimal.class, Datatype.BIGDECIMAL);
    specialTypeMap.put(char[].class, Datatype.CHAR);
  }

  // By default, just use the Java cartridge's bundle's resources
  private JavaCartridgeClassLoader classLoader = 
    new DirectClassLoader();

  @Override
  public IType getType(String extensibleTypeName, ICartridgeContext context) 
    throws MetadataNotFoundException, AmbiguousMetadataException
  {
    // REVIEW currently, we are not using the context at this scope. We, for example, use the application name
    //  to determine the class-space to search...
    
    String appName = (context != null ? context.getApplicationName() : null);
    
    IType found = findExtensibleSimpleType(extensibleTypeName);
    
    if (found == null) 
      found = findExtensibleComplexType(extensibleTypeName, appName);
    
    return found;
  }
  
  public IArrayType getArrayType(String componentExtensibleTypeName, ICartridgeContext context) 
    throws MetadataNotFoundException, AmbiguousMetadataException
  {
    // REVIEW currently, we are not using the context at this scope. We, for example, use the application name
    //  to determine the class-space to search...
    
    String appName = (context != null ? context.getApplicationName() : null);
    
    return findExtensibleArrayType(componentExtensibleTypeName, appName);
  }
  
  public IType getCQLType(Class<?> sourceClass) 
  {
    // Check if class represents one of the native CQL types
    IType found = findNativeType(sourceClass);
    
    // If not, then it must be an extensible type
    if (found == null) 
    {
      if (sourceClass.isArray())
        found = createExtensibleArrayType(sourceClass.getComponentType());
      else 
      {
        found = findExtensibleSimpleType(sourceClass.getName());
        
        if (found == null)
          found = createExtensibleComplexType(sourceClass);
      }
    }
      
    return found;
  }
  
  public IType [] getCQLTypes(Class<?> [] clazzes) 
  {
    IType [] datatypes = new IType[clazzes.length];
    
    for (int i = 0; i < clazzes.length; i++)
    {
      datatypes[i] = getCQLType(clazzes[i]);
    }
    
    return datatypes;
  }

  public Class<?> getJavaType(IType cqlType)
  {
    // The Java type-system is special, because the CQL engine is implemented using it.
    // Therefore, as we know all JavaDatatypes extend CQL's datatype, we can cast to it,
    //  and retrieve the underlying implementation class.
    // This approach would not work for other type systems, like Hadoop's for instance.
    //
    return ((Datatype) cqlType).getImplementationType();
  }

  public Class<?> [] getJavaTypes(IType [] datatype) 
  {
    Class<?> [] clazzes = new Class[datatype.length];
    
    for (int i = 0; i < datatype.length; i++)
    {
      Class<?> clazz = getJavaType(datatype[i]);
      clazzes[i] = clazz;
    }
    
    return clazzes;
  }
  
  private IType findExtensibleSimpleType(String typeName) 
  {
    IType found = null;
    
    for (Datatype nativeType : missingPrimitives)
    {
      if (nativeType.getImplementationType().getName().equals(typeName))
      {
        found = nativeType;
        break;
      }
    }

    return found;
  }
  
  IType findAssignableNativeType(Class<?> sourceClass) 
  {
    IType found = null;
    
    // First do it for the 'special' date, time, interval, and XML types
    found = specialTypeMap.get(sourceClass);
    
    if (found == null)
      for (Datatype nativeType : Datatype.getPublicTypes())
      {
        if (MemberFinder.isJavaAssignable(nativeType.getImplementationType(),sourceClass))
        {
          found = nativeType;
          break;
        }
      }
    
    return found;
  }
  
  private IType findNativeType(Class<?> sourceClass) 
  {
    IType found = null;
    
    // 'getImplementationType()' only works for the Java type-system, and even in this case 
    // there are exceptions, like the case of XMLTYPE, INTERVAL, TIMESTAMP, etc.
    
    for (Datatype nativeType : Datatype.getPublicTypes())
    {
      if (nativeType.getImplementationType().equals(sourceClass))
      {
        found = nativeType;
        break;
      }
    }
    
    if (found == null)
      for (Datatype nativeType : nonpublicNatives)
      {
        if (nativeType.getImplementationType().equals(sourceClass))
        {
          found = nativeType;
          break;
        }
      }
    
    if (found == null) 
    {
      // REVIEW should we do any special handling for SQLXML and java.sql.Date?
//    if (sourceClass. == SQLXML.class) {
//      found = Datatype.XMLTYPE;
//    } else if (typeName.equals(Date.class.getName()) || typeName.equals(java.sql.Date.class.getName() )
//        || typeName.equals(java.sql.Timestamp.class.getName()) ){
//      found = Datatype.TIMESTAMP;
    }

    return found;
  }
  
  private IArrayType findExtensibleArrayType(String javaTypeName, String applicationName) 
    throws MetadataNotFoundException, AmbiguousMetadataException 
  {
    Class<?> javaClass = null;
    
    try
    {
      // It is possible that component is a primitive, hence we can't use Java's loadClass.
      javaClass =
        ClassUtils.resolvePrimitiveClassName(javaTypeName);
      
      if (javaClass == null)
        javaClass = classLoader.loadClass(javaTypeName, applicationName);
      
    } catch (ClassNotFoundException e)
    {
      throw new MetadataNotFoundException("Java", javaTypeName, e);
    } catch (MultipleClassesFoundException e)
    {
      throw new AmbiguousMetadataException("Java", javaTypeName, e.getMessage(), e);
    } 
    
    return createExtensibleArrayType(javaClass);
  }

  private IArrayType createExtensibleArrayType(Class<?> javaClass)
  {
    return new JavaArrayDatatype(this, javaClass);
  }
  
  private IComplexType findExtensibleComplexType(String javaTypeName, String applicationName) 
  throws MetadataNotFoundException, AmbiguousMetadataException
  {
    Class<?> javaClass = null;
    
    try {
      javaClass = 
        classLoader.loadClass(javaTypeName, applicationName);
    } catch (ClassNotFoundException e)
    {
      throw new MetadataNotFoundException("Java", javaTypeName, e);
    } catch (MultipleClassesFoundException e)
    {
      throw new AmbiguousMetadataException("Java", javaTypeName, e.getMessage(), e);
    }
    
    return createExtensibleComplexType(javaClass);
  }

  @SuppressWarnings("unchecked")
  private IComplexType createExtensibleComplexType(Class<?> javaClass)
  {
    IComplexType type = null;
    
    if (Iterable.class.isAssignableFrom(javaClass))
    {
      Class<? extends Iterable<?>> iterableClass = 
        (Class<? extends Iterable<?>>) javaClass;
      type = new JavaIterableDatatype(this, iterableClass);
    }
    else 
      type = new JavaBeanDatatype(this, javaClass);

    return type;
  }
  
  public void setJavaCartridgeClassLoader(JavaCartridgeClassLoader loader) 
  {
    if (logger.isDebugEnabled())
      logger.debug("Setting cartridge class loader to = " + loader);
    
    this.classLoader = loader;
  }
  
  public JavaCartridgeClassLoader getJavaCartridgeClassLoader() 
  {
    return this.classLoader;
  }
  
}
