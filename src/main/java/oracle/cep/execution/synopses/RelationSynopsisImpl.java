/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/RelationSynopsisImpl.java hopark_cqlsnapshot/2 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares RelationSynopsisImpl in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  11/18/09 - add code to drop domain indexes in remove
 udeshmuk  10/08/09 - support multiple indexes and combine their results
 hopark    12/02/08 - move LogLevelManaer to ExecContext
 hopark    06/19/08 - logging refactor
 hopark    12/27/07 - support xmllog
 hopark    11/08/07 - handle exception
 hopark    10/25/07 - make evictable
 hopark    09/07/07 - eval refactor
 hopark    06/15/07 - add getIndex
 hopark    06/07/07 - use LogArea
 hopark    05/24/07 - logging support
 najain    04/11/07 - bug fix
 najain    03/14/07 - cleanup
 najain    01/05/07 - spill over support
 parujain  12/19/06 - FullScan for RelationSynopsis
 parujain  12/07/06 - propagating relation
 najain    08/16/06 - concurrency issues
 najain    06/27/06 - add remove 
 najain    06/14/06 - query deletion support 
 najain    06/14/06 - bug fix 
 ayalaman  04/21/06 - set hash index scan for synopsis 
 anasrini  04/05/06 - add method setFullScan 
 najain    03/10/06 - 
 skaluska  03/01/06 - Creation
 skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/RelationSynopsisImpl.java hopark_cqlsnapshot/2 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.CombinedIterator;
import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.FilterIterator;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.ExecContext;

/**
 * Scan Specification
 *
 * @author skaluska
 */
class ScanSpec
{
  /** scan predicate */
  IBEval predicate;
  /** index list */
  List<Index> indexList;

  /**
   * Constructor for ScanSpec
   * 
   * @param predicate
   * @param list of indexes associated with the scan
   */
  public ScanSpec(IBEval predicate, List<Index> indexList)
  {
    this.predicate = predicate;
    this.indexList = indexList;
  }
  
}

/**
 * RelationSynopsisImpl
 * <p>
 * Note that this is currently a Single Threaded Implementation. The single
 * threading is due to pre-initialization of the iterator objects and usage
 * of other entities that could have single threaded implementations (like
 * HashIndex) for example.
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
public class RelationSynopsisImpl extends ExecSynopsis implements
    RelationSynopsis
{
  static final String TAG_INDEXES = "Indexes";
  static final String[] TAG_INDEXES_ATTRIBS = {"Length"};
  
  /** evalContext */
  private IEvalContext              evalContext;
  /** store */
  private RelStore                  store;
  /** Indexes on the synopsis data */
  private ArrayList<Index>          indexes;
  /** Number of indexes */
  private int                       numIndexes;
  /** Scan specifications */
  private ArrayList<ScanSpec>       scans;
  /** Number of scans */
  private int                       numScans;
  /** filterIters */
  private ArrayList<FilterIterator> filterIters;
  /** sourceIters */
  private ArrayList<TupleIterator>  sourceIters;

  /**
   * Constructor for RelationSynopsisImpl
   */
  public RelationSynopsisImpl(ExecContext ec)
  {
    super(ExecSynopsisType.RELATION, ec);

    evalContext = null;
    store = null;
    numScans = 0;
    numIndexes = 0;

    scans   = new ArrayList<ScanSpec>();
    indexes = new ArrayList<Index>();
  }

  public void init()
  {
    evalContext = null;
    store = null;
    stubId = 0;
    numScans = 0;
    numIndexes = 0;

    scans.clear();
    indexes.clear();
    filterIters = null;
    sourceIters = null;
  }


  /** Remove the synopsis */
  public void remove() throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DDL, this, 
                          "remove");
    store.removeStub(stubId);
    
    for(Index idx : indexes)
    {
      idx.clear();
    }
  }

  /**
   * Getter for evalContext in RelationSynopsisImpl
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext in RelationSynopsisImpl
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for store in RelationSynopsisImpl
   * 
   * @return Returns the store
   */
  public RelStore getStore()
  {
    return store;
  }

  /**
   * Setter for store in RelationSynopsisImpl
   * 
   * @param store
   *          The store to set.
   */
  public void setStore(RelStore store)
  {
    this.store = store;
  }

  /**
   * Add a new scan with index
   * @param predicate Predicate to apply
   * @param index Index associated with the scan
   * @return Scanid
   */
  public int setIndexScan(IBEval predicate, Index index)
  {
    ScanSpec s;

    // TODO: revisit. The predicate could be null when an hash index is 
    // being set for find the exact tuple in the synopsis. Some of the 
    // examples of such usage include IStream and DStream. 
    // assert (predicate != null);
    assert(index != null);
    LinkedList<Index> indexList= new LinkedList<Index>();
    indexList.add(index);
    // Allocate the scan spec
    s = new ScanSpec(predicate, indexList);

    // The new scan goes at the end of the list
    scans.add(s);
    numScans++;

    // Add the index to the list
    indexes.add(index);
    numIndexes++;
    
    return scans.size()-1;
  }

  /**
   * add a new scan with indices
   * @param predicate Predicate to apply
   * @param indexList indices associated with the scan
   * @return scanid
   */
  public int setIndexScan(IBEval predicate, List<Index> indexList)
  {
    ScanSpec s;

    assert(indexList != null);

    // Allocate the scan spec
    s = new ScanSpec(predicate, indexList);

    // The new scan goes at the end of the list
    scans.add(s);
    numScans++;

    // Add the index to the list
    indexes.addAll(indexList);
    numIndexes += indexList.size();
    
    return scans.size()-1;
  }

  /**
   * Add a new scan without index
   * @param predicate To evaluate
   * @return Scanid
   */
  public int setScan(IBEval predicate)
  {
    ScanSpec s;

    assert (predicate != null);

    // Allocate the scan spec
    s = new ScanSpec(predicate, null);

    // The new scan goes at the end of the list
    scans.add(s);
    numScans++;

    return scans.size()-1;
  }

  /**
   * Add a new full scan 
   * @return Scanid
   */
  public int setFullScan()
  {
    ScanSpec s;

    // Allocate the scan spec
    s = new ScanSpec(null, null);

    // The new scan goes at the end of the list
    scans.add(s);
    numScans++;

    return scans.size()-1;
  }

  /**
   * Initializes the synopsis
   */
  public void initialize()
  {
    ScanSpec sc;
    Iterator<ScanSpec> i;

    filterIters = new ArrayList<FilterIterator>(numScans);
    sourceIters = new ArrayList<TupleIterator>(numScans);

    i = scans.iterator();
    for (int s = 0; s < numScans; s++) {
      sc = i.next();
      if (sc.predicate != null) {
        filterIters.add(s, new FilterIterator(sc.predicate, evalContext, 
                                              ((ExecStore)store).getFactory()));
      }
      else {
        filterIters.add(s, null);
      }

      sourceIters.add(s, null);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.synopses.RelationSynopsis#insertTuple
   * (oracle.cep.execution.internals.Tuple)
   */
  public void insertTuple(ITuplePtr tuple) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_INSERT, this, 
                          tuple);

    // Insert the tuple into the indexes
    for (int i = 0; i < numIndexes; i++)
      indexes.get(i).insertTuple(tuple);

    // Insert the tuple into the store
    store.insertTuple_r(tuple, stubId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.synopses.RelationSynopsis#deleteTuple
   * (oracle.cep.execution.internals.Tuple)
   */
  public void deleteTuple(ITuplePtr tuple) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this, 
                          tuple);

    // Delete the tuple from the indexes
    try
    {
    for (int i = 0; i < numIndexes; i++)
      indexes.get(i).deleteTuple(tuple);
    }
    catch(ExecException e)
    {
      e.printStackTrace();
      throw e;
    }
    // Delete the tuple from the store
    store.deleteTuple_r(tuple, stubId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.synopses.RelationSynopsis#getScan(int)
   */
  public TupleIterator getScan(int scanId) throws ExecException
  {
    ScanSpec s;
    TupleIterator tupIter, iter;
    FilterIterator filIter;
   
    assert scanId != -1;
    assert (scanId < numScans) : scanId + " >= " + numScans;
    s = scans.get(scanId);
    
    // Get the underlying iterator on the source
    if(s.indexList != null)
    {
      if(s.indexList.size() == 1)
      {
        tupIter = scans.get(scanId).indexList.get(0).getScan();
      }
      else 
        tupIter = combineResults(s);
    }
    else
      tupIter = store.getScan_r(stubId);
    sourceIters.set(scanId, tupIter);
    
    // Create a filter iterator if necessary
    if (s.predicate != null) {
      filIter = filterIters.get(scanId);
      filIter.initialize(this, tupIter);
      iter = filIter;
    }
    else
      iter = tupIter;

    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_START, this, 
                  scanId, iter);

    return iter;
  }
  
  private TupleIterator combineResults(ScanSpec s) 
                                       throws ExecException
  {
    List<Index> indexList = s.indexList;
    List<Set<ITuplePtr>> returnedResultSets = new LinkedList<Set<ITuplePtr>>();
    ListIterator<Index> indexListIter  = indexList.listIterator();
    
    //Iterate through the indices associated with the scan and 
    //accumulate their resultSets.

    while(indexListIter.hasNext())
    {
      Index index = indexListIter.next();
      TupleIterator indexTupleIter = index.getScan();
      
      Set<ITuplePtr> resultSet = new LinkedHashSet<ITuplePtr>();
      ITuplePtr tuplePtr;
      while((tuplePtr = indexTupleIter.getNext()) != null)
      {
        resultSet.add(tuplePtr);
        ((ExecStore)store).getFactory().release(tuplePtr);
      }
      
      index.releaseScan(indexTupleIter);
      
      //if the result set is non-empty, add it to the collection
      if(resultSet.size() > 0)
        returnedResultSets.add(resultSet);
      else
      {
        returnedResultSets.clear();
        break;
      }
    }
      
    return new CombinedIterator(returnedResultSets,
                                ((ExecStore)store).getFactory());
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.synopses.RelationSynopsis#releaseScan(int,
   *      oracle.cep.execution.internals.TupleIterator)
   */
  public void releaseScan(int scanId, TupleIterator iter) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_STOP, this, 
                          scanId, iter);

    ScanSpec s;
    TupleIterator srcIter;

    // Check validity of the scanId
    assert (scanId < numScans);

    // Get out context
    s       = scans.get(scanId);
    srcIter = sourceIters.get(scanId);

    // Sanity Check
    assert srcIter != null;

    // Release the source iterator
    if(s.indexList == null) 
      store.releaseScan_r(srcIter, stubId);

    // if need be, initialize the filterIter also
    if (s.predicate != null) 
    {
      assert srcIter != iter;
      assert iter instanceof FilterIterator;
      ((FilterIterator)iter).release();
    }

    // Reset the iterator being used for scan to null
    sourceIters.set(scanId, null);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<RelationSynopsisImpl id=\"" + id + "\" stubId=\"" +
              stubId + "\" >");
    sb.append("</RelationSynopsisImpl>");
    return sb.toString();
  }

  public List<Index> getIndexes() 
  {
    return indexes;
  }
  
  public synchronized void dump(IDumpContext dump) 
  {
    String tag = LogUtil.beginDumpObj(dump, this);
    ((ExecStore)store).dump(dump);
    LogUtil.endDumpObj(dump, tag);
  }
  
  @Override
  protected void dumpIndex(IDumpContext dumper)
  {
    LogUtil.beginTag(dumper, TAG_INDEXES, TAG_INDEXES_ATTRIBS, numIndexes);
    for (int i = 0; i < numIndexes; i++)
    {
      Index index = indexes.get(i);
      index.dump(dumper);
    }
    dumper.endTag(TAG_INDEXES);
  }
  
  public boolean evict()
    throws ExecException
  {
    boolean res = false;
    for (int i = 0; i < numIndexes; i++)
    {
      Index index = indexes.get(i);
      res |= index.evict();
    }
    return res;
  }

  /**
   * Write full snapshot of synopsis to output stream.
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    throw new IOException("Not enough context information to read synopsis data");  
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    throw new IOException("Not enough context information to write into synopsis");  
  }

  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    // Get the full scan identifier to obtain the iterator on synopsis
    int fullScanId = ctx.getScanId();
    
    // Obtain the iterator using given scan id
    TupleIterator iter = null;
    try
    {
      ArrayList<ITuplePtr> savedTuples = new ArrayList<ITuplePtr>();
      iter = getScan(fullScanId); 
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
      // Release scan if exists
      if(iter != null)
      {
        try
        {
          releaseScan(fullScanId, iter);
        } 
        catch (ExecException e)
        {
          throw new IOException(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    ArrayList<ITuplePtr> recoveredList = (ArrayList<ITuplePtr>) in.readObject();
    HashSet tuplesAlreadyInsertedInStore = ctx.getCache();
    
    int totalRecoveredTuples = recoveredList.size();
    if(recoveredList != null)
    {
      int count=0;
      for(ITuplePtr next: recoveredList)
      {
        try
        { 
          // Reset isRecovered flag of tuple if this tuple is already
          // recovered by other synopsis also.
          // To ensure we don't have duplicate data into relation store
          // we set this flag.
          // Check RelStoreImpl.java to see about detailed usage of this flag.
          if(tuplesAlreadyInsertedInStore.contains(next.getId()))
            next.setRecovered(false);
          else if(!ctx.isSilent())            
          {
            next.setRecovered(true);
            tuplesAlreadyInsertedInStore.add(next.getId());
          }
          // Ensure that RelStore hasn't already recovered same tuple
          if(ctx.isSilent())
          {
            TupleIterator iter = null;
            try
            {
              iter = getScan(ctx.getScanId());
              ITuplePtr nxt= iter.getNext();
              boolean isMatched = false;
              while(nxt != null && !isMatched)
              {
                if(nxt.getId() == next.getId())
                  isMatched = true;
                nxt = iter.getNext();
              }
              next.setRecovered(!isMatched);
            }
            finally 
            {
              if(iter != null)
                releaseScan(ctx.getScanId(), iter);
            }
          }
          
          // isFirstRecovered and isLastRecovered flag will only be used if the
          // underlying store is a window store else flag values will be discarded.
          
          // isFirstRecovered and isLastRecovered flag for a tuple will be used
          // if the window store is being shared by more than one synopsis.
          // In that case, for the first recovered tuple, we need to ensure
          // that the stub (for this syonpsis) points to the right node of global
          // list of window store.
          // Check WinStoreImpl.java for further details about usage of this flag.          
          if(count ==0)
            next.setFirstRecovered(true);
          if(count == totalRecoveredTuples - 1)
            next.setLastRecovered(true);

          // Binding recovered tuple to desired role. This is required by ExtensibleIndexProxy
          if(ctx.isRoleSet())
          {
            evalContext.bind(next, ctx.getRole());
          }
          insertTuple(next);
          count++;
        }        
        catch (ExecException e)
        {
          e.printStackTrace();
          throw new IOException(e.getLocalizedMessage(), e);
        }       
      }
    }
    // Mark that store is recovered after a synopsis recovery from snapshot load
    store.setRecovered(true);
  }

}
