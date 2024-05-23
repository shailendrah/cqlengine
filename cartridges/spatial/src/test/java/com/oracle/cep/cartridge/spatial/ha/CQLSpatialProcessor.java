package com.oracle.cep.cartridge.spatial.ha;

import com.oracle.cep.cartridge.java.impl.JavaCartridge;
import com.oracle.cep.cartridge.java.impl.JavaTypeSystemImpl;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.exceptions.CEPException;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.service.BaseCartridgeRegistry;
import oracle.cep.service.ICartridgeLocator;
import oracle.cep.test.ha.server.CQLProcessor;
import oracle.cep.test.ha.server.CartridgeRegistryImpl;

/**
 * Extends test CQLProcessor to register spatial cartridge.
 * @author santkumk
 *
 */
public class CQLSpatialProcessor extends CQLProcessor {

	@Override 
	protected ICartridgeLocator setupCartridgeLocator()
			    throws CEPException
			  {
			    BaseCartridgeRegistry registry = new CartridgeRegistryImpl();
			    ITypeLocator typeLocator = new JavaTypeSystemImpl();
			    registry.setJavaTypeSystem(typeLocator);

			    try {
			      JavaCartridge javaCartridge = new JavaCartridge(registry);
			      javaCartridge.setTypeLocator(typeLocator);
			      javaCartridge.afterPropertiesSet();

			      SpatialCartridge.createInstance(registry);
			    } catch(Exception e) 
			    {
			      throw new RuntimeException(e);
			    }
			     
			    return registry;
			  }
}
