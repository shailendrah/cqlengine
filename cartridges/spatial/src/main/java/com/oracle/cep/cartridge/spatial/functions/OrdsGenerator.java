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

public class OrdsGenerator implements ISimpleFunctionMetadata 
{
  public static final String NAME = "ordsgenerator";
  
  Datatype[] paramTypes;
  static Datatype[] s_defaultParamTypes = { Datatype.INT, Datatype.INT, Datatype.DOUBLE, Datatype.DOUBLE };
  
  public static OrdsGenerator getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
    throws MetadataNotFoundException
  {
    if (paramTypes == null)
    {
    	paramTypes = s_defaultParamTypes;
    }
    if (paramTypes.length < 2)
    {
      return null;
    }
    int i = 0;
    if (paramTypes[0].kind == IType.Kind.INT)
    {
    	if (paramTypes[1].kind != IType.Kind.INT)
    	      return null;
    	i = 2;
    }
    for (; i < paramTypes.length; i++)
    {
      Datatype typ = paramTypes[i];
      if (typ.kind != IType.Kind.DOUBLE && typ.kind != IType.Kind.FLOAT)
      {
          return null;
      }
    }
    return new OrdsGenerator(paramTypes);
  }

  OrdsGenerator(Datatype[] paramTypes)
  {
    this.paramTypes = paramTypes;
  }
  
  @Override
  public Datatype getReturnType()
  {
    return SpatialCartridge.getCQLType(double[].class);
  }
  
  public SingleElementFunction getImplClass()
  {
    return new SingleElementFunction()
    {
      @Override
      public Object execute(Object[] args) throws UDFException
      {
        if (paramTypes[0].kind == IType.Kind.INT)
        {
          int dim  = (Integer) args[0];
          int ncoords = (Integer) args[1];
          double[] xyz3d = new double[ncoords * dim];
          int pos = 2;
          for (int i = 0; i < ncoords; i++)
          {
            for (int j = 0; j < dim; j++)
            {
              Datatype typ = paramTypes[pos];
              if (typ.kind == IType.Kind.DOUBLE)
                xyz3d[i*dim + j] = (Double) args[pos++];
              else if (typ.kind == IType.Kind.FLOAT)
                xyz3d[i*dim + j] = (Float) args[pos++];
            }
          }
          return xyz3d;
        }
        else
        {
          double[] xyz3d = new double[paramTypes.length];
          for (int i = 0; i < paramTypes.length; i++)
          {
            Datatype typ = paramTypes[i];
            if (typ.kind == IType.Kind.DOUBLE)
              xyz3d[i] = (Double) args[i];
            else if (typ.kind == IType.Kind.FLOAT)
              xyz3d[i] = (Float) args[i];
          }
          return xyz3d;
        }
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
