/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/BindingSynopsisImpl.java /main/23 2012/07/25 21:19:31 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/20/12 - empty partition removal on prevRangeExists
    udeshmuk    08/20/09 - pattern re-org
    udeshmuk    05/15/09 - add APIs for activeItem
    udeshmuk    04/21/09 - don't create unsureItem when nonEvent is present.
    udeshmuk    04/01/09 - partn by w/o all matches opt
    udeshmuk    03/17/09 - move getPrevArr to patternpartncontext
    udeshmuk    03/10/09 - restructure bindstore.
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    udeshmuk    08/04/08 - fix bug 7240994
    hopark      06/19/08 - logging refactor
    rkomurav    06/03/08 - add getLitsOfBindListsIter and
                           getListsOfUnsureListsIter
    rkomurav    03/27/08 - rename alphSize to numCorrs
    rkomurav    02/25/08 - add setAlphSize
    hopark      12/27/07 - support xmllog
    hopark      11/08/07 - handle exception
    rkomurav    10/09/07 - cleanup
    rkomurav    10/03/07 - add delete paritition APIs
    rkomurav    09/06/07 - prev(n)
    rkomurav    08/07/07 - prtnby timestamp ordering bug
    anasrini    07/14/07 - support for partition by
    rkomurav    06/27/07 - addEndOfActiveList
    hopark      06/22/07 - logging
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/BindingSynopsisImpl.java /main/23 2012/07/25 21:19:31 pkali Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.pattern.ActiveItem;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.execution.pattern.BindingTreeSetIterator;
import oracle.cep.execution.pattern.PatternPartnContext;
import oracle.cep.execution.pattern.UnsureItem;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.stores.BindStore;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.service.ExecContext;

@DumpDesc(attribTags={"Id", "PhyId", "StubId"}, 
          attribVals={"getId", "getPhyId", "getStubId"},
          infoLevel=LogLevel.SYNOPSIS_INFO,
          evPinLevel=LogLevel.SYNOPSIS_TUPLE_PINNED,
          evUnpinLevel=LogLevel.SYNOPSIS_TUPLE_UNPINNED,
          dumpLevel=LogLevel.SYNOPSIS_DUMP,
          verboseDumpLevel=LogLevel.SYNOPSIS_DUMPELEMS)
public class BindingSynopsisImpl extends ExecSynopsis implements BindingSynopsis
{
  /** store */
  private BindStore store;
  
  /** current partition context */
  private PatternPartnContext currContext;

  /** maximum prev index */
  private int         maxPrevIndex;

  /** if PARTITION BY is present then TRUE, FALSE otherwise */
  private boolean     hasPartnAttrs;
  
  /** true if query has prev with range, false otherwise */
  private boolean     prevRangeExists;
  
  /** Pattern skip clause */
  private PatternSkip patternSkip;
  
  /** if non event query then TRUE , FALSE otherwise */
  private boolean     isNonEvent;
  
  /** if non-event clause has variable duration, false otherwise */ 
  private boolean     isVariableDuration;
  
  /** null Input Tuple */
  private ITuplePtr   nullInputTuple;
  
  /** return array of prev tuples */
  private ITuplePtr[] prevReturnArr;
  
  public BindingSynopsisImpl()
  {
    super();
  }
  
  public BindingSynopsisImpl(ExecContext ec)
  {
    super(ExecSynopsisType.BINDING, ec);
  }
  
  public void initialize() throws ExecException
  {
    if(maxPrevIndex > 0)
    {
      prevReturnArr = new ITuplePtr[maxPrevIndex];
      for(int i=0; i < prevReturnArr.length; i++)
      {
        prevReturnArr[i] = nullInputTuple;
      }
    }
    else
      prevReturnArr = null;
    
    currContext = null;
    store.initialize();
  }
  
  public boolean isEmpty() throws ExecException
  {
    //dummy
    return true;
  }
  
  /**
   * @param store the store to set
   */
  public void setStore(BindStore store)
  {
    this.store = store;
  }

  public void setBindLength(int bindLength)
  {
    store.setBindLength(bindLength);
  }

  public void setNumCorrs(int numCorrs)
  {
    store.setNumCorrs(numCorrs);
  }
  
  public Binding createBinding() throws ExecException
  {
    return store.createBinding();
  }
  
  public Iterator<Binding> getIterator() 
    throws ExecException
  {
    return currContext.getIterator();
  }
  
  public void setAggrTupleFactory(IAllocator<ITuplePtr>  aggrTupleFactory)
  {
    store.setAggrTupleFactory(aggrTupleFactory);
  }
  
  public void setNullInputTuple(ITuplePtr nullInputTuple)
  {
    this.nullInputTuple = nullInputTuple;
    store.setNullInputTuple(nullInputTuple);
  }
  
  public ListIterator<Binding> getUnsureIterator() 
    throws ExecException
  {
    return currContext.getUnsureIterator();
  }
  
  public Binding getBindingB0()
  {
    return store.getBindingB0();
  }
  
  public void addToReadyToOutputBindings(Binding b)
  {
    store.addToReadyToOutputBindings(b);
  }
  
  public PriorityQueue<Binding> getReadyToOutputBindings()
  {
    return store.getReadyToOutputBindings();
  }
  
  public synchronized void dump(IDumpContext dump) 
  {
    String tag = LogUtil.beginDumpObj(dump, this);
    ((ExecStore)store).dump(dump);
    LogUtil.endDumpObj(dump, tag);
  }
  
  public void setPatternSkip(PatternSkip patternSkip)
  {
    this.patternSkip = patternSkip;  
  }
  
  public void setHasPartnAttrs(boolean hasPartnAttrs)
  {
    this.hasPartnAttrs = hasPartnAttrs;  
  }
  
  public void setPrevRangeExists(boolean prevRangeExists)
  {
    this.prevRangeExists = prevRangeExists;
  }
  
  public void setIsNonEvent(boolean isNonEvent)
  {
    this.isNonEvent = isNonEvent;
  }
  
  public void setIsVariableDuration(boolean isVariableDuration)
  {
    this.isVariableDuration = isVariableDuration;
  }
  
  public long getUnsureMinMatchedTs() throws ExecException
  {
    return store.getUnsureMinMatchedTs();
  }
  
  public void setMaxPrevIndex(int maxPrevIndex)
  {
    this.maxPrevIndex = maxPrevIndex;
    store.setMaxPrevIndex(maxPrevIndex);
  }
  
  public void addPrevTuple(ITuplePtr prevTuple, IAllocator<ITuplePtr> 
  inTupleStorageAlloc) throws ExecException
  {
    currContext.addPrevTuple(prevTuple, inTupleStorageAlloc);
  }
  
  public ITuplePtr[] getPrevArr() throws ExecException
  {
    currContext.getPrevArr(prevReturnArr);
    return prevReturnArr;
  }
  
  public void removeEmptyPartns() throws ExecException
  {
    if(((maxPrevIndex == 0) 
                     || (maxPrevIndex > 0 && hasPartnAttrs && prevRangeExists))
        && (currContext.getNumActiveBindings() == 0))
    { // active list is empty
      assert currContext.getUnsureList().size() == 0;
      store.removeEmptyPartns(currContext.getHeaderTuplePtr());
      //set currContext to null;
      currContext.setPtrToListEntry(null);
    }
  }
  
  public void addToActiveBindings(Iterator<Binding> activeIter, Binding binding)
  {
    if(isVariableDuration || ((patternSkip == PatternSkip.ALL_MATCHES) && isNonEvent))
    {
      ((BindingTreeSetIterator) activeIter).add(binding);
    }
    else
      ((ListIterator<Binding>) activeIter).add(binding);
  }
      
  /**
   * Adds the binding at the end of unsurelist of the current partition.
   */
  public void addEndOfFinalList(Binding finalBinding)
  {
    currContext.getUnsureList().add(finalBinding);
    
    /* UnsureItems Treeset is maintained only to quickly find the minMatchedTs. 
     * This functionality is required for normal queries and variable duration
     * nonevent queries with partn attrs for SKIP PAST LAST ROW case.
     */
    if(hasPartnAttrs && (patternSkip == PatternSkip.SKIP_PAST_LAST_ROW)
       && (!isNonEvent || (isNonEvent && isVariableDuration)))
    {
      UnsureItem unsureItem = new UnsureItem(finalBinding.getMatchedTs(),
                                           finalBinding);
      //set ref in binding
      finalBinding.setUnsureItem(unsureItem);
      //insert unsureItem in the TreeSet
      store.addUnsureItem(unsureItem);
    }
  }
  
  /**
   * removes the unsure item from TreeSet of unsure items
   * @param unsureItem unsure item to be deleted
   */
  public void removeUnsureItem(UnsureItem unsureItem)
  {
    store.removeUnsureItem(unsureItem);
  }
  
  /**
   * Gets the partition context for the argument tuple
   */
  public void setPartnContext(ITuplePtr inputTuple) throws ExecException
  {
    currContext = store.getPartnContext(inputTuple);
  }
  
  /**
   * Sets the currContext to the argument value.
   * Used in non-event cases.
   */
  public void setNonEventPartnContext(PatternPartnContext partnContext)
  {
    currContext    = partnContext;
  }
  
  /**
   * @return Get the current partition context object
   */
  public PatternPartnContext getCurrContext()
  {
    return currContext;
  }
  
  /**
   * Used to add instance of activeItem corresponding to the argument activeBind
   */
  public void addActiveItem(Binding activeBind)
  {
    ActiveItem activeItem = new ActiveItem(activeBind, currContext);
    activeBind.setActiveItem(activeItem);
    store.addActiveItem(activeItem);
  }

  /** 
   * Used to remove the corresponding instance of activeItem for the argument 
   * activeBind
   */
  public void removeActiveItem(Binding activeBind)
  {
    ActiveItem activeItem = activeBind.getActiveItem();
    store.removeActiveItem(activeItem);
    activeItem.setReferredBinding(null);
    activeItem.setOwnerPartnContext(null);
    activeBind.setActiveItem(null);
    activeItem = null;
  }

  /**
   * get the TreeSet of activeItems
   */
  public TreeSet<ActiveItem> getActiveItems()
  {
    return store.getActiveItems();
  }

  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    assert false;
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    assert false;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(store);
    out.writeObject(prevReturnArr);
    out.writeObject(currContext);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    store = (BindStore) in.readObject();
    prevReturnArr = (ITuplePtr[]) in.readObject();
    currContext = (PatternPartnContext) in.readObject();
  }
  
  @Override
  public void copyFrom(BindingSynopsis other) throws IOException
  {
    store.copyFrom(other.getStore());
    try
    {
      if(other.getCurrContext() != null && other.getCurrContext().getPrevArr() != null)
        this.prevReturnArr = other.getPrevArr();
    } 
    catch (ExecException e)
    {
      e.printStackTrace();
      LogUtil.logStackTrace(e);
      throw new IOException("Can't Initialize PartnList of BindingSynopsis [" + this.toString()+"] from snapshot", e);
    }
    this.currContext = other.getCurrContext();
  }

  @Override
  public BindStore getStore()
  {
    return this.store;
  }
}

