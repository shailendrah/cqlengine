package oracle.cep.test.csfb;
/* $Header: CSFB2.java 11-dec-2006.21:12:47 rkomurav Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    12/11/06 - fix because of count(*) emitting a zero when no rows
                           in output
    najain      10/25/06 - integrate with mds
    bisong      09/01/06 - change inp/outfiles directory
    anasrini    08/25/06 - more tests
    najain      08/21/06 - Creation
 */

/**
 *  @version $Header: CSFB2.java 11-dec-2006.21:12:47 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

/********* Requirements

   1. Every 5 minutes send one element with following counts -
         1. Total number of trades in last 5 mins -- 
               total number of trades that had cutoff in last 5 mins
         2. Total number of failed trades in last 5 mins
               1. BUY among these
               2. SELL among these
               3. EXCHG among these
               4. In TRADE_NOACK status among these
               5. In TRADE_RECVD status among these
               6. In TRADE_PROCESSING status among these
         3. To report total failures (BUY+SELL+EXCHG)
   2. Every 5 minutes send one element per ticker symbol with following info
         1. Ticker Symbol
         2. Number of failed trades with that ticker symbol
   3. Send one element with total count and failed count when SLA1 violation 
      occurs -
      SLA1 detection to be done once every 5 mins. 
      SLA1 - more than 5% of trades having cutoff in last 5 minutes are failed
      i.e not executed within 20 seconds
   4. Send one element with total count and failed count when SLA3 violation 
      occurs -
      As per SLA3 a trade is considered failed iff no state change occurs
      for more than 3 seconds
      SLA3 detection to be done once every 5 mins
      SLA3 - more than 10% of trades having state change cutoff in last 
             10 minutes are failed trades

Here is the proposed schema -

_*Schema

   1. TradeInputs(int tradeId, int tradeVolume, char(4) tickerSymbol,
      int tradeType)
         1. tradeType is an integer enumeration constant for BUY, SELL,
            EXCHG such as 0,1,2
   2. TradeUpdates(int tradeId, int status)
         1. status is an integer enumeration constant for 
            TRADE_NOACK, TRADE_RECVD, TRADE_PROCESSING, TRADE_EXECUTED
            such as 0,1,2,3
         2. TRADE_EXECUTED will not come on this stream, it is implied
            by arrival of element on TradeMatched stream
   3. TradeMatched(int tradeId)


*******************************/

import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;

import oracle.cep.service.CEPManager;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.TableManager;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.scheduler.Scheduler;
import oracle.cep.execution.scheduler.RoundRobinScheduler;

/**
 * Test for the CSFB use case
 */

public class CSFB2 {

    private static String var = null;


  // Table names
  private static final String[] tablenames = { 
    "TradeInputs", "TradeUpdates", "TradeMatched", "TradeTypeCodes",
    "TradeStatusCodes", "Dual",
  };

  // Tables 
  private static final String[] tables = {
    "register stream TradeInputs(tradeId integer, tradeVolume integer, tradeSymbol char(4), tradeType integer);",
    // The fields are as follows:
    // tradeId: unique identifier for every trade
    // tradeVolume: volume of the trade
    // tradeSymbol: ticker symbol of the trade
    // tradeType: type of the trade (BUY/SELL/EXCHG)

    "register stream TradeUpdates(tradeId integer, statusCode integer);",
    // the fields are as follows:
    // tradeId: unique identifier for every trade
    // statusCode:
    //  1. TRADE_RECVD
    //  2. TRADE_PROCESSING
    // Note: this stream could contain multiple elements for the same tradeId
    //       Further, there may be multiple elements for the same trade with
    //       same status code

    "register stream TradeMatched(tradeId integer);",

    // Static Relations
    // Relation which contains all permitted tradeType codes
    "register relation TradeTypeCodes(tradeType char(10), code integer);",

    // Relation which contains all permitted status codes
    "register relation TradeStatusCodes(statusCode char(20), code integer);",

    // Dummy relation Dual
    "register relation Dual(c1 integer);",
  };

  // Sources for tables
  private static  String[] sources      = {
    "inpTI1.txt",
    "inpTU1.txt",
    "inpTM1.txt",

    // For the static relations
    "inpTT1.txt",
    "inpTS1.txt",
    "inpDual.txt",
  };

  // Views 
  private static final String[] views = {
    ////////////////////////// SLA1 ///////////////////////////////

    // CutOffTrades - stream of input trades at cutoff time as per SLA1
    "register view CutOffTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as DStream(select * from TradeInputs[range 20 seconds]);",

    // stream of FineTrades as per SLA1 
    // - timestamp of trade is its cutoff time as per SLA1
    "register view FineTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(select c.tradeId, c.tradeVolume, c.tradeSymbol, c.tradeType from CutOffTrades[now] as c, TradeMatched[range 20 seconds] as t where c.tradeId = t.tradeId);",

    // stream of FailedTrades as per SLA1
    // - timestamp of trade is its cutoff time as per SLA1
    "register view Dummy1(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from CutOffTrades[NOW];",
    "register view Dummy2(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from FineTrades[NOW];",
    "register view FailedTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(Dummy1 except Dummy2);",


    // Count of fine trades in last 5 minutes
    "register view TotalFineTrades(c1) as select count(*) from FineTrades[range 5 minutes slide 5 minutes];",

    // Count of failed trades by type (BUY/SELL/EXCHG)
    "register view TotalFailedByNonZeroType(tradeType, total) as select tradeType, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeType;",
    "register view Dummy6(tradeType) as select tradeType from TotalFailedByNonZeroType;",
    "register view Dummy7(tradeType) as select code from TradeTypeCodes;",
    "register view Dummy8(tradeType) as Dummy7 except Dummy6;",
    "register view Dummy9(tradeType, total) as select tradeType, 0 from Dummy8;",
    "register view TotalFailedByType(tradeType, total) as TotalFailedByNonZeroType union Dummy9;",

    // Count of failed trades in last 5 minutes
    "register view TotalFailedTrades(total, dummy) as select sum(total), count(*) from TotalFailedByType;",

    // Count of trades having cutoff in last 5 minutes
    "register view TotalTrades(c1) as select fine.c1+failed.total from TotalFineTrades as fine, TotalFailedTrades as failed;",


    // Unpivot to get count of failed trades by type as a single element
    // (rather than as 3 elements - one each for BUY/SELL/EXCHG)
    "register view Dummy4(buyCount, sellCount) as select a.total, b.total from TotalFailedByType as a, TotalFailedByType as b where a.tradeType = 0 and b.tradeType = 1;",
    "register view TotalFailuresByTypes(buyCount, sellCount, exchgCount) as select b.buyCount, b.sellCount, a.total from TotalFailedByType as a, Dummy4 as b where a.tradeType = 2;",

    // FailedTrades augmented with status at time of failure detection
    "register view FailedTradesStatus(tradeId, statusCode) as IStream(select f.tradeId, u.statusCode from FailedTrades[NOW] as f, TradeUpdates[partition by u.tradeId rows 1] as u where f.tradeId = u.tradeId);",


    // Count of failed trades by status 
    // (TRADE_NOACK/TRADE_RECVD/TRADE_PROCESSING)
    "register view TotalFailedNonZeroByStatus12(statusCode, total) as select statusCode, count(*) from FailedTradesStatus[range 5 minutes slide 5 minutes] group by statusCode;",
    "register view Dummy10(statusCode) as select statusCode from TotalFailedNonZeroByStatus12;",
    "register view Dummy11(statusCode) as select code from TradeStatusCodes where code = 1;",
    "register view Dummy12(statusCode) as select code from TradeStatusCodes where code = 2;",
    "register view Dummy13(statusCode) as Dummy11 union Dummy12;",
    "register view Dummy14(statusCode) as Dummy13 except Dummy10;",
    "register view Dummy15(statusCode, total) as select statusCode, 0 from Dummy14;",
    "register view TotalFailedStatus12(statusCode, total) as TotalFailedNonZeroByStatus12 union Dummy15;",
    "register view Dummy18(total, dummy) as select sum(total), count(*) from TotalFailedStatus12;",
    "register view TotalFailedStatus0(statusCode, total) as select 0, all.total - status12.total from TotalFailedTrades as all, Dummy18 as status12;",
    "register view TotalFailedByStatus(statusCode, total) as TotalFailedStatus0 union TotalFailedStatus12;",

    // Unpivot to get count of failed trades by status as a single element
    // (rather than as 3 elements - one each for
    // (TRADE_NOACK/TRADE_RECVD/TRADE_PROCESSING)
    "register view Dummy5(status0, count0, status1, count1) as select a.statusCode, a.total, b.statusCode, b.total from TotalFailedByStatus as a, TotalFailedByStatus as b where a.statusCode = 0 and b.statusCode = 1;",
    "register view TotalFailuresByStatus(status0, count0, status1, count1, status2, count2) as select b.status0, b.count0, b.status1, b.count1, a.statusCode, a.total from TotalFailedByStatus as a, Dummy5 as b where a.statusCode = 2;",

    // Unpivot to get count of failed trades by status and type as single 
    // element
    "register view TotalFailuresByStatusType(buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select a.buyCount, a.sellCount, a.exchgCount, b.count0, b.count1, b.count2 from TotalFailuresByTypes as a, TotalFailuresByStatus as b;",

    // Unpivot above to include total trades count and total failed trades 
    // count
    "register view TotalReq1(totalCount, totalFailedCount, buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select t.c1, a.buyCount+a.sellCount+a.exchgCount, a.buyCount, a.sellCount, a.exchgCount, a.noackCount, a.recvdCount, a.processedCount from TotalFailuresByStatusType as a, TotalTrades as t;",
    

    ////////////////////////// SLA3 ///////////////////////////////
    
    // Stream of status changes of each trade
    "register view TradeStatus0(tradeId, statusCode) as select tradeId, 0 from TradeInputs;",
    "register view TradeStatus3(tradeId, statusCode) as select tradeId, 3 from TradeMatched;",
    "register view TradeStatus03(tradeId, statusCode) as TradeStatus0 union TradeStatus3;",
    "register view TradeStatusStreamDup(tradeId, statusCode) as TradeStatus03 union TradeUpdates;",
    "register view TradeStatus(tradeId, statusCode) as select * from TradeStatusStreamDup[partition by tradeId rows 1];",
    "register view TradeStatusStream(tradeId, statusCode) as IStream(select * from TradeStatus);",

    // CutOff stream for the SLA3
    "register view CutOff3(tradeId, statusCode) as DStream(select tradeId, statusCode from TradeStatusStream[range 3 seconds] where statusCode < 3);",

    // Failed stream for the SLA3
    "register view Failed3(tradeId, statusCode) as IStream(select a.tradeId, a.statusCode from CutOff3[NOW] as a, TradeStatus as b where a.tradeId = b.tradeId and a.statusCode = b.statusCode);",

    // Count of candidates -- refreshed every 5 minutes
    "register view TotalCutOff3(c1) as select count(*) from CutOff3[range 10 minutes slide 5 minutes];",

    // Count of failed trades -- refreshed every 5 minutes
    "register view TotalFailed3(c1) as select count(*) from Failed3[range 10 minutes slide 5 minutes];",

    ////////////////// FOR THE SAKE OF NAMED ATTRIBUTES //////////////////////
    
    "register view Req1(total, totalFailed, buy, sell, exchg, trade_noack, trade_recvd, trade_processing) as IStream(select * from TotalReq1);",
    "register view Req2(tickerSymbol, symbolCount) as IStream(select tradeSymbol, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeSymbol);",
    "register view Req3(totalCount, failedCount) as IStream(select t.c1, t.c1-f.c1 from TotalTrades as t, TotalFineTrades as f where f.c1*100 < 95*t.c1);",
    "register view Req4(totalCount, failedCount) as IStream(select t.c1, f.c1 from TotalCutOff3 as t, TotalFailed3 as f where f.c1*100 > 10*t.c1);",


  };

  private static final String[] queries = { 

    // Testing

    "select * from TotalFailuresByTypes;",
    "select * from TotalFailuresByStatus;",
    "select * from TotalFailuresByStatusType;",
    "select * from TotalFailedByStatus;",
    "select * from FailedTradesStatus;",
    "select * from TotalFailedStatus12;",
    "select * from TotalFailedStatus0;",

    // Requirement 1
    "select * from Req1;",

    // Requirement 2
    "select * from Req2;",

    // Requirement 3
    "select * from Req3;",

    // Requirement 4
    "select * from Req4;",

  };

  private static String[] destinations = {
    
    // Testing 
    "outcsfbq1.txt",
    "outcsfbq2.txt",
    "outcsfbq3.txt",
    "outcsfbq4.txt",
    "outcsfbq5.txt",
    "outcsfbq6.txt",
    "outcsfbq7.txt",

    // The real ones
    "outcsfbr1.txt",
    "outcsfbr2.txt",
    "outcsfbr3.txt",
    "outcsfbr4.txt",
  };

  //getXMLPlan gets the XMLDump of the Physical plan and dumps it to a file.
  public static void getXMLPlan(QueryManager queryMgr)
  {
    String s = queryMgr.getXMLPlan();
    PrintWriter xml = null;
    try {
      xml = new PrintWriter("/tmp/csfb2.xml");
      xml.append(s);
      xml.flush();
    }
    catch (IOException e) {
      System.out.println("problem with dumping xml");
    }
    finally {
      if(xml != null)
        xml.close();
    }
  }

  public static void getXMLPlan2(QueryManager queryMgr) throws CEPException
  {
    String s = queryMgr.getXMLPlan2();
    PrintWriter xml = null;
    try {
      xml = new PrintWriter("/tmp/cmdint2_viz.xml");
      xml.append(s);
      xml.flush();
    }
    catch (IOException e) {
      System.out.println("problem with dumping xml");
    }
    finally {
      if(xml != null)
        xml.close();
    }
  }


  public static void main(String[] args) throws CEPException, IOException
  {
    FileInputStream fis    = new FileInputStream("cep1.config");
    Properties      config = new Properties();
    config.load(fis);
    
/* TODO rewrite with ApplicationContext    
    CEPManager.init(config);
    CEPManager.seed();
    TableManager tableMgr = CEPManager.getTableMgr();
    QueryManager queryMgr = CEPManager.getQueryMgr();
    CommandInterpreter cmd = CEPManager.getCmdInt();
    Scheduler sched = CEPManager.getSched();
    Command c = CEPManager.getCmd();

    var = args[0];

    // Add streams, relations
    for (int i = 0; i < tables.length; i++) {
      // Add table
      c.setCql(tables[i]);
      cmd.execute(c);
      System.out.println("Executed " + "\"" + tables[i] + "\"");
      if (c.isBSuccess())
        System.out.println("Tableid: " + c.getTableId());
      else
        System.out.println("Error: " + c.getErrorMsg());

     //pass the input files
    String sourceFile = "<EndPointReference> <Address>file://" +var + "/work/cep/inpfiles/"  + sources[i] +  "</Address> </EndPointReference>";


      // Add source
      if (tables[i].startsWith("register stream"))
        tableMgr.addStreamSource(tablenames[i], sourceFile);
      else
        tableMgr.addRelationSource(tablenames[i], sourceFile);
    }

    // Add views
    for (int i = 0; i < views.length; i++) {
      // Add view
      c.setCql(views[i]);
      cmd.execute(c);
      System.out.println("Executed " + "\"" + views[i] + "\"");
      if (c.isBSuccess())
        System.out.println("Tableid: " + c.getViewId());
      else
        System.out.println("Error: " + c.getErrorMsg());
    }

    // Add the queries
    for (int i = 0; i < queries.length; i++) {
      // Add query
      c.setCql(queries[i]);
      cmd.execute(c);

      System.out.println("Executed " + "\"" + queries[i] + "\"");
      if (c.isBSuccess())
        System.out.println("Queryid: " + c.getQueryId());
      else
        System.out.println("Error: " + c.getErrorMsg());

      // Add destination
 String destinationFile ="<EndPointReference> <Address>file://" +var + "/work/cep/outfiles/" + destinations[i] + "</Address></EndPointReference>";

      queryMgr.addQueryDestination(c.getQueryId(), destinationFile);

      // Start query
      try {
        queryMgr.startQuery(c.getQueryId());
      }
      finally { 
        getXMLPlan(queryMgr);
      }
    }

    getXMLPlan2(queryMgr);

    // Run the scheduler for some time
    sched.run(100);
*/    
  }

}

