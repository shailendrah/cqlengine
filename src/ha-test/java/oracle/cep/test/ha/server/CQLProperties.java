package oracle.cep.test.ha.server;

import java.io.File;
import java.io.FileInputStream;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CQLProperties {
    private static Logger logger = Logger.getLogger(CQLProperties.class.getName());

    // properties used as jvm args
    public static final String DEFAULT_BASE_PATH = "/tmp/cqldriver";
    public static final String DEFAULT_CONFIG_FILE = "cqldriver.properties";

    public static final String CONFIG_FILE = "beam.config.file";
    public static final String TWORK_DIR = "twork.dir";
    public static final String WORK_FOLDER = "work_folder";
    public static final String STORAGE_FOLDER = "storage_folder";

    public static final String METADATA_RESTORE_PROP = "metadata_restore";
    public static final String METADATA_STORAGE_NAME = "metadata_storage";

    public static final String COHERENCE_OVERRIDE = "coherence_override";
    public static final String COHERENCE_TTL = "coherence_ttl";
    public static final String COHERENCE_LOG_LEVEL = "coherence_log_level";

    private static boolean s_loaded = false;
    /*
    * The Map hold the mapping between attribute name and it's value.
    */
    private static Properties s_properties;

    /**
     * If use file to do configuration, this should be set, otherwise it is null.
     */
    private static String configFileName;

    public static String getProperty(String key, String defval) {

        try {
            if (!s_loaded)
                load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System properties variable has a priority over the property value.
        String val = System.getProperty(key);

        if (val == null) {
            val = s_properties.getProperty(key);
        }

        // Try to get it from env as last try.
        val = val == null ? System.getenv(key) : val;

        return (val == null) ? defval : val;
    }

    public static String getProperty(String key) {
        return getProperty(key, "");
    }

    public static boolean getProperty(String key, boolean defval) {
        String val = getProperty(key, null);
        if (val == null)
            return defval;
        return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes");
    }

    public static int getProperty(String key, int defval) {
        String val = getProperty(key, null);
        if (val == null)
            return defval;
        int ival = 0;
        try {
            ival = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return ival;
    }

    public static long getProperty(String key, long defval) {
        String val = getProperty(key, null);
        if (val == null)
            return defval;
        long ival = 0;
        try {
            ival = Long.parseLong(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return ival;
    }

    public static double getProperty(String key, double defval) {
        String val = getProperty(key, null);
        if (val == null)
            return defval;
        double ival = 0;
        try {
            ival = Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return ival;
    }

    public static final String getWorkFolder() {
        String wfolder = getProperty(WORK_FOLDER, null);
        if (wfolder == null) {
            wfolder = getProperty(TWORK_DIR, null);
        }
        return wfolder;
    }

    public static String getDefaultConfigFilePath() {
        String configFileName = System.getProperty(CONFIG_FILE);
        if (configFileName == null) {
            String basepath = DEFAULT_BASE_PATH;
            if (basepath == null)
                basepath = System.getProperty("user.home");
            configFileName = basepath + File.separator + DEFAULT_CONFIG_FILE;
        }
        return configFileName;
    }

    public static Properties getConfigValues() {
        return s_properties;
    }

    /**
     * Load runtime properties from File.
     *
     * @return
     * @throws Exception
     */
    private static Properties loadFromFile(String fileName) throws Exception {

        boolean exists = (new File(fileName)).exists();
        if (!exists) {
            //throw new RuntimeException("cannot find " + fileName);
        	return new Properties();
        }

        FileInputStream propFile = new FileInputStream(fileName);
        Properties p = new Properties();
        p.load(propFile);

        return p;
    }

    private static void load() throws Exception {
        load(null);
    }

    public static synchronized void load(String configFileName0) throws Exception {
        if (s_loaded)
            return;

        s_properties = new Properties();

        // Log source of properties and property values at the following level.
        Level oLogLevel = Level.FINE;
        boolean isCorrectLogLevel = logger.isLoggable(oLogLevel);

        StringBuilder oStringBuilder = null;
        StringBuilder oSystemOverrides = null;
        StringBuilder oBamProperties = null;

        if (isCorrectLogLevel) {
            oStringBuilder = new StringBuilder(512);
            oSystemOverrides = new StringBuilder(1024);
            oBamProperties = new StringBuilder(1024);
            oStringBuilder.append("[");
            oStringBuilder.append(Thread.currentThread().getId());
            oStringBuilder.append("] CQL CONFIGURATION LOADED.");
            oStringBuilder.append(System.lineSeparator());
            oStringBuilder.append("CQL configuration loaded from default first.");
        }

        // Load the default first.
        Properties p = new Properties();

        String strFileName = configFileName0;

        // If configuration file was not specified, then use default.
        if (strFileName == null || strFileName.equals("null"))
            strFileName = getDefaultConfigFilePath();

        configFileName = strFileName;
        p = loadFromFile(configFileName);
        s_properties.putAll(p);

        if (isCorrectLogLevel) {
            oStringBuilder.append(System.lineSeparator());
            oStringBuilder.append("  The following configuration is in effect:");
        }

        // Run through loaded BAM properties and determine if there are any
        // System property overrides. System property (-Dkey=value) values
        // override the corresponding BAM property values.
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            Object oBamKey = entry.getKey(); // Loaded BAM property key.
            Object oBamValue = entry.getValue(); // Loaded BAM property value.

            property_logged:
            {
                // If this is BAM property is "beam.page.composer" then make it
                // available as a System property as well.
                if ("beam.page.composer".equals(oBamKey))
                    System.setProperty(oBamKey.toString(), oBamValue.toString());

                // If this BAM property is also specified in the System properties.
                else {
                    String strSysValue = System.getProperty(oBamKey.toString());
                    if (strSysValue != null) {
                        // System properties override BAM properties.
                        s_properties.remove(oBamKey);

                        // Log property value loaded. Indicate system override.
                        if (isCorrectLogLevel) {
                            oSystemOverrides.append(System.lineSeparator());
                            oSystemOverrides.append("    SYS Property: ");
                            oSystemOverrides.append(oBamKey.toString());
                            oSystemOverrides.append("=");
                            oSystemOverrides.append(strSysValue);
                            oSystemOverrides.append(" (Overridden BAM Value=");
                            oSystemOverrides.append(oBamValue.toString());
                            oSystemOverrides.append(")");
                            break property_logged;
                        }
                    }
                }

                // Log property value loaded.
                if (isCorrectLogLevel) {
                    oBamProperties.append(System.lineSeparator());
                    oBamProperties.append("    BAM Property: ");
                    oBamProperties.append(oBamKey);
                    oBamProperties.append("=");
                    oBamProperties.append(oBamValue);
                }
            }
        }

        // Log final configuration at level FINEST.
        if (isCorrectLogLevel)
            logger.log(oLogLevel, oStringBuilder.toString() + oSystemOverrides.toString() + oBamProperties.toString());

        // set the system properties
        s_loaded = true;

    }

    public static void resetLoadedVar() {
        s_loaded = false;
    }
}
