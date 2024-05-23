/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/FactoryManager.java /main/39 2011/08/18 12:08:43 alealves Exp $ */
/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini    08/12/11 - non-synchronized getters for fixed factories
 hopark      10/10/08 - remove statics
 hopark      05/05/08 - remove FullSpillMode
 hopark      03/27/08 - use getFullSpillMode
 hopark      03/08/08 - add SQPageFactoryId
 hopark      02/21/08 - add PagedFactory for WinStore
 hopark      02/28/08 - add TDoublyList factories
 hopark      01/31/08 - add queueelement factory
 hopark      01/25/08 - remove QSinglyList1
 hopark      11/29/07 - remove DoublyList2
 hopark      11/29/07 - remove DoublyList2
 hopark      12/05/07 - cleanup spill
 hopark      10/31/07 - add Paged list factories
 hopark      10/16/07 - add new
 hopark      07/09/07 - use PagedTupleFactory
 hopark      09/19/07 - add iterator
 hopark      08/28/07 - use two impl of DoublyListIter
 hopark      08/29/07 - add DoublyList2IterFactory
 hopark      06/19/07 - cleanup
 hopark      06/13/07 - rename LogFlags
 skmishra    06/07/07 - fix lint error
 hopark      05/22/07 - logging support
 hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
 najain      03/14/07 - cleanup
 hopark      03/12/07 - add StoredTupleFactory
 najain      03/12/07 - bug fix
 najain      03/08/07 - cleanup
 najain      03/08/07 - cleanup
 hopark      03/06/07 - spill-over support
 parujain    02/21/07 - remove ElementList
 parujain    02/13/07 - interface with configManager
 parujain    02/21/07 - remove ElementList
 parujain    02/13/07 - interface with configManager
 najain      01/24/07 - add SinglyListNodeFactory
 hopark      01/07/07 - add factory ID
 hopark      12/26/06 - add TimedTupleFactory
 parujain    11/30/06 - DoublyListIter Factory
 rkomurav    11/25/06 - add debuglevel support
 najain      11/08/06 - doubly list is a storage element
 najain      08/18/06 - concurrency issues
 parujain    07/31/06 - Generic Doubly linkedlist
 parujain    07/26/06 - Generic objects 
 parujain    07/24/06 - Generic LinkedList 
 najain      07/21/06 - ref-count tuples 
 najain      06/29/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/FactoryManager.java /main/38 2008/10/24 15:50:19 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import java.util.BitSet;
import java.util.Iterator;

import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.factory.QueueElementFactory;
import oracle.cep.memmgr.factory.memory.DoublyListItrFactory;
import oracle.cep.memmgr.factory.memory.DoublyListFactory;
import oracle.cep.memmgr.factory.memory.DoublyListNodeFactory;
import oracle.cep.memmgr.factory.memory.TDoublyListIterFactory;
import oracle.cep.memmgr.factory.memory.TDoublyListFactory;
import oracle.cep.memmgr.factory.memory.TDoublyListNodeFactory;
import oracle.cep.memmgr.factory.memory.PartitionFactory;
import oracle.cep.memmgr.factory.memory.PartitionIterFactory;
import oracle.cep.memmgr.factory.memory.PartitionNodeFactory;
import oracle.cep.memmgr.factory.memory.TTSinglyListFactory;
import oracle.cep.memmgr.factory.memory.TTSinglyListNodeFactory;
import oracle.cep.memmgr.factory.memory.QSinglyListFactory;
import oracle.cep.memmgr.factory.memory.QSinglyListNodeFactory;
import oracle.cep.memmgr.factory.memory.SinglyListFactory;
import oracle.cep.memmgr.factory.memory.SinglyListIterFactory;
import oracle.cep.memmgr.factory.memory.SinglyListNodeFactory;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;

/**
 * Manages allocation of all kinds of factories
 * 
 * @author najain
 * @since 1.0
 */

public class FactoryManager
{
  private IAllocator[]            factories;
  private IAllocator[]            fixedFactories;
  private BitSet                  usages;
  
  private static final int        INIT_SZ                       = 32;
  private boolean                 memMode;
  private CEPManager              cepMgr;
  private IEvictPolicy            evPolicy;
  private boolean                 usePagedTuple;
  private boolean                 usePagedList;

  public static final int         TUPLE_FACTORY_ID               = 1;
  public static final int         DOUBLY_LIST_NODE_FACTORY_ID    = 2;
  public static final int         DOUBLY_LIST_FACTORY_ID         = 3;
  public static final int         DOUBLY_LIST_ITER_FACTORY_ID    = 4;
  public static final int         DOUBLY_LIST_NODE2_FACTORY_ID   = 5;
  public static final int         MSINGLY_LIST_NODE_FACTORY_ID   = 6;
  public static final int         MSINGLY_LIST_FACTORY_ID        = 7;
  public static final int         SINGLY_LIST_NODE_FACTORY_ID    = 8;
  public static final int         SINGLY_LIST_FACTORY_ID         = 9;
  public static final int         SINGLY_LIST_ITER_FACTORY_ID    = 10;
  public static final int         QSINGLY_LIST_FACTORY_ID        = 11;
  public static final int         QSINGLY_LIST_NODE_FACTORY_ID   = 12;
  public static final int         PARTITION_FACTORY_ID           = 13;
  public static final int         PARTITION_ITER_FACTORY_ID      = 14;
  public static final int         PARTITION_NODE_FACTORY_ID      = 15;
  public static final int         TTSINGLY_LIST_FACTORY_ID       = 16;
  public static final int         TTSINGLY_LIST_NODE_FACTORY_ID  = 17;
  public static final int         QUEUEELEMENT_FACTORY_ID        = 18;
  public static final int         TDOUBLY_LIST_NODE_FACTORY_ID   = 19;
  public static final int         TDOUBLY_LIST_FACTORY_ID        = 20;
  public static final int         TDOUBLY_LIST_ITER_FACTORY_ID   = 21;
  public static final int         WINSTOREPAGE_FACTORY_ID        = 22;
  public static final int         QUEUEPAGE_FACTORY_ID           = 23;
  public static final int         SQPAGE_FACTORY_ID              = 24;
  public static final int         MAX_FIXED_FACTORY_ID           = 25;

  private interface IFactoryGen
  {
    IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode);
  }
  private IFactoryGen[] factoryGens;

  public FactoryManager(CEPManager cepMgr)
  {
    this.cepMgr = cepMgr;
    ConfigManager cm = cepMgr.getConfigMgr();
    usePagedTuple = cm.getUsePagedTuple();
    usePagedList = cm.getUsePagedList();
    
    int sz = MAX_FIXED_FACTORY_ID + INIT_SZ;
    factories = new IAllocator[sz];
    fixedFactories = new IAllocator[MAX_FIXED_FACTORY_ID];

    for (int i=0; i<MAX_FIXED_FACTORY_ID; i++)
      fixedFactories[i] = null;
    for (int i = 0; i < sz; i++)
      factories[i] = null;
    usages = new BitSet();
    
    evPolicy = cepMgr.getEvictPolicy();
    memMode = (evPolicy == null || !evPolicy.isFullSpill());
    if (!memMode)
    {
      usePagedTuple = true;
      usePagedList = true;
    }
    factoryGens = new IFactoryGen[MAX_FIXED_FACTORY_ID];
    factoryGens[MSINGLY_LIST_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        return new SinglyListNodeFactory(factoryMgr, id);
      }
    };
    factoryGens[QUEUEELEMENT_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        return new QueueElementFactory(factoryMgr, id);
      }
    };
    factoryGens[MSINGLY_LIST_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        return new SinglyListFactory(factoryMgr, id);
      }
    };
    factoryGens[TUPLE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        return null;
      }
    };
    factoryGens[DOUBLY_LIST_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        ConfigManager cm = factoryMgr.getServiceManager().getConfigMgr();
        int initPageTableSize = cm.getListInitPageTableSize();
        if (memMode)
        {
          return usePagedList ? new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize) 
                              : new DoublyListNodeFactory(factoryMgr, id);
        }
        else
        {
          return new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize);
        }
      }
    };
    factoryGens[DOUBLY_LIST_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.DoublyListFactory(factoryMgr, id) 
                              : new DoublyListFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.DoublyListFactory(factoryMgr, id);
      }
    };
    factoryGens[DOUBLY_LIST_ITER_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.DoublyListIterFactory(factoryMgr, id) 
                              : new DoublyListItrFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.DoublyListIterFactory(factoryMgr, id);
      }
    };
    factoryGens[SINGLY_LIST_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        ConfigManager cm = factoryMgr.getServiceManager().getConfigMgr();
        int initPageTableSize = cm.getListInitPageTableSize();
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize) 
                              : new SinglyListNodeFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize);
      }
    };
    factoryGens[SINGLY_LIST_ITER_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.SinglyListIterFactory(factoryMgr, id) 
                              : new SinglyListIterFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.SinglyListIterFactory(factoryMgr, id);
      }
    };
    factoryGens[SINGLY_LIST_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.SinglyListFactory(factoryMgr, id) 
                              : new SinglyListFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.SinglyListFactory(factoryMgr, id);
      }
    };
    factoryGens[QSINGLY_LIST_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.QSinglyListFactory(factoryMgr, id) 
                              : new QSinglyListFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.QSinglyListFactory(factoryMgr, id);
      }
    };
    factoryGens[QSINGLY_LIST_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        ConfigManager cm = factoryMgr.getServiceManager().getConfigMgr();
        int initPageTableSize = cm.getListInitPageTableSize();
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize)
                              : new QSinglyListNodeFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize);
      }
    };
    factoryGens[PARTITION_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.PartitionFactory(factoryMgr, id) 
                              : new PartitionFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.PartitionFactory(factoryMgr, id);
      }
    };
    factoryGens[PARTITION_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        ConfigManager cm = factoryMgr.getServiceManager().getConfigMgr();
        int initPageTableSize = cm.getListInitPageTableSize();
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize)
                              : new PartitionNodeFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize);
      }
    };
    factoryGens[PARTITION_ITER_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.PartitionIterFactory(factoryMgr, id) 
                              :  new PartitionIterFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.PartitionIterFactory(factoryMgr, id);
      }
    };
    factoryGens[TTSINGLY_LIST_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.TTSinglyListFactory(factoryMgr, id) 
                              : new TTSinglyListFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.TTSinglyListFactory(factoryMgr, id);
      }
    };
    factoryGens[TTSINGLY_LIST_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        ConfigManager cm = factoryMgr.getServiceManager().getConfigMgr();
        int initPageTableSize = cm.getListInitPageTableSize();
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize)
                              : new TTSinglyListNodeFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize);
      }
    };
    factoryGens[TDOUBLY_LIST_NODE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        ConfigManager cm = factoryMgr.getServiceManager().getConfigMgr();
        int initPageTableSize = cm.getListInitPageTableSize();
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize) 
                              : new TDoublyListNodeFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.paged.ListNodeFactory(factoryMgr, id, initPageTableSize);
      }
    };
    factoryGens[TDOUBLY_LIST_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.TDoublyListFactory(factoryMgr, id) 
                              : new TDoublyListFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.TDoublyListFactory(factoryMgr, id);
      }
    };
    factoryGens[TDOUBLY_LIST_ITER_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return usePagedList ? new oracle.cep.memmgr.factory.paged.TDoublyListIterFactory(factoryMgr, id) 
                              : new TDoublyListIterFactory(factoryMgr, id);
        else
          return new oracle.cep.memmgr.factory.stored.TDoublyListIterFactory(factoryMgr, id);
      }
    };
    factoryGens[WINSTOREPAGE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return null;
        else
          return new oracle.cep.memmgr.factory.paged.WinStorePageFactory(factoryMgr, id);
      }
    };
    factoryGens[QUEUEPAGE_FACTORY_ID] = new IFactoryGen() {
      public IAllocator newFactory(FactoryManager factoryMgr, int id, boolean memMode)
      {
        if (memMode)
          return null;
        else
          return new oracle.cep.memmgr.factory.paged.QueuePageFactory(factoryMgr, id);
      }
    };
    // populate global factories
    for (int id =  DOUBLY_LIST_NODE_FACTORY_ID; id < MAX_FIXED_FACTORY_ID; id++ )
    {
      if (factoryGens[id] == null) continue;
      IAllocator fac = factoryGens[id].newFactory(this, id, memMode);
      if (fac != null)
        addFactory(fac);
    }

  }
  
  public synchronized int getNextId()
  {
    int id = usages.nextClearBit(MAX_FIXED_FACTORY_ID);
    usages.set(id);
    return id;
  }
  
  public CEPManager getServiceManager() {return cepMgr;}
  
  private void increaseCap(int id)
  {
    int oldsz = factories.length;
    int newsz = factories.length + factories.length / 2 + 1;
    if (newsz <= id)
    {
      newsz = id + id / 2 + 1;
    }
    IAllocator[] newlist = new IAllocator[newsz];
    System.arraycopy(factories, 0, newlist, 0, oldsz);
    factories = newlist;
    for (int i = oldsz; i < newsz; i++)
      factories[i] = null;
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> IAllocator<T> getFactory(int id)
  {
    if (id >= factories.length)
    {
      return null;
    }
    return (IAllocator<T>) factories[id];
  }

  @SuppressWarnings("unchecked")
  public <T> IAllocator<T> getFixedFactory(int id)
  {
    if (id >= fixedFactories.length)
    {
      return null;
    }
    return (IAllocator<T>) fixedFactories[id];
  }

  public synchronized IAllocator addFactory(IAllocator factory)
  {
    int id = factory.getId();
    if (id >= factories.length)
    {
      // Need to increase the array
      increaseCap(id);
    }
    assert (factories[id] == null);
    factories[id] = factory;

    if (id < MAX_FIXED_FACTORY_ID)
      fixedFactories[id] = factory;

    usages.set(id);
    return factory;
  }

  @SuppressWarnings("unchecked")
  public <T> IAllocator<T> get(TupleSpec ts)
  {
    return getFactory(TUPLE_FACTORY_ID, ts);
  }

  @SuppressWarnings("unchecked")
  public IAllocator getFac(int id)
  {
    return getFactory(id);
  }
  
  @SuppressWarnings("unchecked")
  public <T> IAllocator<T> get(int id)
  {
    return getFactory(id);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> IAllocator<T> newFactory(int typeId)
  {
    int id = getNextId();
    IAllocator fac = factoryGens[typeId].newFactory(this, id, memMode);
    addFactory(fac);
    return (IAllocator<T>) fac;
  }
  
  public synchronized void deleteFactory(int id)
  {
    factories[id] = null;
    usages.clear(id);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized <T> IAllocator<T> getFactory(int id, TupleSpec ts)
  {
    IAllocator<T> f = getFactory(id);

    if (f != null)
      return f;

    assert ts != null;

    // TupleSpec awares of MAX_FIXED_FACTORY_ID, so we do not need to consider
    // it here.
    id = ts.getId();
    f = getFactory(id);

    if (f != null)
      return f;

    IAllocator<T> tf = null;
    ConfigManager cfgMgr = cepMgr.getConfigMgr();
    int initPageTableSize = cfgMgr.getTupleInitPageTableSize();
    if (evPolicy == null || !evPolicy.isFullSpill())
    {
      if (usePagedTuple)      
        tf = (IAllocator<T>) new oracle.cep.memmgr.factory.paged.TupleFactory(cepMgr, ts, id, initPageTableSize);
      else
        tf = (IAllocator<T>) new oracle.cep.memmgr.factory.memory.TuplePtrFactory(cepMgr, ts, id);
    }
    else
    {
      tf = (IAllocator<T>) new oracle.cep.memmgr.factory.stored.TupleFactory(cepMgr, ts, id, initPageTableSize);
    }
    addFactory(tf);
    return tf;
  }

  private class FactoryIterator implements Iterator<IAllocator>
  {
    int pos = 0;

    public FactoryIterator()
    {
      pos = 0;
    }
  
    private IAllocator advance(boolean consume)
    {
      IAllocator res = null;
      while (res == null)
      {
        if (pos >= factories.length)
          return null;
        res = factories[pos];
        if (res != null) {
          if (consume) pos++;
          return res;
        }
        pos++;
      }
      return null;
    }    
    
    public IAllocator next()
    {
      return advance(true);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext()
    {
      IAllocator res = advance(false);
      return (res != null);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
      assert false : "not implemented";
    }
  }
  
  public Iterator<IAllocator> getIterator()
  {
    return new FactoryIterator();
  }
}
