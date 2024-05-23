/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeFunctionMetadataLocator.java /main/1 2010/03/29 02:41:02 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/11/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeFunctionMetadataLocator.java /main/1 2010/03/29 02:41:02 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;

public class JdbcCartridgeFunctionMetadataLocator 
  implements IUserFunctionMetadataLocator
{
  public JdbcCartridgeFunctionMetadataLocator()
  {
    
  }
  
  public List<IUserFunctionMetadata> getAllFunctions(ICartridgeContext context) 
    throws MetadataNotFoundException
  {
    //Retrieve the functionmap from the context properties
    Map<String, Object> ctxProps = context.getProperties();
    Map<String, IUserFunctionMetadata> functionMap = 
      (Map<String, IUserFunctionMetadata>)ctxProps.get(JdbcCartridgeContext.FUNC_MAP);
    
    if((functionMap != null) && (functionMap.size() > 0))
    {
      Collection<IUserFunctionMetadata> allFunctions = functionMap.values();
      return new ArrayList<IUserFunctionMetadata>(allFunctions);  
    }
    else
      throw new MetadataNotFoundException(
        (String)ctxProps.get(JdbcCartridgeContext.CONTEXT_NAME), "allfunctions");
    
  }

  public IUserFunctionMetadata getFunction(String name, Datatype[] paramTypes,
    ICartridgeContext context) 
    throws MetadataNotFoundException, AmbiguousMetadataException
  {
    Map<String, Object> ctxProps = context.getProperties();
    Map<String, IUserFunctionMetadata> functionMap = 
      (Map<String, IUserFunctionMetadata>)ctxProps.get(JdbcCartridgeContext.FUNC_MAP);
    //mapkey formed by concatenating function name with param type names
    String mapKey = name;
    for(Datatype dt : paramTypes)
    {
      mapKey = mapKey + dt.typeName;
    }
    
    IUserFunctionMetadata meta = null;
    
    if((functionMap != null) && ((meta=functionMap.get(mapKey)) != null))
    {
      return meta;
    }
    else
      throw new MetadataNotFoundException(
        (String)ctxProps.get(JdbcCartridgeContext.CONTEXT_NAME), name);
  }
  
}
  