/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/CartridgeContextDelegate.java /main/8 2011/03/17 10:20:06 alealves Exp $ */

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
      alealves  06/24/10 - Adde getFields() to get all fields of a complex type
    alealves    Oct 1, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/CartridgeContextDelegate.java /main/7 2010/06/29 09:16:03 udeshmuk Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.cartridge;

import java.util.Collections;
import java.util.Map;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IConstructor;
import oracle.cep.extensibility.type.IConstructorMetadata;
import oracle.cep.extensibility.type.IField;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IMethod;
import oracle.cep.extensibility.type.IMethodMetadata;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.metadata.MetadataException;

/**
 * This delegate sets the context into ICartridge so that it can be used during compilation-time.
 * 
 * @author Alex Alves
 *
 */
public class CartridgeContextDelegate implements ICartridge
{
  // This is the set of properties provided by the cartridge instance. 
  // It must be merged with the built-in properties coming from the engine (e.g. application name).
  private final Map<String, Object> properties;

  private IUserFunctionMetadataLocator functionLocator;

  private IndexInfoLocatorDelegate indexLocator;

  private TypeLocatorDelegate typeLocator;
  
  private class UserFunctionMetadataDelegate implements IUserFunctionMetadataLocator 
  {
    private final IUserFunctionMetadataLocator delegate;
    
    UserFunctionMetadataDelegate(IUserFunctionMetadataLocator locator) 
    {
      assert locator != null : "missing type locator";
      this.delegate = locator;
    }
    
    @Override
    public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes, ICartridgeContext context)
        throws MetadataNotFoundException, AmbiguousMetadataException
    {
      context.getProperties().putAll(properties);
      
      IUserFunctionMetadata ret = delegate.getFunction(name, paramTypes, context);
      if (ret instanceof ISimpleFunctionMetadata)
        ret = new SimpleFunctionMetadataDelegate((ISimpleFunctionMetadata) ret);
      
      return ret;
    }

    public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context)
      throws MetadataNotFoundException
    {
        List<IUserFunctionMetadata> retVal = delegate.getAllFunctions(context);
        for (IUserFunctionMetadata function : retVal)
        {
            if (function instanceof ISimpleFunctionMetadata)
            {
                retVal.remove(function);
                retVal.add(new SimpleFunctionMetadataDelegate((ISimpleFunctionMetadata) function));
            }
        }
        return retVal;
    }
  }
  
  private class IndexInfoLocatorDelegate implements IIndexInfoLocator
  {
    private final IIndexInfoLocator delegate;
    
    IndexInfoLocatorDelegate(IIndexInfoLocator locator) 
    {
      assert locator != null : "missing index info locator";
      this.delegate = locator;
    }
    
    @Override
    public IIndexInfo[] getIndexInfo(IUserFunctionMetadata operation,
        int paramPosition, ICartridgeContext context)
    {
      context.getProperties().putAll(properties);
      
      // When invoking back user-code (e.g. cartridge provider), we should use
      //  original delegate, rather than wrapper.
      if (operation instanceof SimpleFunctionMetadataDelegate) 
        operation = ((SimpleFunctionMetadataDelegate) operation).delegate;
      
      IIndexInfo[] ret = delegate.getIndexInfo(operation, paramPosition, context);
      return ret;
    }
  }
  
  private class TypeLocatorDelegate implements ITypeLocator
  {
    private final ITypeLocator delegate;
    
    TypeLocatorDelegate(ITypeLocator locator)
    {
      assert locator != null : "missing type locator";
      this.delegate = locator;
    }
    
    @Override
    public IArrayType getArrayType(String componentExtensibleTypeName, ICartridgeContext context)
        throws MetadataNotFoundException, AmbiguousMetadataException
    {
      context.getProperties().putAll(properties);
      
      IArrayType ret = delegate.getArrayType(componentExtensibleTypeName, context);
      return ret;    
    }

    @Override
    public IType getType(String extensibleTypeName, ICartridgeContext context)
        throws MetadataNotFoundException, AmbiguousMetadataException
    {
      context.getProperties().putAll(properties);
      
      IType ret = delegate.getType(extensibleTypeName, context);
      
      if (ret instanceof IComplexType)
        ret = new ComplexTypeDelegate((IComplexType) ret);
      
      return ret;
    }
  }
  
  private class SimpleFunctionMetadataDelegate implements ISimpleFunctionMetadata 
  {
    final ISimpleFunctionMetadata delegate;
    
    public SimpleFunctionMetadataDelegate(ISimpleFunctionMetadata delegate)
    {
      this.delegate = delegate;
    }

    @Override
    public int getNumParams()
    {
      return delegate.getNumParams();
    }

    @Override
    public IAttribute getParam(int pos) throws MetadataException
    {
      return delegate.getParam(pos);
    }

    @Override
    public Datatype getReturnType()
    {
      return delegate.getReturnType();
    }

    @Override
    public String getName()
    {
      return delegate.getName();
    }

    @Override
    public String getSchema()
    {
      return delegate.getSchema();
    }

    @Override
    public UserDefinedFunction getImplClass()
    {
      UserDefinedFunction func = delegate.getImplClass();
      if (func instanceof ISimpleFunction)
        func = new SimpleFunctionDelegate((ISimpleFunction) delegate.getImplClass());
      
      return func;
    }
  }
  
  private class SimpleFunctionDelegate implements ISimpleFunction
  {
    final ISimpleFunction delegate;

    public SimpleFunctionDelegate(ISimpleFunction implClass)
    {
      this.delegate = implClass;
    }

    @Override
    public Object execute(Object[] args, ICartridgeContext context)
        throws RuntimeInvocationException
    {
      context.getProperties().putAll(properties);
      
      return delegate.execute(args, context);
    }
  }
  
  private class ComplexTypeDelegate
    extends Datatype
    implements IComplexType
  {
    private static final long serialVersionUID = -2225401574220328712L;
    
    private final IComplexType delegate;

    ComplexTypeDelegate(IComplexType type) 
    {
      super(type.name(), type.getClass());
      this.delegate = type;
    }

    @Override
    public IConstructorMetadata getConstructor(IType... parameters)
        throws MetadataNotFoundException
    {
      return new ConstructorMetadataDelegate(delegate.getConstructor(parameters));
    }

    @Override
    public IFieldMetadata getField(String fieldName)
        throws MetadataNotFoundException
    {
      IFieldMetadata fieldMetadata = 
        delegate.getField(fieldName);
      
      if (fieldMetadata != null && fieldMetadata.isGetStatic())
        fieldMetadata = new FieldMetadataDelegate(fieldMetadata);
      
      return fieldMetadata;
    }

    @Override
    public IFieldMetadata[] getFields() throws MetadataNotFoundException
    {
      // Need only to use context and hence delegate if field is static
      IFieldMetadata [] fields = 
        delegate.getFields();

      final IFieldMetadata [] delegatedFields = 
        new IFieldMetadata [fields.length];
      
      for (int i = 0; i < fields.length; i++)
      {
        if (fields[i].isGetStatic())
          delegatedFields[i] = new FieldMetadataDelegate(fields[i]);
        else
          delegatedFields[i] = fields[i];
      }

      return delegatedFields;
    }

    @Override
    public IMethodMetadata getMethod(String methodName, IType... parameters)
        throws MetadataNotFoundException
    {
      IMethodMetadata methodMetadata =
        delegate.getMethod(methodName, parameters);
      
      if (methodMetadata != null && methodMetadata.isStatic())
        methodMetadata = new MethodMetadataDelegate(methodMetadata);
      
      return methodMetadata;
    }

    @Override
    public Kind getKind()
    {
      return delegate.getKind();
    }

    @Override
    public int getLength()
    {
      return delegate.getLength();
    }

    @Override
    public int getPrecision()
    {
      return delegate.getPrecision();
    }

    @Override
    public boolean isAssignableFrom(IType fromDatatype)
    {
      return delegate.isAssignableFrom(fromDatatype);
    }

    @Override
    public boolean isCaseSensitive()
    {
      return delegate.isCaseSensitive();
    }

    @Override
    public String name()
    {
      return delegate.name();
    }

    @Override
    public short getNullable()
    {
      if (delegate instanceof Datatype)
        return ((Datatype) delegate).getNullable();
      else
        return super.getNullable();
    }

    @Override
    public String getAttrValClass()
      throws UnsupportedOperationException
    {
      if (delegate instanceof Datatype)
        return ((Datatype) delegate).getAttrValClass();
      else
        return super.getAttrValClass();
    }

    @Override
    public int getSqlType()
      throws UnsupportedOperationException
    {
      if (delegate instanceof Datatype)
        return ((Datatype) delegate).getSqlType();
      else
        return super.getSqlType();
    }
  }
  
  private class ConstructorMetadataDelegate implements IConstructorMetadata 
  {
    private IConstructorMetadata delegate;

    ConstructorMetadataDelegate(IConstructorMetadata metadata) 
    {
      this.delegate = metadata;
    }
    
    @Override
    public IConstructor getConstructorImplementation()
    {
      return new ConstructorDelegate(delegate.getConstructorImplementation());
    }

    @Override
    public IType getInstanceType()
    {
      return delegate.getInstanceType();
    }

    @Override
    public IType[] getParameterTypes()
    {
      return delegate.getParameterTypes();
    }

    @Override
    public String getName()
    {
      return delegate.getName();
    }

    @Override
    public String getSchema()
    {
      return delegate.getSchema();
    }
  }
  
  private class ConstructorDelegate implements IConstructor
  {
    private IConstructor delegate;

    ConstructorDelegate(IConstructor invocable) 
    {
      this.delegate = invocable;
    }

    @Override
    public Object instantiate(Object[] args, ICartridgeContext context) throws RuntimeInvocationException
    {
      context.getProperties().putAll(properties);
      
      Object ret = delegate.instantiate(args, context);
      return ret;
    }
  }
  
  private class MethodMetadataDelegate implements IMethodMetadata 
  {
    final IMethodMetadata delegate;
    
    MethodMetadataDelegate(IMethodMetadata metadata)
    {
      this.delegate = metadata;
    }

    @Override
    public IMethod getMethodImplementation()
    {
      return new MethodDelegate(delegate.getMethodImplementation());
    }

    @Override
    public IType[] getParameterTypes()
    {
      return delegate.getParameterTypes();
    }

    @Override
    public IType getReturnType()
    {
      return delegate.getReturnType();
    }

    @Override
    public boolean isStatic()
    {
      return delegate.isStatic();
    }

    @Override
    public String getName()
    {
      return delegate.getName();
    }

    @Override
    public String getSchema()
    {
      return delegate.getSchema();
    }
  }
  
  private class MethodDelegate implements IMethod
  {
    private final IMethod delegate;

    MethodDelegate(IMethod invocable)
    {
      this.delegate = invocable;
    }

    @Override
    public Object invoke(Object obj, Object[] args, ICartridgeContext context)
        throws RuntimeInvocationException
    {
      context.getProperties().putAll(properties);
      
      Object ret = delegate.invoke(obj, args, context);
      return ret;
    }
  }
  
  private class FieldMetadataDelegate implements IFieldMetadata
  {
    private final IFieldMetadata delegate;
    
    FieldMetadataDelegate(IFieldMetadata metadata)
    {
      this.delegate = metadata;
    }

    @Override
    public IField getFieldImplementation()
    {
      return new FieldDelegate(delegate.getFieldImplementation());
    }

    @Override
    public IType getType()
    {
      return delegate.getType();
    }

    @Override
    public boolean isGetStatic()
    {
      return delegate.isGetStatic();
    }

    @Override
    public String getName()
    {
      return delegate.getName();
    }

    @Override
    public String getSchema()
    {
      return delegate.getSchema();
    }

    @Override
    public boolean hasGet()
    {
      return delegate.hasGet();
    }

    @Override
    public boolean hasSet()
    {
      return delegate.hasSet();
    }

    @Override
    public boolean isSetStatic()
    {
      return delegate.isSetStatic();
    }
  }
  
  private class FieldDelegate implements IField
  {
    private final IField delegate;
    
    FieldDelegate(IField invocable)
    {
      this.delegate = invocable;
    }
    
    @Override
    public Object get(Object obj, ICartridgeContext context) throws RuntimeInvocationException
    {
      context.getProperties().putAll(properties);
      
      Object ret = delegate.get(obj, context);
      return ret;
    }

    @Override
    public void set(Object obj, Object arg, ICartridgeContext context) throws RuntimeInvocationException
    {
      context.getProperties().putAll(properties);
      delegate.set(obj, arg, context);
    }
  }
  
  public CartridgeContextDelegate(ICartridge cartridge, Map<String, Object> properties)
  {
    if (properties == null) 
      this.properties = Collections.emptyMap();
    else 
      this.properties = Collections.unmodifiableMap(properties);
    
    if (cartridge.getFunctionMetadataLocator() != null)
      this.functionLocator = new UserFunctionMetadataDelegate(cartridge.getFunctionMetadataLocator()); 
    
    if (cartridge.getIndexInfoLocator() != null)
      this.indexLocator = new IndexInfoLocatorDelegate(cartridge.getIndexInfoLocator());
    
    if (cartridge.getTypeLocator() != null)
      this.typeLocator = new TypeLocatorDelegate(cartridge.getTypeLocator());
  }

  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    return functionLocator;
  }

  @Override
  public IIndexInfoLocator getIndexInfoLocator()
  {
    return indexLocator;
  }

  @Override
  public ITypeLocator getTypeLocator()
  {
    return typeLocator;
  }

}
