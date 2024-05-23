/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/CartridgeContextHolder.java /main/1 2009/12/02 02:35:11 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      alealves  11/27/09 - Data cartridge context, default package support
    alealves    Nov 16, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/CartridgeContextHolder.java /main/1 2009/12/02 02:35:11 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java;

import oracle.cep.extensibility.cartridge.ICartridgeContext;

/**
 * Allows context to be retrieved from a Java type through a Thread Local.
 * 
 * @author Alex Alves
 *
 */
public class CartridgeContextHolder
{
  protected static ThreadLocal<ICartridgeContext> runtimeContext =
    new ThreadLocal<ICartridgeContext>() {};
    
  /**
   * Returns Cartridge Context associated to this Java type.
   * 
   * @return ICartridgeContext cartridge context
   */
  public static ICartridgeContext get()
  {
    return runtimeContext.get();
  }
}
