/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/SingleElementFunction.java /main/3 2009/08/31 10:56:55 alealves Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Interface for functions that act on a single data element

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    04/16/07 - Throw Error
    anasrini    06/20/06 - Creation
    anasrini    06/20/06 - Creation
    anasrini    06/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/SingleElementFunction.java /main/3 2009/08/31 10:56:55 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.extensibility.functions;

import oracle.cep.extensibility.cartridge.IRuntimeInvocable;

/**
 * Interface for functions that act on a single data element
 *
 * @since 1.0
 * @author anasrini
 */

public interface SingleElementFunction extends UserDefinedFunction, IRuntimeInvocable {
  
  /**
   * Generic execute method
   * <p>
   * This method will be called by the CEP system to invoke the function
   * @param args array of function arguments. This will always be non-null.
   *             The length of this array is equal to the number of arguments
   *             of this function. The ith element of this array is the ith
   *             argument of the function.
   *             The datatype of this element will be the java equivalent of
   *             the corresponding CEP datatype i.e.
   *                Integer for int
   *                Float   for float
   *                String  for char
   *                byte[]  for byte
   * @return function result. Datatype will be the java equivalent of the CEP
   *         datatype for the return result.
   *         Function return should be non null
   */
  public Object execute(Object[] args) throws UDFException;
}


