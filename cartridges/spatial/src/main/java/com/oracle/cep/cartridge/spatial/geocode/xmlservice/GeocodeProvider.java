package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

public interface GeocodeProvider {

	/**
	 * Geocode accepts the address of following format US1Form,US2form,GENForm & GDFForm
	 * and return the geocode matches having location(Geometry) and complete address
	 * @param address
	 * @return
	 */
	GeocodeMatches geocode(GeocodeAddress address);
	
	GeocodeAddress reverseGeocode(GeocodePosition point);
}
