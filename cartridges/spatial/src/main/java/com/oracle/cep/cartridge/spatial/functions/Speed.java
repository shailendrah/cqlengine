package com.oracle.cep.cartridge.spatial.functions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

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

public class Speed implements ISimpleFunctionMetadata {
	public static final String NAME = "speed";
	private static final Map<String, PreviousLocation> prevTuples = new HashMap<>();
	private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

	Datatype[] paramTypes;
	static Datatype[] s_defaultParamTypes = { Datatype.CHAR,
			Datatype.BIGINT, Datatype.OBJECT, Datatype.CHAR };

	public static Speed getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
			throws MetadataNotFoundException {
		if (paramTypes == null) {
			paramTypes = s_defaultParamTypes;
		}
		if (paramTypes.length < 3) {
			return null;
		}
		if (paramTypes[0].kind == IType.Kind.CHAR) {
			if (paramTypes[1].kind != IType.Kind.BIGINT)
				if (paramTypes[2].kind != IType.Kind.OBJECT)
					return null;
		}
		if(paramTypes.length==4){
			if(paramTypes[3].kind != IType.Kind.CHAR) return null; 
		}
		return new Speed(paramTypes);
	}

	Speed(Datatype[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	@Override
	public Datatype getReturnType() {
		return SpatialCartridge.getCQLType(double.class);
	}

	public SingleElementFunction getImplClass() {
		return new SingleElementFunction() {
			@Override
			public Object execute(Object[] args) throws UDFException {
				String id = (String) args[0];
				Long t1 = (Long) args[1];
				Geometry g1 = (Geometry) args[2];
				String unit = null;
				if(args.length > 3)
					unit = (String) args[3];
				PreviousLocation prevTuple = prevTuples.get(id);
				prevTuples.put(id, new PreviousLocation(t1, g1));
				double speed=0.0;
				if (prevTuple == null)
					return speed;
				try {
					double dist = Geometry.distance(g1, prevTuple.geometry);
					if(unit==null || unit.equalsIgnoreCase("mph")){
						speed = (dist/1609.344) / ((t1 - prevTuple.time)/3600000000000.0);
						log.debug("dist=" + dist/1609.344 + "miles time=" + ((t1 - prevTuple.time)/3600000000000.0 + "hour" + " Speed=" + speed +"mph"));
					}else if(unit.equalsIgnoreCase("kph")){
						speed = (dist/1000) / ((t1 - prevTuple.time)/3600000000000.0);
						log.debug("dist=" + dist/1000 + "km time=" + ((t1 - prevTuple.time)/3600000000000.0 + "hour"  + " Speed=" + speed +"kph"));
					}else
						throw new IllegalArgumentException("Unit '" + unit + "' is not supported. please provide either of the unit 'kph' for kilometers per hour or 'mph' for miles per hour");
					
				} catch (Exception e) {
					throw new UDFException(
							UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e,
							NAME);
				}
				return speed;
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

	private static class PreviousLocation {
		long time;
		Geometry geometry;

		public PreviousLocation(long time, Geometry geom) {
			this.time = time;
			this.geometry = geom;
		}
	}
}
