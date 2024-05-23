/* $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/Binding.java /main/17 2009/06/15 22:04:06 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Binding class for pattern operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/16/09 - add activeItem API
    udeshmuk    04/01/09 - partn by w/o all matches opt
    hopark      06/19/08 - logging refactor
    rkomurav    05/19/08 - add targetTime field
    rkomurav    03/27/08 - rename alphsize to cnumcorrs
    hopark      02/28/08 - resurrect refcnt
    rkomurav    02/25/08 - init incr eval with bitset
    hopark      12/27/07 - support xmllog
    hopark      12/07/07 - cleanup spill
    hopark      10/22/07 - remove TimeStamp
    rkomurav    07/19/07 - add null check before release
    rkomurav    06/11/07 - add outputts
    rkomurav    05/23/07 - add currentState
    rkomurav    05/30/07 - restructure
    rkomurav    04/18/07 - edit the decrRef
    rkomurav    04/16/07 - add decrRef method
    najain      03/29/07 - cleanup
    rkomurav    03/10/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/Binding.java /main/17 2009/06/15 22:04:06 hopark Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.IDumpable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IAllocator;

@DumpDesc(valueTags={"StartIndex", "CurState", "MatchedTs"}, 
          values={"@startIndex", "@curState", "@matchedTs"})
public class Binding implements IDumpable, Externalizable
{
  static final String TAG_ELEMS = "Elems";
  
  /** Unique identifier for the binding */ 
  private long bindingId;
  
  /** Elements of Binding */
  ITuplePtr[] elems;
  
  /** Least Sequence number among the elements for the Binding */
  long        startIndex;
  
  /** Null Input Tuple */
  ITuplePtr   nullInputTuple;

  /** Current state in the state machine */
  int         curState;
  
  /** pattern matched timestamp */
  long        matchedTs;
  
  /** init incr vector for aggrs */
  BitSet      initIncrFlag;
  
  /** Target time : used for non event detection cases */
  long        targetTime;
  
  /** 
   * Reference to instance of UnsureItem corresponding to this binding
   * in the TreeSet of UnsureItems.
   */
  UnsureItem  unsureItem;
  
  /**
   * Reference to activeItem instance in the ActiveItems treeset.
   * Used in non-event detection.
   */
  ActiveItem  activeItem;

  /** Constructor for HA Recovery */
  public Binding()
  {}
  
  /**
   * Constructor
   * @param length number of elements in the binding
   * @param nullInputTuple null input tuple
   * @param numCorrs number of correlations = 
   *        explicit defined correlations +
   *        implicit defined correlations +
   *        subset correlations
   * @param bindingId binding identifier
   */
  public Binding(int length, ITuplePtr nullInputTuple, int numCorrs, long bindingId)
  {
    elems               = new ITuplePtr[length];
    this.nullInputTuple = nullInputTuple;
    initIncrFlag        = new BitSet(numCorrs);
    unsureItem          = null;
    this.bindingId      = bindingId; 
  }
  
  /**
   * Constructor NEVER USED NOW. IS THERE FOR PATTERN CLASS A.
   * @param length number of elements in the binding
   * @param nullInputTuple null input tuple
   */
  public Binding(int length, ITuplePtr nullInputTuple)
  {
    elems               = new ITuplePtr[length];
    this.nullInputTuple = nullInputTuple;
    unsureItem          = null;
  }

  /**
   * @param startIndex the startIndex to set
   */
  public void setStartIndex(long startIndex)
  {
    this.startIndex = startIndex;
  }

  /**
   * @return the startIndex
   */
  public long getStartIndex()
  {
    return startIndex;
  }
  
  /**
   * @return the elems
   */
  public ITuplePtr[] getElems()
  {
    return elems;
  }

  /**
   * @return the current state
   */
  public int getCurState()
  {
    return curState;
  }

  public void setCurState(int curState)
  {
    this.curState = curState;
  }

  /**
   * @return the matchedTs
   */
  public long getMatchedTs() 
  {
    return matchedTs;
  }

  /**
   * @param matchedTs the matchedTs to set
   */
  public void setMatchedTs(long matchedTs)
  {
    this.matchedTs = matchedTs;
  }

  /**
   * set the flag for the given index
   * @param index
   */
  public void setInitIncrFlag(int index)
  {
    this.initIncrFlag.set(index);
  }
  
  /**
   * @param index
   * @return the flag
   */
  public boolean getInitIncrFlag(int index)
  {
    return this.initIncrFlag.get(index);
  }
  
  public BitSet getInitIncrFlag()
  {
    return this.initIncrFlag;
  }
  
  /**
   * @return the targetTime
   */
  public long getTargetTime()
  {
    return targetTime;
  }

  /**
   * @param targetTime the targetTime to set
   */
  public void setTargetTime(long targetTime)
  {
    this.targetTime = targetTime;
  }

  /**
   * Return the binding identifier
   */
  public long getBindingId()
  {
    return this.bindingId;
  }
  
  /**
   * copy the current attr array to the binding b
   * @param b Binding to which attrs are to be copied
   * @param tupleAlloc The tuple factory which keeps track of ref counting
   * @param delPrevBinding if the previous binding needed to be decrrefed.
   */
  public void copyAttrs(Binding b, IAllocator<ITuplePtr> tupleAlloc,
      boolean delPrevBinding)
  {
    if(this.equals(b))
      return;
    
    //copy only corr attrs and not aggr tuple
    int len = elems.length - 1;
    ITuplePtr[] del = null;
    
    if(delPrevBinding)
    {
      del = new ITuplePtr[len];
      System.arraycopy(b.getElems(), 0, del, 0, len);
    }
    
    System.arraycopy(this.elems, 0, b.getElems(), 0, len);
    b.incrRef(tupleAlloc);
    
    //Decrementing of ref counting has to happen after increment of ref counting
    //as once a tuple is decr refed, it may potentially be removed from the system
    if(delPrevBinding)
    {
      for(ITuplePtr ptr: del)
      {
        if((ptr != null) && (ptr != nullInputTuple))
          tupleAlloc.release(ptr);
      }
    }
  }
  
  /**
   * copy the initincr bitset structure to the new binding
   * @param b
   */
  public void copyInitIncrFlag(Binding b)
  {
    b.getInitIncrFlag().or(this.initIncrFlag);
  }
  
  /**
   * Set current tuple t at position pos and increment ref count
   * @param t
   * @param pos
   */
  public void setCurrTuple(ITuplePtr t, int pos,
      IAllocator<ITuplePtr> tupleAlloc)
  {
    elems[pos] = t;
    tupleAlloc.addRef(t);
  }
  
  /**
   * set tuple t at position pos
   * @param t
   * @param pos
   */
  public void setTuple(ITuplePtr t, int pos)
  {
    elems[pos] = t;
  }
  
  public ITuplePtr getTuple(int pos)
  {
    return elems[pos];
  }
 
  /**
   * @return the activeItem instance corresponding to this binding
   */
  public ActiveItem getActiveItem()
  {
    return activeItem;
  }
  
  /**
   * Set the reference to the activeItem corresponding to this binding
   * @param activeItem reference to the instance of activeItem
   */
  public void setActiveItem(ActiveItem activeItem)
  {
    this.activeItem = activeItem;  
  }
   
  /**
   * @return the UnsureItem instance corresponding to this binding
   */
  public UnsureItem getUnsureItem()
  {
    return unsureItem;
  }

  /**
   * Set the reference to the UnsureItem corresponding to this binding 
   * @param unsureItem reference to instance of UnsureItem
   */
  public void setUnsureItem(UnsureItem unsureItem)
  {
    this.unsureItem = unsureItem;
  }
  
  /**
   * Decrement reference count for every tuplePtr in the binding
   */
  public void decrRef(IAllocator<ITuplePtr> tupleAlloc)
  {
    //do not decrease ref count for aggr Tuple
    for(int i = 0; i < elems.length -1; i++)
    {
      if((elems[i] != null) && (elems[i] != nullInputTuple))
        tupleAlloc.release(elems[i]);
      //Never make elements null, instead set them to nullInputTuple
      elems[i] = nullInputTuple;
    }
  }
  
  /**
   * Increment refrence count for every tuplePtr in the binding
   */
  public void incrRef(IAllocator<ITuplePtr> tupleAlloc)
  {
    //do not increase ref count for aggr Tuple
    for(int i = 0; i < elems.length -1; i++)
    {
      if(elems[i] != null)
        tupleAlloc.addRef(elems[i]);
    }
  }
  
  public synchronized void dump(IDumpContext dumper) 
  {
    String tag = LogUtil.beginDumpObj(dumper, this);
    LogUtil.beginTag(dumper, TAG_ELEMS, LogTags.ARRAY_ATTRIBS, 
                    (elems == null ? 0 : elems.length));
    if (elems != null)
    {
      for (ITuplePtr elem : elems)
      {
        if (elem != null)
        {
          elem.dump(dumper);
        }
      }
    }
    dumper.endTag(TAG_ELEMS);
    LogUtil.endDumpObj(dumper, tag);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(bindingId);
    // elems shouldn't be null for a binding
    assert elems != null;
    out.writeInt(elems.length);
    for(int i=0; i < elems.length; i++)
      out.writeObject(elems[i]);
    out.writeLong(startIndex);
    out.writeObject(nullInputTuple);
    out.writeInt(curState);
    out.writeLong(matchedTs);
    out.writeObject(initIncrFlag);
    out.writeLong(targetTime);
    out.writeObject(unsureItem);
    out.writeObject(activeItem);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.bindingId = in.readLong();
    int numElems = in.readInt();
    this.elems = new ITuplePtr[numElems];
    for(int i=0; i < numElems; i++)
      this.elems[i] = (ITuplePtr) in.readObject();    
    this.startIndex = in.readLong();
    this.nullInputTuple = (ITuplePtr) in.readObject();
    this.curState = in.readInt();
    this.matchedTs = in.readLong();
    this.initIncrFlag = (BitSet) in.readObject();
    this.targetTime = in.readLong();
    this.unsureItem = (UnsureItem) in.readObject();
    this.activeItem = (ActiveItem) in.readObject();
  }

}

