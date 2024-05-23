/* $Header: ITupleSinglyList.java 03-mar-2008.13:30:08 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/08/08 - Creation
 */

/**
 *  @version $Header: ITupleSinglyList.java 03-mar-2008.13:30:08 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of sproduct did this appear in)
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.memmgr.IAllocator;

public interface ITupleSinglyList extends ISinglyList<ITuplePtr>
{
  void setTupleFactory(IAllocator<ITuplePtr> fac);
  IAllocator<ITuplePtr> getTupleFactory();
}