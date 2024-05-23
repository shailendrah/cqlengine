/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/rcs/TestOSGB.java /main/1 2015/10/01 22:29:50 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      08/12/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/rcs/TestOSGB.java /main/1 2015/10/01 22:29:50 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rcs;

import junit.framework.TestCase;

public class TestOSGB extends TestCase
{
	private static final double EPSILON = 0.0000000001;
	
	protected boolean doubleEqual(double actual, double expected)
	{
		return Math.abs(expected - actual) < EPSILON;
	}
	
	public void testToWGS() 
	{
		LatLon pOSGB = new LatLon(52,59,58.72, LatLon.NORTH, 
					1,0,6.49, LatLon.EAST, Datum.OSGB36);
		System.out.println(pOSGB);;
		System.out.println(pOSGB.toDMSString());
		LatLon pWGS84 = pOSGB.convertDatum(Datum.WGS84);
		System.out.println(pWGS84);;
		System.out.println(pWGS84.toDMSString());
		assertTrue(doubleEqual(pWGS84.getLatitude(), 53.0000002353188));
		assertTrue(doubleEqual(pWGS84.getLongitude(), 0.9999999457647897));
	}

	public void testToWGS1()
	{
		LatLon pOSGB = new LatLon(51.1097, 1.27666, Datum.OSGB36);
		System.out.println(pOSGB);;
		System.out.println(pOSGB.toDMSString());
		LatLon pWGS84 = pOSGB.convertDatum(Datum.WGS84);
		System.out.println(pWGS84);;
		System.out.println(pWGS84.toDMSString());
		assertTrue(doubleEqual(pWGS84.getLatitude(), 51.11027955089868));
		assertTrue(doubleEqual(pWGS84.getLongitude(),  1.2749119099766566));
	}

	public void testToOSGB() 
	{
		LatLon pWGS84 = new LatLon(51.4778, -0.0016, Datum.WGS84);
		System.out.println(pWGS84);;
		System.out.println(pWGS84.toDMSString());
		LatLon pOSGB = pWGS84.convertDatum(Datum.OSGB36);
		System.out.println(pOSGB);;
		System.out.println(pOSGB.toDMSString());
		assertTrue(doubleEqual(pOSGB.getLatitude(), 51.47728415218075));
		assertTrue(doubleEqual(pOSGB.getLongitude(), 1.9599494549129943E-5));
	}
}

