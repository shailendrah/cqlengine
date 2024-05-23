/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/CombinedIterator.java /main/1 2009/10/29 21:18:23 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/14/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/CombinedIterator.java /main/1 2009/10/29 21:18:23 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.indexes;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.memmgr.IAllocator;

public class CombinedIterator implements TupleIterator
{
  Set<ITuplePtr>        resultSet;
  Iterator<ITuplePtr>   resultSetIter;
  IAllocator<ITuplePtr> factory;
  
  public CombinedIterator(List<Set<ITuplePtr>> resultSets,
                          IAllocator<ITuplePtr> factory)
  {
    this.factory       = factory;
    this.resultSet     = findIntersection(resultSets);
    this.resultSetIter = this.resultSet.iterator();
  }
  
  @Override
  public ITuplePtr getNext() throws ExecException
  {
    assert resultSetIter != null;
    if(resultSetIter.hasNext())
    {
      ITuplePtr tuple = resultSetIter.next();
      factory.addRef(tuple);
      return tuple;
    }
    else
      return null;
  }
  
  private Set<ITuplePtr> findIntersection(
    List<Set<ITuplePtr>> returnedResultSets)
  {
    int numReturnedResultSets = returnedResultSets.size();
    
    //if no resultsets then return empty linkedhashset
    if(numReturnedResultSets == 0)
      return new LinkedHashSet<ITuplePtr>();
    else
    {
      //TODO: some optimization is possible here.
      //We may find out which is the smallest size resultSet among all.
      //Then that can be used as the base instead of always choosing first.
      //This can improve iteration performance during retainAll execution.
      
      for(int j=1; j < numReturnedResultSets; j++)
      {
        //find intersection of all sets with the first set.
        //the first set keeps getting modified.
        returnedResultSets.get(0).retainAll(returnedResultSets.get(j));
      }
      return returnedResultSets.get(0);
    }
  }
}
