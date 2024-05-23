/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeTypeLocator.java /main/1 2010/03/29 02:41:02 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/17/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeTypeLocator.java /main/1 2010/03/29 02:41:02 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import java.util.Map;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;

public class JdbcCartridgeTypeLocator implements ITypeLocator
{

  @Override
  public IArrayType getArrayType(String componentExtensibleTypeName,
      ICartridgeContext context) throws MetadataNotFoundException
  {
    // No array type supported by this cartridge
	Map<String, Object> ctxProps = context.getProperties();
    throw new MetadataNotFoundException(
      (String) ctxProps.get(JdbcCartridgeContext.CONTEXT_NAME),
      componentExtensibleTypeName);
  }

  @Override
  public IType getType(String extensibleTypeName, ICartridgeContext context) 
    throws MetadataNotFoundException
  {
    Map<String, Object> ctxProps = context.getProperties();
    Map<String, IType> typeMap = 
      (Map<String, IType>)ctxProps.get(JdbcCartridgeContext.TYPE_MAP);

    IType type = null;
    
    if((typeMap != null)
       && ((type=typeMap.get(extensibleTypeName)) != null))
    {
      return type;
    }
    else
      throw new MetadataNotFoundException(
        (String)ctxProps.get(JdbcCartridgeContext.CONTEXT_NAME), 
        extensibleTypeName);      
  }
  
}