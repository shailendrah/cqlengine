/* $Header: IQSinglyList.java 03-mar-2008.13:54:42 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    IQSinglyList represents a singly list for a queue.
    It is ISinglyList<ITuplePtr, int, long>.
    However ISinglyList cannot be used directly, as Java does 
    not allow primitive types for generics and we do not want to
    create additional Long object for it.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/30/08 - optimize
    hopark      01/25/08 - return new node
    hopark      12/26/07 - support xmllog
    hopark      12/26/07 - support xmllog
    hopark      10/30/07 - remove IQueueElement
    hopark      10/12/07 - Creation
 */

/**
 *  @version $Header: IQSinglyList.java 03-mar-2008.13:54:42 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

public interface IQSinglyList extends ITupleSinglyList
{
  IQSinglyListNode add(QueueElement elem) throws ExecException;
  IQSinglyListNode add() throws ExecException;
  QueueElement getFirstElem(QueueElement buf) throws ExecException;
  QueueElement removeElem(QueueElement buf) throws ExecException;
}

