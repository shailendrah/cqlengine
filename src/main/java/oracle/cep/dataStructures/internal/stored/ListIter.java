/* $Header: ListIter.java 28-dec-2007.11:33:04 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 List is the stored version of IDoublyList implementation.

 PRIVATE CLASSES

 NOTES

 MODIFIED    (MM/DD/YY)
  hopark      12/28/07 - change iterator semantics
  hopark      12/07/07 - cleanup spill
  hopark      11/03/07 - remove getNodeStr
  hopark      10/30/07 - Creation
 */
package oracle.cep.dataStructures.internal.stored;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.paged.List.ListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;

/**
 * @version $Header: ListIter.java 28-dec-2007.11:33:04 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
public class ListIter<E> 
  extends oracle.cep.dataStructures.internal.paged.ListIter<E>
{
  public ListIter()
  {
    super();
  }

  /**
   * Gets the next element in the list
   * 
   * @return Next Node element to be read.
   */
  @SuppressWarnings("unchecked")
  public E next()
  {
    try 
    {
      if (m_current == null)
      {
        m_current = m_list.createNode();
        m_current.copy((ListNode<E>) m_list.getHeadPtr());
        m_list = null;
      }
      else
      {
        m_current.getNext(m_current);
      }
      if (m_current != null && !m_current.isNull())
      {
        m_current.pin(IPinnable.READ);
        return (E) m_current.getNodeElem();
      }
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }
}
