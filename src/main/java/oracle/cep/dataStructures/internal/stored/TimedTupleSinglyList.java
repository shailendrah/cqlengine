/* $Header: TimedTupleSinglyList.java 06-mar-2008.10:30:47 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
   TimedTupleSinglyList

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/25/08 - Clear page reference on unpin
    hopark      12/27/07 - support xmllog
    hopark      11/03/07 - Creation
 */

/**
 *  @version $Header: TimedTupleSinglyList.java 06-mar-2008.10:30:47 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.stored;

import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyList;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;

@DumpDesc(tag="TTSList", 
    attribTags={"Name", "Id", "Length"}, 
    attribVals={"getName", "getId", "getSize"})
public class TimedTupleSinglyList
  extends TupleSinglyList
  implements ITimedTupleSinglyList
{
  public TimedTupleSinglyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.TTNODE;
  }
    
  protected LayoutDesc getPageLayoutDesc() 
  {
    return oracle.cep.dataStructures.internal.paged.TimedTupleSinglyList.s_layoutDesc;
  }
    
  public ListNode<ITuplePtr> createNode() 
  {
    return new TimedTupleSinglyListNode(this);
  }

  public void  add(ITuplePtr tuple, long ts, int readers) 
    throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    ITimedTupleSinglyListNode node4 = addElem(tuple);
    node4.setTs(ts);
    node4.setReaders(readers);
  }


  public static class TimedTupleSinglyListNode 
    extends oracle.cep.dataStructures.internal.paged.TimedTupleSinglyList.TimedTupleSinglyListNode 
  {
    public TimedTupleSinglyListNode(oracle.cep.dataStructures.internal.paged.List<ITuplePtr> list)
    {
      super(list);
    }

    @SuppressWarnings("unchecked")
    public <T extends IPinnable<IListNode<ITuplePtr>>> T pin(int mode) throws ExecException 
    {
      //need to invoke pin to set the dirty flag correctly
      m_page = pin(m_pm, mode);
      return (T) this;
    }
    
    public void unpin() throws ExecException 
    {
      m_page = null;
    }
   }
}
