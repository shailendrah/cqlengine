/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTestDoubleCounting1.java /main/7 2015/07/07 18:30:34 sbishnoi Exp $ */

/* Copyright (c) 2012, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    06/30/15 - changing db host to adc01jky
    sbishnoi    10/06/14 - modifying dbhost to use adc2101019
    sbishnoi    05/29/14 - changing test db host to slc01eav
    udeshmuk    08/08/12 - remove the wid and tid columns from database entity
                           table
    udeshmuk    06/25/12 - use wlevs.home as sysproperty
    udeshmuk    06/23/12 - use WLEVS_HOME instead of ADE_VIEW_ROOT
    udeshmuk    06/06/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTestDoubleCounting1.java /main/7 2015/07/07 18:30:34 sbishnoi Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class TkJDBCTestDoubleCounting1 extends TkJDBCTestBase
{
  private String dbHost = System.getenv("test.db.host");
  private String dbPort = System.getenv("test.db.port");
  private String dbSID = System.getenv("test.db.sid");
  private String username=System.getenv("test.db.user");
  private String password=System.getenv("test.db.password");
 
  private static final String TEST_DB_HOST = "adc01jky.us.oracle.com";
  private static final String TEST_DB_PORT = "1521";
  private static final String TEST_DB_SID = "xe";
  private static final String TEST_DB_USER = "soainfra";
  private static final String TEST_DB_PASSWORD = "soainfra";

  private String url = null;
  private Connection dbConn = null;
  private Statement  dbStmt = null;
  
  /**
   * Suffix is needed to ensure that the tablenames are unique 
   */
  private String     suffix = null;
  
  /**
   * The list of created table names 
   */
  private List<String> tblNames = null;
  private List<String> dropCEPDDLs = null;

  public TkJDBCTestDoubleCounting1()
  {
    StringBuffer sb = new StringBuffer("jdbc:oracle:thin:@");
    sb.append(dbHost != null ? dbHost : TEST_DB_HOST);
    sb.append(":");
    sb.append(dbPort != null ? dbPort : TEST_DB_PORT);
    sb.append(":");
    sb.append(dbSID != null ? dbSID : TEST_DB_SID);

    url = sb.toString();
    username = username != null ? username : TEST_DB_USER;
    password = password != null ? password : TEST_DB_PASSWORD;
  }  

  public static void main(String[] args) throws Exception
  {
    TkJDBCTestDoubleCounting1 test = new TkJDBCTestDoubleCounting1();
    test.init(args);
    test.run();
    test.exit();
  }
    
  @Override
  protected void runTest() throws Exception
  { 
    System.out.println("Running TkJdbcTestDoubleCounting1...");

    suffix = System.getProperty("wlevs.home"); 
    System.out.println("wlevs.home in test="+suffix);
    LogUtil.info(LoggerType.TRACE, "wlevs.home in test="+suffix);
    if(suffix == null)
    {
      System.out.println("Cannot run the test - wlevs.home is not set!");
      LogUtil.info(LoggerType.TRACE, 
                   "Cannot run the test - wlevs.home is not set!");
      System.out.println("Unable to generate unique table names!");
      LogUtil.info(LoggerType.TRACE, 
                   "unable to generate unique table names!");
      return;
    }
    //we get back wlevs.home in this form /ade/udeshmuk_cep3/cep/wlevs_cql
    //and we are interested in only the name 'udeshmuk_cep3'
    String[] tmp = suffix.split("/"); 
    suffix = tmp[tmp.length-3];
    suffix = suffix.replace('-','_');
    
    tblNames = new LinkedList<String>();
    
    try
    {
      //create db tables
      createDBArtifacts();
      
      //add history data
      sendHistoryData();
 
      //create CEP relations and queries
      createCEPArtifacts();
     
      //start query q1
      stmt.executeUpdate("alter query q_double_counting_1 start");
      
      stmt.executeUpdate("alter system run");
      
      //send some data prior to starting q2
      sendPreSecondQueryData();
            
      //start query q2 - when q1 is already running.
      stmt.executeUpdate("alter query q_double_counting_2 start");
      
      //send some new data - db and cep both
      sendPostSecondQueryData();
      
      System.out.println("Done! ");
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
      throw e;
    }
    finally
    {
      removeCEPArtifacts();
      removeDBArtifacts();
    }
  }
  
  private void createDBArtifacts() throws Exception
  {
    getDBConnection();
    dbStmt = dbConn.createStatement();

    String tblName = "dc_"+suffix;
    if(tblName.length() > 30)
      tblName = tblName.substring(0,30);
    tblNames.add(tblName);
    
    String ddl = "create table "+tblName
                 +" (c1 integer, eid number(19,0), c2 varchar2(10))";
    
    dbStmt.executeUpdate(ddl);
    System.out.println(ddl);
    LogUtil.info(LoggerType.TRACE, ddl);
    
    tblName = "BTC_"+suffix;
    if(tblName.length() > 30)
      tblName = tblName.substring(0,30);
    tblNames.add(tblName);
    
    ddl =  "create table "+tblName
           + "(transaction_cid number(19,0), transaction_tid number(19,0))";
    
    dbStmt.executeUpdate(ddl);
    System.out.println(ddl);
    LogUtil.info(LoggerType.TRACE, ddl);
  }
  
  private void createCEPArtifacts() throws Exception
  {
    dropCEPDDLs = new LinkedList<String>();
    String dest = null;
    
    //create archived relation
    stmt.executeUpdate(
        "create archived relation dc_R(c1 int, tid bigint," +
        " eid bigint, c2 char(10), wid bigint) archiver " +
        "BIArchiver entity \"soainfra."+tblNames.get(0)+"\""+
        " event identifier eid worker identifier wid " +
        "transaction identifier tid");
    LogUtil.info(LoggerType.TRACE,
        "create archived relation dc_R(c1 int, tid bigint," +
        " eid bigint, c2 char(10), wid bigint) archiver " +
        "BIArchiver entity \"soainfra."+tblNames.get(0)+"\""+
        " event identifier eid worker identifier wid " +
        "transaction identifier tid");
    
    stmt.executeUpdate("alter relation dc_R add source push");
    dropCEPDDLs.add("drop relation dc_R");
      
    //create query q1
    stmt.executeUpdate("create query q_double_counting_1 as select distinct c2 from dc_R");
    LogUtil.info(LoggerType.TRACE,
                 "create query q_double_counting_1 as select distinct c2 from dc_R");
    dest = getFileDest("dc_1");
    stmt.executeUpdate("alter query q_double_counting_1 add destination "+dest);
    stmt.executeUpdate("alter query q_double_counting_1 set start_time 1000000000L ");
    dropCEPDDLs.add("drop query q_double_counting_1");
    
    //create query q2
    stmt.executeUpdate("create query q_double_counting_2 as IStream(select c2 from dc_R)");
    LogUtil.info(LoggerType.TRACE, 
                 "create query q_double_counting_2 as IStream(select c2 from dc_R)");
    dest = getFileDest("dc_2");
    stmt.executeUpdate("alter query q_double_counting_2 add destination "+dest);
    stmt.executeUpdate("alter query q_double_counting_2 set start_time 2000000000L ");
    dropCEPDDLs.add("drop query q_double_counting_2");
  }
  
  private void removeCEPArtifacts() 
  {
    if(dropCEPDDLs.size() > 0)
    {
      for(int idx = dropCEPDDLs.size() - 1; idx >= 0; idx--)
      {
        try
        {
          stmt.executeUpdate(dropCEPDDLs.get(idx));
          LogUtil.info(LoggerType.TRACE, dropCEPDDLs.get(idx));
        }
        catch(Exception e)
        {
          System.out.println(e.getMessage());
          continue;
        }
      }
    }
  }
  
  private void removeDBArtifacts() 
  {
    try
    {
      for(String tblName : tblNames)
      {  
        String ddl = "drop table "+tblName;
        System.out.println(ddl);
        dbStmt.executeUpdate(ddl);
        LogUtil.info(LoggerType.TRACE, ddl);
      }
      dbStmt.close();
      dbStmt = null;
      dbConn.close();
      dbConn = null;
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
   
  }
  
  private void sendHistoryData() throws Exception
  {
    //add data in db
    
    //double counting DO first :
    //c1, eid, c2
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values " +
      "(25, 1, 'unmesh' )");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values " +
      "(50, 2, 'sandeep' )");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values " +
      "(75, 3, 'patha' )");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values " +
      "(100, 4, 'prathab' )");
    //BEAM_TRANSACTION_CONTEXT DO:
    //transaction_cid (workerid) , transaction_tid(txn_id)
    dbStmt.executeUpdate("insert into "+tblNames.get(1)+" values " +
      "(2, 1)");
    dbStmt.executeUpdate("insert into "+tblNames.get(1)+" values " +
      "(3, 2)");
    dbStmt.executeUpdate("insert into "+tblNames.get(1)+" values " +
      "(1, 1)");
  }
  
  private void sendPreSecondQueryData() throws Exception
  {
    //send to database
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(125, 5, 'prabish')");
    dbStmt.executeUpdate("update "+tblNames.get(1)+" set transaction_tid=2 where transaction_cid=1");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(175, 6, 'anand')");
    dbStmt.executeUpdate("update "+tblNames.get(1)+" set transaction_tid=3 where transaction_cid=3");
    
    //send to CEP server
    //Schema: timestamp, c1, tid, eid, c2, wid
    stmt.executeUpdate("insert into dc_R values (5000000000l, 125, 2, 5, \"prabish\", 1)");
    //the tuple below should be ignored by q1.
    stmt.executeUpdate("insert into dc_R values (6000000000l, 25, 1, 1, \"unmesh\", 2)");
    stmt.executeUpdate("insert into dc_R values (7000000000l, 175, 3, 6, \"anand\", 3)");
    
  }
  
  private void sendPostSecondQueryData() throws Exception
  {
    //send to database
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(175, 7, 'sunny')");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(200, 8, 'vikram')");
    dbStmt.executeUpdate("update "+tblNames.get(1)+" set transaction_tid=2 where transaction_cid=2");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(225, 9, 'junger')");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(300, 10, 'eric')");
    dbStmt.executeUpdate("update "+tblNames.get(1)+" set transaction_tid=3 where transaction_cid=1");
    dbStmt.executeUpdate("insert into "+tblNames.get(0)+" values" +
      "(325, 11, 'jeff')");
    dbStmt.executeUpdate("update "+tblNames.get(1)+" set transaction_tid=4 where transaction_cid=3");
    
    //send to CEP server
    //the tuple below should be ignored by q2
    stmt.executeUpdate("insert into dc_R values (8000000000l, 175, 2, 7, \"sunny\", 1)");
    //the tuple below should be ignored by q1 and q2 both
    stmt.executeUpdate("insert into dc_R values (9000000000l, 25, 1, 1, \"unmesh\", 2)");
    //the tuple below should be processed by q1 and q2 both
    stmt.executeUpdate("insert into dc_R values (10000000000l, 200, 2, 8, \"vikram\", 2)");
    //the tuple below should be ignored by q2
    stmt.executeUpdate("insert into dc_R values (11000000000l, 225, 3, 9, \"junger\", 3)");
    //the tuple below should be processed by q1 and q2 both
    stmt.executeUpdate("insert into dc_R values (12000000000l,300, 3, 10, \"eric\", 1)");
    //the tuple below should be processed by q1 and q2 both
    stmt.executeUpdate("insert into dc_R values (13000000000l, 325, 4, 11, \"jeff\", 3)");
    
    //heartbeat sent to flush out IStream output
    stmt.executeUpdate("insert into dc_R heartbeat at 14000000000l");
  }
  
  private void getDBConnection() throws Exception 
  {
    if (dbConn == null) {
      System.out.println("Connecting  to  DB "+url);
      DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
      dbConn = DriverManager.getConnection(url, username, password);
      System.out.println("Connection Established...");
    }
    else
      System.out.println("dbConn is not null");
  }

}
