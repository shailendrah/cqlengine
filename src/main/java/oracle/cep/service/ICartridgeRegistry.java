package oracle.cep.service;

import java.util.Map;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.type.ITypeLocator;

/**
 * Registry service for Cartridges.
 * <p/>
 * All cartridges that are to be used by the CQL engine must be registered in this
 *  service.
 * <p/>
 * A cartridge implementation is identified by a cartridgeID, which is unique, must not change
 *  across a re-launch of the server, and conforms to the syntax of a symbolic-name (e.g. com.oracle.cep.java).
 * CQL queries reference to a symbol provided by a cartridge through the use of links, therefore links
 *  must be unique across cartridges. 
 * <p/>
 * The process to register a new cartridge is:
 * <ol>
 * <li>Register cartridge implementation, using method <i>registerCartridge().</i></li>
 * <li>Associate server-scoped link to cartridge, using method <i>registerServerContext().</i></li>
 * <li>Optionally, associate an application-scoped link to cartridge, using method <i>registerApplicationContext().</i></li>
 * </ol>
 */
public interface ICartridgeRegistry
{
  /**
   * Registers a cartridge implementation.
   * <p/>
   * Links can be associated to this cartridge afterwards using the registerServerContext
   * and registerApplicationContext methods. 
   * <p> The cartridge ID must be unique.
   * It is recommended to use names following inverse domain name pattern
   * to make sure that the names are unique.
   *  
   * @param cartridgeID unique cartridge ID
   * @param cartridge ICartridge implementation
   * 
   * @throws CartridgeException if cartridge with the given ID already
   *         exists
   */
  void registerCartridge(String cartridgeID, ICartridge cartridge)
    throws CartridgeException;
  
  /**
   * Registers server-scoped cartridge link to an existing cartridge implementation.
   * The link may be optionally associated to a set of properties, which
   * contain contextual information.
   * <p/> 
   * Server-scoped links must be unique within a server instance. Note that they must be unique even
   *  if they are associated to a different <code>cartridgeID</code>.
   * <p/>
   * The cartridge ID must already have been registered with the registry.
   *  
   * @param linkID the link id for this context
   * @param cartridgeID identifier of the cartridge implementation
   * @param properties context
   * 
   * @throws CartridgeException if cartridge ID does not exist
   */
  void registerServerContext(String linkID,
                             String cartridgeID,
                             Map<String, Object> properties)
    throws CartridgeException;
  
  /**
   * Registers application-scoped cartridge link to an existing cartridge implementation.
   * The link may be optionally associated to a set of properties, which
   * contain contextual information.
   * <p/>
   * Application-scoped links must be unique within <code>applicationName</code>. They must also
   *  not override any existing server-scoped links. Note that they must be unique even
   *  if they are associated to a different <code>cartridgeID</code>.
   * <p/>
   * The cartridge ID must already have been registered with the registry. 
   *  
   * @param applicationName name of OCEP application
   * @param linkID the link id for this context
   * @param cartridgeID identifier of the cartridge implementation
   * @param properties context
   * @throws CartridgeException if the cartridge ID does not exist
   */
  void registerApplicationContext(String applicationName,
                                  String linkID,
                                  String cartridgeID, 
                                  Map<String, Object> properties)
    throws CartridgeException;
  
  /**
   * Returns the built-in Java type system. 
   * <p/>
   * This is can be used by other cartridges if they need to access Java types.
   * 
   * @return built-in Java type system
   */
  ITypeLocator getJavaTypeSystem();

  /**
   * Unregisters a previously registered cartridge.
   * 
   * @param cartridgeID unique cartridge ID
   * 
   * @throws CartridgeException if cartridge is not found
   * 
   */
  void unregisterCartridge(String cartridgeID) throws CartridgeException;

  /**
   * Unregisters a server-scoped cartridge link.
   * 
   * @param cartridgeID the ID of the cartridge whose context is to be deleted
   * @param linkID the link id for the context that should be removed
   * 
   * @throws CartridgeException if cartridge is not found
   * 
   */
  void unregisterServerContext(String cartridgeID, String linkID)
    throws CartridgeException;

  /**
   * Unregisters a application-scoped cartridge link.
   * 
   * @param cartridgeID the ID of the cartridge whose context is to be deleted
   * @param appName the name of the application to which the
   *        context is associated
   * @param linkID the link id for the context that should be removed
   * 
   * @throws CartridgeException if cartridge is not found
   * 
   */
  void unregisterApplicationContext(String cartridgeID,
                                    String appName,
                                    String linkID)
    throws CartridgeException;
  
}
