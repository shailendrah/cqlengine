/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geodesic/ReferenceSystem.java /main/1 2015/06/18 19:14:13 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geodesic/ReferenceSystem.java /main/1 2015/06/18 19:14:13 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geodesic;

public class ReferenceSystem {
	// http://home.online.no/~sigurdhu/WGS84_Eng.html
	public static final double WGS84_semi_major_axis = 6378137;
	public static final double WGS84_flattening = 1 / 298.257223563;

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
	  public static final int NONE          = 0;
	  /**
	   * Calculate latitude <i>lat2</i>.  (It's not necessary to include this as a
	   * capability to {@link GeodesicLine} because this is included by default.)
	   **********************************************************************/
	  public static final int LATITUDE      = 1<<7  | CAP_NONE;
	  /**
	   * Calculate longitude <i>lon2</i>.
	   **********************************************************************/
	  public static final int LONGITUDE     = 1<<8  | CAP_C3;
	  /**
	   * Calculate azimuths <i>azi1</i> and <i>azi2</i>.  (It's not necessary to
	   * include this as a capability to {@link GeodesicLine} because this is
	   * included by default.)
	   **********************************************************************/
	  public static final int AZIMUTH       = 1<<9  | CAP_NONE;
	  /**
	   * Calculate distance <i>s12</i>.
	   **********************************************************************/
	  public static final int DISTANCE      = 1<<10 | CAP_C1;
	  /**
	   * Allow distance <i>s12</i> to be used as <i>input</i> in the direct
	   * geodesic problem.
	   **********************************************************************/
	  public static final int DISTANCE_IN   = 1<<11 | CAP_C1 | CAP_C1p;
	  /**
	   * Calculate reduced length <i>m12</i>.
	   **********************************************************************/
	  public static final int REDUCEDLENGTH = 1<<12 | CAP_C1 | CAP_C2;
	  /**
	   * Calculate geodesic scales <i>M12</i> and <i>M21</i>.
	   **********************************************************************/
	  public static final int GEODESICSCALE = 1<<13 | CAP_C1 | CAP_C2;
	  /**
	   * Calculate area <i>S12</i>.
	   **********************************************************************/
	  public static final int AREA          = 1<<14 | CAP_C4;
	  /**
	   * Unroll <i>lon2</i>.
	   **********************************************************************/
	  public static final int LONG_UNROLL   = 1<<15;
	  /**
	   * For backward compatibility only; use LONG_UNROLL instead.
	   **********************************************************************/
	  public static final int LONG_NOWRAP   = LONG_UNROLL;
	  /**
	   * All capabilities, calculate everything.  (LONG_UNROLL is not included in
	   * this mask.)
	   **********************************************************************/
	  public static final int ALL           = OUT_ALL| CAP_ALL;
	
	public static class Pair {
		  /**
		   * The first member of the pair.
		   **********************************************************************/
		  public double first;
		  /**
		   * The second member of the pair.
		   **********************************************************************/
		  public double second;
		  /**
		   * Constructor
		   * <p>
		   * @param first the first member of the pair.
		   * @param second the second member of the pair.
		   **********************************************************************/
		  public Pair(double first, double second)
		  { this.first = first; this.second = second; }
		}
	
	  /**
	   * The number of binary digits in the fraction of a double precision
	   * number 
	   **********************************************************************/
	  public static final int digits = 53;
	  public static final double epsilon = Math.pow(0.5, digits - 1);
	  public static final double min = Math.pow(0.5, 1022);
	  /**
	   * The number of radians in a degree. 
	   **********************************************************************/
	  public static final double degree = Math.PI / 180;

	  /**
	   * The order of the expansions used by Geodesic.
	   **********************************************************************/
	  protected static final int GEOGRAPHICLIB_GEODESIC_ORDER = 6;

	  protected static final int nA1_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nC1_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nC1p_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nA2_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nC2_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nA3_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nA3x_ = nA3_;
	  protected static final int nC3_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nC3x_ = (nC3_ * (nC3_ - 1)) / 2;
	  protected static final int nC4_ = GEOGRAPHICLIB_GEODESIC_ORDER;
	  protected static final int nC4x_ = (nC4_ * (nC4_ + 1)) / 2;
	  private static final int maxit1_ = 20;
	  private static final int maxit2_ = maxit1_ + digits + 10;

	  // Underflow guard.  We require
	  //   tiny_ * epsilon() > 0
	  //   tiny_ + epsilon() == epsilon()
	  protected static final double tiny_ = Math.sqrt(min);
	  private static final double tol0_ = epsilon;
	  private static final double tol1_ = 200 * tol0_;
	  private static final double tol2_ = Math.sqrt(tol0_);
	  // Check on bisection interval
	  private static final double tolb_ = tol0_ * tol2_;
	  private static final double xthresh_ = 1000 * tol2_;

	  protected double _a, _f, _f1, _e2, _ep2, _b, _c2;
	  private double _n, _etol2;
	  private double _A3x[], _C3x[], _C4x[];

	  /**
	   * A global instantiation of Geodesic with the parameters for the WGS84
	   * ellipsoid.
	   **********************************************************************/
	  public static final ReferenceSystem WGS84 =
	    new ReferenceSystem(WGS84_semi_major_axis, WGS84_flattening);
	  
	  /**
	   * Constructor for a ellipsoid with
	   * <p>
	   * @param a equatorial radius (meters).
	   * @param f flattening of ellipsoid.  Setting <i>f</i> = 0 gives a sphere.
	   *   Negative <i>f</i> gives a prolate ellipsoid.  If <i>f</i> &gt; 1, set
	   *   flattening to 1/<i>f</i>.
	   * @exception GeographicErr if <i>a</i> or (1 &minus; <i>f</i> ) <i>a</i> is
	   *   not positive.
	   **********************************************************************/
	  public ReferenceSystem(double a, double f) {
	    _a = a;
	    _f = f <= 1 ? f : 1/f;
	    _f1 = 1 - _f;
	    _e2 = _f * (2 - _f);
	    _ep2 = _e2 / sq(_f1);       // e2 / (1 - e2)
	    _n = _f / ( 2 - _f);
	    _b = _a * _f1;
	    _c2 = (sq(_a) + sq(_b) *
	           (_e2 == 0 ? 1 :
	            (_e2 > 0 ? atanh(Math.sqrt(_e2)) :
	             Math.atan(Math.sqrt(-_e2))) /
	            Math.sqrt(Math.abs(_e2))))/2; // authalic radius squared
	    // The sig12 threshold for "really short".  Using the auxiliary sphere
	    // solution with dnm computed at (bet1 + bet2) / 2, the relative error in
	    // the azimuth consistency check is sig12^2 * abs(f) * min(1, 1-f/2) / 2.
	    // (Error measured for 1/100 < b/a < 100 and abs(f) >= 1/1000.  For a
	    // given f and sig12, the max error occurs for lines near the pole.  If
	    // the old rule for computing dnm = (dn1 + dn2)/2 is used, then the error
	    // increases by a factor of 2.)  Setting this equal to epsilon gives
	    // sig12 = etol2.  Here 0.1 is a safety factor (error decreased by 100)
	    // and max(0.001, abs(f)) stops etol2 getting too large in the nearly
	    // spherical case.
	    _etol2 = 0.1 * tol2_ /
	              Math.sqrt( Math.max(0.001, Math.abs(_f)) *
	                         Math.min(1.0, 1 - _f/2) / 2 );
	    if (!(isfinite(_a) && _a > 0))
	      throw new RuntimeException("Major radius is not positive");
	    if (!(isfinite(_b) && _b > 0))
	      throw new RuntimeException("Minor radius is not positive");
	    _A3x = new double[nA3x_];
	    _C3x = new double[nC3x_];
	    _C4x = new double[nC4x_];

	    A3coeff();
	    C3coeff();
	    C4coeff();
	  }

	  /**
	   * Solve the direct geodesic problem where the length of the geodesic
	   * is specified in terms of distance.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param azi1 azimuth at point 1 (degrees).
	   * @param s12 distance between point 1 and point 2 (meters); it can be
	   *   negative.
	   * @return a {@link GeodesicData} object with the following fields:
	   *   <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, <i>lat2</i>, <i>lon2</i>,
	   *   <i>azi2</i>, <i>s12</i>, <i>a12</i>.
	   * <p>
	   * <i>lat1</i> should be in the range [&minus;90&deg;, 90&deg;]; <i>lon1</i>
	   * and <i>azi1</i> should be in the range [&minus;540&deg;, 540&deg;).  The
	   * values of <i>lon2</i> and <i>azi2</i> returned are in the range
	   * [&minus;180&deg;, 180&deg;).
	   * <p>
	   * If either point is at a pole, the azimuth is defined by keeping the
	   * longitude fixed, writing <i>lat</i> = &plusmn;(90&deg; &minus; &epsilon;),
	   * and taking the limit &epsilon; &rarr; 0+.  An arc length greater that
	   * 180&deg; signifies a geodesic which is not a shortest path.  (For a
	   * prolate ellipsoid, an additional condition is necessary for a shortest
	   * path: the longitudinal extent must not exceed of 180&deg;.)
	   **********************************************************************/
	  public Geodesic Direct(double lat1, double lon1,
	                             double azi1, double s12) {
	    return Direct(lat1, lon1, azi1, false, s12,
	                  LATITUDE | LONGITUDE |
	                  AZIMUTH);
	  }
	  /**
	   * Solve the direct geodesic problem where the length of the geodesic is
	   * specified in terms of distance and with a subset of the geodesic results
	   * returned.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param azi1 azimuth at point 1 (degrees).
	   * @param s12 distance between point 1 and point 2 (meters); it can be
	   *   negative.
	   * @param outmask a bitor'ed combination of {@link GeodesicMask} values
	   *   specifying which results should be returned.
	   * @return a {@link GeodesicData} object with the fields specified by
	   *   <i>outmask</i> computed.
	   * <p>
	   * <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, <i>s12</i>, and <i>a12</i> are
	   * always included in the returned result.  The value of <i>lon2</i> returned
	   * is in the range [&minus;180&deg;, 180&deg;), unless the <i>outmask</i>
	   * includes the {@link GeodesicMask#LONG_UNROLL} flag.
	   **********************************************************************/
	  public Geodesic Direct(double lat1, double lon1,
	                             double azi1, double s12, int outmask) {
	    return Direct(lat1, lon1, azi1, false, s12, outmask);
	  }

	  /**
	   * Solve the direct geodesic problem where the length of the geodesic
	   * is specified in terms of arc length.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param azi1 azimuth at point 1 (degrees).
	   * @param a12 arc length between point 1 and point 2 (degrees); it can
	   *   be negative.
	   * @return a {@link GeodesicData} object with the following fields:
	   *   <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, <i>lat2</i>, <i>lon2</i>,
	   *   <i>azi2</i>, <i>s12</i>, <i>a12</i>.
	   * <p>
	   * <i>lat1</i> should be in the range [&minus;90&deg;, 90&deg;]; <i>lon1</i>
	   * and <i>azi1</i> should be in the range [&minus;540&deg;, 540&deg;).  The
	   * values of <i>lon2</i> and <i>azi2</i> returned are in the range
	   * [&minus;180&deg;, 180&deg;).
	   * <p>
	   * If either point is at a pole, the azimuth is defined by keeping the
	   * longitude fixed, writing <i>lat</i> = &plusmn;(90&deg; &minus; &epsilon;),
	   * and taking the limit &epsilon; &rarr; 0+.  An arc length greater that
	   * 180&deg; signifies a geodesic which is not a shortest path.  (For a
	   * prolate ellipsoid, an additional condition is necessary for a shortest
	   * path: the longitudinal extent must not exceed of 180&deg;.)
	   **********************************************************************/
	  public Geodesic ArcDirect(double lat1, double lon1,
	                                double azi1, double a12) {
	    return Direct(lat1, lon1, azi1, true, a12,
	                  LATITUDE | LONGITUDE |
	                  AZIMUTH | DISTANCE);
	  }

	  /**
	   * Solve the direct geodesic problem where the length of the geodesic is
	   * specified in terms of arc length and with a subset of the geodesic results
	   * returned.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param azi1 azimuth at point 1 (degrees).
	   * @param a12 arc length between point 1 and point 2 (degrees); it can
	   *   be negative.
	   * @param outmask a bitor'ed combination of {@link GeodesicMask} values
	   *   specifying which results should be returned.
	   * @return a {@link GeodesicData} object with the fields specified by
	   *   <i>outmask</i> computed.
	   * <p>
	   * <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, and <i>a12</i> are always included
	   * in the returned result.  The value of <i>lon2</i> returned is in the range
	   * [&minus;180&deg;, 180&deg;), unless the <i>outmask</i> includes the {@link
	   * GeodesicMask#LONG_UNROLL} flag.
	   **********************************************************************/
	  public Geodesic ArcDirect(double lat1, double lon1,
	                                double azi1, double a12, int outmask) {
	    return Direct(lat1, lon1, azi1, true, a12, outmask);
	  }

	  /**
	   * The general direct geodesic problem.  {@link #Direct Direct} and
	   * {@link #ArcDirect ArcDirect} are defined in terms of this function.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param azi1 azimuth at point 1 (degrees).
	   * @param arcmode boolean flag determining the meaning of the
	   *   <i>s12_a12</i>.
	   * @param s12_a12 if <i>arcmode</i> is false, this is the distance between
	   *   point 1 and point 2 (meters); otherwise it is the arc length between
	   *   point 1 and point 2 (degrees); it can be negative.
	   * @param outmask a bitor'ed combination of {@link GeodesicMask} values
	   *   specifying which results should be returned.
	   * @return a {@link GeodesicData} object with the fields specified by
	   *   <i>outmask</i> computed.
	   * <p>
	   * The {@link GeodesicMask} values possible for <i>outmask</i> are
	   * <ul>
	   * <li>
	   *   <i>outmask</i> |= LATITUDE for the latitude <i>lat2</i>;
	   * <li>
	   *   <i>outmask</i> |= LONGITUDE for the latitude <i>lon2</i>;
	   * <li>
	   *   <i>outmask</i> |= AZIMUTH for the latitude <i>azi2</i>;
	   * <li>
	   *   <i>outmask</i> |= DISTANCE for the distance <i>s12</i>;
	   * <li>
	   *   <i>outmask</i> |= REDUCEDLENGTH for the reduced length
	   *   <i>m12</i>;
	   * <li>
	   *   <i>outmask</i> |= GEODESICSCALE for the geodesic scales
	   *   <i>M12</i> and <i>M21</i>;
	   * <li>
	   *   <i>outmask</i> |= AREA for the area <i>S12</i>;
	   * <li>
	   *   <i>outmask</i> |= ALL for all of the above;
	   * <li>
	   *   <i>outmask</i> |= LONG_UNROLL to unroll <i>lon2</i>
	   *   (instead of reducing it to the range [&minus;180&deg;, 180&deg;)).
	   * </ul>
	   * <p>
	   * The function value <i>a12</i> is always computed and returned and this
	   * equals <i>s12_a12</i> is <i>arcmode</i> is true.  If <i>outmask</i>
	   * includes {@link GeodesicMask#DISTANCE} and <i>arcmode</i> is false, then
	   * <i>s12</i> = <i>s12_a12</i>.  It is not necessary to include {@link
	   * GeodesicMask#DISTANCE_IN} in <i>outmask</i>; this is automatically
	   * included is <i>arcmode</i> is false.
	   **********************************************************************/
	  public Geodesic Direct(double lat1, double lon1, double azi1,
	                             boolean arcmode, double s12_a12, int outmask) {
		return new GeodesicPath(this, lat1, lon1, azi1,
	                            // Automatically supply DISTANCE_IN if necessary
	                            outmask | (arcmode ? NONE :
	                                       DISTANCE_IN))
	      .                         // Note the dot!
	      Position(arcmode, s12_a12, outmask);
	  }

	  /**
	   * Solve the inverse geodesic problem.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param lat2 latitude of point 2 (degrees).
	   * @param lon2 longitude of point 2 (degrees).
	   * @return a {@link GeodesicData} object with the following fields:
	   *   <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, <i>lat2</i>, <i>lon2</i>,
	   *   <i>azi2</i>, <i>s12</i>, <i>a12</i>.
	   * <p>
	   * <i>lat1</i> and <i>lat2</i> should be in the range [&minus;90&deg;,
	   * 90&deg;]; <i>lon1</i> and <i>lon2</i> should be in the range
	   * [&minus;540&deg;, 540&deg;).  The values of <i>azi1</i> and <i>azi2</i>
	   * returned are in the range [&minus;180&deg;, 180&deg;).
	   * <p>
	   * If either point is at a pole, the azimuth is defined by keeping the
	   * longitude fixed, writing <i>lat</i> = &plusmn;(90&deg; &minus; &epsilon;),
	   * taking the limit &epsilon; &rarr; 0+.
	   * <p>
	   * The solution to the inverse problem is found using Newton's method.  If
	   * this fails to converge (this is very unlikely in geodetic applications
	   * but does occur for very eccentric ellipsoids), then the bisection method
	   * is used to refine the solution.
	   **********************************************************************/
	  public Geodesic Inverse(double lat1, double lon1,
	                              double lat2, double lon2) {
	    return Inverse(lat1, lon1, lat2, lon2,
	                   DISTANCE | AZIMUTH);
	  }

	  /**
	   * Solve the inverse geodesic problem with a subset of the geodesic results
	   * returned.
	   * <p>
	   * @param lat1 latitude of point 1 (degrees).
	   * @param lon1 longitude of point 1 (degrees).
	   * @param lat2 latitude of point 2 (degrees).
	   * @param lon2 longitude of point 2 (degrees).
	   * @param outmask a bitor'ed combination of {@link GeodesicMask} values
	   *   specifying which results should be returned.
	   * @return a {@link GeodesicData} object with the fields specified by
	   *   <i>outmask</i> computed.
	   * <p>
	   * The {@link GeodesicMask} values possible for <i>outmask</i> are
	   * <ul>
	   * <li>
	   *   <i>outmask</i> |= DISTANCE for the distance <i>s12</i>;
	   * <li>
	   *   <i>outmask</i> |= AZIMUTH for the latitude <i>azi2</i>;
	   * <li>
	   *   <i>outmask</i> |= REDUCEDLENGTH for the reduced length
	   *   <i>m12</i>;
	   * <li>
	   *   <i>outmask</i> |= GEODESICSCALE for the geodesic scales
	   *   <i>M12</i> and <i>M21</i>;
	   * <li>
	   *   <i>outmask</i> |= AREA for the area <i>S12</i>;
	   * <li>
	   *   <i>outmask</i> |= ALL for all of the above.
	   * </ul>
	   * <p>
	   * <i>lat1</i>, <i>lon1</i>, <i>lat2</i>, <i>lon2</i>, and <i>a12</i> are
	   * always included in the returned result.
	   **********************************************************************/
	  public Geodesic Inverse(double lat1, double lon1,
	                              double lat2, double lon2, int outmask) {
	    outmask &= OUT_MASK;
	    Geodesic r = new Geodesic();
	    lon1 = AngNormalize(lon1);
	    lon2 = AngNormalize(lon2);
	    // Compute longitude difference (AngDiff does this carefully).  Result is
	    // in [-180, 180] but -180 is only for west-going geodesics.  180 is for
	    // east-going and meridional geodesics.
	    double lon12 = AngDiff(lon1, lon2);
	    // If very close to being on the same half-meridian, then make it so.
	    lon12 = AngRound(lon12);
	    // Make longitude difference positive.
	    int lonsign = lon12 >= 0 ? 1 : -1;
	    lon12 *= lonsign;
	    // If really close to the equator, treat as on equator.
	    lat1 = AngRound(lat1);
	    lat2 = AngRound(lat2);
	    // Save input parameters post normalization
	    r.lat1 = lat1; r.lon1 = lon1; r.lat2 = lat2; r.lon2 = lon2;
	    // Swap points so that point with higher (abs) latitude is point 1
	    int swapp = Math.abs(lat1) >= Math.abs(lat2) ? 1 : -1;
	    if (swapp < 0) {
	      lonsign *= -1;
	      { double t = lat1; lat1 = lat2; lat2 = t; }
	    }
	    // Make lat1 <= 0
	    int latsign = lat1 < 0 ? 1 : -1;
	    lat1 *= latsign;
	    lat2 *= latsign;
	    // Now we have
	    //
	    //     0 <= lon12 <= 180
	    //     -90 <= lat1 <= 0
	    //     lat1 <= lat2 <= -lat1
	    //
	    // longsign, swapp, latsign register the transformation to bring the
	    // coordinates to this canonical form.  In all cases, 1 means no change was
	    // made.  We make these transformations so that there are few cases to
	    // check, e.g., on verifying quadrants in atan2.  In addition, this
	    // enforces some symmetries in the results returned.

	    double phi, sbet1, cbet1, sbet2, cbet2, s12x, m12x;
	    s12x = m12x = Double.NaN;

	    phi = lat1 * degree;
	    // Ensure cbet1 = +epsilon at poles
	    sbet1 = _f1 * Math.sin(phi);
	    cbet1 = lat1 == -90 ? tiny_ : Math.cos(phi);
	    { Pair p = norm(sbet1, cbet1);
	      sbet1 = p.first; cbet1 = p.second; }

	    phi = lat2 * degree;
	    // Ensure cbet2 = +epsilon at poles
	    sbet2 = _f1 * Math.sin(phi);
	    cbet2 = Math.abs(lat2) == 90 ? tiny_ : Math.cos(phi);
	    { Pair p = norm(sbet2, cbet2);
	      sbet2 = p.first; cbet2 = p.second; }

	    // If cbet1 < -sbet1, then cbet2 - cbet1 is a sensitive measure of the
	    // |bet1| - |bet2|.  Alternatively (cbet1 >= -sbet1), abs(sbet2) + sbet1 is
	    // a better measure.  This logic is used in assigning calp2 in Lambda12.
	    // Sometimes these quantities vanish and in that case we force bet2 = +/-
	    // bet1 exactly.  An example where is is necessary is the inverse problem
	    // 48.522876735459 0 -48.52287673545898293 179.599720456223079643
	    // which failed with Visual Studio 10 (Release and Debug)

	    if (cbet1 < -sbet1) {
	      if (cbet2 == cbet1)
	        sbet2 = sbet2 < 0 ? sbet1 : -sbet1;
	    } else {
	      if (Math.abs(sbet2) == -sbet1)
	        cbet2 = cbet1;
	    }

	    double
	      dn1 = Math.sqrt(1 + _ep2 * sq(sbet1)),
	      dn2 = Math.sqrt(1 + _ep2 * sq(sbet2));

	    double
	      lam12 = lon12 * degree,
	      slam12 = Math.abs(lon12) == 180 ? 0 : Math.sin(lam12),
	      clam12 = Math.cos(lam12);      // lon12 == 90 isn't interesting

	    double a12, sig12, calp1, salp1, calp2, salp2;
	    a12 = sig12 = calp1 = salp1 = calp2 = salp2 = Double.NaN;
	    // index zero elements of these arrays are unused
	    double C1a[] = new double[nC1_ + 1];
	    double C2a[] = new double[nC2_ + 1];
	    double C3a[] = new double[nC3_];

	    boolean meridian = lat1 == -90 || slam12 == 0;

	    if (meridian) {

	      // Endpoints are on a single full meridian, so the geodesic might lie on
	      // a meridian.

	      calp1 = clam12; salp1 = slam12; // Head to the target longitude
	      calp2 = 1; salp2 = 0;           // At the target we're heading north

	      double
	        // tan(bet) = tan(sig) * cos(alp)
	        ssig1 = sbet1, csig1 = calp1 * cbet1,
	        ssig2 = sbet2, csig2 = calp2 * cbet2;

	      // sig12 = sig2 - sig1
	      sig12 = Math.atan2(Math.max(csig1 * ssig2 - ssig1 * csig2, 0.0),
	                    csig1 * csig2 + ssig1 * ssig2);
	      {
	        LengthsV v =
	          Lengths(_n, sig12, ssig1, csig1, dn1, ssig2, csig2, dn2,
	                  cbet1, cbet2,
	                  (outmask & GEODESICSCALE) != 0, C1a, C2a);
	        s12x = v.s12b; m12x = v.m12b;
	        if ((outmask & GEODESICSCALE) != 0) {
	          r.M12 = v.M12; r.M21 = v.M21;
	        }
	      }
	      // Add the check for sig12 since zero length geodesics might yield m12 <
	      // 0.  Test case was
	      //
	      //    echo 20.001 0 20.001 0 | GeodSolve -i
	      //
	      // In fact, we will have sig12 > pi/2 for meridional geodesic which is
	      // not a shortest path.
	      if (sig12 < 1 || m12x >= 0) {
	        m12x *= _b;
	        s12x *= _b;
	        a12 = sig12 / degree;
	      } else
	        // m12 < 0, i.e., prolate and too close to anti-podal
	        meridian = false;
	    }

	    double omg12 = Double.NaN;
	    if (!meridian &&
	        sbet1 == 0 &&   // and sbet2 == 0
	        // Mimic the way Lambda12 works with calp1 = 0
	        (_f <= 0 || lam12 <= Math.PI - _f * Math.PI)) {

	      // Geodesic runs along equator
	      calp1 = calp2 = 0; salp1 = salp2 = 1;
	      s12x = _a * lam12;
	      sig12 = omg12 = lam12 / _f1;
	      m12x = _b * Math.sin(sig12);
	      if ((outmask & GEODESICSCALE) != 0)
	        r.M12 = r.M21 = Math.cos(sig12);
	      a12 = lon12 / _f1;

	    } else if (!meridian) {

	      // Now point1 and point2 belong within a hemisphere bounded by a
	      // meridian and geodesic is neither meridional or equatorial.

	      // Figure a starting point for Newton's method
	      double dnm;
	      {
	        InverseStartV v =
	          InverseStart(sbet1, cbet1, dn1, sbet2, cbet2, dn2,
	                       lam12,
	                       C1a, C2a);
	        sig12 = v.sig12;
	        salp1 = v.salp1; calp1 = v.calp1;
	        salp2 = v.salp2; calp2 = v.calp2;
	        dnm = v.dnm;
	      }

	      if (sig12 >= 0) {
	        // Short lines (InverseStart sets salp2, calp2, dnm)
	        s12x = sig12 * _b * dnm;
	        m12x = sq(dnm) * _b * Math.sin(sig12 / dnm);
	        if ((outmask & GEODESICSCALE) != 0)
	          r.M12 = r.M21 = Math.cos(sig12 / dnm);
	        a12 = sig12 / degree;
	        omg12 = lam12 / (_f1 * dnm);
	      } else {

	        // Newton's method.  This is a straightforward solution of f(alp1) =
	        // lambda12(alp1) - lam12 = 0 with one wrinkle.  f(alp) has exactly one
	        // root in the interval (0, pi) and its derivative is positive at the
	        // root.  Thus f(alp) is positive for alp > alp1 and negative for alp <
	        // alp1.  During the course of the iteration, a range (alp1a, alp1b) is
	        // maintained which brackets the root and with each evaluation of
	        // f(alp) the range is shrunk, if possible.  Newton's method is
	        // restarted whenever the derivative of f is negative (because the new
	        // value of alp1 is then further from the solution) or if the new
	        // estimate of alp1 lies outside (0,pi); in this case, the new starting
	        // guess is taken to be (alp1a + alp1b) / 2.
	        double ssig1, csig1, ssig2, csig2, eps;
	        ssig1 = csig1 = ssig2 = csig2 = eps = Double.NaN;
	        int numit = 0;
	        // Bracketing range
	        double salp1a = tiny_, calp1a = 1, salp1b = tiny_, calp1b = -1;
	        for (boolean tripn = false, tripb = false; numit < maxit2_; ++numit) {
	          // the WGS84 test set: mean = 1.47, sd = 1.25, max = 16
	          // WGS84 and random input: mean = 2.85, sd = 0.60
	          double v, dv;
	          {
	            Lambda12V w =
	              Lambda12(sbet1, cbet1, dn1, sbet2, cbet2, dn2, salp1, calp1,
	                       numit < maxit1_, C1a, C2a, C3a);
	            v = w.lam12 - lam12;
	            salp2 = w.salp2; calp2 = w.calp2;
	            sig12 = w.sig12;
	            ssig1 = w.ssig1; csig1 = w.csig1;
	            ssig2 = w.ssig2; csig2 = w.csig2;
	            eps = w.eps; omg12 = w.domg12;
	            dv = w.dlam12;
	          }
	          // 2 * tol0 is approximately 1 ulp for a number in [0, pi].
	          // Reversed test to allow escape with NaNs
	          if (tripb || !(Math.abs(v) >= (tripn ? 8 : 2) * tol0_)) break;
	          // Update bracketing values
	          if (v > 0 && (numit > maxit1_ || calp1/salp1 > calp1b/salp1b))
	            { salp1b = salp1; calp1b = calp1; }
	          else if (v < 0 && (numit > maxit1_ || calp1/salp1 < calp1a/salp1a))
	            { salp1a = salp1; calp1a = calp1; }
	          if (numit < maxit1_ && dv > 0) {
	            double
	              dalp1 = -v/dv;
	            double
	              sdalp1 = Math.sin(dalp1), cdalp1 = Math.cos(dalp1),
	              nsalp1 = salp1 * cdalp1 + calp1 * sdalp1;
	            if (nsalp1 > 0 && Math.abs(dalp1) < Math.PI) {
	              calp1 = calp1 * cdalp1 - salp1 * sdalp1;
	              salp1 = nsalp1;
	              { Pair p = norm(salp1, calp1);
	                salp1 = p.first; calp1 = p.second; }
	              // In some regimes we don't get quadratic convergence because
	              // slope -> 0.  So use convergence conditions based on epsilon
	              // instead of sqrt(epsilon).
	              tripn = Math.abs(v) <= 16 * tol0_;
	              continue;
	            }
	          }
	          // Either dv was not postive or updated value was outside legal
	          // range.  Use the midpoint of the bracket as the next estimate.
	          // This mechanism is not needed for the WGS84 ellipsoid, but it does
	          // catch problems with more eccentric ellipsoids.  Its efficacy is
	          // such for the WGS84 test set with the starting guess set to alp1 =
	          // 90deg:
	          // the WGS84 test set: mean = 5.21, sd = 3.93, max = 24
	          // WGS84 and random input: mean = 4.74, sd = 0.99
	          salp1 = (salp1a + salp1b)/2;
	          calp1 = (calp1a + calp1b)/2;
	          { Pair p = norm(salp1, calp1);
	            salp1 = p.first; calp1 = p.second; }
	          tripn = false;
	          tripb = (Math.abs(salp1a - salp1) + (calp1a - calp1) < tolb_ ||
	                   Math.abs(salp1 - salp1b) + (calp1 - calp1b) < tolb_);
	        }
	        {
	          LengthsV v =
	            Lengths(eps, sig12, ssig1, csig1, dn1, ssig2, csig2, dn2,
	                    cbet1, cbet2,
	                    (outmask & GEODESICSCALE) != 0, C1a, C2a);
	          s12x = v.s12b; m12x = v.m12b;
	          if ((outmask & GEODESICSCALE) != 0) {
	            r.M12 = v.M12; r.M21 = v.M21;
	          }
	        }
	        m12x *= _b;
	        s12x *= _b;
	        a12 = sig12 / degree;
	        omg12 = lam12 - omg12;
	      }
	    }

	    if ((outmask & DISTANCE) != 0)
	      r.s12 = 0 + s12x;           // Convert -0 to 0

	    if ((outmask & REDUCEDLENGTH) != 0)
	      r.m12 = 0 + m12x;           // Convert -0 to 0

	    if ((outmask & AREA) != 0) {
	      double
	        // From Lambda12: sin(alp1) * cos(bet1) = sin(alp0)
	        salp0 = salp1 * cbet1,
	        calp0 = hypot(calp1, salp1 * sbet1); // calp0 > 0
	      double alp12;
	      if (calp0 != 0 && salp0 != 0) {
	        double
	          // From Lambda12: tan(bet) = tan(sig) * cos(alp)
	          ssig1 = sbet1, csig1 = calp1 * cbet1,
	          ssig2 = sbet2, csig2 = calp2 * cbet2,
	          k2 = sq(calp0) * _ep2,
	          eps = k2 / (2 * (1 + Math.sqrt(1 + k2)) + k2),
	          // Multiplier = a^2 * e^2 * cos(alpha0) * sin(alpha0).
	          A4 = sq(_a) * calp0 * salp0 * _e2;
	        { Pair p = norm(ssig1, csig1);
	          ssig1 = p.first; csig1 = p.second; }
	        { Pair p = norm(ssig2, csig2);
	          ssig2 = p.first; csig2 = p.second; }
	        double C4a[] = new double[nC4_];
	        C4f(eps, C4a);
	        double
	          B41 = SinCosSeries(false, ssig1, csig1, C4a),
	          B42 = SinCosSeries(false, ssig2, csig2, C4a);
	        r.S12 = A4 * (B42 - B41);
	      } else
	        // Avoid problems with indeterminate sig1, sig2 on equator
	        r.S12 = 0;

	      if (!meridian &&
	          omg12 < 0.75 * Math.PI && // Long difference too big
	          sbet2 - sbet1 < 1.75) {            // Lat difference too big
	        // Use tan(Gamma/2) = tan(omg12/2)
	        // * (tan(bet1/2)+tan(bet2/2))/(1+tan(bet1/2)*tan(bet2/2))
	        // with tan(x/2) = sin(x)/(1+cos(x))
	        double
	          somg12 = Math.sin(omg12), domg12 = 1 + Math.cos(omg12),
	          dbet1 = 1 + cbet1, dbet2 = 1 + cbet2;
	        alp12 = 2 * Math.atan2( somg12 * ( sbet1 * dbet2 + sbet2 * dbet1 ),
	                           domg12 * ( sbet1 * sbet2 + dbet1 * dbet2 ) );
	      } else {
	        // alp12 = alp2 - alp1, used in atan2 so no need to normalize
	        double
	          salp12 = salp2 * calp1 - calp2 * salp1,
	          calp12 = calp2 * calp1 + salp2 * salp1;
	        // The right thing appears to happen if alp1 = +/-180 and alp2 = 0, viz
	        // salp12 = -0 and alp12 = -180.  However this depends on the sign
	        // being attached to 0 correctly.  The following ensures the correct
	        // behavior.
	        if (salp12 == 0 && calp12 < 0) {
	          salp12 = tiny_ * calp1;
	          calp12 = -1;
	        }
	        alp12 = Math.atan2(salp12, calp12);
	      }
	      r.S12 += _c2 * alp12;
	      r.S12 *= swapp * lonsign * latsign;
	      // Convert -0 to 0
	      r.S12 += 0;
	    }

	    // Convert calp, salp to azimuth accounting for lonsign, swapp, latsign.
	    if (swapp < 0) {
	      { double t = salp1; salp1 = salp2; salp2 = t; }
	      { double t = calp1; calp1 = calp2; calp2 = t; }
	      if ((outmask & GEODESICSCALE) != 0)
	        { double t = r.M12; r.M12 = r.M21; r.M21 = t; }
	    }

	    salp1 *= swapp * lonsign; calp1 *= swapp * latsign;
	    salp2 *= swapp * lonsign; calp2 *= swapp * latsign;

	    if ((outmask & AZIMUTH) != 0) {
	      // minus signs give range [-180, 180). 0- converts -0 to +0.
	      r.azi1 = 0 - Math.atan2(-salp1, calp1) / degree;
	      r.azi2 = 0 - Math.atan2(-salp2, calp2) / degree;
	    }
	    // Returned value in [0, 180]
	    r.a12 = a12;
	    return r;
	  }

	  /**
	   * @return <i>a</i> the equatorial radius of the ellipsoid (meters).  This is
	   *   the value used in the constructor.
	   **********************************************************************/
	  public double MajorRadius() { return _a; }

	  /**
	   * @return <i>f</i> the  flattening of the ellipsoid.  This is the
	   *   value used in the constructor.
	   **********************************************************************/
	  public double Flattening() { return _f; }

	  /**
	   * @return total area of ellipsoid in meters<sup>2</sup>.  The area of a
	   *   polygon encircling a pole can be found by adding EllipsoidArea()/2 to
	   *   the sum of <i>S12</i> for each side of the polygon.
	   **********************************************************************/
	  public double EllipsoidArea() { return 4 * Math.PI * _c2; }


	  // This is a reformulation of the geodesic problem.  The notation is as
	  // follows:
	  // - at a general point (no suffix or 1 or 2 as suffix)
	  //   - phi = latitude
	  //   - beta = latitude on auxiliary sphere
	  //   - omega = longitude on auxiliary sphere
	  //   - lambda = longitude
	  //   - alpha = azimuth of great circle
	  //   - sigma = arc length along great circle
	  //   - s = distance
	  //   - tau = scaled distance (= sigma at multiples of pi/2)
	  // - at northwards equator crossing
	  //   - beta = phi = 0
	  //   - omega = lambda = 0
	  //   - alpha = alpha0
	  //   - sigma = s = 0
	  // - a 12 suffix means a difference, e.g., s12 = s2 - s1.
	  // - s and c prefixes mean sin and cos

	  protected static double SinCosSeries(boolean sinp,
	                                       double sinx, double cosx,
	                                       double c[]) {
	    // Evaluate
	    // y = sinp ? sum(c[i] * sin( 2*i    * x), i, 1, n) :
	    //            sum(c[i] * cos((2*i+1) * x), i, 0, n-1)
	    // using Clenshaw summation.  N.B. c[0] is unused for sin series
	    // Approx operation count = (n + 5) mult and (2 * n + 2) add
	    int
	      k = c.length,             // Point to one beyond last element
	      n = k - (sinp ? 1 : 0);
	    double
	      ar = 2 * (cosx - sinx) * (cosx + sinx), // 2 * cos(2 * x)
	      y0 = (n & 1) != 0 ? c[--k] : 0, y1 = 0;        // accumulators for sum
	    // Now n is even
	    n /= 2;
	    while (n-- != 0) {
	      // Unroll loop x 2, so accumulators return to their original role
	      y1 = ar * y0 - y1 + c[--k];
	      y0 = ar * y1 - y0 + c[--k];
	    }
	    return sinp
	      ? 2 * sinx * cosx * y0    // sin(2 * x) * y0
	      : cosx * (y0 - y1);       // cos(x) * (y0 - y1)
	  }

	  private class LengthsV {
	    private double s12b, m12b, m0, M12, M21;
	    private LengthsV() {
	      s12b = m12b = m0 = M12 = M21 = Double.NaN;
	    }
	  }

	  private LengthsV Lengths(double eps, double sig12,
	                           double ssig1, double csig1, double dn1,
	                           double ssig2, double csig2, double dn2,
	                           double cbet1, double cbet2,
	                           boolean scalep,
	                           // Scratch areas of the right size
	                           double C1a[], double C2a[]) {
	    // Return m12b = (reduced length)/_b; also calculate s12b = distance/_b,
	    // and m0 = coefficient of secular term in expression for reduced length.
	    LengthsV v = new LengthsV(); // To hold s12b, m12b, m0, M12, M21;
	    C1f(eps, C1a);
	    C2f(eps, C2a);
	    double
	      A1m1 = A1m1f(eps),
	      AB1 = (1 + A1m1) * (SinCosSeries(true, ssig2, csig2, C1a) -
	                          SinCosSeries(true, ssig1, csig1, C1a)),
	      A2m1 = A2m1f(eps),
	      AB2 = (1 + A2m1) * (SinCosSeries(true, ssig2, csig2, C2a) -
	                          SinCosSeries(true, ssig1, csig1, C2a));
	    v.m0 = A1m1 - A2m1;
	    double J12 = v.m0 * sig12 + (AB1 - AB2);
	    // Missing a factor of _b.
	    // Add parens around (csig1 * ssig2) and (ssig1 * csig2) to ensure accurate
	    // cancellation in the case of coincident points.
	    v.m12b = dn2 * (csig1 * ssig2) - dn1 * (ssig1 * csig2) -
	      csig1 * csig2 * J12;
	    // Missing a factor of _b
	    v.s12b = (1 + A1m1) * sig12 + AB1;
	    if (scalep) {
	      double csig12 = csig1 * csig2 + ssig1 * ssig2;
	      double t = _ep2 * (cbet1 - cbet2) * (cbet1 + cbet2) / (dn1 + dn2);
	      v.M12 = csig12 + (t * ssig2 - csig2 * J12) * ssig1 / dn1;
	      v.M21 = csig12 - (t * ssig1 - csig1 * J12) * ssig2 / dn2;
	    }
	    return v;
	  }

	  private static double Astroid(double x, double y) {
	    // Solve k^4+2*k^3-(x^2+y^2-1)*k^2-2*y^2*k-y^2 = 0 for positive root k.
	    // This solution is adapted from Geocentric::Reverse.
	    double k;
	    double
	      p = sq(x),
	      q = sq(y),
	      r = (p + q - 1) / 6;
	    if ( !(q == 0 && r <= 0) ) {
	      double
	        // Avoid possible division by zero when r = 0 by multiplying equations
	        // for s and t by r^3 and r, resp.
	        S = p * q / 4,            // S = r^3 * s
	        r2 = sq(r),
	        r3 = r * r2,
	        // The discrimant of the quadratic equation for T3.  This is zero on
	        // the evolute curve p^(1/3)+q^(1/3) = 1
	        disc = S * (S + 2 * r3);
	      double u = r;
	      if (disc >= 0) {
	        double T3 = S + r3;
	        // Pick the sign on the sqrt to maximize abs(T3).  This minimizes loss
	        // of precision due to cancellation.  The result is unchanged because
	        // of the way the T is used in definition of u.
	        T3 += T3 < 0 ? -Math.sqrt(disc) : Math.sqrt(disc); // T3 = (r * t)^3
	        // N.B. cbrt always returns the double root.  cbrt(-8) = -2.
	        double T = cbrt(T3); // T = r * t
	        // T can be zero; but then r2 / T -> 0.
	        u += T + (T != 0 ? r2 / T : 0);
	      } else {
	        // T is complex, but the way u is defined the result is double.
	        double ang = Math.atan2(Math.sqrt(-disc), -(S + r3));
	        // There are three possible cube roots.  We choose the root which
	        // avoids cancellation.  Note that disc < 0 implies that r < 0.
	        u += 2 * r * Math.cos(ang / 3);
	      }
	      double
	        v = Math.sqrt(sq(u) + q),    // guaranteed positive
	        // Avoid loss of accuracy when u < 0.
	        uv = u < 0 ? q / (v - u) : u + v, // u+v, guaranteed positive
	        w = (uv - q) / (2 * v);           // positive?
	      // Rearrange expression for k to avoid loss of accuracy due to
	      // subtraction.  Division by 0 not possible because uv > 0, w >= 0.
	      k = uv / (Math.sqrt(uv + sq(w)) + w);   // guaranteed positive
	    } else {               // q == 0 && r <= 0
	      // y = 0 with |x| <= 1.  Handle this case directly.
	      // for y small, positive root is k = abs(y)/sqrt(1-x^2)
	      k = 0;
	    }
	    return k;
	  }

	  private class InverseStartV {
	    private double sig12, salp1, calp1,
	    // Only updated if return val >= 0
	      salp2, calp2,
	    // Only updated for short lines
	      dnm;
	    private InverseStartV() {
	      sig12 = salp1 = calp1 = salp2 = calp2 = dnm = Double.NaN;
	    }
	  }

	  private InverseStartV InverseStart(double sbet1, double cbet1, double dn1,
	                                     double sbet2, double cbet2, double dn2,
	                                     double lam12,
	                                     // Scratch areas of the right size
	                                     double C1a[], double C2a[]) {
	    // Return a starting point for Newton's method in salp1 and calp1 (function
	    // value is -1).  If Newton's method doesn't need to be used, return also
	    // salp2 and calp2 and function value is sig12.

	    // To hold sig12, salp1, calp1, salp2, calp2, dnm.
	    InverseStartV w = new InverseStartV();
	    w.sig12 = -1;               // Return value
	    double
	      // bet12 = bet2 - bet1 in [0, pi); bet12a = bet2 + bet1 in (-pi, 0]
	      sbet12 = sbet2 * cbet1 - cbet2 * sbet1,
	      cbet12 = cbet2 * cbet1 + sbet2 * sbet1;
	    double sbet12a = sbet2 * cbet1 + cbet2 * sbet1;
	    boolean shortline = cbet12 >= 0 && sbet12 < 0.5 &&
	      cbet2 * lam12 < 0.5;
	    double omg12 = lam12;
	    if (shortline) {
	      double sbetm2 = sq(sbet1 + sbet2);
	      // sin((bet1+bet2)/2)^2
	      // =  (sbet1 + sbet2)^2 / ((sbet1 + sbet2)^2 + (cbet1 + cbet2)^2)
	      sbetm2 /= sbetm2 + sq(cbet1 + cbet2);
	      w.dnm = Math.sqrt(1 + _ep2 * sbetm2);
	      omg12 /= _f1 * w.dnm;
	    }
	    double somg12 = Math.sin(omg12), comg12 = Math.cos(omg12);

	    w.salp1 = cbet2 * somg12;
	    w.calp1 = comg12 >= 0 ?
	      sbet12 + cbet2 * sbet1 * sq(somg12) / (1 + comg12) :
	      sbet12a - cbet2 * sbet1 * sq(somg12) / (1 - comg12);

	    double
	      ssig12 = hypot(w.salp1, w.calp1),
	      csig12 = sbet1 * sbet2 + cbet1 * cbet2 * comg12;

	    if (shortline && ssig12 < _etol2) {
	      // really short lines
	      w.salp2 = cbet1 * somg12;
	      w.calp2 = sbet12 - cbet1 * sbet2 *
	        (comg12 >= 0 ? sq(somg12) / (1 + comg12) : 1 - comg12);
	      { Pair p = norm(w.salp2, w.calp2);
	        w.salp2 = p.first; w.calp2 = p.second; }
	      // Set return value
	      w.sig12 = Math.atan2(ssig12, csig12);
	    } else if (Math.abs(_n) > 0.1 || // Skip astroid calc if too eccentric
	               csig12 >= 0 ||
	               ssig12 >= 6 * Math.abs(_n) * Math.PI * sq(cbet1)) {
	      // Nothing to do, zeroth order spherical approximation is OK
	    } else {
	      // Scale lam12 and bet2 to x, y coordinate system where antipodal point
	      // is at origin and singular point is at y = 0, x = -1.
	      double y, lamscale, betscale;
	      // In C++ volatile declaration needed to fix inverse case
	      // 56.320923501171 0 -56.320923501171 179.664747671772880215
	      // which otherwise fails with g++ 4.4.4 x86 -O3
	      double x;
	      if (_f >= 0) {            // In fact f == 0 does not get here
	        // x = dlong, y = dlat
	        {
	          double
	            k2 = sq(sbet1) * _ep2,
	            eps = k2 / (2 * (1 + Math.sqrt(1 + k2)) + k2);
	          lamscale = _f * cbet1 * A3f(eps) * Math.PI;
	        }
	        betscale = lamscale * cbet1;

	        x = (lam12 - Math.PI) / lamscale;
	        y = sbet12a / betscale;
	      } else {                  // _f < 0
	        // x = dlat, y = dlong
	        double
	          cbet12a = cbet2 * cbet1 - sbet2 * sbet1,
	          bet12a = Math.atan2(sbet12a, cbet12a);
	        double m12b, m0;
	        // In the case of lon12 = 180, this repeats a calculation made in
	        // Inverse.
	        LengthsV v =
	          Lengths(_n, Math.PI + bet12a,
	                  sbet1, -cbet1, dn1, sbet2, cbet2, dn2,
	                  cbet1, cbet2, false, C1a, C2a);
	        m12b = v.m12b; m0 = v.m0;

	        x = -1 + m12b / (cbet1 * cbet2 * m0 * Math.PI);
	        betscale = x < -0.01 ? sbet12a / x :
	          -_f * sq(cbet1) * Math.PI;
	        lamscale = betscale / cbet1;
	        y = (lam12 - Math.PI) / lamscale;
	      }

	      if (y > -tol1_ && x > -1 - xthresh_) {
	        // strip near cut
	        if (_f >= 0) {
	          w.salp1 = Math.min(1.0, -x);
	          w.calp1 = - Math.sqrt(1 - sq(w.salp1));
	        } else {
	          w.calp1 = Math.max(x > -tol1_ ? 0.0 : -1.0, x);
	          w.salp1 = Math.sqrt(1 - sq(w.calp1));
	        }
	      } else {
	        // Estimate alp1, by solving the astroid problem.
	        //
	        // Could estimate alpha1 = theta + pi/2, directly, i.e.,
	        //   calp1 = y/k; salp1 = -x/(1+k);  for _f >= 0
	        //   calp1 = x/(1+k); salp1 = -y/k;  for _f < 0 (need to check)
	        //
	        // However, it's better to estimate omg12 from astroid and use
	        // spherical formula to compute alp1.  This reduces the mean number of
	        // Newton iterations for astroid cases from 2.24 (min 0, max 6) to 2.12
	        // (min 0 max 5).  The changes in the number of iterations are as
	        // follows:
	        //
	        // change percent
	        //    1       5
	        //    0      78
	        //   -1      16
	        //   -2       0.6
	        //   -3       0.04
	        //   -4       0.002
	        //
	        // The histogram of iterations is (m = number of iterations estimating
	        // alp1 directly, n = number of iterations estimating via omg12, total
	        // number of trials = 148605):
	        //
	        //  iter    m      n
	        //    0   148    186
	        //    1 13046  13845
	        //    2 93315 102225
	        //    3 36189  32341
	        //    4  5396      7
	        //    5   455      1
	        //    6    56      0
	        //
	        // Because omg12 is near pi, estimate work with omg12a = pi - omg12
	        double k = Astroid(x, y);
	        double
	          omg12a = lamscale * ( _f >= 0 ? -x * k/(1 + k) : -y * (1 + k)/k );
	        somg12 = Math.sin(omg12a); comg12 = -Math.cos(omg12a);
	        // Update spherical estimate of alp1 using omg12 instead of lam12
	        w.salp1 = cbet2 * somg12;
	        w.calp1 = sbet12a - cbet2 * sbet1 * sq(somg12) / (1 - comg12);
	      }
	    }
	    // Sanity check on starting guess.  Backwards check allows NaN through.
	    if (!(w.salp1 <= 0))
	      { Pair p = norm(w.salp1, w.calp1);
	        w.salp1 = p.first; w.calp1 = p.second; }
	    else {
	      w.salp1 = 1; w.calp1 = 0;
	    }
	    return w;
	  }

	  private class Lambda12V {
	    private double lam12, salp2, calp2, sig12, ssig1, csig1, ssig2, csig2,
	      eps, domg12, dlam12;
	    private Lambda12V() {
	      lam12 = salp2 = calp2 = sig12 = ssig1 = csig1 = ssig2 = csig2
	        = eps = domg12 = dlam12 = Double.NaN;
	    }
	  }

	  private Lambda12V Lambda12(double sbet1, double cbet1, double dn1,
	                             double sbet2, double cbet2, double dn2,
	                             double salp1, double calp1,
	                             boolean diffp,
	                             // Scratch areas of the right size
	                             double C1a[], double C2a[], double C3a[]) {
	    // Object to hold lam12, salp2, calp2, sig12, ssig1, csig1, ssig2, csig2,
	    // eps, domg12, dlam12;

	    Lambda12V w = new Lambda12V();

	    if (sbet1 == 0 && calp1 == 0)
	      // Break degeneracy of equatorial line.  This case has already been
	      // handled.
	      calp1 = -tiny_;

	    double
	      // sin(alp1) * cos(bet1) = sin(alp0)
	      salp0 = salp1 * cbet1,
	      calp0 = hypot(calp1, salp1 * sbet1); // calp0 > 0

	    double somg1, comg1, somg2, comg2, omg12;
	    // tan(bet1) = tan(sig1) * cos(alp1)
	    // tan(omg1) = sin(alp0) * tan(sig1) = tan(omg1)=tan(alp1)*sin(bet1)
	    w.ssig1 = sbet1; somg1 = salp0 * sbet1;
	    w.csig1 = comg1 = calp1 * cbet1;
	    { Pair p = norm(w.ssig1, w.csig1);
	      w.ssig1 = p.first; w.csig1 = p.second; }
	    // norm(somg1, comg1); -- don't need to normalize!

	    // Enforce symmetries in the case abs(bet2) = -bet1.  Need to be careful
	    // about this case, since this can yield singularities in the Newton
	    // iteration.
	    // sin(alp2) * cos(bet2) = sin(alp0)
	    w.salp2 = cbet2 != cbet1 ? salp0 / cbet2 : salp1;
	    // calp2 = sqrt(1 - sq(salp2))
	    //       = sqrt(sq(calp0) - sq(sbet2)) / cbet2
	    // and subst for calp0 and rearrange to give (choose positive sqrt
	    // to give alp2 in [0, pi/2]).
	    w.calp2 = cbet2 != cbet1 || Math.abs(sbet2) != -sbet1 ?
	      Math.sqrt(sq(calp1 * cbet1) +
	           (cbet1 < -sbet1 ?
	            (cbet2 - cbet1) * (cbet1 + cbet2) :
	            (sbet1 - sbet2) * (sbet1 + sbet2))) / cbet2 :
	      Math.abs(calp1);
	    // tan(bet2) = tan(sig2) * cos(alp2)
	    // tan(omg2) = sin(alp0) * tan(sig2).
	    w.ssig2 = sbet2; somg2 = salp0 * sbet2;
	    w.csig2 = comg2 = w.calp2 * cbet2;
	    { Pair p = norm(w.ssig2, w.csig2);
	      w.ssig2 = p.first; w.csig2 = p.second; }
	    // norm(somg2, comg2); -- don't need to normalize!

	    // sig12 = sig2 - sig1, limit to [0, pi]
	    w.sig12 = Math.atan2(Math.max(w.csig1 * w.ssig2 - w.ssig1 * w.csig2, 0.0),
	                  w.csig1 * w.csig2 + w.ssig1 * w.ssig2);

	    // omg12 = omg2 - omg1, limit to [0, pi]
	    omg12 = Math.atan2(Math.max(comg1 * somg2 - somg1 * comg2, 0.0),
	                  comg1 * comg2 + somg1 * somg2);
	    double B312, h0;
	    double k2 = sq(calp0) * _ep2;
	    w.eps = k2 / (2 * (1 + Math.sqrt(1 + k2)) + k2);
	    C3f(w.eps, C3a);
	    B312 = (SinCosSeries(true, w.ssig2, w.csig2, C3a) -
	            SinCosSeries(true, w.ssig1, w.csig1, C3a));
	    h0 = -_f * A3f(w.eps);
	    w.domg12 = salp0 * h0 * (w.sig12 + B312);
	    w.lam12 = omg12 + w.domg12;

	    if (diffp) {
	      if (w.calp2 == 0)
	        w.dlam12 = - 2 * _f1 * dn1 / sbet1;
	      else {
	        double dummy;
	        LengthsV v =
	          Lengths(w.eps, w.sig12, w.ssig1, w.csig1, dn1, w.ssig2, w.csig2, dn2,
	                  cbet1, cbet2, false, C1a, C2a);
	        w.dlam12 = v.m12b;
	        w.dlam12 *= _f1 / (w.calp2 * cbet2);
	      }
	    }

	    return w;
	  }

	  protected double A3f(double eps) {
	    // Evaluate A3
	    return polyval(nA3_ - 1, _A3x, 0, eps);
	  }

	  protected void C3f(double eps, double c[]) {
	    // Evaluate C3 coeffs
	    // Elements c[1] thru c[nC3_ - 1] are set
	    double mult = 1;
	    int o = 0;
	    for (int l = 1; l < nC3_; ++l) { // l is index of C3[l]
	      int m = nC3_ - l - 1;          // order of polynomial in eps
	      mult *= eps;
	      c[l] = mult * polyval(m, _C3x, o, eps);
	      o += m + 1;
	    }
	  }

	  protected void C4f(double eps, double c[]) {
	    // Evaluate C4 coeffs
	    // Elements c[0] thru c[nC4_ - 1] are set
	    double mult = 1;
	    int o = 0;
	    for (int l = 0; l < nC4_; ++l) { // l is index of C4[l]
	      int m = nC4_ - l - 1;          // order of polynomial in eps
	      c[l] = mult * polyval(m, _C4x, o, eps);
	      o += m + 1;
	      mult *= eps;
	    }
	  }

	  // The scale factor A1-1 = mean value of (d/dsigma)I1 - 1
	  protected static double A1m1f(double eps) {
	    final double coeff[] = {
	      // (1-eps)*A1-1, polynomial in eps2 of order 3
	      1, 4, 64, 0, 256,
	    };
	    int m = nA1_/2;
	    double t = polyval(m, coeff, 0, sq(eps)) / coeff[m + 1];
	    return (t + eps) / (1 - eps);
	  }

	  // The coefficients C1[l] in the Fourier expansion of B1
	  protected static void C1f(double eps, double c[]) {
	    final double coeff[] = {
	      // C1[1]/eps^1, polynomial in eps2 of order 2
	      -1, 6, -16, 32,
	      // C1[2]/eps^2, polynomial in eps2 of order 2
	      -9, 64, -128, 2048,
	      // C1[3]/eps^3, polynomial in eps2 of order 1
	      9, -16, 768,
	      // C1[4]/eps^4, polynomial in eps2 of order 1
	      3, -5, 512,
	      // C1[5]/eps^5, polynomial in eps2 of order 0
	      -7, 1280,
	      // C1[6]/eps^6, polynomial in eps2 of order 0
	      -7, 2048,
	    };
	    double
	      eps2 = sq(eps),
	      d = eps;
	    int o = 0;
	    for (int l = 1; l <= nC1_; ++l) { // l is index of C1p[l]
	      int m = (nC1_ - l) / 2;         // order of polynomial in eps^2
	      c[l] = d * polyval(m, coeff, o, eps2) / coeff[o + m + 1];
	      o += m + 2;
	      d *= eps;
	    }
	  }

	  // The coefficients C1p[l] in the Fourier expansion of B1p
	  protected static void C1pf(double eps, double c[]) {
	    final double coeff[] = {
	      // C1p[1]/eps^1, polynomial in eps2 of order 2
	      205, -432, 768, 1536,
	      // C1p[2]/eps^2, polynomial in eps2 of order 2
	      4005, -4736, 3840, 12288,
	      // C1p[3]/eps^3, polynomial in eps2 of order 1
	      -225, 116, 384,
	      // C1p[4]/eps^4, polynomial in eps2 of order 1
	      -7173, 2695, 7680,
	      // C1p[5]/eps^5, polynomial in eps2 of order 0
	      3467, 7680,
	      // C1p[6]/eps^6, polynomial in eps2 of order 0
	      38081, 61440,
	    };
	    double
	      eps2 = sq(eps),
	      d = eps;
	    int o = 0;
	    for (int l = 1; l <= nC1p_; ++l) { // l is index of C1p[l]
	      int m = (nC1p_ - l) / 2;         // order of polynomial in eps^2
	      c[l] = d * polyval(m, coeff, o, eps2) / coeff[o + m + 1];
	      o += m + 2;
	      d *= eps;
	    }
	  }

	  // The scale factor A2-1 = mean value of (d/dsigma)I2 - 1
	  protected static double A2m1f(double eps) {
	    final double coeff[] = {
	      // A2/(1-eps)-1, polynomial in eps2 of order 3
	      25, 36, 64, 0, 256,
	    };
	    int m = nA2_/2;
	    double t = polyval(m, coeff, 0, sq(eps)) / coeff[m + 1];
	    return t * (1 - eps) - eps;
	  }

	  // The coefficients C2[l] in the Fourier expansion of B2
	  protected static void C2f(double eps, double c[]) {
	    final double coeff[] = {
	      // C2[1]/eps^1, polynomial in eps2 of order 2
	      1, 2, 16, 32,
	      // C2[2]/eps^2, polynomial in eps2 of order 2
	      35, 64, 384, 2048,
	      // C2[3]/eps^3, polynomial in eps2 of order 1
	      15, 80, 768,
	      // C2[4]/eps^4, polynomial in eps2 of order 1
	      7, 35, 512,
	      // C2[5]/eps^5, polynomial in eps2 of order 0
	      63, 1280,
	      // C2[6]/eps^6, polynomial in eps2 of order 0
	      77, 2048,
	    };
	    double
	      eps2 = sq(eps),
	      d = eps;
	    int o = 0;
	    for (int l = 1; l <= nC2_; ++l) { // l is index of C2[l]
	      int m = (nC2_ - l) / 2;         // order of polynomial in eps^2
	      c[l] = d * polyval(m, coeff, o, eps2) / coeff[o + m + 1];
	      o += m + 2;
	      d *= eps;
	    }
	  }

	  // The scale factor A3 = mean value of (d/dsigma)I3
	  protected void A3coeff() {
	    final double coeff[] = {
	      // A3, coeff of eps^5, polynomial in n of order 0
	      -3, 128,
	      // A3, coeff of eps^4, polynomial in n of order 1
	      -2, -3, 64,
	      // A3, coeff of eps^3, polynomial in n of order 2
	      -1, -3, -1, 16,
	      // A3, coeff of eps^2, polynomial in n of order 2
	      3, -1, -2, 8,
	      // A3, coeff of eps^1, polynomial in n of order 1
	      1, -1, 2,
	      // A3, coeff of eps^0, polynomial in n of order 0
	      1, 1,
	    };
	    int o = 0, k = 0;
	    for (int j = nA3_ - 1; j >= 0; --j) { // coeff of eps^j
	      int m = Math.min(nA3_ - j - 1, j);  // order of polynomial in n
	      _A3x[k++] = polyval(m, coeff, o, _n) / coeff[o + m + 1];
	      o += m + 2;
	    }
	  }

	  // The coefficients C3[l] in the Fourier expansion of B3
	  protected void C3coeff() {
	    final double coeff[] = {
	      // C3[1], coeff of eps^5, polynomial in n of order 0
	      3, 128,
	      // C3[1], coeff of eps^4, polynomial in n of order 1
	      2, 5, 128,
	      // C3[1], coeff of eps^3, polynomial in n of order 2
	      -1, 3, 3, 64,
	      // C3[1], coeff of eps^2, polynomial in n of order 2
	      -1, 0, 1, 8,
	      // C3[1], coeff of eps^1, polynomial in n of order 1
	      -1, 1, 4,
	      // C3[2], coeff of eps^5, polynomial in n of order 0
	      5, 256,
	      // C3[2], coeff of eps^4, polynomial in n of order 1
	      1, 3, 128,
	      // C3[2], coeff of eps^3, polynomial in n of order 2
	      -3, -2, 3, 64,
	      // C3[2], coeff of eps^2, polynomial in n of order 2
	      1, -3, 2, 32,
	      // C3[3], coeff of eps^5, polynomial in n of order 0
	      7, 512,
	      // C3[3], coeff of eps^4, polynomial in n of order 1
	      -10, 9, 384,
	      // C3[3], coeff of eps^3, polynomial in n of order 2
	      5, -9, 5, 192,
	      // C3[4], coeff of eps^5, polynomial in n of order 0
	      7, 512,
	      // C3[4], coeff of eps^4, polynomial in n of order 1
	      -14, 7, 512,
	      // C3[5], coeff of eps^5, polynomial in n of order 0
	      21, 2560,
	    };
	    int o = 0, k = 0;
	    for (int l = 1; l < nC3_; ++l) {        // l is index of C3[l]
	      for (int j = nC3_ - 1; j >= l; --j) { // coeff of eps^j
	        int m = Math.min(nC3_ - j - 1, j);  // order of polynomial in n
	        _C3x[k++] = polyval(m, coeff, o, _n) / coeff[o + m + 1];
	        o += m + 2;
	      }
	    }
	  }

	  protected void C4coeff() {
	    final double coeff[] = {
	      // C4[0], coeff of eps^5, polynomial in n of order 0
	      97, 15015,
	      // C4[0], coeff of eps^4, polynomial in n of order 1
	      1088, 156, 45045,
	      // C4[0], coeff of eps^3, polynomial in n of order 2
	      -224, -4784, 1573, 45045,
	      // C4[0], coeff of eps^2, polynomial in n of order 3
	      -10656, 14144, -4576, -858, 45045,
	      // C4[0], coeff of eps^1, polynomial in n of order 4
	      64, 624, -4576, 6864, -3003, 15015,
	      // C4[0], coeff of eps^0, polynomial in n of order 5
	      100, 208, 572, 3432, -12012, 30030, 45045,
	      // C4[1], coeff of eps^5, polynomial in n of order 0
	      1, 9009,
	      // C4[1], coeff of eps^4, polynomial in n of order 1
	      -2944, 468, 135135,
	      // C4[1], coeff of eps^3, polynomial in n of order 2
	      5792, 1040, -1287, 135135,
	      // C4[1], coeff of eps^2, polynomial in n of order 3
	      5952, -11648, 9152, -2574, 135135,
	      // C4[1], coeff of eps^1, polynomial in n of order 4
	      -64, -624, 4576, -6864, 3003, 135135,
	      // C4[2], coeff of eps^5, polynomial in n of order 0
	      8, 10725,
	      // C4[2], coeff of eps^4, polynomial in n of order 1
	      1856, -936, 225225,
	      // C4[2], coeff of eps^3, polynomial in n of order 2
	      -8448, 4992, -1144, 225225,
	      // C4[2], coeff of eps^2, polynomial in n of order 3
	      -1440, 4160, -4576, 1716, 225225,
	      // C4[3], coeff of eps^5, polynomial in n of order 0
	      -136, 63063,
	      // C4[3], coeff of eps^4, polynomial in n of order 1
	      1024, -208, 105105,
	      // C4[3], coeff of eps^3, polynomial in n of order 2
	      3584, -3328, 1144, 315315,
	      // C4[4], coeff of eps^5, polynomial in n of order 0
	      -128, 135135,
	      // C4[4], coeff of eps^4, polynomial in n of order 1
	      -2560, 832, 405405,
	      // C4[5], coeff of eps^5, polynomial in n of order 0
	      128, 99099,
	    };
	    int o = 0, k = 0;
	    for (int l = 0; l < nC4_; ++l) {        // l is index of C4[l]
	      for (int j = nC4_ - 1; j >= l; --j) { // coeff of eps^j
	        int m = nC4_ - j - 1;               // order of polynomial in n
	        _C4x[k++] = polyval(m, coeff, o, _n) / coeff[o + m + 1];
	        o += m + 2;
	      }
	    }
	  }

	  /**
	   * Square a number.
	   * <p>
	   * @param x the argument.
	   * @return <i>x</i><sup>2</sup>.
	   **********************************************************************/
	  public static double sq(double x) { return x * x; }

	  /**
	   * The hypotenuse function avoiding underflow and overflow.  In Java version
	   * 1.5 and later, Math.hypot can be used.
	   * <p>
	   * @param x the first argument.
	   * @param y the second argument.
	   * @return sqrt(<i>x</i><sup>2</sup> + <i>y</i><sup>2</sup>).
	   **********************************************************************/
	  public static double hypot(double x, double y) {
	    x = Math.abs(x); y = Math.abs(y);
	    double a = Math.max(x, y), b = Math.min(x, y) / (a != 0 ? a : 1);
	    return a * Math.sqrt(1 + b * b);
	    // For an alternative method see
	    // C. Moler and D. Morrision (1983) https://dx.doi.org/10.1147/rd.276.0577
	    // and A. A. Dubrulle (1983) https://dx.doi.org/10.1147/rd.276.0582
	  }

	  /**
	   * log(1 + <i>x</i>) accurate near <i>x</i> = 0.  In Java version 1.5 and
	   * later, Math.log1p can be used.
	   * <p>
	   * This is taken from D. Goldberg,
	   * <a href="https://dx.doi.org/10.1145/103162.103163">What every computer
	   * scientist should know about floating-point arithmetic</a> (1991),
	   * Theorem 4.  See also, N. J. Higham, Accuracy and Stability of Numerical
	   * Algorithms, 2nd Edition (SIAM, 2002), Answer to Problem 1.5, p 528.
	   * <p>
	   * @param x the argument.
	   * @return log(1 + <i>x</i>).
	   **********************************************************************/
	  public static double log1p(double x) {
	    double
	      y = 1 + x,
	      z = y - 1;
	    // Here's the explanation for this magic: y = 1 + z, exactly, and z
	    // approx x, thus log(y)/z (which is nearly constant near z = 0) returns
	    // a good approximation to the true log(1 + x)/x.  The multiplication x *
	    // (log(y)/z) introduces little additional error.
	    return z == 0 ? x : x * Math.log(y) / z;
	  }

	  /**
	   * The inverse hyperbolic tangent function.  This is defined in terms of
	   * log1p(<i>x</i>) in order to maintain accuracy near <i>x</i> = 0.
	   * In addition, the odd parity of the function is enforced.
	   * <p>
	   * @param x the argument.
	   * @return atanh(<i>x</i>).
	   **********************************************************************/
	  public static double atanh(double x)  {
	    double y = Math.abs(x);     // Enforce odd parity
	    y = Math.log1p(2 * y/(1 - y))/2;
	    return x < 0 ? -y : y;
	  }

	  /**
	   * The cube root function.  In Java version 1.5 and later, Math.cbrt can be
	   * used.
	   * <p>
	   * @param x the argument.
	   * @return the real cube root of <i>x</i>.
	   **********************************************************************/
	  public static double cbrt(double x) {
	    double y = Math.pow(Math.abs(x), 1/3.0); // Return the real cube root
	    return x < 0 ? -y : y;
	  }

	  public static Pair norm(double sinx, double cosx) {
	    double r = hypot(sinx, cosx);
	    return new Pair(sinx/r, cosx/r);
	  }

	  /**
	   * The error-free sum of two numbers.
	   * <p>
	   * @param u the first number in the sum.
	   * @param v the second number in the sum.
	   * @return Pair(<i>s</i>, <i>t</i>) with <i>s</i> = round(<i>u</i> +
	   *   <i>v</i>) and <i>t</i> = <i>u</i> + <i>v</i> - <i>s</i>.
	   * <p>
	   * See D. E. Knuth, TAOCP, Vol 2, 4.2.2, Theorem B.
	   **********************************************************************/
	  public static Pair sum(double u, double v) {
	    double s = u + v;
	    double up = s - v;
	    double vpp = s - up;
	    up -= u;
	    vpp -= v;
	    double t = -(up + vpp);
	    // u + v =       s      + t
	    //       = round(u + v) + t
	    return new Pair(s, t);
	  }

	  /**
	   * Evaluate a polynomial.
	   * <p>
	   * @param N the order of the polynomial.
	   * @param p the coefficient array (of size <i>N</i> + <i>s</i> + 1 or more).
	   * @param s starting index for the array.
	   * @param x the variable.
	   * @return the value of the polynomial.
	   *
	   * Evaluate <i>y</i> = &sum;<sub><i>n</i>=0..<i>N</i></sub>
	   * <i>p</i><sub><i>s</i>+<i>n</i></sub>
	   * <i>x</i><sup><i>N</i>&minus;<i>n</i></sup>.  Return 0 if <i>N</i> &lt; 0.
	   * Return <i>p</i><sub><i>s</i></sub>, if <i>N</i> = 0 (even if <i>x</i> is
	   * infinite or a nan).  The evaluation uses Horner's method.
	   **********************************************************************/
	  public static double polyval(int N, double p[], int s, double x) {
	    double y = N < 0 ? 0 : p[s++];
	    while (--N >= 0) y = y * x + p[s++];
	    return y;
	  }

	  public static double AngRound(double x) {
	    // The makes the smallest gap in x = 1/16 - nextafter(1/16, 0) = 1/2^57
	    // for reals = 0.7 pm on the earth if x is an angle in degrees.  (This
	    // is about 1000 times more resolution than we get with angles around 90
	    // degrees.)  We use this to avoid having to deal with near singular
	    // cases when x is non-zero but tiny (e.g., 1.0e-200).  This also converts
	    // -0 to +0.
	    final double z = 1/16.0;
	    double y = Math.abs(x);
	    // The compiler mustn't "simplify" z - (z - y) to y
	    y = y < z ? z - (z - y) : y;
	    return x < 0 ? 0 - y : y;
	  }

	  /**
	   * Normalize an angle (restricted input range).
	   * <p>
	   * @param x the angle in degrees.
	   * @return the angle reduced to the range [&minus;180&deg;, 180&deg;).
	   * <p>
	   * <i>x</i> must lie in [&minus;540&deg;, 540&deg;).
	   **********************************************************************/
	  public static double AngNormalize(double x)
	  { return x >= 180 ? x - 360 : (x < -180 ? x + 360 : x); }

	  /**
	   * Normalize an arbitrary angle.
	   * <p>
	   * @param x the angle in degrees.
	   * @return the angle reduced to the range [&minus;180&deg;, 180&deg;).
	   * <p>
	   * The range of <i>x</i> is unrestricted.
	   **********************************************************************/
	  public static double AngNormalize2(double x)
	  { return AngNormalize(x % 360.0); }

	  /**
	   * Difference of two angles reduced to [&minus;180&deg;, 180&deg;]
	   * <p>
	   * @param x the first angle in degrees.
	   * @param y the second angle in degrees.
	   * @return <i>y</i> &minus; <i>x</i>, reduced to the range [&minus;180&deg;,
	   *   180&deg;].
	   * <p>
	   * <i>x</i> and <i>y</i> must both lie in [&minus;180&deg;, 180&deg;].  The
	   * result is equivalent to computing the difference exactly, reducing it to
	   * (&minus;180&deg;, 180&deg;] and rounding the result.  Note that this
	   * prescription allows &minus;180&deg; to be returned (e.g., if <i>x</i> is
	   * tiny and negative and <i>y</i> = 180&deg;).
	   **********************************************************************/
	  public static double AngDiff(double x, double y) {
	    double d, t;
	    { Pair r = sum(-x, y); d = r.first; t = r.second; }
	    if ((d - 180.0) + t > 0.0) // y - x > 180
	      d -= 360.0;            // exact
	    else if ((d + 180.0) + t <= 0.0) // y - x <= -180
	      d += 360.0;            // exact
	    return d + t;
	  }
	  /**
	   * Test for finiteness.
	   * <p>
	   * @param x the argument.
	   * @return true if number is finite, false if NaN or infinite.
	   **********************************************************************/
	  public static boolean isfinite(double x) {
	    return Math.abs(x) <= Double.MAX_VALUE;
	  }
	

}
