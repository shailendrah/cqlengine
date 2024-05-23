/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicTypeCartridge.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/01/12 - Creation
 */

package oracle.cep.extensibility.cartridge.internal.dynamictype;

import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.ITypeLocator;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicTypeCartridge.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class DynamicTypeCartridge implements ICartridge
{

  public static final String CARTRIDGE_ID
    = "oracle.cep.extensibility.cartridge.internal.dynamictype";
  
  public static final String CONTEXT_ID
    = "DynamicType";
  
  /** Type Locator for Dynamic Types*/
  private DynamicTypeLocator cartridgeTypeLocator;
  
  /**
   * DynamicType Cartridge Constructor
   */
  public DynamicTypeCartridge()
  {
    cartridgeTypeLocator = new DynamicTypeLocator();
  }
  
  @Override
  public ITypeLocator getTypeLocator()
  {
    return cartridgeTypeLocator;
  }

  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IIndexInfoLocator getIndexInfoLocator()
  {
    // TODO Auto-generated method stub
    return null;
  }  
}
