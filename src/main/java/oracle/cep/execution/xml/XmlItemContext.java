/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XmlItemContext.java /main/2 2008/10/24 15:50:22 hopark Exp $ */

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
    parujain    05/12/08 - XMLItem Context
    parujain    05/12/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/XmlItemContext.java /main/2 2008/10/24 15:50:22 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.xml;

import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xquery.exec.OXQueryItem;

public class XmlItemContext implements IXmlContext 
{
  XmlManager m_xmlMgr;
  
  public XmlItemContext(XmlManager xmlMgr)
  {
    m_xmlMgr = xmlMgr;
  }
  
  public XMLItem createItem() {
    return new XMLItem(m_xmlMgr);
  }
  
  public OXMLItem createOXMLItem() {
	  return new OXQueryItem();
  }
  
  public long getId() {return -1;}
}
