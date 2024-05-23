/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/LatLon.java /main/1 2015/10/01 22:29:51 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rcs/LatLon.java /main/1 2015/10/01 22:29:51 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rcs;

public class LatLon {
	double lon;
	double lat;
	Datum datum;
	public static final int NORTH = 1;
	public static final int SOUTH = -1;
	public static final int EAST = 1;
	public static final int WEST = -1;

	private static class Vector3d {
		double x;
		double y;
		double z;

		public Vector3d(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		Vector3d applyTransform(Transform t) {
			double x1 = this.x, y1 = this.y, z1 = this.z;

			double tx = t.tx, ty = t.ty, tz = t.tz;
			double rx = Math.toRadians((t.rx / 3600)); // normalise seconds to
														// radians
			double ry = Math.toRadians((t.ry / 3600)); // normalise seconds to
														// radians
			double rz = Math.toRadians((t.rz / 3600)); // normalise seconds to
														// radians
			double s1 = t.s / 1e6 + 1; // normalise ppm to (s+1)

			// apply transform
			double x2 = tx + x1 * s1 - y1 * rz + z1 * ry;
			double y2 = ty + x1 * rz + y1 * s1 - z1 * rx;
			double z2 = tz - x1 * ry + y1 * rx + z1 * s1;

			return new Vector3d(x2, y2, z2);
		};

		LatLon toLatLon(Datum datum) {
			double x = this.x, y = this.y, z = this.z;
			double a = datum.ellipsoid.a, b = datum.ellipsoid.b;

			double e2 = (a * a - b * b) / (a * a); // 1st eccentricity squared
			double epsilon2 = (a * a - b * b) / (b * b); // 2nd eccentricity
															// squared
			double p = Math.sqrt(x * x + y * y); // distance from minor axis
			double R = Math.sqrt(p * p + z * z); // polar radius

			// parametric latitude (Bowring eqn 17, replacing tanbeta = z.a/ p.b)
			double tanbeta = (b * z) / (a * p) * (1 + epsilon2 * b / R);
			double sinbeta = tanbeta / Math.sqrt(1 + tanbeta * tanbeta);
			double cosbeta = sinbeta / tanbeta;

			// geodetic latitude (Bowring eqn 18)
			double phi = Math.atan2(z + epsilon2 * b * sinbeta * sinbeta
					* sinbeta, p - e2 * a * cosbeta * cosbeta * cosbeta);

			// longitude
			double lambda = Math.atan2(y, x);

			// height above ellipsoid (Bowring eqn 7) [not currently used]
			double sinphi = Math.sin(phi), cosphi = Math.cos(phi);
			double v = a * Math.sqrt(1 - e2 * sinphi * sinphi); // length of the
																// normal
																// terminated by
																// the minor
																// axis
			double h = p * cosphi + z * sinphi - (a * a / v);
			return new LatLon(Math.toDegrees(phi), Math.toDegrees(lambda), datum);
		}
	}

	public LatLon(double lat, double lon) {
		this.lon = lon;
		this.lat = lat;
		this.datum = Datum.WGS84;
	}

	public LatLon(double lat, double lon, Datum datum) {
		this.lon = lon;
		this.lat = lat;
		this.datum = datum;
	}

	public LatLon(int latDegrees, int latMinutes,
			double latSeconds, int northSouth, int lonDegrees,
			int lonMinutes, double lonSeconds, int eastWest,
			Datum datum) {
		this.lat = northSouth
				* (latDegrees + (latMinutes / 60.0) + (latSeconds / 3600.0));
		this.lon = eastWest
				* (lonDegrees + (lonMinutes / 60.0) + (lonSeconds / 3600.0));
		this.datum = datum;

	}

	public double getLongitude() {
		return lon;
	}

	public double getLatitude() {
		return lat;
	}

	public String toString() {
		return "(" + this.lat + ", " + this.lon + ")";
	}

	public String toDMSString() {
		String ret = formatLatitude() + " " + formatLongitude();

		return ret;
	}

	private String formatLatitude() {
		String ns = getLatitude() >= 0 ? "N" : "S";
		return Math.abs(getLatitudeDegrees()) + " " + getLatitudeMinutes()
				+ " " + getLatitudeSeconds() + " " + ns;
	}

	private String formatLongitude() {
		String ew = getLongitude() >= 0 ? "E" : "W";
		return Math.abs(getLongitudeDegrees()) + " " + getLongitudeMinutes()
				+ " " + getLongitudeSeconds() + " " + ew;
	}

	public int getLatitudeDegrees() {
		double ll = getLatitude();
		int deg = (int) Math.floor(ll);
		double minx = ll - deg;
		if (ll < 0 && minx != 0.0) {
			deg++;
		}
		return deg;
	}

	public int getLatitudeMinutes() {
		double ll = getLatitude();
		int deg = (int) Math.floor(ll);
		double minx = ll - deg;
		if (ll < 0 && minx != 0.0) {
			minx = 1 - minx;
		}
		int min = (int) Math.floor(minx * 60);
		return min;
	}

	public double getLatitudeSeconds() {
		double ll = getLatitude();
		int deg = (int) Math.floor(ll);
		double minx = ll - deg;
		if (ll < 0 && minx != 0.0) {
			minx = 1 - minx;
		}
		int min = (int) Math.floor(minx * 60);
		double sec = ((minx * 60) - min) * 60;
		return sec;
	}

	public int getLongitudeDegrees() {
		double ll = getLongitude();
		int deg = (int) Math.floor(ll);
		double minx = ll - deg;
		if (ll < 0 && minx != 0.0) {
			deg++;
		}
		return deg;
	}

	public int getLongitudeMinutes() {
		double ll = getLongitude();
		int deg = (int) Math.floor(ll);
		double minx = ll - deg;
		if (ll < 0 && minx != 0.0) {
			minx = 1 - minx;
		}
		int min = (int) Math.floor(minx * 60);
		return min;
	}

	public double getLongitudeSeconds() {
		double ll = getLongitude();
		int deg = (int) Math.floor(ll);
		double minx = ll - deg;
		if (ll < 0 && minx != 0.0) {
			minx = 1 - minx;
		}
		int min = (int) Math.floor(minx * 60);
		double sec = ((minx * 60) - min) * 60;
		return sec;
	}

	public void toLatLonE() {
		lon = Math.toDegrees(lon);
		lat = Math.toDegrees(lat);
	}

	public LatLon convertDatum(Datum toDatum) {
		LatLon oldLatLon = this;
		Transform transform = null;

		if (oldLatLon.datum == Datum.WGS84) {
			// converting from WGS 84
			transform = toDatum.getTransform();
		}
		if (toDatum == Datum.WGS84) {
			// converting to WGS 84; use inverse transform (don't overwrite
			// original!)
			transform = datum.getTransform().getInverse();
		}
		if (transform == null) {
			// neither this.datum nor toDatum are WGS84: convert this to WGS84
			// first
			oldLatLon = this.convertDatum(Datum.WGS84);
			transform = toDatum.getTransform();
		}

		Vector3d cartesian = oldLatLon.toCartesian(); // convert polar to
														// cartesian...
		cartesian = cartesian.applyTransform(transform); // ...apply
															// transform...
		return cartesian.toLatLon(toDatum); // ...and convert cartesian to polar
	};

	private Vector3d toCartesian() {
		double phi = Math.toRadians(lat);
		double lambda = Math.toRadians(lon);
		double h = 0; // height above ellipsoid - not currently used
		double a = this.datum.ellipsoid.a;
		double b = this.datum.ellipsoid.b;

		double sinphi = Math.sin(phi), cosphi = Math.cos(phi);
		double sinlambda = Math.sin(lambda), coslambda = Math.cos(lambda);

		double eSq = (a * a - b * b) / (a * a);
		double v = a / Math.sqrt(1 - eSq * sinphi * sinphi);

		double x = (v + h) * cosphi * coslambda;
		double y = (v + h) * cosphi * sinlambda;
		double z = ((1 - eSq) * v + h) * sinphi;

		return new Vector3d(x, y, z);
	};

}