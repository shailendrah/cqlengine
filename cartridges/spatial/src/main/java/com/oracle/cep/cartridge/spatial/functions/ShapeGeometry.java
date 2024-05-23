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

/**
 * @author  santkumk
 * shape function in spatial cartridge provides Shape Geometry around a given central point and the dimension of the shape.
 */
public class ShapeGeometry implements ISimpleFunctionMetadata {
	public static final String NAME = "shape";
	private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

	Datatype[] paramTypes;
	static Datatype[] s_defaultParamTypes = { Datatype.OBJECT, Datatype.OBJECT, Datatype.OBJECT, Datatype.OBJECT };

	public static ShapeGeometry getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
			throws MetadataNotFoundException {
		if (paramTypes == null) {
			paramTypes = s_defaultParamTypes;
		}
		if (paramTypes.length < 3) {
			return null;
		}
		if (paramTypes.length == 3){
			if (paramTypes[0].kind != IType.Kind.OBJECT) {
				return null;
			}if (paramTypes[1].kind != IType.Kind.DOUBLE) {
				return null;
			}if (paramTypes[2].kind != IType.Kind.DOUBLE) {
				return null;
			}
		}
		if(paramTypes.length ==4){
			if (paramTypes[0].kind != IType.Kind.DOUBLE) {
				return null;
			}if (paramTypes[1].kind != IType.Kind.DOUBLE){
					return null;
			}if(paramTypes[2].kind != IType.Kind.DOUBLE){
				return null;
			}if(paramTypes[3].kind != IType.Kind.DOUBLE) {
				return null;
			}
		}

		return new ShapeGeometry(paramTypes);
	}

	ShapeGeometry(Datatype[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	@Override
	public Datatype getReturnType() {
		return SpatialCartridge.getJavaType(Geometry.class);
	}

	public SingleElementFunction getImplClass() {
		return new SingleElementFunction() {
			@Override
			public Object execute(Object[] args) throws UDFException {
				double lat;
				double lng;
				double[] dim;
				if(paramTypes.length ==3){
					Geometry loc = (Geometry)args[0];
					if(!loc.isPoint())
						throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION,"geometry should be a point type. found type " + loc.getTypeName(),NAME);
					double[] coords = loc.getOrdinatesArray();
					lat = coords[0];
					lng = coords[1];
					dim = new double[]{(double)args[1], (double)args[2]};
				}else {
					lat = (double)args[0];
					lng = (double) args[1];
					dim = new double[]{(double)args[2], (double)args[3]};
				}

				double[] mbr = GeomUtil.shapeMBR(lat,lng,dim);
				return Geometry.createRectangle(mbr[1], mbr[0], mbr[3], mbr[2]);
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
