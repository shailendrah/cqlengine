/* $Header: IListNodeHandle.java 18-dec-2007.10:30:52 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/06/07 - pass IList
    hopark      09/22/07 - Creation
 */

/**
 *  @version $Header: IListNodeHandle.java 18-dec-2007.10:30:52 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal;

import java.io.Serializable;

import oracle.cep.execution.ExecException;

public interface IListNodeHandle<E> extends Serializable
{
  <T extends IListNode<E>> T getNode(IList<E> l, int mode) throws ExecException;
}
