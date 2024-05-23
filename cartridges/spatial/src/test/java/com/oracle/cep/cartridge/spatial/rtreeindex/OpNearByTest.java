package com.oracle.cep.cartridge.spatial.rtreeindex;

public class OpNearByTest extends OpTestBase {
	
	private OpNearBy opNearBy;

	public void testNearBy() {
		try {
			opNearBy = new OpNearBy(0, cartesianContext);
			String name = (String) opNearBy.execute(new Object[] {37.482120171750815, -122.20790145225877});
			assertTrue(name.equals("Redwood City"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

