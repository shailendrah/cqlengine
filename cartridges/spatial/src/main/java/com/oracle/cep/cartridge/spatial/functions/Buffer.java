package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.java.impl.JavaDatatype;
import com.oracle.cep.cartridge.java.impl.JavaTypeSystemImpl;
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

import java.util.Arrays;

/**
 * @author santkumk
 *         buffer function in spatial cartridge returns a buffered version of given geometry with specified buffer (width) whose unit is the unit of srid, for ex.- for srid 8307 the buffer unit is in meter.
 * @see Geometry
 */
public class Buffer implements ISimpleFunctionMetadata {
    public static final String NAME = "buffer";
    private static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);

    Datatype[] paramTypes;
    static Datatype[] s_defaultParamTypes = {Datatype.OBJECT, Datatype.DOUBLE};

    public static Buffer getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
            throws MetadataNotFoundException {
        if (paramTypes == null) {
            paramTypes = s_defaultParamTypes;
        }
        if (paramTypes.length != 2) {
            log.error("buffer function parameters provided is " + paramTypes.length + " should be only 2.");
            return null;
        }
        if (paramTypes[0].kind != IType.Kind.OBJECT){
            log.error("buffer function parameter 1 " + paramTypes[0] + " is not of type " + IType.Kind.OBJECT.toString());
            return null;
        }
        if (paramTypes[1].kind != IType.Kind.DOUBLE) {
            log.error("buffer function parameter 2 " + paramTypes[1] + " is not of type " + IType.Kind.DOUBLE.toString());
            return null;
        }

        return new Buffer(paramTypes);
    }

    Buffer(Datatype[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    @Override
    public Datatype getReturnType() {
        Datatype retType = null;
        try {
            Class<?> clz = Class.forName("com.oracle.cep.cartridge.spatial.Geometry");
            retType = new JavaDatatype(new JavaTypeSystemImpl(), clz);
        } catch (Exception e) {
            e.printStackTrace();
            retType = Datatype.OBJECT;
        }
        return retType;
    }

    public SingleElementFunction getImplClass() {
        return new SingleElementFunction() {
            @Override
            public Object execute(Object[] args) throws UDFException {
                log.debug("buffer args: " + Arrays.toString(args));
                Geometry geom = (Geometry) args[0];
                double buffer = (double) args[1];
                try {
                    return Geometry.bufferPolygon(geom, buffer);
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
