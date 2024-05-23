package com.oracle.cep.cartridge.java.impl;

import oracle.cep.common.Datatype;

public class BytePrimitive extends Datatype
{
  private static final long serialVersionUID = 2242049962257250191L;
  
  static Datatype TYPE = new BytePrimitive();
  
  private BytePrimitive() 
  {
    super(byte.class.getName(), byte.class);
  }
  
  @Override
  public String toString()
  {
    return "byte@java";
  }
}