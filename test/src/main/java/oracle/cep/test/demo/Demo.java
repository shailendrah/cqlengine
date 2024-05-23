package oracle.cep.test.demo;
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
 * Test for the command interpreter
 */

public class Demo
{
   private static String var = null;
 
  // Table names
  private static final String[] tablenames = {"TradeInputs", "TradeMatched"};

  // Tables 
  private static final String[] tables       = {
    "create stream TradeInputs (tradeId integer, tradeVolume int);",
    "create stream TradeMatched (tradeId integer);"
  };

  // Sources for tables
  private static String[] sources      = {
    "inpDemoS1.txt",
    "inpDemoS2.txt"
};

  // Functions
  private static final String[] fns = {
    "create function var(c1 int) return float aggregate using \"Variance\" supports incremental computation;",
    "create function stddev(c1 int) return float aggregate using \"StdDev\" supports incremental computation;",
  };

  // Queries
  private static final String[] queries      = { 
    "IStream(select i.tradeId, tradeVolume from TradeInputs [range 1 second] as i, TradeMatched [now] as m where i.tradeVolume > 50000 and i.tradeId = m.tradeId);",
    "IStream(select avg(tradeVolume), stddev(tradeVolume) from TradeInputs[range 1000] as i , TradeMatched[range 1000] as m where i.tradeId = m.tradeId);"
  };
  //  private static final String[] queries      = { 
  //    "select * from S1 [range 1], S2 [range 0] where S1.c2 > 50000;"
  //  };

  // Destinations for queries
  private static  String[] destinations = {
    "outdemo.txt",
    "outaggr.txt"
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
      if (tables[i].startsWith("create stream"))
        tableMgr.addStreamSource(tablenames[i], sourceFile);
      else
        tableMgr.addRelationSource(tablenames[i], sourceFile);
    }
 
    // Add functions
    for (int i = 0; i < fns.length; i++) {
      // Add view
      c.setCql(fns[i]);
      cmd.execute(c);
      System.out.println("Executed " + "\"" + fns[i] + "\"");
      if (c.isBSuccess())
        System.out.println("Function id: " + c.getFunctionId());
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
