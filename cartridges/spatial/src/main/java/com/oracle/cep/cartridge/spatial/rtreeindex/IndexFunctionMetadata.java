package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

public class IndexFunctionMetadata implements ISimpleFunctionMetadata 
{
  private Datatype[]          paramTypes;
  private Datatype            returnType;
  private OpBase              op;
  
  IndexFunctionMetadata(Datatype[] paramTypes, Datatype returnType, OpBase op)
  {
    this.paramTypes = paramTypes;
    this.returnType = returnType;
    this.op         = op;
  }
  
  public Datatype getReturnType()
  {
    return returnType;
  }
  
  public String getName()
  {
    return op.getOpName();
  }

  public void setOp(OpBase op)
  {
    this.op = op;
  }
  
  public OpBase getOp()
  {
    return op;
  }
  
  public SingleElementFunction getImplClass()
  {
    return op;
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
}
