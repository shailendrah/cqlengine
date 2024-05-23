/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/Constants.java /main/86 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
     Constants used in the CEP system

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/14/13 - change default time slice to 1M from 1K to ensure
                           all archiver records from dim side are flushed at
                           start time
    udeshmuk    07/23/13 - bug 16813624: introduce useMillisTs as config param
                           so that system timestamped sources use
                           System.currentTimeMillis()
    sbishnoi    03/12/13 - bug 14309560
    udeshmuk    07/17/12 - add default values for beam_transaction_context
                           related names
    sbishnoi    04/04/12 - adding config parameter for targetSQLType in context
                           of archived relations
    pkali       03/20/12 - groupbyexpr support
    alealves    10/28/11 - XbranchMerge alealves_bug-12630784_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    08/29/11 - adding constant for interval year to month
    anasrini    08/10/11 - add DEFAULT_STATS_ENABLED
    anasrini    07/08/11 - set DEFAULT_SPILL_QUEUESRC to false
    anasrini    04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
    anasrini    03/29/11 - add CQL_RESERVED_PREFIX
    anasrini    03/23/11 - add DEFAULT_DEGREE_OF_PARALLELISM
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/06/10 - add constants for destination properties
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sbishnoi    06/22/10 - adding threshold value for max allowed external rows
    sbishnoi    04/26/10 - adding a new constant HBT_TIMEOUT
    hopark      10/30/09 - support large string
    sbishnoi    09/24/09 - adding const TABLE_FUNCTION_RETURN_TYPE
    sborah      06/23/09 - support for bigdecimal
    sbishnoi    04/16/09 - making Constants.MIN_EXEC_TIME to Long.MIN_VAL
    sbishnoi    04/03/09 - adding constant MIN_EXEC_TIME
    udeshmuk    04/03/09 - add MAX_EXEC_TIME and MIN_EXEC_TIME
    sborah      04/01/09 - reduce initial sizes
    hopark      03/25/09 - add USE_MESSAGECATALOG
    hopark      02/17/09 - support boolean as external datatype
    anasrini    02/12/09 - add REGRESS_PUSH_MODE
    parujain    01/13/09 - metadata in memory
    hopark      12/04/08 - add queuesrc spill thresholds
    hopark      11/28/08 - move TIMESTAMP_FORMAT to Config
    parujain    11/17/08 - increase constants size
    parujain    09/26/08 - add Constant
    sbishnoi    09/23/08 - modify jdbc related constant
    sbishnoi    09/08/08 - adding few constants related to schema
    hopark      08/14/08 - remove obsolete metadata.store
    udeshmuk    06/16/08 - add constant for default subset name.
    sbishnoi    06/10/08 - Adding DEFAULT_IS_METADATA_CLEANUP_ON_STARTUP
    sbishnoi    05/15/08 - changing max queries from 100 to 1000
    rkomurav    05/14/08 - add duration symbol
    hopark      03/17/08 - add CONFIGMGR_BEAN_NAME
    najain      04/03/08 - 
    udeshmuk    03/18/08 - add constant for infinite_sched_wait_time
    hopark      03/12/08 - add spill.queuesrc
    hopark      03/08/08 - add spill mode
    mthatte     03/14/08 - adding default jdbc port
    mthatte     02/27/08 - adding URL's for JDBC
    parujain    02/07/08 - 
    hopark      02/04/08 - add trc file format
    hopark      02/04/08 - turn on dynamic page
    parujain    01/30/08 - 
    udeshmuk    01/29/08 - support for double data type.
    hopark      12/20/07 - add xmltag for logging config
    udeshmuk    01/22/08 - add NULL_TIMESTAMP.
    hopark      10/31/07 - add pagedlist
    najain      10/19/07 - add MAX_XMLTYPE_LENGTH
    parujain    12/18/07 - inner and outer constants
    parujain    12/04/07 - Add constants for prepared stmt
    mthatte     11/06/07 - adding scheman and catalog for jdbc
    mthatte     11/20/07 - changing Timestamp_Format to CEPDateFormat
    mthatte     11/06/07 - adding schema and catalog for jdbc
    hopark      10/12/07 - evictPolicyParam follows the same convention as
                           others
    hopark      09/04/07 - add tuple.usepage
    mthatte     10/17/07 - adding length for datatypes
    mthatte     09/07/07 - Adding universal timestamp format
    sbishnoi    08/27/07 - change constant MAX_QUERIES from 50 to 100
    sbishnoi    08/27/07 - change constant MAX_OUT_BRANCHING from 16 to 100
    hopark      07/31/07 - add dynamic tuple class
    najain      07/12/07 - add NUM_THREADS
    najain      07/09/07 - remove SCHED_THREADED
    hopark      06/08/07 - fix config
    hopark      05/14/07 - move debug flags to LogFlags
    skmishra    03/29/07 - default scheduler run time to 30000
    parujain    03/21/07 - threaded scheduler
    parujain    03/16/07 - remove debug constants
    hopark      03/14/07 - merge metadata/spill storage settings
    hopark      02/19/07 - add properties for storage
    parujain    02/13/07 - STORE LOCATION
    dlenkov     01/16/07 - added SCHED_RUN
    hopark      12/06/06 - increase max stub
    parujain    11/29/06 - Constant for Sched.run
    parujain    11/29/06 - Remove Constant
    parujain    11/22/06 - Start Size for array
    dlenkov     11/16/06 - added MAX_OCERLOADS
    anasrini    11/14/06 - 
    parujain    11/09/06 - invalid value
    najain      10/30/06 - debugging support
    najain      10/25/06 - add config property names and their default values
    najain      08/15/06 - increase MAX_OUT_BRANCHING to 16
    parujain    08/11/06 - Length timestamp
    ayalaman    07/29/06 - add MAX PARTITION BY attributes
    anasrini    06/19/06 - add MAX_CHAR_LENGTH and MAX_BYTE_LENGTH 
    najain      06/14/06 - add MAX_NUM_STUBS
    anasrini    04/20/06 - add MAX_AGGR_ATTRS 
    najain      04/06/06 - cleanup
    anasrini    03/22/06 - add constant BYTE_SIZE 
    anasrini    03/14/06 - add MAX_INSTRS 
    najain      03/03/06 - add MAX_OUT_BRANCHING
    anasrini    02/26/06 - add constants for MAX_PRED, MAX_PROJ, 
                           MAX_GROUP_ATTRS 
    anasrini    02/21/06 - add constant for INFINITE 
    anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/Constants.java /main/86 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Constants used in the CEP system
 * 
 * @since 1.0
 */

public class Constants 
{ 

  // true : use wlevs I18N message catalog
  // false : oracle error message bundle
  public static final boolean I18N_MESSAGE_CATALOG = true;
  
  /**
   * Default lengths for datatypes
   */	
  public static final int INTEGER_LENGTH     = 32;
  public static final int BIGINT_LENGTH      = 64;
  public static final int TIMESTAMP_LENGTH   = 64;
  public static final int FLOAT_LENGTH       = 32;
  public static final int DOUBLE_LENGTH      = 64;
  public static final int INTERVAL_LENGTH    = 64;
  public static final int OBJECT_LENGTH      = 64;
  public static final int BIGDECIMAL_LENGTH  = 64;
  public static final int BOOLEAN_LENGTH     =  1;	
  public static final int BIGDECIMAL_PRECISION = 52;

  
  /** JDBC Related Constants */

  /* URL for JDBC single task */ 
  public static final String CEP_LOCAL_URL  = "jdbc:oracle:ceplocal";

  /* default port for client-server jdbc */
  public static final int DEFAULT_JDBC_PORT = 1199;
    
  /* default catalog name for JDBC interface*/
  public static final String CEP_CATALOG    = "Oracle-CEP";

  /* default schema name for JDBC interface*/
  public static final String DEFAULT_SCHEMA = "public";
  
  /* default system table schema */
  public static final String DEFAULT_SYSTEM_TABLE_SCHEMA = "system";
  
  /* default cep service name for JDBC interface */
  public static final String DEFAULT_CEP_SERVICE_NAME = "sys";
  
  /**
  *	The format used to denote time in CEP
  */
  //public static final SimpleDateFormat	TIMESTAMP_FORMAT = new CEPDateFormat();//new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
  
  /**
   * Value that indicates null timestamp for a tuple
   */
  public static final long NULL_TIMESTAMP = Long.MAX_VALUE;
  
  /** 
   * Initial number of registered (named) tables (streams/relations)
   * in the system
   */
  public static final int INITIAL_TABLES_CAPACITY = 50;

    /** Initial number of attributes per (registered) table */
  // previously 50
  public static final int INITIAL_ATTRS_NUMBER = 5;

  /** Maximum number of queue size for the operator */
  public static final int MIN_QUEUESZ = 10;
  public static final int MAX_QUEUESZ = 250;
  
  /** Invalid Value */ 
  public static final int INVALID_VALUE = -1;

  /** Initial number of operators reading from an operator */
  // previously 1000
  public static final int INTIAL_NUM_OUT_BRANCHING = 5;

  /** The number of seconds in UNBOUNDED */
  public static final long INFINITE = Long.MAX_VALUE;

  /** Intial number of attributes in the group by clause of an SFW query */
  public static final int INITIAL_NUM_GROUP_EXPRS = 10;
  public static final int INITIAL_NUM_GROUP_ATTRS = 10;

  /** Initial number of attributes in the partition by clause */
  public static final int INITIAL_NUM_PARTN_ATTRS = 10; 

  /** 
   * Initial number of aggregation expressions in select list of
   * an SFW query 
   */
  public static final int INITIAL_NUM_AGGR_ATTRS = 10;

  /** Maximum Inputs operators */
  public static final int MAX_INPUT_OPS = 10;
  
  /** Initial readers (number of stubs) for a store */
  // previously 25
  public static final int INTIAL_NUM_STUBS = 25;

  /** Maximum number of instructions in an AEval */
  public static final int MAX_INSTRS = 50;

  /** Number of bits in a byte */
  public static final int BITS_PER_BYTE = 8;

  /** Maximum of the maxlen for a CHAR type */
  public static final int MAX_CHAR_LENGTH = Integer.MAX_VALUE;

  /** Maximum of the maxlen for a BYTE type */
  public static final int MAX_BYTE_LENGTH = Integer.MAX_VALUE;

  /** Maximum of the maxlen for a XMLTYPE type */
  public static final int MAX_XMLTYPE_LENGTH = Integer.MAX_VALUE;
  
  /**Maximum length of timestamp MM/dd/yyyy hh:mm:ss */
  public static final int MAX_TIMESTAMP_LENGTH = 19;

  /** Maximum number of overloaded functions with the same name */
  public static final int MAX_OVERLOADS = 10;


  ////////////////  CONFIG PROPERTY NAMES AND THEIR DEFAULT VALUES ///////////

  /** Fully Qualified class name of the scheduler */
  public static final String SCHED_NAME = "sched.name";
  public static final String DEFAULT_SCHED_NAME =
    "oracle.cep.execution.scheduler.FIFOScheduler";

  /** Time slice for the scheduler */
  public static final String SCHED_TIME_SLICE = "sched.timeslice";
  public static final int    DEFAULT_SCHED_TIME_SLICE = 1000000;
  /** Time in milliseconds for which infinitely running scheduler manager 
   *  thread will sleep if no operator is available for scheduling */
  public static final int    INFINITE_SCHED_WAIT_TIME = 5;

  /** Debugging level */
  public static final String DEBUGGING_LEVEL = "debugLevel";
  
  /** Logging */
  public static final String LOG_USE_XMLTAG = "log.xmltag";
  public static final boolean DEFAULT_LOG_USE_XMLTAG = true;
  
  public static final String LOG_TRACE_FOLDER = "log.trace_folder";
  public static final String DEFAULT_LOG_TRACE_FOLDER = "@HOME@/diag";

  public static final String LOG_TRACE_FILE_POSTFIX = "log.trace_file_postfix";
  public static final String DEFAULT_LOG_TRACE_FILE_POSTFIX = "@TRC_ID@_@TRC_DATETIME@";

  /** Scheduler Run time */
  public static final String SCHED_RUN_TIME = "sched.run";
  public static final long   DEFAULT_RUN_TIME = 1000000;

  /** 
   * Is a new thread started when Scheduler is started
   * This is just used for testing purpose in which we run tests
   *  with finite duration
   */
  public static final String SCHED_ON_NEW_THREAD = "sched_on_new_thread";
  public static final boolean DEFAULT_SCHED_ON_NEW_THREAD = true;

  /** Number of threads */
  public static final String SCHED_NUM_THREADS = "sched.num_threads";
  public static final int DEFAULT_SCHED_NUM_THREADS = 1;

  public static final String SCHED_THREADPOOL_QSIZ = "sched.thpool_qsize";

  /** Direct Interop scheduler */
  public static final String DIRECT_INTEROP = "sched.direct_interop";
  public static final boolean DEFAULT_DIRECT_INTEROP = true;

  /** Emulating push mode in regressions when using directInterop */
  public static final String REGRESS_PUSH_MODE = "regress.push_mode";
  public static final boolean DEFAULT_REGRESS_PUSH_MODE = false;

  /** Dynamic Tuple class generation */
  public static final String DYNAMIC_TUPLE_CLASS = "tuple.dynamic_class_gen";
  public static final boolean DEFAULT_DYNAMIC_TUPLE_CLASS = false;
  
  /** Dynamic Page class generation */
  public static final String DYNAMIC_PAGE_CLASS = "page.dynamic_class_gen";
  public static final boolean DEFAULT_DYNAMIC_PAGE_CLASS = true;

  /** Paged List use in memory mode */
  public static final String PAGED_LIST = "page.list.use";
  public static final boolean DEFAULT_PAGED_LIST = false;

  /** Initial page table size for lists */
  public static final String LIST_INIT_PAGETABLESIZE = "page.list.init_table_size";
  public static final int DEFAULT_LIST_PAGETABLE_SIZE = 2;
  
  /** page size for lists */
  public static final String LIST_PAGESIZE = "page.list.page_size";
  public static final int DEFAULT_LIST_PAGESIZE = 1024;

  /** minimum nodes in a page */
  public static final String LIST_MINNODES_PAGE = "page.list.min_objs";
  public static final int DEFAULT_LIST_MINNODES_PAGE = 200;

  /** Paged Tuple use in memory mode */
  public static final String PAGED_TUPLE = "page.tuple.use";
  public static final boolean DEFAULT_PAGED_TUPLE = false;

  /** Initial page table size for tuples */
  public static final String TUPLE_INIT_PAGETABLESIZE = "page.tuple.init_table_size";
  public static final int DEFAULT_TUPLE_PAGETABLE_SIZE = 5;

  /** page size for tuples */
  public static final String TUPLE_PAGESIZE = "page.tuple.page_size";
  public static final int DEFAULT_TUPLE_PAGESIZE = 4096;

  /** minimum nodes in a page */
  public static final String TUPLE_MINNODES_PAGE = "page.list.min_objs";
  public static final int DEFAULT_TUPLE_MINNODES_PAGE = 200;

  /** Storage properties */
  /** storage classes
   *  - storage.class.0 = MemStorage, oracle.cep.storage.memory.MemStorage
   *  - storage.class.1 = BDBStorage, oracle.cep.storage.berkeleyDB.BDEStorage
   */
  public static final String STORAGE_CLASS = "storage.class.{0}";
  public static final String STORAGE_FOLDER = "storage.folder";
  public static final String DEFAULT_STORAGE_FOLDER = "@HOME@/storage";
  public static final String STORAGE_CACHE = "storage.cache_size";
  public static final long DEFAULT_STORAGE_CACHESIZE = -10;     //10%
  
  /** Metadata properties */
  /** 
   * Fully Qualified class name of the class implementing the Persistant Store 
   * for metadata
   */
  public static final String DEFAULT_METADATA_STORAGE_FOLDER = ".";
  public static final String METADATA_STORAGE_NAME = "metadata.storage";
  public static final String DEFAULT_METADATA_STORAGE_NAME = "MemStorage";
  public static final boolean DEFAULT_IS_METADATA_CLEANUP_ON_STARTUP = true;
  
  /** Spill properties */
  public static final String DEFAULT_SPILL_STORAGE_FOLDER = "data";
  public static final String SPILL_STORAGE_NAME = "spill.storage";
  public static final String DEFAULT_SPILL_STORAGE_NAME = null;
  public static final String SPILL_FULLMODE = "spill.full";
  public static final boolean DEFAULT_SPILL_FULLMODE = false;
  public static final String SPILL_EVICT_POLICY = "spill.evict_policy";
  public static final String SPILL_EVICT_POLICYPARAM = "spill.evict_policy_param";
  public static final String SPILL_QUEUESRC = "spill.queuesrc";
  public static final boolean DEFAULT_SPILL_QUEUESRC = false;
  public static final int DEFAULT_SPILL_QUEUESRC_NORMAL_THRESHOLD = -30; //30%
  public static final int DEFAULT_SPILL_QUEUESRC_PARTIAL_THRESHOLD = -20; //20%
  public static final int DEFAULT_SPILL_QUEUESRC_FULL_THRESHOLD = -10; //10%
  public static final int DEFAULT_SPILL_QUEUESRC_SYNC_THRESHOLD = -5; //5%
  
  /** Constants for prepared statement to verify external relation schema */
  public static final int INTEGER_CONST = 2;
  public static final float FLOAT_CONST = (float)3.2f;
  public static final double DOUBLE_CONST = (double)3.2d;
  public static final double BIGDECIMAL_CONST = (double)3.2d;
  public static final String CHAR_CONST = "abc";
  public static final long BIGINT_CONST = 234;
  public static final char[] BYTE_CONST = {'1','2','3'};
  public static final long TIMESTAMP_CONST = 270930142;
  public static final String INTERVAL_CONST = "4 5:12:10.222";
  public static final String INTERVALYM_CONST = "4-5";
  public static final boolean BOOLEAN_CONST = false;
  
  // Constants for inner and outer in case of joins
  public static final int OUTER = 0;
  public static final int INNER = 1;
  
  /** Pattern constants */
  public static final String durationSymbol      = "#";
  public static final String DEFAULT_SUBSET_NAME = "0"; //user will not be able to specify this subset name.
  
  /** Undefined duration */
  public static final int UNDEFINED_TARGET_TIME = -55555;
  

  public static final String DEFAULT_CONFIG_FILE = "ApplicationContext.xml";

  // Spring bean names
  public static final String CEPSERVER_BEAN_NAME = "cepServer";
  public static final String CEPMGR_BEAN_NAME = "cepManager";
  public static final String CONFIGMGR_BEAN_NAME = "config";
  public static final String EVICTPOLICY_BEAN_NAME = "evictPolicy";
  public static final String STORAGE_BEAN_NAME = "storage";
  public static final String LOGCONFIG_BEAN_NAME = "logConfig";

  /** Maximum possible timestamp value of a tuple*/
  public static final long MAX_EXEC_TIME = Long.MAX_VALUE;
  /** Minimum possible timestamp value of a tuple*/
  public static final long MIN_EXEC_TIME = Long.MIN_VALUE;
  
  /** Reminder interval - 100 milliseconds */
  public static final int DEFAULT_HBT_TIMEOUT_MILLIS = 100;
  public static final int DEFAULT_HBT_TIMEOUT_NANOS = 100000000;
 
  public static final long DEFAULT_EXTERNAL_ROWS_THRESHOLD = 100000L;

  /** Constants for destination properties */
  public static final String USE_UPDATE_SEMANTICS = "USE UPDATE SEMANTICS";
  public static final String BATCH_OUTPUT = "BATCH OUTPUT";
  public static final String PROPAGATE_HB = "PROPAGATE HEARTBEAT";

  /** Constants for partition parallelism support */
  public static final int DEFAULT_DEGREE_OF_PARALLELISM = 4;

  /** Constant relatd to gathering statistics */
  public static final boolean DEFAULT_STATS_ENABLED = false;
  
  /** Constant for default target sql type of Archiver query*/
  public static final SQLType DEFAULT_TARGET_SQL_TYPE = SQLType.ORACLE;
  
  /** Constant for default value of whether system ts sources should use 
   *  millis or nanos */
  public static final boolean DEFAULT_USE_MILLIS_TS = false;
  
  /** Constant for default name of BEAM_TRANSACTION_CONTEXT table */
  public static final String DEFAULT_BEAM_TXN_CTX_NAME = "BEAM_TRANSACTION_CONTEXT";
  
  /** Constant for default context col name in BEAM_TRANSACTION_CONTEXT table */
  public static final String DEFAULT_CONTEXT_COL_NAME = "TRANSACTION_CID";
  
  /** Constant for default txn col name in BEAM_TRANSACTION_CONTEXT table */
  public static final String DEFAULT_TXN_COL_NAME = "TRANSACTION_TID";
  
  /**
   * The prefix for names of objects that are internally created by the
   * system.
   *
   * An example is the name of the query object created fo a view.
   * Another example is the schema name for partition parallelism.
   *
   * Names with this prefix cannot be used by the end user.
   */
  public static final String CQL_RESERVED_PREFIX = "cql$";
  
  /** Default & max precision for leading and trailing fields of interval datatype */
  public static final int DEFAULT_INTERVAL_LEADING_PRECISION = 2;
  public static final int MAX_INTERVAL_LEADING_PRECISION = 9;
  public static final int DEFAULT_INTERVAL_FRACTIONAL_SECONDS_PRECISION = 6;
  public static final int MAX_INTERVAL_FRACTIONAL_SECONDS_PRECISION = 9;

  public static final String CQL_THIS_POINTER = "_this";

  // Prefix of the work folder
  public static final String WORK_FOLDER_PREFIX = "CqlTemp";
}
