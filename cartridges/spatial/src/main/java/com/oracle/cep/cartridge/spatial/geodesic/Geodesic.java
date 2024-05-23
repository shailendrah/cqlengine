/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geodesic/Geodesic.java /main/1 2015/06/18 19:14:12 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
   The shortest path between two points on a ellipsoid at (lat1, lon1) and (lat2, lon2) is called the geodesic. 
   Its length is s12 and the geodesic from point 1 to point 2 has azimuths azi1 and azi2 at the two end points. 
   (The azimuth is the heading measured clockwise from north. azi2 is the "forward" azimuth, i.e., the heading that takes you beyond point 2 not back to point 1.)
   The algorithms can be found in:
   http://link.springer.com/article/10.1007%2Fs00190-012-0578-z
   
   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geodesic/Geodesic.java /main/1 2015/06/18 19:14:12 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geodesic;

public class Geodesic
{
	  protected static final int CAP_NONE = 0;
	  protected static final int CAP_C1   = 1<<0;
	  protected static final int CAP_C1p  = 1<<1;
	  protected static final int CAP_C2   = 1<<2;
	  protected static final int CAP_C3   = 1<<3;
	  protected static final int CAP_C4   = 1<<4;
	  protected static final int CAP_ALL  = 0x1F;
	  protected static final int CAP_MASK = CAP_ALL;
	  protected static final int OUT_ALL  = 0x7F80;
	  protected static final int OUT_MASK = 0xFF80; // Include LONG_UNROLL

	  /**
	   * No capabilities, no output.
	   **********************************************************************/
	  public static final int MASK_NONE          = 0;
	  /**
	   * Calculate latitude <i>lat2</i>.  (It's not necessary to include this as a
	   * capability to {@link GeodesicLine} because this is included by default.)
	   **********************************************************************/
	  public static final int MASK_LATITUDE      = 1<<7  | CAP_NONE;
	  /**
	   * Calculate longitude <i>lon2</i>.
	   **********************************************************************/
	  public static final int MASK_LONGITUDE     = 1<<8  | CAP_C3;
	  /**
	   * Calculate azimuths <i>azi1</i> and <i>azi2</i>.  (It's not necessary to
	   * include this as a capability to {@link GeodesicLine} because this is
	   * included by default.)
	   **********************************************************************/
	  public static final int MASK_AZIMUTH       = 1<<9  | CAP_NONE;
	  /**
	   * Calculate distance <i>s12</i>.
	   **********************************************************************/
	  public static final int MASK_DISTANCE      = 1<<10 | CAP_C1;
	  /**
	   * Allow distance <i>s12</i> to be used as <i>input</i> in the direct
	   * geodesic problem.
	   **********************************************************************/
	  public static final int MASK_DISTANCE_IN   = 1<<11 | CAP_C1 | CAP_C1p;
	  /**
	   * Calculate reduced length <i>m12</i>.
	   **********************************************************************/
	  public static final int MASK_REDUCEDLENGTH = 1<<12 | CAP_C1 | CAP_C2;
	  /**
	   * Calculate geodesic scales <i>M12</i> and <i>M21</i>.
	   **********************************************************************/
	  public static final int MASK_GEODESICSCALE = 1<<13 | CAP_C1 | CAP_C2;
	  /**
	   * Calculate area <i>S12</i>.
	   **********************************************************************/
	  public static final int MASK_AREA          = 1<<14 | CAP_C4;
	  /**
	   * Unroll <i>lon2</i>.
	   **********************************************************************/
	  public static final int MASK_LONG_UNROLL   = 1<<15;
	  /**
	   * For backward compatibility only; use LONG_UNROLL instead.
	   **********************************************************************/
	  public static final int MASK_LONG_NOWRAP   = MASK_LONG_UNROLL;
	  /**
	   * All capabilities, calculate everything.  (LONG_UNROLL is not included in
	   * this mask.)
	   **********************************************************************/
	  public static final int MASK_ALL           = OUT_ALL| CAP_ALL;
	
  public double lat1;
  public double lon1;
  public double azi1;

  public double lat2;
  public double lon2;
  public double azi2;

  public double s12;	//distance between point 1 and point 2 (meters).
  public double a12;	//arc length on the auxiliary sphere between point 1 and point 2
  public double m12;	//reduced length of geodesic (meters).
  public double M12;	//geodesic scale of point 2 relative to point 1 (dimensionless).
  public double M21;	//geodesic scale of point 1 relative to point 2 (dimensionless).
  public double S12;	//area under the geodesic (meters<sup>2</sup>).

  public Geodesic() {
    lat1 = lon1 = azi1 = lat2 = lon2 = azi2 =
    s12 = a12 = m12 = M12 = M21 = S12 = Double.NaN;
  }


}