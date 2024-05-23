/* $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestDoublyList1.java /main/11 2008/10/24 15:50:22 hopark Exp $ */

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
    hopark      10/16/07 - use local node factory
    hopark      10/23/07 - remove TimeStamp
    hopark      08/28/07 - use itr factory
    najain      03/15/07 - cleanup
    najain      03/12/07 - bug fix
    najain      03/06/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestDoublyList1.java /main/11 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.dataStructures;

import oracle.cep.dataStructures.internal.IDoublyList;
import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.CEPManager;

public abstract class TestDoublyList1 extends TestDoublyList
{
  protected IAllocator nodeFac;
  protected IAllocator listFac;

  public TestDoublyList1()
  {
    super();
  }

  @SuppressWarnings("unchecked")
  protected void setUpData()
  {
    try {
      super.setUpData();

      CEPManager cepMgr = CEPManager.getInstance();
      FactoryManager factoryMgr = cepMgr.getFactoryManager();
      nodeFac = factoryMgr.getFac(FactoryManager.DOUBLY_LIST_NODE_FACTORY_ID);
      listFac = factoryMgr.getFac(FactoryManager.DOUBLY_LIST_FACTORY_ID);
      listItrFac = factoryMgr.getFac(FactoryManager.DOUBLY_LIST_ITER_FACTORY_ID);

      tupList = (IDoublyList<ITuplePtr>) listFac.allocate();
      tupList.setFactory(nodeFac);
      
      //Allocate a tuple
      int specid = 0;
      ITuplePtr ref;
      TTuple tt;
      tt = new TTuple();
      ref = tt.setTuple(specid, 12345, "Test");
      tupList.add(ref);
      tuples.add(tt);
      
      tt = new TTuple();
      ref = tt.setTuple(specid, 45, "def");
      tupList.add(ref);
      tuples.add(tt);

      tt = new TTuple();
      ref = tt.setTuple(specid, 12, "abc");
      tupList.add(ref);
      tuples.add(tt);
    } catch(CEPException e)
    {
      System.out.println(e.toString());
    }
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
    tupList = null;
    tuples = null;
  }

  @SuppressWarnings("unchecked")
  protected void verifyNode(IListNode node, TTuple tt)
    throws CEPException
  {
    IDoublyListNode<ITuplePtr> n = (IDoublyListNode<ITuplePtr>) node;
    ITuplePtr nt = n.getNodeElem();
    assertEquals(tt.m_tuple, nt);
  }
  
}
