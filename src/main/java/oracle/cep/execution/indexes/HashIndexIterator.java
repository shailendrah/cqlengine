/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/HashIndexIterator.java /main/23 2011/02/07 03:36:25 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares HashIndexIterator in package oracle.cep.execution.internals.
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>
 MODIFIED    (MM/DD/YY)
 anasrini  12/19/10 - replace eval() with eval(ec)
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    06/19/08 - logging refactor
 hopark    02/28/08 - resurrect refcnt
 hopark    01/01/08 - trace cleanup
 hopark    01/03/08 - remove refcnt
 hopark    12/07/07 - cleanup spill
 hopark    11/07/07 - change list api
 hopark    10/31/07 - change DoublyList api
 hopark    12/18/07 - change iterator semantics
 hopark    09/17/07 - cleanup
 hopark    09/07/07 - eval refactor
 hopark    06/19/07 - cleanup
 hopark    06/07/07 - use LogArea
 hopark    05/27/07 - logging support
 hopark    04/08/07 - fix pincount
 najain    04/03/07 - bug fix
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    03/08/07 - cleanup
 najain    01/05/07 - spil over support
 parujain  12/01/06 - Iterato memory re-use
 najain    11/08/06 - use DoublyList
 najain    08/16/06 - concurrency issues
 ayalaman  04/30/06 - fix iterator initialization bug
 ayalaman  04/29/06 - handle empty list in getNext
 skaluska  03/01/06 - Creation
 skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/HashIndexIterator.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:43 anasrini Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.indexes;

import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator;

/**
 * HashIndexIterator
 *
 * @author skaluska
 */
public class HashIndexIterator extends StoreImplIter
{
  /** evalContext */
  private IEvalContext evalContext;

  private static final int SCAN_ROLE = IEvalContext.SCAN_ROLE;

  private IAllocator<ITuplePtr> factory;
  private IAllocator<ITupleDoublyListIter> iFactory;
  
  /** keyEqual */
  private IBEval keyEqual;

  /** tuple list */
  private ITupleDoublyListIter   tupleList;

  private ITupleDoublyList tuples;

  /**
   * Constructor for HashIndexIterator
   *
   * @param evalContext
   *          Evaluation context
   * @param keyEqual
   *          Key equality check
   */
  public HashIndexIterator(IEvalContext evalContext, 
                           IBEval keyEqual, 
                           IAllocator<ITuplePtr> factory,
                           IAllocator<ITupleDoublyListIter> iFactory)
  {
    this.evalContext = evalContext;
    this.keyEqual = keyEqual;
    this.factory = factory;
    this.iFactory = iFactory;
    this.tupleList = null;
  }
  
  public void release() 
    throws ExecException
  {
    initialized = false;

    if (tupleList != null)
    {
      tupleList.release(tuples);
      iFactory.release(tupleList);
      tupleList = null;
    }
  }

  public void initialize(ILoggable target, 
                         ITupleDoublyList tuples, 
                         IAllocator<ITupleDoublyListIter> iFactory) 
    throws ExecException
  {
    super.initialize(target);
    if (tuples == null)
    {
      if (tupleList != null)
	      tupleList.initialize(tuples);
      this.tuples = null;
      return;
    }
    synchronized (tuples)
    {
      if (tupleList == null)
        tupleList = iFactory.allocate();
      tupleList.initialize(tuples);
      this.tuples = tuples;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.internals.TupleIterator#getNext()
   */
  public ITuplePtr getNext() throws ExecException
  {
    if (!isInitialized())
      throw new ExecException(ExecutionError.ITERATOR_UNINIT);

    if (tuples == null)
      return null;

    ITuplePtr current = null;
    synchronized (tuples)
    {
      // get the next tuple that matches the tuple in eval context
      while ( (current = tupleList.next()) != null)
      {
        evalContext.bind(current, SCAN_ROLE);

        // found the tuple
        if (keyEqual.eval(evalContext))
          break;
        else
        {
          current = null; // to return null if loop ends
        }
      }
    }
    if (current != null)
    {
      factory.addRef(current);
      LogLevelManager.trace(LogArea.INDEX, LogEvent.INDEX_SCAN, logTarget, current);
    }
    return current;
  }
}
