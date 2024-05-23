/* $Header: ExecStoreIter.java 20-mar-2007.15:44:59 najain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      03/14/07 - cleanup
    najain      03/12/07 - 
    hopark      01/22/07 - spill-over support
    parujain    01/12/07 - General Execution Store iterator
    parujain    01/12/07 - Creation
 */

/**
 *  @version $Header: ExecStoreIter.java 20-mar-2007.15:44:59 najain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * ExecStoreIter returns getNext always null
 * @author parujain
 *
 */
public class ExecStoreIter extends StoreImplIter {

  public ExecStoreIter()
  {
    super();
  }

  public ITuplePtr getNext() throws ExecException
  {
    return null;
  }

}
