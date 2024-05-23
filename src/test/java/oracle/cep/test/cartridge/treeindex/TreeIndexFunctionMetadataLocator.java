/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexFunctionMetadataLocator.java /main/3 2009/12/24 13:57:29 mjames Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    11/27/09 - Data cartridge context, default package support
    udeshmuk    09/11/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexFunctionMetadataLocator.java /main/3 2009/12/24 13:57:29 mjames Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import java.util.List;
import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;

public class TreeIndexFunctionMetadataLocator implements IUserFunctionMetadataLocator
{

  @Override
  public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes, ICartridgeContext context)
    throws MetadataNotFoundException, AmbiguousMetadataException
  {
    if(name.equals(TreeIndexConstants.LESSTHAN) )
      return new TreeIndexFunctionMetadata(paramTypes,
                                           TreeIndexOperation.LESS);
    else if(name.equals(TreeIndexConstants.GREATERTHAN))
      return new TreeIndexFunctionMetadata(paramTypes,
                                           TreeIndexOperation.GREATER);
    else
      throw new MetadataNotFoundException(TreeIndexConstants.TREEINDEXCARTRIDGE,
                                          name);
  }

  public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context)
  {
    List<IUserFunctionMetadata> functions = new ArrayList<IUserFunctionMetadata>();
    functions.add(new TreeIndexFunctionMetadata(null,
                                           TreeIndexOperation.LESS));
    functions.add(new TreeIndexFunctionMetadata(null,
                                           TreeIndexOperation.GREATER));
    return functions;
  }
  
}
