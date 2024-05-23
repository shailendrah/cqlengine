/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/dumper/XMLDumper.java /main/2 2008/10/24 15:50:22 hopark Exp $ */

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
    hopark      11/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/dumper/XMLDumper.java /main/2 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.dumper;

import oracle.cep.logging.ILogLevelManager;

public class XMLDumper extends StrDumper
{
  public XMLDumper(ILogLevelManager lm)
  {
    super(lm);
  }
  
  public void close()
  {
  }

  protected void indent()
  {
    if (m_level >= 0)
    {
      m_indent = (m_level < m_indents.length) ? m_level : m_indents.length-1;   
      m_buf.append(m_indents[m_indent]);   
    }
  }
  
  public void beginTag(String tag, String[] attribs, Object[] vals)
  {
    indent();
    m_level++;
    m_buf.append("<");
    m_buf.append(tag);
    if (attribs != null && vals != null)
    {
      assert (attribs.length == vals.length);

      for (int i = 0; i < attribs.length; i++)
      {
        String name = attribs[i];
        Object v = vals[i];
        m_buf.append(" ");
        m_buf.append(name);
        m_buf.append("=\"");
        m_buf.append(v == null ? "null" : v.toString());
        m_buf.append("\"");
      }
    }
    m_buf.append(">\n");
  }
  
  public void endTag(String tag)
  {
    m_level--;
    indent();
    m_buf.append("</");
    m_buf.append(tag);
    m_buf.append(">");
    m_buf.append("\n");
    
  }

  
  public void writeln(String tag, Object v)
  {
    indent();
    m_buf.append("<");
    m_buf.append(tag);
    m_buf.append(">");
    m_buf.append(v == null ? "null" : v.toString());
    m_buf.append("</");
    m_buf.append(tag);
    m_buf.append(">");
    m_buf.append("\n");
  }
}

