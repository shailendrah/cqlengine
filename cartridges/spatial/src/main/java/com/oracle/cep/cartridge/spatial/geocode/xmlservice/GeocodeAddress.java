package com.oracle.cep.cartridge.spatial.geocode.xmlservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeocodeAddress implements Serializable {
	
	public enum AddressType {
		US_FORM1, US_FORM2, GEN_FORM, GDF_FORM, UNFORMATTED,OUTPUT
	};
	
	/**
	 * The Geocoder performs matching based on a strictness parameter the user inputs with the address.
	 * The strictness attribute, "match_mode" in a geocoding XML request tells 
	 * the Geocoder which matching algorithm to use
	 */
	public enum MatchMode {
		EXACT("exact"),				
		RELAX_STREET_TYPE("relax_street_type"),
		RELAX_POI_NAME("relax_poi_name"),
		RELAX_HOUSE_NUMBER("relax_house_number"),	
		RELAX_BASE_NAME("relax_base_name"),	
		RELAX_POSTAL_CODE("relax_postal_code"),
		RELAX_BUILTUP_AREA("relax_builtup_area"),
		RELAX_ALL("relax_all"),
		DEFAULT	("DEFAULT")	;		

		public final String relaxType;

		MatchMode(String relaxType) {
			this.relaxType = relaxType;
		}
		@Override
		public String toString() {
			return relaxType;
		}
		
	};

	public String name;
	public String street;
	public String intersectingStreet;
	public String lastline;
	public String city;
	public String state;
	public String zipCode;
	public String builtupArea;
	public String order8Area;
	public String order2Area;
	public String order1Area;
	public String country;
	public String postalCode;
	public String postalAddonCode;
	public String subArea;
	public String region;
	public String houseNumber;
	public String side;
	public String percent;
	public String edgeId;
	public List<String> unformatted;
	public String matchMode = MatchMode.DEFAULT.toString();
	public AddressType type;

	@Override
       	public String toString() {
                        return getOutputAddress();
        }
	
	public GeocodeAddress(AddressType type){
		this.type = type;
		
	}
	public static GeocodeAddress asGeocodeAddress(Object obj){
		if(obj instanceof GeocodeAddress) return (GeocodeAddress)obj;
		else throw new IllegalArgumentException(obj.getClass() + " is not a type of GeocodeAddress");
	}

	public static GeocodeAddress createUsForm1Address(String name, String street,
			String intersectingStreet, String lastline){
		GeocodeAddress us1 = new GeocodeAddress(AddressType.US_FORM1);
		us1.name = name;
		us1.street = street;
		us1.intersectingStreet = intersectingStreet;
		us1.lastline = lastline;
		return us1;
	}

	public static GeocodeAddress createUsForm1Address(String name, String street,
			String intersectingStreet, String lastline,MatchMode matchMode){
		GeocodeAddress us1 = new GeocodeAddress(AddressType.US_FORM1);
		us1.name = name;
		us1.street = street;
		us1.intersectingStreet = intersectingStreet;
		us1.lastline = lastline;
		us1.matchMode = matchMode.toString();
		return us1;
	}
	
	public static GeocodeAddress createUsForm2Address(String name, String street,
			String intersectingStreet, String city, String state, String zipCode){
		GeocodeAddress us2 = new GeocodeAddress(AddressType.US_FORM2);
		us2.name = name;
		us2.street = street;
		us2.intersectingStreet = intersectingStreet;
		us2.city = city;
		us2.state = state;
		us2.zipCode = zipCode;
		return us2;
	}
	
	public static GeocodeAddress createUsForm2Address(String name, String street,
			String intersectingStreet, String city, String state, String zipCode,MatchMode matchMode){
		GeocodeAddress us2 = new GeocodeAddress(AddressType.US_FORM2);
		us2.name = name;
		us2.street = street;
		us2.intersectingStreet = intersectingStreet;
		us2.city = city;
		us2.state = state;
		us2.zipCode = zipCode;
		us2.matchMode = matchMode.toString();
		return us2;
	}
	
	public static GeocodeAddress createGDFAddress(String name, String street,
			String intersectingStreet, String builtupArea, String order8Area,
			String order2Area, String order1Area, String country,
			String postalCode, String postalAddonCode) {
		GeocodeAddress gdf = new GeocodeAddress(AddressType.GDF_FORM);
		gdf.name = name;
		gdf.street = street;
		gdf.intersectingStreet = intersectingStreet;
		gdf.builtupArea = builtupArea;
		gdf.order8Area = order8Area;
		gdf.order2Area = order2Area;
		gdf.order1Area = order1Area;
		gdf.country = country;
		gdf.postalCode = postalCode;
		gdf.postalAddonCode = postalAddonCode;
		return gdf;
	}
	public static GeocodeAddress createGDFAddress(String name, String street,
			String intersectingStreet, String builtupArea, String order8Area,
			String order2Area, String order1Area, String country,
			String postalCode, String postalAddonCode,MatchMode matchMode) {
		GeocodeAddress gdf = new GeocodeAddress(AddressType.GDF_FORM);
		gdf.name = name;
		gdf.street = street;
		gdf.intersectingStreet = intersectingStreet;
		gdf.builtupArea = builtupArea;
		gdf.order8Area = order8Area;
		gdf.order2Area = order2Area;
		gdf.order1Area = order1Area;
		gdf.country = country;
		gdf.postalCode = postalCode;
		gdf.postalAddonCode = postalAddonCode;
		gdf.matchMode = matchMode.toString();
		return gdf;
	}
	
	public static GeocodeAddress createGENAddress(String name, String street,
			String intersectingStreet, String subArea, String city,
			String region, String country, String postalCode,
			String postalAddonCode) {
		GeocodeAddress gen = new GeocodeAddress(AddressType.GEN_FORM);
		gen.name = name;
		gen.street = street;
		gen.intersectingStreet = intersectingStreet;
		gen.subArea = subArea;
		gen.city = city;
		gen.region = region;
		gen.country = country;
		gen.postalCode = postalCode;
		gen.postalAddonCode = postalAddonCode;
		return gen;
	}
	
	public static GeocodeAddress createGENAddress(String name, String street,
			String intersectingStreet, String subArea, String city,
			String region, String country, String postalCode,
			String postalAddonCode,MatchMode matchMode) {
		GeocodeAddress gen = new GeocodeAddress(AddressType.GEN_FORM);
		gen.name = name;
		gen.street = street;
		gen.intersectingStreet = intersectingStreet;
		gen.subArea = subArea;
		gen.city = city;
		gen.region = region;
		gen.country = country;
		gen.postalCode = postalCode;
		gen.postalAddonCode = postalAddonCode;
		gen.matchMode = matchMode.toString();
		return gen;
	}
	
	public static GeocodeAddress createUnformattedAddress(List<String> addressList){
		GeocodeAddress address = new GeocodeAddress(AddressType.UNFORMATTED);
		address.unformatted = new ArrayList<>(addressList.size());
		address.unformatted.addAll(addressList);
		return address;
	}
	public String getOutputAddress(){
		StringBuilder output = new StringBuilder();
		append(output,name);
		append(output,houseNumber);
		append(output,street);
		append(output,builtupArea);
		append(output,order1Area);
		append(output,order8Area);
		append(output,country);
		append(output,postalCode);
		append(output,postalAddonCode);
		append(output,side);
		append(output,percent);
		append(output,edgeId);
		return output.toString();
	}
	
	private StringBuilder append(StringBuilder output , String outputVal){
		if(outputVal!=null)
			output.append(outputVal + " ");
		return output;
		
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((builtupArea == null) ? 0 : builtupArea.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((intersectingStreet == null) ? 0 : intersectingStreet.hashCode());
		result = prime * result + ((lastline == null) ? 0 : lastline.hashCode());
		result = prime * result + ((matchMode == null) ? 0 : matchMode.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((order1Area == null) ? 0 : order1Area.hashCode());
		result = prime * result + ((order2Area == null) ? 0 : order2Area.hashCode());
		result = prime * result + ((order8Area == null) ? 0 : order8Area.hashCode());
		result = prime * result + ((postalAddonCode == null) ? 0 : postalAddonCode.hashCode());
		result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((subArea == null) ? 0 : subArea.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unformatted == null) ? 0 : unformatted.hashCode());
		result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
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
		GeocodeAddress other = (GeocodeAddress) obj;
		if (builtupArea == null) {
			if (other.builtupArea != null)
				return false;
		} else if (!builtupArea.equals(other.builtupArea))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (intersectingStreet == null) {
			if (other.intersectingStreet != null)
				return false;
		} else if (!intersectingStreet.equals(other.intersectingStreet))
			return false;
		if (lastline == null) {
			if (other.lastline != null)
				return false;
		} else if (!lastline.equals(other.lastline))
			return false;
		if (matchMode == null) {
			if (other.matchMode != null)
				return false;
		} else if (!matchMode.equals(other.matchMode))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (order1Area == null) {
			if (other.order1Area != null)
				return false;
		} else if (!order1Area.equals(other.order1Area))
			return false;
		if (order2Area == null) {
			if (other.order2Area != null)
				return false;
		} else if (!order2Area.equals(other.order2Area))
			return false;
		if (order8Area == null) {
			if (other.order8Area != null)
				return false;
		} else if (!order8Area.equals(other.order8Area))
			return false;
		if (postalAddonCode == null) {
			if (other.postalAddonCode != null)
				return false;
		} else if (!postalAddonCode.equals(other.postalAddonCode))
			return false;
		if (postalCode == null) {
			if (other.postalCode != null)
				return false;
		} else if (!postalCode.equals(other.postalCode))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (subArea == null) {
			if (other.subArea != null)
				return false;
		} else if (!subArea.equals(other.subArea))
			return false;
		if (type != other.type)
			return false;
		if (unformatted == null) {
			if (other.unformatted != null)
				return false;
		} else if (!unformatted.equals(other.unformatted))
			return false;
		if (zipCode == null) {
			if (other.zipCode != null)
				return false;
		} else if (!zipCode.equals(other.zipCode))
			return false;
		return true;
	}

}
