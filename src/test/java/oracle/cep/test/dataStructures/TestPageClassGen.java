/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestPageClassGen.java /main/5 2009/11/09 10:10:59 sborah Exp $ */

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
    hopark      10/10/08 - remove statics
    hopark      02/04/08 - support double
    hopark      09/04/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestPageClassGen.java /main/5 2009/11/09 10:10:59 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.dataStructures;

import java.util.Random;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.factory.stored.TupleFactory;
import oracle.cep.service.CEPManager;
import oracle.cep.test.storage.TestStorageBase;

public class TestPageClassGen extends TestTuple
{
  Random m_rand = new Random(123456); 

  public void setUp()
  {
    TestStorageBase.setUpStorage(true);
    m_doEvicts = true;
  }

  protected IAllocator<ITuplePtr> getTupleFactory(TupleSpec spec)
  {
    CEPManager cepMgr = CEPManager.getInstance();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    IAllocator<ITuplePtr> fac = factoryMgr.get(spec);
    TupleFactory pfac = (TupleFactory) fac;
    PageManager pm = pfac.getPageManager();
    pm.setUseDynamicPage(true);
    pfac.setInitialPages(120);
    
    return fac;
  }

  protected void printStat()
  {
    TupleFactory fac = (TupleFactory) m_tupleFactory;
    System.out.println(fac.toString());
  }

  protected void resetFac() throws CEPException
  {
    TupleFactory fac = (TupleFactory) m_tupleFactory;
    fac.reset();
  }

  private TupleItem alloc() throws CEPException
  {
    TupleItem item = new TupleItem();
    item.m_ref = m_tupleFactory.allocate();
    int j = m_rand.nextInt(1000);
    item.fill(j);
    return item;
  }
  
  public void sizeTest() throws CEPException {}
  public void timeTest() throws CEPException {}
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestPageClassGen.class);
  }
}
