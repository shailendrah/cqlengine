/* Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
All rights reserved.*/
package com.oracle.cep.cartridge.spatial;

import java.util.HashMap;
import java.util.Map;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.service.ICartridgeRegistry;

public class CartridgeRegistry implements ICartridgeRegistry
{
  Map<String, Map<String,Object>> contextMap;
  Map<String, Map<String,Object>> contextMap2;
  
  public CartridgeRegistry()
  {
	  contextMap = new HashMap<String, Map<String,Object>>();
	  contextMap2 = new HashMap<String, Map<String,Object>>();
  }
  @Override
  public void registerCartridge(String cartridgeID, ICartridge cartridge)
      throws CartridgeException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void registerServerContext(String linkID, String cartridgeID,
      Map<String, Object> properties) throws CartridgeException
  {
    contextMap.put(linkID, properties);
    contextMap2.put(cartridgeID, properties);
  }

  @Override
  public void registerApplicationContext(String applicationName, String linkID,
      String cartridgeID, Map<String, Object> properties)
      throws CartridgeException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ITypeLocator getJavaTypeSystem()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void unregisterCartridge(String cartridgeID) throws CartridgeException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void unregisterServerContext(String cartridgeID, String linkID)
      throws CartridgeException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void unregisterApplicationContext(String cartridgeID, String appName,
      String linkID) throws CartridgeException
  {
    // TODO Auto-generated method stub
    
  }

}
