/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/stored/Partition.java /main/7 2012/06/20 05:24:30 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   Partition is the stored version of IPartition implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
  pkali       05/30/12 - added windows size data member
  hopark      02/28/08 - tupleptr serialization optimization
  hopark      12/27/07 - support xmllog
  hopark      12/27/07 - support xmllog
    hopark      11/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/stored/Partition.java /main/7 2012/06/20 05:24:30 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.stored;

import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.IPartitionIter;
import oracle.cep.dataStructures.internal.IPartitionNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;

@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class Partition 
  extends TupleDoublyList
  implements IPartition
{
  protected static final int TUPLE_POS = oracle.cep.dataStructures.internal.paged.Partition.TUPLE_POS;
  protected static final int TS_POS = oracle.cep.dataStructures.internal.paged.Partition.TS_POS;

  /** Maintains the partition window size.
   * The list size may not represent the correct window size 
   * in the scenario of memory optimization where tuple is 
   * not added to the list, hence a separate counter is maintained
   */
  private int windowSize;
  
  public void incrementWindowSize()
  {
    ++windowSize;
  }
  
  public void decrementWindowSize()
  {
    --windowSize;
  }
  
  public int getWindowSize()
  {
    return windowSize;
  }
  
  public Partition()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.PARTNNODE;
  }
  
  protected LayoutDesc getPageLayoutDesc() 
  {
    return oracle.cep.dataStructures.internal.paged.Partition.s_layoutDesc;
  }

  public ListNode<ITuplePtr> createNode() {return new PartitionNode(this);}

  @SuppressWarnings("unchecked")
  public <T extends IPartitionNode> T  add(ITuplePtr tuple, long ts) throws ExecException
  {
    IPartitionNode node4 = add(tuple);
    node4.setTs(ts);
    return (T) node4;
  }

  /**
   * It is important to extend stored.DoublyList.DoublyListNode.
   * It cannot extend memory.Partition.PartitionNode.
   * If we do, we will get ClassCastException in DoublyList.
   * It's because stored.DoublyList extends stored.DoublyList
   * an stored.ToublyList is expecting stored.DoublyList.DoublyListNode.
   */
  public static class PartitionNode 
    extends TupleDoublyListNode
    implements IPartitionNode
  {
    public PartitionNode(oracle.cep.dataStructures.internal.paged.List<ITuplePtr> list)
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
    
    public void setTs(long ts) throws ExecException
    {
      m_page.lValueSet(m_index, TS_POS, ts);
    }

    public long getTs() throws ExecException
    {
      long ts = m_page.lValueGet(m_index, TS_POS);
      return ts;
    }
   }
  
  public static class PartitionIter 
  extends DoublyList.DoublyListIter<ITuplePtr>
  implements IPartitionIter
{
    @SuppressWarnings("unchecked")
    public long getTs() throws ExecException
    {
      assert (m_current != null);
      m_current.pin(IPinnable.READ);
      PartitionNode node = (PartitionNode) m_current;
      return node.getTs();
    }
}
}
