/* $Header: TupleSinglyList.java 03-mar-2008.13:31:48 hopark Exp $ */

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
 *  @version $Header: TupleSinglyList.java 03-mar-2008.13:31:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.ITupleSinglyList;
import oracle.cep.dataStructures.internal.ITupleSinglyListIter;
import oracle.cep.dataStructures.internal.ITupleSinglyListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.util.DebugUtil;

public class TupleSinglyList 
  extends SinglyList<ITuplePtr>
  implements ITupleSinglyList
{
  /**
   * Empty Constructor
   * 
   */
  public TupleSinglyList()
  {
    super();
  }

  public void setTupleFactory(IAllocator<ITuplePtr> fac) {}
  public IAllocator<ITuplePtr> getTupleFactory() {return null;}
  
  public static class TupleSinglyListNode 
    extends SinglyList.SinglyListNode<ITuplePtr>
    implements ITupleSinglyListNode
  {
    public TupleSinglyListNode()
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
  }
  
  public static class TupleSinglyListIter 
    extends SinglyList.SinglyListIter<ITuplePtr>
    implements ITupleSinglyListIter 
  {
  }
}
