package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.IMetadataElement;

public interface IMethodMetadata extends IMetadataElement
{
  /**
   * Returns method parameter data-types
   * 
   * @return parameters
   */
  IType [] getParameterTypes(); 

  /** 
   * Get the return type of the function
   * 
   * @return the return type of the function
   */
  IType getReturnType();
  
  /**
   * Returns true if method is static.
   * 
   * @return boolean
   */
  boolean isStatic();

  /**
   * Return underlying runtime implementation
   */
  IMethod getMethodImplementation();

}
