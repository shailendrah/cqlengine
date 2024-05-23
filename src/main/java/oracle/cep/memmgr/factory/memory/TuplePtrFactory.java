/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TuplePtrFactory.java /main/8 2009/06/15 22:04:06 hopark Exp $ */
/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */
/*
 DESCRIPTION
 Declares TupleFactory in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    06/11/09 - add refcount dump
 hopark    10/10/08 - remove statics
 hopark    10/16/08 - implement ITupleAllocator
 hopark    03/23/08 - refcnt test
 hopark    02/28/08 - resurrect refcnt
 hopark    10/15/07 - add namespace
 hopark    07/31/07 - add dynamic tuple class gen
 hopark    05/08/07 - ITuple api cleanup
 hopark    03/30/07 - during refresh (mkelem)
 najain    03/12/07 - 
 najain    03/12/07 - 
 hopark    01/17/07 - rename setTupleSpec
 hopark    01/12/07 - spill-over support
 hopark    11/16/06 - add bigint datatype
 parujain  10/06/06 - Interval datatype
 najain    09/07/06 - remove newAttrVal
 parujain  08/04/06 - Datatype Timestamp
 najain    07/05/06 - cleanup
 najain    05/08/06 - add newAttrVal
 najain    05/08/06 - add getTupleSpec 
 najain    04/19/06 - add newTuple 
 najain    04/18/06 - time is a part of tuple 
 anasrini  03/24/06 - add toString 
 najain    03/13/06 - add Constructor
 anasrini  03/14/06 - add public constructor 
 najain    03/06/06 - add more methods
 skaluska  02/25/06 - Creation
 skaluska  02/25/06 - Creation
 */
/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TuplePtrFactory.java /main/8 2009/06/15 22:04:06 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory.memory;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.memory.TuplePtr;
import oracle.cep.dataStructures.internal.memory.RCTuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.ITupleAllocator;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.util.DebugLogger;
import oracle.cep.util.DebugUtil;

public class TuplePtrFactory extends AbsAllocator<ITuplePtr> implements ITupleAllocator
{
  /** TupleSpec for this factory */
  protected TupleSpec tupleSpec;

  private DebugLogger refCountLogger;
  private long allocCount = 0;
  protected boolean dynamicTupleClass;
  
  public TuplePtrFactory(CEPManager cepMgr, TupleSpec spec, int id)
  {
    super(cepMgr.getFactoryManager(), id, NameSpace.TUPLEPTR);
    this.tupleSpec = spec;
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      refCountLogger = new DebugLogger("RefCount", DebugLogger.FIELD, "m_refCount", false);
      allocCount = 0;
    }
    tupleSpec.setBaseClass("oracle.cep.dataStructures.internal.memory.DynTupleBase");
    ConfigManager cm = cepMgr.getConfigMgr();
    dynamicTupleClass = cm.getDynamicTupleClass();
  }

  // Not for general usage. only for unit testing
  public void useDynamicTupleClass(boolean b)
  {
    dynamicTupleClass = b;
  }
  
  public ITuplePtr allocate() throws ExecException
  {
    ITuple elem = (ITuple) allocBody();
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      RCTuplePtr ref = new RCTuplePtr(elem);
      ref.setFactoryId(getId());
      allocCount++;
      return ref;
    }
    return new TuplePtr(elem);
  }
  
  /**
   * @return Returns the tupleSpec.
   */
  public TupleSpec getTupleSpec()
  {
    return tupleSpec;
  }

  protected ITuple allocateTuple()
  {
    return new oracle.cep.dataStructures.internal.memory.Tuple();
  }
  
  public Object allocBody() throws ExecException
  {
    ITuple t = null;
    if (dynamicTupleClass)
    {
      try {
        Class tc = tupleSpec.getTupleClass();
        t = (ITuple) tc.newInstance();
       } catch(Exception ex) {
         LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
       }
    } 
    else
    {
      t = allocateTuple();
    }
    t.init(tupleSpec, false /* nullValue */);
    return t;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<TupleFactory id=\"" + getId() + "\">");
    sb.append(tupleSpec.toString());
    sb.append("</TupleFactory>");

    return sb.toString();
  }

  public void dumpRefCount(long id)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      refCountLogger.print(id, DebugLogger.ALL);
    }
  }
  
  public int dumpRefCount(java.io.Writer writer)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      String desc = this.getClass().getName() + "_" + this.getId() + " alloc="+allocCount+" ";
      return refCountLogger.printAll(writer, desc, DebugLogger.DEFAULT);
    }
    return 0;
  }

  public void clearRefCount()
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      refCountLogger.clear();
    }
  }

  public int addRef(ITuplePtr element)
  {
    int rc = 0;
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      RCTuplePtr rct = (RCTuplePtr) element;
      rc = rct.addRef(1);
      refCountLogger.log(element.getId(), element);
    }
    return rc;
  }
  
  /**
   * Adds new references to the specified StorageElement. This increments an
   * internal reference count by the specified count.
   * 
   * @param element
   *          The StorageElement to which the reference needs to be added.
   * @param ref
   *          The number of new references.
   */
  public int addRef(ITuplePtr element, int ref)
  {
    int rc = 0;
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      RCTuplePtr rct = (RCTuplePtr) element;
      rc = rct.addRef(ref);
      refCountLogger.log(element.getId(), element, new Integer(ref));
    }
    return rc;
  }
  
  /**
   * Removes existing reference to the specified StorageElement. This decrements
   * an internal reference count by 1.
   * 
   * @param element
   *          The StorageElement to which the reference needs to be added.
   */
  public int release(ITuplePtr element)
  {
    int rc = 0;
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      RCTuplePtr rct = (RCTuplePtr) element;
      rc = rct.release();
      if (rct.getFactoryId() != getId() )
      {
        String msg = "*** Trying to release from the wrong factory (alloc=" + rct.getFactoryId() +
               " release=" + getId() +")";
        LogUtil.info(LoggerType.TRACE, msg);
        dumpRefCount(rct.getId());
        assert false : msg;
      }
      refCountLogger.log(element.getId(), element, "release");
      if (rc == 0) 
      {
        refCountLogger.log(element.getId(), element, "free");
        refCountLogger.free(element.getId());
      }
      if (rct.getRefCount() < 0)
      {
        String msg = "*** Trying to release the tuple which is already freed." + rct.getFactoryId() +
        " release=" + getId() +")";
         LogUtil.info(LoggerType.TRACE, msg);
         dumpRefCount(rct.getId());
         assert false : msg;
      }
    }
    return rc;
  }
}


