/* $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/memory/RCTuplePtr.java /main/3 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 RCTuplePtr provides ref count debug facilities for TuplePtr.
 It's only used when DebugUtil.DEBUG_REFCOUNT_TUPLE.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/10/08 - remove statics
 hopark      03/23/08 - refcnt test
 hopark      03/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/memory/RCTuplePtr.java /main/3 2008/10/24 15:50:22 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.util.concurrent.atomic.AtomicLong;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.factory.memory.TuplePtrFactory;
import oracle.cep.service.CEPManager;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.LogUtil;

/**
 * @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/memory/RCTuplePtr.java /main/3 2008/10/24 15:50:22 hopark Exp $
 * @author hopark
 * @since release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id"}, 
          attribVals={"getId"})
public class RCTuplePtr extends TuplePtr
{
  private static AtomicLong s_nextId = new AtomicLong();
  private long    m_id;
  private short   m_refCount;
  private int         m_factoryId;

  public RCTuplePtr(ITuple referent)
  {
    super(referent);
    m_id = s_nextId.incrementAndGet();
    m_refCount = 1;
  }

  public void setFactoryId(int id) {m_factoryId = id;}
  public int getFactoryId() {return m_factoryId;}
  
  public synchronized int getRefCount() {return m_refCount;}
  public synchronized int addRef(int ref)
  {
    int rc = 0;
    m_refCount += ref;
    rc = m_refCount;
    return rc;
  }
  
  public synchronized int release()
  {
    int rc = 0;
    m_refCount--;
    rc = m_refCount;
    if (rc == 0)
    {
      m_ref = null;
    }
    return rc;
  }
  
  /**
   * Pins the tuple. If the tuple has swappend out, retreive it from
   * the storage and reset the referent.
   * 
   * @return
   */
  public ITuple pinTuple(int mode) throws ExecException
  {
    if (m_ref == null)
    {
      String msg = "*** Trying to pinTuple from deleted tuplePtr(" + getId() + ")";
      LogUtil.info(LoggerType.TRACE, msg);
      FactoryManager factoryMgr = CEPManager.getInstance().getFactoryManager();
      IAllocator<ITuplePtr> tpfac =  factoryMgr.get(m_factoryId);
      TuplePtrFactory factory = (TuplePtrFactory) tpfac;
      factory.dumpRefCount(getId());
      assert false : msg;
    }
    return m_ref;
  }

  public final long getId()
  {
    long id = 0;
    if (m_ref != null)
    	id = m_ref.getId();
    id = m_id;
    return id;
  }
}

