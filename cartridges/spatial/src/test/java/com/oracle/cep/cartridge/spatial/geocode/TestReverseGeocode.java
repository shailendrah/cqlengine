/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geocode/TestReverseGeocode.java /main/1 2015/10/01 22:29:44 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/10/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geocode/TestReverseGeocode.java /main/1 2015/10/01 22:29:44 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geocode;

import junit.framework.TestCase;

public class TestReverseGeocode extends TestCase
{
	public void testRCode1()
	{
		String url = "classpath:/cities1000.txt";
		ReverseGeoCode c = ReverseGeoCode.getInstance(url);
		GeoName n = c.nearestPlace(37.482120171750815, -122.20790145225877);
		assertTrue(n != null);
		String name = n.getName();
		assertTrue(name.equals("North Fair Oaks"));
	}

	public void testRCode2()
	{
		String url = "classpath:/cities5000.txt";
		ReverseGeoCode c = ReverseGeoCode.getInstance(url);
		GeoName n = c.nearestPlace(37.482120171750815, -122.20790145225877);
		assertTrue(n != null);
		String name = n.getName();
		assertTrue(name.equals("North Fair Oaks"));
	}

	public void testRCode3()
	{
		String url = "classpath:/cities15000.txt";
		ReverseGeoCode c = ReverseGeoCode.getInstance(url);
		GeoName n = c.nearestPlace(37.482120171750815, -122.20790145225877);
		assertTrue(n != null);
		String name = n.getName();
		assertTrue(name.equals("Redwood City"));
	}
}