package oracle.cep.extensibility.cartridge;

import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.ITypeLocator;

/**
 * The Cartridge interface aggregates several extensibility points, such as
 *  types, functions, indexes and sources, into a single manageable unit.   
 *
 */
public interface ICartridge 
{
  /**
   * Returns locator for non-native types provided by this cartridge.
   * 
   * @return type locator
   */
  ITypeLocator getTypeLocator();

  /**
   * Returns locator for functions provided by this cartridge.
   * 
   * @return function locator
   */
  IUserFunctionMetadataLocator getFunctionMetadataLocator();

  /**
   * Returns locator for index information of indexes provided by the
   * cartridge 
   * @return index information locator locator
   */
  IIndexInfoLocator getIndexInfoLocator();
}
