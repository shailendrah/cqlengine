package oracle.cep.test.csfb;
/* $Header: CSFB.java 25-oct-2006.23:13:09 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      10/25/06 - integrate with mds
    bisong      09/01/06 - change inp/outfiles directory
    anasrini    06/02/06 - Creation
    anasrini    06/02/06 - Creation
    anasrini    06/02/06 - Creation
 */

/**
 *  @version $Header: CSFB.java 25-oct-2006.23:13:09 najain Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */
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

public class CSFB {

    private static String var = null;

  // Table names
  private static final String[] tablenames = { 
    "TradeInputs", "TradeUpdates", "TradeMatched"
  };

  // Tables 
  private static final String[] tables = {
    "register stream TradeInputs(tradeId integer, tradeVolume integer);",
    "register stream TradeUpdates(tradeId integer, statusCode integer);",
    "register stream TradeMatched(tradeId integer);"
  };

  // Sources for tables
  private static  String[] sources      = {
    "inpTI.txt",
    "inpTU.txt",
    "inpTM.txt"
  };

  // Views 
  private static final String[] views = {
    "register view CutOffTrades(tradeId, tradeVolume) as DStream(select * from TradeInputs[range 200 seconds]);",
    "register view FineTrades(tradeId) as IStream(select t.tradeId from CutOffTrades[now] as c, TradeUpdates[range 200 seconds] as t where c.tradeId = t.tradeId group by t.tradeId);",
    "register view TotalTrades(c1) as select count(*) from CutOffTrades[range 1 hour];",
    "register view TotalFineTrades(c1) as select count(*) from FineTrades[range 1 hour];"
  };

  private static final String[] queries = { 
    //    "select * from CutOffTrades;",
    //    "IStream(select t.tradeId from CutOffTrades[now] as c, TradeUpdates[range 200 seconds] as t where c.tradeId = t.tradeId group by t.tradeId);",
    //    "select * from FineTrades;"
    //    "select count(*) from CutOffTrades[range 1 hour];",
    "select * from TotalTrades;",
    //    "select count(*) from FineTrades[range 1 hour];"
    "select * from TotalFineTrades;",
    "select t.c1, f.c1 from TotalTrades as t, TotalFineTrades as f where f.c1*100 < 95*t.c1;",
    "IStream(select t.c1, f.c1 from TotalTrades as t, TotalFineTrades as f where f.c1*100 < 95*t.c1);",
  };

  private static String[] destinations = {
    "outcsfbtt.txt",
    "outcsfbtf.txt",
    "outcsfb1.txt",
    "outcsfb2.txt",
  };

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
      queryMgr.startQuery(c.getQueryId());
    }

    // Run the scheduler for some time
    sched.run(100);
*/    
  }

}
