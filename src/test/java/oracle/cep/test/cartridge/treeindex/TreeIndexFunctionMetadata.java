/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexFunctionMetadata.java /main/2 2009/10/08 12:59:29 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/22/09 - add getProviderName
    anasrini    09/10/09 - Creation
    anasrini    09/10/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexFunctionMetadata.java /main/2 2009/10/08 12:59:29 alealves Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

class TreeIndexFunctionMetadata implements ISimpleFunctionMetadata 
{
  private Datatype[]          paramTypes;
  private TreeIndexOperation  op;
  
  TreeIndexFunctionMetadata(Datatype[] paramTypes, TreeIndexOperation op)
  {
    this.paramTypes = paramTypes;
    this.op         = op;
  }
  
  public Datatype getReturnType()
  {
    return Datatype.BOOLEAN;
  }
  
  public String getName()
  {
    return op.getOpName();
  }

  public TreeIndexOperation getOp()
  {
    return op;
  }
  
  public SingleElementFunction getImplClass()
  {
    return new SingleElementFunction() {

        public Object execute(Object[] args) throws UDFException
        {
          Integer x = (Integer) args[0];
          Integer y = (Integer) args[1];
          
          switch(op)
          {
            case LESS:
              return (x.compareTo(y) < 0);
            case GREATER:
              return (x.compareTo(y) > 0);
            default:
              assert false : "Invalid operation in TreeIndexCartridge";
              return false;
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
    return new Attribute("attr", paramTypes[pos],0);
  }

  @Override
  public String getSchema()
  {
    return TreeIndexConstants.TREEINDEXCARTRIDGE;
  }
}

