/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/HashIndex.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares HashIndex in package oracle.cep.execution.indexes.
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>
 MODIFIED    (MM/DD/YY)
 anasrini  12/19/10 - replace eval() with eval(ec)
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    10/10/08 - remove statics
 udeshmuk  09/23/08 - replace hashtable by hashmap
 hopark    06/18/08 - logging refactor
 hopark    03/03/08 - set TupleFactory for list
 hopark    02/28/08 - resurrect refcnt
 hopark    02/05/08 - parameterized error
 hopark    12/26/07 - add xmllog support
 hopark    01/03/08 - remove refcnt
 hopark    12/07/07 - cleanup spill
 hopark    12/04/07 - nodeFactory life cycle
 hopark    11/15/07 - init NodeFac
 hopark    10/31/07 - change DoublyList api
 hopark    12/18/07 - change iterator semantics
 hopark    11/29/07 - remove IDoublyList2
 hopark    10/25/07 - make evictable
 hopark    10/16/07 - use local node factory
 hopark    10/22/07 - remove TimeStamp
 hopark    09/07/07 - eval refactor
 hopark    08/03/07 - structured log
 hopark    06/19/07 - cleanup
 hopark    06/07/07 - use LogArea
 hopark    06/07/07 - use LogArea
 hopark    05/24/07 - debug logging
 najain    04/11/07 - bug fix
 hopark    04/05/07 - memmgr reorg
 najain    04/03/07 - bug fix
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/05/07 - spil over support
 parujain  12/01/06 - Iterato memory re-use
 najain    11/08/06 - use DoublyList
 najain    08/16/06 - concurrency issues
 ayalaman  08/11/06 - implement full scan
 najain    03/31/06 - fix bugs
 skaluska  03/01/06 - Creation
 skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/HashIndex.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package

oracle.cep.execution.indexes;

import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.Hash;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.memmgr.FactoryManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

/**
 * This is a single threaded hash index implementation. The (hash value, list of
 * tuples) pairs are stored in a hash map. TODO: It might make sense to move
 * the HEval execution into the Hash.hashCode method. This will need to be
 * coordinated
 *
 * @author skaluska
 */
@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"},
          infoLevel=LogLevel.INDEX_INFO,
          evPinLevel=LogLevel.INDEX_TUPLE_PINNED,
          evUnpinLevel=LogLevel.INDEX_TUPLE_UNPINNED,
          dumpLevel=LogLevel.INDEX_DUMP,
          verboseDumpLevel=LogLevel.INDEX_DUMPELEMS)
public class HashIndex implements Index, ILoggable
{
  /** index id */
  protected int id;
  private static AtomicInteger nextId = new AtomicInteger();

  /** Storage manager for the list */
  private IAllocator<ITupleDoublyList> lFactory;

  /** Evaluation context */
  private IEvalContext evalContext;

  private IAllocator<ITuplePtr>                   factory;

  private IAllocator                              nodeFactory;
  
  private static final int UPDATE_ROLE = IEvalContext.UPDATE_ROLE;

  /** Storage manager for doubly list iterator */
  private IAllocator<ITupleDoublyListIter> iFactory;

  /** Hashes the keys of a (bound) tuple. */
  private IHEval updateHashEval;

  /** Used to eliminate false positives during scan. */
  private IBEval keyEqual;

  /** Computes the hash for a scan */
  private IHEval scanHashEval;

  /** Hash map of tuples */
  private HashMap<Hash, ITupleDoublyList> ht;

  /** iterator */
  private HashIndexIterator iter;

  /** temp hash */
  private Hash hash;

  private ExecContext execContext;
  
  /**
   * Constructor for HashIndex
   * @param ec TODO
   */
  public HashIndex(ExecContext ec)
  {
    execContext = ec;
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    lFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_FACTORY_ID);
    iFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_ITER_FACTORY_ID);
    nodeFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_NODE_FACTORY_ID);
    id = nextId.incrementAndGet();
    ht = new HashMap<Hash, ITupleDoublyList>();
    hash = new Hash(0);
    LogLevelManager.trace(LogArea.INDEX, LogEvent.INDEX_DDL, this, "new");
  }

  public String getName() {return StringUtil.getBaseClassName(this);}
  public int getId() {return id;}
  public int getSize() {return ht.size();}
  
  /**
   * Getter for evalContext in HashIndex
   *
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext in HashIndex
   *
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for keyEqual in HashIndex
   *
   * @return Returns the keyEqual
   */
  public IBEval getKeyEqual()
  {
    return keyEqual;
  }

  /**
   * Setter for keyEqual in HashIndex
   *
   * @param keyEqual
   *          The keyEqual to set.
   */
  public void setKeyEqual(IBEval keyEqual)
  {
    this.keyEqual = keyEqual;
  }

  /**
   * Getter for scanHashEval in HashIndex
   *
   * @return Returns the scanHashEval
   */
  public IHEval getScanHashEval()
  {
    return scanHashEval;
  }

  /**
   * Setter for scanHashEval in HashIndex
   *
   * @param scanHashEval
   *          The scanHashEval to set.
   */
  public void setScanHashEval(IHEval scanHashEval)
  {
    this.scanHashEval = scanHashEval;
  }

  /**
   * Getter for updateHashEval in HashIndex
   *
   * @return Returns the updateHashEval
   */
  public IHEval getUpdateHashEval()
  {
    return updateHashEval;
  }

  /**
   * Setter for updateHashEval in HashIndex
   *
   * @param updateHashEval
   *          The updateHashEval to set.
   */
  public void setUpdateHashEval(IHEval updateHashEval)
  {
    this.updateHashEval = updateHashEval;
  }

  public void setFactory(IAllocator<ITuplePtr> factory)
  {
    this.factory = factory;
  }

  public void initialize()
  {
    // Create a hash index iterator
    assert factory != null;
    iter = new HashIndexIterator(evalContext, keyEqual, factory, iFactory);
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.indexes.Index#clear()
   */
  public synchronized void clear()
  {
    Set<Hash> keyset = ht.keySet();
    Iterator<Hash> keyitr = keyset.iterator();
    while (keyitr.hasNext())
    {
      Hash key = keyitr.next();
      ITupleDoublyList val = ht.get(key);
      keyitr.remove();
      lFactory.release(val);
    }
    ht.clear();

    // We are done with the nodeFactory.
    nodeFactory = null;
  }
  
  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.indexes.Index#insertTuple(oracle.cep.execution.internals.Tuple)
   */

  public void insertTuple(ITuplePtr tuple) throws ExecException
  {
    LogLevelManager.trace(LogArea.INDEX, LogEvent.INDEX_INSERT, this, tuple);

    ITupleDoublyList value;

    synchronized (this)
    {
      // Compute the hash of the tuple
      evalContext.bind(tuple, UPDATE_ROLE);
      updateHashEval.eval(hash, evalContext);

      // Check value in hash map
      value = ht.get(hash);

      // Insert new list if empty
      if (value == null)
      {
        value = lFactory.allocate();
        value.setTupleFactory(factory);
        value.setFactory(nodeFactory);
        
        ht.put(hash, value);
      }

      synchronized (value)
      {
        // Add current tuple
        value.add(tuple);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.indexes.Index#deleteTuple(oracle.cep.execution.internals.Tuple)
   */

  public void deleteTuple(ITuplePtr tuple) throws ExecException
  {
    LogLevelManager.trace(LogArea.INDEX, LogEvent.INDEX_DELETE, this, tuple);

    ITupleDoublyList value;

    synchronized (this)
    {
      // Compute the hash of the tuple
      evalContext.bind(tuple, UPDATE_ROLE);
      updateHashEval.eval(hash, evalContext);
      // Check value in hash map 
      value = ht.get(hash);
      // List can't be empty
      if (value == null)
        throw new ExecException(ExecutionError.INVALID_TUPLE, tuple.toString());

      synchronized (value)
      {
        // Remove current tuple
        if (!value.remove(tuple))
          throw new ExecException(ExecutionError.INVALID_TUPLE, tuple.toString());

        if (value.isEmpty())
        {
          ITupleDoublyList res = ht.remove(hash);
          assert res == value;
          lFactory.release(value);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.indexes.Index#getScan()
   */

  public TupleIterator getScan() throws ExecException
  {
    ITupleDoublyList value;

    synchronized (this)
    {
      // Compute the hash
      scanHashEval.eval(hash, evalContext);

      // Check value in hash map 
      if (ht.isEmpty())
        value = null;
      else
        value = ht.get(hash);

      // Initialize my iterator
      iter.initialize(this, value, iFactory);

      LogLevelManager.trace(LogArea.INDEX, LogEvent.INDEX_SCAN_START, this, iter);
      return iter;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.indexes.Index#releaseScan(oracle.cep.execution.internals.TupleIterator)
   *
   */
  public TupleIterator getFullScan()  throws ExecException
  {
    return new FullScanIterator();
  }
  
  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.indexes.Index#releaseScan(oracle.cep.execution.internals.TupleIterator)
   */

  public void releaseScan(TupleIterator iter) throws ExecException
  {
    LogLevelManager.trace(LogArea.INDEX, LogEvent.INDEX_SCAN_STOP, this, iter);
    assert iter instanceof StoreImplIter;
    ((StoreImplIter)iter).release();
  }

  
  /******************************************************************/
  // FullScanInterator
  private class FullScanIterator extends StoreImplIter
  {
    Iterator<ITupleDoublyList> m_values;
    ITupleDoublyListIter       m_curListItr;
    ITupleDoublyList           m_curList;
    
    public FullScanIterator()
    {
      m_values = ht.values().iterator();
      m_curList = null;
      m_curListItr = null;
    }
    
    public void release()  throws ExecException
    {
      if (m_curListItr != null)
      {
        m_curListItr.release(m_curList);
        iFactory.release(m_curListItr);
      }
    }
    
    public ITuplePtr getNext() throws ExecException
    {
      ITuplePtr res = null;
      boolean has = false;
      if (m_curListItr != null)
      {
        res = m_curListItr.next();
        has = (res != null);
      }
      else 
      {
        m_curListItr = iFactory.allocate();
      }
      if (!has)
      {
        if (m_values.hasNext())
        {
          m_curListItr.initialize(m_values.next());
          res = m_curListItr.next();
        }
      }
      return res;
    }
  }
  
  /******************************************************************/
  // ILoggable implementation
  public String getTargetName()
  {
    return StringUtil.getBaseClassName(this);
  }

  public int getTargetId()
  {
    return id;
  }

  public int getTargetType()
  {
    return -1;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
  }

  public synchronized void dump(IDumpContext dumper)
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    LogLevelManager lm = execContext.getLogLevelManager();
    IDumpContext b = lm.openDumper(dumperKey, dumper);
    String tag = LogUtil.beginDumpObj(b, this);
    assert (LogTags.INDEX_ENTRY_ATTRIB.length == 1);
    Object[] evals = new Object[1]; 
    for (Entry<Hash, ITupleDoublyList> e : ht.entrySet()) 
    {
      Hash hash = e.getKey();
      evals[0] = hash;
      ITupleDoublyList val = e.getValue();
      b.beginTag(LogTags.INDEX_ENTRY, LogTags.INDEX_ENTRY_ATTRIB, evals);
      val.dump(b);
      b.endTag(tag);
    }
    LogUtil.endDumpObj(b, tag);
    lm.closeDumper(dumperKey, dumper, b);
  }

  public boolean evict()
    throws ExecException
  { 
    boolean res = false;
    for (ITupleDoublyList val: ht.values())
    {
      res |= val.evict();
    }
    return res;
  }
}
