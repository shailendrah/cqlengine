/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/WindowSynopsisImpl.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares WindowSynopsisImpl in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  01/21/09 - API to compare two consecutive tuples in synopsis
 hopark    12/02/08 - move LogLevelManaer to ExecContext
 hopark    06/19/08 - logging refactor
 hopark    12/27/07 - support xmllog
 hopark    11/08/07 - handle exception
 hopark    10/22/07 - remove TimeStamp
 hopark    06/07/07 - use LogArea
 hopark    05/24/07 - logging support
 najain    03/14/07 - cleanup
 najain    01/05/07 - spill over support
 parujain  12/19/06 - No fullScan for windowSynopsis
 parujain  12/07/06 - propagating relation
 najain    07/13/06 - ref-count timeStamp support 
 najain    06/27/06 - add remove 
 najain    06/14/06 - query deletion support 
 anasrini  03/24/06 - add toString 
 najain    03/08/06 - change Element to StorageElement
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/WindowSynopsisImpl.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.stores.WinStore;
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.ExecContext;

/**
 * WindowSynopsisImpl
 *
 * @author skaluska
 */
@DumpDesc(attribTags={"Id", "PhyId", "StubId"}, 
          attribVals={"getId", "getPhyId", "getStubId"},
          infoLevel=LogLevel.SYNOPSIS_INFO,
          evPinLevel=LogLevel.SYNOPSIS_TUPLE_PINNED,
          evUnpinLevel=LogLevel.SYNOPSIS_TUPLE_UNPINNED,
          dumpLevel=LogLevel.SYNOPSIS_DUMP,
          verboseDumpLevel=LogLevel.SYNOPSIS_DUMPELEMS)
public class WindowSynopsisImpl extends ExecSynopsis implements WindowSynopsis 
{

  /** store */
  private WinStore store;
  
  /**
   * Constructor for WindowSynopsisImpl
   */
  public WindowSynopsisImpl(ExecContext ec) {
    super(ExecSynopsisType.WINDOW, ec);
  }

  public void init() {
    stubId = 0;
    store = null;
  }

  /** remove the synopsis from the underlying store */
  public void remove() throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DDL, this, "remove");

    store.removeStub(stubId);
  }

  /**
   * Getter for store in WindowSynopsisImpl
   * @return Returns the store
   */
  public WinStore getStore() {
    return store;
  }

  /**
   * Setter for store in WindowSynopsisImpl
   * @param store The store to set.
   */
  public void setStore(WinStore store) {
    this.store = store;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.WindowSynopsis#insertTuple(oracle.cep.execution.queues.Element)
   */
  public void insertTuple(ITuplePtr tuple, long ts) throws ExecException 
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_INSERT, this, tuple, ts);
    store.insertTuple_w(tuple, ts, stubId);
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.WindowSynopsis#isEmpty()
   */
  public boolean isEmpty() throws ExecException {
    return store.isEmpty_w(stubId);
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.WindowSynopsis#getOldestTuple()
   */
  public ITuplePtr getOldestTuple() throws ExecException 
  {
    ITuplePtr ret = store.getOldestTuple_w(stubId);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this, "getOldestTuple", ret);
    return ret;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.WindowSynopsis#getOldestTimeStamp()
   */
  public long getOldestTimeStamp() throws ExecException 
  {
    long ret = store.getOldestTimeStamp_w(stubId);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this, "getOldestTimeStamp", ret);
    return ret;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.WindowSynopsis#deleteOldestTuple()
   */
  public void deleteOldestTuple() throws ExecException 
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this);
    store.deleteOldestTuple_w(stubId);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<WindowSynopsisImpl id=\"" + id + "\">");
    sb.append(store.toString());
    sb.append("</WindowSynopsisImpl>");
    return sb.toString();
  }
  
  public TupleIterator getScan(int scanId) throws ExecException
  {
    assert scanId == -1;

    TupleIterator tupIter = null;
  
    tupIter = store.getScan_r(stubId);
 
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_START, this, 
                  scanId, tupIter);
    return tupIter;
  }
  
  public void releaseScan(int scanId, TupleIterator iter) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_STOP, this, 
                  scanId, iter);
    store.releaseScan_r(iter, stubId);
  }

  public synchronized void dump(IDumpContext dumper) 
  {
    String tag = LogUtil.beginDumpObj(dumper, this);
    ((ExecStore)store).dump(dumper);
    LogUtil.endDumpObj(dumper, tag);
  }
  
  public boolean compareConsecutiveTuples(Window window) throws ExecException
  { 
    return store.compareConsecutiveTuples(window, stubId);
  }

  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    int scanId = ctx.getScanId();
    TupleIterator iter = null;
    try
    {
      iter = this.getScan(scanId);
      ITuplePtr next = iter.getNext();
      ArrayList<ITuplePtr> savedTuples = new ArrayList<ITuplePtr>();
      while(next != null)
      { 
        savedTuples.add(next);
        next = iter.getNext();
      }
      out.writeObject(savedTuples);
    } 
    catch (ExecException e)
    {
      throw new IOException(e.getLocalizedMessage() ,e);
    }
    finally
    {
      if(iter != null)
      {
        try
        {
          this.releaseScan(scanId, iter);
        } 
        catch (ExecException e)
        {
          throw new IOException(e.getLocalizedMessage() ,e);
        }
      }
    }
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    ArrayList<ITuplePtr> recoveredList = (ArrayList<ITuplePtr>) in.readObject();

    int count=0;
    int numTotalRecovered = recoveredList.size();
    
    if(recoveredList != null)
    {
      for(ITuplePtr next: recoveredList)
      {
        try
        {
          // isFirstRecovered and isLastRecovered flag for a tuple will be used
          // if the window store is being shared by more than one synopsis.
          // In that case, for the first recovered tuple, we need to ensure
          // that the stub (for this syonpsis) points to the right node of global
          // list of window store.
          // Check WinStoreImpl.java for further details about usage of this flag.
          if(count == 0)
            next.setFirstRecovered(true);
          if(count == numTotalRecovered -1)
            next.setLastRecovered(true);
          
          this.insertTuple(next, next.getTimestamp());
          
          count++;
        } 
        catch (ExecException e)
        {
          throw new IOException(e.getLocalizedMessage(), e);
        }
      }
    }
    // Mark that store is recovered after a synopsis recovery from snapshot load
    store.setRecovered(true);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    throw new IOException("Not enough context information to read synopsis data");  
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    throw new IOException("Not enough context information to write in synopsis");  
  }
}
