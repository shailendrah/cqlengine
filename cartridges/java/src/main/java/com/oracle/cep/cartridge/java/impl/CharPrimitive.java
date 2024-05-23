package com.oracle.cep.cartridge.java.impl;

import oracle.cep.common.Datatype;

public class CharPrimitive extends Datatype
{
  private static final long serialVersionUID = -7162641361682886094L;
  
  static Datatype TYPE = new CharPrimitive();
  
  private CharPrimitive() 
  {
    super(char.class.getName(), char.class);
  }
  
  @Override
  public String toString()
  {
    return "char@java";
  }
}

