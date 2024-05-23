/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geodesic/GeodesicPath.java /main/1 2015/06/18 19:14:13 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geodesic/GeodesicPath.java /main/1 2015/06/18 19:14:13 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial.geodesic;

public class GeodesicPath {
	private static final int nC1_ = ReferenceSystem.nC1_;
	private static final int nC1p_ = ReferenceSystem.nC1p_;
	private static final int nC2_ = ReferenceSystem.nC2_;
	private static final int nC3_ = ReferenceSystem.nC3_;
	private static final int nC4_ = ReferenceSystem.nC4_;

	private double _lat1, _lon1, _azi1;
	private double _a, _f, _b, _c2, _f1, _salp0, _calp0, _k2, _salp1, _calp1,
			_ssig1, _csig1, _dn1, _stau1, _ctau1, _somg1, _comg1, _A1m1, _A2m1,
			_A3c, _B11, _B21, _B31, _A4, _B41;
	// index zero elements of _C1a, _C1pa, _C2a, _C3a are unused
	private double _C1a[], _C1pa[], _C2a[], _C3a[], _C4a[]; // all the elements
															// of _C4a are used
	private int _caps;

	/**
	 * Constructor for a geodesic line staring at latitude <i>lat1</i>,
	 * longitude <i>lon1</i>, and azimuth <i>azi1</i> (all in degrees).
	 * <p>
	 * 
	 * @param g
	 *            A {@link ReferenceSystem} object used to compute the necessary
	 *            information about the GeodesicPath.
	 * @param lat1
	 *            latitude of point 1 (degrees).
	 * @param lon1
	 *            longitude of point 1 (degrees).
	 * @param azi1
	 *            azimuth at point 1 (degrees).
	 *            <p>
	 *            <i>lat1</i> should be in the range [&minus;90&deg;, 90&deg;];
	 *            <i>lon1</i> and <i>azi1</i> should be in the range
	 *            [&minus;540&deg;, 540&deg;).
	 *            <p>
	 *            If the point is at a pole, the azimuth is defined by keeping
	 *            <i>lon1</i> fixed, writing <i>lat1</i> = &plusmn;(90&deg;
	 *            &minus; &epsilon;), and taking the limit &epsilon; &rarr; 0+.
	 **********************************************************************/
	public GeodesicPath(ReferenceSystem g, double lat1, double lon1, double azi1) {
		this(g, lat1, lon1, azi1, ReferenceSystem.ALL);
	}

	/**
	 * Constructor for a geodesic line staring at latitude <i>lat1</i>,
	 * longitude <i>lon1</i>, and azimuth <i>azi1</i> (all in degrees) with a
	 * subset of the capabilities included.
	 * <p>
	 * 
	 * @param g
	 *            A {@link ReferenceSystem} object used to compute the necessary
	 *            information about the GeodesicPath.
	 * @param lat1
	 *            latitude of point 1 (degrees).
	 * @param lon1
	 *            longitude of point 1 (degrees).
	 * @param azi1
	 *            azimuth at point 1 (degrees).
	 * @param caps
	 *            bitor'ed combination of {@link GeodesicMask} values specifying
	 *            the capabilities the GeodesicPath object should possess, i.e.,
	 *            which quantities can be returned in calls to {@link #Position
	 *            Position}.
	 *            <p>
	 *            The {@link GeodesicMask} values are
	 *            <ul>
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.LATITUDE for the latitude
	 *            <i>lat2</i>; this is added automatically;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.LONGITUDE for the latitude
	 *            <i>lon2</i>;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.AZIMUTH for the latitude
	 *            <i>azi2</i>; this is added automatically;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.DISTANCE for the distance
	 *            <i>s12</i>;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.REDUCEDLENGTH for the reduced
	 *            length <i>m12</i>;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.GEODESICSCALE for the geodesic
	 *            scales <i>M12</i> and <i>M21</i>;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.AREA for the area <i>S12</i>;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.DISTANCE_IN permits the length
	 *            of the geodesic to be given in terms of <i>s12</i>; without
	 *            this capability the length can only be specified in terms of
	 *            arc length;
	 *            <li>
	 *            <i>caps</i> |= ReferenceSystem.ALL for all of the above;
	 *            </ul>
	 **********************************************************************/
	public GeodesicPath(ReferenceSystem g, double lat1, double lon1,
			double azi1, int caps) {
		_a = g._a;
		_f = g._f;
		_b = g._b;
		_c2 = g._c2;
		_f1 = g._f1;
		// Always allow latitude and azimuth and unrolling the longitude
		_caps = caps | ReferenceSystem.LATITUDE | ReferenceSystem.AZIMUTH
				| ReferenceSystem.LONG_UNROLL;

		// Guard against underflow in salp0
		azi1 = ReferenceSystem.AngRound(ReferenceSystem.AngNormalize(azi1));
		_lat1 = lat1;
		_lon1 = lon1;
		_azi1 = azi1;
		// alp1 is in [0, pi]
		double alp1 = azi1 * ReferenceSystem.degree;
		// Enforce sin(pi) == 0 and cos(pi/2) == 0. Better to face the ensuing
		// problems directly than to skirt them.
		_salp1 = azi1 == -180 ? 0 : Math.sin(alp1);
		_calp1 = Math.abs(azi1) == 90 ? 0 : Math.cos(alp1);
		double cbet1, sbet1, phi;
		phi = lat1 * ReferenceSystem.degree;
		// Ensure cbet1 = +epsilon at poles
		sbet1 = _f1 * Math.sin(phi);
		cbet1 = Math.abs(lat1) == 90 ? ReferenceSystem.tiny_ : Math.cos(phi);
		{
			ReferenceSystem.Pair p = ReferenceSystem.norm(sbet1, cbet1);
			sbet1 = p.first;
			cbet1 = p.second;
		}
		_dn1 = Math.sqrt(1 + g._ep2 * ReferenceSystem.sq(sbet1));

		// Evaluate alp0 from sin(alp1) * cos(bet1) = sin(alp0),
		_salp0 = _salp1 * cbet1; // alp0 in [0, pi/2 - |bet1|]
		// Alt: calp0 = hypot(sbet1, calp1 * cbet1). The following
		// is slightly better (consider the case salp1 = 0).
		_calp0 = ReferenceSystem.hypot(_calp1, _salp1 * sbet1);
		// Evaluate sig with tan(bet1) = tan(sig1) * cos(alp1).
		// sig = 0 is nearest northward crossing of equator.
		// With bet1 = 0, alp1 = pi/2, we have sig1 = 0 (equatorial line).
		// With bet1 = pi/2, alp1 = -pi, sig1 = pi/2
		// With bet1 = -pi/2, alp1 = 0 , sig1 = -pi/2
		// Evaluate omg1 with tan(omg1) = sin(alp0) * tan(sig1).
		// With alp0 in (0, pi/2], quadrants for sig and omg coincide.
		// No atan2(0,0) ambiguity at poles since cbet1 = +epsilon.
		// With alp0 = 0, omg1 = 0 for alp1 = 0, omg1 = pi for alp1 = pi.
		_ssig1 = sbet1;
		_somg1 = _salp0 * sbet1;
		_csig1 = _comg1 = sbet1 != 0 || _calp1 != 0 ? cbet1 * _calp1 : 1;
		{
			ReferenceSystem.Pair p = ReferenceSystem.norm(_ssig1, _csig1);
			_ssig1 = p.first;
			_csig1 = p.second;
		} // sig1 in (-pi, pi]
		// ReferenceSystem.norm(_somg1, _comg1); -- don't need to normalize!

		_k2 = ReferenceSystem.sq(_calp0) * g._ep2;
		double eps = _k2 / (2 * (1 + Math.sqrt(1 + _k2)) + _k2);

		if ((_caps & ReferenceSystem.CAP_C1) != 0) {
			_A1m1 = ReferenceSystem.A1m1f(eps);
			_C1a = new double[nC1_ + 1];
			ReferenceSystem.C1f(eps, _C1a);
			_B11 = ReferenceSystem.SinCosSeries(true, _ssig1, _csig1, _C1a);
			double s = Math.sin(_B11), c = Math.cos(_B11);
			// tau1 = sig1 + B11
			_stau1 = _ssig1 * c + _csig1 * s;
			_ctau1 = _csig1 * c - _ssig1 * s;
			// Not necessary because C1pa reverts C1a
			// _B11 = -SinCosSeries(true, _stau1, _ctau1, _C1pa, nC1p_);
		}

		if ((_caps & ReferenceSystem.CAP_C1p) != 0) {
			_C1pa = new double[nC1p_ + 1];
			ReferenceSystem.C1pf(eps, _C1pa);
		}

		if ((_caps & ReferenceSystem.CAP_C2) != 0) {
			_C2a = new double[nC2_ + 1];
			_A2m1 = ReferenceSystem.A2m1f(eps);
			ReferenceSystem.C2f(eps, _C2a);
			_B21 = ReferenceSystem.SinCosSeries(true, _ssig1, _csig1, _C2a);
		}

		if ((_caps & ReferenceSystem.CAP_C3) != 0) {
			_C3a = new double[nC3_];
			g.C3f(eps, _C3a);
			_A3c = -_f * _salp0 * g.A3f(eps);
			_B31 = ReferenceSystem.SinCosSeries(true, _ssig1, _csig1, _C3a);
		}

		if ((_caps & ReferenceSystem.CAP_C4) != 0) {
			_C4a = new double[nC4_];
			g.C4f(eps, _C4a);
			// Multiplier = a^2 * e^2 * cos(alpha0) * sin(alpha0)
			_A4 = ReferenceSystem.sq(_a) * _calp0 * _salp0 * g._e2;
			_B41 = ReferenceSystem.SinCosSeries(false, _ssig1, _csig1, _C4a);
		}
	}

	/**
	 * A default constructor. If GeodesicPath.Position is called on the
	 * resulting object, it returns immediately (without doing any
	 * calculations). The object can be set with a call to
	 * {@link ReferenceSystem.Line}. Use {@link Init()} to test whether object
	 * is still in this uninitialized state. (This constructor was useful in
	 * C++, e.g., to allow vectors of GeodesicPath objects. It may not be needed
	 * in Java, so make it private.)
	 **********************************************************************/
	private GeodesicPath() {
		_caps = 0;
	}

	/**
	 * Compute the position of point 2 which is a distance <i>s12</i> (meters)
	 * from point 1.
	 * <p>
	 * 
	 * @param s12
	 *            distance between point 1 and point 2 (meters); it can be
	 *            negative.
	 * @return a {@link GeodesicData} object with the following fields:
	 *         <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, <i>lat2</i>, <i>lon2</i>,
	 *         <i>azi2</i>, <i>s12</i>, <i>a12</i>. Some of these results may be
	 *         missing if the GeodesicPath did not include the relevant
	 *         capability.
	 *         <p>
	 *         The values of <i>lon2</i> and <i>azi2</i> returned are in the
	 *         range [&minus;180&deg;, 180&deg;).
	 *         <p>
	 *         The GeodesicPath object <i>must</i> have been constructed with
	 *         <i>caps</i> |= {@link GeodesicMask#DISTANCE_IN}; otherwise no
	 *         parameters are set.
	 **********************************************************************/
	public Geodesic Position(double s12) {
		return Position(false, s12, ReferenceSystem.LATITUDE
				| ReferenceSystem.LONGITUDE | ReferenceSystem.AZIMUTH);
	}

	/**
	 * Compute the position of point 2 which is a distance <i>s12</i> (meters)
	 * from point 1 and with a subset of the geodesic results returned.
	 * <p>
	 * 
	 * @param s12
	 *            distance between point 1 and point 2 (meters); it can be
	 *            negative.
	 * @param outmask
	 *            a bitor'ed combination of {@link GeodesicMask} values
	 *            specifying which results should be returned.
	 * @return a {@link GeodesicData} object including the requested results.
	 *         <p>
	 *         The GeodesicPath object <i>must</i> have been constructed with
	 *         <i>caps</i> |= {@link GeodesicMask#DISTANCE_IN}; otherwise no
	 *         parameters are set. Requesting a value which the GeodesicPath
	 *         object is not capable of computing is not an error (no parameters
	 *         will be set). The value of <i>lon2</i> returned is normally in
	 *         the range [&minus;180&deg;, 180&deg;); however if the
	 *         <i>outmask</i> includes the {@link GeodesicMask#LONG_UNROLL}
	 *         flag, the longitude is "unrolled" so that the quantity
	 *         <i>lon2</i> &minus; <i>lon1</i> indicates how many times and in
	 *         what sense the geodesic encircles the ellipsoid.
	 **********************************************************************/
	public Geodesic Position(double s12, int outmask) {
		return Position(false, s12, outmask);
	}

	/**
	 * Compute the position of point 2 which is an arc length <i>a12</i>
	 * (degrees) from point 1.
	 * <p>
	 * 
	 * @param a12
	 *            arc length between point 1 and point 2 (degrees); it can be
	 *            negative.
	 * @return a {@link GeodesicData} object with the following fields:
	 *         <i>lat1</i>, <i>lon1</i>, <i>azi1</i>, <i>lat2</i>, <i>lon2</i>,
	 *         <i>azi2</i>, <i>s12</i>, <i>a12</i>. Some of these results may be
	 *         missing if the GeodesicPath did not include the relevant
	 *         capability.
	 *         <p>
	 *         The values of <i>lon2</i> and <i>azi2</i> returned are in the
	 *         range [&minus;180&deg;, 180&deg;).
	 *         <p>
	 *         The GeodesicPath object <i>must</i> have been constructed with
	 *         <i>caps</i> |= {@link GeodesicMask#DISTANCE_IN}; otherwise no
	 *         parameters are set.
	 **********************************************************************/
	public Geodesic ArcPosition(double a12) {
		return Position(true, a12, ReferenceSystem.LATITUDE
				| ReferenceSystem.LONGITUDE | ReferenceSystem.AZIMUTH
				| ReferenceSystem.DISTANCE);
	}

	/**
	 * Compute the position of point 2 which is an arc length <i>a12</i>
	 * (degrees) from point 1 and with a subset of the geodesic results
	 * returned.
	 * <p>
	 * 
	 * @param a12
	 *            arc length between point 1 and point 2 (degrees); it can be
	 *            negative.
	 * @param outmask
	 *            a bitor'ed combination of {@link GeodesicMask} values
	 *            specifying which results should be returned.
	 * @return a {@link GeodesicData} object giving <i>lat1</i>, <i>lon2</i>,
	 *         <i>azi2</i>, and <i>a12</i>.
	 *         <p>
	 *         The GeodesicPath object <i>must</i> have been constructed with
	 *         <i>caps</i> |= {@link GeodesicMask#DISTANCE_IN}; otherwise no
	 *         parameters are set. Requesting a value which the GeodesicPath
	 *         object is not capable of computing is not an error (no parameters
	 *         will be set). The value of <i>lon2</i> returned is in the range
	 *         [&minus;180&deg;, 180&deg;), unless the <i>outmask</i> includes
	 *         the {@link GeodesicMask#LONG_UNROLL} flag.
	 **********************************************************************/
	public Geodesic ArcPosition(double a12, int outmask) {
		return Position(true, a12, outmask);
	}

	/**
	 * The general position function. {@link #Position(double, int) Position}
	 * and {@link #ArcPosition(double, int) ArcPosition} are defined in terms of
	 * this function.
	 * <p>
	 * 
	 * @param arcmode
	 *            boolean flag determining the meaning of the second parameter;
	 *            if arcmode is false, then the GeodesicPath object must have
	 *            been constructed with <i>caps</i> |=
	 *            {@link GeodesicMask#DISTANCE_IN}.
	 * @param s12_a12
	 *            if <i>arcmode</i> is false, this is the distance between point
	 *            1 and point 2 (meters); otherwise it is the arc length between
	 *            point 1 and point 2 (degrees); it can be negative.
	 * @param outmask
	 *            a bitor'ed combination of {@link GeodesicMask} values
	 *            specifying which results should be returned.
	 * @return a {@link GeodesicData} object with the requested results.
	 *         <p>
	 *         The {@link GeodesicMask} values possible for <i>outmask</i> are
	 *         <ul>
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.LATITUDE for the latitude
	 *         <i>lat2</i>.
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.LONGITUDE for the latitude
	 *         <i>lon2</i>.
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.AZIMUTH for the latitude
	 *         <i>azi2</i>.
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.DISTANCE for the distance
	 *         <i>s12</i>.
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.REDUCEDLENGTH for the reduced
	 *         length <i>m12</i>.
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.GEODESICSCALE for the geodesic
	 *         scales <i>M12</i> and <i>M21</i>.
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.ALL for all of the above;
	 *         <li>
	 *         <i>outmask</i> |= ReferenceSystem.LONG_UNROLL to unroll
	 *         <i>lon2</i> (instead of reducing it to the range
	 *         [&minus;180&deg;, 180&deg;)).
	 *         </ul>
	 *         <p>
	 *         Requesting a value which the GeodesicPath object is not capable
	 *         of computing is not an error; Double.NaN is returned instead.
	 **********************************************************************/
	public Geodesic Position(boolean arcmode, double s12_a12, int outmask) {
		outmask &= _caps & ReferenceSystem.OUT_MASK;
		Geodesic r = new Geodesic();
		if (!(Init() && (arcmode || (_caps & ReferenceSystem.DISTANCE_IN & ReferenceSystem.OUT_MASK) != 0)))
			// Uninitialized or impossible distance calculation requested
			return r;
		r.lat1 = _lat1;
		r.azi1 = _azi1;
		r.lon1 = ((outmask & ReferenceSystem.LONG_UNROLL) != 0) ? _lon1
				: ReferenceSystem.AngNormalize(_lon1);

		// Avoid warning about uninitialized B12.
		double sig12, ssig12, csig12, B12 = 0, AB1 = 0;
		if (arcmode) {
			// Interpret s12_a12 as spherical arc length
			r.a12 = s12_a12;
			sig12 = s12_a12 * ReferenceSystem.degree;
			double s12a = Math.abs(s12_a12);
			s12a -= 180 * Math.floor(s12a / 180);
			ssig12 = s12a == 0 ? 0 : Math.sin(sig12);
			csig12 = s12a == 90 ? 0 : Math.cos(sig12);
		} else {
			// Interpret s12_a12 as distance
			r.s12 = s12_a12;
			double tau12 = s12_a12 / (_b * (1 + _A1m1)), s = Math.sin(tau12), c = Math
					.cos(tau12);
			// tau2 = tau1 + tau12
			B12 = -ReferenceSystem.SinCosSeries(true, _stau1 * c + _ctau1 * s,
					_ctau1 * c - _stau1 * s, _C1pa);
			sig12 = tau12 - (B12 - _B11);
			ssig12 = Math.sin(sig12);
			csig12 = Math.cos(sig12);
			if (Math.abs(_f) > 0.01) {
				// Reverted distance series is inaccurate for |f| > 1/100, so
				// correct
				// sig12 with 1 Newton iteration. The following table shows the
				// approximate maximum error for a = WGS_a() and various f
				// relative to
				// GeodesicExact.
				// erri = the error in the inverse solution (nm)
				// errd = the error in the direct solution (series only) (nm)
				// errda = the error in the direct solution (series + 1 Newton)
				// (nm)
				//
				// f erri errd errda
				// -1/5 12e6 1.2e9 69e6
				// -1/10 123e3 12e6 765e3
				// -1/20 1110 108e3 7155
				// -1/50 18.63 200.9 27.12
				// -1/100 18.63 23.78 23.37
				// -1/150 18.63 21.05 20.26
				// 1/150 22.35 24.73 25.83
				// 1/100 22.35 25.03 25.31
				// 1/50 29.80 231.9 30.44
				// 1/20 5376 146e3 10e3
				// 1/10 829e3 22e6 1.5e6
				// 1/5 157e6 3.8e9 280e6
				double ssig2 = _ssig1 * csig12 + _csig1 * ssig12, csig2 = _csig1
						* csig12 - _ssig1 * ssig12;
				B12 = ReferenceSystem.SinCosSeries(true, ssig2, csig2, _C1a);
				double serr = (1 + _A1m1) * (sig12 + (B12 - _B11)) - s12_a12
						/ _b;
				sig12 = sig12 - serr
						/ Math.sqrt(1 + _k2 * ReferenceSystem.sq(ssig2));
				ssig12 = Math.sin(sig12);
				csig12 = Math.cos(sig12);
				// Update B12 below
			}
			r.a12 = sig12 / ReferenceSystem.degree;
		}

		double omg12, lam12, lon12;
		double ssig2, csig2, sbet2, cbet2, somg2, comg2, salp2, calp2;
		// sig2 = sig1 + sig12
		ssig2 = _ssig1 * csig12 + _csig1 * ssig12;
		csig2 = _csig1 * csig12 - _ssig1 * ssig12;
		double dn2 = Math.sqrt(1 + _k2 * ReferenceSystem.sq(ssig2));
		if ((outmask & (ReferenceSystem.DISTANCE
				| ReferenceSystem.REDUCEDLENGTH | ReferenceSystem.GEODESICSCALE)) != 0) {
			if (arcmode || Math.abs(_f) > 0.01)
				B12 = ReferenceSystem.SinCosSeries(true, ssig2, csig2, _C1a);
			AB1 = (1 + _A1m1) * (B12 - _B11);
		}
		// sin(bet2) = cos(alp0) * sin(sig2)
		sbet2 = _calp0 * ssig2;
		// Alt: cbet2 = hypot(csig2, salp0 * ssig2);
		cbet2 = ReferenceSystem.hypot(_salp0, _calp0 * csig2);
		if (cbet2 == 0)
			// I.e., salp0 = 0, csig2 = 0. Break the degeneracy in this case
			cbet2 = csig2 = ReferenceSystem.tiny_;
		// tan(alp0) = cos(sig2)*tan(alp2)
		salp2 = _salp0;
		calp2 = _calp0 * csig2; // No need to normalize

		if ((outmask & ReferenceSystem.DISTANCE) != 0 && arcmode)
			r.s12 = _b * ((1 + _A1m1) * sig12 + AB1);

		if ((outmask & ReferenceSystem.LONGITUDE) != 0) {
			// tan(omg2) = sin(alp0) * tan(sig2)
			somg2 = _salp0 * ssig2;
			comg2 = csig2; // No need to normalize
			int E = _salp0 < 0 ? -1 : 1; // east or west going?
			// omg12 = omg2 - omg1
			omg12 = ((outmask & ReferenceSystem.LONG_UNROLL) != 0) ? E
					* (sig12
							- (Math.atan2(ssig2, csig2) - Math.atan2(_ssig1,
									_csig1)) + (Math.atan2(E * somg2, comg2) - Math
							.atan2(E * _somg1, _comg1)))
					: Math.atan2(somg2 * _comg1 - comg2 * _somg1, comg2
							* _comg1 + somg2 * _somg1);
			lam12 = omg12
					+ _A3c
					* (sig12 + (ReferenceSystem.SinCosSeries(true, ssig2,
							csig2, _C3a) - _B31));
			lon12 = lam12 / ReferenceSystem.degree;
			// Use ReferenceSystem.AngNormalize2 because longitude might have
			// wrapped
			// multiple times.
			r.lon2 = ((outmask & ReferenceSystem.LONG_UNROLL) != 0) ? _lon1
					+ lon12 : ReferenceSystem.AngNormalize(r.lon1
					+ ReferenceSystem.AngNormalize2(lon12));
		}

		if ((outmask & ReferenceSystem.LATITUDE) != 0)
			r.lat2 = Math.atan2(sbet2, _f1 * cbet2) / ReferenceSystem.degree;

		if ((outmask & ReferenceSystem.AZIMUTH) != 0)
			// minus signs give range [-180, 180). 0- converts -0 to +0.
			r.azi2 = 0 - Math.atan2(-salp2, calp2) / ReferenceSystem.degree;

		if ((outmask & (ReferenceSystem.REDUCEDLENGTH | ReferenceSystem.GEODESICSCALE)) != 0) {
			double B22 = ReferenceSystem.SinCosSeries(true, ssig2, csig2, _C2a), AB2 = (1 + _A2m1)
					* (B22 - _B21), J12 = (_A1m1 - _A2m1) * sig12 + (AB1 - AB2);
			if ((outmask & ReferenceSystem.REDUCEDLENGTH) != 0)
				// Add parens around (_csig1 * ssig2) and (_ssig1 * csig2) to
				// ensure
				// accurate cancellation in the case of coincident points.
				r.m12 = _b
						* ((dn2 * (_csig1 * ssig2) - _dn1 * (_ssig1 * csig2)) - _csig1
								* csig2 * J12);
			if ((outmask & ReferenceSystem.GEODESICSCALE) != 0) {
				double t = _k2 * (ssig2 - _ssig1) * (ssig2 + _ssig1)
						/ (_dn1 + dn2);
				r.M12 = csig12 + (t * ssig2 - csig2 * J12) * _ssig1 / _dn1;
				r.M21 = csig12 - (t * _ssig1 - _csig1 * J12) * ssig2 / dn2;
			}
		}

		if ((outmask & ReferenceSystem.AREA) != 0) {
			double B42 = ReferenceSystem
					.SinCosSeries(false, ssig2, csig2, _C4a);
			double salp12, calp12;
			if (_calp0 == 0 || _salp0 == 0) {
				// alp12 = alp2 - alp1, used in atan2 so no need to normalize
				salp12 = salp2 * _calp1 - calp2 * _salp1;
				calp12 = calp2 * _calp1 + salp2 * _salp1;
				// The right thing appears to happen if alp1 = +/-180 and alp2 =
				// 0, viz
				// salp12 = -0 and alp12 = -180. However this depends on the
				// sign
				// being attached to 0 correctly. The following ensures the
				// correct
				// behavior.
				if (salp12 == 0 && calp12 < 0) {
					salp12 = ReferenceSystem.tiny_ * _calp1;
					calp12 = -1;
				}
			} else {
				// tan(alp) = tan(alp0) * sec(sig)
				// tan(alp2-alp1) = (tan(alp2) -tan(alp1)) /
				// (tan(alp2)*tan(alp1)+1)
				// = calp0 * salp0 * (csig1-csig2) / (salp0^2 + calp0^2 *
				// csig1*csig2)
				// If csig12 > 0, write
				// csig1 - csig2 = ssig12 * (csig1 * ssig12 / (1 + csig12) +
				// ssig1)
				// else
				// csig1 - csig2 = csig1 * (1 - csig12) + ssig12 * ssig1
				// No need to normalize
				salp12 = _calp0
						* _salp0
						* (csig12 <= 0 ? _csig1 * (1 - csig12) + ssig12
								* _ssig1 : ssig12
								* (_csig1 * ssig12 / (1 + csig12) + _ssig1));
				calp12 = ReferenceSystem.sq(_salp0)
						+ ReferenceSystem.sq(_calp0) * _csig1 * csig2;
			}
			r.S12 = _c2 * Math.atan2(salp12, calp12) + _A4 * (B42 - _B41);
		}

		return r;
	}

	/**
	 * @return true if the object has been initialized.
	 **********************************************************************/
	private boolean Init() {
		return _caps != 0;
	}

	/**
	 * @return <i>lat1</i> the latitude of point 1 (degrees).
	 **********************************************************************/
	public double Latitude() {
		return Init() ? _lat1 : Double.NaN;
	}

	/**
	 * @return <i>lon1</i> the longitude of point 1 (degrees).
	 **********************************************************************/
	public double Longitude() {
		return Init() ? _lon1 : Double.NaN;
	}

	/**
	 * @return <i>azi1</i> the azimuth (degrees) of the geodesic line at point
	 *         1.
	 **********************************************************************/
	public double Azimuth() {
		return Init() ? _azi1 : Double.NaN;
	}

	/**
	 * @return <i>azi0</i> the azimuth (degrees) of the geodesic line as it
	 *         crosses the equator in a northward direction.
	 **********************************************************************/
	public double EquatorialAzimuth() {
		return Init() ? Math.atan2(_salp0, _calp0) / ReferenceSystem.degree
				: Double.NaN;
	}

	/**
	 * @return <i>a1</i> the arc length (degrees) between the northward
	 *         equatorial crossing and point 1.
	 **********************************************************************/
	public double EquatorialArc() {
		return Init() ? Math.atan2(_ssig1, _csig1) / ReferenceSystem.degree
				: Double.NaN;
	}

	/**
	 * @return <i>a</i> the equatorial radius of the ellipsoid (meters). This is
	 *         the value inherited from the Geodesic object used in the
	 *         constructor.
	 **********************************************************************/
	public double MajorRadius() {
		return Init() ? _a : Double.NaN;
	}

	/**
	 * @return <i>f</i> the flattening of the ellipsoid. This is the value
	 *         inherited from the Geodesic object used in the constructor.
	 **********************************************************************/
	public double Flattening() {
		return Init() ? _f : Double.NaN;
	}

	/**
	 * @return <i>caps</i> the computational capabilities that this object was
	 *         constructed with. LATITUDE and AZIMUTH are always included.
	 **********************************************************************/
	public int Capabilities() {
		return _caps;
	}

	/**
	 * @param testcaps
	 *            a set of bitor'ed {@link GeodesicMask} values.
	 * @return true if the GeodesicPath object has all these capabilities.
	 **********************************************************************/
	public boolean Capabilities(int testcaps) {
		testcaps &= ReferenceSystem.OUT_ALL;
		return (_caps & testcaps) == testcaps;
	}

}
