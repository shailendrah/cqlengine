/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/PatternPartnContext.java /main/4 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/24/09 - enable relase in addPrevTuple
    udeshmuk    03/17/09 - add prevArr to patternpartncontext.
    udeshmuk    03/13/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/PatternPartnContext.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/2 2009/09/03 04:13:33 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.ListIterator;

import oracle.cep.dataStructures.internal.IListNodeHandle; 
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.IAllocator;
/**
 * This class represents the entire context related to a partition in pattern
 * @author udeshmuk
 */
public class PatternPartnContext implements Externalizable
{  
  // Active list of bindings
  private BindingList    activeList;
  
  // Active treeset of bindings
  private BindingTreeSet activeTreeSet;
  
  // Unsure list of bindings
  private BindingList    unsureList;
  
  // ptr to the list node that contains this patternpartncontext in the partnlist
  private IListNodeHandle<PatternPartnContext> ptrToListEntry;
  
  // previous tuples array of this partition
  private ITuplePtr[] prevArr;

  // next slot to fill in prevArr
  private int         nextSlotToFill;

  //null input tuple
  private ITuplePtr   nullInputTuple;
  
  /** Empty Constructor for HA*/
  public PatternPartnContext()
  {}
  
  /**
   * Constructor
   * @param activeList Active BindingList for this partition
   * @param unsureList Unsure BindingList for this partition
   * @param max prev index value
   * @param null input tuple
   * @param activeTreeSet Active treeset for this partition
   */
  public PatternPartnContext(BindingList activeList, BindingList unsureList,
                             int maxPrevIndex, ITuplePtr nullInputTuple, 
                             BindingTreeSet activeTreeSet)
  {
    this.activeList    = activeList;
    this.unsureList    = unsureList;
    this.activeTreeSet = activeTreeSet;
    if(maxPrevIndex > 0)
    {
      this.prevArr = new ITuplePtr[maxPrevIndex]; 
      for(int i=0; i < prevArr.length; i++)
      {
        prevArr[i] = nullInputTuple;
      }
    }
    else {
      prevArr = null;
    }
    this.nextSlotToFill = maxPrevIndex - 1;
    this.nullInputTuple = nullInputTuple;
  }
  
  /**
   * @param ptr Ptr to node that has this partn context in partnList
   */
  public void setPtrToListEntry(IListNodeHandle<PatternPartnContext> ptr)
  {
    this.ptrToListEntry = ptr;
  }
  
  /**
   * @return ptr to the node that has this partn context in partnList
   */
  public IListNodeHandle<PatternPartnContext> getPtrToListEntry()
  {
    return this.ptrToListEntry;
  }
  
  /**
   * @return the list iterator for active bindings of this partition
   */
  public Iterator<Binding> getIterator()
  {
    if(activeList != null)
      return activeList.listIterator();
    else
      return activeTreeSet.getIterator();
  }
  
  /**
   * @return the list iterator for unsure bindinglist of this partition
   */
  public ListIterator<Binding> getUnsureIterator()
  {
    return unsureList.listIterator();
  }
  
  /**
   * @return number of active bindings
   */
  public int getNumActiveBindings()
  {
    if(activeList != null)
      return activeList.size();
    else
      return activeTreeSet.size();
  }
  
  /**
   * @return header tuple ptr of the partition
   */
  public ITuplePtr getHeaderTuplePtr()
  {
    if(activeList != null)
      return activeList.getHeaderTuplePtr();
    else
      return activeTreeSet.getHeaderTuplePtr();
  }
  
  /**
   * @return return the active bindinglist of this partition
   */
  public BindingList getActiveList()
  {
    return activeList; 
  }
  
  /**
   * @return return the unsure bindinglist of this partition
   */
  public BindingList getUnsureList()
  {
    return unsureList;
  }
  
  /**
   * @return return the active binding treeset
   */
  public BindingTreeSet getActiveTreeSet()
  {
    return activeTreeSet;
  }
  
  /**
   * param is populated to contain the previous tuples array for this partition
   * in correct order
   * @param prev tuples array
   */
  public void getPrevArr(ITuplePtr[] prevReturnArr)
  {
    assert ((prevArr != null) && (prevReturnArr != null)) : "prevArr and/or prevReturnArr is null";
    int prevArrLength = prevArr.length;
    // Populate the array in correct order
    if(nextSlotToFill + 1 == prevArrLength) //takes care of empty as well as full array
      System.arraycopy(prevArr, 0, prevReturnArr, 0, prevArrLength);
    else {
      System.arraycopy(prevArr, nextSlotToFill+1, prevReturnArr, 0, prevArrLength - nextSlotToFill - 1);
      System.arraycopy(prevArr, 0, prevReturnArr, prevArrLength - nextSlotToFill - 1, nextSlotToFill+1);
    }  
  }
  
  /** This method should be invoked while recovering the binding synopsis. 
   *  The goal is to check whether prevArr isn't null before invoking 
   *  getPrevArr(ITuplePtr[])
   */
  public ITuplePtr[] getPrevArr()
  {
    return this.prevArr;
  }
  
  /**
   * adds a tuple in the prevArr
   * @param prevTuple new prev tuple
   * @param inTupleStorageAlloc factory for prev tuple
   * @throws ExecException
   */
  public void addPrevTuple(ITuplePtr prevTuple, IAllocator<ITuplePtr> 
    inTupleStorageAlloc) throws ExecException
  {
    assert prevArr != null : "prevArr is null so can't add";
  
    if(prevArr[nextSlotToFill] != this.nullInputTuple)
      inTupleStorageAlloc.release(prevArr[nextSlotToFill]);
  
    prevArr[nextSlotToFill] = prevTuple;

    if(nextSlotToFill == 0)
      nextSlotToFill = prevArr.length - 1;
    else
      nextSlotToFill--;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(activeList);
    out.writeObject(unsureList);
    out.writeObject(activeTreeSet);
    out.writeObject(ptrToListEntry);
    out.writeObject(prevArr);
    out.writeInt(nextSlotToFill);
    out.writeObject(nullInputTuple);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.activeList = (BindingList) in.readObject();
    this.unsureList = (BindingList) in.readObject();
    this.activeTreeSet = (BindingTreeSet) in.readObject();
    this.ptrToListEntry = (IListNodeHandle<PatternPartnContext>) in.readObject();
    this.prevArr = (ITuplePtr[]) in.readObject();
    this.nextSlotToFill = in.readInt();
    this.nullInputTuple = (ITuplePtr) in.readObject();
  }
  
  public void copyFrom(PatternPartnContext other)
  {
    this.activeList = other.activeList;
    this.unsureList = other.unsureList;
    this.activeTreeSet = other.activeTreeSet;
    this.ptrToListEntry = other.ptrToListEntry;
    this.prevArr = other.prevArr;
    this.nextSlotToFill = other.nextSlotToFill;
    this.nullInputTuple = other.nullInputTuple;
  }
}
