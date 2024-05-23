package com.oracle.cep.cartridge.java.impl;

import java.util.HashMap;

//import org.springframework.beans.factory.InitializingBean;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.service.ICartridgeRegistry;

public class JavaCartridge implements ICartridge
        //, InitializingBean
{
  public static final String JAVA_CARTRIDGE_NAME = "com.oracle.cep.cartridge.java";
  public static final String JAVA_SERVER_CONTEXT_NAME = "java";
  
  public static final String JAVA_CARTRIDGE_LOGGER = "com.oracle.cep.cartridge.java";
  
  private ICartridgeRegistry registry;
  private ITypeLocator typeSystem;
  
  private IUserFunctionMetadataLocator functionLocator;

  public JavaCartridge(ICartridgeRegistry registry) throws CartridgeException {
    this.registry = registry;
  }

  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    return functionLocator;
  }

  @Override
  public ITypeLocator getTypeLocator()
  {
    return typeSystem;
  }
  
  @Override 
  public IIndexInfoLocator getIndexInfoLocator()
  {
    return null;
  }
  
  public void setTypeLocator(ITypeLocator typeLocator)
  {
    typeSystem = typeLocator;
  }

  //@Override
  public void afterPropertiesSet() throws Exception
  {
    assert typeSystem != null;
  
    functionLocator = new JavaFunctors((JavaTypeSystem) typeSystem);
    
    registry.registerCartridge(JAVA_CARTRIDGE_NAME, this);
    registry.registerServerContext(JAVA_SERVER_CONTEXT_NAME, JAVA_CARTRIDGE_NAME, new HashMap<String, Object>());
  }
  
}
