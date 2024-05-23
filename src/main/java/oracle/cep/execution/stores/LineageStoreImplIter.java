/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/LineageStoreImplIter.java /main/21 2008/12/10 18:55:56 hopark Exp $ */

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
 hopark      06/20/07 - cleanup
 hopark      05/27/07 - logging support
 hopark      05/08/07 - ITuple api cleanup
 najain      04/11/07 - bug fix
 hopark      04/09/07 - fix pincount
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      03/08/07 - cleanup
 najain      02/05/07 - coverage
 najain      01/04/07 - spill over support
 parujain    11/30/06 - Use DoublyListIter Factory
 najain      08/16/06 - concurrency issues
 parujain    08/01/06 - Generic store list
 najain      06/16/06 - bug fix 
 anasrini    04/10/06 - bug fix 
 anasrini    03/22/06 - change constructore to take in colIns and colDel 
 najain      03/14/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/LineageStoreImplIter.java /main/21 2008/12/10 18:55:56 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.util.BitVectorUtil;

/**
 * Lineage Store Iterator
 *
 * @author najain
 */
public class LineageStoreImplIter extends StoreImplIter
{
  private int               stubId;

  /** tuple list */
  private ITupleDoublyListIter   tupleList;

  private ITupleDoublyList tuples;

  private Column            colIns;

  private IAllocator<ITuplePtr>        factory;

  public LineageStoreImplIter(int stubId, Column colIns, IAllocator<ITuplePtr> factory)
  {
    this.stubId = stubId;
    this.colIns = colIns;
    this.factory = factory;
    this.tupleList = null;
  }
   
  public void release(IAllocator<ITupleDoublyListIter> iFactory) 
    throws ExecException
  {
    initialized = false;
    if(tupleList != null)
    {
      tupleList.release(tuples);
      iFactory.release(tupleList);
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
      tupleList = null;
      this.tuples = null;
      return;
    }

    synchronized (tuples)
    {
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
  // getNext is not consistent in the condition when we insert or delete
  // the tuple after initializing the iterator. Currently, the iterator
  // keeps the current location to be read. If there is an insertion
  // before the current location, then the iterator will never read that
  // tuple which is inserted later on. Similar is the case of deletion.
  public ITuplePtr getNext() throws ExecException
  {
    if (!isInitialized())
      throw new ExecException(ExecutionError.ITERATOR_UNINIT);

    if (tuples == null)
      return null;

    ITuplePtr current = null;
    synchronized (tuples)
    {
      while ((current = tupleList.next()) != null)
      {
        if (current == null)
        {
          LogUtil.finest(LoggerType.TRACE, tupleList.toString());
          LogUtil.finest(LoggerType.TRACE, tuples.toString());
        }
        ITuple tuple = current.pinTuple(IPinnable.READ);
        byte[] stubBits = tuple.bValueGet(colIns.getColnum());
        boolean has = BitVectorUtil.checkBit(stubBits, stubId);
        current.unpinTuple();
        if (has)
          break;
        else
        {
          current = null;
        }
      }
    }
    if (current != null)
    {
      factory.addRef(current);
      LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN,
                      logTarget, current);
    }
    return current;
  }
}
