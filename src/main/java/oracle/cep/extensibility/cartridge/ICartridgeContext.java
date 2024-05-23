/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/ICartridgeContext.java /main/1 2009/12/02 02:35:19 alealves Exp $ */

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
    alealves    Nov 25, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/ICartridgeContext.java /main/1 2009/12/02 02:35:19 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.cartridge;

import java.util.Map;

/**
 * Context for cartridge providers. 
 * 
 * 
 * @author Alex Alves
 *
 */
public interface ICartridgeContext
{
  /**
   * Returns application name associated to a particular invocation of cartridge extensible object.
   * 
   * @return String
   */
  String getApplicationName();
  
  /**
   * Returns properties associated to a particular invocation of a cartridge extensible object.
   * If no properties are present, returns an empty map.
   * 
   * @return Map<String, Object>
   */
  Map<String, Object> getProperties();
  
}
