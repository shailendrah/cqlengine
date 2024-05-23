/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TestSpatialBase.java /main/1 2009/12/30 21:49:27 hopark Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/18/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TestSpatialBase.java /main/1 2009/12/30 21:49:27 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.spatial;

import java.util.Arrays;

import oracle.spatial.geometry.JGeometry;

import junit.framework.TestCase;

public abstract class TestSpatialBase extends TestCase
{
  public TestSpatialBase()
  {
    super();
  }

  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
  }

  public static boolean compareGeometry(JGeometry g1, JGeometry g2)
  {
	  System.out.println(g1.toStringFull());
	  System.out.println(g2.toStringFull());
	  if (g1.getDimensions() != g2.getDimensions()) 
		  return false;
	  if (g1.getSRID() != g2.getSRID()) 
		  return false;
	  int[] elemInfo1 = g1.getElemInfo();
	  int[] elemInfo2 = g2.getElemInfo();
	  
	  if ( !Arrays.equals(elemInfo1, elemInfo2)) 
		  return false;

	  double[] ord1 = g1.getOrdinatesArray();
	  double[] ord2 = g2.getOrdinatesArray();
	  if (ord1.length != ord2.length) 
		  return false;
	  for (int i = 0; i < ord1.length; i++)
	  {
		  double v1 = ord1[i];
		  double v2 = ord2[i];
		  double diff = v2 - v1;
		  if (diff > 0.000005)
			  return false;
	  }
	  return true;
  }
  
  protected void assertGeometry(JGeometry g1, JGeometry g2)
  {
	  assertTrue ( compareGeometry(g1, g2));
  }
}
