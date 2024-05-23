/* $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/LineageSynopsisImpl.java /main/13 2008/12/10 18:55:57 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares LineageSynopsisImpl in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    12/02/08 - move LogLevelManaer to ExecContext
    hopark    06/18/08 - logging refactor
    hopark    12/27/07 - support xmllog
    hopark    12/05/07 - cleanup spill
    hopark    11/08/07 - handle exception
    parujain  12/17/07 - db-join
    hopark    06/07/07 - use LogArea
    hopark    05/24/07 - logging support
    hopark    04/27/07 - refcount debug
    najain    03/14/07 - cleanup
    najain    01/05/07 - spill over support
    parujain  12/19/06 - No fullScan for LineageSynopsis
    parujain  12/07/06 - propagating relation
    najain    06/28/06 - add init
    anasrini  03/24/06 - add toString 
    najain    03/10/06 - account for Exceptions
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/LineageSynopsisImpl.java /main/13 2008/12/10 18:55:57 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.stores.LineageStoreInternal;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 * LineageSynopsisImpl
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
public class LineageSynopsisImpl extends ExecSynopsis implements
    LineageSynopsis
{
  /** The store that stores the tuples of the synopsis */
  private LineageStore store;

  /**
   * Constructor for LineageSynopsisImpl
   */
  public LineageSynopsisImpl(ExecContext ec)
  {
    super(ExecSynopsisType.LINEAGE, ec);
  }

  public void init()
  {
    stubId = 0;
    store = null;
  }

  /** Remove the synopsis */
  public void remove() throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DDL, this, 
                  "remove", this);
    store.removeStub(stubId);
  }

  /**
   * Getter for store in LineageSynopsisImpl
   * @return Returns the store
   */
  public LineageStore getStore()
  {
    return store;
  }

  /**
   * Setter for store in LineageSynopsisImpl
   * @param store The store to set.
   */
  public void setStore(LineageStore store)
  {
    this.store = store;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.LineageSynopsis#insertTuple(oracle.cep.execution.internals.Tuple, oracle.cep.execution.internals.Tuple[])
   */
  public void insertTuple(ITuplePtr tuple, ITuplePtr[] lineage) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_INSERT, this, tuple);
    store.insertTuple_l(tuple, lineage, stubId);
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.synopses.LineageSynopsis#deleteTuple(oracle.cep.execution.internals.Tuple)
   */
  public void deleteTuple(ITuplePtr tuple) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this, tuple);
    store.deleteTuple_l(tuple, stubId);
  }

  public TupleIterator getScan_l(ITuplePtr[] lineage) throws ExecException
  {
    TupleIterator iter = store.getScan_l(lineage, stubId);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_START, this, 
                  "getScan_l", lineage, stubId, iter);
    return iter;
  }
  
  public void releaseScan_l(TupleIterator scan) throws ExecException
  {
    store.releaseScan_l(scan);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_STOP, this, 
    		"releaseScan_l", scan, stubId);
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

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<LineageSynopsisImpl id=\"" + id + "\" stubId=\"" +
              stubId + "\" >");
    sb.append(store.toString());
    sb.append("</LineageSynopsisImpl>");
    return sb.toString();
  }


  public synchronized void dump(IDumpContext dump) 
  {
    String tag = LogUtil.beginDumpObj(dump, this);
    ((ExecStore)store).dump(dump);
    LogUtil.endDumpObj(dump, tag);
  }
  
  /**
   * Write full snapshot of synopsis to output stream.
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    TupleIterator iter = null;
    try 
    {
      iter = store.getScan_r(stubId);
      ArrayList<ITuplePtr> savedTuples = new ArrayList<ITuplePtr>();
      ITuplePtr next = iter.getNext();
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
      if(iter !=null )      
      {
        try 
        {
          store.releaseScan_r(iter, stubId);
        } 
        catch (ExecException e) 
        {
          throw new IOException(e.getLocalizedMessage() ,e);
        }
      }
    }
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    LineageStoreInternal linStore = null;
    if(store instanceof LineageStoreInternal)
      linStore = (LineageStoreInternal)store;
    else
      throw new IOException("Synopsis is not loadable from given input stream");
    
    ArrayList<ITuplePtr> recoveredTuples = (ArrayList<ITuplePtr>) in.readObject();
    
    if(recoveredTuples != null)
    {
      ArrayList<Column> colLineage = linStore.getColLineage();
      for(ITuplePtr recovered: recoveredTuples)
      {
        try 
        {
          // Create a lineage tuple id array
          long[] lineageTupleIds = new long[linStore.getNumLins()];
          ITuple tuple = recovered.pinTuple(IPinnable.READ);
          
           // Instantiate the lineage array
          for(int i=0; i < lineageTupleIds.length; i++)       
            lineageTupleIds[i] = tuple.lValueGet(colLineage.get(i).getColnum());

          recovered.unpinTuple();
          
          // Insert recovered tuple with lineage into store
          linStore.insertTuple_l(recovered, lineageTupleIds, stubId);
        } 
        catch (ExecException e) 
        {
          throw new IOException(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    throw new IOException("API not supported. Please use writeExternal(ObjectOutput)");
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    throw new IOException("API not supported. Please use readExternal(ObjectInput)");
    
  }
}
