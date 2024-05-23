/* $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/functions/XML2ColtFunction.java /main/4 2012/03/31 10:59:35 apiper Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/29/08 - 
    sbishnoi    08/04/21 - changing package definition
    sbishnoi    08/08/07 - changed arguments to main
    sbishnoi    06/21/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/functions/XML2ColtFunction.java /main/4 2012/03/31 10:59:35 apiper Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

/**
 *  Generates all the colt functions supported by CEP   
 */
package oracle.cep.tools.colt.functions;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Method;
import org.w3c.dom.*;

public class XML2ColtFunction {

  public XML2ColtFunction() {
  }
  
  private void transform(File xmlFile, File xslFile, String outputDir, 
                         String className, String cid, String functionName,
                         String fid, String javaClassName)
    throws TransformerException
  {
      javaClassName = (javaClassName.substring(0,1).toUpperCase()).concat(javaClassName.substring(1));
      new File(outputDir).mkdirs();
      File   outputFile = new File(outputDir, "/CEP" + javaClassName + ".java");
      Source xmlSource  = new StreamSource(xmlFile);
      Source xslSource  = new StreamSource(xslFile);
      Result result     = new StreamResult(outputFile.toURI().getPath());

      // Get Transformer instance
      TransformerFactory factory     = TransformerFactory.newInstance();
      Transformer        transformer = factory.newTransformer(xslSource);

      transformer.setParameter("className", className);
      transformer.setParameter("clsId", cid);
      transformer.setParameter("functionName", functionName);
      transformer.setParameter("functionId", fid);
      transformer.setParameter("javaClass", javaClassName);
      transformer.transform(xmlSource, result);
  }



  public static void main(String[] args)
    throws javax.xml.transform.TransformerException, ClassNotFoundException,
           ParserConfigurationException, DOMException, org.xml.sax.SAXException,
           java.io.IOException
  {
    
    XML2ColtFunction       coltFunc  = new XML2ColtFunction();
    File                   xslFile   = new File(args[0]);
    File                   xmlFile   = new File(args[1]);
    String                 outputDir = args[2];
    
    for(String s: args)
      System.out.println(s + ":");
    DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
    DocumentBuilder        db       = dbf.newDocumentBuilder();
    Document               d        = db.parse(xmlFile);
    NodeList               clsList  = d.getElementsByTagName("class");

    for(int i=0 ; i < clsList.getLength() ; i++) 
    {
      Node         clsNode         = clsList.item(i);
      NamedNodeMap tmpClsName      = clsNode.getAttributes();
      Node         clsName         = tmpClsName.getNamedItem("className");
      Node         cId             = tmpClsName.getNamedItem("classId");
      NodeList     fnNodeList      = clsNode.getChildNodes();

      for(int j=0 ; j < fnNodeList.getLength() ; j++) {
        Node         fnNode          = fnNodeList.item(j);
        NamedNodeMap tmpFnName       = fnNode.getAttributes();
        Node         fnName          = tmpFnName.getNamedItem("fName");
        Node         javaClassName   = fnNode.getLastChild();
        NamedNodeMap nameofClassList = javaClassName.getAttributes();
        Node         nameofClass     = nameofClassList.getNamedItem("nameOfClass");
        Node         fid             = tmpFnName.getNamedItem("fId");
        coltFunc.transform(xmlFile, xslFile, outputDir, clsName.getNodeValue(),
                           cId.getNodeValue(), fnName.getNodeValue(), 
                           fid.getNodeValue(), nameofClass.getNodeValue()); 
      }
    }
  }
}
