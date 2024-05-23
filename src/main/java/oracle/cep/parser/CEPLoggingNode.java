/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPLoggingNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    parujain    08/11/08 - error offset
    hopark      06/18/08 - logging refactor
    hopark      08/01/07 - add dump
    hopark      07/03/07 - pass id/name in a same list
    hopark      06/07/07 - use LogArea
    hopark      05/23/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPLoggingNode.java /main/6 2008/08/25 19:27:24 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.parser;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;

/**
 * Parse tree node corresponding to system DDL
 *
 * @since 1.0
 */

public class CEPLoggingNode implements CEPParseTreeNode 
{
  public static final int DISABLE = 0;
  public static final int ENABLE = 1;
  public static final int CLEAR = 2;
  public static final int DUMP = 3;
  
  protected ILogArea m_area;
  protected List<Integer> m_levels;
  protected List<Integer> m_types;
  protected List<String> m_names;
  protected List<Integer> m_ids;
  protected List<ILogEvent> m_events;
  protected int m_cmd;
  
  protected int startOffset;
  
  protected int endOffset;
  
  public CEPLoggingNode(int cmd,  ILogArea area, 
                 List<Integer> types, 
                 List<String> names, 
                 List<ILogEvent> events, List<Integer> levels)
  {
    m_area = area;
    m_levels = levels;
    m_types = types;
    m_names = names;
    m_events = events;
    m_cmd = cmd;
    m_ids = null;
    if (names != null)
    {
      m_ids = new LinkedList<Integer>();
      for (String name : names)
      {
        try {
            int n = Integer.parseInt(name);
            m_ids.add(n);
        } catch(NumberFormatException e)
        {
          // eats up the exception.
        }
      }
    }
  }
  
  public ILogArea getArea()
  {
    return m_area;
  }
  
  public void setArea(ILogArea area)
  {
    m_area = area;
  }

  public List<Integer> getLevels()
  {
    return m_levels;
  }
  
  public List<Integer> getTypes()
  {
    return m_types;
  }
  
  public List<ILogEvent> getEvents()
  {
    return m_events;
  }

  public List<String> getNames()
  {
    return m_names;
  }
  
  public List<Integer> getIds()
  {
    return m_ids;
  }
  
  public void setIds(List<Integer> ids)
  {
    m_ids = ids;
  }
  
  public int getCmd() 
  {
    return m_cmd;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

}
