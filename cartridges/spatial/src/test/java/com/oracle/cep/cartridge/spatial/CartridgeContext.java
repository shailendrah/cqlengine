/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */
package com.oracle.cep.cartridge.spatial;

import java.util.Map;

import oracle.cep.extensibility.cartridge.ICartridgeContext;

public class CartridgeContext implements ICartridgeContext
{
  private String name = "test";
  private Map<String, Object> props;
  
  public CartridgeContext(Map<String, Object> props)
  {
    this.props = props;
  }

  @Override
  public String getApplicationName()
  {
    return name;
  }

  @Override
  public Map<String, Object> getProperties()
  {
    return props;
  }

}
