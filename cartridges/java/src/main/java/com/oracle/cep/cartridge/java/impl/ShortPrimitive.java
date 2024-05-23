package com.oracle.cep.cartridge.java.impl;

import oracle.cep.common.Datatype;

public class ShortPrimitive extends Datatype
{
  private static final long serialVersionUID = -697008484079517102L;
  
  static Datatype TYPE = new ShortPrimitive();

  private ShortPrimitive()
  {
    super(short.class.getName(), short.class);
  }
  
  @Override
  public String toString()
  {
    return "short@java";
  }
}
