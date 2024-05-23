package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import junit.framework.TestCase;

import com.oracle.cep.cartridge.spatial.Geometry;

/**
 * These test cases are added for just references and not intended to run since these test cases access elocation server
 * So annotated with ignore to skip these tests.
 */
@Ignore
public class TestOracleSpatialGeocodeProvider  extends TestCase{
	
	public void testGeocodeUsForm1()
	{
		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		GeocodeAddress input = GeocodeAddress.createUsForm1Address(null, "10 fifth ave", null, "New York, NY");
		GeocodeMatches geocodeMatches = geoProvider.geocode(input);
		GeocodeAddress address = geocodeMatches.get(0).address;
		Geometry location = geocodeMatches.get(0).location;
		System.out.println(geocodeMatches.get(0).address.getOutputAddress());
		
		double lon = -73.99628714285714;
		double lat=  40.73233571428572;
		assertTrue(location.compareTo(Geometry.createPoint(8307, lon, lat)));
		assertTrue(address.houseNumber.equals("10"));
		
		//Get from cache
		GeocodeAddress input2 = GeocodeAddress.createUsForm1Address(null, "10 fifth ave", null, "New York, NY");
		GeocodeMatches geocodeMatches2 = geoProvider.geocode(input2);
		assertTrue(geocodeMatches == geocodeMatches2);
		
	}
	
	public void testGeocodeUsForm2()
	{
		
		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		double lon = -71.46006;
		double lat =  42.71004;
		
		GeocodeAddress input = GeocodeAddress.createUsForm2Address(null, "1 Oracle DR", null, "Nashua", "NH", null);
		GeocodeMatches geocodeMatches = geoProvider.geocode(input);
		GeocodeAddress address = geocodeMatches.get(0).address;
		Geometry location = geocodeMatches.get(0).location;
		System.out.println(geocodeMatches.get(0).address.getOutputAddress());
		
		assertTrue(location.compareTo(Geometry.createPoint(8307, lon, lat)));
		assertTrue(address.houseNumber.equals("1"));
		
		//Get from cache
		GeocodeAddress input2 = GeocodeAddress.createUsForm2Address(null, "1 Oracle DR", null, "Nashua", "NH", null);
		GeocodeMatches geocodeMatches2 = geoProvider.geocode(input2);
		assertTrue(geocodeMatches==geocodeMatches2);
	}
	
	public void testGeocodeUnformatted()
	{
		
		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		double lon = -122.26109;
		double lat =  37.53117;

		List<String> addressList = new ArrayList<>();
		addressList.add("Mr. Larry Ellison");
		addressList.add("Oracle Corp.");
		addressList.add("500 Oracle Pky");
		addressList.add("Redwood city");
		addressList.add("CA");
		GeocodeAddress unformAddr = GeocodeAddress.createUnformattedAddress(addressList);
		GeocodeMatches geocodeMatches = geoProvider.geocode(unformAddr);
		GeocodeAddress address = geocodeMatches.get(0).address;
		Geometry location = geocodeMatches.get(0).location;	
		assertTrue(location.compareTo(Geometry.createPoint(8307, lon, lat)));
		assertTrue(address.houseNumber.equals("500"));
		
		//Get from cache
		List<String> addressList2 = new ArrayList<>();
		addressList2.add("Mr. Larry Ellison");
		addressList2.add("Oracle Corp.");
		addressList2.add("500 Oracle Pky");
		addressList2.add("Redwood city");
		addressList2.add("CA");
		GeocodeAddress unformAddr2 = GeocodeAddress.createUnformattedAddress(addressList2);
		GeocodeMatches geocodeMatches2 = geoProvider.geocode(unformAddr2);
		assertTrue(geocodeMatches == geocodeMatches2);
	}
	
	public void testReverseGeocode()
	{
		
		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		GeocodePosition point = new GeocodePosition(-71.46006, 42.71004);
		GeocodeAddress reverseGeocCode = geoProvider.reverseGeocode(point);
		
		assertTrue(reverseGeocCode.street.equals("Oracle Dr"));
		assertTrue(reverseGeocCode.houseNumber.equals("3"));
		
		//Get from cache
		GeocodePosition point2 = new GeocodePosition(-71.46006, 42.71004);
		GeocodeAddress reverseGeocCode2 = geoProvider.reverseGeocode(point2);
		assertTrue(reverseGeocCode == reverseGeocCode2);
		
	}
	
	public void testReverseGeocodeNotFound()
	{
		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		try{
		GeocodePosition point = new GeocodePosition(42.71004, -71.46006);
		geoProvider.reverseGeocode(point);
		 fail("expected exception was not occured.");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("match_code : 0"));
			assertTrue(e.getMessage().contains("not geocoded"));
		}
	}
}
