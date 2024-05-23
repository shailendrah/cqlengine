/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/RelStoreImplIter.java /main/22 2008/12/10 18:55:56 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      12/02/08 - move LogLevelManager to ExecContext
 hopark      06/19/08 - logging refactor
 hopark      02/28/08 - resurrect refcnt
 hopark      01/01/08 - trace cleanup
 hopark      12/07/07 - cleanup spill
 hopark      11/07/07 - change list api
 hopark      10/31/07 - change DoublyList api
 hopark      12/18/07 - change iterator semantics
 hopark      06/20/07 - cleanup
 hopark      06/07/07 - use LogArea
 hopark      06/07/07 - use LogArea
 hopark      05/28/07 - logging support
 hopark      05/08/07 - ITuple api cleanup
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/08/07 - cleanup
 najain      03/06/07 - bug fix
 najain      02/05/07 - coverage
 najain      01/15/07 - spill-over support
 hopark      01/09/07 - Supports TimedTuple for partition window store
 parujain    11/30/06 - Use DoublyListIter Factory
 najain      08/16/06 - concurrency issues
 parujain    07/28/06 - Generic doubly linkedlist 
 najain      06/15/06 - cleanup
 najain      06/15/06 - bug fix
 ayalaman    04/30/06 - fix iterator initialization bug 
 najain      04/19/06 - add initialize 
 anasrini    03/22/06 - add colIns and colDel to constructor 
 najain      03/13/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/RelStoreImplIter.java /main/22 2008/12/10 18:55:56 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.util.BitVectorUtil;

/**
 * Rel Store Iterator
 *
 * @author najain
 */
public class RelStoreImplIter extends StoreImplIter
{
  IAllocator<ITuplePtr>                factory;

  private int               stubId;

  private Column            colIns;

  /** tuple list */
  private ITupleDoublyListIter       tupleList;
  private ITupleDoublyList    tuples;
  ITuplePtr                         t;

  public RelStoreImplIter(int stubId, Column colIns, IAllocator<ITuplePtr> factory)
  {
    this.factory = factory;
    this.stubId = stubId;
    this.colIns = colIns;
    this.tupleList = null;
    this.t = null;
  }

  /**
   * @return Returns the colIns.
   */
  public Column getColIns()
  {
    return colIns;
  }

  /**
   * @param colIns
   *          The colIns to set.
   */
  public void setColIns(Column colIns)
  {
    this.colIns = colIns;
  }

  public void release(IAllocator<ITupleDoublyListIter> iFactory) 
    throws ExecException
  {
    initialized = false;

    if (t != null)
    {
      t = null;
    }

    if (tupleList != null)
    {
      tupleList.release(tuples);
      iFactory.release(tupleList);
    }

    tupleList = null;
  }

  public void initialize(ILoggable target, 
                         ITupleDoublyList tuples, 
                         IAllocator<ITupleDoublyListIter> iFactory) 
    throws ExecException
  {
    super.initialize(target);
    if (t != null)
    {
      t = null;
    }

    if (tuples == null)
    {
      if (tupleList != null)
	tupleList.initialize(tuples);
      return;
    }

    synchronized (tuples)
    {
      if (tupleList == null)
        tupleList = iFactory.allocate();
      tupleList.initialize(tuples);

      this.tuples = tuples;

      super.initialize(target);

      if (tupleList != null)
      {
        // get the next tuple
        while ( (t = tupleList.next()) != null)
        {
          ITuple tuple = t.pinTuple(IPinnable.READ);
          byte[] stubBits = tuple.bValueGet(colIns.getColnum());
          boolean has = BitVectorUtil.checkBit(stubBits, stubId);
          t.unpinTuple();

          if (has)
            break;
          else
	  {
            t = null;
	  }
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.internals.TupleIterator#getNext()
   */
  // getNext is not consistent in the condition when we insert or delete
  // the tuple after initializing the iterator. Currently, the iterator
  // keeps the current location to be read. If there is an insertion
  // before the current location, then the iterator will never read that
  // tuple which is inserted later on. Similar is the case of deletion.
  public ITuplePtr getNext() throws ExecException
  {
    if (!isInitialized())
      throw new ExecException(ExecutionError.ITERATOR_UNINIT);

    ITuplePtr curr = t;

    if (curr == null)
      return null;

    synchronized (tuples)
    {
      // get the next tuple
      while ( (t = (ITuplePtr) tupleList.next()) != null)
      {
        ITuple tuple = t.pinTuple(IPinnable.READ);

        byte[] stubBits = tuple.bValueGet(colIns.getColnum());
        boolean has = BitVectorUtil.checkBit(stubBits, stubId);
        t.unpinTuple();

        if (has)
          break;
        else
        {
	        t = null;
	}
      }
    }
    if (curr != null)
    {
      factory.addRef(curr);
      LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN,
                    logTarget, curr);
    }
    return curr;
  }
}
