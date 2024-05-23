/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/AbsAllocator.java /main/11 2008/10/24 15:50:19 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      02/28/08 - resurrect refcnt
    hopark      12/07/07 - cleanup spill
    hopark      12/04/07 - remove factory
    hopark      09/18/07 - add getNameSpace
    hopark      01/24/07 - add toString
    najain      07/07/06 - Creation
 */

/**
 * @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/AbsAllocator.java /main/11 2008/10/24 15:50:19 hopark Exp $
 * @author najain  
 * @since release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.execution.ExecException;

/**
 * StorageElementFactory
 *
 * @author skaluska
 */
public abstract class AbsAllocator<E> implements IAllocator<E>
{
  protected FactoryManager factoryMgr;
  protected int id;
  protected NameSpace nameSpace;
  protected MemStat stat;
  
  static AtomicInteger nextId;
  static 
  {
    nextId = new AtomicInteger();
    // default factories uses id from 0.
    nextId.set(FactoryManager.MAX_FIXED_FACTORY_ID);
  }

  public AbsAllocator(FactoryManager factoryMgr)
  {
    this(factoryMgr, nextId.incrementAndGet() );
  }

  public AbsAllocator(FactoryManager factoryMgr, int i)
  {
    this(factoryMgr, i, null);
  }

  public AbsAllocator(FactoryManager fm, int i, NameSpace ns)
  {
    factoryMgr = fm;
    id = i;
    nameSpace = ns;
    stat = new MemStat();
  }

  protected void finalize() throws Throwable
  {
    // we are done with the factory.
    // reclaim the slot in the factory manager.
    factoryMgr.deleteFactory(id);
  }
  
  public int getId() {return id;}
  public NameSpace getNameSpace() {return nameSpace;}
  public MemStat getStat() {return stat;}
  
  @SuppressWarnings("unchecked")
  public E allocate() throws ExecException 
  {
    stat.m_totalObjs++;
    return (E) allocBody();
  }
  public abstract Object allocBody() throws ExecException;
  
  public int addRef(E element) {return 0;}
  public int addRef(E element, int ref) {return 0;}
  public int release(E element) {return 0;}
  public void dump() {}
  public String toString() { return null; }
}
