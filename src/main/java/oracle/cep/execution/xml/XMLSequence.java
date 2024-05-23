/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XMLSequence.java /main/2 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XMLSequence.java /main/2 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.xml;

import java.io.PrintWriter;

import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xml.xqxp.datamodel.OXMLSequence;
import oracle.xquery.XQMesg;
import oracle.xquery.exec.XQueryUtils;

public class XMLSequence
{
  protected OXMLSequence m_seq;
  protected XmlManager m_xmlMgr;
  
  public XMLSequence(XmlManager xmlMgr)
  {
    m_xmlMgr = xmlMgr;
  }
  
  public XMLSequence(XmlManager xmlMgr, OXMLSequence seq)
  {
    this(xmlMgr);
    m_seq = seq;
  }
  
  public XMLItem getItem()
  {
    OXMLItem item = m_seq.getItem();
    XMLItem res = new XMLItem(m_xmlMgr, item);
    return res;
  }

  public boolean next()
  {
    return m_seq.next();
  }

  public void printResult(PrintWriter out, XQMesg msg) 
    throws java.lang.Exception
  {
    OXMLItem item = m_seq.getItem();
    XQueryUtils.printResult(item, out, msg);
  }

  public long getId() {return m_seq.hashCode();}
}
