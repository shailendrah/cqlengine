/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/PreparedXQuery.java /main/4 2009/02/25 14:23:51 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    parujain    05/12/08 - support multiple xmlcontext
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/PreparedXQuery.java /main/4 2009/02/25 14:23:51 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.xml;

import javax.xml.namespace.QName;

import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xml.xqxp.datamodel.OXMLSequence;

public class PreparedXQuery implements IXmlContext
{
  protected oracle.xquery.PreparedXQuery m_query;
  protected XmlManager m_xmlMgr;
  
  public PreparedXQuery(XmlManager xmlMgr)
  {
    m_xmlMgr = xmlMgr;
  }

  public PreparedXQuery(XmlManager xmlMgr, oracle.xquery.PreparedXQuery qry)
  {
    this(xmlMgr);
    m_query = qry;
  }

  public XMLItem createItem()
  {
    OXMLItem item = m_query.createItem();
    return new XMLItem(m_xmlMgr, item);
  }

  public OXMLItem createOXMLItem()
  {
	  return m_query.createItem();
  }
  
  public XMLSequence executeQuery(boolean printPlan)
  {
    OXMLSequence res = m_query.executeQuery(printPlan);
    return new XMLSequence(m_xmlMgr, res);
  }

  public void setContextItem(XMLItem item)
  {
    m_query.setContextItem(item.get());
  }

  public void setFloat(QName bindName, float val)
  {
    m_query.setFloat(bindName, val);
  }

  public void setInt(QName bindName, int val)
  {
    m_query.setInt(bindName, val);
  }

  public void setString(QName bindName, String val)
  {
    m_query.setString(bindName, val);
  }

  public void setBoolean(QName bindName, boolean val)
  {
    m_query.setBoolean(bindName, val);
  }

  public long getId() {return m_query.hashCode();}
}
