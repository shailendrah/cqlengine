/* $Header: cep/wlevs_cql/modules/cqlengine/standaloneEnv/src/oracle/cep/env/standalone/CartridgeRegistry.java /main/9 2010/06/02 15:49:17 alealves Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/29/09 - fix java cartridge
    alealves    11/27/09 - Data cartridge context, default package support
    anasrini    09/10/09 - add TreeIndexCartridge
    alealves    07/23/09 - Data cartridge
    hopark      06/04/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/standaloneEnv/src/oracle/cep/env/standalone/CartridgeRegistry.java /main/9 2010/06/02 15:49:17 alealves Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.env.standalone;

import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.service.BaseCartridgeRegistry;

public class CartridgeRegistry 
  extends BaseCartridgeRegistry
{
  public CartridgeRegistry()
    throws CartridgeException
  {
    super();
  }

  protected void preRegisterTypeActions(String cartridgeID, ICartridge metadata)
  {
    if (JAVA_CARTRIDGE.equals(cartridgeID))
      setJavaTypeSystem(metadata.getTypeLocator());
  }

  protected void preUnregisterTypeActions(String cartridgeID)
  {
  }
}

