package oracle.cep.test.visualizer;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Properties;

import oracle.cep.memmgr.FactoryManager;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.TableManager;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.CEPManager;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.execution.scheduler.Scheduler;
import oracle.cep.execution.scheduler.RoundRobinScheduler;

public class VisDemo
{
  // Table names
  private static final String[] tablenames = { "S0" };
  // Tables 
  private static final String[] tables = {
    "register stream S0 (c1 integer, c2 float);"
    };

  // Sources for tables
  private static final String[] sources      = {
    "<EndPointReference> <Address>file:///tmp/inpS0.txt</Address> </EndPointReference>"
    };


  // Views 
  private static final String[] views = {
    "register view v0 (c1 integer, c2 float) as select * from S0 where c1 > 10;"
  };

  // Functions
  private static final String[] fns = {
  };

  private static String[] queries      = { 
"select (c1+10) from v0 [range 10 slide 5];",
"select max(c1) from v0 [rows 10];"
 };

  private static String[] destinations = {
      "<EndPointReference> <Address>file:///tmp/outVisDemo1.txt</Address> </EndPointReference>",
      "<EndPointReference> <Address>file:///tmp/outVisDemo2.txt</Address> </EndPointReference>"
};


  // DDLs
  private static final String[] ddls = {
  };
 
  // drop DDLs 
  private static final String[] drop_ddls = {
  };

  private static final String[] test_queries      = { 
  };

  private static final String[] test_destinations      = { 
  };


  //getXMLPlan gets the XMLDump of the Physical plan and dumps it to a file.
  public static void getXMLPlan(QueryManager queryMgr)
  {
    String s = queryMgr.getXMLPlan();
    PrintWriter xml = null;
    try {
      xml = new PrintWriter("/tmp/cmdint2_xml_dump1.xml");
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
/* TODO fix with ApplicationContext    
    CEPManager.init(config);
    CEPManager.seed();
    TableManager tableMgr = CEPManager.getTableMgr();
    QueryManager queryMgr = CEPManager.getQueryMgr();
    CommandInterpreter cmd = CEPManager.getCmdInt();
   // Scheduler sched = CEPManager.getSched();
    Command c = CEPManager.getCmd();
    
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

      // Add source
      if ( (tables[i].startsWith("register stream")) ||
           (tables[i].startsWith("create stream"))
         )
        tableMgr.addStreamSource(tablenames[i], sources[i]);
      else
        tableMgr.addRelationSource(tablenames[i], sources[i]);
    }

    // Add views
    for (int i = 0; i < views.length; i++) {
      // Add view
      c.setCql(views[i]);
      cmd.execute(c);
      System.out.println("Executed " + "\"" + views[i] + "\"");
      if (c.isBSuccess())
        System.out.println("View id: " + c.getViewId());
      else
        System.out.println("Error: " + c.getErrorMsg());
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
      try
      {
        cmd.execute(c);
        
        System.out.println("Executed " + "\"" + queries[i] + "\"");
        if (c.isBSuccess())
          System.out.println("Queryid: " + c.getQueryId());
        else
          System.out.println("Error: " + c.getErrorMsg());

        // Add destination
        queryMgr.addQueryDestination(c.getQueryId(), destinations[i]);

        // Start query
        queryMgr.startQuery(c.getQueryId());
      }
      catch(MetadataException e)
      {
        if(e.getErrorCode() != MetadataError.QUERY_ALREADY_EXISTS) 
          throw (e);
        else
        {
          System.out.println("Executed " + "\"" + queries[i] + "\"");
          if (c.isBSuccess())
            System.out.println("Queryid: " + c.getQueryId());
          else
            System.out.println("Error: " + c.getErrorMsg());

          // Add destination
          queryMgr.addQueryDestination(c.getQueryId(), destinations[i]);

          // Start query
          queryMgr.startQuery(c.getQueryId());
        }
          
      }
 
    }

    // Add ddls
    for (int i = 0; i < ddls.length; i++) {

      c.setCql( ddls[i]);

      cmd.execute( c);
        
      System.out.println( "Executed " + "\"" + ddls[i] + "\"");

      if (c.isBSuccess()) {
        System.out.println( "Success");
      }
      else
        System.out.println( "Error: " + c.getErrorMsg());
    }

    getXMLPlan2(queryMgr);

    // Run the scheduler for some time
//    sched.run(100);

    // drop DDLs
    for (int i = 0; i < drop_ddls.length; i++) {

      c.setCql( drop_ddls[i]);
      cmd.execute( c);

      System.out.println("Executed " + "\"" + drop_ddls[i] + "\"");
      if (c.isBSuccess())
        System.out.println("Success");
      else
        System.out.println("Error");
    }

    FactoryManagerContext ctx = 
      new FactoryManagerContext(FactoryManagerContext.ELEMENT_FACTORY_ID);

    // Set the element storage alloc
    ElementFactory e = (ElementFactory)FactoryManager.get(ctx);
    
    System.out.println("Number of elements in the list: " + e.numElements());

    ctx.setId(FactoryManagerContext.TIMESTAMP_FACTORY_ID);

    // Set the element storage alloc
    TimeStampFactory f = (TimeStampFactory)FactoryManager.get(ctx);
    
    System.out.println("Number of timestamps in the list: " + f.numElements());

    FactoryManager.initialize();
    while (true)
    {
      StorageElementFactory sf = FactoryManager.getNext();
      if (sf == null)
	break;

      System.out.println("Number of elems in the list: " + sf.numElements());
    }
*/    
  }
}
