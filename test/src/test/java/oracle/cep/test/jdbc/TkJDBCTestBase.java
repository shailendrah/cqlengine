/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTestBase.java /main/7 2011/04/27 18:37:35 apiper Exp $ */

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
    hopark    03/01/09 - use TkJDBCTestBase
    sbishnoi  09/09/08 - changing url structure
    hopark    05/19/08 - change rmi port
    hopark    03/28/08 - 
    mthatte   11/06/07 - using Constants.SCHEMA
    mthatte   10/01/07 - 
 mthatte     08/16/07 - 
 mthatte     09/06/07 - 
 sbishnoi    05/23/07 - 
 parujain    05/09/07 - 
 najain      04/25/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTestBase.java /main/6 2010/10/05 12:03:21 hopark Exp $
 *  @author najain
 *  @since release specific (what release of product did this appear in)
 */


/**
 * A sample jdbc program. A very simple test with integer datatypes. New
 * tests can be modelled on it.
 *
 * @author najain
 */
package oracle.cep.test.jdbc;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.JavaLoggerFactory;
import oracle.cep.service.CEPManager;
import oracle.cep.util.DebugUtil;
import oracle.cep.util.HeapDump;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;


public abstract class TkJDBCTestBase implements Runnable {
  protected String serviceName = null;
  protected boolean singletask = false;
  protected String workFolder = null;
  protected Connection con = null;
  protected Statement stmt = null;
  protected String contextFile = null;
  protected String hostName = null;
  protected boolean dumpmem = false;
  protected PrintStream output = null;
  protected String wlevs_home = null;
  protected boolean isLocal = false;
  public static String OUTPUT_FILE_PREFIX = "out_S_JDBC_";
  protected String testOutputDir;

  public void setTestOutputDirectory(String testOutputFile) {
    this.testOutputDir = testOutputFile;
  }


  public TkJDBCTestBase() {
  }

  public TkJDBCTestBase(String serviceName) {
    this.serviceName = serviceName;
  }

  public void setIsLocal(boolean isLocal) {
    this.isLocal = isLocal;
  }

  public void init(String[] args) {
    int i = 0;
    while (i < args.length) {
      String arg = args[i++];
      if (arg.length() == 0)
        continue;
      if (arg.equals("-single")) {
        singletask = true;
      } else if (arg.equals("-work")) {
        workFolder = args[i++];
      } else if (arg.equals("-home")) {
        wlevs_home = args[i++];
      } else if (arg.equals("-host")) {
        hostName = args[i++];
      } else if (arg.equals("-dumpmem")) {
        dumpmem = true;
      } else {
        contextFile = arg;
      }
    }

    if (workFolder == null) {
      workFolder = System.getProperty("twork");
      if (workFolder == null) {
        workFolder = System.getenv("T_WORK");
      }
    }

    if (testOutputDir == null) {
      testOutputDir = workFolder + "/cep/log";
    }

    if (contextFile == null) {
      contextFile = workFolder + "/cep/ApplicationContext.xml";
    }

    // Deal with windows nastiness
    contextFile = "file://" + (contextFile.startsWith("/") ? "" : "/") + contextFile;

    if (hostName == null) {
      try {
        hostName = InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
      }
    }
  }

  public void run() {
    //default logger factory for unit tests
    LogUtil.setLoggerFactory(new JavaLoggerFactory());
    ApplicationContext appContext;
    if (singletask) {
      try {
        appContext = new FileSystemXmlApplicationContext(contextFile);
      } catch (BeansException e) {
        throw e;
      }
    }

    try {
      // Load the JDBC-ODBC bridge
      Class.forName("oracle.cep.jdbc.CEPDriver");

      // specify the JDBC data source's URL
      String url = "jdbc:oracle:cep:@" + hostName + ":" + Constants.DEFAULT_JDBC_PORT;

      if (singletask || isLocal)
        url = Constants.CEP_LOCAL_URL + ":@" + hostName + ":" + Constants.DEFAULT_JDBC_PORT;

      if (serviceName != null) {
        url += ":" + serviceName;
      }
      //System.out.println("Connecting to " + url);

      // connect
      con = DriverManager.getConnection(url, "system", "oracle");
      stmt = con.createStatement();

      runTest();
    } catch (java.lang.Exception ex) {
      ex.printStackTrace();
      return;
    }

    try {
      // close statement and connection
      stmt.close();
      con.close();
    } catch (java.lang.Exception ex) {
      ex.printStackTrace();
    }

    stmt = null;
    con = null;

    if (singletask && dumpmem) {
      DebugUtil.invokeGC();

      String dumpFile = workFolder + File.separator + "tkjdbc.hprof";
      System.out.println("**** dumping heap to " + dumpFile);
      HeapDump.dumpHeap(dumpFile);
      System.out.println("**** heap dumped to " + dumpFile);
    }
  }

  protected void openOutputFile(String outfile) {
    try {
      output = new PrintStream(new FileOutputStream(outfile));
    } catch (Exception e) {
      System.out.println("Could not create file!");
      return;
    }
  }

  protected void printException(Exception e) {
    if (output != null) {
      Throwable c = e;
      do {
        output.print(c != null ? c.getClass().getName() : "");
        if (c instanceof CEPException) {
          CEPException ce = (CEPException) c;
          output.print("(" + ce.getErrorCode() + ")");
        }
        c = c.getCause();
        if (c != null) {
          output.print(" ");
        }
      } while (c != null);
      output.println("");
    }
  }

  protected void runddl(String ddl) {
    if (output != null)
      output.println(ddl);
    try {
      stmt.executeUpdate(ddl);
    } catch (Exception e) {
      printException(e);
      return;
    }
    if (output != null)
      output.println("ok");
  }


  protected void runpddl(PreparedStatement s) {
    try {
      s.executeUpdate();
    } catch (Exception e) {
      printException(e);
      return;
    }
    if (output != null)
      output.println("ok");
  }

  protected void exit() throws Exception {
    if (singletask) {
      CEPManager cepMgr = CEPManager.getInstance();
      LogUtil.info(LoggerType.TRACE, "Closing...");
      try {
        cepMgr.close();
      } catch (Exception e) {
        LogUtil.severe(LoggerType.TRACE, "Exception in close\n" + e.toString());
      }
      LogUtil.info(LoggerType.TRACE, "Exiting...");
      System.exit(1);
    }
  }

  protected abstract void runTest() throws Exception;

  protected String getFileNamePrefix() {
    return testOutputDir + "/" + OUTPUT_FILE_PREFIX +
        (singletask ? "s" : "");
  }

  protected String getFileName(String name, String ext) {
    return getFileNamePrefix() + name + ext;
  }

  protected String getFileName(String name) {
    return getFileName(name, ".txt");
  }

  protected String getFileDest(String name) {
    return getFileDest(name, ".txt");
  }

  protected String getFileDest(String name, String ext) {
    String fname = getFileName(name, ext);
    return "\"<EndPointReference><Address>file://" + (fname.startsWith("/") ? "" : "/")
        + fname.replace('\\', '/') +
        "</Address></EndPointReference>\"";
  }
}

