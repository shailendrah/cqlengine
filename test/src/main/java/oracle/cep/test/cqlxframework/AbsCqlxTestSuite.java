package oracle.cep.test.cqlxframework;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import oracle.cep.service.CEPManager;
import oracle.cep.util.PathUtil;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.junit.AfterClass;

public abstract class AbsCqlxTestSuite extends TestSuite {
    public static final String PROPERTY_QUERYTEST_INCLUDE = "include";
    public static final String PROPERTY_QUERYTEST_EXCLUDE = "exclude";

    private static final String PROPERTY_BASE_FOLDER = "testroot";
    private static final String PROPERTY_RESOURCE_FOLDER = "resources";

    private static final String PROPERTY_CQLX_FOLDER = "cqlxs";;
    private static final String PROPERTY_DATA_FOLDER = "data";;
    private static final String PROPERTY_REF_FOLDER = "refs";;
    private static final String PROPERTY_OUTPUT = "output";

    private static final String PROPERTY_DEBUG_SUITE = "test.suite";;
    private static final String PROPERTY_DEBUG_TEST = "test.cqlx";;

    private Set<String> m_includes = null;
    private Set<String> m_excludes = null;

    protected String m_appContextPath;
    protected CQLRunner m_runner;
    protected static FileSystemXmlApplicationContext s_appContext;
    
    protected String getInputExtension() {
        return ".cqlx";
    }

    protected boolean canProcess(String filename) {
        if (m_excludes != null) {
            if (m_excludes.contains(filename))
                return false;
        }
        if (m_includes != null) {
            return (m_includes.contains(filename));
        }
        return true;
    }

    protected void initFilter() {
        String includes = SystemProperties.getProperty(
                PROPERTY_QUERYTEST_INCLUDE, null);

        if (includes != null && !includes.isEmpty()) {
            m_includes = new HashSet<String>();
            for (String v : includes.split(",")) {
                m_includes.add(v);
            }
        }

        String excludes = SystemProperties.getProperty(
                PROPERTY_QUERYTEST_EXCLUDE, null);
        if (excludes != null && !excludes.isEmpty()) {
            m_excludes = new HashSet<String>();
            for (String v : excludes.split(",")) {
                m_excludes.add(v);
            }
        }
    }

    public AbsCqlxTestSuite(String name, String appContextPath) {
        super(name);
        m_appContextPath = appContextPath;
    }

    public CQLRunner getRunner() { return m_runner;}
    
    private String getRootName(String path) {
        String name = PathUtil.getFileName(path);
        int pos = name.indexOf('.');
        if (pos > 0)
            name = name.substring(0, pos);
        return name;
    }

    private String getPath(String propname, String defval, String basePath)
    {
        return getPath(propname, defval, basePath, false);
    }
    private String getPath(String propname, String defval, String basePath, boolean make)
    {
        String path = SystemProperties.getProperty(propname, defval);
        File f = new File(path);
        if (f.exists()) return path;
        if (f.isAbsolute())
            throw new RuntimeException(path + " + does not exist for "+propname);
        path = basePath + File.separator + path;
        if (f.exists()) return path;
        if (make) {
            if (f.mkdirs()) return path;
        }
        throw new RuntimeException(path + " + does not exist for "+propname);
    }

    public TestSuite generateSuite() {
        String basePath = SystemProperties.getProperty(PROPERTY_BASE_FOLDER,
                ".");
        LogUtil.debug("base folder : " + basePath);

        String resourcePath = getPath(PROPERTY_RESOURCE_FOLDER, "src/test/resources", basePath);
        if (!new File(resourcePath).isAbsolute()) {
            //resourcePath = "."+File.separator+resourcePath;
        }

        m_appContextPath = resourcePath + File.separator + m_appContextPath;
        LogUtil.debug("loading " + m_appContextPath);

        System.setProperty("log4j.defaultInitOverride", "true");
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.Logger rootLogger = org.apache.log4j.LogManager
                .getRootLogger();
        rootLogger.setLevel(org.apache.log4j.Level.WARN);
        if (s_appContext != null) {
            s_appContext.close();
            //We need to reset the singleton CEPManager for different env setup.
            CEPManager.resetInstance();
        }
        
        s_appContext = new FileSystemXmlApplicationContext(m_appContextPath);

        m_runner = (CQLRunner) s_appContext.getBean("cqlRunner");
        
        //Make sure the path exists or adjust to base, and get the absolute path.
        String cqlxPath = getPath(PROPERTY_CQLX_FOLDER, m_runner.getTestCqlxFolder(), basePath);
        m_runner.setTestCqlxFolder(new File(cqlxPath).getAbsolutePath());
        String dataPath = getPath(PROPERTY_DATA_FOLDER, m_runner.getTestDataFolder(), basePath);
        m_runner.setTestDataFolder(new File(dataPath).getAbsolutePath());
        String refPath = getPath(PROPERTY_REF_FOLDER, m_runner.getTestRefFolder(), basePath);
        m_runner.setTestRefFolder(new File(refPath).getAbsolutePath());
        String outputPath = getPath(PROPERTY_OUTPUT, m_runner.getTestOutputFolder(), basePath, true);
        m_runner.setTestOutputFolder(new File(outputPath).getAbsolutePath());
        
        LogUtil.debug("cqlx folder : " + cqlxPath);
        LogUtil.debug("data folder : " + dataPath);
        LogUtil.debug("output folder : " + outputPath);

        
        initFilter();

        final String debugSuite = SystemProperties.getProperty(PROPERTY_DEBUG_SUITE, null);
        final String debugCqlx = SystemProperties.getProperty(PROPERTY_DEBUG_TEST, null);
        
        final String inputExt = getInputExtension();
        File path = new File(cqlxPath);
        File[] files = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if  ( (debugSuite == null || debugSuite.equals(getName()) )  &&
                      (debugCqlx == null || debugCqlx.equals(getRootName(name))) ) {
                   return name.toLowerCase().endsWith(inputExt);
                } else return false;
            }
        });
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    continue;
                String filename = f.getName();
                if (!canProcess(filename))
                    continue;

                String testName = getRootName(filename);

                TestCase tc = new CqlxTestCase(this, testName,
                        f.getAbsolutePath());
                addTest(tc);
            }
        }

        try {
            m_runner.init(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    @AfterClass 
    public static void tearDownClass() { 
        System.out.println("Master tearDown");
    }
}
