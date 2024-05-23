package oracle.cep.test.mantas;
/* $Header: TkCEPMantasData.java 16-oct-2007.11:55:49 mthatte Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     10/16/07 - 
    najain      08/27/07 - Creation
 */

/**
 *  @version $Header: TkCEPMantasData.java 16-oct-2007.11:55:49 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

import java.sql.*;
import oracle.jdbc.pool.OracleDataSource;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This is a test data generator for the Mantas POC
 * <p>
 *
 * The arguments to main are as follows -
 * <ol>
 * <li> Name of the file to hold Acct table data
 * <li> Name of the file to hold AcctAddr table data
 * <li> Name of the file to hold CashTrxn table data
 * </ol>
 *
 * This test program can be enhanced later on, whereby the underlying tables
 * can be described and the stream data generated accordingly
 */

public class TkCEPMantasData
{
  public static void main (String args [])
       throws Exception
  {
    int j = 0;
    String      acctFile = args[j++];
    File        f1   = new File(acctFile);
    PrintWriter pw1  = new PrintWriter(f1);

    String url = "jdbc:oracle:oci8:@";
    try {
      String url1 = System.getProperty("JDBC_URL");
      if (url1 != null)
        url = url1;
    } catch (Exception e) {
    }

    // Create a OracleDataSource instance and set properties
    OracleDataSource ods = new OracleDataSource();
    ods.setUser("business");
    ods.setPassword("business");
    ods.setURL(url);

    // Connect to the database
    Connection conn = ods.getConnection(); 

    // Create a Statement
    Statement stmt = conn.createStatement ();

    ResultSet rset = stmt.executeQuery ("select ACCT_INTRL_ID, MANTAS_ACCT_BUS_TYPE_CD, ACCT_EFCTV_RISK_NB from Acct");
    pw1.println("c 50, c 20, i, s");

    // Iterate through the result and print the account numbers
    while (rset.next ())
    {
      String acc    = rset.getString(1);
      String typ    = rset.getString(2);
      int    excmt  = rset.getInt(3);
      pw1.println ("0 + " + acc + ", " + typ + ", " + excmt);
    }

    pw1.println("h 5000000000000");
    pw1.close();
    rset.close();

    String      acctAddrFile = args[j++];
    File        f2   = new File(acctAddrFile);
    PrintWriter pw2  = new PrintWriter(f2);
    
    rset = stmt.executeQuery ("select ACCT_INTRL_ID, ADDR_CNTRY_CD from acct_addr");
    pw2.println("c 50, c 2, s");

    // Iterate through the result and print the account numbers
    while (rset.next ())
    {
      String acc = rset.getString(1);
      String cntry = rset.getString(2);
      pw2.println ("0 + " + acc + ", " + cntry);
    }

    pw2.println("h 5000000000000");
    pw2.close();
    rset.close();

    String      cashTrxnFile = args[j++];
    File        f3   = new File(cashTrxnFile);
    PrintWriter pw3  = new PrintWriter(f3);
    
    rset = stmt.executeQuery ("select FO_TRXN_SEQ_ID, TRXN_EXCTN_DT, TRXN_EXCTN_TM, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, DBT_CDT_CD, MANTAS_TRXN_CHANL_CD, TRXN_LOC_ADDR_SEQ_ID from cash_trxn order by TRXN_EXCTN_DT, TRXN_EXCTN_TM");
    pw3.println("i, c 50, i, c 2, c 20, c 20, i");

    // Iterate through the result and print the account numbers
    while (rset.next ())
    {
      int txnId = rset.getInt(1);
      Date dt = rset.getDate(2);
      long tm = dt.getTime();
      String ctm2 = rset.getString(3);
      Integer itm2;

      if (ctm2.charAt(0) == '0')
	itm2 = Integer.decode(ctm2.substring(1,6));
      else
	itm2 = Integer.decode(ctm2.substring(0,6));

      int tm2 = itm2.intValue();
      
      tm += (tm2 % 100) * 1000;
      tm2 = tm2 / 100;
      tm += (tm2 % 100) * 60 * 1000;
      tm2 = tm2 / 100;
      tm += tm2 * 60 * 60 * 1000;
      
      String acc = rset.getString(4);
      int txnAmt = rset.getInt(5);
      String cntry = rset.getString(6);
      String typeTxn = rset.getString(7);
      String chan = rset.getString(8);
      int locn = rset.getInt(9);
      pw3.println (tm + " " + txnId + ", " + acc + ", " + txnAmt + ", " + cntry + ", " + typeTxn + ", " + chan + ", " + locn);
    }

    pw3.println("h 5000000000000");
    pw3.close();
    rset.close();

    stmt.close();
    conn.close();   
  }
}
