/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCChar.java /main/2 2011/04/27 18:37:35 apiper Exp $ */

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
 hopark      05/22/09 - XbranchMerge hopark_bug-8445493_r1 from main
 hopark      05/21/09 - add getQueryPlan, toQXML test
 hopark      04/28/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCChar.java /main/1 2009/11/21 07:38:14 hopark Exp $
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

import java.sql.PreparedStatement;

public class TkJDBCChar extends TkJDBCTestBase
{

  
  public static void main(String[] args) throws Exception
  {
    TkJDBCChar test = new TkJDBCChar();
    test.init(args);
    test.run();
    test.exit();
  }
  
  protected void runTest()
    throws Exception
  {
      stmt.executeUpdate("register stream lchar (c1 integer, c2 char(2147483647))");    
      stmt.executeUpdate("alter stream lchar add source push");

      stmt.executeUpdate("create query tklchar_q1 as select c2, concat(c2, c2) from lchar [range 1]");
      String dest1 = getFileDest("lchar_1");  
      stmt.executeUpdate("alter query tklchar_q1 add destination "+dest1);
      stmt.executeUpdate("alter query tklchar_q1 start");

      stmt.executeUpdate("register stream tklchar_xmlstream (c1 xmltype)");
      stmt.executeUpdate("alter stream tklchar_xmlstream add source push");
      stmt.executeUpdate("create view tklchar_v1(orderId char(16), LastShares integer, LastPrice float) as SELECT X.OrderId, X.LastShares, X.LastPrice from tklchar_xmlstream XMLTable ('//FILL' PASSING BY VALUE tklchar_xmlstream.c1 as \".\" COLUMNS OrderId char(16) PATH 'fn:data(../@ID)', LastShares integer PATH 'fn:data(@LastShares)', LastPrice float PATH 'fn:data(@LastPx)') AS X");
      stmt.executeUpdate("create query tklchar_q2 as IStream(select orderId, sum(LastShares * LastPrice), sum(LastShares * LastPrice) / sum(LastShares) from tklchar_v1[now] group by orderId)");
      String dest2 = getFileDest("lchar_2");  
      stmt.executeUpdate("alter query tklchar_q2 add destination " + dest2);
      stmt.executeUpdate("alter query tklchar_q2 start");
      
      stmt.executeUpdate("alter system run");

      PreparedStatement pstmt = con.prepareStatement("insert into lchar values (?, ?, ?)");
      
      //System.out.println("sending tuples...");
      StringBuilder longstr = new StringBuilder();
      for (int x = 0; x < 10000; x ++)
      {
        longstr.append("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        longstr.append("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
      }
      System.out.println(longstr.length());
      int i = 0;
      while (i < 10)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, i);
        longstr.append(i);
        pstmt.setString(3, longstr.toString());
        pstmt.executeUpdate();
        i++;
      }
      pstmt.close();
      
      pstmt = con.prepareStatement("insert into tklchar_xmlstream values (?, ?)");
      StringBuilder b = new StringBuilder();
      b.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
      b.append("<FIXML>");
      b.append("<ORDER ID=\"A2006101\">");
      for (int x = 0; x < 100; x++)
      {
      b.append("<FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"300\" LastPx=\"81.92\"/>");
      b.append("<FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"900\" LastPx=\"81.90\"/>");
      b.append("<FILL ExecType=\"full\" Sym=\"IBM\" LastShares=\"800\" LastPx=\"81.96\"/>");
      }
      b.append("</ORDER>");
      b.append("</FIXML>");
      
      String xmldata = b.toString();
      System.out.println(xmldata.length());

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
      pstmt = null;
            
  }
}
