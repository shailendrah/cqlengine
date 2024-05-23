/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/metadata/TestStore.java /main/8 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - refactor storage
    parujain    10/02/09 - dependency
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/24/08 - multiple schema
    hopark      09/17/08 - support schema
    hopark      09/12/08 - add schema indexing
    hopark      03/26/08 - server reorg
    hopark      03/20/07 - Creation
 */
package oracle.cep.test.metadata;

import java.io.Serializable;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Table;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheKey;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.MetadataTransaction;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.StorageException;
import oracle.cep.storage.StorageManager;
import oracle.cep.test.storage.TestStorageBase;
import oracle.cep.transaction.TransactionManager;

import java.lang.reflect.*;

import java.util.LinkedList;

import junit.framework.Test;

import junit.framework.TestSuite;

import oracle.cep.execution.ExecException;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.QueryState;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.StorageStat;
import oracle.cep.common.Constants;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/metadata/TestStore.java /main/7 2009/11/23 21:21:22 parujain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class TestStore extends TestCase
{
  IStorage m_storage;
  Cache    m_store;
  QueryManager m_queryMan;
  TransactionManager m_txnMan;
  ExecContext   m_ec;
  
  String ns = "MYOBJ";
  String ns1 = "MYOBJ1";

  private static boolean s_addns = false;
  
  public TestStore(String name)
  {
    super(name);
  }

  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
    TestStorageBase.setUpStorage(true);
    CEPManager cepMgr = CEPManager.getInstance();
    StorageManager sm = cepMgr.getStorageManager();
    m_storage = sm.getMetadataStorage();
    m_ec = cepMgr.getSystemExecContext();
    m_store = m_ec.getCache();
    m_queryMan = m_ec.getQueryMgr();
    m_txnMan = m_ec.getTransactionMgr();
    if (!s_addns)
    {
      System.out.println("add NameSpace");
      try {
        m_storage.addNameSpace(null, ns, false, null, null, null);
        m_storage.addNameSpace(null, ns1, false, null, null, null);
      }
      catch(StorageException e)
      {
      }
      s_addns = true;
    }
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
    StorageStat stat = m_storage.getStat();
    System.out.println(stat.toString());
  }

  private void compareObj(Object a, Object b, String[] ignore)
  {
    Class aclass = a.getClass();
    Class bclass = b.getClass();
    boolean t = aclass.equals(bclass);
    assertTrue(t);
    Field fields[] = aclass.getDeclaredFields();
    for (int i = 0; i < fields.length; ++i) 
    {
      Field f = fields[i];
      if (ignore != null)
      {
        boolean iflag= false;
        for (String name : ignore)
        {
          if (f.getName().equals(name)) 
          {
            iflag = true;
            break;
          }
        }
        if (iflag) continue;
      }
      f.setAccessible(true);
      try 
      {
        Object afv = f.get(a);
        Object bfv = f.get(b);
        if (afv == null || bfv == null)
        {
          t = (afv == bfv);
        } else {
          t = afv.equals(bfv);
        }
        if (!t) 
        {
          System.out.println(aclass.getName() + "." + f.getName() + " " + afv + ":" + bfv);
        }
        assertTrue(t);
      } catch(IllegalAccessException e)
      {
        
      }
    }
  }

  public void verify(NameSpace ns, CacheObject data, String[] ignore, boolean delete)
    throws MetadataException
  {
    IStorageContext  ctx = m_store.beginContext();
    assertNotNull(ctx);
    boolean b = data.writeObject(m_storage, ctx);
    assertTrue(b);
    m_store.removeContext(ctx, true);
    //FIXME. m_store.getType is taking MetadataTransaction instead of ITransaction..
    MetadataTransaction txn = (MetadataTransaction) m_txnMan.begin();
    m_ec.setTransaction(txn);
    
    CacheObject objb = m_store.readObject(txn.getStorageContext(), ns, Constants.DEFAULT_SCHEMA, data.getKey());
    assertNotNull(objb);
    compareObj(data, objb, ignore);
    CacheObjectType objt = m_store.getType(txn, data.getKey(), ns, Constants.DEFAULT_SCHEMA);
    assertNotNull(objt);
    b = objt.equals(data.getType());
    assertTrue(b);
    if (delete)
    {
      ctx = m_store.beginContext();
      assertNotNull(ctx);
      b = objb.deleteObject(m_storage, ctx);
      assertTrue(b);
      m_store.removeContext(ctx, true);
      objb = m_store.readObject(null, ns, Constants.DEFAULT_SCHEMA, data.getKey());
      assertNull(objb);
    }
    txn.commit(m_ec);
    m_ec.setTransaction(null);
  }
  
  private Table addTable(String name, String src) throws MetadataException, CEPException
  {
    Table tbl = new Table(name, Constants.DEFAULT_SCHEMA);
    ReentrantReadWriteLock.WriteLock lock = tbl.getLock().writeLock();
    lock.lock();
    tbl.setIsSilent(true);
    tbl.setSource("<EndPointReference><Address>file:///scratch/temp/test.txt</Address></EndPointReference>");
    lock.unlock();
    return tbl;
  }
  
  private Query addQuery(String name, String cql) throws MetadataException, CEPException
  {
    Query qry = new Query(name, Constants.DEFAULT_SCHEMA, cql);
    ReentrantReadWriteLock.WriteLock lock = qry.getLock().writeLock();
    lock.lock();
    qry.setState(QueryState.READY);
    qry.setDesiredState(QueryState.RUN);
    // TODO:PJ Fix it
    //qry.addRefView(5);
   // qry.addRefFunction(6);
    lock.unlock();
    return qry;
  }
  
  public void testSource() throws MetadataException, CEPException
  {
    System.out.println("testSource");
    String name = "testTbl";
    String src = "<EndPointReference><Address>file:///scratch/temp/test.txt</Address></EndPointReference>";
    Table tbl = addTable(name, src);
    String[] ignore = new String[] {"refQueries"};
    verify(NameSpace.SOURCE, tbl, ignore, true);
  }
  
  public void testQuery() throws MetadataException, CEPException
  {
    System.out.println("testQuery");
    List<String> lst = new LinkedList<String>();
    String name = "qry1";
    String cql = "select c1 from SBigInt [range 10] where c1 <= 10000000000L;";
    Query qry = addQuery(name, cql);
    lst.add(name);
    String[] ignore = new String[] {"refQueries", "lastDestId"};
    verify(NameSpace.QUERY, qry, ignore, false);
    String name1 = "qry2";
    cql = "select c1 from SBigInt [range 10];";
    Query qry2 = addQuery(name1, cql);
    lst.add(name1);
    verify(NameSpace.QUERY, qry2, ignore, false);
    
    int cnt = 0;
    IStorageContext cursor = m_queryMan.intialize(null);
    String q = null;
    CacheKey key = null;
    do {
      key = m_queryMan.getNextQuery(cursor);
      if (key == null)
        break;
      q = key.getObjectName().toString();
      if (q != null)
      {
        cnt++;
        boolean b = lst.contains(q);
        assertTrue(b);
      }
    } while(q != null);
    assertEquals(cnt, lst.size());
  }

  private static class MyKey implements Serializable
  {
    String m_name;
    MyKey(String n) 
    {
      m_name = n;
    }    
  }

  private static class MyObj implements Serializable
  {
    String m_name;
    String m_addr;
    MyObj(String n, String a) 
    {
      m_name = n;
      m_addr = a;
    }
  }

  private static class MyObjNS 
  {
    String m_name;
    String m_addr;
    MyObjNS(String n, String a) 
    {
      m_name = n;
      m_addr = a;
    }
  }
  
  public void testNameSpace() throws ExecException
  {
    System.out.println("testNameSpace");
    MyObj obj = new MyObj("Oracle", "Redwod");
    boolean b = m_storage.putRecord(null, ns, null, new MyKey(obj.m_name), obj);
    assertTrue(b);
/* -ea should be turned of for this
    try {
      MyObjNS obj1 = new MyObjNS("Oracle", "Redwod");
      b = m_storage.putRecord(null, ns, new MyKey(obj1.m_name), obj1);
      assertFalse(b);
    } catch(Throwable e)
    {
    }
*/    
  }

  public void testClean() throws ExecException
  {
    System.out.println("testClean");
    m_storage.clean();
    m_storage.close();
    m_storage.open();
  }

/* TODO: need to find a way to keep closed db.
  public void testExceptionW() throws ExecException
  {
    m_storage.close();
    
    MyObj obj = new MyObj("Oracle", "Redwod");
    boolean b = m_storage.putRecord(null, ns, new MyKey(obj.m_name), obj);
    assertFalse(b);

  }
  
  public void testExceptionR() throws ExecException
  {
    m_storage.close();
    
    Object o = m_storage.getRecord(null, ns, new MyKey("XYZ"));
    assertNull(o);
 }
    
  public void testExceptionD() throws ExecException
  {
    m_storage.close();
    
    boolean b = m_storage.deleteRecord(null, ns, new MyKey("XYZ"));
    assertFalse(b);
  }
*/

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    addSuite(suite);
    return suite;
  }
  
  public static Test addSuite(TestSuite suite)
  {
    suite.addTest(new TestStore("testSource"));
    suite.addTest(new TestStore("testQuery"));
    suite.addTest(new TestStore("testNameSpace"));
    suite.addTest(new TestStore("testClean"));
    return suite;
  }

  public static void main(String[] args)
  {
      junit.textui.TestRunner.run(suite());
  }
}
