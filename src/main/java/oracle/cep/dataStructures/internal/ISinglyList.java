/* $Header: ISinglyList.java 26-dec-2007.15:45:45 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      12/26/07 - support xmllog
 hopark      10/30/07 - use IList
 hopark      10/15/07 - make it evictable
 hopark      06/19/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/12/07 - bug fix
 najain      03/02/07 - 
 hopark      03/01/07 - Creation
 */

/**
 *  @version $Header: ISinglyList.java 26-dec-2007.15:45:45 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

/**
 *  @version $Header: ISinglyList.java 26-dec-2007.15:45:45 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */
public interface ISinglyList<E> extends IList<E>
{
  /**
   * Removes the first element in the singly linked list
   */
  E remove() throws ExecException;

  /**
   * Removes the first element in the singly linked list
   */
  E remove(boolean freeMem) throws ExecException;

  /**
   * Add the last element from the list to the this list without allocatin
   * memory for the node
   *
   * @param list
   *          The list to allocate memory from
   */
  void addLast(ISinglyList<E> list) throws ExecException;

  void addNext(ISinglyList<E> list) throws ExecException;

  /**
   * Adds an element in the singly linked list. Element will be inserted in the
   * end.
   * 
   * @param elem
   *          Element to be inserted in the list.
   */
  void add(E elem) throws ExecException;
  
  boolean isTailSame(ISinglyList<E> list) throws ExecException;
}

