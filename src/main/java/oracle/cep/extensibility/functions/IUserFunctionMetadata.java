package oracle.cep.extensibility.functions;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.IMetadataElement;
import oracle.cep.metadata.MetadataException;

/**
 * Metadata to describe functions, such as parameter and return type.
 * 
 * @author Alex Alves
 *
 */
public interface IUserFunctionMetadata extends IMetadataElement
{
  /**
   * Returns number of parameters for function.
   * 
   * @return
   */
  int getNumParams();

  /**
   * Returns function parameter metadata.
   * 
   * @param pos
   * @return
   * @throws MetadataException
   */
  IAttribute getParam(int pos) throws MetadataException;

  /** 
   * Get the return type of the function
   * 
   * @return the return type of the function
   */
  Datatype getReturnType();
}
