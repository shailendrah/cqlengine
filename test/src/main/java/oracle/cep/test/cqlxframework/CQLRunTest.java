/* $Header: cep/cqlengine/test/src/main/java/oracle.cep.test.cqlxframework/CQLRunTest.java st_pcbpel_hopark_cqlmaven/1 2011/03/28 10:00:23 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle.cep.test.cqlxframework/test/CQLRunner.java /main/11 2010/05/27 09:44:55 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.cqlxframework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.cep.server.CEPServer;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.service.CEPLoadParser;
import oracle.cep.test.cqlxframework.env.QryDestLocator;
import oracle.cep.util.DebugUtil;
import oracle.cep.util.HeapDump;
import oracle.cep.util.StringUtil;
import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.factory.memory.TuplePtrFactory;
import oracle.cep.memmgr.factory.paged.TupleFactory;
import oracle.cep.metadata.ConfigManager;

public class CQLRunTest implements Runnable {
	private static final String CONVERTTS = "convertTs";
	private static final long S_TIMEOUT = 60 * 1000L;  //60secs
	private Pattern m_queryPattern;
	private Pattern[] m_endpointPatterns;
	private Map<String, IVerifier> m_postVerifiers;
	private Map<String, IVerifier> m_onlineVerifiers;
	private long m_timeout = 0;
	private CqlxTestCase m_currentTest;
	private CQLRunner m_runner;
	private ExecContext m_execContext;
	private long m_starttime;
	private String m_memInfoBefore;
	private String m_suite;
	private ReportProgress m_progress;
	private String m_currentService;
	private boolean m_completed;
	private volatile boolean m_stop;
	private SimpleDateFormat m_df = new SimpleDateFormat();
	
	public CQLRunTest(CQLRunner cqlRunner, CqlxTestCase testsToRun) {
		m_runner = cqlRunner;
		m_currentTest= testsToRun;
		m_queryPattern = Pattern
				.compile(
						"alter\\s+query\\s+([\\p{L}_\\d]+)\\s+add\\s+destination\\s+\"(.+)\"",
						Pattern.CASE_INSENSITIVE);
		m_endpointPatterns = new Pattern[3];
		m_endpointPatterns[0] = Pattern
				.compile(
						"<EndPointReference>\\s*<Address>\\s*file://(.+)</Address>\\s*</EndPointReference>",
						Pattern.CASE_INSENSITIVE);
		m_endpointPatterns[1] = Pattern
				.compile(
						"<EndPointReference>\\s*<Address>\\s*<Type>\\s*file\\s*</Type>\\s*<FileName>\\s*(.+)\\s*</FileName>\\s*</Address>\\s*</EndPointReference>",
						Pattern.CASE_INSENSITIVE);
		m_endpointPatterns[2] = Pattern
				.compile(
						"<EndPointReference>\\s*<Address>\\s*file:(.+)</Address>\\s*</EndPointReference>",
						Pattern.CASE_INSENSITIVE);
		m_postVerifiers = new HashMap<String, IVerifier>();
		m_onlineVerifiers = new HashMap<String, IVerifier>();
	}

	public boolean start() {
	    Thread thread = new Thread(this);
	    thread.start();
	    m_completed = false;
	    m_timeout = m_runner.getTimeout();
	    if (m_timeout <= 0) m_timeout = S_TIMEOUT;
	    else m_timeout *= 1000L;
	    synchronized(this) {
	        long timeout = m_timeout;
	        while(timeout > 0) {
	            try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                }
	            timeout -= 1000;
	            if (m_completed) break;
	        }
	        m_stop = true;
	    }
	    return m_completed;
	}
	
	public void run() {
		try {
			m_suite = m_runner.getSuite().getName();
			m_progress = m_runner.getReportProgress();

			CEPManager cepMgr = m_runner.getCEPManager();
			CEPServerRegistryImpl svrReg = cepMgr.getServerRegistry();
			CEPServer server;    //svrReg.getSystemServer();
			int hc = hashCode();
			if (hc < 0)
				hc = -hc;
			m_currentService = "s" + Integer.toString(hc);
			server = svrReg.createServer(m_currentService);

			m_execContext = server.getExecContext();
			//System.out.println("=================== " + m_suite + "." + m_currentTest.getName() + " ===================");
			//String cfg = m_currentTest.toString();
			//if (cfg != null)
			//	System.out.println(cfg);
			runCqlx();
		} catch (Exception ex) {
            handleAddError(ex);
			LogUtil.logStackTrace(ex);
		}
	}

	public ExecContext getExecContext() {
		return m_execContext;
	}

	public void runCqlx() {
		LogUtil.info("================================================ " + m_currentTest.getName());
		String cqlxpath = m_currentTest.getCqlxPath();

		beforeRun();
		handleStartTest();
		try {
			CEPLoadParser loadParser = new CEPLoadParser();
			boolean b = loadParser.parseFile(cqlxpath, false /*
															 * retry on
															 * validation
															 */);
			if (!b) {
				LogUtil.severe("Failed to parse\n"
								+ cqlxpath);
				handleAddError(new RuntimeException("Failed to parse "
						+ cqlxpath));
			} else {
				runCqlxLoad(m_currentTest.getName(), loadParser);
			}
		} catch (Throwable e) {
			LogUtil.severe("Failed to run\n"
					+ e.toString() + cqlxpath);
			handleAddError(e);
		}
		handleEndTest();
		afterRun();
		m_currentTest = null;
	}

	private void completed() {
	    synchronized(this) {
	        m_completed = true;
	        this.notifyAll();
	    }
	}

	private void handleStartTest() {
		if (m_progress != null)
			m_progress.handleStartTest(m_suite, m_currentTest.getName());
		else
			System.out.println("Start " + m_suite + "."
					+ m_currentTest + " "+m_df.format(new Date()));
	}

	private void handleEndTest() {
	    completed();
		if (m_progress != null)
			m_progress.handleEndTest(m_suite, m_currentTest.getName());
		else
			System.out.println("End " + m_suite + "."
					+ m_currentTest+ " "+m_df.format(new Date()));
	}

	private void handleAddError(Throwable e) {
        completed();
		if (m_progress != null)
			m_progress.handleAddError(m_suite, m_currentTest.getName(), e);
		else
			System.out.println("Error " + m_suite + "."
					+ m_currentTest + " " + e.toString()+ " "+m_df.format(new Date()));
	}

	private void handleAddFailure(String msg) {
        completed();
		if (m_progress != null)
			m_progress.handleAddFailure(m_suite, m_currentTest.getName(), msg);
		else
			System.out.println("Fail " + m_suite + "."
					+ m_currentTest + " " + msg+ " "+m_df.format(new Date()));
	}

	private void runCqlxLoad(String testcase, CEPLoadParser loadParser)
			throws CEPException {

		ConfigManager configMgr = m_runner.getCEPManager().getConfigMgr();
		String scheduler = configMgr.getSchedulerClassName();
		configMgr.setSchedTimeSlice(Constants.DEFAULT_SCHED_TIME_SLICE);
		configMgr.setSchedRuntime(Constants.DEFAULT_RUN_TIME);
		configMgr.setSchedulerClassName(scheduler);
		m_execContext.setSchema(testcase);

		QryDestLocator locator = (QryDestLocator) configMgr
				.getQueryDestLocator();
		locator.clear();
		m_postVerifiers.clear();
		m_onlineVerifiers.clear();

		HashMap<String, String> valMap = new HashMap<String, String>();
		valMap.put("TEST_DATA", m_runner.getTestDataFolder());
		valMap.put("TEST_OUTPUT", m_runner.getTestOutputFolder());

		List<String> ddls = loadParser.getLoadDDLs();
		// dumpDDLs(ddls);
		for (String ddl : ddls) {
			String queryId = getQueryId(ddl);
			if (queryId != null) {
				ddl = processQuery(queryId, ddl);
			}
			ddl = StringUtil.expand(ddl, valMap);
			LogUtil.debug(ddl);
			try {
				m_execContext.executeDDL(ddl, !m_currentTest
						.isIgnoreexceptions());
			} catch (Throwable e) {
				m_currentTest.clear(e);
			}
		}
		waitForVerifier();
		if (!m_currentTest.isAllClear()) {
			String msg = m_currentTest.getErrMsg();
			// System.out.println(msg);
			handleAddFailure(msg);
		}

	}

	private String getQueryId(String ddl) {
		Matcher m = m_queryPattern.matcher(ddl);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	private String processQuery(String queryId, String ddl) {
		Matcher m = m_queryPattern.matcher(ddl);
		if (m.find()) {
			String ep = m.group(2);
			boolean found = false;
			for (Pattern p : m_endpointPatterns) {
				Matcher mep = p.matcher(ep);
				if (mep.find()) {
					String outputFile = mep.group(1);
					outputFile = outputFile.replaceAll("@TEST_OUTPUT@/", "");
					int idx = outputFile.indexOf('?');
					boolean convertts = true;
					if (idx >= 0) {
						String flags = outputFile.substring(idx + 1);
						outputFile = outputFile.substring(0, idx);
						String[] fv = flags.split("=");
						String cmd = fv[0];
						if (cmd.equals(CONVERTTS))
							convertts = Boolean.parseBoolean(fv[1]);
						// System.out.println(flags +
						// " :  convertts="+convertts);
					}

					String outputPath = m_runner.getTestOutputFolder()
							+ File.separator + outputFile;
					String goldOutputPath = m_runner
							.getTestRefFolder()
							+ File.separator + outputFile;
					IVerifier v = m_currentTest.getVerifier();
					if (v == null) {
						System.out.println("Failed to process " + ep);
						assert (false);
					} else {
						v.setId(queryId);
						v.setGoldenOutputFile(goldOutputPath);
						v.setOutputFile(outputPath);
						v.setConvertts(convertts);
						v.setIgnorets(m_currentTest.isIgnorets());
						v.setIgnoreorder(m_currentTest.isIgnoreorder());
						v.setGenOutputs(m_runner.getGenerateOutputs());
						v.setShow(m_runner.getShowOutputs());
						v.setPostProcessors(m_currentTest.getPostProcessors());
						try {
							v.init();
						} catch (Exception e) {
							System.out.println("Failed to process " + ep + " "
									+ e.toString());
							//e.printStackTrace();
						}
						if (v.needToCapture()) {
							// if (outputFile.equals("outtkdata53_q1.txt")) {
							ConfigManager configMgr = m_runner.getCEPManager()
									.getConfigMgr();
							QryDestLocator locator = (QryDestLocator) configMgr
									.getQueryDestLocator();
							locator.addDestination(queryId, v);
							// System.out.println("Verifier : " + queryId + ","
							// + PathUtil.getFileName(goldOutputPath));
							ddl = String
									.format(
											"alter query %s add destination \"<EndPointReference><Address><Type>java</Type><Id>%s</Id></Address></EndPointReference>\"",
											queryId, queryId);
							// }
						}
						if (v.isOnlineVerifier()) {
							m_onlineVerifiers.put(queryId, v);
						} else {
							m_postVerifiers.put(queryId, v);
						}
					}

					found = true;
					break;
				}
			}
			if (!found) {
				System.out.println("Failed to parse " + ep);
			}
		}
		return ddl;
	}

	private void waitForVerifier() {
		StringBuilder result = new StringBuilder();
		int querys = m_onlineVerifiers.size();
		if (querys > 0 && !m_currentTest.isIgnoreorder()) {
			result.append("Capturedverifiers - ");
			result.append(m_suite);
			result.append(".");
			result.append(m_currentTest.getName());
			result.append(" success ");
		}

		// Process on-the-fly verifiers
		long b = System.currentTimeMillis();
		while (true) {
		    if (m_stop) break;
			Map<String, IVerifier> vv = new HashMap<String, IVerifier>();
			for (String qryId : m_onlineVerifiers.keySet()) {
				IVerifier v = m_onlineVerifiers.get(qryId);
				IVerifier.Status r = v.getStatus();
				if (r != IVerifier.Status.PROCESSING) {
					if (r != IVerifier.Status.FAIL) {
						result.append(qryId);
						result.append(" : ");
						result.append(v.getMsg());
						result.append(" ");
					}
				} else {
					vv.put(qryId, v);
				}
			}
			m_onlineVerifiers = vv;
			if (m_onlineVerifiers.size() == 0) {
				if (querys > 0 && !m_currentTest.isIgnoreorder()) {
					// System.out.println(result.toString());
				}
				break;
			}
			// System.out.println(m_captureVerifiers.size() + " remains from " +
			// querys);
			long e = System.currentTimeMillis();
			if (m_timeout == 0 || (e - b) >= m_timeout) {
				StringBuilder msg = new StringBuilder();
				msg.append("incomplete queries - ");
				for (String qryId : m_onlineVerifiers.keySet()) {
					IVerifier v = m_onlineVerifiers.get(qryId);
					msg.append(qryId);
					msg.append(":");
					msg.append(v.getMsg());
					msg.append(" ");
				}
				m_currentTest.addDiff(msg.toString());
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
		}

		// re-run failed tests with canonicalizer
		for (String qryId : m_onlineVerifiers.keySet()) {
			IVerifier v = m_onlineVerifiers.get(qryId);
			IVerifier.Status r = v.getStatus();
			if (r == IVerifier.Status.FAIL) {
				try {
					String msgold = v.getMsg();
					if (!v.verify()) {
						String msg = v.getMsg();
						m_currentTest.addDiff(msg);
					}
				} catch (Exception e) {
					m_currentTest.addDiff(e.toString());
				}
			}
		}

		// Process post-processing verifiers
		for (String qryId : m_postVerifiers.keySet()) {
			try {
				IVerifier v = m_postVerifiers.get(qryId);
				if (!v.verify()) {
					String msg = v.getMsg();
					m_currentTest.addDiff(msg);
				}
			} catch (Exception e) {
				m_currentTest.addDiff(e.toString());
			}
		}
	}

	private void beforeRun() {
		if (m_runner.isDumpMemInfo() || m_runner.isHeapDump())
			DebugUtil.invokeGC();
		if (m_runner.isHeapDump()) {
			dumpHeap(m_currentTest.getName(), "0");
		}
		if (m_runner.isDumpMemInfo()) {
			m_memInfoBefore = DebugUtil.dumpMemoryPools();
		}
		Thread.currentThread().setContextClassLoader(
				this.getClass().getClassLoader());
		m_starttime = System.currentTimeMillis();
	}

	private void afterRun() {
		long lendtime = System.currentTimeMillis();
		long difftime = lendtime - m_starttime;
		String msg ="**** Completed : "
				+ m_currentTest.getName() + " in " + timeStr(difftime);
        System.out.println(msg);
        LogUtil.info(msg);
		if (m_runner.isDumpMemInfo() || m_runner.isHeapDump())
			DebugUtil.invokeGC();
		if (m_runner.isDumpMemInfo()) {
			String minfo = DebugUtil.dumpMemoryPools();
			LogUtil.info("**** MemInfo : before ****");
			LogUtil.info(m_memInfoBefore);
			LogUtil.info("**** MemInfo : after ****");
			LogUtil.info(minfo);
		}
		if (m_runner.isHeapDump()) {
			dumpHeap(m_currentTest.getName(), "1");
		}
		if (DebugUtil.DEBUG_TUPLE_REFCOUNT) {
			dumpRefCounts(m_currentTest.getName());
		}

		if (m_runner.isDumpPlan()) {
			dumpPlan(m_currentTest.getName());
		}

		// String dropddl = "drop schema " + testcase;
		// System.out.println(dropddl);
		try {
			m_execContext.dropSchema(m_currentTest.getName(), true);
		} catch (Throwable e) {
			LogUtil.trace(e.getMessage());
		}
        try {
            CEPManager cepMgr = m_runner.getCEPManager();
            CEPServerRegistryImpl svrReg = cepMgr.getServerRegistry();
            svrReg.removeServer(m_currentService);
        } catch (Throwable e) {
            LogUtil.trace(e.getMessage());
        }
		
	}

	private void dumpHeap(String schemaName, String postfix) {
		String work = ConfigManager.getWorkFolder();
		String dumpFile = work + File.separator + schemaName + "_" + postfix
				+ ".hprof";
		System.out.println("**** dumping heap to " + dumpFile);
		HeapDump.dumpHeap(dumpFile);
		System.out.println("**** heap dumped to " + dumpFile);
	}

	protected String timeStr(long tm) {
		long s = tm / 1000;
		long m = s / 60;
		long h = m / 60;
		s = s % 60;
		m = m % 60;
		tm = tm % 1000;
		return h + ":" + m + ":" + s + "." + tm;
	}

	private void dumpPlan(String schemaName) {
		PrintWriter xml = null;
		try {
			String view_root = null;
			;
			view_root = System.getenv("T_WORK");
			String dumpFile = "/tmp/XMLVisDump.xml";
			if (view_root != null) {
				String actualFile = null;
				actualFile = schemaName + "_dump.xml";
				dumpFile = view_root + "/cep/" + actualFile;
			}
			System.out.println("Dumping plan to " + dumpFile);
			m_execContext.setTransaction(m_execContext.getTransactionMgr()
					.begin());
			String s = m_execContext.getQueryMgr().getXMLPlan2();
			m_execContext.getTransactionMgr().commit(
					m_execContext.getTransaction());
			xml = new PrintWriter(dumpFile);
			xml.append(s);
			xml.flush();
		} catch (Exception e) {
			System.out.println(e.toString());
			m_execContext.getTransactionMgr().rollback(
					m_execContext.getTransaction());
			LogUtil.trace("problem with dumping xml\n"
					+ e.toString());
		} finally {
			m_execContext.setTransaction(null);
			if (xml != null)
				xml.close();
		}

	}

	private void dumpDDLs(List<String> ddls) {
		try {
			FileOutputStream fos = new FileOutputStream("/tmp/test.xml");
			Writer out = new OutputStreamWriter(fos, "UTF8");
			for (String l : ddls) {
				out.write(l);
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	private void dumpRefCounts(String schemaName) {
		if (DebugUtil.DEBUG_TUPLE_REFCOUNT) {
			int cnt = 0;
			String filename = m_runner.getTestOutputFolder() + File.separator
					+ schemaName + "_rcsummary.log";
			try {
				FileWriter fw = new FileWriter(filename);
				PrintWriter pw = new PrintWriter(fw);

				FactoryManager factoryMgr = m_runner.getCEPManager()
						.getFactoryManager();
				Iterator<IAllocator> facItr = factoryMgr.getIterator();
				while (facItr.hasNext()) {
					IAllocator sf = facItr.next();
					if (sf instanceof TuplePtrFactory) {
						TuplePtrFactory fac = (TuplePtrFactory) sf;
						int t = fac.dumpRefCount(pw);
						if (t > 0)
							cnt++;
						fac.clearRefCount();
					} else if (sf instanceof TupleFactory) {
						TupleFactory fac = (TupleFactory) sf;
						int t = fac.dumpRefCount(pw);
						if (t > 0)
							cnt++;
						fac.clearRefCount();
					}
				}
				pw.close();
				fw.close();
			} catch (Exception e) {
				LogUtil.logStackTrace(e);
			}
			if (cnt == 0) {
				File f = new File(filename);
				f.delete();
			} else {
				System.out.println("Ref count summary : " + filename);
			}
		}
	}
}
