/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCNLS.java /main/4 2011/04/27 18:37:35 apiper Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      05/21/09 - add getQueryPlan, toQXML test
 hopark      04/28/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCNLS.java /main/3 2009/12/05 13:43:51 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */


/**
 * A sample jdbc program. A very simple test with integer datatypes. New
 * tests can be modelled on it.
 *
 * @author najain
 */
package oracle.cep.test.jdbc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;

import oracle.cep.jdbc.CEPConnection;

public class TkJDBCNLS extends TkJDBCTestBase
{

  
  public static void main(String[] args) throws Exception
  {
    TkJDBCNLS test = new TkJDBCNLS();
    test.init(args);
    test.run();
    test.exit();
  }
  
  protected void dumpXMLtoFile(String xml, String filepath)
  {
      try {
        FileOutputStream fos = new FileOutputStream(filepath);
        Writer out = new OutputStreamWriter(fos, "UTF8");
        out.write(xml);
        out.write("\n");
        out.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
  
  protected void runTest()
    throws Exception
  {
      String utf16str = new String("\u65e5\u672c\u8a9e\u6587\u5b57\u5217");
      
      stmt.executeUpdate("register stream " + utf16str + " (c1 integer, c2 char(40))");
      stmt.executeUpdate("alter stream " + utf16str + " add source push");

      stmt.executeUpdate("create query tknls_q1 as select * from " + utf16str+ " [range 1]");
      String dest1 = getFileDest("nls_1");  
      stmt.executeUpdate("alter query tknls_q1 add destination "+dest1);
      stmt.executeUpdate("alter query tknls_q1 start");

      stmt.executeUpdate("register stream tknls_xmlstream (c1 xmltype)");
      stmt.executeUpdate("alter stream tknls_xmlstream add source push");
      stmt.executeUpdate("create view tknls_v1(orderId char(16), LastShares integer, LastPrice float) as SELECT X.OrderId, X.LastShares, X.LastPrice from tknls_xmlstream XMLTable ('//FILL' PASSING BY VALUE tknls_xmlstream.c1 as \".\" COLUMNS OrderId char(16) PATH 'fn:data(../@ID)', LastShares integer PATH 'fn:data(@LastShares)', LastPrice float PATH 'fn:data(@LastPx)') AS X");
      stmt.executeUpdate("create query tknls_q2 as IStream(select orderId, sum(LastShares * LastPrice), sum(LastShares * LastPrice) / sum(LastShares) from tknls_v1[now] group by orderId)");
      String dest2 = getFileDest("nls_2");  
      stmt.executeUpdate("alter query tknls_q2 add destination " + dest2);
      stmt.executeUpdate("alter query tknls_q2 start");
      
      stmt.executeUpdate("create query tknls_q3 as select * from " + utf16str+ "[NOW] where c2 =\"" + utf16str + "5\"");
      String dest3 = getFileDest("nls_3");  
      stmt.executeUpdate("alter query tknls_q3 add destination "+dest3);
      stmt.executeUpdate("alter query tknls_q3 start");
      
      stmt.executeUpdate("register stream tknls_str2 (c1 integer, "+utf16str + " char(40))");
      stmt.executeUpdate("alter stream tknls_str2 add source push");

      stmt.executeUpdate("create query tknls_q4 as select c1, " + utf16str + " from tknls_str2 [range 1]");
      String dest4 = getFileDest("nls_4");  
      stmt.executeUpdate("alter query tknls_q4 add destination "+dest4);
      stmt.executeUpdate("alter query tknls_q4 start");
      
      stmt.executeUpdate("alter system run");

      System.out.println("dumping query plan");
      CEPConnection cepcon = (CEPConnection) con;
      String queryPlan = null;
      String dest = null;
      /*
       * getXMLPlan2 is redundant as we are invoking toQCXML below
       * It also seems too much dependent to the internal ids.
       * We do not compare the output. 
       */
      try
      {
        queryPlan = cepcon.getCEPServer().getXMLPlan2();
        dest = getFileName("nls_qplan", ".log");  
        dumpXMLtoFile(queryPlan, dest);
      } catch (RemoteException e)
      {
        e.printStackTrace();
      } 
      
//xmltable is not supported..      
//      queryPlan = cepcon.toQCXML("tknls_v1", true);
//      String dest = getFileDest("nls_v1");  
//      dumpXMLtoFile(queryPlan, dest);
      queryPlan = cepcon.toQCXML("tknls_q1", false);
      dest = getFileName("nls_q1");  
      dumpXMLtoFile(queryPlan, dest);
      queryPlan = cepcon.toQCXML("tknls_q2", false);
      dest = getFileName("nls_q2");  
      dumpXMLtoFile(queryPlan, dest);
      queryPlan = cepcon.toQCXML("tknls_q3", false);
      dest = getFileName("nls_q3");  
      dumpXMLtoFile(queryPlan, dest);
      queryPlan = cepcon.toQCXML("tknls_q4", false);
      dest = getFileName("nls_q4");  
      dumpXMLtoFile(queryPlan, dest);

      PreparedStatement pstmt = con.prepareStatement("insert into " + utf16str + " values (?, ?, ?)");
      
      //System.out.println("sending tuples...");
      int i = 0;
      while (i < 10)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, i);
        pstmt.setString(3, utf16str + i);
        pstmt.executeUpdate();
        i++;
      }
      pstmt.close();
      
      pstmt = con.prepareStatement("insert into tknls_xmlstream values (?, ?)");
      StringBuilder b = new StringBuilder();
      b.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
      b.append("<FIXML>");
      b.append("<ORDER ID=\"" + utf16str + "A2006101\">");
      b.append("<FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"300\" LastPx=\"81.92\"/>");
      b.append("<FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"900\" LastPx=\"81.90\"/>");
      b.append("<FILL ExecType=\"full\" Sym=\"IBM\" LastShares=\"800\" LastPx=\"81.96\"/>");
      b.append("</ORDER>");
      b.append("</FIXML>");
      
      String xmldata = b.toString();
      //System.out.println("sending tuples...");
      i = 11;
      while (i < 20)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setString(2, xmldata);
        pstmt.executeUpdate();
        i++;
      }  
      pstmt.close();
      
      pstmt = con.prepareStatement("insert into tknls_str2 values (?, ?, ?)");
      //System.out.println("sending tuples...");
      i = 21;
      while (i < 30)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, i);
        pstmt.setString(3, utf16str + i);
        pstmt.executeUpdate();
        i++;
      }
      pstmt.close();
      pstmt = null;
            
  }
}
