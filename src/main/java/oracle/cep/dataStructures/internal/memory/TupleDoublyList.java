/* $Header: TupleDoublyList.java 03-mar-2008.13:31:48 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/07/08 - Creation
 */

/**
 *  @version $Header: TupleDoublyList.java 03-mar-2008.13:31:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.util.DebugUtil;

public class TupleDoublyList 
  extends DoublyList<ITuplePtr>
  implements ITupleDoublyList
{
  /**
   * Empty Constructor
   * 
   */
  public TupleDoublyList()
  {
    super();
  }

  public void setTupleFactory(IAllocator<ITuplePtr> fac) {}
  public IAllocator<ITuplePtr> getTupleFactory() {return null;}
  
  public static class TupleDoublyListNode 
    extends DoublyList.DoublyListNode<ITuplePtr>
    implements ITupleDoublyListNode, Externalizable
  {
    /**
     * 
     */
    private static final long serialVersionUID = -4899696039817346610L;

    public TupleDoublyListNode()
    {
      super();
    }

    public ITuplePtr getNodeElem()
    {
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
        try 
        {
          if (NodeElem != null)
            NodeElem.pinTuple(IPinnable.READ);
        }
        catch (ExecException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        }
      }
      return NodeElem;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
    }
  }
  
  public static class TupleDoublyListIter 
    extends DoublyList.DoublyListIter<ITuplePtr>
    implements ITupleDoublyListIter 
  {
  }
}
