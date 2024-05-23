package com.oracle.cep.cartridge.java;

import oracle.cep.extensibility.functions.ISimpleFunction;


/**
 * Metadata for cartridge functions that return Java types.
 * The infrastructure uses the Java cartridge to convert the Java types into CQL types.
 * 
 * @author Alex Alves
 *
 */
public interface FunctionMetadata
{
  /**
   * Returns the name of the method
   */
  public String getName();

  /**
   * Returns method parameter data-types
   * 
   * @return parameters
   */
  Class<?> [] getParameterTypes(); 

  /** 
   * Get the return type of the function
   * 
   * @return the return type of the function
   */
  Class<?> getReturnType();
  
  /**
   * Returns function implementation to be invoked at runtime during query execution.
   * 
   * @return runtime implementation
   */
   ISimpleFunction getFunctionImplementation();
  
}
