package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeAddress;

import oracle.cep.extensibility.functions.UDFException;

public class OpNearByPlacesTest extends OpTestBase {
	
	private OpNearByPlace opNearByPlace;
	

	public void testNearBy() {
		try {
			opNearByPlace = new OpNearByPlace( 0, cartesianContext);
			GeocodeAddress address = (GeocodeAddress) opNearByPlace.execute(new Object[] { -122.20790145225877, 37.482120171750815});
                        assertTrue(" 783 Hurlingame Ave Redwood City CA  US 94063  L 0.8406457141095385 23607144 "
					.equals(address.getOutputAddress())
					|| " 783 Hurlingame Ave San Francisco CA  US 94063  L 0.8406457141095385 23607144 "
							.equals(address.getOutputAddress()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testNearByWithCountry() {
		try {
			opNearByPlace = new OpNearByPlace( 0, cartesianContext);
			GeocodeAddress address = (GeocodeAddress) opNearByPlace.execute(new Object[] { -122.20790145225877, 37.482120171750815, "US"});
                        assertTrue(" 783 Hurlingame Ave Redwood City CA  US 94063  L 0.8406457141095385 23607144 "
                                        .equals(address.getOutputAddress())
                                        || " 783 Hurlingame Ave San Francisco CA  US 94063  L 0.8406457141095385 23607144 "
                                                        .equals(address.getOutputAddress()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void testNearByNeg() {
		try {
			opNearByPlace = new OpNearByPlace( 0, cartesianContext);
			GeocodeAddress address = (GeocodeAddress) opNearByPlace.execute(new Object[] { 37.482120171750815, -122.20790145225877});
		} catch (RuntimeException | UDFException e) {
			// cordinates do not exist, must throw an exception
			assertTrue(true);
		}
	}
}

