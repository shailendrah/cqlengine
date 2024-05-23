/*
 * Created on Sep 16, 2004
 */

/**
 * Class to handle initialization settings, such as:
 *   default host, default port, resource paths ...
 * All initialization parameters are stored in a file "visualizer.ini"
 * 
 * @author zubin
 */
package oracle.cep.driver.util;

import java.util.*;
import java.io.*;
import java.util.zip.*;

import com.oracle.cep.common.util.SecureFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InitManager {
    //constants
	//private static final String INIT_FILE = "/oracle/cep/driver/util/visualizer.ini",
	
	private static final Log logger = LogFactory.getLog("InitManager");
    //private static final String INIT_FILE = "visualizer.ini",
    private static final String INIT_FILE = "/scratch/sbishnoi/clones/clone_soaosa_OSACS164/soa-osa/source/modules/spark-cql/cqlengine/tools/visualizer/bin/visualizer.ini",

    DEFAULT_HOST = "default_host",
    DEFAULT_PORT = "default_port",
    SCRIPT_PATH = "script_path",
    DEMO_PATH = "demo_path",
        OC4J_PATH = "oc4j_path";

    public static final String DEMO_ZIP_FILE =
    "/demo.zip";

    //data members
    private static Properties initProperties = new Properties(); 
    
    //hack, used for .jar distribution to get file
    public static class Dummy {
    }
    
    //initialization
    //
    /*
    static  {
    try {

        InputStream in = (new Dummy()).getClass().getResourceAsStream(INIT_FILE);

        //initProperties.load(in);        
        //initProperties.list(System.out);
    } catch (FileNotFoundException e) {
        System.out.println("can't find file visualizer.ini");
        e.printStackTrace();
    } catch (IOException e) {
        System.out.println("visualizer.ini contains error");
        e.printStackTrace();
    }
    }*/
    
    //getters
    public static String getDefaultHost() {
    return initProperties.getProperty(DEFAULT_HOST, "");
    }
    
    public static String getDefaultPort() {
    return initProperties.getProperty(DEFAULT_PORT, "");
    }

    public static String getScriptPath() {
    return initProperties.getProperty(SCRIPT_PATH, "");
    }
    
    public static String getDemoPath () {
    return initProperties.getProperty (DEMO_PATH, "");
    }

    public static String getOc4jPath() {
        return initProperties.getProperty (OC4J_PATH, "");
    }
    
    /**
     * Sets up the paths
     */
    public static void setupPaths() throws IOException {

        String isdemo = System.getProperty("stream.isdemo");
    
        // if not Demo mode, do nothing
        if(isdemo == null) {
            return;
        }
    
        // first get a handle on the users's home directory
        String homeDir = System.getProperty("user.home");

        // create a .stream directory
        String streamHome = homeDir + File.separator + ".stream";
        File file = new File(streamHome);
        file.mkdir();

        // create the scripts directory
        String scriptDir = streamHome + File.separator + "scripts";
        file = SecureFile.getFile(scriptDir);
        file.mkdir();

        //System.setProperty("stream.home", streamHome);
        initProperties.setProperty(SCRIPT_PATH, scriptDir + "/");
        initProperties.setProperty(DEMO_PATH, scriptDir + "/");

        // unpack the scripts.zip file
        InputStream in = (new Dummy()).getClass().getResourceAsStream(DEMO_ZIP_FILE);
        if (in == null) {
            throw new IOException("Unable to find scripts file");
        }
        ZipInputStream zf = new ZipInputStream(in);
        try {
            while (true) {
                ZipEntry entry = zf.getNextEntry();
                if (entry == null || entry.getSize() == 0) break;
                long size = entry.getSize();
                String name = entry.getName();
                writeEntry(scriptDir + File.separator + name, size, zf);
            }
        } finally {
            try {
                zf.close();
            } catch (IOException e) {
                logger.warn("Error closing demo zip file: " + e.getMessage());
            }
        }
    }

    private static void writeEntry(String name, long size, ZipInputStream stream)
    throws IOException {
        byte[] buf = new byte[1024];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(name));

        try {
            while (size > 0) {
                int n = stream.read(buf, 0, (int)Math.min(size,1024));
                if (n <= 0) throw new IOException("Unexpected end of file");
                out.write(buf, 0, n);
                size -= n;
            }
        } finally {
           out.close();
        }
    }   
}
