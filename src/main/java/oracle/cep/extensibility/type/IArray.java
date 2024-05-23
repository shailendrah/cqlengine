/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IArray.java /main/1 2009/09/19 05:25:36 alealves Exp $ */

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

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IArray.java /main/1 2009/09/19 05:25:36 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;

/**
 * REVIEW As we don't have assignments, there is no need for a setter.
 * 
 * @author Alex Alves
 *
 */
public interface IArray extends IRuntimeInvocable
{
  /**
   * Gets arg from array.
   * 
   * @param index array index
   * @return Object
   * @throws RuntimeInvocationException
   */
  Object get(Object obj, int index)
    throws RuntimeInvocationException, ArrayIndexOutOfBoundsException;

  /**
   * Instantiate array of component type with length size
   * 
   * @param componentType
   * @param length
   * @return
   * @throws RuntimeInvocationException
   */
  Object instantiate(IType componentType, int length)
    throws RuntimeInvocationException;
  
}