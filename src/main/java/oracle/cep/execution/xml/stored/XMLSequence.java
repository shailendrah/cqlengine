/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/stored/XMLSequence.java /main/3 2008/10/24 15:50:22 hopark Exp $ */

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
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/stored/XMLSequence.java /main/3 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.xml.stored;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.execution.xml.XmlManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IManagedObj;
import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xml.xqxp.datamodel.OXMLSequence;
import oracle.xquery.XQMesg;
import oracle.xquery.exec.XQueryUtils;

public class XMLSequence   
  extends oracle.cep.execution.xml.XMLSequence
  implements Externalizable, IManagedObj
{
  protected long m_id;
  protected List<XMLItem> m_items;
  protected int m_pos;
  
  public XMLSequence(XmlManager xmlMgr)
  {
    super(xmlMgr);
  }
  
  public XMLSequence(XmlManager xmlMgr, PreparedXQuery qry, OXMLSequence seq)
  {
    super(xmlMgr);
    m_id = xmlMgr.getNextXMObjID();

    m_items = new LinkedList<XMLItem>();
    while (seq.next())
    {
      OXMLItem item = seq.getItem();
      XMLItem i = new XMLItem(xmlMgr, qry, item, true);
      m_items.add(i);
      i.addRef();
    }
    //note that we don't have a reference to seq.
    //Otherwise, OXMLQueryItem cannot be evicted.
    m_pos = -1;;
  }
  
  public XMLItem getItem()
  {
    assert (m_pos >= 0) :"next() should be invoked first";
    if (m_pos >= m_items.size())
      return null;
    XMLItem res = m_items.get(m_pos);
    res.addRef();
    return res;
  }

  public boolean next()
  {
    m_pos++;
    return (m_pos < m_items.size());
  }

  public void printResult(PrintWriter out, XQMesg msg) 
    throws java.lang.Exception
  {
    XMLItem item = getItem();
    XQueryUtils.printResult(item.get(), out, msg);
  }
  
  public long getId() {return m_id;}
  
  public void free(IAllocator<?> fac)
  {
    for (XMLItem i : m_items)
    {
      i.free(fac);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput in) throws IOException,
    ClassNotFoundException
  {
      m_id = in.readLong();
      m_pos = in.readInt();
      long[] items = (long[])in.readObject();
      m_items = new LinkedList<XMLItem>();
      if (items != null)
      {
        for (long id : items)
        {
          XMLItem item = (XMLItem) m_xmlMgr.getXMLObj(id);
          assert (item != null);
          m_items.add(item);
        }
      }
  }
  
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(m_id);
    out.writeInt(m_pos);
    long[] items = null;
    if (m_items.size() > 0)
    {
      items = new long[m_items.size()];
      int pos = 0;
      for (XMLItem i : m_items)
      {
        items[pos++] = i.getId();
        i.evict();
      }
    }
    out.writeObject(items);
    m_items.clear();
  }
}