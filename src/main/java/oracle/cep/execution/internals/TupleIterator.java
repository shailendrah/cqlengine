/* $Header: TupleIterator.java 20-mar-2007.15:44:53 najain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares TupleIterator in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    najain    03/14/07 - cleanup
    najain    03/12/07 - bug fix
    najain    01/04/07 - spill over support
    skaluska  02/15/06 - Creation
    skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: TupleIterator.java 20-mar-2007.15:44:53 najain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * TupleIterator interface
 * @author skaluska
 *
 */
public interface TupleIterator {
  /**
   * Get the next tuple in this iteration
   *
   * @return The next tuple or null
   * @throws ExecException 
   */
  abstract ITuplePtr getNext() throws ExecException;
}
