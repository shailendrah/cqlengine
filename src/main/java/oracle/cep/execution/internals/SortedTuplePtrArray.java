/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/SortedTuplePtrArray.java /main/3 2008/11/07 23:08:44 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    this class holds a sorted collection of ITuplePtr
    at any time the collection will always be sorted on
    some order (order descriptors are held elsewhere).
      

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/16/08 - fix NPEfrom TuplePtr
    skmishra    07/23/08 - implements ISortedArray
    skmishra    07/17/08 - Creation
 */

package oracle.cep.execution.internals;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.memmgr.IPinnable;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/SortedTuplePtrArray.java /main/3 2008/11/07 23:08:44 udeshmuk Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class SortedTuplePtrArray implements ISortedArray<ITuplePtr>
{
  
  List<ITuplePtr> sortedList;
  TupleComparator comparator;
  
  public SortedTuplePtrArray(TupleComparator tc)
  {
    sortedList = new ArrayList<ITuplePtr>();
    comparator = tc;
  }
  
  //Inserts to maintain the sort order of the list.
  //Currently uses sequential search to find position. O(n)
  //TODO: Can be optimized to use binary search O(lg n)
  
  public int insert(ITuplePtr t) throws CEPException
  {
    int length = sortedList.size();
    //if this is the first insert
    if(length ==0) 
    {
      sortedList.add(t);
      return(0);
    }
    else
    {
      ITuple tuple0 = t.pinTuple(IPinnable.READ);
      int i = 0;
      for(i=0; i < length; i++)
      {
        ITuplePtr t1 = sortedList.get(i);
        //if compareTuples returns greater than zero, t1 should be before t, so continue
        ITuple tuple1 = t1.pinTuple(IPinnable.READ);
        
        if (comparator.compareTuples(tuple0, tuple1) > 0)
        {
          t1.unpinTuple();
          continue;
        }
        else 
        {
          //this is the position at which to insert tuple t
          sortedList.add(i, t);
          t1.unpinTuple();
          t.unpinTuple();
          return i;
        }
      }
      
      //tuple t is "greater than" all other tuples in the list.
      sortedList.add(i, t);
      t.unpinTuple();
      return length;
    }
  }
  
  public boolean contains(ITuplePtr t) throws CEPException
  {
    try
    {
      boolean ret = sortedList.contains(t);
      return ret;
    }
    
    catch(ClassCastException e)
    {
      throw new CEPException(ExecutionError.XML_AGG_RUNTIME_ERROR);
    }
    
    catch(NullPointerException ne)
    {
      throw new CEPException(ExecutionError.XML_AGG_RUNTIME_ERROR);
    }
  }


  public void clear() throws CEPException
  {
    for(ITuplePtr t: sortedList)
    {
      t.unpinTuple();
    }
    sortedList.clear();
  }
}
