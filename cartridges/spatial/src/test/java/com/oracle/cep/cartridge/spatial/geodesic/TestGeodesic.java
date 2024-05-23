/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geodesic/TestGeodesic.java /main/1 2015/06/18 19:14:13 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/geodesic/TestGeodesic.java /main/1 2015/06/18 19:14:13 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geodesic;

import junit.framework.TestCase;

public class TestGeodesic extends TestCase
{
	  public void testDirect() 
	  {
		  double lat1 = 40.6;
		  double lon1 = -73.8;
		  double azi1 = 45;
		  double s12 = 10000e3;
		  
		  Geodesic g = ReferenceSystem.WGS84.Direct(lat1, lon1, azi1, s12);
		  //System.out.println(g.lat2 + " " + g.lon2 + " " + g.azi2);
		  double expLat2 = 32.642844327605516;
		  double expLon2 = 49.01103958322419;
		  double expAzi2 = 140.36623046535098;
		  assertTrue( Math.abs(g.lat2 - expLat2) < ReferenceSystem.epsilon);
		  assertTrue( Math.abs(g.lon2 - expLon2) < ReferenceSystem.epsilon);
		  assertTrue( Math.abs(g.azi2 - expAzi2) < ReferenceSystem.epsilon);
	  }
	  
	  public void testInverse() 
	  {
		  double lat1 = -41.32;
		  double lon1 = 174.81;
		  double lat2 = 40.96;
		  double lon2 = -5.50;
		  
		  Geodesic g = ReferenceSystem.WGS84.Inverse(lat1, lon1, lat2, lon2);
		  System.out.println(g.azi1+ " " + g.azi2 + " " + g.s12);
		  double expAzi1 = 161.0676699861589;
		  double expAzi2 = 18.8251951232483;
		  double expS12 = 1.9959679267353818E7;
		  assertTrue( Math.abs(g.azi1 - expAzi1) < ReferenceSystem.epsilon);
		  assertTrue( Math.abs(g.azi2 - expAzi2) < ReferenceSystem.epsilon);
		  assertTrue( Math.abs(g.s12 - expS12) < ReferenceSystem.epsilon);
	  }
}