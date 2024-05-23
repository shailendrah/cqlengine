/* $Header: ITupleDoublyList.java 03-mar-2008.13:30:09 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/07/08 - Creation
 */

/**
 *  @version $Header: ITupleDoublyList.java 03-mar-2008.13:30:09 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.memmgr.IAllocator;

public interface ITupleDoublyList extends IDoublyList<ITuplePtr>
{
  void setTupleFactory(IAllocator<ITuplePtr> fac);
  IAllocator<ITuplePtr> getTupleFactory();
}