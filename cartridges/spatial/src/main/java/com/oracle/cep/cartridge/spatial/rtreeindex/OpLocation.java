package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.java.impl.JavaDatatype;
import com.oracle.cep.cartridge.java.impl.JavaTypeSystemImpl;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeAddress.MatchMode;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeMatches;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeAddress;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeProvider;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.OracleSpatialGeocodeProvider;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.indexes.IIndexInfo;

public class OpLocation extends OpBase{
	
	public final static int NO_ARGS_US1_TYPE = 5;
	public final static int NO_ARGS_US2_TYPE = 7;
	public final static int NO_ARGS_GDF_TYPE = 11;
	public final static int NO_ARGS_GEN_TYPE = 10;

	public static final String NAME = "location";

	public OpLocation(int keyPos, ICartridgeContext ctx){
		super(NAME,keyPos, ctx);
	}

	public static IndexFunctionMetadata getMetadata(Datatype[] paramTypes, ICartridgeContext ctx){
		if(paramTypes.length == NO_ARGS_US1_TYPE)
			paramTypes = new Datatype[]{Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR,Datatype.CHAR };
		else if(paramTypes.length == NO_ARGS_US2_TYPE)
			paramTypes = new Datatype[]{Datatype.CHAR , Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR,Datatype.CHAR };
		else if(paramTypes.length == NO_ARGS_GEN_TYPE)
			paramTypes = new Datatype[]{Datatype.CHAR , Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR , Datatype.CHAR  , Datatype.CHAR  , Datatype.CHAR  };
		else if(paramTypes.length == NO_ARGS_GDF_TYPE)
			paramTypes = new Datatype[]{ Datatype.CHAR, Datatype.CHAR , Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR, Datatype.CHAR , Datatype.CHAR  , Datatype.CHAR  , Datatype.CHAR  };
		else 
			return null;
		Datatype retType = null;
		try {
			Class<?> clz = Class.forName("com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeMatches");
			retType = new JavaDatatype(new JavaTypeSystemImpl(), clz);
		}catch (Exception e){
			e.printStackTrace();
			retType = Datatype.OBJECT;
		}
    	return new IndexFunctionMetadata(paramTypes, retType,  new OpLocation(0, ctx) );
	}
	
	public MatchMode getMatchMode(String matchMode){
		MatchMode mmode = null;
			for (MatchMode mode : MatchMode.values()){
				if(matchMode.equals(mode.relaxType))
					mmode = mode;
			}
			return mmode;
	}
	
	@Override
	public Object execute(Object[] args) throws UDFException {
			
	 	String name;
	 	String street;
	 	String intersectingStreet;
	 	String lastline;
	 	String matchMode;
	 	String state;
	 	String zipCode;
	 	String builtupArea;
	 	String order8Area;
	 	String order2Area;
	 	String order1Area;
	 	String country;
	 	String postalCode;
	 	String postalAddonCode;
	 	String subArea;
	 	String region;
	 	String city;
	 	
	 	GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
	 	GeocodeMatches geocodeMatches = null;
	 	
	 	if (args.length == NO_ARGS_US1_TYPE ){
	 		name = args[0].toString();
	 		street = args[1].toString();
	 		intersectingStreet = args[2].toString();
	 		lastline = args[3].toString();
	 		matchMode = args[4].toString();
	 		
	 		geocodeMatches = getAddressForUS1Address( name,  street,  intersectingStreet,  lastline,  matchMode, 
	 				 geocodeMatches,  geoProvider);
	 		
	 	}else if (args.length == NO_ARGS_US2_TYPE){
	 		name = args[0].toString();
	 		street = args[1].toString();
	 		intersectingStreet = args[2].toString();
	 		city = args[3].toString();
	 		state = args[4].toString();
	 		zipCode = args[5].toString();
	 		matchMode = args[6].toString();
	 		
	 		geocodeMatches = getAddressForUS2Address( name,  street,  intersectingStreet,  city,  state,
	 				 zipCode,  matchMode,  geocodeMatches,  geoProvider);
	 		
	 	}else if (args.length == NO_ARGS_GDF_TYPE ){
	 		name = args[0].toString();
	 		street = args[1].toString();
	 		intersectingStreet = args[2].toString();
	 		builtupArea = args[3].toString();
	 		order8Area = args[4].toString();
	 		order2Area = args[5].toString();
	 		order1Area = args[6].toString();
	 		country = args[7].toString();
	 		postalCode = args[8].toString();
	 		postalAddonCode = args[9].toString();
	 		matchMode = args[10].toString();
	 		
	 		geocodeMatches = getAddressForGdfAddress( name,  street,  intersectingStreet,  builtupArea,  order8Area,
	 				 order2Area,  order1Area,  country,  postalCode,  postalAddonCode,  matchMode,  geocodeMatches,
	 				 geoProvider);
	 		
	 	}else if (args.length == NO_ARGS_GEN_TYPE ){
	 		name = args[0].toString();
	 		street = args[1].toString();
	 		intersectingStreet = args[2].toString();
	 		subArea = args[3].toString();
	 		city = args[4].toString();
	 		region = args[5].toString();
	 		country = args[6].toString();
	 		postalCode = args[7].toString();
	 		postalAddonCode = args[8].toString();
	 		matchMode = args[9].toString();
	 		
	 		geocodeMatches = getAddressForGenAddress( name,  street,  intersectingStreet,  subArea,  city,
	 				 region,  country,  postalCode,  postalAddonCode,  matchMode,  geocodeMatches,
	 				 geoProvider);
	 		
	 	}else 
	 		throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
		return geocodeMatches;
	}
	
	public GeocodeMatches getAddressForUS1Address(String name, String street, String intersectingStreet, String lastline, String matchMode, 
			GeocodeMatches geocodeMatches, GeocodeProvider geoProvider){
		
		if(matchMode.equals("")){
 			GeocodeAddress input = GeocodeAddress.createUsForm1Address(name, street, intersectingStreet, lastline, MatchMode.DEFAULT);
 			geocodeMatches = geoProvider.geocode(input);
 		}else{
 			MatchMode mmode = getMatchMode(matchMode);
 			GeocodeAddress input = GeocodeAddress.createUsForm1Address(name, street, intersectingStreet, lastline, mmode);
 			geocodeMatches = geoProvider.geocode(input);
 		}
		
		return geocodeMatches;
		
	}
	
	public GeocodeMatches getAddressForUS2Address(String name, String street, String intersectingStreet, String city, String state,
			String zipCode, String matchMode, GeocodeMatches geocodeMatches, GeocodeProvider geoProvider){
		
		if(matchMode.equals("")){
 			GeocodeAddress input = GeocodeAddress.createUsForm2Address(name, street, intersectingStreet, city, state, zipCode, MatchMode.DEFAULT);
 			geocodeMatches = geoProvider.geocode(input);
 		}else{
 			MatchMode mmode = getMatchMode(matchMode);
 			GeocodeAddress input = GeocodeAddress.createUsForm2Address(name, street, intersectingStreet, city, state, zipCode, mmode);
 			geocodeMatches = geoProvider.geocode(input);
 		}
		return geocodeMatches;
	
	}
	
	public GeocodeMatches getAddressForGdfAddress(String name, String street, String intersectingStreet, String builtupArea, String order8Area,
			String order2Area, String order1Area, String country, String postalCode, String postalAddonCode, String matchMode, GeocodeMatches geocodeMatches,
			GeocodeProvider geoProvider){
		
		if(matchMode.equals("")){
 			GeocodeAddress input = GeocodeAddress.createGDFAddress(name,  street,  intersectingStreet,  builtupArea,  order8Area,  order2Area,  order1Area,  country,  postalCode,  postalAddonCode, MatchMode.DEFAULT);
 			geocodeMatches = geoProvider.geocode(input);
 		}else{
 			MatchMode mmode = getMatchMode(matchMode);
 			GeocodeAddress input = GeocodeAddress.createGDFAddress(name,  street,  intersectingStreet,  builtupArea,  order8Area,  order2Area,  order1Area,  country,  postalCode,  postalAddonCode, mmode);
 			geocodeMatches = geoProvider.geocode(input);
 		}
		return geocodeMatches;
	}
	
	public GeocodeMatches getAddressForGenAddress(String name, String street, String intersectingStreet, String subArea, String city,
			String region, String country, String postalCode, String postalAddonCode, String matchMode, GeocodeMatches geocodeMatches,
			GeocodeProvider geoProvider){
		if(matchMode.equals("")){
 			GeocodeAddress input = GeocodeAddress.createGENAddress(name, street, intersectingStreet, subArea, city, region, country, postalCode, postalAddonCode, MatchMode.DEFAULT);
 			geocodeMatches = geoProvider.geocode(input);
 		}else{
 			MatchMode mmode = getMatchMode(matchMode);
 			GeocodeAddress input = GeocodeAddress.createGENAddress(name, street, intersectingStreet, subArea, city, region, country, postalCode, postalAddonCode, mmode);
 			geocodeMatches = geoProvider.geocode(input);
 		}
		return geocodeMatches;
	}

	@Override
	public IIndexInfo[] getIndexInfo(int paramPosition,
			ICartridgeContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
