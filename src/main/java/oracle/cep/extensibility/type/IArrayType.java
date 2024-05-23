/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IArrayType.java /main/2 2010/03/20 08:53:21 sbishnoi Exp $ */

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
      sbishnoi  01/29/10 - migrating getComponentType to IIterableTytpe
    alealves    Sep 2, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IArrayType.java /main/2 2010/03/20 08:53:21 sbishnoi Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.type;

/**
 * Array types are types that contain inner components.
 * 
 * @author Alex Alves
 *
 */
public interface IArrayType extends IIterableType
{
  /**
   * Returns runtime implementation to access array.
   * 
   * @return runtime implementation.
   */
  IArray getArrayImplementation();
  
}
