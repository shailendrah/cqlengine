  /* $Header: ListIter.java 28-dec-2007.11:23:01 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/28/07 - change iterator semantics
    hopark      12/11/07 - Creation
 */

/**
 *  @version $Header: ListIter.java 28-dec-2007.11:23:01 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.paged;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.IList;
import oracle.cep.dataStructures.internal.paged.List.ListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageManager;

/*
 * The list iterator should only hold on to the current node in order to avoid
 * accessing dangling node.
 * The reference to the list should also be cleared on eviction and release.
 */
public class ListIter<E> implements IEvictableObj
{
  protected ListNode<E> m_current;
  protected ListNode<E> m_temp;
  protected PageManager m_pm;
  protected List<E> m_list;    // it will be released from the first next invocation. 
  /**
   * Constructor
   * 
   */
  public ListIter()
  {
    super();
    m_current = null;
    m_temp = null;
    m_list = null;
  }

  @SuppressWarnings("unchecked")
  public void initialize(IList<E> list) throws ExecException
  {
    m_current = null;
    m_temp = null;
    if (list != null)
    {
      List<E> pglist = (List<E>) list;
      m_temp = pglist.createNode();
      m_pm = pglist.m_pm;
      pglist.addEvictableNode(this);
    }
    m_list = (List<E>) list;
  }

  public void release(IList<E> list) throws ExecException
  {
    if (m_current != null)
      m_current.clear();
    m_current = null;
    if (list != null)
    {
      List<E> pglist = (List<E>) list;
      pglist.removeEvictableNode(this);
    }
  }

  public boolean evict() throws ExecException
  {
    if (m_current != null)
      m_current.evict();
    if (m_temp != null)
      m_temp.evict();
    return true;
  }
  
  /**
   * Resets the current node
   */
  public void resetCurrent() throws ExecException
  {
    assert (m_current != null && !m_current.isNull());

    m_current.pin(m_pm, IPinnable.READ);
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
        m_current.copy(m_list.m_head);
        m_list = null;
      }
      else
      {
        m_current.getNext(m_current);
      }
      if (m_current != null && !m_current.isNull())
      {
        return (E) m_current.getNodeElem();
      }
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }

  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append("current=");
    b.append((m_current == null) ? "null":m_current.toString());
    return b.toString();
  }
}  
