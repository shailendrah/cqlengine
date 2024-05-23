package com.oracle.cep.cartridge.java;

import java.util.List;
import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;
import oracle.cep.service.ICartridgeRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractJavaCartridge implements ICartridge
{
  private static final Log logger = 
    LogFactory.getLog("JavaCartridge");
  
  private final JavaTypeSystem javaTypeSystem;
  private final String cartridgeName;
  private final IUserFunctionMetadataLocator functionLocator = 
    new UserFunctionMetadataLocatorWrapper();
  private ICartridgeRegistry registry;

  public AbstractJavaCartridge(String name, ICartridgeRegistry registry) throws CartridgeException {
    registry.registerCartridge(name, this);

    this.registry = registry;
    this.cartridgeName = name;
    this.javaTypeSystem = 
      (JavaTypeSystem) registry.getJavaTypeSystem();
  }
  
  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    return functionLocator;
  }

  @Override
  public ITypeLocator getTypeLocator()
  {
    return (ITypeLocator) this.javaTypeSystem;
  }
  
  public IIndexInfoLocator getIndexInfoLocator()
  {
    return null;  
  }

  public ICartridgeRegistry getRegistry() {
      return registry;
  }
  
  private class UserFunctionMetadataLocatorWrapper implements IUserFunctionMetadataLocator {

    @Override
    public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes, ICartridgeContext context)
        throws MetadataNotFoundException, AmbiguousMetadataException
    {
      Class<?>[] convertedParamTypes;
      try
      {
        convertedParamTypes = javaTypeSystem.getJavaTypes(paramTypes);
      } catch (Exception e)
      {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to convert Java type to CQL type", e);
        }
        
        throw new MetadataNotFoundException(cartridgeName, 
            name);
      }
      
      FunctionMetadata metadata = AbstractJavaCartridge.this.getFunction(name, convertedParamTypes, context);
      
      if (metadata == null) 
      {
        throw new MetadataNotFoundException(cartridgeName, 
            name);
      }
      
      return new FunctionMetadataWrapper(name, metadata);
    }

    public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context)
        throws MetadataNotFoundException
    {
      List<FunctionMetadata> mdList = AbstractJavaCartridge.this.getAllFunctions(context);
      
      List<IUserFunctionMetadata> retVal = new ArrayList<IUserFunctionMetadata>();
      for (FunctionMetadata metadata : mdList)
      {
        retVal.add(new FunctionMetadataWrapper(metadata.getName(), metadata));
      }

      return retVal;
    }
  }
  
  private class FunctionMetadataWrapper implements ISimpleFunctionMetadata
  {
    private final String name;
    private final Datatype returnType;
    private final IAttribute [] params;
    private final ISimpleFunction impl;

    public FunctionMetadataWrapper(String name, FunctionMetadata metadata) 
    throws MetadataNotFoundException
    {
      params = 
        convertToAttribute(metadata.getParameterTypes());
      returnType = 
        (Datatype) javaTypeSystem.getCQLType(metadata.getReturnType());
      impl =
        metadata.getFunctionImplementation();
      this.name = name + "@" + cartridgeName;
    }

    @Override
    public int getNumParams()
    {
      return params.length;
    }

    @Override
    public IAttribute getParam(int pos) throws MetadataException
    {
      return params[pos];
    }

    @Override
    public Datatype getReturnType()
    {
      return returnType;
    }

    @Override
    public String getName()
    {
      return name;
    }
    
    private IAttribute[] convertToAttribute(Class<?>[] parameterTypes) 
    throws MetadataNotFoundException
    {
      Attribute [] attributes = new Attribute[parameterTypes.length];
      
      for (int i = 0; i < parameterTypes.length; i++)
      {
        IType type = javaTypeSystem.getCQLType(parameterTypes[i]);
        
        attributes[i] = new Attribute("attr" + new Integer(i), 
            (Datatype) type, 0);
      }
      
      return attributes;
    }

    @Override
    public ISimpleFunction getImplClass()
    {
      return impl;
    }

    @Override
    public String getSchema()
    {
      return cartridgeName;
    }
  }
  
  /**
   * Returns locator for java functions provided by this cartridge.
   * Java functions, as opposed to regular CQL functions {@link IUserFunctionMetadata}, 
   *  defined their parameters and return type in terms of Java classes instead of IType.
   *  
   * @return java function metadata locator
   * @see IType
   */
  public abstract FunctionMetadata getFunction(String name, Class<?>[] paramTypes, ICartridgeContext context)
    throws MetadataNotFoundException, AmbiguousMetadataException;

  public abstract List<FunctionMetadata> getAllFunctions(ICartridgeContext context)
    throws MetadataNotFoundException;
  
  /**
   * Can be invoked by client to unregister cartridge from registry.
   * 
   * @throws CartridgeException if unregister fails.
   */
  public void destroy() throws CartridgeException 
  {
    if (registry != null)
      registry.unregisterCartridge(cartridgeName);
  }

}
