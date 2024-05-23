/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/WinStoreImplIter.java /main/20 2008/12/10 18:55:56 hopark Exp $ */

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
 hopark      10/10/08 - remove statics
 hopark      06/19/08 - logging refactor
 hopark      02/28/08 - resurrect refcnt
 hopark      01/01/08 - trace cleanup
 hopark      12/07/07 - cleanup spill
 hopark      11/07/07 - change list api
 hopark      11/29/07 - remove AtomicInteger
 hopark      10/22/07 - remove TimeStamp
 hopark      08/28/07 - use itr factory
 hopark      06/07/07 - use LogArea
 hopark      05/28/07 - logging support
 najain      04/09/07 - ref count bugs
 hopark      04/06/07 - fix pincount
 najain      03/29/07 - cleanup
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      02/13/07 - cleanup
 najain      01/12/07 - spill over support
 najain      08/16/06 - concurrency issues
 parujain    07/26/06 - Generic object 
 parujain    07/25/06 - use generic linkedlist 
 najain      06/14/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/WinStoreImplIter.java /main/20 2008/12/10 18:55:56 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ISinglyListIter;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyList;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;

/**
 * WinStore Iterator
 *
 * @author najain
 */
public class WinStoreImplIter extends StoreImplIter
{
  IAllocator<ITuplePtr> factory;

  ISinglyListIter<ITuplePtr> iter;
  ITimedTupleSinglyList iterstub;
  
  // total number of elements to be returned by the iterator
  int                                 totalElems;
  // number of elements already returned by the iterator
  int                                 iterElems;

  public WinStoreImplIter(FactoryManager factoryMgr, IAllocator<ITuplePtr> factory)
  {
    this.factory = factory;
    try {
      IAllocator<ISinglyListIter<ITuplePtr>> itrfac = 
        factoryMgr.get(FactoryManager.SINGLY_LIST_ITER_FACTORY_ID);
      iter = itrfac.allocate();
    } catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }

  public void clear()
  {
    totalElems = 0;
    iterElems  = 0;
  }
  
  public void initialize(ILoggable target,
                         ITimedTupleSinglyList stub) 
    throws ExecException
  {
    super.initialize(target);
    iterstub = stub;
    iter.initialize(stub);
    assert stub.getSize() > 0;
    totalElems = stub.getSize() - 1;
    iterElems = 0;
  }

  public void release()  throws ExecException
  {
    if (iter != null)
    {
      iter.release(iterstub);
    }
    super.release();
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

    if (iterElems == totalElems)
      return null;
    
    iterElems++;
    ITuplePtr tuple = iter.next();
    factory.addRef(tuple);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN,
                       logTarget, tuple);
    return tuple;
  }
}
