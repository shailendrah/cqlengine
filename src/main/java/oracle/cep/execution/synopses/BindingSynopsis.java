/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/BindingSynopsis.java /main/17 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/20/09 - pattern re-org
    udeshmuk    05/15/09 - APIs for activeItem
    udeshmuk    04/01/09 - partn by w/o all matches opt
    udeshmuk    03/17/09 - move getPrevArr to patternpartncontext
    udeshmuk    03/10/09 - restructure bindstore
    udeshmuk    08/04/08 - fix bug 7240994.
    rkomurav    06/03/08 - add getListsOfBindListsIter and
                           getListsOfUnsureListsIter
    rkomurav    03/27/08 - rename alphSize to numCorrs
    rkomurav    02/25/08 - add setAlphSize
    rkomurav    10/09/07 - cleanup.
    rkomurav    10/03/07 - add delete partition APIs
    rkomurav    09/06/07 - prev(n)
    rkomurav    08/07/07 - prtnby timestamp ordering bug
    anasrini    07/14/07 - support for partition by
    rkomurav    06/27/07 - addEndOfActiveList
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/BindingSynopsis.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/2 2009/09/03 04:13:35 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.pattern.ActiveItem;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.execution.pattern.PatternPartnContext;
import oracle.cep.execution.pattern.UnsureItem;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.execution.stores.BindStore;
import oracle.cep.memmgr.IAllocator;

/**
 * BindingSynopsis is the interface used by Pattern Operator for
 * maintaining bindings
 */

public interface BindingSynopsis extends IPersistable
{

  /**
   * Create a new binding and add it to the existing list of bindings
   * @return new Binding
   */
  public Binding createBinding() throws ExecException;
  
  /**
   * Return iterator over the current active bindings 
   * @return iterator over the current active bindings
   */
  public Iterator<Binding> getIterator() 
    throws ExecException;
  
  /**
   * set the binding length
   * @param binding length
   */
  public void setBindLength(int len);
  
  /**
   * set number of correlations
   * @param numCorrs number of correlations
   */
  public void setNumCorrs(int numCorrs);
  
  /**
   * set the aggrTupleFactory
   * @param aggrTupleFactory
   */
  public void setAggrTupleFactory(IAllocator<ITuplePtr>  aggrTupleFactory);
  
  /**
   * set null input tuple
   * @param nullInputTuple
   */
  public void setNullInputTuple(ITuplePtr nullInputTuple);
  
  public ListIterator<Binding> getUnsureIterator() 
    throws ExecException;
  
  public Binding getBindingB0();
  
  public void addToActiveBindings(Iterator<Binding> itr, Binding b);
  
  public void addEndOfFinalList(Binding finalBinding);
  
  public void removeUnsureItem(UnsureItem unsureItem);
  
  public void addToReadyToOutputBindings(Binding b);
  
  public PriorityQueue<Binding> getReadyToOutputBindings();
  
  public long getUnsureMinMatchedTs() throws ExecException;
    
  public void setMaxPrevIndex(int maxPrevIndex);
  
  public void addPrevTuple(ITuplePtr prevTuple, IAllocator<ITuplePtr> 
    inTupleStorageAlloc) throws ExecException;
  
  public ITuplePtr[] getPrevArr() throws ExecException;
  
  public void removeEmptyPartns() throws ExecException;
  
  public void setPartnContext(ITuplePtr partnTuple) throws ExecException;
  
  public void setNonEventPartnContext(PatternPartnContext partnContext);
  
  public PatternPartnContext getCurrContext();

  public void addActiveItem(Binding activeBind);

  public void removeActiveItem(Binding activeBind);

  public TreeSet<ActiveItem> getActiveItems();
  
  public void copyFrom(BindingSynopsis other) throws IOException;
  
  public BindStore getStore();
}

