/* $Header: pcbpel/cep/server/src/oracle/cep/service/IUserFunctionLocator.java /main/1 2009/02/12 03:52:37 alealves Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    01/30/09 - creation

 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/service/IUserFunctionLocator.java /main/1 2009/02/12 03:52:37 alealves Exp $
 *  @author  alealves
 *  @since   
 */
package oracle.cep.service;

import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.SingleElementFunction;


/**
 * Locates user functions (aggregate and single element) as object instances. 
 *
 */
public interface IUserFunctionLocator
{
  /**
   * Returns object instance of user defined function associated to
   *  <code>name</code>
   *   
   * @param name instance name
   * @return Object UDF object instance, or null if not found 
   */
  SingleElementFunction getUserFunction(String name);
  
  /**
   * Returns object instance of user defined aggregate function associated to
   *  <code>name</code>
   *   
   * @param name instance name
   * @return Object UDA object instance, or null if not found 
   */
  IAggrFnFactory getUserAggrFunction(String name);
}
