/* $Header: cep/wlevs_cql/modules/cqlengine/tools/src/oracle/cep/tools/MessageGenerator.java hopark_cqlsnapshot/1 2015/12/18 23:59:58 hopark Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/24/09 - add weblogic I18N message catalog src xml
    parujain    08/21/08 - syntax error
    hopark      11/19/07 - add CustomerLogMsg
    parujain    05/29/07 - 
    anasrini    05/05/07 - component id is 1820
    anasrini    05/04/07 - Take destination for messages.xml as an argument
    skmishra    04/11/07 - convert from oeps to cep
    sbishnoi    02/07/07 - 
    skmishra    02/01/07 - 
    anasrini    02/01/07 - 
    sbishnoi    02/01/07 - Driver for Resource Bundle
    sbishnoi    02/01/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/tools/src/oracle/cep/tools/MessageGenerator.java hopark_cqlsnapshot/1 2015/12/18 23:59:58 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.tools;

import oracle.cep.exceptions.CustomerLogMsg;
import com.oracle.osa.exceptions.ErrorCode;
import oracle.cep.exceptions.ErrorHelper;
/*
import oracle.cep.exceptions.SemanticError;
import oracle.cep.exceptions.ParserError;
import oracle.cep.exceptions.SyntaxError;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.exceptions.DataStructuresError;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.exceptions.MemManagerError;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.NetworkError;
import oracle.cep.exceptions.PhysicalPlanError;
import oracle.cep.exceptions.ServerError;
import oracle.cep.exceptions.StorageError;
import oracle.cep.exceptions.UDAError;
import oracle.cep.exceptions.UDFError;
import com.oracle.cep.exceptions.SparkError;
*/

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.LinkedList;
import java.util.BitSet;

class MessageGenerator
{
  static final int WLS_BASEID = 2050000;

  abstract class GenMsgBase
  {
    abstract void open(String fileName);
    void begin(String name) {}
    abstract void handleErrorCode(ErrorCode ec);
    void end() {}
    abstract void close();
  };
  
  public static void main(String args[])
  {
    try
    {
    boolean wlsmsg = false;
    String outFile = null;
    for (int i = 0; i < args.length; i++)
    {
      String arg = args[i];
      if (arg.equals("-wlevs")) 
      {
        wlsmsg = true;
      } 
      else 
      {
        outFile = arg;
      }
    }
    MessageGenerator g = new MessageGenerator();
    g.generateMsg(outFile, wlsmsg);
    } catch(Throwable e)
    {
      e.printStackTrace();
    }
  }

  private void handleErrorCodes(String name, GenMsgBase m, String errorCodeClassName) {
    try {
      Class<?> c = Class.forName(errorCodeClassName);
      ErrorCode[] codes = (ErrorCode[]) c.getEnumConstants();
      m.begin(name);
      for (ErrorCode code : codes) {
        m.handleErrorCode(code);
      }
      m.end();
    } catch(ClassNotFoundException e) {
      throw new RuntimeException("Class "+errorCodeClassName + " is not found. check the classpath");
    }
  }

  public void generateMsg(String outFile, boolean wlsmsg)
  {
    GenMsgBase m = (wlsmsg ? new WlevsMsgGen() : new OracleMsgGen());

    m.open(outFile);

    handleErrorCodes("Parser", m, "oracle.cep.exceptions.ParseError");
    handleErrorCodes("Semantic", m, "oracle.cep.exceptions.SemanticError");
    handleErrorCodes("Execution", m, "oracle.cep.exceptions.ExecutionError");
    handleErrorCodes("CodeGen", m, "oracle.cep.exceptions.CodeGenError");
    handleErrorCodes("DataStructures", m, "oracle.cep.exceptions.DataStructuresError");
    handleErrorCodes("Interface", m, "oracle.cep.exceptions.InterfaceError");
    handleErrorCodes("LogicalPlan", m, "oracle.cep.exceptions.LogicalPlanError");
    handleErrorCodes("MemManager", m, "oracle.cep.exceptions.MemManagerError");
    handleErrorCodes("Metadata", m, "oracle.cep.exceptions.MetadataError");
    handleErrorCodes("Network", m, "oracle.cep.exceptions.NetworkError");
    handleErrorCodes("PhysicalPlan", m, "oracle.cep.exceptions.PhysicalPlanError");
    handleErrorCodes("Server", m, "oracle.cep.exceptions.ServerError");
    handleErrorCodes("Storage", m, "oracle.cep.exceptions.StorageError");
    handleErrorCodes("Customer", m, "oracle.cep.exceptions.CustomerLogMsg");
    handleErrorCodes("Syntax", m, "oracle.cep.exceptions.SyntaxError");
    handleErrorCodes("UDA", m, "oracle.cep.exceptions.UDAError");
    handleErrorCodes("UDF", m, "oracle.cep.exceptions.UDFError");
    handleErrorCodes("Spark", m, "com.oracle.cep.exceptions.SparkError");

    m.close();
      
  }

  class OracleMsgGen extends GenMsgBase
  {
    PrintWriter out = null;
    StringBuilder xml;
    String outFile;
    
    void open(String outFile)
    {
      this.outFile = outFile;
      try
      {
        out= new PrintWriter(outFile);
      }
      catch(IOException e)
      {
        System.out.println("Problem in writing to " + outFile);
      }

      xml = new StringBuilder();
     
      xml.append("<messages><component number=\"");
      xml.append(ErrorHelper.ComponentID);
      xml.append("\">");
    }
    
    void close()
    {
      xml.append("</component></messages>");
      if (out != null)
      {
        out.append(xml.toString());
        out.flush();
        out.close();
      }
    }
  
    void handleErrorCode(ErrorCode ec)
    {
      xml.append("<message>");
      xml.append("<prefix>");
      xml.append(ErrorHelper.PREFIX);
      xml.append("</prefix>");
      xml.append("<number>");
      xml.append(ErrorHelper.getNumString(ec));
      xml.append("</number>");
      xml.append("<text>");
      xml.append(ErrorHelper.getText(ec));
      xml.append("</text>");
      xml.append("<category>Programmatic</category>");
      xml.append("<type>");
      xml.append(ErrorHelper.getType(ec));
      xml.append("</type>");
      xml.append("<level>");
      xml.append(ErrorHelper.getLevel(ec));
      xml.append("</level>");
      xml.append("<name>");
      xml.append(ErrorHelper.getCodeName(ec));
      xml.append("</name>");
      xml.append("<documented>");
      xml.append(ErrorHelper.isDocumented(ec));
      xml.append("</documented>");
      xml.append("<causes>");
      xml.append("<cause>");
      xml.append(ErrorHelper.getCause(ec));
      xml.append("</cause>");
      xml.append("<action>");
      xml.append(ErrorHelper.getAction(ec));
      xml.append("</action>");
      xml.append("</causes>");
      xml.append("</message>");
    }
  };

  class WlevsMsgGen extends GenMsgBase
  {
    LinkedList<ErrorCode> errorCodes;
    BitSet codemap;
    int minid = Integer.MAX_VALUE;
    int maxid = Integer.MIN_VALUE;
    PrintWriter out = null;
    StringBuilder xml;
    String outFolder;
    String outFile;
    
    void open(String outFolder)
    {
      this.outFolder = outFolder;
      File of = new File(outFolder);
      if (!of.exists()) {
        of.mkdirs();
      }
      codemap = new BitSet();     
    }
    
    void close()
    {
    }
    
    void begin(String name)
    {
      outFile = outFolder + "/" + name + ".xml";
      try
      {
        out= new PrintWriter(outFile);
      }
      catch(IOException e)
      {
        System.out.println("Problem in writing to " + outFile);
      }
      System.out.println("Generating " + outFile);
      xml = new StringBuilder();
      errorCodes = new LinkedList<ErrorCode>();   
      minid = Integer.MAX_VALUE;
      maxid = Integer.MIN_VALUE;
    }
    
    void end()
    {
      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      xml.append("<!-- Copyright 2009 Oracle, Corp. -->\n");
      xml.append("<!DOCTYPE message_catalog PUBLIC \"weblogic-message-catalog-dtd\"  \"http://www.bea.com/servers/wls710/dtd/msgcat.dtd\">\n");
      xml.append("<message_catalog\n");
      xml.append("  i18n_package=\"oracle.cep.exceptions\"\n");
      xml.append("  l10n_package=\"oracle.cep.exceptions\"\n");
      xml.append("  subsystem=\"CEP\"\n");
      xml.append("  version=\"1.0\"\n");
      xml.append("  baseid=\"" + minid + "\"\n");
      xml.append("  endid=\"" + maxid + "\"\n");
      xml.append("  loggables=\"true\"\n");
      xml.append("  prefix=\"BEA\">\n");
      for (ErrorCode ec : errorCodes)
      {
        genMsg(ec, xml);
      }
      xml.append("</message_catalog>\n");
      if (out != null)
      {
        out.append(xml.toString());
        out.flush();
        out.close();
      }
    }
  
    void handleErrorCode(ErrorCode ec)
    {
      int bn = ErrorHelper.getNum(ec);
      int n = bn + WLS_BASEID;
      if (n < minid)
        minid = n;
      if (n > maxid)
        maxid = n;
      if (codemap.get(bn)) 
      {
        ErrorCode ec1 = null;
        for (ErrorCode oec : errorCodes)
        {
          if (bn == ErrorHelper.getNum(oec))
          {
            ec1 = oec;
            break;
          }
        }
        System.out.println(n + " for '"+ ec.getClass().getSimpleName() + "' is already used by " + ec1.getClass().getSimpleName());
      }
      codemap.set(bn);
      errorCodes.add(ec);
    }
    
    String getMethod(ErrorCode ec) 
    {
      StringBuilder b= new StringBuilder();
      /*
      String n = ec.toString();
      b.append("get");
      String[] words = n.split("_");
      for (String w : words)
      {
        String ww = w.toLowerCase();
        String wwh = ww.substring(0, 1);
        b.append(wwh.toUpperCase());
        b.append(ww.substring(1));
      }
      */
      String n = ec.toString();
      b.append(n);
      b.append("(");
      String txt = ErrorHelper.getText(ec);
      int i = 0;
      while (true) 
      {
        String key = "{"+i+"}";
        int pos = txt.indexOf(key);
        if (pos < 0) break;
        if (i > 0)
          b.append(", ");
        b.append("Object arg"+i);
        i++;
      }
      b.append(")");
      return b.toString();
    }
        
    void genMsg(ErrorCode ec, StringBuilder xml)
    {
      xml.append("<logmessage\n");
      xml.append("  messageid=\"");
      int n = ErrorHelper.getNum(ec);
      n += WLS_BASEID;
      xml.append(n);
      xml.append("\"\n");
      xml.append("  severity=\"");
      xml.append(ErrorHelper.getSeverity(ec));
      xml.append("\"\n");
      xml.append("  methodtype=\"getter\"\n");
      xml.append("  method=\"");
      xml.append(getMethod(ec));
      xml.append("\"");
      xml.append(">\n");

      xml.append("  <messagebody>");
      xml.append(ErrorHelper.getText(ec));
      xml.append("</messagebody>\n");
      xml.append("  <messagedetail>");
      xml.append("</messagedetail>\n");
      xml.append("  <cause>");
      xml.append(ErrorHelper.getCause(ec));
      xml.append("</cause>\n");
      xml.append("  <action>");
      xml.append(ErrorHelper.getAction(ec));
      xml.append("</action>\n");
      xml.append("</logmessage>\n\n");
    }
  };
}
