/* $Header: IDoublyListIter.java 18-dec-2007.10:30:52 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/31/07 - use IListIter
 hopark      12/18/07 - change iterator semantics
 hopark      08/27/07 - add setNode
 hopark      08/29/07 - add resetCurrent
 hopark      06/19/07 - cleanup pin
 hopark      04/09/07 - fix pincount
 najain      03/30/07 - bug fix
 hopark      03/26/07 - fix unpin
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/06/07 - bug fix
 hopark      01/23/07 - use StorageElementImpl
 najain      01/15/07 - spill-over support
 parujain    11/30/06 - DoublyList Iterator
 parujain    11/30/06 - Creation
 */

/**
 *  @version $Header: IDoublyListIter.java 18-dec-2007.10:30:52 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

/**
 * This class implements the iterator for Doubly linked list
 * 
 * @author parujain
 * 
 */
public interface IDoublyListIter<E> extends IListIter<E>
{
  void resetCurrent() throws ExecException;
}
