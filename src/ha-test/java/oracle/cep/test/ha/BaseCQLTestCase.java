package oracle.cep.test.ha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Properties;
import java.util.Set;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;
import oracle.cep.serializer.ObjStreamFactory;
import oracle.cep.serializer.ObjStreamUtil;
import oracle.cep.snapshot.SnapshotContext;
import oracle.cep.test.ha.server.CQLProcessor;
import oracle.cep.test.ha.server.CQLProcessorWrapper;
import oracle.cep.test.ha.server.CQSFaultHandler;
import oracle.cep.util.PathUtil;
import oracle.cep.dataStructures.external.TupleValue;
import com.oracle.osa.exceptions.ErrorCode;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;

/**
 * Base Class for All CQL HA test cases.
 */

public abstract class BaseCQLTestCase extends TestCase
{
  // Test execution will read input data from this directory.
  // Create all input files into this directory.
  protected String inputDir;

  // Test execution will write output files into this directory.
  // Check this for all test output files.
  protected String outDir;

  // Test execution will use the following sample output files to
  // decleare success or failure. Create desired output files in this directory.
  protected String logDir;

  // Test will use db.properties file to connect to database specfied in
  // db.properties file
  protected String dbPropsFile;

  protected boolean isSuccessful;

  public static boolean createSnapshot = false;
  public static boolean checkLoadVersions = true;

  private static final String SNAPSHOT_FOLDER = "snapshots";
  private static final String FULL_SNAPSHOT_MARK = "full";
  private static final String JFULL_SNAPSHOT_MARK = "jfull";
  private static final String JOURNAL_SNAPSHOT_MARK = "journal";

  protected static String FILE_SEPARATOR = "/"; // If we use FILE_SEPARATOR, it
                                                // fails in parsing eprxml on
                                                // Windows platform.
  protected static long DEFAULT_TIMEOUT = 10000;
  protected static long STEP_DELAY = 5000;
  protected static int DEFAULT_FULL_SNAPSHOT_TEST_PHASES = 2;
  protected static int DEFAULT_JOURNAL_SNAPSHOT_TEST_PHASES = 3;

  protected ArrayList<String> ignoredVersions = new ArrayList<String>(Arrays.asList("1.00.javaext","1.10.javaext","1.20.javaext","1.30.javaext"));
  
  static
  {
    String v = System.getProperty("test.createSnapshot");
    if (v != null && Boolean.parseBoolean(v))
    {
      createSnapshot = true;
      checkLoadVersions = false;
    }
  }
  /**
   * Maximum number of test attempts before marking it final. It is done to
   * filter out intermittent failures
   */
  protected static int MAX_ATTEMPTS = 3;

  /** Default is false. Enable this flag to dump query plan */
  protected boolean isDebugMode;

  /**
   * Return number of phases for full snapshot test case. This number specifies
   * how many times CQL Engine will restart query
   */
  public int getNumFullSnapshotTestPhases()
  {
    return DEFAULT_FULL_SNAPSHOT_TEST_PHASES;
  }

  /** Return number of phases for journal snapshot test case */
  public int getNumJournalSnapshotTestPhases()
  {
    return DEFAULT_JOURNAL_SNAPSHOT_TEST_PHASES;
  }

  public BaseCQLTestCase()
  {
    inputDir = System.getProperty("test.inputFolder");
    logDir = System.getProperty("test.logFolder");
    outDir = System.getProperty("test.outputFolder");
    dbPropsFile = System.getProperty("db.properties");
    isDebugMode = Boolean.getBoolean("debug");
    inputDir = PathUtil.getUnixPath(inputDir);
    logDir = PathUtil.getUnixPath(logDir);
    outDir = PathUtil.getUnixPath(outDir);
    // clean(outDir);
  }

  protected CQLProcessor getCqlProcessor()
  {
    return CQLProcessorWrapper.getCQLProcessor();
  }

  protected String getInputFilePath(String inp)
  {
    return inputDir + FILE_SEPARATOR + inp;
  }

  protected String getOutputFilePath(String out)
  {
    return outDir + FILE_SEPARATOR + out;
  }

  protected String getLogFilePath(String log)
  {
    return logDir + FILE_SEPARATOR + log;
  }

  /**
   * Setup Data Sources with CEP Server
   */
  public void setupDataSources(CQLProcessor processor, TestMetadata md)
  {
    if (md.dataSources != null)
    {
      Iterator<Entry<String, String>> iter = md.dataSources.entrySet()
          .iterator();
      while (iter.hasNext())
      {
        Entry<String, String> dsEntry = iter.next();
        processor.addDataSource(dsEntry.getKey(), dsEntry.getValue());
      }
    }

  }

  /**
   * The setup method will do the following: 
   * 1) Initialize CQL processor 
   * 2) Invoke register stream and query DDLs 
   * 3) Generate and invoke DDLs to add sources to stream 
   * 4) Generate and invoke DDLs to add destination to query
   * 5) Generate and invoke DDLs to start query
   */
  public void setup(CQLProcessor processor, TestMetadata md, String schema,
      int phaseId) throws Exception
  {
    CQSFaultHandler.getInstance().clearExceptions();
    processor.start();
    setupDataSources(processor, md);
    invokeDDLs(processor, schema, md.getSetupDDLs());

    // Generate and invoke DDLs to add sources to stream
    Set<String> sources = md.getSourceMetadata().keySet();
    for (String source : sources)
    {
      String sourcePath = md.getInpFile(source, phaseId);
      SourceType sourceType = md.getSourceType(source);
      if (sourceType == SourceType.STREAM)
        invokeDDL(processor, schema, "alter stream " + source
            + " add source \"<EndPointReference><Address>file://" + sourcePath
            + "</Address></EndPointReference>\"");
      else if (sourceType == SourceType.RELATION)
        invokeDDL(processor, schema, "alter relation " + source
            + " add source \"<EndPointReference><Address>file://" + sourcePath
            + "</Address></EndPointReference>\"");
      else
        invokeDDL(processor, schema, "alter relation " + source
            + " add source \"<EndPointReference><Address>external:"
            + sourcePath + "</Address></EndPointReference>\"");

    }

    // Generate and invoke DDLs to add destination to query
    // Generate and invoke DDLs to start query
    Set<String> queries = md.getDestinationMetadata().keySet();
    for (String query : queries)
    {
      String destPath = md.getOutFile(query, phaseId);
      invokeDDL(processor, schema, "alter query " + query
          + " add destination \"<EndPointReference><Address>file://" + destPath
          + "</Address></EndPointReference>\"");
      invokeDDL(processor, schema, "alter query " + query + " start");
    }
  }

  public void setup(CQLProcessor processor, TestMetadata md, String schema,
      int phaseId, int batchId) throws Exception
  {
    CQSFaultHandler.getInstance().clearExceptions();
    processor.start();
    setupDataSources(processor, md);
    invokeDDLs(processor, schema, md.getSetupDDLs());

    // Generate and invoke DDLs to add sources to stream
    Set<String> sources = md.getSourceMetadata().keySet();
    for (String source : sources)
    {
      String sourcePath = md.getInpFile(source, phaseId, batchId);
      SourceType sourceType = md.getSourceType(source);
      if (sourceType == SourceType.STREAM)
        invokeDDL(processor, schema, "alter stream " + source
            + " add source \"<EndPointReference><Address>file://" + sourcePath
            + "</Address></EndPointReference>\"");
      else if (sourceType == SourceType.RELATION)
        invokeDDL(processor, schema, "alter relation " + source
            + " add source \"<EndPointReference><Address>file://" + sourcePath
            + "</Address></EndPointReference>\"");
      else
        invokeDDL(processor, schema, "alter relation " + source
            + " add source \"<EndPointReference><Address>external:"
            + sourcePath + "</Address></EndPointReference>\"");

    }

    // Generate and invoke DDLs to add destination to query
    // Generate and invoke DDLs to start query
    Set<String> queries = md.getDestinationMetadata().keySet();
    for (String query : queries)
    {
      String destPath = md.getOutFile(query, phaseId, batchId);
      invokeDDL(processor, schema, "alter query " + query
          + " add destination \"<EndPointReference><Address>file://" + destPath
          + "</Address></EndPointReference>\"");
      invokeDDL(processor, schema, "alter query " + query + " start");
    }
  }

  /*
   * public void setup(CQLProcessor processor, TestMetadata md, String schema,
   * int phaseId, int batchId) throws Exception { processor.start();
   * invokeDDLs(processor, schema, md.getSetupDDLs());
   * 
   * // Generate and invoke DDLs to add sources to stream
   * addSourceAndDestination(processor, md, schema, phaseId, batchId); }
   */

  /**
   * This method will do the following: 
   * 1) Invoke all DDLs which cleans the metadata 
   * 2) Release handle of CQL Processor
   */
  public void teardown(CQLProcessor processor, TestMetadata md, String schema)
      throws Exception
  {
    invokeDDLs(processor, schema, md.getTearDownDDLs());
    processor.close();
  }

  /** Start the CQL Query Execution. */
  public void start(CQLProcessor processor, String schema) throws Exception
  {
    processor.getCEPServer().executeDDL(schema, "alter system run");
  }

  /** Stop CQL Queries running in this test */
  public void stop(CQLProcessor processor, TestMetadata md, String schema)
      throws Exception
  {
    // Generate and invoke DDLs to add destination to query
    // Generate and invoke DDLs to start query
    Set<String> queries = md.getDestinationMetadata().keySet();
    for (String query : queries)
      invokeDDL(processor, schema, "alter query " + query + " stop");
  }

  /** Invoke given DDL on CQL Processor */
  public void invokeDDL(CQLProcessor processor, String schema, String ddl)
      throws Exception
  {
    try
    {
      processor.getCEPServer().executeDDL(schema, ddl);
    } catch (Exception e)
    {
      e.printStackTrace();
      throw e;
    }
  }

  /** Invoke given DDLs on CQL Processor */
  public void invokeDDLs(CQLProcessor processor, String schema, String[] ddls)
      throws Exception
  {
    for (String ddl : ddls)
      invokeDDL(processor, schema, ddl);
  }

  /** Invoke DML to insert given tuple in schema */
  public void invokeDML(CQLProcessor processor, String schema, TupleValue tuple)
      throws Exception
  {
    processor.getCEPServer().executeDML(schema, tuple);
  }

  /** Dump query plan in XML format into a file */
  public void dumpQueryPlan(CQLProcessor processor, TestMetadata md,
      String schema, int phaseId) throws Exception
  {
    if (isDebugMode)
    {
      String queryDumpFile = md.getQueryPlanDumpFile(phaseId, schema);
      String xmlPlan = processor.getCEPServer().dumpQueryPlan();
      PrintStream out = new PrintStream(new FileOutputStream(queryDumpFile));
      out.print(xmlPlan);
      out.flush();
      out.close();
      addLineSeparatorWithMessage("Phase:" + phaseId
          + "- Dumped Query Plan into file "
          + md.getQueryPlanDumpFile(phaseId, schema));
    }
  }

  /** Generate Snapshot for this test's schema */
  public byte[] createSnapshot(CQLProcessor processor, String schema,
      boolean isFull) throws Exception
  {
    ObjStreamFactory osfac = ObjStreamUtil.getObjStreamFactory();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = osfac.createObjectOutputStream(bos);
    SnapshotContext.writeVersion(out);
    processor.getCEPServer().createSnapshot(schema, out, isFull);
    out.flush();
    out.close();
    return bos.toByteArray();
  }

  /** Create Full Snapshot for the schema of the test */
  public byte[] createFullSnapshot(CQLProcessor processor, String schema)
      throws Exception
  {
    return createSnapshot(processor, schema, true);
  }

  /** Load snapshot for this test's schema from a byte array */
  public void loadFullSnapshot(CQLProcessor processor, String schema,
      byte[] snapshotBytes) throws Exception
  {
    loadSnapshot(processor, schema, snapshotBytes, true);
  }

  public void loadJournalSnapshot(CQLProcessor processor, String schema,
      byte[] snapshotBytes) throws Exception
  {
    loadSnapshot(processor, schema, snapshotBytes, false);
  }

  protected void loadSnapshot(CQLProcessor processor, String schema,
      byte[] snapshotBytes, boolean fullsnapshot) throws Exception
  {
    ObjStreamFactory osfac = ObjStreamUtil.getObjStreamFactory();
    ByteArrayInputStream bis = new ByteArrayInputStream(snapshotBytes);
    ObjectInputStream in = osfac.createObjectInputStream(bis);
    SnapshotContext.readVersion(in);
    processor.getCEPServer().loadSnapshot(schema, in, fullsnapshot);
    in.close();
  }

  public void addLineSeparatorWithMessage(String msg)
  {
    System.out.println("================================= " + msg
        + " =================================");
  }

  private void clean(String outDir)
  {
    try
    {
      new File(outDir).mkdir();
    } catch (SecurityException e)
    {
      e.printStackTrace();
    }
  }

  public void waitForCompletion(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Metadata for a test case. Necessary to create a test metadata prior to
   * invoking test execution.
   */
  public class TestMetadata
  {
    // DDLs to create streams and query. Also add sources and destination
    // to streams and query respectively.
    String[] setupDDLs;

    // DDLs to destroy streams and query.
    String[] tearDownDDLs;

    // Source Metadata for source input file
    HashMap<String, String> sourceMetadata;

    // Source Metadata for source type
    Map<String, SourceType> sourceType;

    // Destination Metadata
    HashMap<String, String> destinationMetadata;

    HashMap<Integer, File> queryPlanDumpFiles;
    // Source metadata for external datasources
    HashMap<String, String> dataSources;

    /**
     * Construct metadata for a test case.
     * 
     * @param arr1
     *          setup ddls which create stream and query.
     * @param arr2
     *          tear down ddls which destroys stream and query.
     */
    public TestMetadata(String[] arr1, String[] arr2) throws Exception
    {
      setupDDLs = arr1;
      tearDownDDLs = arr2;
      sourceMetadata = new HashMap<String, String>();
      sourceType = new HashMap<String, SourceType>();
      destinationMetadata = new HashMap<String, String>();
      queryPlanDumpFiles = new HashMap<Integer, File>();
      dataSources = new HashMap<String, String>();
    }

    /**
     * if not specified, return source type 'stream'
     * 
     * @param source
     * @return
     */
    public SourceType getSourceType(String source)
    {
      if (sourceType.get(source) == null)
        return SourceType.STREAM;
      else
        return sourceType.get(source);
    }

    public String[] getSetupDDLs()
    {
      return setupDDLs;
    }

    public String[] getTearDownDDLs()
    {
      return tearDownDDLs;
    }

    public String getInpFile(String source, int phaseId)
    {
      return getSourceType(source) == SourceType.EXTERNAL_RELATION ? sourceMetadata
          .get(source) : getInputFilePath(sourceMetadata.get(source) + "_phase"
          + phaseId);
    }

    public String getOutFile(String queryId, int phaseId)
    {
      return getOutputFilePath(destinationMetadata.get(queryId) + "_phase"
          + phaseId);
    }

    public String getLogFile(String queryId, int phaseId)
    {
      return getLogFilePath(destinationMetadata.get(queryId) + "_phase"
          + phaseId);
    }

    public String getInpFile(String source, int phaseId, int batchId)
    {
      return getInpFile(source, phaseId) + "_batch" + batchId;
    }

    public String getOutFile(String queryId, int phaseId, int batchId)
    {
      return getOutFile(queryId, phaseId) + "_batch" + batchId;
    }

    public String getLogFile(String queryId, int phaseId, int batchId)
    {
      return getLogFile(queryId, phaseId) + "_batch" + batchId;
    }

    public HashMap<String, String> getSourceMetadata()
    {
      return sourceMetadata;
    }

    public HashMap<String, String> getDestinationMetadata()
    {
      return destinationMetadata;
    }

    public void addSourceMetadata(String key, String val)
    {
      sourceMetadata.put(key, val);
    }

    public void addSourceType(String key, SourceType val)
    {
      sourceType.put(key, val);
    }

    public void addDestinationMetadata(String key, String val)
    {
      destinationMetadata.put(key, val);
    }

    public void addDataSource(String dsName, String dbURL)
    {
      dataSources.put(dsName, dbURL);
    }

    public String getQueryPlanDumpFile(int phaseId, String schema)
        throws Exception
    {
      File queryPlanDumpFile = queryPlanDumpFiles.get(phaseId);
      if (queryPlanDumpFile == null)
      {
        queryPlanDumpFile = File.createTempFile(schema + "_", ".queryplan"
            + phaseId, new File(outDir));
        queryPlanDumpFiles.put(phaseId, queryPlanDumpFile);
        return queryPlanDumpFile.getAbsolutePath();
      } else
        return queryPlanDumpFile.getAbsolutePath();
    }
  }

  public enum SourceType
  {
    RELATION, STREAM, EXTERNAL_RELATION
  }

  /**
   * Read database properties file
   */
  public Properties readProperties() throws IOException
  {
    Properties prop = new Properties();
    InputStream input = null;
    try
    {
      input = new FileInputStream(dbPropsFile);
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    // load properties file
    prop.load(input);
    return prop;
  }

  protected String getSnapshotSrcFolder()
  {
    return inputDir + FILE_SEPARATOR + SNAPSHOT_FOLDER;
  }

  protected String getSnapshotFolder(String version)
  {
    return outDir + FILE_SEPARATOR + version;
  }

  protected File[] getVersions()
  {
    File path = new File(getSnapshotSrcFolder());
    return path.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        return name.toLowerCase().endsWith(".zip");
      }
    });
  }

  private void unzipFile(String outputPath, String zipFile) throws IOException
  {
    System.out.println("Unzipping " + zipFile + " to " + outputPath);
    File f = new File(zipFile);
    f = new File(outputPath);
    f.mkdirs();

    InputStream is = new FileInputStream(zipFile);
    ZipInputStream zis = new ZipInputStream(is);
    ZipEntry ze = zis.getNextEntry();
    String rootFolderName = null;
	if(ze.isDirectory()){
		rootFolderName = ze.getName();
		ze = zis.getNextEntry();
	}
    byte[] buffer = new byte[4096];

    while (ze != null)
    {
      String fileName = ze.getName();
      if(rootFolderName!=null){
		   fileName=fileName.substring(rootFolderName.length());
		}
      File newFile = new File(outputPath + File.separator + fileName);
      FileOutputStream fos = new FileOutputStream(newFile);

      int len;
      while ((len = zis.read(buffer)) > 0)
      {
        fos.write(buffer, 0, len);
      }

      fos.close();
      ze = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
  }

  private static final String getVersionStr()
  {
    return String.format("%.2f", SnapshotContext.getVersion())
        + (ObjStreamUtil.DEFAULT_SERIALIZER == ObjStreamUtil.JAVA_SERIALIZER ? ".javaext"
            : ".kryo");
  }

  protected String getSnapshotPath(String schema, String version, String key)
  {
    return getSnapshotFolder(version) + FILE_SEPARATOR + schema + "-" + key
        + ".snapshot";
  }

  protected void writeSnapshot(String schema, String key, byte[] snapshot)
      throws IOException
  {
    String version = getVersionStr();
    String filepath = getSnapshotPath(schema, version, key);
    String snapshotFolder = new File(filepath).getParent();
    new File(snapshotFolder).mkdirs();
    Path path = Paths.get(filepath);
    Files.write(path, snapshot);
    // addLineSeparatorWithMessage("Snapshot created : "+filepath);
  }

  protected byte[] readSnapshot(String schema, String version, String key)
      throws IOException
  {
    String filepath = getSnapshotPath(schema, version, key);
    // addLineSeparatorWithMessage("Loading Snapshot : "+filepath +
    // " schema:"+schema+" version:"+version + " key:"+key);
    Path path = Paths.get(filepath);
    byte[] bytes = Files.readAllBytes(path);
    return bytes;
  }

  private void dumpExceptions()
  {
    List<Throwable> exceptions = CQSFaultHandler.getInstance().getExceptions();
    for (Throwable e : exceptions)
    {
      System.out.println("********* " + e);
    }
  }

  private boolean checkExceptionFromLoadSnapshot()
  {
    List<Throwable> exceptions = CQSFaultHandler.getInstance().getExceptions();
    if (exceptions.size() == 0)
      return true;
    for (Throwable e : exceptions)
    {
      if (e instanceof ExecException)
      {
        ExecException ee = (ExecException) e;
        ErrorCode ec = ee.getErrorCode();
        if (ec == ExecutionError.SNAPSHOT_LOAD_ERROR_CNF
            || ec == ExecutionError.SNAPSHOT_LOAD_ERROR
            || ec == ExecutionError.SNAPSHOT_PROCESSING_ERROR)
          return false;
      }
      if (e instanceof java.io.IOException)
        return false;
      Throwable ce = e.getCause();
      if (ce != null && (ce instanceof java.io.IOException))
        return false;
      System.out.println("********* " + e);
    }
    return true;
  }

  protected void checkLoadAllVersions(CQLProcessor processor, TestMetadata md,
      String schema, int phase, boolean full) throws Exception
  {
    File[] versions = getVersions();
    if (versions == null)
      return;
    for (File fv : versions)
    {
      String version = fv.getName();
      if (version.indexOf(".") > 0)
      {
        version = version.substring(0, version.lastIndexOf("."));
      }
      
      // Check if version should be ignored.
      if(ignoredVersions.contains(version))
      {
        addLineSeparatorWithMessage("Skipping Versioning Test For Version:"+version);
        continue;
      }

      String snapshotFolder = getSnapshotFolder(version);
      File f = new File(snapshotFolder);
      if (!f.exists())
      {
        unzipFile(snapshotFolder, fv.getAbsolutePath());
      }
      String desc = "test:" + schema + " version:" + version;
      String msg = "Failed to load " + desc;
      boolean matchserial = (version.endsWith(".kryo") && ObjStreamUtil.DEFAULT_SERIALIZER == ObjStreamUtil.KRYO_SERIALIZER)
          || (version.endsWith(".javaext") && ObjStreamUtil.DEFAULT_SERIALIZER == ObjStreamUtil.JAVA_SERIALIZER);
      if (matchserial)
      {
        addLineSeparatorWithMessage("TestCase " + desc);
        setup(processor, md, schema, phase);
        try
        {
          if (full)
          {
            byte[] snapshot = readSnapshot(schema, version, FULL_SNAPSHOT_MARK);
            loadFullSnapshot(processor, schema, snapshot);
            assertTrue(msg, checkExceptionFromLoadSnapshot());
          } else
          {
            byte[] snapshot = readSnapshot(schema, version, JFULL_SNAPSHOT_MARK);
            loadFullSnapshot(processor, schema, snapshot);
            assertTrue(msg, checkExceptionFromLoadSnapshot());

            snapshot = readSnapshot(schema, version, JOURNAL_SNAPSHOT_MARK);
            loadJournalSnapshot(processor, schema, snapshot);
            assertTrue(msg, checkExceptionFromLoadSnapshot());
          }
        } catch (java.nio.file.NoSuchFileException e)
        {
          e.printStackTrace();
          teardown(processor, md, schema);
          addLineSeparatorWithMessage("Skipping Version Testing for test:"
              + schema + " version:" + version);
          continue;
          // To create new snapshot files use
          // mvn test -Dskip.ha.unit.tests=false -Dskip.unit.tests=true
          // -Dtest.createSnapshot=true
          // break;
        }
        start(processor, schema);
        processor.waitForServerStop();
        teardown(processor, md, schema);
        assertTrue(msg, checkExceptionFromLoadSnapshot());

        addLineSeparatorWithMessage("Done Version Test for " + desc);
      }
    }
  }

  /**
   * Executes the test case. This involves following steps: 1) Run setup() to
   * create necessary queries and streams 2) Load Query state from a snapshot
   * byte array (only if numPhases greater than 1) 3) Start query execution 4)
   * Wait for a fixed duration for query to process given data 5) Generate
   * Snapshot on the basis of query state 6) Run teardown() to remove all
   * queries and metadata
   */
  protected void runFullSnapshotTest(TestMetadata md, String schema)
      throws Exception
  {
    CQLProcessor processor = new CQLProcessor();
    runFullSnapshotTestBase(processor, md, schema);
  }

  protected void runFullSnapshotTestBase(CQLProcessor processor,
      TestMetadata md, String schema) throws Exception
  {
    if (isDebugMode)
      addLineSeparatorWithMessage("DEBUG MODE IS ON");
    else
      addLineSeparatorWithMessage("DEBUG MODE IS OFF");
    int attemptCount = 0;
    boolean isSuccessful = false;
    SnapshotContext.reset();
    while (attemptCount++ < MAX_ATTEMPTS && !isSuccessful)
    {
      byte[] snapshotBytes = null;
      int numPhases = getNumFullSnapshotTestPhases();
      for (int i = 1; i <= numPhases; i++)
      {
        setup(processor, md, schema, i);
        addLineSeparatorWithMessage("Phase:" + i
            + "- DDL registration completed");
        if (i != 1)
        {
          loadFullSnapshot(processor, schema, snapshotBytes);
          addLineSeparatorWithMessage("Phase:" + i
              + "- Loaded snapshot [snapshot-size=" + snapshotBytes.length
              + "]");
        }
        start(processor, schema);
        processor.waitForServerStop();
        addLineSeparatorWithMessage("Phase:" + i
            + "- CQL query execution finished");
        dumpExceptions();
        snapshotBytes = createFullSnapshot(processor, schema);
        if (createSnapshot && i == 1)
        {
          writeSnapshot(schema, FULL_SNAPSHOT_MARK, snapshotBytes);
        }
        addLineSeparatorWithMessage("Phase:" + i
            + "- New Snapshot generated [snapshot-size=" + snapshotBytes.length
            + "]");
        
        // Dump Query Plan (For Debug Mode Only)
        dumpQueryPlan(processor, md, schema, i);
        
        teardown(processor, md, schema);
        addLineSeparatorWithMessage("Phase:" + i
            + "- DDL deregistration completed ");
      }
      isSuccessful = verifyFullSnapshotOutput(md);
      if (!isSuccessful)
      {
        addLineSeparatorWithMessage("Attempting to run the test again..");
        processor = new CQLProcessor();
      }
    }
    assertTrue(isSuccessful);
    if (checkLoadVersions)
    {
      addLineSeparatorWithMessage("Beginning the Snapshot Version Based Loading Test");
      checkLoadAllVersions(processor, md, schema, 1, true);
    }
  }

  /**
   * Verify output files generated in this test case.
   */
  protected boolean verifyFullSnapshotOutput(TestMetadata md)
  {
    System.out.println("Verifying Test Output..");
    int numPhases = getNumFullSnapshotTestPhases();
    try
    {
      Set<String> queries = md.getDestinationMetadata().keySet();
      boolean isMatching = true;
      for (String query : queries)
      {
        for (int i = 1; (i <= numPhases) && isMatching; i++)
        {
          System.out.println("\tComparing Files...");
          String outFile = md.getOutFile(query, i);
          String logFile = md.getLogFile(query, i);

          // Canonicalized file should be in the target directory
          String outFileCanonicalized = outFile + "_canonicalized_out";
          String logFileCanonicalized = outFile + "_canonicalized_log";

          System.out.println("\tfile1:" + outFileCanonicalized);
          System.out.println("\tfile2:" + logFileCanonicalized);

          Canonicalizer.getInstance().processOutputFiles(outFile,
              outFileCanonicalized, logFile, logFileCanonicalized);
          File file1 = new File(outFileCanonicalized);
          File file2 = new File(logFileCanonicalized);
          isMatching = isMatching && FileUtils.contentEquals(file1, file2);
          if (!isMatching)
          {
            printFile(file1);
            printFile(file2);
          }
        }
        if (!isMatching)
          break;
      }
      return isMatching;
    } catch (java.io.IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }

  public void addSourceAndDestination(CQLProcessor processor, TestMetadata md,
      String schema, int phaseId, int batchId) throws Exception
  {
    Set<String> sources = md.getSourceMetadata().keySet();
    for (String source : sources)
    {
      String sourcePath = md.getInpFile(source, phaseId, batchId);
      if (md.getSourceType(source) == SourceType.STREAM)
        invokeDDL(processor, schema, "alter stream " + source
            + " add source \"<EndPointReference><Address>file://" + sourcePath
            + "</Address></EndPointReference>\"");
      else
        invokeDDL(processor, schema, "alter relation " + source
            + " add source \"<EndPointReference><Address>file://" + sourcePath
            + "</Address></EndPointReference>\"");
    }

    // Generate and invoke DDLs to add destination to query
    // Generate and invoke DDLs to start query
    Set<String> queries = md.getDestinationMetadata().keySet();
    for (String query : queries)
    {
      String destPath = md.getOutFile(query, phaseId, batchId);
      invokeDDL(processor, schema, "alter query " + query
          + " add destination \"<EndPointReference><Address>file://" + destPath
          + "</Address></EndPointReference>\"");
      invokeDDL(processor, schema, "alter query " + query + " start");
    }
  }

  protected void runJournalSnapshotTest(TestMetadata md, String schema)
      throws Exception
  {
    runJournalSnapshotTestBase(null, md, schema);
  }

  protected void runJournalSnapshotTestBase(CQLProcessor processor,
      TestMetadata md, String schema) throws Exception
  {
    if (isDebugMode)
      addLineSeparatorWithMessage("Debug Mode is ON");
    else
      addLineSeparatorWithMessage("Debug Mode is OFF");

    int attemptCount = 0;
    boolean isSuccessful = false;
    if (processor == null)
      processor = new CQLProcessor();
    SnapshotContext.reset();
    while (attemptCount++ < MAX_ATTEMPTS && !isSuccessful)
    {
      // //////////// Phase 1 /////////////////////
      byte[] fullSnapshotBytes = null;
      byte[] journalSnapshotBytes = null;
      setup(processor, md, schema, 1);
      addLineSeparatorWithMessage("Phase-1: DDL registration completed");

      start(processor, schema);
      processor.waitForServerStop();
      addLineSeparatorWithMessage("Phase-1: CQL query execution finished");
      dumpExceptions();

      fullSnapshotBytes = createFullSnapshot(processor, schema);
      addLineSeparatorWithMessage("Phase-1:New Snapshot generated ");
      if (createSnapshot && attemptCount == 0)
      {
        writeSnapshot(schema, JFULL_SNAPSHOT_MARK, fullSnapshotBytes);
      }

      // Dump Query Plan (For Debug Mode Only)
      dumpQueryPlan(processor, md, schema, 1);
      
      teardown(processor, md, schema);
      addLineSeparatorWithMessage("Phase-1:DDL deregistration completed ");

      // /////////// Phase 2 ////////////////////////
      setup(processor, md, schema, 2, 1);
      addLineSeparatorWithMessage("Phase-2 Batch 1: DDL registration completed");

      loadFullSnapshot(processor, schema, fullSnapshotBytes);
      addLineSeparatorWithMessage("Phase-2 Batch 1:Loaded snapshot from file ");

      // Start batch for incremental snapshot
      processor.getCEPServer().startBatch(schema, false);
      start(processor, schema);
      processor.waitForServerStop();

      processor.getCEPServer().endBatch(schema, false);
      addLineSeparatorWithMessage("Phase-2 Batch 1: CQL query execution finished");
      dumpExceptions();

      // Create incremental snapshot for batch 1
      journalSnapshotBytes = createSnapshot(processor, schema, false);
      addLineSeparatorWithMessage("Phase-2 Batch 1: New Journal Snapshot generated in file:");
      if (createSnapshot && attemptCount == 0)
      {
        writeSnapshot(schema, JOURNAL_SNAPSHOT_MARK, journalSnapshotBytes);
      }
      // Dump Query Plan (For Debug Mode Only)
      dumpQueryPlan(processor, md, schema, 2);
      
      teardown(processor, md, schema);
      addLineSeparatorWithMessage("Phase-2: DDL deregistration completed ");

      // //////////// Phase 3 //////////////////////
      setup(processor, md, schema, 3);

      // Load full snapshot generated in phase 1
      loadFullSnapshot(processor, schema, fullSnapshotBytes);
      addLineSeparatorWithMessage("Phase-3: Loaded full snapshot from file ");

      // Apply snapshots generated in phase 2
      addLineSeparatorWithMessage("Phase-3: Applied journal snapshot from file ");
      loadJournalSnapshot(processor, schema, journalSnapshotBytes);

      // Run the queries
      start(processor, schema);
      processor.waitForServerStop();
      addLineSeparatorWithMessage("Phase-3: CQL query execution finished");
      dumpExceptions();

      // Dump Query Plan (For Debug Mode Only)
      dumpQueryPlan(processor, md, schema, 3);
      
      addLineSeparatorWithMessage("Phase:" + 3
          + "- Dumped Query Plan into file "
          + md.getQueryPlanDumpFile(3, schema));

      // Remove all the queries
      teardown(processor, md, schema);
      addLineSeparatorWithMessage("Phase-3: DDL deregistration completed ");

      // //////////// Verification //////////////////////////////
      isSuccessful = verifyJournalOutput(md);
      addLineSeparatorWithMessage("Attempting to run the test again..");
    }
    assertTrue(isSuccessful);
    if (checkLoadVersions)
    {
      addLineSeparatorWithMessage("Phase-4: check loading ");
      checkLoadAllVersions(processor, md, schema, 1, false);
    }
  }

  /**
   * Verify output files generated in this test case.
   */
  protected boolean verifyJournalOutput(TestMetadata md)
  {
    // Assumption: Journal snapshot is taken in second phase
    int journalSnapshotPhaseId = 2;

    System.out.println("Verifying Test Output..");
    int numPhases = getNumJournalSnapshotTestPhases();
    try
    {
      Set<String> queries = md.getDestinationMetadata().keySet();
      boolean isMatching = true;
      for (String query : queries)
      {
        for (int i = 1; (i <= numPhases) && isMatching; i++)
        {
          System.out.println("\tComparing Files...");
          String outFile = null;
          String logFile = null;
          if (i == journalSnapshotPhaseId)
          {
            outFile = md.getOutFile(query, i, 1);
            logFile = md.getLogFile(query, i, 1);
          } else
          {
            outFile = md.getOutFile(query, i);
            logFile = md.getLogFile(query, i);
          }

          // Canonicalized file should be in the target directory
          String outFileCanonicalized = outFile + "_canonicalized_out";
          String logFileCanonicalized = outFile + "_canonicalized_log";

          System.out.println("\tfile1:" + outFileCanonicalized);
          System.out.println("\tfile2:" + logFileCanonicalized);

          Canonicalizer.getInstance().processOutputFiles(outFile,
              outFileCanonicalized, logFile, logFileCanonicalized);

          File file1 = new File(outFileCanonicalized);
          File file2 = new File(logFileCanonicalized);
          isMatching = isMatching && FileUtils.contentEquals(file1, file2);
          if (!isMatching)
          {
            printFile(file1);
            printFile(file2);
          }
        }
        if (!isMatching)
          break;
      }
      return isMatching;
    } catch (java.io.IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }

  public void printFile(File file)
  {
    System.err.println("File:" + file.getAbsolutePath());
    BufferedReader br;
    if (file != null)
    {
      try
      {
        br = new BufferedReader(new FileReader(file));
        String nextLine;
        while ((nextLine = br.readLine()) != null)
          System.err.println(nextLine);
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
