package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.geocode.GeoName;
import com.oracle.cep.cartridge.spatial.geocode.ReverseGeoCode;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.indexes.IIndexInfo;

public class OpNearBy extends OpBase{

	public static final String NAME = "nearby";

	public OpNearBy(int keyPos, ICartridgeContext ctx){
		super(NAME,keyPos, ctx);
	}

	public static IndexFunctionMetadata getMetadata(Datatype[] paramTypes, ICartridgeContext ctx){
		paramTypes = new Datatype[]{Datatype.DOUBLE , Datatype.DOUBLE };
    		return new IndexFunctionMetadata(paramTypes, Datatype.CHAR,  new OpNearBy(0, ctx) );
	}
	
	@Override
	public Object execute(Object[] args) throws UDFException {
		if(args.length != 2){
			throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
		}
		
		double latitude  = ((Number) args[0]).doubleValue();
		double longitude = ((Number) args[1]).doubleValue();
		
		String url = "classpath:/cities15000.txt";
        	ReverseGeoCode c = ReverseGeoCode.getInstance(url);
        	GeoName n = c.nearestPlace(latitude, longitude);
        	String name = n.getName();

		return name;
	}

	@Override
	public IIndexInfo[] getIndexInfo(int paramPosition,
			ICartridgeContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
