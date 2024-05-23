/* $Header: IListNode.java 27-dec-2007.11:09:12 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Common behavior of nodes in the list.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 IListNode extends IPinnable in order to release node from client (e.g stores).
 Currently, the stored implementation of IListNode maintains the reference of IPage.
 The reference should be released in order for eviction to work.
 The reference hold by the list and list iterator is released by the evict call.
 The reference of IListNode hold by the stores should be released by the unpin call accordingly.
 
 MODIFIED    (MM/DD/YY)
 hopark      12/27/07 - support xmllog
 hopark      12/07/07 - cleanup spill
 hopark      11/06/07 - pass IList
 hopark      11/03/07 - add getId
 hopark      09/22/07 - add getHandle
 hopark      06/19/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/02/07 - 
 hopark      01/23/07 - creation
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.IDumpable;
import oracle.cep.memmgr.IPinnable;

/**
 * @version $Header: IListNode.java 27-dec-2007.11:09:12 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public interface IListNode<E> extends IPinnable<IListNode<E>>, IDumpable
{
  void setNext(IListNode<E> n) throws ExecException;
  <T extends IListNode<E>> T getNext(IList<E> l) throws ExecException;
  void setNodeElem(E elem)  throws ExecException;
  E getNodeElem() throws ExecException;
  IListNodeHandle<E> getHandle(IList<E> l) throws ExecException;
}

