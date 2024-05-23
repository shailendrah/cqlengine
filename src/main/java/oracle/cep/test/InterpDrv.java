/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle.cep.test.InterpDrv.java /main/16 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Test driver for interpreter related test cases

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/13/09 - fix npe
    parujain    01/28/09 - Txn support
    hopark      11/07/08 - activate refactor
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/24/08 - multiple schema
    hopark      03/18/08 - reorg config
    hopark      03/17/08 - set storage folder
    parujain    06/26/07 - fix activate
    parujain    03/22/07 - run Scheduler thru CPEManager
    parujain    02/15/07 - system startup
    hopark      11/27/06 - add embedded cqlx
    hopark      11/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle.cep.test.InterpDrv.java /main/16 2009/11/09 10:10:59 sborah Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test;

import java.io.File;
import java.io.IOException;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.TableManager;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.test.CQLRunner;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle.cep.test.InterpDrv.java /main/16 2009/11/09 10:10:59 sborah Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class InterpDrv
{
    private IEvictPolicy m_evictPolicy = null;
    private String m_testInputFolder = null;
    private String m_testOutputFolder = null;
    private String m_adeViewRoot = null;
    private String m_twork = null;
    
    private final String TEST_ADE_FOLDER_PROPERTY = "ade.view.root";
    private final String TEST_TWORK_PROPERTY = "twork";
    private final String TEST_INPUT_FOLDER_PROPERTY = "test.inputFolder";
    private final String TEST_OUTPUT_FOLDER_PROPERTY = "test.outputFolder";
    private final String TEST_CONFIG_PROPERTY = "test.configFile";

    private boolean m_initalized= false;
    protected CQLRunner m_runner;
    
    static InterpDrv s_driver = null;

    // Due to static functions from CEPManager (e.g init and seed),
    // We cannot maintain multiple copies of Interp driver.
    public static synchronized InterpDrv getInstance() 
    {
        if (s_driver == null)
            s_driver = new InterpDrv();
        return s_driver;
    }
    
    protected InterpDrv()
    {
    }

    public ExecContext getExecContext() {return m_runner.getExecContext();}
    public void setEvictPolicy(IEvictPolicy policy)
    {
      m_evictPolicy = policy;
    }
    
    public boolean setUp(ConfigManager cfg) throws Exception
    {
      try {
        if (m_initalized) return true;
        if (cfg == null)
          cfg = new ConfigManager();
        m_adeViewRoot = System.getProperty(TEST_ADE_FOLDER_PROPERTY, ".");
        m_twork = System.getProperty(TEST_TWORK_PROPERTY, ".");
        m_testInputFolder = System.getProperty(TEST_INPUT_FOLDER_PROPERTY, ".");
        m_testOutputFolder = System.getProperty(TEST_OUTPUT_FOLDER_PROPERTY, ".");

        /*
        if (m_adeViewRoot.length() == 0 || 
            m_twork.length() == 0 || 
            m_testInputFolder.length() == 0 || 
            m_testOutputFolder.length() == 0) {
        	throw new RuntimeException("No test folder founds in the system property");
        }
		*/
        
        CEPServerRegistryImpl reg = new CEPServerRegistryImpl();
        CEPManager cep = CEPManager.getInstance();
        cep.setConfig(cfg);
        cep.setEvictPolicy(m_evictPolicy);
        cep.setServerRegistry(reg);
        cep.init();
        reg.init(cep);
        m_runner = new CQLRunner(cep);
        m_runner.setTestRule("exit=false");
        m_runner.init(); 
        m_initalized = true;
        return true;
      }
      catch(Exception e)
      {
        throw(e);
      }
    }


    public static void cleanStorageFolder(String folder)
    {
      File sf = new File(folder);
      String[] fileNames = sf.list();
      if (fileNames != null)
      {
        for (String filename: fileNames)
        {
          String path = folder + File.separator + filename;
          boolean success = (new File(path)).delete();
          if (!success)
          {
            System.out.println("failed to delete " + path);
          }
        }
      }
    }
    
    public void tearDown()
    {
        
    }
    
    /**
     * Run cql commands.
     * 
     * @param descs
     * @throws CEPException
     */
    public void setCql(CqlDesc[] descs) throws CEPException
    {
        assert (descs != null);
        for (CqlDesc cql: descs)
        {
            try {
                cql.preprocess(this);
            } catch(IOException e) 
            {
                System.out.println("IO Error while preprocessing " + cql.getQuery());
                System.out.println(e);
            }
            cql.execute(getExecContext());
        }
    }

    /**
     * Run cql commands from a cqlx file.
     * @param fileName
     * @throws CEPException
     */
    public void setCqlXFile(String fileName) throws CEPException
    {
        m_runner.runCqlx(m_runner.getExecContext(), fileName);
    }

    /**
     * Run cql commands from cqlx.
     * @param cqlx commands in xml format.
     * @throws CEPException
     */
    public void setCqlX(String text) throws CEPException
    {
        String text0 = text.replaceAll("@ADE_VIEW_ROOT@", m_adeViewRoot);
        String text1 = text0.replaceAll("@T_WORK@", m_twork);
        String text2 = text1.replaceAll("@TEST_DATA@", m_testInputFolder);
        String text3 = text2.replaceAll("@TEST_OUTPUT@", m_testOutputFolder);
        m_runner.getExecContext().executeDDL(text3, false);
    }

    /**
     * Runs scheduler for given time.
     * 
     * @param tm
     * @throws CEPException
     */
    public void run(int tm) throws CEPException
    {
      m_runner.getExecContext().runScheduler(tm, true);
    }

    private String appendPath(String path, String path1)
    {
        return path + 
               ((path.endsWith("/") || path1.startsWith("/")) ? "" : "/") +
               path1;
    }
    
    /**
     * Creates a source file end point.
     * 
     * @param src
     * @return
     */
    private String getSrcFileEndpoint(String src)
    {
      return "<EndPointReference> <Address>file://" +
             m_testInputFolder + 
             (src.startsWith("/") ? "":"/" )+
             src +  
             "</Address> </EndPointReference>";
    }

    /**
     * Creates a destination end point.
     * 
     * @param dest
     * @return
     */
    private String getDestFileEndpoint(String dest)
    {
      return "<EndPointReference> <Address>file://" +
             m_testOutputFolder + 
             (dest.startsWith("/") ? "":"/") +
             dest +  
             "</Address> </EndPointReference>";
    }
    
    // CQL Descriptors
    public static abstract class CqlDesc
    {
        protected String    m_qry;
        private boolean     m_result;
        private Exception   m_exception;
        private String      m_errMsg;
        
        public CqlDesc(String ddl)
        {
            m_qry = ddl;
            m_result = false;
            m_exception = null;
            m_errMsg = null;
        }
        
        public boolean execute(ExecContext ec) throws CEPException
        {
            CommandInterpreter cmd = ec.getCmdInt();
            Command c = ec.getCmd(); 
            c.setCql(m_qry);
            cmd.execute(c);
            m_result = c.isBSuccess();
            
            System.out.println("Executed " + "\"" + m_qry + "\"");
            if (c.isBSuccess()) {
                String res = getResultStr(c);
                if (res == null) 
                {
                    System.out.println("Success");
                } else {
                    System.out.println(res);
                }
            } else {
                m_exception =  c.getException();
                m_errMsg = c.getErrorMsg();
                System.out.println("Error: " + c.getErrorMsg());        
            }
            return c.isBSuccess();
        }
        public String getQuery() {return m_qry;}
        
        public boolean isBSuccess() {return m_result;}
        public String getResultStr(Command c) {return m_errMsg;}
        public Exception getException() {return m_exception;}
        protected void preprocess(InterpDrv drv) throws IOException {}
    }

    // Table cql : name, ddl, source
    public static class TableDesc extends CqlDesc
    {
        String m_name;
        String m_src;

        public TableDesc(String name, String ddl, String src)
        {
            super(ddl);
            m_name = name;
            m_src = src;
        }

        public void preprocess(InterpDrv drv) throws IOException
        {
            if (!m_src.startsWith("<EndPointReference>") &&
                !m_src.startsWith("push source")) 
            {
                String srcName = m_src;
                m_src = drv.getSrcFileEndpoint(m_src);
            }
        }

        public String getName() {return m_name;}
        public String getSrc() {return m_src;}

        public String getResultStr(Command c) {
            return "Tableid: " + c.getTableId();
        }
        
        public boolean execute(ExecContext ec) throws CEPException
        {
            boolean b = super.execute(ec);
            if (b) {
                String schema = ec.getSchema();
                TableManager tableMgr = ec.getTableMgr();
                ITransaction txn = ec.getTransactionMgr().begin();
                ec.setTransaction(txn);
                // Add source
                if ((m_qry.startsWith("register stream")) ||
                    (m_qry.startsWith("create stream")))
                {
                    if (m_src.startsWith("push source"))
                        tableMgr.addStreamPushSource(m_name, schema);
                    else
                        tableMgr.addStreamSource(m_name, schema, m_src);
                } else
                {
                    if (m_src.startsWith("push source"))
                        tableMgr.addRelationPushSource(m_name, schema);
                    else
                        tableMgr.addRelationSource(m_name, schema, m_src);
                }
                txn.commit(ec);
                ec.setTransaction(null);
            }
            return b;
        }
    }

    // View cql : ddl
    public static class ViewDesc extends CqlDesc
    {
        public ViewDesc(String ddl) {super(ddl);}
    
        public String getResultStr(Command c) {
            return "View Id: " + c.getViewId();
        }
    }
    
    // Func cql : ddl
    public static class FuncDesc extends CqlDesc
    {
        public FuncDesc(String ddl) {super(ddl);}

        public String getResultStr(Command c) {
            return "Function Id: " + c.getFunctionId();
        }
    }    

    // General query cql : query, output
    public static class QueryDesc extends CqlDesc
    {
        String m_destination;

        public QueryDesc(String query, String dest)
        {
            super(query);
            m_destination = dest;
        }

        public void preprocess(InterpDrv drv) throws IOException
        {
            if (!m_destination.startsWith("<EndPointReference>") ) 
            {
                m_destination = drv.getDestFileEndpoint(m_destination);
            }
        }
        
        public String getResultStr(Command c) {
            return "Queryid: " + c.getQueryId();
        }
        
        public boolean execute(ExecContext ec) throws CEPException
        {
            boolean b = super.execute(ec);
            if (b) {
                QueryManager queryMgr = ec.getQueryMgr();
                if (m_destination != null)
                {
                    Command c = ec.getCmd(); 
                    ITransaction txn = ec.getTransactionMgr().begin();
                    ec.setTransaction(txn);
                    queryMgr.addQueryDestination(c.getQueryId(), m_destination);

                    // Start query
                    queryMgr.startQuery(c.getQueryId());
                    txn.commit(ec);
                    ec.setTransaction(null);
                }
            }
            return b;
        }
    }
}

