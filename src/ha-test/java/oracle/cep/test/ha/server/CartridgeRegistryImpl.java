package oracle.cep.test.ha.server;

import oracle.cep.service.BaseCartridgeRegistry;
import oracle.cep.extensibility.cartridge.ICartridge;

/**
 * Cartridge registry and locator implementation to be used in BEAM.
 * 
 * @author mjames
 * @since   12c
 */
public class CartridgeRegistryImpl
    extends BaseCartridgeRegistry
{
    protected void preRegisterTypeActions(String cartridgeID,
                                          ICartridge metadata)
    {
        // Nothing to do here
    }

    protected void preUnregisterTypeActions(String cartridgeID)
    {
        // Nothing to do here
    }
}
