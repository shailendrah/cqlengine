/* $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestSinglyList.java /main/13 2008/12/10 18:55:57 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    hopark      10/10/08 - remove statics
    hopark      12/27/07 - support xmllog
    hopark      12/27/07 - support xmllog
    hopark      12/18/07 - change iterator semantics
    hopark      10/23/07 - remove TimeStamp
    hopark      10/08/07 - use IListNodeElem
    hopark      08/28/07 - use itr factory
    hopark      06/20/07 - cleanup
    najain      03/12/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/dataStructures/TestSinglyList.java /main/13 2008/12/10 18:55:57 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.dataStructures;

import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IListNodeElem;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ISinglyListIter;
import oracle.cep.dataStructures.internal.ISinglyListNode;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.dumper.StrDumper;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.CEPManager;


public abstract class TestSinglyList<E extends IListNodeElem> extends TestList
{
  protected ISinglyList<E> tupList;
  protected ISinglyList<E> tupList1;
  
  public TestSinglyList()
  {
    super();
  }

  protected abstract void addMore();

  protected void verifyElement(TTuple tt, E elem)
  {
    assertEquals(tt.m_tuple, elem);
  }

  private void verifyList() throws CEPException
  {
    int size = tuples.size();
    int ssize = tupList.getSize();
    assertEquals(size, ssize);

    TTuple tt;
    ISinglyListNode<E> h = tupList.getHead();
    E ht = h.getNodeElem();
    tt = tuples.get(0);
    verifyElement(tt, ht);
    E ht1 = tupList.getFirst();
    assertEquals(ht, ht1);

    ISinglyListNode<E> t = tupList.getTail();
    E tailt = t.getNodeElem();
    tt = tuples.get(size - 1);
    verifyElement(tt, tailt);

    ISinglyListNode<E> n = h;
    for (int i = 0; i < size; i++)
    {
      System.out.println(i + " : " + n.toString());
      int hashCode = n.hashCode();
      assertTrue(hashCode != 0);
      if (i == 0)
      {
        boolean b = n.equals(h);
        assertTrue(b);
      } else
      {
        boolean b = n.equals(h);
        assertFalse(b);
      }

      E nt = n.getNodeElem();
      tt = tuples.get(i);
      verifyElement(tt, nt);
      // check next node
      if (i < size - 1)
      {
        ISinglyListNode<E> next = n.getNext(tupList);
        E nextt = next.getNodeElem();
        TTuple tnt = tuples.get(i + 1);
        verifyElement(tnt, nextt);
      }

      n = n.getNext(tupList);
    }

  }

  public void test0() throws CEPException
  {
    LogLevelManager lm = CEPManager.getInstance().getSystemExecContext().getLogLevelManager();
    IDumpContext dumper = lm.openDumper(null, null);
    tupList.dump(dumper);
    dumper.close();
    System.out.println(dumper.toString());
    verifyList();
    evicts();
    verifyList();
  }

  public void test1() throws CEPException
  {
    int size = tuples.size();
    TTuple tt;
    E ht = tupList.getFirst();
    tt = tuples.get(0);
    verifyElement(tt, ht);

    assertEquals(size, tupList.getSize());

    assertFalse(tupList.isEmpty());
  }

  public void testRemove0() throws CEPException
  {
    TTuple tt = tuples.get(0);
    E t = tupList.remove();
    verifyElement(tt, t);

    tt = tuples.get(1);
    t = tupList.remove();
    verifyElement(tt, t);

    tuples.remove(0); //first
    tuples.remove(0); //second
    verifyList();
  }

  public void testRemove1() throws CEPException
  {
    evicts();
    testRemove0();
  }

  public void testRemove2() throws CEPException
  {
    evicts();
    for (TTuple tt: tuples)
    {
      E t1 = tupList.remove();
      verifyElement(tt, t1);
    }
    assertEquals(0, tupList.getSize());
    assertTrue(tupList.isEmpty());

    IListNode<E> h = tupList.getHead();
    assertNull(h);
    IListNode<E> t = tupList.getTail();
    assertNull(t);
    E ht = tupList.getFirst();
    assertNull(ht);
  }

  public void testRemove3() throws CEPException
  {
    evicts();
    tupList.remove(true);
    tupList.remove(true);

    tuples.remove(0); //first
    tuples.remove(0); //second
    verifyList();
  }

  public void testRemove4() throws CEPException
  {
    evicts();
    for (TTuple tt: tuples)
    {
      tupList.remove(true);
    }
    assertEquals(0, tupList.getSize());
    assertTrue(tupList.isEmpty());

    IListNode<E> h = tupList.getHead();
    assertNull(h);
    IListNode<E> t = tupList.getTail();
    assertNull(t);
    E ht = tupList.getFirst();
    assertNull(ht);

    //
    //tupList.remove(true);

  }

  public void testRemove5() throws CEPException
  {
    evicts();
    addMore();
    tupList.remove(true);
    tupList.remove(true);

    tuples.remove(0); //first
    tuples.remove(0); //second
    verifyList();
  }


  public void testRemove6() throws CEPException
  {
    evicts();
    tupList.remove(false);
    tupList.remove(false);

    tuples.remove(0); //first
    tuples.remove(0); //second
    verifyList();
  }

  @SuppressWarnings("unchecked")
  public void testIter() throws CEPException
  {
    if (listItrFac == null) return;
    int size = tupList.getSize();
    ISinglyListIter<E> iter = null;
    for (int i = 0; i < 2; i++)
    {
      int cnt = 0;
      if (i == 0)
       {
         iter = (ISinglyListIter<E>) listItrFac.allocate();
       } 
       iter.initialize(tupList);
      while ( (iter.next()) != null)
      {
        cnt++;
      }
      assertEquals(size, cnt);
    }
  }

  public void testSetNext()
    throws CEPException
  {
    ISinglyListNode<E> headt = tupList1.getHead();
    ISinglyListNode<E> tailt = tupList.getTail();
    tailt.setNext(headt);
  }

  public void testClear()
    throws CEPException
  {
    tupList.clear();
  }
  
}

