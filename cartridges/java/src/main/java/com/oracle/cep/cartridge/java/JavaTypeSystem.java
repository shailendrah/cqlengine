package com.oracle.cep.cartridge.java;

import oracle.cep.extensibility.type.IType;

public interface JavaTypeSystem 
{
  /**
   * Converts CQL type into a Java class. 
   * 
   * @param CQL type
   * @return Java class
   */
  Class<?> getJavaType(IType datatype);

  /**
   * Converts multiple CQL types into their Java equivalent.
   * 
   * @param CQL types
   * @return Java classes
   */
  Class<?>[] getJavaTypes(IType[] datatype);
  
  /**
   * Converts Java class into a CQL type.
   * The CQL type may be a native type or a extensible type.
   * 
   * @param Java class
   * @return CQL type
   * 
   */
  IType getCQLType(Class<?> sourceClass);


  /**
   * Converts Java classes into CQL types.
   * The CQL type may be a native type or a extensible type.
   * 
   * @param Java classes
   * @return CQL types
   * 
   */
  IType[] getCQLTypes(Class<?>[] clazzes);

  /**
   * Sets class loader to be used to load Java Classes.
   * 
   * @param loader
   */
  void setJavaCartridgeClassLoader(JavaCartridgeClassLoader loader); 

}