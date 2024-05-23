/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IIterableType.java /main/2 2010/03/20 08:53:21 sbishnoi Exp $ */

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
      sbishnoi  01/29/10 - adding new interface method getComponentType
    alealves    Sep 8, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/type/IIterableType.java /main/2 2010/03/20 08:53:21 sbishnoi Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.type;

import java.util.Iterator;

/**
 * This interface indicates that the type is iterable,  that is, it can contains component elements.
 * 
 * @author Alex Alves
 *
 */
public interface IIterableType extends IType
{
  /**
   * Returns iterator that is able to traverse type 
   * 
   * @param obj instance of the iterable type
   * @return iterator
   */
  Iterator<Object> iterator(Object obj);
  
  /**
   * Returns iterator that is able to traverse type whose component's must be of type 
   *  <code>componentType</code>.
   *  
   * If component is not of type <code>component</code>, then a ClassCastException is raised if the 
   *  component type is known at creation time. Otherwise, a ClassCastException 
   *  is thrown when calling <code>Iterator.next()</code>
   *  
   * @param obj instance of the iterable type
   * @param componentType type of the component of this type
   * @return iterator 
   */
  <T> Iterator<T> iterator(Object obj, IType componentType) 
    throws ClassCastException;
 
   /**
     * Returns type of array component
     * @return IType
               null if the component type cannot be determined
     */
   IType getComponentType();
		 
}
