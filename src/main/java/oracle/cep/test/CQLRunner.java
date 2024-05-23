/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/CQLRunner.java /main/11 2010/05/27 09:44:55 parujain Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
All rights reserved. */
/* /*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/21/10 - remove drop schema ddl
    hopark      06/11/09 - add refcount dump
    hopark      05/07/09 - read utf-8
    hopark      02/18/09 - add heapdump
    anasrini    02/12/09 - set isRegressPushMode
    parujain    01/29/09 - transaction mgmt
    hopark      11/19/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/CQLRunner.java /main/11 2010/05/27 09:44:55 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.server.CEPServer;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.service.CEPLoadParser;
import oracle.cep.util.CSVUtil;
import oracle.cep.util.DebugUtil;
import oracle.cep.util.HeapDump;
import oracle.cep.util.PathUtil;
import oracle.cep.util.StringUtil;
import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.PagedFactory;
import oracle.cep.memmgr.factory.memory.TuplePtrFactory;
import oracle.cep.memmgr.factory.paged.TupleFactory;
import oracle.cep.metadata.ConfigManager;

public class CQLRunner
{
  private CEPManager         m_cepMgr;
  private ExecContext        m_execContext = null;
  private String             m_testRule;
  private boolean            m_exit = true;
  private boolean            m_cleanup = true;
  private boolean            m_dumpPlan = false;
  private boolean            m_heapdump = false;
  private boolean            m_meminfo = false;
  private List<String>       m_cqlxFiles;
  private String             m_testData;
  private String             m_testOutput;
  private boolean            m_useSysService = false;
  private String             m_scaleSize;
  private String             m_scaleRate;
  private String             m_scaleRange;
  private String             m_inputDataFile;
  private String             m_outputFile;
  private String             m_lrbkitSource;
  private String             m_lrbkitDest1;
  private String             m_lrbkitDest2;
  private String             m_lrbkitDest3;
  private String             m_lrbkitDuration;
  
  public void setExecContext(ExecContext ec) {m_execContext = ec;}
  public void setUseSysService(boolean b) {m_useSysService = b;}
  public void setTestDataFolder(String s) { m_testData = s; }
  public void setTestOutputFolder(String s) { m_testOutput = s; }


  public void setTestRule(String s) { m_testRule = s; }
  public String getTestRule() { return m_testRule; }
  
  public CQLRunner(CEPManager cep)
  {
  	this.m_cepMgr = cep;
  }
  
  public int init()
    throws Exception
  {
    try
    {
      m_scaleSize = System.getProperty("scale.size", "");
      m_scaleRate = System.getProperty("scale.rate", "");
      m_scaleRange = System.getProperty("scale.range", "");
      m_inputDataFile = System.getProperty("inpf.name", "");
      m_outputFile = System.getProperty("outf.name", "");
      m_lrbkitSource = System.getProperty("lrbkit.source", "");
      m_lrbkitDest1 = System.getProperty("lrbkit.dest1", "");
      m_lrbkitDest2 = System.getProperty("lrbkit.dest2", "");
      m_lrbkitDest3 = System.getProperty("lrbkit.dest3", "");
      m_lrbkitDuration = System.getProperty("lrbkit.duration", "");
      
      m_cqlxFiles = new LinkedList<String>();
      parseRule(m_testRule);    
      String testRule1 = System.getProperty("TEST_RULE", "");
      if (testRule1 == null || testRule1.length() == 0)
      {
        testRule1 = System.getenv("TEST_RULE");
      }
      if (testRule1 != null && testRule1.length() > 0)
      {
        parseRule(testRule1);    
      }
      ConfigManager configMgr = m_cepMgr.getConfigMgr();
      configMgr.setSchedOnNewThread(false);
      configMgr.setSchedRuntime(Constants.DEFAULT_RUN_TIME);
      String scheduler = configMgr.getSchedulerClassName();

      // If this test is in directInterop mode, then request for
      // push mode emulation
      boolean isDirectInterop = configMgr.getDirectInterop();
      if (isDirectInterop)
        configMgr.setIsRegressPushMode(true);

      configMgr.dump();
      if (m_execContext == null)
      {
      	if (m_useSysService)
      	{
          LogUtil.info(LoggerType.TRACE, "Use sys service");
          m_execContext = m_cepMgr.getSystemExecContext();
      	}
      	else
      	{
          CEPServerRegistryImpl svrReg = m_cepMgr.getServerRegistry();
          int hc = hashCode();
          if (hc < 0) hc = -hc;
          String svc = "s" + Integer.toString(hc);
          CEPServer server = svrReg.createServer(svc);
          LogUtil.info(LoggerType.TRACE, "Create new service for runner : " + svc);
          m_execContext = server.getExecContext();
        }
     } 
     else
     {
        LogUtil.info(LoggerType.TRACE, "Stop scheduler for : " + m_execContext.getServiceName());
     	m_execContext.stopScheduler();
     }
      
      LogUtil.info(LoggerType.TRACE, m_cqlxFiles.size() + " tests to run.");
      System.out.println(m_cqlxFiles.size() + " tests to run.");
      int tests = 0;
      for (String cqlxFile : m_cqlxFiles)
      {  
        if (cqlxFile.length() > 0)
        {
          configMgr.setSchedTimeSlice(Constants.DEFAULT_SCHED_TIME_SLICE);
          configMgr.setSchedRuntime(Constants.DEFAULT_RUN_TIME);
          configMgr.setSchedulerClassName(scheduler);
          runCqlx(m_execContext, cqlxFile);
          tests++;
        }
      }
      LogUtil.info(LoggerType.TRACE, tests + " tests completed...");
      if (m_exit) 
      {
        LogUtil.info(LoggerType.TRACE, "Closing...");
        try
        {
          m_cepMgr.close();
        }
        catch(Exception e)
        {
          LogUtil.severe(LoggerType.TRACE, "Exception in close\n" + e.toString());
        }
        LogUtil.info(LoggerType.TRACE, "Exiting...");
        System.exit(1);
      }
      return tests;
    } catch (Exception ex) {
      LogUtil.logStackTrace(ex);
      throw(ex);
    }
  }

  public ExecContext getExecContext() 
  {
      return m_execContext;
  }
  
  public void runCqlx(ExecContext ec, String fileName)
  {
    LogUtil.info(LoggerType.TRACE, "================================================");
    HashMap<String, String> valMap = new HashMap<String, String>();
    valMap.put("TEST_DATA", m_testData);
    valMap.put("TEST_OUTPUT", m_testOutput);

    //Special handling for scalability tests
    valMap.put("NUM_MSGS", m_scaleSize);
    valMap.put("MSGS_PER_SEC", m_scaleRate);
    valMap.put("RANGE", m_scaleRange);
    //Special handling for LRBKit
    valMap.put("DATAFILE", m_inputDataFile);
    valMap.put("OUTPUTFILE", m_outputFile);
    if (m_lrbkitSource.isEmpty() && !m_inputDataFile.isEmpty()) {
		m_lrbkitSource="\"<EndPointReference><Address>file://"+m_testData+ "/" + m_inputDataFile +"</Address></EndPointReference>\"";
		m_lrbkitDest1 = "file://"+m_testOutput+"/"+m_outputFile+".txt";
		m_lrbkitDest2 = "file://"+m_testOutput+"/"+m_outputFile+"_1.txt";
		m_lrbkitDest3 = "file://"+m_testOutput+"/"+m_outputFile+"_2.txt";
    }
    valMap.put("SOURCE", m_lrbkitSource);
    valMap.put("DEST1", m_lrbkitDest1);
    valMap.put("DEST2", m_lrbkitDest2);
    valMap.put("DEST3", m_lrbkitDest3);
    if (m_lrbkitDuration.isEmpty()) {
    	m_lrbkitDuration = "duration=1000000";
    }
    valMap.put("DURATION", m_lrbkitDuration);

    StringBuilder text = new StringBuilder();
    try
    {
      Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
      int ch;
      while ((ch = reader.read()) > -1)
      {
        text.append((char) ch);
      }
      reader.close();
    }
    catch (Exception e)
    {
      String msg = "FAILED: IO Error while reading " + fileName + ": " + e.getMessage() + "\n" 
      + e.toString();
      LogUtil.severe(LoggerType.TRACE, msg);
      System.out.println(msg);
      return;
    }
    String cqlxml = StringUtil.expand(text.toString(), valMap);

    System.out.println("**** Running " + fileName);
    LogUtil.info(LoggerType.TRACE, "**** Running " + fileName);
    String schemaName = PathUtil.getFileName(fileName);
    int pos = schemaName.indexOf('.');
    if (pos > 0)
      schemaName = schemaName.substring(0, pos);

    String minfo0 = null;
    if (m_meminfo || m_heapdump)
      DebugUtil.invokeGC();
    if (m_heapdump)
    {
      dumpHeap(schemaName, "0");
    }
    if (m_meminfo)
    {
      minfo0 = DebugUtil.dumpMemoryPools();
    }
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); 
    long lstarttime = System.currentTimeMillis();

    runCqlx(ec, schemaName, cqlxml);
    
    long lendtime = System.currentTimeMillis();
    long difftime = lendtime - lstarttime;
    LogUtil.info(LoggerType.TRACE, "**** Completed : " + fileName + " in " + timeStr(difftime));
    if (m_meminfo || m_heapdump)
      DebugUtil.invokeGC();
    if (m_meminfo)
    {
      String minfo = DebugUtil.dumpMemoryPools();
      LogUtil.info(LoggerType.TRACE, "**** MemInfo : before ****");
      LogUtil.info(LoggerType.TRACE, minfo0);
      LogUtil.info(LoggerType.TRACE, "**** MemInfo : after ****");
      LogUtil.info(LoggerType.TRACE, minfo);
    }
    if (m_heapdump)
    {
      dumpHeap(schemaName, "1");
    }
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
        int cnt = 0;
        String filename = m_testOutput + File.separator + schemaName + "_rcsummary.log";
        try
        {
          FileWriter fw = new FileWriter(filename);
          PrintWriter pw = new PrintWriter(fw);
  
          CEPManager cepMgr = ec.getServiceManager();
          FactoryManager factoryMgr = cepMgr.getFactoryManager();
          Iterator<IAllocator> facItr = factoryMgr.getIterator();
          while (facItr.hasNext())
          {
            IAllocator sf = facItr.next();
            if (sf instanceof TuplePtrFactory)
            {
              TuplePtrFactory fac = (TuplePtrFactory) sf;
              int t = fac.dumpRefCount(pw);
              if (t > 0) cnt++;
              fac.clearRefCount();
            }
            else
            if (sf instanceof TupleFactory)
            {
              TupleFactory fac = (TupleFactory) sf;
              int t = fac.dumpRefCount(pw);
              if (t > 0) cnt++;
              fac.clearRefCount();
            }
          }       
          pw.close();
          fw.close();
        }catch (Exception e)
        {
          //System.out.println(e.toString());
          LogUtil.severe(LoggerType.TRACE, e.toString());
        }
        if (cnt == 0)
        {
          File f = new File(filename);
          f.delete();
        }
        else
        {
          System.out.println("Ref count summary : " + filename);
        }
    }
  }

  private void dumpHeap(String schemaName, String postfix)
  {
    String work = ConfigManager.getWorkFolder();
    String dumpFile = work + File.separator + schemaName+"_"+postfix+".hprof";
    System.out.println("**** dumping heap to " + dumpFile);
    HeapDump.dumpHeap(dumpFile);
    System.out.println("**** heap dumped to " + dumpFile);
  }

  protected String timeStr(long tm)
  {
    long s = tm / 1000;
    long m = s / 60;
    long h = m / 60;
    s = s % 60;
    m = m % 60;
    tm = tm % 1000;
    return h + ":" + m + ":" + s + "." + tm;
  }
 
  public void runCqlx(ExecContext ec, String schemaName, String cqlxml)
  {
    try
    {
      CEPLoadParser loadParser = new CEPLoadParser();
      boolean b = loadParser.parseStr(cqlxml, false /* retry on validation */);
      if (!b)
      {
        LogUtil.severe(LoggerType.TRACE, "Failed to parse\n" + cqlxml); 
        return;
      }
      runCqlxLoad(ec, schemaName, loadParser);      
    }
    catch (CEPException e)
    {
      LogUtil.severe(LoggerType.CUSTOMER, "Failed to run\n" + e.toString() + cqlxml);
    }
    
  }
    
  private void runCqlxLoad(ExecContext ec, String schemaName, CEPLoadParser loadParser)
    throws CEPException
  {
    ec.setSchema(schemaName);
    
    List<String> ddls = loadParser.getLoadDDLs();
/*
    try {
      FileOutputStream fos = new FileOutputStream("/tmp/test.xml");
      Writer out = new OutputStreamWriter(fos, "UTF8");
      for (String l : ddls)
      {
        out.write(l);
        out.write("\n");
      }
      out.close();
  } catch (IOException e) {
      e.printStackTrace();
  }
*/  
    for (String ddl : ddls)
    {
      ec.executeDDL(ddl, false);
    }

    if (m_dumpPlan)
    {
      PrintWriter xml = null;
      try
      {
        String view_root = null;;
        view_root = System.getenv("T_WORK");
        String dumpFile = "/tmp/XMLVisDump.xml";
        if(view_root != null)
        {
          String actualFile = null;
          actualFile = schemaName + "_dump.xml";
          dumpFile = view_root + "/cep/" + actualFile;
        }
        System.out.println("Dumping plan to " + dumpFile);
        ec.setTransaction(ec.getTransactionMgr().begin());
        String s = ec.getQueryMgr().getXMLPlan2();
        ec.getTransactionMgr().commit(ec.getTransaction());
        xml = new PrintWriter(dumpFile);
        xml.append(s);
        xml.flush();
      }
      catch (Exception e)
      {
        System.out.println(e.toString());
        ec.getTransactionMgr().rollback(ec.getTransaction());
        LogUtil.finest(LoggerType.TRACE, "problem with dumping xml\n" + e.toString());
      }
      finally
      {
    	ec.setTransaction(null);
        if (xml != null)
          xml.close();
      }
    }

    if (m_cleanup)
    {
      String dropddl = "drop schema " + schemaName;
      //System.out.println(dropddl);
      ec.dropSchema(schemaName, true);
    }
  }
  
  
  private void parseRule(String rule)
  {
    List<String> args = null;
    String folder = null;
    String listFile = null;
    String cqlFolder = null;    
      
    LogUtil.info(LoggerType.TRACE, "TEST_ARG=" + rule);
    try 
    {
      args = CSVUtil.parseStr(rule);
      if (args.size() == 0)
      {
        LogUtil.severe(LoggerType.TRACE, "Invalid line : " + rule);
        return;
      }
    } catch(CEPException e)
    {
      LogUtil.severe(LoggerType.TRACE, "Invalid line : " + rule);
      return;
    }
    for (String arg : args)
    {
      String nv[] = arg.split("=");
      if (nv == null || nv.length < 2) continue;
      String cmd = nv[0];
      String val = nv[1];
      if (cmd.equals("cleanup")) 
      {
        m_cleanup=val.equals("true");
        LogUtil.info(LoggerType.TRACE, "cleanup=" + m_cleanup);
      } else if (cmd.equals("cqlx"))
      {
        /* Single cqlx */
        m_cqlxFiles.add(val);
      } else if (cmd.equals("folder"))
      {
        folder = val;
      } else if (cmd.equals("listfile"))
      {
        listFile = val;
        LogUtil.info(LoggerType.TRACE, "listfile=" + listFile);
      } else if (cmd.equals("cqlfolder"))
      {
        cqlFolder = val;
        LogUtil.info(LoggerType.TRACE, "cqlfolder=" + cqlFolder);
      } else if (cmd.equals("dumpplan"))
      {
        m_dumpPlan=val.equals("true");
        LogUtil.info(LoggerType.TRACE, "dumpplan=" + m_dumpPlan);
      } else if (cmd.equals("meminfo"))
      {
        m_meminfo=val.equals("true");
        LogUtil.info(LoggerType.TRACE, "meminfo=" + m_meminfo);
      } else if (cmd.equals("heapdump"))
      {
        m_heapdump=val.equals("true");
        LogUtil.info(LoggerType.TRACE, "heapdump=" + m_heapdump);
      } else if (cmd.equals("usesys"))
      {
        m_useSysService=val.equals("true");
        LogUtil.info(LoggerType.TRACE, "usesys=" + m_useSysService);
      } else if (cmd.equals("exit"))
      {
        m_exit=val.equals("true"); 
        LogUtil.info(LoggerType.TRACE, "exit=" + m_exit);
      } else if (cmd.equals("testdata"))
      {
        m_testData = val;
        LogUtil.info(LoggerType.TRACE, "testdata=" + m_testData);
      } else if (cmd.equals("testoutput"))
      {
        m_testOutput = val;
        LogUtil.info(LoggerType.TRACE, "testoutput=" + m_testOutput);
      } else {
          LogUtil.severe(LoggerType.TRACE, "Invalid command : " + cmd);
      }
    }
    if (folder != null)
    {
        /* all cqlx in folder*/
        File cqlxpath = new File(folder);
        String[] fileNames = cqlxpath.list();
        if (fileNames != null)
        {
          for (String filename: fileNames)
          {
            String ext = PathUtil.getExtension(filename);
            if (ext != null && ext.equals("cqlx"))
            {
              String path = folder + File.separator + filename;
              m_cqlxFiles.add(path);
            }
          }
        }
    }
    if (listFile != null)
    {
        /* cqlx files in a listfile*/
        HashMap<String, String> valMap = new HashMap<String, String>();
        valMap.put("TEST_DATA", m_testData);
        valMap.put("TEST_OUTPUT", m_testOutput);
        LogUtil.info(LoggerType.TRACE, "reading " + listFile);
        try {
          BufferedReader in = new BufferedReader(new FileReader(listFile));
          String str;
          boolean incomment = false;
          while ((str = in.readLine()) != null) 
          {
            if (incomment)
            {
              if (str.startsWith("*/"))
              {
                incomment = false;
              }
              continue;
            }
            else
            {
              if (str.startsWith("/*"))
              {
                incomment = true;
                continue;
              }
            }
            if (!str.startsWith("#") && str.length() > 0)
            {
              String filepath = str.trim(); 
              filepath = StringUtil.expand(filepath.trim(), valMap);
                File cqlxfile = new File(filepath);
              if (!cqlxfile.isAbsolute() && cqlFolder != null)
              {
                filepath = new File(cqlFolder, filepath).toString();
              }
              m_cqlxFiles.add(filepath);
            }
          }
          in.close();
        } catch (IOException e) {
          LogUtil.severe(LoggerType.TRACE, e.toString());
          return;
        }
     }
  }
}


