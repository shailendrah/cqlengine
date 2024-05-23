/* $Header: TestDoublyList.java 11-jan-2008.10:15:35 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/31/07 - change DoublyList api
    hopark      12/18/07 - change iterator semantics
    hopark      10/16/07 - use local node factory
    hopark      10/23/07 - remove TimeStamp
    hopark      08/28/07 - use factory
    hopark      06/20/07 - cleanup
    hopark      03/27/07 - 
    najain      03/15/07 - cleanup
    najain      03/12/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: TestDoublyList.java 11-jan-2008.10:15:35 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.dataStructures;

import oracle.cep.dataStructures.internal.IDoublyListIter;
import oracle.cep.dataStructures.internal.IDoublyList;
import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.exceptions.CEPException;
import oracle.cep.dataStructures.internal.ITuplePtr;

public abstract class TestDoublyList extends TestList
{
  protected IDoublyList<ITuplePtr> tupList;
  
  public TestDoublyList()
  {
  }

  @SuppressWarnings("unchecked")
  protected void verifyNode(IListNode node, TTuple tt)
    throws CEPException
  {
    IDoublyListNode<ITuplePtr> n = (IDoublyListNode<ITuplePtr>) node;
    ITuplePtr nt = n.getNodeElem();
    assertEquals(tt.m_tuple, nt);
  }
  
  private void verifyDoublyList()
    throws CEPException
  {
    int size = tuples.size();
    int ssize = tupList.getSize();
    assertEquals(size, ssize);

    TTuple tt;
    IDoublyListNode<ITuplePtr> h = tupList.getHead();
    ITuplePtr ht = h.getNodeElem();
    tt = tuples.get(0);
    assertEquals(tt.m_tuple, ht);
    ITuplePtr htt = tupList.getFirst();
    assertEquals(ht, htt);
    
    IDoublyListNode<ITuplePtr> t = tupList.getTail();
    ITuplePtr tailt = t.getNodeElem();
    tt = tuples.get(size-1);
    assertEquals(tt.m_tuple, tailt);
    ITuplePtr ttt = tupList.getLast();
    assertEquals(tailt, ttt);
  
    IDoublyListNode<ITuplePtr> n = h;
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
      tt = tuples.get(i);
      verifyNode(n, tt);
      // check prev node
      if (i > 0) 
      {
        IDoublyListNode<ITuplePtr> prev = (IDoublyListNode<ITuplePtr>) n.getPrev(tupList);
        ITuplePtr pt = prev.getNodeElem();
        TTuple tpt = tuples.get(i-1);
        assertEquals(tpt.m_tuple, pt);
      }
      // check next node
      if (i < size -1) 
      {
        IDoublyListNode<ITuplePtr> next = (IDoublyListNode<ITuplePtr>) n.getNext(tupList);
        ITuplePtr nextt = next.getNodeElem();
        TTuple tnt = tuples.get(i+1);
        assertEquals(tnt.m_tuple, nextt);
      }

      n = (IDoublyListNode<ITuplePtr>) n.getNext(tupList);
    }
  }

  protected void evicts() throws CEPException {}
  
  public void test0()
    throws CEPException
  {
    verifyDoublyList();
    // evict all
    evicts();
    verifyDoublyList();
  }

  public void test1()
  throws CEPException
  {
    int size = tuples.size();
    TTuple tt;
    ITuplePtr ht = tupList.peek();
    tt = tuples.get(0);
    assertEquals(tt.m_tuple, ht);

    assertEquals(size, tupList.getSize());

    assertFalse(tupList.isEmpty());
  }

  public void testContain()
    throws CEPException
  {
    for (TTuple tt : tuples) 
    {
      boolean contain = tupList.contains(tt.m_tuple);
      assertTrue(contain);
    }
    TTuple tt1 = new TTuple();
    ITuplePtr ref = tt1.setTuple(specid, 45, "def");
    boolean contain = tupList.contains(ref);
    assertFalse(contain);
  }

  public void testRemove0()
    throws CEPException
  {
    TTuple tt = tuples.get(0);
    boolean b = tupList.removeFirst();
    assertTrue(b);
    boolean contain = tupList.contains(tt.m_tuple);
    assertFalse(contain);
    tt = tuples.get(tuples.size()-1);
    b = tupList.removeLast();
    assertTrue(b);
    contain = tupList.contains(tt.m_tuple);
    assertFalse(contain);

    int size = tuples.size();
    tuples.remove(size - 1);  //last
    tuples.remove(0); //first
    verifyDoublyList();
  }

  public void testRemove1()
    throws CEPException
  {
    evicts();
    testRemove0();  
  }

  public void testRemove2()
    throws CEPException
  {
    evicts();
    for (TTuple tt : tuples) 
    {
      ITuplePtr t = tt.m_tuple;
      boolean b = tupList.remove(t);
      assertTrue(b);
      boolean contain = tupList.contains(t);
      assertFalse(contain);
    }
    assertEquals(0, tupList.getSize());
    assertTrue(tupList.isEmpty());

    IDoublyListNode<ITuplePtr> h = tupList.getHead();
    assertNull(h);
    IDoublyListNode<ITuplePtr> t = tupList.getTail();
    assertNull(t);
    ITuplePtr ht = tupList.peek();
    assertNull(ht);
  }

  public void testIter()
    throws CEPException
  {
    evicts();
    int size = tupList.getSize();
    int cnt = 0;
    IDoublyListIter<ITuplePtr> iter = (IDoublyListIter<ITuplePtr>) listItrFac.allocate();
    iter.initialize(tupList);
    while ((iter.next()) != null) 
    {
      cnt++;
    }
    assertEquals(size, cnt);
  }
}

