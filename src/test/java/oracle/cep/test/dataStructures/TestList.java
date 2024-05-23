/* $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestList.java /main/19 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/08/08 - use getFullSpillMode
    hopark      12/27/07 - support xmllog
    hopark      12/06/07 - cleanup spill
    hopark      12/18/07 - change iterator semantics
    hopark      10/16/07 - use local node factory
    hopark      10/30/07 - remove IQueueElement
    hopark      10/23/07 - remove TimeStamp
    hopark      09/19/07 - use FactoryManager.iterator
    hopark      08/28/07 - use itr factory
    hopark      06/20/07 - cleanup
    hopark      06/07/07 - fix lint warning
    hopark      03/21/07 - use ITuple
    najain      03/15/07 - cleanup
    najain      03/12/07 - bug fix
    najain      03/08/07 - cleanup
    hopark      03/07/07 - use ITuplePtr
    najain      03/06/07 - bug fix
    hopark      03/05/07 - Creation
 */

package oracle.cep.test.dataStructures;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.PagedFactory;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.test.storage.TestStorageBase;
import oracle.cep.service.CEPManager;

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestList.java /main/19 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public abstract class TestList extends TestCase
{
  protected int specid;
  protected IAllocator listFac;
  protected IAllocator listItrFac;
  protected IAllocator  elemFac;

  protected List<TTuple> tuples;


  public TestList()
  {
  }


  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
    tuples = null;
  }

  public static class TTuple
  {
    public ITuplePtr m_tuple;
    public Long m_ts;
    public QueueElement.Kind m_kind;

    TestStorageBase.ObjContent m_content;
    long m_id;
    int m_hashcode;
    int m_specid;

    public TTuple()
    {

    }

    public ITuplePtr setTuple(int specid, Object... arguments)
    {
      m_specid = specid;
      TestStorageBase.Helper helper = TestStorageBase.Helper.getInstance();
      try
      {
        m_tuple = helper.allocTuple(specid);
      //REMOVE_TUPLEPIN ITuplePtr tp = (ITuplePtr) m_tuple.pin(IPinnable.READ);
        helper.setTuple(specid, m_tuple, arguments);
        ITuple t = (ITuple)m_tuple.pinTuple(IPinnable.READ);
        m_id = t.getId();
        m_hashcode = t.hashCode();
        m_content = helper.getContent(specid, t);
      //REMOVE_TUPLEPIN m_tuple.unpinTuple();
      //REMOVE_TUPLEPIN m_tuple.unpin();
        return m_tuple;
      } catch (CEPException ce)
      {
        System.out.println(ce.toString());
      }
      return null;
    }

    public void setTs(Long ts)
    {
      m_ts = ts;
    }

    public void setElem(QueueElement e)
      throws CEPException
    {
      m_tuple = e.getTuple();
      m_ts = e.getTs();
      m_kind = e.getKind();
    }

    public void compareTuple(ITuplePtr tuple) throws CEPException
    {
      TestStorageBase.Helper helper = TestStorageBase.Helper.getInstance();
      ITuple tuple1 = (ITuple)m_tuple.pinTuple(IPinnable.READ);
      ITuple tuple2 = (ITuple)tuple.pinTuple(IPinnable.READ);
      helper.compareTuple(m_specid, tuple1, tuple2);
    //REMOVE_TUPLEPIN m_tuple.unpinTuple();
    //REMOVE_TUPLEPIN tuple.unpinTuple();
    }
  }

  protected abstract void setUpSys();

  protected void setUpData()
  {
    tuples = new LinkedList<TTuple>();
  }

  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
    setUpSys();      
    setUpData();
  }

  protected void evicts() throws CEPException
  {
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    IEvictPolicy evPolicy = cepMgr.getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
      return;
      
    Iterator<IAllocator> facItr = factoryMgr.getIterator();
    while (facItr.hasNext())
    {
      IAllocator sf = facItr.next();
    
      if (!(sf instanceof PagedFactory)) continue;
      PagedFactory rsf = (PagedFactory) sf;
      if (rsf.getPageLayout() == null) continue;
      PageManager pm = rsf.getPageManager();
      pm.evict();
    }
  }

  public void testMap() throws CEPException
  {
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    Iterator<IAllocator> facItr = factoryMgr.getIterator();
    while (facItr.hasNext())
    {
      IAllocator sf = facItr.next();
       if (!(sf instanceof PagedFactory)) continue;
       PagedFactory rsf = (PagedFactory) sf;
       if (rsf.getPageLayout() == null) continue;
       PageManager pm = rsf.getPageManager();
       pm.dump(true);
       pm.evict();
    }        
  }
  
}

