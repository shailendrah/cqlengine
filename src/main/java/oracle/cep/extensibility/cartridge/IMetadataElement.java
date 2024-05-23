/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/IMetadataElement.java /main/3 2009/10/08 12:59:28 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    Sep 2, 2009 - Creation
 */
package oracle.cep.extensibility.cartridge;

public interface IMetadataElement
{
  /**
   * Returns full-name of meta-data element.
   * 
   * @return String
   */
  String getName();
  
  /**
   * Returns schema for meta-data element.
   * If meta-data is built-in, then null is returned.
   * If meta-data is defined by user, then the service schema is used.
   * 
   * @return meta-data element schema
   */
  String getSchema();
  
}
