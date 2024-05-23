/* $Header: IAllocator.java 08-mar-2008.11:22:19 hopark Exp $ */
/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */
/*
 DESCRIPTION
 IAllocator defines common behavior of factories.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    03/08/08 - add SPAGE
 hopark    02/21/08 - add STOREPAGE
 hopark    03/07/08 - add XMLITEM namespace
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/03/07 - add Page namespace
 hopark    10/30/07 - remove IQueueElement
 hopark    09/18/07 - add getNameSpace
 najain    05/24/07 - add toString
 hopark    03/09/06 - memmgr reorganization
 najain    03/14/07 - cleanup
 najain    06/30/06 - cleanup
 anasrini  03/24/06 - add getId 
 skaluska  02/25/06 - Creation
 */
/**
 *  @version $Header: IAllocator.java 08-mar-2008.11:22:19 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.execution.ExecException;

public interface IAllocator<E>
{
  public enum NameSpace
  {
    TUPLE, TUPLEPTR, NODE, NODE2, SNODE, SNODE2, SNODE3, 
    PARTNNODE, TTNODE, QNODE, QNODE1, PAGE, XMLITEM, TXMLTUPLE,
    WINSTOREPAGE, QPAGE, SQPAGE
  };
  
  /**
   * Return the internal identifier 
   * @return the internal identifier 
   */
  int getId();

  /**
   * Return the namespace that this allocator allocates
   * @return the namespace
   */
  NameSpace getNameSpace();
  
  /**
   * Retun the memory statistics for this allocator
   * @return MemStat
   */
  MemStat getStat();
  
  /**
   * Allocates a new object
   * 
   * @return Reference to the newly allocated object
   */
  E allocate() throws ExecException;

  /**
   * Adds a new reference to the specified StorageElement. This increments 
   * an internal reference count by 1.
   * 
   * @param element
   * The StorageElement to which the reference needs to be added.
   */
  int addRef(E element);

  /**
   * Adds new references to the specified StorageElement. This increments 
   * an internal reference count by the specified count.
   * 
   * @param element
   * The StorageElement to which the reference needs to be added.
   * @param ref
   * The number of new references.
   */
  int addRef(E element, int ref);

  /**
   * Removes a reference to the specified storageElement.
   *
   * @param element
   * The StorageElement to which the reference needs to be added.
   */
  int release(E element);
  
  void dump();

  String toString();
}
