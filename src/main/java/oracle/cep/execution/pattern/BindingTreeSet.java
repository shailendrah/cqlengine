/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/BindingTreeSet.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/20/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/BindingTreeSet.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/2 2009/09/03 04:13:34 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.TreeSet;

import oracle.cep.dataStructures.internal.ITuplePtr;

public class BindingTreeSet implements Externalizable
{
  /** Contains current active bindings */
  private TreeSet<Binding> currentBindings;
  
  /** Contains active bindings generated while processing currentBindings.
   *  These will be currentBindings for the next tuple*/
  private TreeSet<Binding> newBindings;
  
  /** header tuple ptr for the partition to which this belongs */
  private ITuplePtr        headerTuplePtr;
  
  /**
   * Constructor for non-partition case
   */
  public BindingTreeSet()
  {
    this.headerTuplePtr = null;
    currentBindings = new TreeSet<Binding>(new ActiveBindingComparator());
    newBindings     = new TreeSet<Binding>(new ActiveBindingComparator());
  }
  
  /**
   * Constructor for partition case
   * @param hdrTuplePtr
   */
  public BindingTreeSet(ITuplePtr hdrTuplePtr)
  {
    this.headerTuplePtr = hdrTuplePtr;
    currentBindings = new TreeSet<Binding>(new ActiveBindingComparator());
    newBindings     = new TreeSet<Binding>(new ActiveBindingComparator());
  }
  
  /**
   * @return Iterator for iterating over the binding set
   */
  public BindingTreeSetIterator getIterator()
  {
    reset(); 
    return new BindingTreeSetItr(currentBindings.iterator());
  }
   
  /**
   * Makes the bindings which were newly generated(for prev tuple) 
   * as current ones (for the current tuple).
   * This method is called by the getIterator() method.
   */
  private void reset()
  {
    if(!newBindings.isEmpty())
    {
      if(currentBindings.isEmpty())
      {
        TreeSet<Binding> temp;
        temp            = currentBindings;
        currentBindings = newBindings;
        newBindings     = temp; 
      }
      else
      {
        currentBindings.addAll(newBindings);
        newBindings.clear();
      }
      assert newBindings.isEmpty() : "New bindings not empty";
    }
  }
  
  /**
   * @return headerTuplePtr entry
   */
  public ITuplePtr getHeaderTuplePtr()
  {
    return this.headerTuplePtr;
  }
  
  /**
   * @return number of bindings present currently in the BindingTreeSet
   */
  public int size()
  {
    return currentBindings.size()+newBindings.size();
  }
 
  /**
   * Custom implementation of iterator for BindingTreeset.
   * 
   * @author udeshmuk
   */
  private class BindingTreeSetItr implements BindingTreeSetIterator
  {
    Iterator<Binding> curItr;
    
    public BindingTreeSetItr(Iterator<Binding> curItr)
    {
      this.curItr = curItr;
    }
    
    public void add(Binding b)
    {
      newBindings.add(b);  
    }
    
    public void remove()
    {
      curItr.remove();
    }
    
    public boolean hasNext()
    {
      return curItr.hasNext();
    }
    
    public Binding next()
    {
      return curItr.next();
    }
  }
  
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(currentBindings);
    out.writeObject(newBindings);
    out.writeObject(headerTuplePtr);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.currentBindings = (TreeSet<Binding>) in.readObject();
    this.newBindings = (TreeSet<Binding>) in.readObject();
    this.headerTuplePtr = (ITuplePtr) in.readObject();
  }
}