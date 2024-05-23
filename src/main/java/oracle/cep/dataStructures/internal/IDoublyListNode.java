/* $Header: IDoublyListNode.java 18-dec-2007.10:30:52 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      11/06/07 - pass IList
 hopark      06/19/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/02/07 - 
 hopark      01/23/07 - creation
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

/**
 * @version $Header: IDoublyListNode.java 18-dec-2007.10:30:52 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public interface IDoublyListNode<E> extends IListNode<E>
{
  void setPrev(IListNode<E> p) throws ExecException;
  <T extends IListNode<E>> T getPrev(IList<E> l) throws ExecException;
}

