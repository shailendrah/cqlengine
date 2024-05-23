/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XmlManager.java /main/4 2008/10/24 15:50:20 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/23/08 - turn off spill mode until fix 7507547
    hopark      10/10/08 - remove statics
    hopark      09/17/08 - support schema
    parujain    05/15/08 - fix mode detection
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XmlManager.java /main/4 2008/10/24 15:50:20 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import oracle.cep.execution.xml.stored.XMLItem;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IManagedObj;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.xquery.Configuration;
import oracle.xquery.XQMesg;
import oracle.xquery.XQueryContext;

public class XmlManager
{
  CEPManager cepMgr;
  boolean memMode;
  
  AtomicLong nextId;
  Map<Long, IManagedObj> map;
  Map<Integer, XMLItem> docMap;

  public XmlManager(CEPManager cepMgr)
  {
    this.cepMgr = cepMgr;
    nextId = new AtomicLong();
    map = new HashMap<Long, IManagedObj>();
    IEvictPolicy evPolicy = cepMgr.getEvictPolicy();
    //memMode = (evPolicy == null || !evPolicy.isFullSpill());
    memMode = true; //disabled due to bug 7507547..
    docMap = new HashMap<Integer, XMLItem>();
  }
  
  // Only used by junit test
  public void setMemMode(boolean b)
  {
    memMode = b;
  }
  
  public IXmlContext createContext()
  {
    return memMode ?
	  new oracle.cep.execution.xml.XmlItemContext(this) :
	  new oracle.cep.execution.xml.stored.XmlItemContext(this);
  }
  
  public PreparedXQuery createPreparedXQuery(String qry)
  {
    XQMesg msg = XQMesg.newInstance(null);
    XQueryContext ctx = new XQueryContext(msg);
    Configuration config = new Configuration();
    config.setXQueryOption(Configuration.XQUERY_UNKNOWN_VARS_AS_EXTERNAL);
    config.setXQueryOption(Configuration.NO_STATIC_TYPING);
    oracle.xquery.PreparedXQuery xq = ctx.prepareXQuery(qry, config);
    return memMode ? new oracle.cep.execution.xml.PreparedXQuery(this, xq) :
      new oracle.cep.execution.xml.stored.PreparedXQuery(this, xq);
  }
  
  public long getNextXMObjID() 
  {
    return nextId.getAndIncrement();
  }
  
  public IManagedObj getXMLObj(long id)
  {
    return map.get(id);
  }
  
  public void putXMLObj(long id, IManagedObj obj)
  {
    map.put(id, obj);
  }

  public Object readXMLObj(long id)
  {
    try
    {
      IStorage storage = cepMgr.getStorageManager().getSpillStorage();
      assert (storage != null);

      NameSpace ns = NameSpace.XMLITEM;
      Object ret =  storage.getRecord(null, ns.toString(), new Long(id));
      assert (ret != null) : "failed to retrieve xmlobj : " + id;
      return ret;
    }
    catch(Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }
  
  public boolean writeXMLObj(long id, Object obj)
  {
    try
    {
      IStorage storage = cepMgr.getStorageManager().getSpillStorage();
      assert (storage != null);
      return storage.putRecord(null, NameSpace.XMLITEM.toString(), null /* schema */, new Long(id), obj);
    }
    catch(Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }  
    return false;
  }
  
  public void freeXMLObj(long id, boolean stored)
  {
    assert (id >= 0);
    map.remove(id);
    if (stored)
    {
      IStorage storage = cepMgr.getStorageManager().getSpillStorage();
      assert (storage != null);
      storage.deleteRecord(null, NameSpace.XMLITEM.toString(), null /* schema */, new Long(id));
    }
  }
  
  public XMLItem getDocument(int docId)
  {
    return docMap.get(docId);
  }
  
  public void putDocument(int docId, XMLItem item)
  {
    docMap.put(docId, item);
  }

  public XMLItem removeDocument(int docId)
  {
    return docMap.remove(docId);
  }
    
}
