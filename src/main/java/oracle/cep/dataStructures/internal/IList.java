/* $Header: IList.java 11-jan-2008.09:15:19 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      01/11/08 - support xmllog
 hopark      12/14/07 - add getId
 hopark      12/04/07 - remove initFactory
 hopark      11/15/07 - add initFactory
 hopark      10/15/07 - make it evictable
 hopark      06/19/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/12/07 - bug fix
 najain      03/02/07 - 
 hopark      03/01/07 - Creation
 */

/**
 *  @version $Header: IList.java 11-jan-2008.09:15:19 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import java.io.Serializable;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.IDumpable;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictableObj;

/**
 *  @version $Header: IList.java 11-jan-2008.09:15:19 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */
public interface IList<E> extends IEvictableObj, IDumpable, Serializable
{
  int getId();
  
  /**
   * Sets the NodeFactory for a particular type of IListNode element
   * 
   * @param fact
   *          Factory
   */
  void setFactory(IAllocator fact);

  /**
   * Gets the NodeFactory
   * 
   * @return fact
   *          Factory
   */
  IAllocator getFactory();
  
  /**
   * @return Returns the size.
   */
  int getSize();

  /**
   * Getter of the first element in the singly linked list
   * 
   * @return First Element
   */
  E getFirst() throws ExecException;

  /**
   * Gets the last element in the doubly linked list
   * 
   * @return Last element in the list
   */
  E getLast() throws ExecException;

  <T extends IListNode<E>> T getHead() throws ExecException;

  <T extends IListNode<E>> T getTail() throws ExecException;

  /**
   * Checks if the linked list has any element or not
   * 
   * @return True/False depending on whether list is empty or not
   */
  boolean isEmpty();

  void clear() throws ExecException;
}

