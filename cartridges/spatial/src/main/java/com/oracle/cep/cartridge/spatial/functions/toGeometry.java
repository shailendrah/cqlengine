package com.oracle.cep.cartridge.spatial.functions;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.type.IType;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;
import java.sql.Struct;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

public class toGeometry implements ISimpleFunctionMetadata 
{
  public static final String NAME = "toGeometry";
  
  Datatype[] paramTypes;
  static Datatype[] s_defaultParamTypes = { Datatype.OBJECT };
  
  public static toGeometry getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
    throws MetadataNotFoundException
  {
    if (paramTypes == null)
    {
        paramTypes = s_defaultParamTypes;
    }
    if (paramTypes.length < 1)
    {
      return null;
    }
    if (paramTypes[0].kind != IType.Kind.OBJECT)
    {
       return null;
    }
    return new toGeometry(paramTypes);
  }

  toGeometry(Datatype[] paramTypes)
  {
    this.paramTypes = paramTypes;
  }
  
  @Override
  public Datatype getReturnType()
  {
    return SpatialCartridge.getCQLType(Geometry.class);
  }
  
  public SingleElementFunction getImplClass()
  {
    return new SingleElementFunction()
    {
      @Override
      public Object execute(Object[] args) throws UDFException
      {
        Struct struct  = (Struct) args[0];
        Geometry geom;
        try {
            geom = Geometry.toGeometry(struct);
        } catch (Exception e) {
            throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e, NAME);
        }
        return geom;
      }
    };
  }

  public Datatype[] getParameterTypes()
  {
    return paramTypes;
  }

  @Override
  public int getNumParams()
  {
    return paramTypes.length;
  }

  @Override
  public IAttribute getParam(int pos) throws MetadataException
  {
    return new Attribute("attr"+pos, paramTypes[pos],0);
  }

  @Override
  public String getSchema()
  {
    return SpatialCartridge.CARTRIDGE_NAME;
  }

  @Override
  public String getName()
  {
    return NAME;
  }
}
