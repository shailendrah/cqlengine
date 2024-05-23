package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeMatches;

public class OpLocationTest extends OpTestBase {
	private OpLocation opLocation;
	
	public void testLocationUsForm1() {
		try {
			opLocation = new OpLocation( 0, cartesianContext);
			GeocodeMatches addresses = (GeocodeMatches) opLocation.execute(new Object[] {"", "1 Oracle st", "", "Nashua, NH 03062",""});
			assertTrue(addresses.get(0).address.getOutputAddress().equals(" 1 Oracle Dr Nashua NH  US 03062  L 0.96 22325991 "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testLocationUsForm2() {
		try {
			opLocation = new OpLocation( 0, cartesianContext);
			GeocodeMatches addresses = (GeocodeMatches) opLocation.execute(new Object[] {"","1 Oracle st","","Nashua","NH", "",""});
			assertTrue(addresses.get(0).address.getOutputAddress().equals(" 1 Oracle Dr Nashua NH  US 03062  L 0.96 22325991 "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testLocationGenForm() {
		try {
			opLocation = new OpLocation( 0, cartesianContext);
			GeocodeMatches addresses = (GeocodeMatches) opLocation.execute(new Object[] {"","1 Oracle st","","","Nashua","NH","US","", "",""});
			assertTrue(addresses.get(0).address.getOutputAddress().equals(" 1 Oracle Dr Nashua NH  US 03062  L 0.96 22325991 "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testLocationGdfForm() {
		try {
			opLocation = new OpLocation( 0, cartesianContext);
			GeocodeMatches addresses = (GeocodeMatches) opLocation.execute(new Object[] {"","1 Oracle st","","Nashua","","","NH","US","", "",""});
			assertTrue(addresses.get(0).address.getOutputAddress().equals(" 1 Oracle Dr Nashua NH  US 03062  L 0.96 22325991 "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


