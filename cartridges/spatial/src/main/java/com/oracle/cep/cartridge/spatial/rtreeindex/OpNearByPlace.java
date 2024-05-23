package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.java.impl.JavaDatatype;
import com.oracle.cep.cartridge.java.impl.JavaTypeSystemImpl;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeAddress;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodePosition;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeProvider;
import com.oracle.cep.cartridge.spatial.geocode.xmlservice.OracleSpatialGeocodeProvider;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.indexes.IIndexInfo;

public class OpNearByPlace extends OpBase{

	public static final String NAME = "nearbyplace";

	public OpNearByPlace(int keyPos, ICartridgeContext ctx){
		super(NAME,keyPos, ctx);
	}

	public static IndexFunctionMetadata getMetadata(Datatype[] paramTypes, ICartridgeContext ctx){
		if(paramTypes.length == 2)
			paramTypes = new Datatype[]{Datatype.DOUBLE , Datatype.DOUBLE };
		else if (paramTypes.length ==3)
			paramTypes = new Datatype[]{Datatype.DOUBLE , Datatype.DOUBLE, Datatype.CHAR };
		Datatype retType = null;
		try {
			Class<?> clz = Class.forName("com.oracle.cep.cartridge.spatial.geocode.xmlservice.GeocodeAddress");
			retType = new JavaDatatype(new JavaTypeSystemImpl(), clz);
		}catch (Exception e){
			e.printStackTrace();
			retType = Datatype.OBJECT;
		}


		return new IndexFunctionMetadata(paramTypes, retType,  new OpNearByPlace(0, ctx) );
	}
	
	@Override
	public Object execute(Object[] args) throws UDFException {
		if(args.length == 2 || args.length ==3){
		
		double latitude  = ((Number) args[0]).doubleValue();
		double longitude = ((Number) args[1]).doubleValue();

		GeocodeProvider geoProvider = OracleSpatialGeocodeProvider.getInstance();
		GeocodePosition point = null;
		if(args.length == 2){
			 point = new GeocodePosition(latitude, longitude);
		}
		else {
			String country = args[2].toString();
			point = new GeocodePosition(latitude, longitude,country);
		}
 		GeocodeAddress reverseGeocCode = geoProvider.reverseGeocode(point);
 		return reverseGeocCode;
		}
 		else {
 			throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
 		}
	}

	@Override
	public IIndexInfo[] getIndexInfo(int paramPosition,
			ICartridgeContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
