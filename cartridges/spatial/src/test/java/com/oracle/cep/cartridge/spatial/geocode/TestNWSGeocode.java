/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geocode/TestNWSGeocode.java /main/1 2015/10/01 22:29:44 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geocode/TestNWSGeocode.java /main/1 2015/10/01 22:29:44 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geocode;

import junit.framework.TestCase;

public class TestNWSGeocode extends TestCase
{
	  private void validateResult(NWSGeocode.DecodeResult r)
	  {
		  assertTrue(r != null);
		  assertTrue(r.geom != null);
	  }
	  
	  public void testFIPS6() throws Exception
	  { 
		  NWSGeocode c = NWSGeocode.getInstance();
		  NWSGeocode.DecodeResult g = c.decodeFIPS6("005093");
		  validateResult(g);
	  }

	  public void testUGCC()  throws Exception
	  { 
		  NWSGeocode c = NWSGeocode.getInstance();
		  NWSGeocode.DecodeResult g = c.decodeUGC("ARC035");
		  validateResult(g);
	  }

	  public void testUGCG()  throws Exception
	  { 
		  NWSGeocode c = NWSGeocode.getInstance();
		  NWSGeocode.DecodeResult g = c.decodeUGC("AKZ222");
		  validateResult(g);
	  }
}