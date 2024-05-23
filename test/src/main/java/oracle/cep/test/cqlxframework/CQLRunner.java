package oracle.cep.test.cqlxframework;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;
import oracle.cep.service.CEPManager;
import oracle.cep.common.Constants;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;

public class CQLRunner {
	public enum cmdoptions {
		gen, show, threads, dumpheap, dumpmeminfo, dumpplan
	};

	
	private CEPManager m_cepMgr;

	private CqlxTestCase[] m_testCases = null;
	private Map<String, CqlxTestCase> m_testCaseMap;
	private String m_testCqlxPath;
	private String m_testDataPath;
	private String m_testOutputPath;
	private String m_testRefPath;

	private List<String> m_inclusionList;
	private List<String> m_exclusionList;
	private List<String> m_globalRules;
	private List<String> m_rules;

	private ReportProgress m_progress;
    private AbsCqlxTestSuite m_suite;
    
    private boolean m_dumpPlan = false;
    private boolean m_heapdump = false;
    private boolean m_meminfo = false;
    private boolean m_generateOutputs = false;
    private boolean m_showOutputs = false;
    private int m_timeout = 0;
    

    /**
     * Converts a standard POSIX Shell globbing pattern into a regular expression
     * pattern. The result can be used with the standard {@link java.util.regex} API to
     * recognize strings which match the glob pattern.
     * <p/>
     * See also, the POSIX Shell language:
     * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
     * 
     * @param pattern A glob pattern.
     * @return A regex pattern to recognize the given glob pattern.
     */
    public static final String convertGlobToRegex(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        int inGroup = 0;
        int inClass = 0;
        int firstIndexInClass = -1;
        char[] arr = pattern.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            switch (ch) {
                case '\\':
                    if (++i >= arr.length) {
                        sb.append('\\');
                    } else {
                        char next = arr[i];
                        switch (next) {
                            case ',':
                                // escape not needed
                                break;
                            case 'Q':
                            case 'E':
                                // extra escape needed
                                sb.append('\\');
                            default:
                                sb.append('\\');
                        }
                        sb.append(next);
                    }
                    break;
                case '*':
                    if (inClass == 0)
                        sb.append(".*");
                    else
                        sb.append('*');
                    break;
                case '?':
                    if (inClass == 0)
                        sb.append('.');
                    else
                        sb.append('?');
                    break;
                case '[':
                    inClass++;
                    firstIndexInClass = i+1;
                    sb.append('[');
                    break;
                case ']':
                    inClass--;
                    sb.append(']');
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
                        sb.append('\\');
                    sb.append(ch);
                    break;
                case '!':
                    if (firstIndexInClass == i)
                        sb.append('^');
                    else
                        sb.append('!');
                    break;
                case '{':
                    inGroup++;
                    sb.append('(');
                    break;
                case '}':
                    inGroup--;
                    sb.append(')');
                    break;
                case ',':
                    if (inGroup > 0)
                        sb.append('|');
                    else
                        sb.append(',');
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    private static class TestNameMatcher {
        String nameRule;
        Pattern pattern = null;
        boolean allMatch = false;
        
        public TestNameMatcher(String namerule) throws Exception
        {
            this.nameRule = namerule;
            if (nameRule.equals("*") || nameRule.contains("*") || nameRule.contains("?")) {
                pattern = null;
                if (!nameRule.equals("*")) {
                    nameRule = convertGlobToRegex(nameRule);
                    pattern = Pattern.compile(nameRule);
                }
                else allMatch = true;
            }
        }
    
        public boolean match(String testcaseName)  {
            boolean match = true;
            if (pattern != null) {
                Matcher matcher = pattern.matcher(testcaseName);
                match = matcher.find();
                return match;
            }
            return allMatch ? true : nameRule.equals(testcaseName);
        }       
    }
    private static class TestNameMatcherList {
        List<TestNameMatcher> partialList = new ArrayList<TestNameMatcher>();
        Set<String> fullList = new HashSet<String>();
        public void add(TestNameMatcher m)
        {
            if (m.pattern != null || m.allMatch) partialList.add(m);
            else fullList.add(m.nameRule);
        }
        
        public boolean isEmpty() {
            return (partialList.size() + fullList.size()) == 0;
        }
        
        public boolean match(String testcaseName) {
            if (fullList.contains(testcaseName)) return true;
            for (TestNameMatcher m : partialList) {
                if (m.match(testcaseName)) return true;
            }
            return false;
        }
    }

    public boolean isDumpPlan() {
        return m_dumpPlan;
    }

    public boolean isHeapDump() {
        return m_heapdump;
    }

    public boolean isDumpMemInfo() {
        return m_meminfo;
    }

    public void setDumpPlan(boolean b) {
        m_dumpPlan = b;
    }

    public void setHeapDump(boolean b) {
        m_heapdump = b;
    }

    public void setMemInfo(boolean b) {
        m_meminfo = b;
    }

    public boolean getGenerateOutputs() {
        return m_generateOutputs;
    }

    public void setGenerateOutputs(boolean b) {
        m_generateOutputs = b;
    }

    public boolean getShowOutputs() {
        return m_showOutputs;
    }

    public void setShowOutputs(boolean b) {
        m_showOutputs = b;
    }
    
    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int secs) {
        m_timeout = secs;
    }

    public CQLRunner(CEPManager cep) {
		this.m_cepMgr = cep;
	}

	public CEPManager getCEPManager() {
		return m_cepMgr;
	}

	public ReportProgress getReportProgress() {
		return m_progress;
	}

	public String getTestCqlxFolder() {
		return m_testCqlxPath;
	}

	public String getTestDataFolder() {
		return m_testDataPath;
	}

	public String getTestOutputFolder() {
		return m_testOutputPath;
	}

	public String getTestRefFolder() {
		return m_testRefPath;
	}

	public void setTestCqlxFolder(String s) {
		m_testCqlxPath = s;
	}

	public void setTestDataFolder(String s) {
		m_testDataPath = s;
	}

	public void setTestOutputFolder(String s) {
		m_testOutputPath = s;
	}

	public void setTestRefFolder(String s) {
		m_testRefPath = s;
	}

	public void setFileList(FileList l) {
		m_inclusionList = l.getIncludes();
		m_exclusionList = l.getExcludes();
		m_rules = l.getRules();

	}

	public void setRules(Rules r) {
		m_globalRules = r.getRules();
	}

    private TestNameMatcherList preprocessFileList(List<String> files) throws Exception {
        TestNameMatcherList matchers = new TestNameMatcherList();
        for (String testname : files) {
            TestNameMatcher m = new TestNameMatcher(testname);
            matchers.add(m);
        }
        return matchers;
    }

	private void processRules(List<String> rules) {
		for (String str : rules) {
			int idx = str.indexOf(':');
			if (idx < 0) {
				System.out.println("Invalid rule " + str);
				continue;
			}
			String testname = str.substring(0, idx);
			String rule = str.substring(idx + 1);
			
			if (testname.equals("*") || testname.contains("?")) {
				try {
					Pattern ptn = null;
					if (!testname.equals("*"))
						ptn = Pattern.compile(testname);
					for (CqlxTestCase t : m_testCases) {
						boolean match = true;
						if (ptn != null) {
							Matcher matcher = ptn.matcher(t.getName());
							match = matcher.find();
						}
						if (match) {
					        t.set(rule);
						}
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			} else {
			    CqlxTestCase test = m_testCaseMap.get(testname);
				if (test != null) test.set(rule);
			}
		}
	}

	public AbsCqlxTestSuite getSuite() { return m_suite;}
	
	public void init(TestSuite testSuite, ReportProgress progress)
			throws Exception {
	    m_suite = (AbsCqlxTestSuite) testSuite;
		m_progress = progress;

		new File(m_testOutputPath).mkdirs();

		TestNameMatcherList inset = preprocessFileList(m_inclusionList);
		TestNameMatcherList exset = preprocessFileList(m_exclusionList);
        
        m_testCaseMap = new HashMap<String, CqlxTestCase>();
		List<CqlxTestCase> testlist = new LinkedList<CqlxTestCase>();
		Enumeration<Test> tests = testSuite.tests();
		while(tests.hasMoreElements()) {
		    CqlxTestCase test = (CqlxTestCase) tests.nextElement();
		    String name = test.getName();
		    
		    boolean include = false;
		    if (inset.isEmpty()) include = !exset.match(name);
		    else if (exset.isEmpty()) include = inset.match(name);
		    else include = (inset.match(name) || !exset.match(name));
		    test.setInclude(include);
            m_testCaseMap.put(name, test);
		}
		m_testCases = testlist.toArray(new CqlxTestCase[0]);

		if (m_globalRules != null)
			processRules(m_globalRules);
		if (m_rules != null)
			processRules(m_rules);

		ConfigManager configMgr = m_cepMgr.getConfigMgr();
		configMgr.setSchedOnNewThread(false);
		configMgr.setSchedRuntime(Constants.DEFAULT_RUN_TIME);

		// If this test is in directInterop mode, then request for
		// push mode emulation
		boolean isDirectInterop = configMgr.getDirectInterop();
		if (isDirectInterop)
			configMgr.setIsRegressPushMode(true);

		configMgr.dump();
	}
	
    public void close()
    {
		LogUtil.info(LoggerType.TRACE, "Closing...");
		try {
			m_cepMgr.close();
		} catch (Exception e) {
			LogUtil.severe(LoggerType.TRACE, "Exception in close\n"
					+ e.toString());
		}
	}
}
