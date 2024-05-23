/* $Header: ITimedTupleSinglyList.java 03-mar-2008.13:54:42 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    ITimedTupleSinglyList represents ISinglyList<ITuplePtr, long, E>.
    However IDoublyList cannot be used directly, as Java does 
    not allow primitive types for generics and we do not want to
    create additional Long object for it.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/29/07 - make it shared
    hopark      10/23/07 - Creation
 */

/**
 *  @version $Header: ITimedTupleSinglyList.java 03-mar-2008.13:54:42 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

public interface ITimedTupleSinglyList extends ITupleSinglyList
{
  void  add(ITuplePtr tuple, long ts, int readers) 
    throws ExecException;
}

