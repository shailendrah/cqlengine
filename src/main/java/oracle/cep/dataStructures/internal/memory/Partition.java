/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/Partition.java /main/6 2012/06/20 05:24:30 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   QSinglyList is the memory version of ISinglyList4 implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       05/29/12 - added window size data member
    hopark      12/27/07 - support xmllog
    hopark      11/07/07 - change add
    hopark      11/03/07 - remove getNodeStr
    hopark      12/18/07 - change iterator semantics
    hopark      10/30/07 - remove IQueueElement
    hopark      10/18/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/Partition.java /main/6 2012/06/20 05:24:30 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.IPartitionIter;
import oracle.cep.dataStructures.internal.IPartitionNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;

@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class Partition extends TupleDoublyList
  implements IPartition
{
  /** Maintains the partition window size.
   * The list size may not represent the correct window size 
   * in the scenario of memory optimization where tuple is 
   * not added to the list, hence a separate counter is maintained
   */
  private int windowSize;
  
  /**
   * Empty Constructor
   * 
   */
  public Partition()
  {
    super();
  }
  
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
  
  @SuppressWarnings("unchecked")
  public <T extends IPartitionNode> T  add(ITuplePtr elem, long ts) throws ExecException
  {
    IPartitionNode node4 = add(elem);
    node4.setTs(ts);
    return (T) node4;
  }

  @DumpDesc(attribTags={"Id", "Prev", "Next"}, 
            attribVals={"getId", "getPrevId", "getNextId"},
            valueTags={"Tuple", "ts"},
            values={"getNodeElem", "getTs"})
  public static class PartitionNode 
    extends TupleDoublyListNode
    implements IPartitionNode
  {
    long ts;
    
    public PartitionNode()
    {
      super();
      ts = 0;
    }
  
    /**
     * Clears the node
     */
    public void clear()
    {
      super.clear();
      ts = 0;
    }

    public void setTs(long ts) throws ExecException
    {
      this.ts = ts;
    }

    /* (non-Javadoc)
     * @see oracle.cep.dataStructures.internal.IQueueElement#getTs()
     */
    public long getTs() throws ExecException
    {
      return ts;
    }

    public String toString()
    {
      String tupleDesc = NodeElem == null ? "null" : NodeElem.toString();
      return super.toString() + " tuple=" + tupleDesc + " ts=" + ts;
    }
  }

  public static class PartitionIter 
    extends DoublyList.DoublyListIter<ITuplePtr>
    implements IPartitionIter
  {
      @SuppressWarnings("unchecked")
      public long getTs() throws ExecException
      {
        assert (current != null);
        PartitionNode node = (PartitionNode) current;
        return node.getTs();
      }
  }
}

