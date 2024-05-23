package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.IMetadataElement;

public interface IConstructorMetadata extends IMetadataElement
{
  /**
   * Returns constructor parameters
   * 
   * @return parameters
   */
  IType [] getParameterTypes(); 
  
  /**
   * Returns data-type of instance created by this constructor.
   * 
   * @return datatype
   */
  IType getInstanceType();
  
  /**
   * Return underlying runtime implementation
   */
  IConstructor getConstructorImplementation();
  
}
