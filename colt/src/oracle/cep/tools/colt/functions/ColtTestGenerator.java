/* $Header: pcbpel/cep/colt/src/oracle/cep/tools/colt/functions/ColtTestGenerator.java /main/4 2009/04/21 21:29:02 sbishnoi Exp $ */

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
    sbishnoi    04/21/08 - changing package definition
    skmishra    10/25/07 - fix semicolons in cqlx statements
    sbishnoi    06/26/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/colt/src/oracle/cep/tools/colt/functions/ColtTestGenerator.java /main/4 2009/04/21 21:29:02 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.tools.colt.functions;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;

/**
 * This file will generate a test file to test colt functions.
 * Each query in this test file maps to a colt library function.
 */

class ColtTestGenerator {
  
  public static void main(String[] args) {
    PrintWriter out     = null;
    File        xmlFile = null;
    StringBuilder cqlx;
    try 
    {
      xmlFile = new File(args[1]);
      out     = new PrintWriter(args[0]);
      cqlx    = new StringBuilder();

      DocumentBuilderFactory dbf       = DocumentBuilderFactory.newInstance();
      DocumentBuilder        db        = dbf.newDocumentBuilder();
      Document               d         = db.parse(xmlFile);
      NodeList               classList = d.getElementsByTagName("class");

      cqlx.append("<CEP xmlns=\"http://xmlns.oracle.com/cep\">\n"); 
      cqlx.append("<CEP_DDL>register stream SColtFunc(c1 integer, c2 float, c3 bigint, c4 double) </CEP_DDL>\n");
      cqlx.append("<CEP_DDL><![CDATA[ alter stream SColtFunc add source \"<EndPointReference><Address>file://@ADE_VIEW_ROOT@/pcbpel/cep/test/data/inpSColtFunc.txt</Address></EndPointReference>\"]]> </CEP_DDL>\n");      
      
      // Variable used to distinguish among function with same name
      int fnNo = 1;
      
      // Iterate for Each Class
      for(int i=0 ; i < classList.getLength() ; i++) 
      {
        Node     classNode = classList.item(i);
        NodeList funcList  = classNode.getChildNodes();

        for(int j=0 ; j < funcList.getLength() ; j++, fnNo++) 
        {
          Node         funcNode          = funcList.item(j);
          Node         javaClassNode     = funcNode.getLastChild();
          NamedNodeMap javaClassNameList = javaClassNode.getAttributes();
          Node         javaClassName     = javaClassNameList.getNamedItem("nameOfClass");
          NodeList     funcChildNodes    = funcNode.getChildNodes();
          Node         argumentsNode     = funcChildNodes.item(1);
          NodeList     argNodeList       = argumentsNode.getChildNodes();
          
          cqlx.append("<CEP_DDL>create query qColt");
          cqlx.append(fnNo);
          cqlx.append(" as select ");
          cqlx.append(javaClassName.getNodeValue());
          cqlx.append("(");
          
          for(int k=0 ; k < (argNodeList.getLength()-1) ; k++) 
          {
            Node         argNode     = argNodeList.item(k);
            NamedNodeMap argAttrList = argNode.getAttributes();
            //Node         argIndex    = argAttrList.getNamedItem("index");
            Node         argDataType = argAttrList.getNamedItem("CEPDataTypeClass");
        
            cqlx.append("c");
            if((argDataType.getNodeValue()).equalsIgnoreCase("integer"))
              cqlx.append("1");
            else if((argDataType.getNodeValue()).equalsIgnoreCase("float"))
              cqlx.append("2");
            else if((argDataType.getNodeValue()).equalsIgnoreCase("bigint"))
              cqlx.append("3");
            else if((argDataType.getNodeValue()).equalsIgnoreCase("double"))
              cqlx.append("4");
            if(k < (argNodeList.getLength()-2))
              cqlx.append(",");
          } 
          
          cqlx.append(") from SColtFunc </CEP_DDL>\n");   
          cqlx.append("<CEP_DDL><![CDATA[alter query qColt");
          cqlx.append(fnNo);
          cqlx.append(" add destination \"<EndPointReference><Address>" +
          		        "file://@T_WORK@/cep/log/outSColt");
          cqlx.append(javaClassName.getNodeValue());
          cqlx.append(".txt</Address></EndPointReference>\"]]> </CEP_DDL>\n");
        }        
      }
      
      // Make separate loop to combine queries having syntax 
      // "alter queryN start" at one place 
      
      for(int tmp=1 ; tmp < fnNo ; tmp++) 
      {
        cqlx.append("<CEP_DDL>alter query qColt");
        cqlx.append(tmp);
        cqlx.append(" start </CEP_DDL>\n");
      }
      cqlx.append("<CEP_DDL>alter system run </CEP_DDL>\n");
      cqlx.append("</CEP>");
      out.append(cqlx.toString());
      out.flush();
    }
    catch(IOException e) 
    {
      System.out.println("Problem in writing to cqlx file");
    }
    catch(Exception e) 
    {
      e.printStackTrace();
    }
    finally 
    {
      if(out!=null)
        out.close();
    }
  }
}
