/* $Header: IDoublyList.java 26-dec-2007.15:45:40 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

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
 hopark      09/22/07 - use ListNodeHandle
 sbishnoi    08/27/07 - add addElement, removeElement
 hopark      06/19/07 - cleanup
 parujain    06/07/07 - 
 najain      03/12/07 - bug fix
 najain      03/12/07 - bug fix
 hopark      01/19/07 - converted to interface
 hopark      01/09/07 - allow passing any object for removing to support TimedTuple
 parujain    11/30/06 - DoublyListIter memory mgmt
 najain      11/08/06 - IDoublyList is a StorageElement
 najain      11/08/06 - bug fix
 parujain    07/28/06 - Generic Doubly Linkedlist 
 parujain    07/28/06 - Creation
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.execution.ExecException;

/**
 * @version $Header: IDoublyList.java 26-dec-2007.15:45:40 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public interface IDoublyList<E> extends IList<E>
{
  /**
   * Removes the first element in the doubly linked list
   * 
   * @return Success/Failure
   */
  boolean removeFirst() throws ExecException;

  /**
   * Removes the last element in the doubly linked list
   * 
   * @return Success/Failure
   */
  boolean removeLast() throws ExecException;

  /**
   * Removes a Inode element from the doubly link list
   * @param e
   *         Element to be removed from the list
   * @throws ExecException
   */
  void remove(IListNodeHandle<E> elem) throws ExecException;
  
  /**
   * Removes a INode element from the doubly linked list
   * 
   * @param elem
   *          Element to be removed from the list
   * @return Success/Failure
   */
  boolean remove(Object objNode) throws ExecException;

  /**
   * Adds an element in the linked list. Element will be inserted in the
   * end.
   * 
   * @param elem
   *          Element to be inserted in the list.
   */
  <T extends IDoublyListNode<E>> T add(E elem) throws ExecException;
  
  /**
   * Gets the first element in the linked list
   * 
   * @return First Element
   */
  E peek() throws ExecException;

  /**
   * Checks whether Doubly linked list contains the elment or not
   * 
   * @param elem
   *          Element to be searched
   * @return Success/Failure
   */
  boolean contains(Object elem) throws ExecException;
}

