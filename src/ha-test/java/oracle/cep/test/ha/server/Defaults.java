package oracle.cep.test.ha.server;

public class Defaults
{
  public static final String CQLENGINE_SERVICENAME = "default";
  public static final String DEFAULT_SCHEMA = "";       //schema name with non qualified names.
  
  public static final String METADATA_STORAGE_FOLDER = "storage";
  public static final String METADATA_STORAGE_NAME = "oracle.cep.storage.BerkeleyDB.BDBStorageMgr";
  public static final boolean METADATA_RESTORE = true;
  public static final String METADATA_USE_BDB = "cqservice.use_bdb"; //property for testing purpose
  
  public static final String TRACE_FOLDER = "trace";
  
  public static final String DB_RESULTSET = "Db.ResultSet";
  
  //IEEE 754R Decimal128 34 significant digits, 34/12
  public static final int DEFAULT_PRECISION = 10;
  public static final int DEFAULT_SCALE = 0;
  
  public static final String TUPLE_KIND_INSERT = "+";
  public static final String TUPLE_KIND_DELETE = "-";
  public static final String TUPLE_KIND_UPDATE = "U";
  public static final String TUPLE_KIND_HEARTBEAT = "H";
  public static final String TUPLE_KIND_START = "S";
  
  public static final String HB_MARK = "_HB_";  //heartbeat mark for historical data for testing purpose
}
