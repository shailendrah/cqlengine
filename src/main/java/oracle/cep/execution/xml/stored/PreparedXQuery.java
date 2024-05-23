/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/stored/PreparedXQuery.java /main/3 2008/10/24 15:50:22 hopark Exp $ */

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
    parujain    05/15/08 - use context
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/stored/PreparedXQuery.java /main/3 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.xml.stored;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

import oracle.cep.execution.xml.XmlManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IManagedObj;
import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xml.xqxp.datamodel.OXMLSequence;

public class PreparedXQuery 
  extends oracle.cep.execution.xml.PreparedXQuery
  implements IManagedObj
{
  protected long m_id;
  
  public PreparedXQuery(XmlManager xmlMgr)
  {
    super(xmlMgr);
  }
  
  public PreparedXQuery(XmlManager xmlMgr, oracle.xquery.PreparedXQuery qry)
  {
    super(xmlMgr, qry);
    m_id = xmlMgr.getNextXMObjID();
    xmlMgr.putXMLObj(m_id, this);
  }
  
  public OXMLItem createOXMLItem()
  {
    return m_query.createItem();
  }
  
  public XMLItem createItem()
  {
    OXMLItem item = m_query.createItem();
    return new XMLItem(m_xmlMgr, this, item, false);
  }

  public XMLSequence executeQuery(boolean printPlan)
  {
    OXMLSequence res = m_query.executeQuery(printPlan);
    return new XMLSequence(m_xmlMgr, this, res);
  }

  public long getId() {return m_id;}
  
  public void free(IAllocator<?> fac)
  {
    m_xmlMgr.freeXMLObj(m_id, false);
  }

  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    m_id = in.readLong();
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(m_id);
  }

  /*
   * ManageXMLObj is not directly serialized into the bdb.
   * We writes only the id and get the ManageXMLObj from the map upon reading.
   */
  public Object readResolve() throws ObjectStreamException
  {
    return (Object) m_xmlMgr.getXMLObj(m_id);
  }

}