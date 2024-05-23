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
 * direction function in spatial cartridge provides direction of an object movement from the previous position of the object.
 */
public class Direction implements ISimpleFunctionMetadata {
	public static final String NAME = "direction";
	private static final Map<String, Geometry> prevLoc = new HashMap<>();
	private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

	Datatype[] paramTypes;
	static Datatype[] s_defaultParamTypes = { Datatype.CHAR, Datatype.OBJECT };

	public static Direction getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
			throws MetadataNotFoundException {
		if (paramTypes == null) {
			paramTypes = s_defaultParamTypes;
		}
		if (paramTypes.length < 2) {
			return null;
		}
		if (paramTypes[0].kind == IType.Kind.CHAR) {
			if (paramTypes[1].kind != IType.Kind.OBJECT)
					return null;
		}
		return new Direction(paramTypes);
	}

	Direction(Datatype[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	@Override
	public Datatype getReturnType() {
		return SpatialCartridge.getCQLType(String.class);
	}

	public SingleElementFunction getImplClass() {
		return new SingleElementFunction() {
			@Override
			public Object execute(Object[] args) throws UDFException {
				String id = String.valueOf(args[0]);
				Geometry p2 = (Geometry) args[1];
				Geometry p1 = prevLoc.get(id);
				prevLoc.put(id, p2);
				if (p1 == null)
					return GeomUtil.Direction.UNKNOWN.toString();
				try {
					return GeomUtil.directionAsString(p1,p2);
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
