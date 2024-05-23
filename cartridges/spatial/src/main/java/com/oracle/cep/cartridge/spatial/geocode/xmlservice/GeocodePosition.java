package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

import com.oracle.cep.cartridge.spatial.Geometry;

public class GeocodePosition
{
	public double longitude;
	public double latitude;
	public String country = "us";
	public GeocodePosition() {}
	public GeocodePosition(double lon, double lat) {longitude = lon; latitude = lat;}
	public GeocodePosition(double lon, double lat, String country) {longitude = lon; latitude = lat; this.country = country;}
	public Geometry toGeometry() {return Geometry.createPoint(8307, longitude, latitude); }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeocodePosition other = (GeocodePosition) obj;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		return true;
	}
}