package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.GeomUtil;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.type.IType;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  santkumk
 * calculate bearing i.e.,  direction or an angle, between the north-south line of earth or meridian and the line connecting the
 * previous (lat1, lng1) and current (lat2, lng2) locations.
 * ref: https://en.wikipedia.org/wiki/Bearing_(navigation)
 * parameters are-
 * 1. object id
 * 2. point geometry
 * @return the angle in degree between 0 to 360.
 */
public class Bearing implements ISimpleFunctionMetadata {
	public static final String NAME = "bearing";
	private static final Map<String, double[]> prevLoc = new HashMap<>();
	private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

	Datatype[] paramTypes;
	static Datatype[] s_defaultParamTypes = { Datatype.CHAR, Datatype.OBJECT, Datatype.OBJECT };

	public static Bearing getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
			throws MetadataNotFoundException {
		if (paramTypes == null) {
			paramTypes = s_defaultParamTypes;
		}
		if (paramTypes.length < 2) {
			return null;
		}
		if(paramTypes.length ==2){
			if((paramTypes[0].kind != IType.Kind.CHAR) && (paramTypes[1].kind != IType.Kind.OBJECT) ) return  null;
		}
		if(paramTypes.length ==3){
			if((paramTypes[0].kind != IType.Kind.CHAR) && (paramTypes[1].kind != IType.Kind.DOUBLE) && (paramTypes[2].kind != IType.Kind.DOUBLE) ) return  null;
		}
		return new Bearing(paramTypes);
	}

	Bearing(Datatype[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	@Override
	public Datatype getReturnType() {
		return Datatype.DOUBLE;
	}

	public SingleElementFunction getImplClass() {
		return new SingleElementFunction() {
			@Override
			public Object execute(Object[] args) throws UDFException {
				String id = (String) args[0];
				double lng;
				double lat;
				if(args.length==2){
					if(!(args[1] instanceof Geometry))throw new UDFException(
							UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
							NAME);
					Geometry pt = (Geometry)args[1];
					double[] c = pt.getOrdinatesArray();
					lat = c[0];
					lng = c[1];
				}
				else{
					lat = (double)args[1];
					lng = (double)args[2];
				}
				double[] prevloc = prevLoc.get(id);
				prevLoc.put(id, new double[]{lat,lng});
				if (prevloc == null)
					return 0.0;
				try {
					return GeomUtil.bearing(prevloc[0], prevloc[1], lat, lng);
				} catch (Exception e) {
					throw new UDFException(
							UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e,
							NAME);
				}
			}
		};
	}

	public Datatype[] getParameterTypes() {
		return paramTypes;
	}

	@Override
	public int getNumParams() {
		return paramTypes.length;
	}

	@Override
	public IAttribute getParam(int pos) throws MetadataException {
		return new Attribute("attr" + pos, paramTypes[pos], 0);
	}

	@Override
	public String getSchema() {
		return SpatialCartridge.CARTRIDGE_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
