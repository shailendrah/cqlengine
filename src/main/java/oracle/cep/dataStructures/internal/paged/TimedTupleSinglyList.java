/* $Header: TimedTupleSinglyList.java 03-mar-2008.13:47:26 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
   TimedTupleSinglyList

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/28/08 - tupleptr serialization optimization
    hopark      10/18/07 - Creation
 */

/**
 *  @version $Header: TimedTupleSinglyList.java 03-mar-2008.13:47:26 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.paged;

import oracle.cep.dataStructures.internal.ITimedTupleSinglyList;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;

public class TimedTupleSinglyList 
  extends TupleSinglyList
  implements ITimedTupleSinglyList
{
  public static final int TUPLE_POS = ELEM_POS;
  public static final int TS_POS = TUPLE_POS + 1;
  public static final int READERS_POS = TS_POS + 1;
  protected static final int MAX_QSINGLY_LIST1_POS = READERS_POS + 1;
  
  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_QSINGLY_LIST1_POS, 0);

    s_layoutDesc.setType(NEXT_POS, PageLayout.LONG);
    s_layoutDesc.setType(TUPLE_POS, PageLayout.LONG);
    s_layoutDesc.setType(TS_POS, PageLayout.LONG);
    s_layoutDesc.setType(READERS_POS, PageLayout.INT);
  }
  
  public TimedTupleSinglyList()
  {
    super();
  }
  
  protected NameSpace getNameSpace() 
  {
    return NameSpace.TTNODE;
  }
    
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
    
  public ListNode<ITuplePtr> createNode() {return new TimedTupleSinglyListNode(this);}
  
  public void add(ITuplePtr elem, long ts, int readers) throws ExecException
  {
    ITimedTupleSinglyListNode node4 = addElem(elem);
    node4.setTs(ts);
    node4.setReaders(readers);
  }

  public static class TimedTupleSinglyListNode
    extends TupleSinglyListNode
    implements ITimedTupleSinglyListNode
  {
    public TimedTupleSinglyListNode(List<ITuplePtr> list)
    {
      super(list);
    }
    
    public ITuplePtr getNodeElem() throws ExecException
    {
      long tupleId = m_page.lValueGet(m_index, TUPLE_POS);
      if (tupleId < 0)
        return null;
      return m_tupleFac.get(tupleId);
    }

    public void setNodeElem(ITuplePtr tuplePtr) throws ExecException
    {
      long tupleId = tuplePtr == null ? -1 : tuplePtr.getId();
      m_page.lValueSet(m_index, TUPLE_POS, tupleId);
    }

    protected void freeElem()
      throws ExecException
    {
    }
    
    public void setReaders(int n)
    {
    	m_page.iValueSet(m_index, READERS_POS, n);
    }
    
    public int decrementAndGet()  throws ExecException
    {
      int readers = m_page.iValueGet(m_index, READERS_POS);
      --readers;
      m_page.iValueSet(m_index, READERS_POS, readers);
      return readers;
    }
    
    public int incrementAndGet()  throws ExecException
    {
        int readers = m_page.iValueGet(m_index, READERS_POS);
        --readers;
        m_page.iValueSet(m_index, READERS_POS, readers);
        return readers;
    }

    public void setTs(long ts) throws ExecException
    {
      m_page.lValueSet(m_index, TS_POS, ts);
    }

    public long getTs() throws ExecException
    {
      long ts =m_page.lValueGet(m_index, TS_POS);
      return ts;
    }

    protected void getDesc(StringBuilder buf)
      throws ExecException
    {
      super.getDesc(buf);
      long ts = getTs();
      buf.append(",ts=");
      buf.append(ts);
      buf.append(",readers=");
      int readers = m_page.iValueGet(m_index, READERS_POS);
      buf.append(readers);
    }
  }
}

