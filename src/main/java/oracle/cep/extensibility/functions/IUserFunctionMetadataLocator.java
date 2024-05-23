package oracle.cep.extensibility.functions;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;

/**
 * Locates function metadata. 
 * 
 * @author Alex Alves
 *
 */
public interface IUserFunctionMetadataLocator {

  /**
   * Returns function metadata.
   * 
   * @param name
   * @param paramTypes
   * @param context cartridge context associated to this extensible object
   * @return
   * @throws MetadataNotFoundException
   * @throws AmbiguousMetadataException
   */
  public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes, ICartridgeContext context)
    throws MetadataNotFoundException, AmbiguousMetadataException;

  /**
   * Returns metadata for all the functions implemented by this cartridge.
   * 
   * @return List of IUserFunctionMetadata with information about the functions
   */
  public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context)
    throws MetadataNotFoundException;
}
