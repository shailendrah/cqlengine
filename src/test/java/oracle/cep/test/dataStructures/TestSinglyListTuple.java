/* $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestSinglyListTuple.java /main/10 2008/10/24 15:50:22 hopark Exp $ */

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
    hopark      10/23/07 - remove Long
    hopark      08/28/07 - use itr factory
    hopark      04/05/07 - 
    najain      03/15/07 - cleanup
    najain      03/12/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestSinglyListTuple.java /main/10 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.dataStructures;

import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.CEPManager;
import oracle.cep.dataStructures.internal.ITuplePtr;

@SuppressWarnings({"unchecked"})
public abstract class TestSinglyListTuple extends TestSinglyList<ITuplePtr>
{
  public TestSinglyListTuple()
  {
    super();
  }

  protected void setUpData()
  {
    try {
      super.setUpData();
      CEPManager cepMgr = CEPManager.getInstance();
      FactoryManager factoryMgr = cepMgr.getFactoryManager();
      IAllocator nodeFac = factoryMgr.get(FactoryManager.SINGLY_LIST_NODE_FACTORY_ID);
      listFac = factoryMgr.get(FactoryManager.SINGLY_LIST_FACTORY_ID);
      listItrFac = factoryMgr.getFac(FactoryManager.SINGLY_LIST_ITER_FACTORY_ID);

      tupList = (ISinglyList<ITuplePtr>) listFac.allocate();
      tupList.setFactory(nodeFac);
      
      //Allocate a tuple
      int specid = 0;
      ITuplePtr ref;
      TTuple tt;
      Long ts;
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

      tupList1 = (ISinglyList<ITuplePtr>) listFac.allocate();
      tupList1.setFactory(nodeFac);
      
      tt = new TTuple();
      ref = tt.setTuple(specid, 112345, "1Test");
      tupList1.add(ref);

      tt = new TTuple();
      ref = tt.setTuple(specid, 145, "1def");
      tupList1.add(ref);

      tt = new TTuple();
      ref = tt.setTuple(specid, 112, "1abc");
      tupList1.add(ref);
    } catch(CEPException e)
    {
      System.out.println(e.toString());
    }
  }
  
  protected void addMore()
  {
    try {
      int specid = 0;
      ITuplePtr ref;
      TTuple tt;
      Long ts;
  
      tt = new TTuple();
      ref = tt.setTuple(specid, 33, "abczasdf");
      tupList.add(ref);
      tuples.add(tt);
  
      tt = new TTuple();
      ref = tt.setTuple(specid, 55, "abcerqwer");
      tupList.add(ref);
      tuples.add(tt);
    } catch(CEPException e)
    {
      System.out.println(e.toString());
    }
    
  }
}
