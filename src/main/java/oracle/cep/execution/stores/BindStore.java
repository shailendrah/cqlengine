/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/BindStore.java /main/17 2011/01/04 06:40:13 udeshmuk Exp $ */

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
    udeshmuk    03/17/09 - move the prevArr to patternpartncontext
    udeshmuk    03/10/09 - restructure bindstore
    udeshmuk    08/04/08 - fix bug 7240994
    rkomurav    06/03/08 - add getLitsOfBindListsIter and
                           getListsOfUnsureListsIter
    rkomurav    02/25/08 - add setAlphSize
    rkomurav    10/09/07 - cleanup
    rkomurav    10/03/07 - add delete partition APIs
    rkomurav    09/06/07 - prev(n)
    rkomurav    08/07/07 - prtnby timestamp ordering bug
    anasrini    07/12/07 - support for partition by
    rkomurav    06/27/07 - addEndOfActiveList
    rkomurav    05/16/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/BindStore.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/1 2009/08/28 02:43:24 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import java.io.Externalizable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.pattern.ActiveItem;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.execution.pattern.PatternPartnContext;
import oracle.cep.execution.pattern.UnsureItem;
import oracle.cep.memmgr.IAllocator;

public interface BindStore extends Externalizable
{
  public Binding createBinding() throws ExecException;
  
  public void setBindLength(int len);
  
  public void setNumCorrs(int alphSize);
  
  public void setAggrTupleFactory(IAllocator<ITuplePtr>  aggrTupleFactory);
  
  public void initialize() throws ExecException;
  
  public void setNullInputTuple(ITuplePtr nullInputTuple);
  
  public Binding getBindingB0();
  
  public void addToReadyToOutputBindings(Binding b);

  public PriorityQueue<Binding> getReadyToOutputBindings();
  
  public long getUnsureMinMatchedTs() throws ExecException;
  
  public void setMaxPrevIndex(int maxPrevIndex);
  
  public void removeEmptyPartns(ITuplePtr hdrTuple) throws ExecException;
  
  public PatternPartnContext getPartnContext(ITuplePtr inputTuple) throws ExecException;
   
  public void addUnsureItem(UnsureItem unsureItem);
  
  public void removeUnsureItem(UnsureItem unsureItem);

  public void addActiveItem(ActiveItem activeItem);

  public void removeActiveItem(ActiveItem activeItem);

  public TreeSet<ActiveItem> getActiveItems();
  
  public void copyFrom(BindStore other) throws IOException;
  
  public long getBindingSequence();
  
  public TreeSet<UnsureItem> getUnsureItems();
  
  public PatternPartnContext getNonPartnCaseContext();
  
  public ArrayList<PatternPartnContext> getPersistedPartnList();
  
  public ArrayList<ITuplePtr> getPersistedIndexContents();
}
