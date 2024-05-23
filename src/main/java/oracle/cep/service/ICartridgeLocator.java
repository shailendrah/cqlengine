package oracle.cep.service;

import java.util.List;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.type.ITypeLocator;

/**
 * 
 * @author Alex Alves
 *
 */
public interface ICartridgeLocator
{
  String JAVA_CARTRIDGE = "com.oracle.cep.cartridge.java";
  
  /**
   * Locates cartridge with the given server context.
   * 
   * @param serverContextID cartridge context ID
   * @return cartridge instance
   * 
   * @throws CartridgeException if not found
   */
  public ICartridge getCartridge(String serverContextID)
    throws CartridgeException;
  
  /**
   * Locates cartridge for the given application with the given context id.
   * 
   * @param appName the name of the application to which the cartridge is
   *        associated
   * @param contextID the ID of the context of the cartridge
   * @return cartridge instance
   * 
   * @throws CartridgeException if not found
   */
  public ICartridge getCartridge(String appName, String contextID)
    throws CartridgeException;

  /**
   * Locates internal cartridge with the given server context.
   * 
   * @param cartridgeId cartridgeID
   * @return cartridge instance
   * 
   * @throws CartridgeException if not found
   */
  public ICartridge getInternalCartridge(String cartridgeId)
    throws CartridgeException;

  /**
   * Get built-in Java Type System
   * 
   * @return java type system
   */
  public ITypeLocator getJavaTypeSystem();
  
}
