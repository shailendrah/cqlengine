/* $Header: pcbpel/cep/common/src/oracle/cep/util/XMLHelper.java /main/12 2009/02/23 00:45:57 skmishra Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    02/20/09 - making html-compatible strings from inner text
    skmishra    02/13/09 - removing MetadataException
    hopark      11/12/08 - add printDOM
    parujain    02/07/08 - parameterizing errors
    hopark      12/20/07 - change buildElement
    anasrini    08/22/07 - disable XMLSchema based validation
    hopark      08/02/07 - add buildElement
    dlenkov     08/07/07 - fixed processElementNode
    hopark      04/27/07 - add verifyDocument
    parujain    02/13/07 - XML Parsing Helper
    parujain    02/13/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/util/XMLHelper.java /main/12 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.Reader;
import java.io.StringReader;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.xml.parser.schema.XMLSchema;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLHelper 
{
  static ThreadLocal<StringBuilder> s_buffers = new ThreadLocal<StringBuilder>();
  
  private static String processChildNode(Node child) throws CEPException
  {
    if (child.getNodeType() == Node.TEXT_NODE)
      return processTextNode((Text)child);
    else if (child.getNodeType() == Node.ELEMENT_NODE)
      return processElementNode((Element)child);
    else 
      throw new CEPException(InterfaceError.XML_FORMAT_ERROR);
  }

  private static String processTextNode(Text node)
  {
    return node.getNodeValue();
  }
  
  public static String processElementNode(Element node) throws CEPException
  {
    String elem = new String("<");
    // elem = elem.concat(node.getTagName());
    elem = elem.concat(node.getLocalName());
    elem = elem.concat(">");
    
    NodeList lst = node.getChildNodes();
    for (int i = 0; i < lst.getLength(); i++)
      elem = elem.concat(processChildNode(lst.item(i)));

    elem = elem.concat("</");
    elem = elem.concat(node.getLocalName());
    elem = elem.concat(">");

    return elem;
  }
  
  public static String processNode(Node n) throws CEPException
  {
    NodeList lst = n.getChildNodes();
    String ddl = null;
    
    for (int i = 0; i < lst.getLength(); i++)
    {
      if (ddl == null)
        ddl = processChildNode(lst.item(i));
      else
        ddl = ddl.concat(processChildNode(lst.item(i)));
    }
    
    return ddl;
  }
  
  public static XMLDocument getXMLDocument(String str) throws CEPException
  {
    DOMParser dp;
    XMLDocument doc;

    try
    {
      dp = new DOMParser();
      // create a document from the source
      Reader reader = new StringReader(str);
      dp.parse(reader);
      doc = dp.getDocument();
    }
    catch (Exception e)
    {
      throw new CEPException(InterfaceError.XML_FORMAT_ERROR);
    }
    return doc;
  }

  public static XMLDocument verifyDocument(String source) 
    throws CEPException 
  {
    XMLDocument xmlDoc;
    XMLSchema xmlSchema = null;

    //
    // Disbling validation - see bug 6357266
    //
    /*
    try {
      XSDBuilder xsdBuild = new XSDBuilder();
      URL url = new URL(new String("http://schemas.xmlsoap.org/ws/2004/03/addressing/"));
      
      String proxy = CEPManager.getConfigMgr().getProperty("proxy", "www-proxy-hqdc.us.oracle.com:80");
      if (proxy != null) 
      {
        int pos = proxy.indexOf(':');
        String port = "80";
        String host = proxy;
        if (pos > 0) 
        {
          host = proxy.substring(0, pos);
          port = proxy.substring(pos+1);
          System.getProperties().put("proxySet","true");
          System.getProperties().put("proxyPort",port);
          System.getProperties().put("proxyHost",host);
        }
      }
      xmlSchema = (XMLSchema) xsdBuild.build(url);
    } catch (Exception xe)
    {
      LogUtil.warning(LoggerType.TRACE, xe.toString());
      xmlSchema = null;
    }
    */

    xmlSchema = null;
    try {
      // Create the document from the source
      oracle.xml.parser.v2.DOMParser parser = new oracle.xml.parser.v2.DOMParser();
      Reader reader = new StringReader(source);
      parser.parse(reader);
      xmlDoc = parser.getDocument();

    } catch (Exception ex) {
      throw new CEPException(InterfaceError.XML_FORMAT_ERROR);
    }

    if (xmlSchema != null)
    {
      xmlDoc.setSchema(xmlSchema);
      short val = xmlDoc.validateDocument();
    // TODO NAMIT: comment for now -- needs to be resolved
    // if (val != NodeEditVAL.VAL_TRUE)
    //     throw new MetadataException(MetadataError.INVALID_ENDPOINTREF);
    }
    
    return xmlDoc;
  }
  
  public static String buildElement(boolean useTag,
                                    String tag, 
                                    String value, 
                                    String[] attrNames, 
                                    String[] attrVals)
  {
    StringBuilder sb = new StringBuilder();
    if (useTag)
    {  
      sb.append("<");
      sb.append(tag);
    }
    else
    {
      sb.append(tag);
      sb.append("(");
    }
    if (attrNames != null && attrVals != null)
    {
      assert (attrNames.length == attrVals.length);
      for (int i = 0; i < attrNames.length; i++)
      {
        String an = attrNames[i];
        String av = attrVals[i];
        sb.append(" ");
        sb.append(an);
        sb.append("=\"");
        sb.append(av);
        sb.append("\"");
      }
    }
    if (!useTag)
    {
      sb.append(")");
      sb.append(" ");
    }
    if (value != null)
    {
      if (useTag)
        sb.append(">");
      else
        sb.append("=");
      sb.append(value);
      if (useTag)
      {
        sb.append("</");
        sb.append(tag);
        sb.append(">");
      }
      else
      {
        sb.append("/");
        sb.append(tag);
      }
    }
    else 
    {
      if (useTag) 
        sb.append("/>");
    }
    return sb.toString();
  }

    
  private static void printNode(Node node, StringBuilder b, String indent)  
  {
    switch (node.getNodeType()) 
    {
      case Node.DOCUMENT_NODE:
        b.append("<?xml version=\"1.0\"?>\n");
        NodeList nodes = node.getChildNodes();
        if (nodes != null) 
        {
           for (int i=0; i<nodes.getLength(); i++) 
           {
             printNode(nodes.item(i), b, indent + "  ");
           }
        }
        break;
                
      case Node.ELEMENT_NODE:
        String name = node.getNodeName();
        b.append(indent + "<" + name);
        NamedNodeMap attributes = node.getAttributes();
        for (int i=0; i<attributes.getLength(); i++) 
        {
          Node current = attributes.item(i);
          b.append(" " + current.getNodeName() +
                   "=\"" + current.getNodeValue() +
                   "\"");
        }
        b.append(">");
                
        NodeList children = node.getChildNodes();
        if (children != null) 
        {
          for (int i=0; i<children.getLength(); i++) 
          {
            printNode(children.item(i), b, indent + "  ");
          }
        }
                
        b.append("</" + name + ">");
        break;

       case Node.TEXT_NODE:
        b.append(node.getNodeValue());
        break;
    }
  }    

  public static String DOMtoString(Node node)
  {
    StringBuilder b = new StringBuilder();
    printNode(node, b, " ");
    return b.toString();
  }

  public static String toHTMLString(String arg)
  {
    String retString;
    retString = arg.replaceAll(">", "&gt;");
    retString = retString.replaceAll("<", "&lt;");
    return retString;
  }
}
