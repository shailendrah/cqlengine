package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

import java.util.ArrayList;
import java.util.List;

import com.oracle.cep.cartridge.spatial.Geometry;

public class GeocodeMatches {
	
	public class GeocodeMatch {
		
		public double latitude;
		public double longitude;
		public Geometry location;
		public GeocodeAddress address;
		public Integer match_code;
		public String error_message;
		public GeocodeMatch() {};
		//public GeocodeMatch(Geometry loc, GeocodeAddress addr) {location = loc ; address = addr;}
		public GeocodeMatch(Double lat, Double lon, Geometry loc, GeocodeAddress addr) {latitude = lat; longitude = lon;  location = loc ; address = addr;}
	}
	
	List<GeocodeMatch> matches = new ArrayList<GeocodeMatch>();
	
	public void add(GeocodeMatch geocodeMatch){
		matches.add(geocodeMatch);
	}

	public int getSize(){
		return matches.size();
	}
	
	public GeocodeMatch get(int index){
		return matches.get(index);
	}
	
	public List<GeocodeMatch> result(){
		return matches;
	}
}
