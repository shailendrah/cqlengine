/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexCartridge.java /main/5 2011/04/27 18:37:35 apiper Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    11/27/09 - Data cartridge context, default package support
    udeshmuk    09/11/09 - do not extend AbstractJavaCartridge
    anasrini    09/09/09 - Creation
    anasrini    09/09/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexCartridge.java /main/4 2009/12/24 13:57:36 mjames Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import java.util.HashMap;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.service.ICartridgeRegistry;

public class TreeIndexCartridge implements ICartridge
{
  private static TreeIndexInfoLocator treeIndexInfoLocator = 
    new TreeIndexInfoLocator();

  private static TreeIndexFunctionMetadataLocator treeIndexMetadataLocator =
    new TreeIndexFunctionMetadataLocator();

  private ICartridgeRegistry registry;

  public TreeIndexCartridge(ICartridgeRegistry registry)
    throws CartridgeException 
  {
    this.registry = registry;
    registry.registerCartridge(TreeIndexConstants.TREEINDEXCARTRIDGE, this);
    registry.registerServerContext(TreeIndexConstants.TREEINDEXCONTEXT,
                                   TreeIndexConstants.TREEINDEXCARTRIDGE, 
                                   new HashMap<String, Object>());
  }    

  public IIndexInfoLocator getIndexInfoLocator()
  {
    return treeIndexInfoLocator;
  }
  
  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    return treeIndexMetadataLocator;
  }

  @Override
  public ITypeLocator getTypeLocator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void destroy() throws CartridgeException
  {
    if (registry != null)
      registry.unregisterCartridge(TreeIndexConstants.TREEINDEXCARTRIDGE);
  }
}
