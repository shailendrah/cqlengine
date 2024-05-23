/* $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/aggr/XML2ColtAggrFunction.java /main/2 2012/03/31 10:59:35 apiper Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/21/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/aggr/XML2ColtAggrFunction.java /main/2 2012/03/31 10:59:35 apiper Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.tools.colt.aggr;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.cep.tools.colt.TransformerHelper;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML2ColtAggrFunction
{
  public static void main(String args[])
    throws javax.xml.transform.TransformerException, ClassNotFoundException,
    ParserConfigurationException, DOMException, org.xml.sax.SAXException,
    java.io.IOException
  {
    File                   xslFile   = new File(args[0]);
    File                   xmlFile   = new File(args[1]);
    String                 outputDir = args[2];
    DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
    DocumentBuilder        db       = dbf.newDocumentBuilder();
    Document               d        = db.parse(xmlFile);
    NodeList               clsList  = d.getElementsByTagName("ColtAggrFunction");
    
    new File(outputDir).mkdirs();
    File outputFile = null;
    TransformerHelper transformer= null;
    for(int i=0 ; i < clsList.getLength() ; i++) 
    {
      Node         clsNode         = clsList.item(i);
      NamedNodeMap tmpClsName      = clsNode.getAttributes();
      Node         clsName         = tmpClsName.getNamedItem("className");
      outputFile = new File(outputDir + "/" + clsName.getNodeValue() + ".java");
      transformer = new TransformerHelper(xmlFile, xslFile, outputFile);
      transformer.setParameter("functionName", clsName.getNodeValue());
      transformer.transform();
    }
  }
  
  
}

