/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/BindStoreImpl.java /main/35 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/23/11 - change to eval(evalContext)
    udeshmuk    08/20/09 - pattern re-org
    udeshmuk    07/24/09 - fix RC leakages
    udeshmuk    05/15/09 - add APIs for activeItem
    udeshmuk    04/21/09 - use unsureItem only when nonevent is not present.
    udeshmuk    04/01/09 - partn by w/o all matches opt
    udeshmuk    03/17/09 - move the prevArr to patternpartncontext
    udeshmuk    03/10/09 - restructure bindstore
    hopark      10/10/08 - remove statics
    udeshmuk    08/04/08 - fix bug 7240994
    hopark      06/19/08 - logging refactor
    hopark      06/19/08 - logging refactor
    rkomurav    06/03/08 - add getLitsOfBindListsIter and
                           getListsOfUnsureListsIter
    rkomurav    03/20/08 - support subset
    hopark      02/28/08 - resurrect refcnt
    rkomurav    02/25/08 - add setAlphsize
    anasrini    02/07/08 - in removeEmptyPartns delete node from list of lists
    hopark      12/27/07 - support xmllog
    hopark      12/06/07 - cleanup spill
    hopark      12/04/07 - nodefac life cycle
    hopark      10/31/07 - change DoublyList api
    hopark      12/18/07 - change iterator semantics
    hopark      10/15/07 - add evict
    hopark      10/22/07 - remove TimeStamp
    hopark      10/11/07 - fix DoublyListNode usage
    rkomurav    10/09/07 - cleanup.
    rkomurav    10/03/07 - ade add deltee partition apis
    rkomurav    09/13/07 - move prev stuff for partn to partn store
    hopark      09/07/07 - eval refactor
    rkomurav    09/06/07 - support prev(n)
    rkomurav    08/07/07 - prtnby timestamp ordering bug
    rkomurav    07/26/07 - remove factory from constructor
    rkomurav    07/19/07 - 
    najain      06/28/07 - compile fix
    anasrini    07/12/07 - support for partition by
    rkomurav    06/27/07 - addEndOfActiveList
    hopark      06/22/07 - logging
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/BindStoreImpl.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/4 2010/12/13 01:51:52 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.IDoublyList;
import oracle.cep.dataStructures.internal.IDoublyListIter;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.comparator.BindingComparator;
import oracle.cep.execution.pattern.UnsureItemComparator;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.pattern.ActiveItem;
import oracle.cep.execution.pattern.ActiveItemComparator;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.execution.pattern.BindingList;
import oracle.cep.execution.pattern.BindingTreeSet;
import oracle.cep.execution.pattern.BindingTreeSetIterator;
import oracle.cep.execution.pattern.PatternPartnContext;
import oracle.cep.execution.pattern.UnsureItem;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

@DumpDesc(attribTags={"Id", "PhyId"}, 
          attribVals={"getId", "getPhyId"},
          infoLevel=LogLevel.STORE_INFO,
          evPinLevel=LogLevel.STORE_TUPLE_PINNED,
          evUnpinLevel=LogLevel.STORE_TUPLE_UNPINNED,
          dumpLevel=LogLevel.STORE_DUMP,
          verboseDumpLevel=LogLevel.STORE_DUMPELEMS)
public class BindStoreImpl extends ExecStore implements BindStore
{
  static final String TAG_PARTNLIST = "PartitionList";
  static final String[] TAG_PARTNLIST_ATTRIBS = {"Length"};
  static final String TAG_BINDLIST = "ActiveBindList";
  static final String[] TAG_BINDLIST_ATTRIBS = {"Length"};
  static final String TAG_UNSURELIST = "UnsureList";
  static final String[] TAG_UNSURELIST_ATTRIBS = TAG_BINDLIST_ATTRIBS;
  
  /** 
   *  Initial capacity of PriorityQueue for ready to output bindings.
   *  11 is chosen because it is the default capacity i.e. when capacity is not
   *  specified explicitly, then 11 is the initial capacity.  
   */
  private final int READY_TO_OUTPUT_INITIAL_CAPACITY = 11;
  
  /**
   * binding sequence to uniquely identify binding
   */
  private long bindingSequence = 0;
  
  // priority queue of ready to output bindings
  // maintained because they cannot be output as some other binding with a
  // lesser matched time stamp is on hold as matched but unsure.
  private PriorityQueue<Binding>  readyToOutputBindings;
  
  // TreeSet of unsureItems. Used to make the getUnsureMinMatchedTs efficient.
  private TreeSet<UnsureItem>     unsureItems;
  
  // Binding B0
  private Binding                 bindingB0;
  
  // Binding length
  private int                     bindLength;
 
  // number of correlations - explicit defined corrs + implicit corrs + subset corrs
  private int                     numCorrs;
  
  // max previous index
  private int                     maxPrevIndex;
  
  private boolean                 aggrsPresent;

  // aggregate Tuple Factory
  private IAllocator<ITuplePtr>   aggrTupleFactory;
  
  // null input tuple
  private ITuplePtr               nullInputTuple;

  // Related to PARTITION BY

  public int getNumElems() { return 0; }
  
  public long getBindingSequence() {return bindingSequence;}
  
  // Is there a PARTITION BY clause
  private boolean                 hasPartnAttrs;

  // Pos in hdrTuple to get index of this partition into the list of lists
  private int                     listIndexPos;

  // Header tuple copy eval
  private IAEval                  hdrCopyEval;

  // Factory for the header tuple
  private IAllocator<ITuplePtr>   hdrTupleFactory;

  // Index for the partition headers for partition context objects
  private HashIndex               partnIndex;

  // Evaluation Context for partition evals
  private IEvalContext            evalContext;
  
  // A list of partition context objects
  private IDoublyList<PatternPartnContext>     partnList;
  
  // Iterator for the partnlist
  private IDoublyListIter<PatternPartnContext> partnIterator;
  
  // Context object to be used when there are no partn attrs
  private PatternPartnContext                  nonPartnCaseContext;
  
  // Treeset for activeItems. Used for non-event with partn attrs
  private TreeSet<ActiveItem>                  activeItems;
  
  private IAllocator<IDoublyListNode<PatternPartnContext>> nFactory;

  private IAllocator<IDoublyList<PatternPartnContext>>     lFactory;
  private IAllocator<IDoublyListIter<PatternPartnContext>> iFactory;
  
  ArrayList<PatternPartnContext> persistedPartnList = new ArrayList<PatternPartnContext>();
  ArrayList<ITuplePtr> persistedIndexContents = new ArrayList<ITuplePtr>();
  
  /**
   * Skip clause of the pattern query
   */
  private PatternSkip patternSkip;
  
  /**
   * true if the query has DURATION clause, false otherwise
   */
  private boolean     isNonEvent;
  
  /**
   * true if the query has DURATION clause which is an expression, false otherwise
   */
  private boolean     isVariableDuration = false;
 
 /**
  * true if pattern operator has WITHIN or WITHIN INCLUSIVE clause defined
  */
  private boolean     withinClausePresent = false;
  
  // Setters
  
  /**
   * @param patternSkip clause value
   */
  public void setPatternSkip(PatternSkip patternSkip)
  {
    this.patternSkip = patternSkip;
  }
  
   /**
    * @param withinClausePresent true if pattern operator for this store has
    *        either isWithin or isWithinInclusive set to true.
    */
  public void setWithinClausePresent(boolean withinClausePresent)
  {
    this.withinClausePresent = withinClausePresent;
  }
		      
  /**
   * @param isVariableDuration true if variable duration non-event, false otherwise
   */
  public void setIsVariableDuration(boolean isVariableDuration)
  {
    this.isVariableDuration = isVariableDuration;
  }
  
  /**
   * @param isNonEvent true if the pattern operator for this store has nonEvent true
   */
  public void setIsNonEvent(boolean isNonEvent)
  {
    this.isNonEvent = isNonEvent;  
  }
  
  /**
   * @return partition context corresponding to input tuple
   */
  public PatternPartnContext getPartnContext(ITuplePtr inputTuple) throws ExecException
  {
    if(hasPartnAttrs)
    {
      TupleIterator       partnScan;
      ITuplePtr           hdrTuplePtr;
      ITuple              hdrTuple;
      PatternPartnContext pattContext;
      BindingList         bindList;
      BindingTreeSet      bindTreeSet;
      
      // get the hdr tuple
      evalContext.bind(inputTuple, IEvalContext.INPUT_ROLE);

      partnScan     = partnIndex.getScan();
      hdrTuplePtr   = partnScan.getNext();

      partnIndex.releaseScan(partnScan);

      // Check if partition already exists
      if (hdrTuplePtr == null)
      {
        // First tuple of new partition
        hdrTuplePtr   = (ITuplePtr)hdrTupleFactory.allocate();
        
        evalContext.bind(hdrTuplePtr, IEvalContext.SYN_ROLE);
        hdrCopyEval.eval(evalContext);
        // Allocate a new unsure and bind list and send the hdr ptr as argument
        if(isVariableDuration || (patternSkip == PatternSkip.ALL_MATCHES && 
           isNonEvent))
        {
          bindList    = null;
          bindTreeSet = new BindingTreeSet(hdrTuplePtr);
        }
        else
        {
          bindList    = new BindingList(hdrTuplePtr);
          bindTreeSet = null;
        }
        
        BindingList unsList  = new BindingList(hdrTuplePtr);

        hdrTuple = hdrTuplePtr.pinTuple(IPinnable.WRITE);
        
        //create partition context and set a ptr to it in the hdrtuple
        pattContext = new PatternPartnContext(bindList, unsList, maxPrevIndex,
                                              nullInputTuple, bindTreeSet);
        hdrTuple.oValueSet(listIndexPos, pattContext);

        //add the partncontext in the partnlist as well
        if(partnList == null)
          pattContext.setPtrToListEntry(null);
        else  
        {
          partnList.add(pattContext);
          IDoublyListNode<PatternPartnContext> tail = partnList.getTail();
          assert tail != null;
          pattContext.setPtrToListEntry(tail.getHandle(partnList));
        }
                
        // Insert the new partition into the index
        partnIndex.insertTuple(hdrTuplePtr);
        hdrTuplePtr.unpinTuple();
      }
      else
      { //partition of the current tuple already exists so return the existing
        //context
        hdrTuple = hdrTuplePtr.pinTuple(IPinnable.READ);
        pattContext = (PatternPartnContext) hdrTuple.oValueGet(listIndexPos);
        hdrTuplePtr.unpinTuple();
        // since getNext will call addRef, call release here
        hdrTupleFactory.release(hdrTuplePtr);
      }
      
      return pattContext;
    }
    else
    { // partn attrs not present
      return nonPartnCaseContext;
    }
  }
  
  public PatternPartnContext getNonPartnCaseContext()
  {
    return nonPartnCaseContext;
  }
  
  public void setBindLength(int len)
  {
    this.bindLength = len;
  }
  
  public void setNumCorrs(int numCorrs)
  {
    this.numCorrs = numCorrs;
  }

  public void setMaxPrevIndex(int maxPrevIndex)
  {
    this.maxPrevIndex = maxPrevIndex;
  }

  public void setAggrTupleFactory(IAllocator<ITuplePtr>  aggrTupleFactory)
  {
    this.aggrTupleFactory = aggrTupleFactory;
  }
  
  public void setNullInputTuple(ITuplePtr nullInputTuple)
  {
    this.nullInputTuple = nullInputTuple;
  }

  public void setHasPartnAttrs(boolean hasPartnAttrs)
  {
    this.hasPartnAttrs = hasPartnAttrs;
  }

  public void setListIndexPos(int listIndexPos)
  {
    this.listIndexPos = listIndexPos;
  }

  public void setHdrTupleFactory(IAllocator<ITuplePtr> hdrTupleFactory)
  {
    this.hdrTupleFactory = hdrTupleFactory;
  }

  public void setHdrCopyEval(IAEval hdrCopyEval)
  {
    this.hdrCopyEval = hdrCopyEval;
  }

  public void setPartnIndex(HashIndex partnIndex)
  {
    this.partnIndex = partnIndex;
  }

  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  // Getters

  public Binding getBindingB0()
  {
    return bindingB0;
  }
  
  public ArrayList<PatternPartnContext> getPersistedPartnList()
  {
    return this.persistedPartnList;
  }
  
  public ArrayList<ITuplePtr> getPersistedIndexContents()
  {
    return this.persistedIndexContents;
  }
  
  /** Empty Constructor for HA */
  public BindStoreImpl()
  {}
  
  /**
   * Constructor
   * 
   * @param ec
   *          TODO
   */
  public BindStoreImpl(ExecContext ec)
  {
    super(ec, ExecStoreType.BINDING);
    readyToOutputBindings = new PriorityQueue<Binding>(READY_TO_OUTPUT_INITIAL_CAPACITY,
                                                       new BindingComparator());
    bindingSequence = 0;
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    lFactory  = factoryMgr.get(FactoryManager.DOUBLY_LIST_FACTORY_ID);
    iFactory  = factoryMgr.get(FactoryManager.DOUBLY_LIST_ITER_FACTORY_ID);
    nFactory  = factoryMgr.get(FactoryManager.DOUBLY_LIST_NODE_FACTORY_ID);
  }

  public void initialize() throws ExecException
  {
    aggrsPresent = false;
    if(aggrTupleFactory != null)
      aggrsPresent = true;
    
    // State 0 binding
    bindingB0 = new Binding(bindLength, nullInputTuple, numCorrs,
                            bindingSequence++);
    initAggrTuple(bindingB0);
    // initialize sate 0 with nullInputTuple
    for(int i = 0; i < bindLength - 1; i++)
    {
      bindingB0.setTuple(nullInputTuple, i);
    }
    // set curState for state 0
    bindingB0.setCurState(0);
   
    //Used in non-event with partn attrs
    if((isNonEvent || withinClausePresent) && hasPartnAttrs)
      activeItems = new TreeSet<ActiveItem>(new ActiveItemComparator());
    else
      activeItems = null;
 
    //TODO : Earlier partnList was used for iteration in non-event processing.
    //       Now however it is used only in dump() method. 
    //       So this be maintained at all?
    if(hasPartnAttrs)
    {
      try
      {
        partnList = lFactory.allocate();
        partnList.setFactory(nFactory);
        partnIterator = iFactory.allocate();
      }
      catch(ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
    }
    else 
      partnList = null;
      
    if(hasPartnAttrs && 
      (patternSkip == PatternSkip.SKIP_PAST_LAST_ROW) && 
      (!isNonEvent || (isNonEvent && isVariableDuration)))
      unsureItems = new TreeSet<UnsureItem>(new UnsureItemComparator());
    else
      unsureItems = null;
    
    //Initialize a ctx object for nonpartn case. maxPrevIndex and nullInputTuple is
    //already set when code reaches here.
    BindingList    activeList;
    BindingTreeSet activeTreeSet;
    
    //For variable duration maintain bindings in treeset
    //For all matches case even for fixed duration maintain bindings in treeset
    if(isVariableDuration || (patternSkip == PatternSkip.ALL_MATCHES && isNonEvent))
    {
      activeList    = null;
      activeTreeSet = new BindingTreeSet(); 
    }
    else
    {
      activeList    = new BindingList();
      activeTreeSet = null;
    }
    
    BindingList unsureList = new BindingList();
    nonPartnCaseContext = new PatternPartnContext(activeList, unsureList,
                                                  maxPrevIndex, nullInputTuple,
                                                  activeTreeSet);
  }
  
  public void removeStub(int stubId)
  {
    // not applicable for binding store
  }
  
  public int addStub()
  {
    return 0;
    // not needed for binding store!
  }
  
  public Binding createBinding() throws ExecException
  {
    Binding b = new Binding(bindLength, nullInputTuple, numCorrs, 
                            bindingSequence++);
    initAggrTuple(b);
    return b;
  }
  
   /**
   * adds activeItem to the TreeSet
   * @param activeItem to be added
   */
  public void addActiveItem(ActiveItem activeItem)
  {
    activeItems.add(activeItem);
  }

  /**
   * remove activeItem from the TreeSet
   * @param activeItem to be removed
   */
  public void removeActiveItem(ActiveItem activeItem)
  {
    activeItems.remove(activeItem);
  }

  /**
   * @return TreeSet of active items. used in non-event with partition attrs.
   */
  public TreeSet<ActiveItem> getActiveItems()
  {
    return this.activeItems;
  }

  /**
   * adds the unsureItem to the TreeSet
   * @param unsure item to be added
   */
  public void addUnsureItem(UnsureItem unsureItem)
  {
    unsureItems.add(unsureItem);
  }
  
  /**
   * removes the unsureItem from the TreeSet
   * @param unsureItem item to be deleted
   */
  public void removeUnsureItem(UnsureItem unsureItem)
  {
    unsureItems.remove(unsureItem);  
  }
  
  public void addToReadyToOutputBindings(Binding b)
  {
    readyToOutputBindings.offer(b);
  }
  
  public PriorityQueue<Binding> getReadyToOutputBindings()
  {
    return readyToOutputBindings;
  }
  
  public TreeSet<UnsureItem> getUnsureItems()
  {
    return this.unsureItems;
  }
  
  /**
   * Returns the minimum matched Ts among all the unsure bindings
   * across all partitions
   */
  public long getUnsureMinMatchedTs() throws ExecException
  { 
    assert unsureItems != null : "getUnsureMinMatchedTs: unsureItems is null";
    
    if(!unsureItems.isEmpty())
      return unsureItems.first().getMatchedTs();
    else
      return Constants.MAX_EXEC_TIME;
  }
  
  /**
   * This method removes the partn context object from the partnlist,
   * and also removes a reference to it from header tuple and deletes
   * the headerTuple from partnIndex
   * @param hdrTuplePtr hdrtupleptr for the current partition
   */
  public void removeEmptyPartns(ITuplePtr hdrTuplePtr) 
    throws ExecException
  {
    ITuple hdrTuple;

    hdrTuple = hdrTuplePtr.pinTuple(IPinnable.WRITE);
    PatternPartnContext ctx = hdrTuple.oValueGet(listIndexPos);
    //remove reference from hdrtuple
    hdrTuple.oValueSet(listIndexPos, null);
    hdrTuplePtr.unpinTuple();
    IListNodeHandle<PatternPartnContext> ptr = ctx.getPtrToListEntry();
    //remove reference from the list
    if(partnList != null)
      partnList.remove(ptr);
    
    //remove from hashtable and release
    partnIndex.deleteTuple(hdrTuplePtr);
    hdrTupleFactory.release(hdrTuplePtr);
  }

  // Related to logging and debugging

  public String getTargetName() {return "BindingStore";}

  public void dumpElements()
  {
    LogUtil.fine(LoggerType.TRACE, toString());
    // wt to do here?
    // listStore.dump();
  }

  public synchronized void dump(IDumpContext dumper) 
  {
    PatternPartnContext pctx;
    if (!dumper.isVerbose() && partnList != null)
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      dumper.writeln("PartnListSize", partnList.getSize());
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    
    if(partnList != null)
    {
      String dumperKey = StringUtil.getBaseClassName(this);
      IDumpContext w = dumper.openDumper(dumperKey);
      String tag = LogUtil.beginDumpObj(w, this);
      
      try 
      {
        LogUtil.beginTag(dumper, TAG_PARTNLIST, TAG_PARTNLIST_ATTRIBS, partnList.getSize());
        partnIterator.initialize(partnList);
        while ((pctx = partnIterator.next()) != null) 
        {
          BindingList bindList       = pctx.getActiveList();
          BindingTreeSet bindTreeSet = pctx.getActiveTreeSet();
          
          if(bindList != null)
          {
            LogUtil.beginTag(dumper, TAG_BINDLIST, TAG_BINDLIST_ATTRIBS,
                             bindList.size());
            for(Binding ab : bindList) 
              ab.dump(w);
            dumper.endTag(TAG_BINDLIST);
          }
          
          if(bindTreeSet != null)
          {
            LogUtil.beginTag(dumper, TAG_BINDLIST, TAG_BINDLIST_ATTRIBS, 
                             bindTreeSet.size());
            BindingTreeSetIterator bts = bindTreeSet.getIterator();
            while(bts.hasNext())
            {
              Binding ab = bts.next();
              ab.dump(w);
            }
          }
          
          BindingList unsureList = pctx.getUnsureList();
          if(unsureList != null)
          {
            LogUtil.beginTag(dumper, TAG_UNSURELIST, TAG_UNSURELIST_ATTRIBS, unsureList.size());
            for(Binding ub : unsureList)
              ub.dump(w);
            dumper.endTag(TAG_UNSURELIST);
          }
        }
        dumper.endTag(TAG_PARTNLIST);
      }
      catch(ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
      LogUtil.endDumpObj(w, tag);
      w.closeDumper(dumperKey, dumper);
    }
  }

  // Private methods
  
  private void initAggrTuple(Binding b) throws ExecException
  {
    if(aggrsPresent)
    {
      ITuplePtr aggrTuple = (ITuplePtr)aggrTupleFactory.allocate(); // SCRATCH_TUPLE
      b.setTuple(aggrTuple, bindLength - 1);
    }
  }
  
  public boolean evict()
    throws ExecException
  {
    return false;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(bindingSequence);
    out.writeObject(readyToOutputBindings);
    out.writeObject(unsureItems);
    //out.writeObject(bindingB0);
    //out.writeObject(partnList);
    try
    {
      if(partnList != null)
      {
        PatternPartnContext pctx;        
        partnIterator.initialize(partnList);
        while ((pctx = partnIterator.next()) != null) 
        {
          persistedPartnList.add(pctx);
        }       
      }
      out.writeObject(persistedPartnList);
      
      if(partnIndex != null)
      {
        TupleIterator iter = partnIndex.getFullScan();
        ITuplePtr next = iter.getNext();
        while(next != null)
        {
          persistedIndexContents.add(next);
          next = iter.getNext();
        }
        partnIndex.releaseScan(iter);
      }
      out.writeObject(persistedIndexContents);
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(e);
      throw new IOException("Can't Write PartnList or PartnIndex of BindStore [" + this.toString()+"] to snapshot", e);
    }
    out.writeObject(nonPartnCaseContext);
    out.writeObject(activeItems);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.bindingSequence = in.readLong();
    this.readyToOutputBindings = (PriorityQueue<Binding>) in.readObject();
    this.unsureItems = (TreeSet<UnsureItem>) in.readObject();
    //this.bindingB0 = (Binding) in.readObject();
    //this.partnList = (IDoublyList<PatternPartnContext>) in.readObject();
    this.persistedPartnList = (ArrayList<PatternPartnContext>) in.readObject();
    this.persistedIndexContents = (ArrayList<ITuplePtr>) in.readObject();
    this.nonPartnCaseContext = (PatternPartnContext) in.readObject();
    this.activeItems = (TreeSet<ActiveItem>) in.readObject();
  }
  
  @Override
  public void copyFrom(BindStore other) throws IOException
  {
    this.bindingSequence = other.getBindingSequence();
    this.readyToOutputBindings = other.getReadyToOutputBindings();
    this.unsureItems = other.getUnsureItems();    
    //this.bindingB0 = other.getBindingB0();
    this.nonPartnCaseContext = other.getNonPartnCaseContext();
    this.activeItems = other.getActiveItems();
    try
    {
      if(this.partnList != null)
      {
        ArrayList<PatternPartnContext> recoveredList = other.getPersistedPartnList();
        for(PatternPartnContext next: recoveredList)
        {
          partnList.add(next);
          IDoublyListNode<PatternPartnContext> tail = partnList.getTail();
          assert tail != null;
          next.setPtrToListEntry(tail.getHandle(partnList));
        }
      }
      
      if(this.partnIndex != null)
      {
        ArrayList<ITuplePtr> recoveredIndexContents = other.getPersistedIndexContents();
        for(ITuplePtr next: recoveredIndexContents)
        {
          partnIndex.insertTuple(next);
        }
      }
    }
    catch(ExecException e)
    {
      e.printStackTrace();
      LogUtil.logStackTrace(e);
      throw new IOException("Can't Initialize PartnList or PartnIndex of BindStore [" + this.toString()+"] from snapshot", e);
    }
  }
}
