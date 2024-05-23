/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/dumper/StrDumper.java /main/2 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      02/05/08 - add indentation
    hopark      12/26/07 - add xml support
    hopark      11/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/dumper/StrDumper.java /main/2 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.dumper;

import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogLevelManager;

public class StrDumper implements IDumpContext
{
  ILogLevelManager m_logLevelManager;
  StringBuilder m_buf;
  ILogArea m_logArea;
  int     m_logLevel;
  boolean m_verbose;
  int     m_level;
  int     m_indent;
  boolean m_void;
  
  static String[] m_indents = {" ", "  ", "   ", "    ", "     ", "      ", "       ",
                               "        ", "         ", "          ", "           ",};
  
  public StrDumper(ILogLevelManager lm)
  {
    m_logLevelManager = lm;
    m_buf = new StringBuilder();
    m_level = 0;
  }
  
  public void close()
  {
  }

  public void setVoid()
  {
    m_void = true;
  }

  public boolean isVoid()
  {
    return m_void;
  }
  
  public void setLevel(ILogArea area, int level, boolean verbose)
  {
    m_logArea = area;
    m_logLevel = level;
    m_verbose = verbose;
  }
  
  public ILogLevelManager getLogLevelManager()
  {
    return m_logLevelManager;
  }
  
  public IDumpContext openDumper(String dumpKey)
  {
    return m_logLevelManager.openDumper(dumpKey, this);
  }
  
  public void closeDumper(String dumpKey, IDumpContext prev)
  {
    m_logLevelManager.closeDumper(dumpKey, prev, this);
  }
  
  public ILogArea getArea() {return m_logArea;}
  public int getLevel() {return m_logLevel;}
  public boolean isVerbose() {return m_verbose;}
  
  protected void indent()
  {
    if (m_indent == 0)
    {
      m_indent = (m_level < m_indents.length) ? m_level : m_indents.length-1;   
      if (m_indent >= 0)
        m_buf.append(m_indents[m_indent]);   
    }
  }
  
  public void beginTag(String tag, String[] attribs, Object[] vals)
  {
    if (m_level > 0)
    {
      m_buf.append("\n");
      m_indent = 0;
      indent();
    }
    m_level++;
    m_buf.append(" ");
    m_buf.append(tag);
    m_buf.append("(");
    if (attribs != null && vals != null)
    {
      assert (attribs.length == vals.length);
      for (int i = 0; i < attribs.length; i++)
      {
        if (i > 0) m_buf.append(",");
        String name = attribs[i];
        Object v = vals[i];
        m_buf.append(name);
        m_buf.append("=");
        m_buf.append(v == null ? "null" : v.toString());
      }
    }
    m_buf.append(")");
  }
  
  public void endTag(String tag)
  {
    m_level--;
    indent();
    m_buf.append(" /");
    m_buf.append(tag);
    m_buf.append("\n");
    m_indent = 0;
  }

  public String toString()
  {
    return m_buf.toString();
  }
  
  public void write(Object v)
  {
    indent();
    m_buf.append(v == null ? "null" : v.toString());
  }

  public void writeln(Object v)
  {
    indent();
    m_buf.append(" ");
    m_buf.append(v == null ? "null" : v.toString());
  }

  public void writeln(String name, Object v)
  {
    indent();
    m_buf.append(" ");
    m_buf.append(name);
    m_buf.append("=");
    m_buf.append(v == null ? "null" : v.toString());
  }
}

