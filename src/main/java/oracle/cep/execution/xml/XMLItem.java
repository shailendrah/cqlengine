/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XMLItem.java /main/3 2008/10/24 15:50:22 hopark Exp $ */

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
    parujain    05/12/08 - getNode api
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XMLItem.java /main/3 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.xml;

import java.io.PrintWriter;
import java.io.StringWriter;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLNode;
import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xquery.XQMesg;
import oracle.xquery.exec.XQueryUtils;
import oracle.xquery.exec.OXQueryItem;

public class XMLItem
{
  protected OXMLItem m_item;
  protected XmlManager m_xmlMgr;
  
  public XMLItem(XmlManager xmlMgr)
  {
    m_xmlMgr = xmlMgr;
    m_item = new OXQueryItem();
  }

  public XMLItem(XmlManager xmlMgr, OXMLItem item)
  {
    m_xmlMgr = xmlMgr;
    m_item = item;
  }
  
  public OXMLItem get() {return m_item;}
  
  public void setNode(XMLNode doc)
  {
    m_item.setNode(doc);
  }
  
  public void setNode(XMLDocument doc)
  {
    m_item.setNode(doc);
  }
  
  public XMLNode getNode()
  {
    return m_item.getNode();
  }
  
  public long getId() {return m_item.hashCode();}
  
  public static String XMLNodetoString(XMLNode node)
  {
    OXQueryItem item = new OXQueryItem();
    item.setNode(node);
    return OXMLItemToString(item);
  }
  
  private static String OXMLItemToString(OXMLItem item)
  {
    try
    {
      StringWriter wr = new StringWriter();
      PrintWriter wrp = new PrintWriter(wr);
      XQueryUtils.printResult(item, wrp, XQMesg.newInstance(null));
      wrp.flush();
      wr.flush();
      return wr.toString();
    } 
    catch(Exception e)
    {
      //eats up..
    }
    return null;
  }
  
  public String toString()
  {
    return OXMLItemToString(m_item);
  }
  
  public XMLItem clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }
  
  public void dump() {}
}