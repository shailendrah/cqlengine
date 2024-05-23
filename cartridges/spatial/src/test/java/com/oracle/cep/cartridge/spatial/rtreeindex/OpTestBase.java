package com.oracle.cep.cartridge.spatial.rtreeindex;

import java.util.HashMap;
import java.util.Map;

import com.oracle.cep.cartridge.spatial.CartridgeContext;
import com.oracle.cep.cartridge.spatial.CartridgeRegistry;
import com.oracle.cep.cartridge.spatial.GeodeticParam;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;
import com.oracle.cep.cartridge.spatial.SpatialContext;

import junit.framework.TestCase;
import oracle.cep.extensibility.cartridge.CartridgeException;

public abstract class OpTestBase extends TestCase {

	protected CartridgeContext cartesianContext;

	protected void setUp() throws CartridgeException {
		SpatialCartridge.createInstance(new CartridgeRegistry());
		int srid = GeodeticParam.LAT_LNG_WGS84_SRID;
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put(SpatialContext.GEO_PARAM, GeodeticParam.get(srid));
		cartesianContext = new CartridgeContext(props);
	}

}
