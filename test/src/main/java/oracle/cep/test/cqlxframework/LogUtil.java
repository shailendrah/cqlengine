package oracle.cep.test.cqlxframework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;

public class LogUtil {
    private static final Logger log = Logger.getLogger("cqlxtest");
    public static void info(String msg) {
        log.info(msg);
    }
    
    public static void warning(String msg) {
        log.warning(msg);
    }

    public static void severe(String msg) {
        log.severe(msg);
    }

    public static void debug(String msg) {
        log.fine(msg);
    }

    public static void trace(String msg) {
        log.finest(msg);
    }

    public static void logStackTrace(Exception cause) {
        String errorMessage = cause.getMessage();
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        cause.printStackTrace(printWriter);
        String trace = result.toString();      
        severe("Error:" + errorMessage);
        severe("Caused By:\n" + trace);
    }

}
