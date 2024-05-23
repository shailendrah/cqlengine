package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.type.IType;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

public class ElemInfoGenerator implements ISimpleFunctionMetadata 
{
  public static final String NAME = "einfogenerator";
  
  int srid = 0;
  Datatype[] paramTypes;
  static Datatype[] s_defaultParamTypes = { Datatype.INT, Datatype.INT, Datatype.INT };
    
  public static ElemInfoGenerator getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
    throws MetadataNotFoundException
  {
    if (paramTypes == null)
    {
    	paramTypes = s_defaultParamTypes;
    }
    if (paramTypes.length < 3)
    {
        return null;
    }
    for (int i = 0; i < paramTypes.length; i++)
    {
      Datatype typ = paramTypes[i];
      if (typ.kind != IType.Kind.INT)
      {
          return null;
      }
    }
    int srid = SpatialCartridge.getContextSRID(ctx);
    return new ElemInfoGenerator(srid, paramTypes);
  }
  
  ElemInfoGenerator(int srid, Datatype[] paramTypes)
  {
    this.srid = srid;
    this.paramTypes = paramTypes;
  }
  
  @Override
  public Datatype getReturnType()
  {
    return SpatialCartridge.getCQLType(int[].class);
  }
  
  public SingleElementFunction getImplClass()
  {
    return new SingleElementFunction()
    {
      @Override
      public Object execute(Object[] args) throws UDFException
      {
        int[] einfo = new int[paramTypes.length];
        int pos = 0;
        for (int i = 0; i < paramTypes.length; i++)
        {
       	  einfo[i] = (Integer) args[pos++];
        }
        return einfo;
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
