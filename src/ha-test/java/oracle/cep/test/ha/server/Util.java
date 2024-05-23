package oracle.cep.test.ha.server;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Properties;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.BooleanAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.IntervalAttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;

/**
 * Util has the set of utility static methods.
 * 
 *  @version $Header: beam/main/modules/cqservice/core/src/main/java/com.oracle.cep.spark/util/Util.java /main/10 2014/01/21 21:38:13 shusun Exp $
 *  @author  hopark  
 *  @since   12c
 */
public class Util
{
  public static String getJVMname()
  {
    return ManagementFactory.getRuntimeMXBean().getName();
  }
  
  public static byte[] getIPAddress() throws Exception
  {
    InetAddress ownIP=InetAddress.getLocalHost();
    return ownIP.getAddress();
  }
  
  public static String getHostName()
  {
    String hostname = System.getenv("COMPUTERNAME");
    if (hostname == null)
      hostname = System.getenv("HOSTNAME");
    return hostname;
  }
  
  public static String getUserName()
  {
    return System.getProperty("user.name");  
  }
  
  public static String getCurrentFolder()
  {
    return System.getProperty("user.dir");
  }
  
  public static String getUserHome()
  {
    return System.getProperty("user.home");
  }
  
  public static String getTempPath(String path, String filename)
  {
    String tempdir = System.getProperty("java.io.tmpdir");

    StringBuilder b = new StringBuilder();
    b.append(tempdir);
    if ( !(tempdir.endsWith("/") || tempdir.endsWith("\\")) )
       b.append(File.separator);
    if (path != null) b.append(path);
    if (filename != null) b.append(filename);
    return b.toString();
  }
  
  public static String makeFilePath(String path, String filename)
  {
    StringBuilder b = new StringBuilder();
    if (File.separator != "/")
    {
      b.append(path.replace("/", File.separator));
    }
    else
    {  
      b.append(path);
    }
    if (filename != null)
    {
      b.append(File.separator);
      b.append(filename);
    }
    return b.toString();
  }   

  public static boolean deleteDir(File dir)
  {
    if (dir.isDirectory())
    {
      String[] children = dir.list();
      for (String child : children)
      {
        boolean success = deleteDir(new File(dir, child));
        if (!success)
        {
          return false;
        }
      }
    }
    return dir.delete();
  }
  
  /*
  public static void setMetadataStorage(String storageFolder, boolean cleanUp)
  {
    if (storageFolder == null)
    {
      String workFolder  = BeamProperties.getProperty(BeamProperties.WORK_FOLDER, null);
      if (workFolder == null)
        workFolder = Util.getTempPath(null  , null);
      if (workFolder.endsWith(File.separator))
        workFolder.substring(0, workFolder.length()-1);
      storageFolder  = BeamProperties.getProperty(BeamProperties.STORAGE_FOLDER, workFolder + File.separator + Defaults.METADATA_STORAGE_FOLDER);
    }
    if (cleanUp)
    {
      File sf = new File(storageFolder);
      deleteDir(sf);
    }
    System.setProperty(BeamProperties.STORAGE_FOLDER, storageFolder);
  }
  */
  
  public static String TupleKind2Str(TupleValue tupleValue)
  {
    String tupleKind = null;
    TupleKind kind = tupleValue.getKind();
    switch (kind)
    {
    case PLUS:
      tupleKind = Defaults.TUPLE_KIND_INSERT;
      break;
    case MINUS:
      tupleKind = Defaults.TUPLE_KIND_DELETE;
      break;
    case UPDATE:
      tupleKind = Defaults.TUPLE_KIND_UPDATE;
      break;    
    case HEARTBEAT:
      tupleKind = Defaults.TUPLE_KIND_HEARTBEAT;
      break;
    case START:
      tupleKind = Defaults.TUPLE_KIND_START;
      break;
    }
    return tupleKind;
  }
  
  public static TupleKind Str2TupleKind(String tupleKind)
  {  
    TupleKind kind = null;
    if (tupleKind.equals(Defaults.TUPLE_KIND_INSERT))
      kind = TupleKind.PLUS;
    else if (tupleKind.equals(Defaults.TUPLE_KIND_DELETE))
      kind = TupleKind.MINUS;
    else if (tupleKind.equals(Defaults.TUPLE_KIND_UPDATE))
      kind = TupleKind.UPDATE;
    else if (tupleKind.equals(Defaults.TUPLE_KIND_START))
      kind = TupleKind.START;
    else  
      kind = TupleKind.HEARTBEAT;
    return kind;
  }

  public static Properties loadSystemResourceProperties(String overridePropertyName, String defaultPropertyName)
    throws CEPException
  {
    // get the name of the resources
    String name = System.getProperty(overridePropertyName);
    if (name == null)
      name = defaultPropertyName;

    Properties props = new Properties();
    InputStream in = null;
    try
    {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      //if (LogUtil.isTraceEnabled())
      //{
        System.out.println("loading system resource properties from resource:" + name);
      //}
      in = loader.getResourceAsStream(name);
      if (in != null)
      {
        props.load(in);
      }
    } catch (Exception e)
    {
//      if (LogUtil.isFatalEnabled())
//      {
    	  System.out.println("Failed to load system resource properties " + name + "\n"
            + e.getMessage());
//      }
      throw new RuntimeException("FAILED_LOADING_SYSTEM_RESOURCE_PROPERTIES " + e + name);
    }
    if (props.size() == 0)
    {
      throw new RuntimeException("FAILED_LOADING_SYSTEM_RESOURCE_PROPERTIES "+ name);
    }
    return props;
  }
  
  public static TupleValue genTupleValue(String name, long time, boolean isHeartBeat, String[] attrNames, AttrType[] attrTypes, Object[] args) throws CEPException
  {
    if(isHeartBeat)
      return new TupleValue(name,time,null,isHeartBeat);

    AttributeValue[] attrs = new AttributeValue[args.length];
	  for(int pos = 0; pos < args.length; pos++)
	  {
		  String attrname = attrNames[pos];
		  AttrType dtype = attrTypes[pos];
		  Object v = args[pos];
		  switch(dtype) {
		  case INT: v = new IntAttributeValue(attrname, (Integer)v); break; 
		  case BYTE: v = new CharAttributeValue(attrname, ((String)v).toCharArray()); break; 
		  case CHAR: v = new CharAttributeValue(attrname, ((String)v).toCharArray()); break; 
		  case BIGINT: v = new BigintAttributeValue(attrname, (Long)v); break; 
		  case FLOAT: v = new FloatAttributeValue(attrname, (Float)v); break;
		  case DOUBLE: v = new DoubleAttributeValue(attrname, (Double)v); break;
		  case BOOLEAN: v = new BooleanAttributeValue(attrname, (Boolean)v); break;
		  case TIMESTAMP: v = new TimestampAttributeValue(attrname, (Long)v); break; 
		  case INTERVAL: v = new IntervalAttributeValue(attrname, (String)v); break; 
		  case OBJECT: v = new ObjAttributeValue(attrname, v); break; 
		  default: throw new RuntimeException(dtype + " is not supported");
		  //case BIGDECIMAL: v = new BigDecimalAttributeValue(attrname, (BigDecimal)v); break;
		  /*  
		    VOID, 
		    XMLTYPE, 
		    UNKNOWN, 
		    INTERVALYM
		    */
		  }
		  attrs[pos] = (AttributeValue)v;
	  }
	  return new TupleValue(name, time, attrs, isHeartBeat);
  }
  /*
  public static TupleValue genBatchEndMarkerEvent(String name, long time) 
    throws CEPException
  {
    TupleValue tuple = genTupleValue(name,time,true, null, null, null);
    tuple.setIsBatchEndMarker(true);
    return tuple;
  }*/
  
  public static Object[] getTupleAttrs(TupleValue tuple) throws CEPException
  {
	  	int n = tuple.getNoAttributes();
	  	Object[] attrs = new Object[n];
	  	for (int i = 0; i < n; i++)
	  	{
	  		attrs[i] = tuple.getObjectValue(i);
	  	}
	  	return attrs;
  }
}
