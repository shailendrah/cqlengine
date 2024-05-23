package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

public class OrdsGenerator2 implements ISimpleFunctionMetadata 
{
  public static final String NAME = "ORDSGENERATOR2";
  
  Datatype[] paramTypes;
  
  public static OrdsGenerator2 getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
    throws MetadataNotFoundException
  {
    Datatype datype = SpatialCartridge.getCQLType(double[].class);
    if (paramTypes == null)
    {
    	paramTypes = new Datatype[1];
        paramTypes[0] = datype;
    	
    }
    if (paramTypes.length < 2)
    {
        return null;
    }
    for (int i = 0; i < paramTypes.length; i++)
    {
      Datatype typ = paramTypes[i];
      if (typ.getKind() != datype.getKind() ||
          !typ.typeName.equals(datype.typeName) )
      {
          return null;
      }
    }
    return new OrdsGenerator2(paramTypes);
  }

  OrdsGenerator2(Datatype[] paramTypes)
  {
    this.paramTypes = paramTypes;
  }
  
  @Override
  public Datatype getReturnType()
  {
    return SpatialCartridge.getCQLType(double[][].class);
  }
  
  public SingleElementFunction getImplClass()
  {
    return new SingleElementFunction()
    {
      @Override
      public Object execute(Object[] args) throws UDFException
      {
	        double[][] res = new double[paramTypes.length][];
	        for (int i = 0; i < paramTypes.length; i++)
	        {
        	  res[i] = (double[]) args[i];
	        }
	        return res;
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
