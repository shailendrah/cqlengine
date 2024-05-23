/* $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/stored/XMLItem.java /main/3 2008/10/24 15:50:22 hopark Exp $ */

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
    hopark      05/15/08 - 
    parujain    05/15/08 - use context
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/xml/stored/XMLItem.java /main/3 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.xml.stored;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.cep.execution.xml.IXmlContext;
import oracle.cep.execution.xml.XmlManager;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IManagedObj;
import oracle.xml.parser.schema.XSDTypeConstants;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLNode;
import oracle.xml.xqxp.datamodel.OXMLItem;
import oracle.xml.xqxp.datamodel.OXMLSequenceType;
import oracle.xml.xqxp.datamodel.XMLItemConstants;
import oracle.xquery.XQMesg;
import oracle.xquery.exec.XQueryUtils;

public class XMLItem 
  extends oracle.cep.execution.xml.XMLItem
  implements IManagedObj, Externalizable
{
  protected long m_id;
  protected boolean m_stored;
  protected long m_queryId;     //need query to re-create OXMLItem
  protected int m_itemType;
  protected int m_docId;
  protected int m_docOrderId;
  protected int m_refCount;     //doc refcount
  
  private static final boolean TEST = false;
  
  public XMLItem(XmlManager xmlMgr)
  {
    super(xmlMgr);
  }
  
  public XMLItem(XmlManager xmlMgr, IXmlContext context, OXMLItem item, boolean itemReady)
  {
    super(xmlMgr, item);

    m_queryId = context.getId();
    
    m_item = item;
    if (itemReady)
      m_itemType = item.getPrimitiveType();

    m_id = xmlMgr.getNextXMObjID();
    xmlMgr.putXMLObj(m_id, this);

    m_docOrderId = -1;
    m_docId = -1;
    m_refCount = 0;
    
    setInfo();
  }
  
  public void addRef()
  {
    m_refCount++;
  }
  
  public int release()
  {
    return --m_refCount;
  }
  
  public OXMLItem get() 
  {
    if (m_item == null)
    {
      XMLNode node = null;
      if (m_docOrderId < 0)
      {
        // node that's not part of the document
        // just get it from the XmlManager map.
        node = (XMLNode) m_xmlMgr.readXMLObj(m_id);
      }
      else
      {
        // find a node from the document
        XMLItem docItem = (XMLItem) m_xmlMgr.getDocument(m_docId);
        FindNodeCtx ctx = new FindNodeCtx(m_docOrderId);
        boolean r = docItem.iterateXMLNodes(ctx);
        assert (r);
        node = ctx.m_result;
      }
      if (m_queryId >= 0)
      {
        // the xmlitem was created through preparedxquery
        PreparedXQuery xq = (PreparedXQuery) m_xmlMgr.getXMLObj(m_queryId);
        m_item = xq.createOXMLItem();
      }
      else
      {
        // the xmlitem was created through XmlItemContext
        IXmlContext ctx = m_xmlMgr.createContext();
        m_item = ctx.createOXMLItem();
      }
      m_item.setNode(node);
    }
    return m_item;
  }
  
  public void setNode(XMLDocument doc)
  {
    OXMLItem item = get();
    assert (item != null);
    item.setNode(doc);
    m_itemType = item.getPrimitiveType();

    setInfo();
  }
  
  private void setInfo()
  {
    // we already know where this node belongs to
    if (m_docOrderId >= 0)
      return;

    Node n = m_item.getNode();
    if (n == null)
    {
      return;
    }
    
    if (TEST) dump();
    Document doc = n.getOwnerDocument();
    XMLNode onode = (XMLNode) n;
    int docOrderId = onode.getDocOrderId();
    if (doc == null || docOrderId == 0)
    {
      // the node is not part of document (primitive or docuement itself)
      assert (docOrderId == 0);
      m_docOrderId = -1;
      if (m_itemType == XMLItemConstants.NODEVALUE)
      {
        // new document
        m_docId = onode.hashCode();
        m_xmlMgr.putDocument(m_docId, this);
      }
    } else {
      // this node is part of document
      assert (n instanceof Serializable);
      m_docId = doc.hashCode();
      assert (doc instanceof Serializable);
      
      XMLItem docnode = m_xmlMgr.getDocument(m_docId);
      // document should be already registered..
      assert (docnode != null);
      
      // find the document order id in the document.
      OXMLItem item = get();
      XMLNode target = (XMLNode) item.getNode();
      GetOrderIdCtx ctx = new GetOrderIdCtx(target);
      boolean r = docnode.iterateXMLNodes(ctx);
      assert (r);
      m_docOrderId = ctx.m_resultOrderId;
      docnode.addRef();
    }
  }
  
  public XMLItem clone()  throws CloneNotSupportedException
  {
    XMLItem item = new XMLItem(m_xmlMgr);
    item.m_queryId = m_queryId;
    item.m_id = m_id;
    item.m_item = null;
    if (m_item != null)
    {
      String istr = toString();
      item.fromString(istr);
    }
    return item;
  }
  
  public boolean equals(Object o)
  {
    if (o == null) return false;
    XMLItem other = (XMLItem) o;
    if (m_id != other.m_id) return false;
    if (m_queryId != other.m_queryId) return false;
    String istr = toString();
    String ostr = other.toString();
    return istr.equals(ostr);
  }
  
  public boolean evict()
  {
    if (m_item == null) return false;
    try
    {
      // only write the document
      if (m_docOrderId < 0)
      {
        // node is not part of document
        // write it through XmlManager map.
        Node n = m_item.getNode();
        if (!m_xmlMgr.writeXMLObj(m_id, n))
          return false;
        m_stored = true;
      }
      else
      {
        /* TODO Evict the parent doc.
         * Keep it for now as there is a problem rebuilding the doc structure..
         * */
        XMLItem docnode = m_xmlMgr.getDocument(m_docId);
        assert (docnode != null);
        docnode.evict();
        
      }
      m_item = null;
    }
    catch(Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return false;
    }
    return true;
  }
  
  public long getId() {return m_id;}
  
  public void free(IAllocator<?> fac)
  {
    if (release() == 0)
    {
      XMLItem docnode = m_xmlMgr.getDocument(m_docId);
      assert (docnode != null);
      if (docnode.release() == 0)
      {
        m_xmlMgr.removeDocument(m_docId);
      }
    }
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(m_id);
    evict();
  }

  public void readExternal(ObjectInput in) throws IOException,
    ClassNotFoundException
  {
    m_id = in.readLong();
  }
  
  /*
   * This method allows us to keep using the same XMLItem object for the same id.
   * The new object created by the serialization is replaced with the one in the hashmap.
   */
  public Object readResolve() throws ObjectStreamException
  {
    return (XMLItem) m_xmlMgr.getXMLObj(m_id);
  }

  public void fromString(String itemStr)
  {
    try
    {
      PreparedXQuery xq = (PreparedXQuery) m_xmlMgr.getXMLObj(m_queryId);
      m_item = xq.createOXMLItem();
      switch (m_itemType)
      {
      case XSDTypeConstants.iANY_SIMPLE:
      case XSDTypeConstants.iSTRING:
        m_item.setString(OXMLSequenceType.TSTRING, itemStr);
        break;
      case XSDTypeConstants.iDECIMAL:
      {
        m_item.setString(OXMLSequenceType.TSTRING, itemStr);
        m_item.convert(OXMLSequenceType.TINTEGER);
        break;
      }
      case XSDTypeConstants.iBOOLEAN:
      {
        m_item.setString(OXMLSequenceType.TSTRING, itemStr);
        m_item.convert(OXMLSequenceType.TBOOLEAN);
        break;
      }
      case XSDTypeConstants.iFLOAT:
      {
        m_item.setString(OXMLSequenceType.TSTRING, itemStr);
        m_item.convert(OXMLSequenceType.TFLOAT);
        break;
      }
      case XSDTypeConstants.iDOUBLE:
      {
        m_item.setString(OXMLSequenceType.TSTRING, itemStr);
        m_item.convert(OXMLSequenceType.TDOUBLE);
        break;
      }
      case XMLItemConstants.NODEVALUE:
      {
        DOMParser dom = new DOMParser();
        Reader reader = new StringReader(itemStr);
        dom.parse(reader);
        m_item.setNode(dom.getDocument());
        break;
      }
      default:
        assert false : "add primitive types";
        break;
      }
    }
    catch(Exception e)
    {
      m_item.setString(OXMLSequenceType.TSTRING, itemStr);
    }
  }
  
  public String toString()
  {
    OXMLItem item = get();
    assert (item != null);
    try
    {
      if (item.isNode())
      {
        StringWriter wr = new StringWriter();
        PrintWriter wrp = new PrintWriter(wr);
        XQueryUtils.printResult(item, wrp, XQMesg.newInstance(null));
        wrp.flush();
        wr.flush();
        return wr.toString();
      }
      else
      {
        switch(item.getPrimitiveType())
        {
        case XSDTypeConstants.iDECIMAL:
          return Integer.toString(item.getInt());
        case XSDTypeConstants.iBOOLEAN:
          return Boolean.toString(item.getBoolean());
        case XSDTypeConstants.iFLOAT:
          return Float.toString(item.getFloat());
        case XSDTypeConstants.iDOUBLE:
          return Double.toString(item.getDouble());
        case XSDTypeConstants.iANY_SIMPLE:
        case XSDTypeConstants.iSTRING:
          return item.getString();
        default:
          assert false : "add new primitive type";
        }
      }
    } 
    catch(Exception e)
    {
      //eats up..
    }
    return null;
  }
  
  public String getNodeStr(Node node)
  {
    StringBuilder b = new StringBuilder();
    try
    {
      XMLNode onode = (XMLNode) node;
      int id = onode.getDocOrderId();
      b.append("id=");
      b.append(id);
      b.append(" ");
      b.append("hash=");
      b.append(node.hashCode());
      b.append(" ");
      
      b.append(node.getNodeName());
      b.append("=");
      b.append(node.getNodeValue());
      b.append(" ");
      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null)
      {
      // Get number of attributes in the element
      int numAttrs = attrs.getLength();
      
      // Process each attribute
      for (int i=0; i<numAttrs; i++) {
          Attr attr = (Attr)attrs.item(i);
      
          // Get attribute name and value
          String attrName = attr.getNodeName();
          String attrValue = attr.getNodeValue();
          b.append(attrName);
          b.append("=");
          b.append(attrValue);
          b.append(" ");
      }
      }
    }
    catch(Exception e)
    {
      
    }
    return b.toString();
  }
  
  private void dump0(Node node, int level)
  {
    // Process node
    XMLNode onode = (XMLNode) node;
    //if (onode.getNodeType() == Node.ELEMENT_NODE)
    //{
      System.out.println(getNodeStr(node));
    //}
    // If there are any children, visit each one
    NodeList list = node.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
        // Get child node
        Node childNode = list.item(i);

        // Visit child node
        dump0(childNode, level+1);
    }
  }
  
  public void dump()
  {
    OXMLItem item = get();
    assert (item != null);
    Node n = item.getNode();
    if (n == null)
    {
      System.out.println(m_id + " node=empty ");
      return;
    }
    System.out.println(m_id + " "  + getNodeStr(n) + " " + toString());
    Document doc = n.getOwnerDocument();
    if (doc == null)
    {
      dump0(n, 0);
    }
    else
    {
      System.out.println("parent Doc <");
      
      dump0(doc, 0);
      System.out.println("/parent Doc");
    }
   }

  static abstract class IteratorCtx
  {
    abstract boolean meets(XMLNode n, IteratorCtx ctx);
    IteratorCtx()
    {
    }
  }
  
  static class GetOrderIdCtx extends IteratorCtx
  {
    XMLNode m_target;
    int     m_resultOrderId;
    
    GetOrderIdCtx(XMLNode target)
    {
      super();
      m_target = target;
      m_resultOrderId = -1;
    }
    
    boolean meets(XMLNode n, IteratorCtx c)
    {
      GetOrderIdCtx ctx = (GetOrderIdCtx) c;
      if (ctx.m_target == n) {
        m_resultOrderId = n.getDocOrderId();
        return true;
      }
      return false;
    }
  }
  
  static class FindNodeCtx extends IteratorCtx
  {
    int         m_targetOrderId;
    XMLNode     m_result;
    
    FindNodeCtx(int target)
    {
      super();
      m_targetOrderId = target;
      m_result = null;
    }
    
    boolean meets(XMLNode n, IteratorCtx c)
    {
      FindNodeCtx ctx = (FindNodeCtx) c;
      if (ctx.m_targetOrderId == n.getDocOrderId()) {
        m_result = n;
        return true;
      }
      return false;
    }
  }
  
  /*
   * Gets an order id from the document.
   * DocOrderId in XMLNode is not preserved from writing and reading.
   */
  public boolean iterateXMLNodes(IteratorCtx ctx)
  {
    OXMLItem item = get();
    assert (item != null);
    XMLNode n = (XMLNode) m_item.getNode();
    assert (n != null);
    return iterateXMLNodes(n, ctx);
  }
  
  private boolean iterateXMLNodes(XMLNode n, IteratorCtx ctx)
  {
    if (ctx.meets(n, ctx))
      return true;

    NodeList list = n.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
        // Get child node
      n = (XMLNode) list.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.ATTRIBUTE_NODE)
      {
        if (iterateXMLNodes(n, ctx)) 
          return true;
      }
    }
    return false;
  }  
}