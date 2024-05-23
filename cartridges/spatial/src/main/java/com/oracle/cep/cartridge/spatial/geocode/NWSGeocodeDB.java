/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSGeocodeDB.java /main/1 2015/10/01 22:29:45 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSGeocodeDB.java /main/1 2015/10/01 22:29:45 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geocode;

import com.oracle.cep.cartridge.spatial.JsonUtil;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class NWSGeocodeDB
{
	private static final String GEOMETRY_PROPNAME = "geometry";
	private static final String GEOMETRY_TYPE_PROPNAME = "type";
	private static final String GEOMETRY_COORDS_PROPNAME = "coordinates";
	
	private String dbZipfilePath;
	private String dbPath;
	private String dbName;
	private String metadbName;
	private String indexdbName;
	private Environment env;
	private Database db;
	private Database metadb;
	private SecondaryDatabase secondaryIndexDb;
	private NWSRecord.SecondKeyCreator keyCreator; 
	private String primaryKey;
	private String secondaryKey;

	public NWSGeocodeDB(String dbpath)
	{
		dbpath = dbpath.replace("\\","/");
		int pos = dbpath.indexOf(".zip");
		if (pos > 0)
		{
			dbZipfilePath = dbpath;
			int spos = dbpath.lastIndexOf('/');
			if (spos < 0) spos = dbpath.lastIndexOf('\\');
			dbpath = dbpath.substring(spos+1, pos);
		}
		dbPath = dbpath;
		int spos = dbpath.lastIndexOf('/');
		if (spos < 0) spos = dbpath.lastIndexOf('\\');
		dbName = dbpath.substring(spos+1);
		metadbName = dbName + "_meta";
		indexdbName = dbName + "_index";
	}
	
	public void setDBPath(String dbpath)
	{
		dbPath = dbpath;
	}
	
	private void setKey(String primaryKey, String secondaryKey) {
		this.primaryKey = primaryKey;
		this.secondaryKey = secondaryKey;
	}

	private void openEnv(boolean create)
	{
		if (dbZipfilePath != null)
		{
		}
		try {
		    EnvironmentConfig envConfig = new EnvironmentConfig();
		    envConfig.setReadOnly(!create);
		    envConfig.setAllowCreate(create);
		    File f = new File(dbPath);
		    if (!f.exists())
		    {
		    	f.mkdirs();
		    }
		    else
		    {
		    	if (create)
		    	{
		    		cleanupDBPath(f);
		    	}
		    }
		    env = new Environment(f, envConfig);
		} catch (DatabaseException dbe) {
		    throw new RuntimeException("Failed to open env from " + dbPath, dbe);
		}
	}
	
	private void cleanupDBPath(File envf)
	{
		String[] fileNames = envf.list();
		for (String filename : fileNames) {
			String path = dbPath + File.separator + filename;
			File f = new File(path);
			if (f.isDirectory())
				continue;
			System.out.println("deleting "+path);
			f.delete();
		}
		
	}

	public void open(boolean create)
	{
		openEnv(create);
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setReadOnly(!create);
		    dbConfig.setAllowCreate(create);
		    db = env.openDatabase(null, dbName, dbConfig); 
		} catch (DatabaseException dbe) {
		    throw new RuntimeException("Failed to open db at " + dbPath + "/"+dbName, dbe);
		}
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setReadOnly(!create);
		    dbConfig.setAllowCreate(create);
		    metadb = env.openDatabase(null, metadbName, dbConfig); 
		} catch (DatabaseException dbe) {
		    throw new RuntimeException("Failed to open db at " + dbPath + "/"+metadbName, dbe);
		}
		try {
		    SecondaryConfig idxConfig = new SecondaryConfig();
		    idxConfig.setAllowPopulate(true);
		    idxConfig.setReadOnly(!create);
		    idxConfig.setAllowCreate(create);
		    idxConfig.setSortedDuplicates(true);
		    if (secondaryKey != null && !secondaryKey.isEmpty())
		    {
		    	keyCreator = new NWSRecord.SecondKeyCreator(secondaryKey);
		    	idxConfig.setKeyCreator(keyCreator);
		    }
		    secondaryIndexDb = env.openSecondaryDatabase(null, indexdbName, db, idxConfig);
		} catch (DatabaseException dbe) {
		    throw new RuntimeException("Failed to open db at " + dbPath + "/"+indexdbName, dbe);
		}
		if (!create)
		{
			// Load metadata
			NWSRecordMetadata.Binding dataBinding = new NWSRecordMetadata.Binding();
			Cursor c = null;
			try {
				c = metadb.openCursor(null, null);
				DatabaseEntry foundKey = new DatabaseEntry();
			    DatabaseEntry foundData = new DatabaseEntry();
			    while (c.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			    {
					NWSRecordMetadata r = (NWSRecordMetadata) dataBinding.entryToObject(foundData);
					//System.out.println(r.toString());
			    }
			} catch (DatabaseException e) {
				throw new RuntimeException("Failed to access db", e);
			} finally {
				if (c != null)
				{
					try {
						c.close();
					} catch (DatabaseException e) {
					}
				}
			}
			
			
		}
	}
	
	public void close() {
		try {
			if (secondaryIndexDb != null) {
				secondaryIndexDb.close();
			}
			if (metadb != null) {
				metadb.close();
			}
			if (db != null) {
				db.close();
			}
		    if (env != null) {
		    	env.close();
		    } 
		} catch (DatabaseException dbe) {
		    throw new RuntimeException("Failed to close db env at " + dbPath, dbe);
		} 
	}

	@SuppressWarnings("unchecked")
	private void setCoords(NWSRecord record, List<Object> coordlist)
	{
		for (Object coords: coordlist)
		{
			List<Object> clist = (List<Object>) coords;
			Object v = clist.get(0);
			if (v instanceof List)
			{
				setCoords(record, (List<Object>) clist);
			}
			else
			{
				double[] c = new double[clist.size()];
				for (int i = 0; i < clist.size(); i++) c[i] = (Double) clist.get(i);
				record.addCoordinates(c);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void load(String geojsonFilePath) {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(geojsonFilePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("file not found " + geojsonFilePath, e);
		}
		System.out.println("Reading "+geojsonFilePath);
		Reader reader = new InputStreamReader(inputStream);
		List<Object> objs = JsonUtil.parseJson(reader,  JsonUtil.GEOJSON_FEATURES_KEY);
		System.out.println("Total " + objs.size()  + " read");
		
		NWSRecordMetadata meta = new NWSRecordMetadata(metadbName);
		int cnt = 0;
		NWSRecord.Binding dataBinding = new NWSRecord.Binding(meta);
		int dim = 0;
		Properties props = null;
		for (Object o : objs)
		{
			Map<String,Object> obj = (Map<String, Object>) o;
			JsonUtil.processFlattenIgnore(obj, JsonUtil.GEOJSON_FLATTEN_KEYS, JsonUtil.GEOJSON_IGNORE_KEYS);
			if (cnt == 0)
			{
				for (String k : obj.keySet())
				{
					if (k.equals(GEOMETRY_PROPNAME)) continue;
					meta.addPropertyName(k);
				}
				meta.setPrimaryKey(primaryKey);
				meta.setSecondaryKey(secondaryKey);
				if (keyCreator != null)
				{
					keyCreator.setMetadata(meta);
				}
				System.out.println(meta.toString());
				NWSRecordMetadata.Binding metadataBinding = new NWSRecordMetadata.Binding();
				DatabaseEntry metakey = null;
				try {
					metakey = new DatabaseEntry(metadbName.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
				DatabaseEntry metadata = new DatabaseEntry();
				metadataBinding.objectToEntry(meta, metadata);
				try {
					metadb.put(null, metakey, metadata);
				} catch (DatabaseException e) {
					throw new RuntimeException("Failed to put to " + metadbName);
				}
			}
			
			NWSRecord record = new NWSRecord(meta);
			for (String k : obj.keySet())
			{
				Object v = obj.get(k);
				if (k.equals(GEOMETRY_PROPNAME))
				{
					Map<String,Object> geomobj = (Map<String, Object>) v;
					String geomType = (String) geomobj.get(GEOMETRY_TYPE_PROPNAME);
					record.setGeomType(geomType);
					List<Object> coordlist = (List<Object>) geomobj.get(GEOMETRY_COORDS_PROPNAME);
					if (dim == 0) props = new Properties();
					JsonUtil.flatCopyArrayList(coordlist, props);
					if (dim == 0)
					{
						dim = (Integer) props.get(JsonUtil.DIM_PROPERTY);
						props = null;
					}
					record.setDimension(dim);
					setCoords(record, coordlist);
				}
				else {
					record.putProperty(k, v== null ? "":v.toString());
				}
			}
			String key = (String) obj.get(primaryKey);
			DatabaseEntry datakey = null;
			try {
				datakey = new DatabaseEntry(key.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
			DatabaseEntry data = new DatabaseEntry();
			dataBinding.objectToEntry(record, data);
			try {
				db.put(null, datakey, data);
			} catch (DatabaseException e) {
				throw new RuntimeException("Failed to put to " + metadbName);
			}
			cnt++;
		}
		System.out.println(cnt + " objects written");
	}


	public List<NWSRecord> search(String key, String value) {
		DatabaseEntry searchKey = null;
		DatabaseEntry found =  new DatabaseEntry();
		NWSRecordMetadata meta = NWSRecordMetadata.getMetadata(this.metadbName);
		String pKey = meta.getPrimaryKey();
		String sKey = meta.getSecondaryKey();
		NWSRecord.Binding dataBinding = new NWSRecord.Binding(meta);
		try {
			searchKey = new DatabaseEntry(value.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		if (key.equalsIgnoreCase(pKey))
		{
			try {
				if (db.get(null, searchKey, found,  LockMode.DEFAULT) != OperationStatus.SUCCESS)
				{
					return null;
				}
				else
				{
					NWSRecord r = (NWSRecord) dataBinding.entryToObject(found);
					List<NWSRecord> result = new ArrayList<NWSRecord>();
					result.add(r);
					return result;
				}
			} catch (DatabaseException e) {
				throw new RuntimeException("Failed to access db", e);
			}
		}
		else if (key.equalsIgnoreCase(sKey))
		{
			SecondaryCursor c = null;
			try
			{
				c = secondaryIndexDb.openSecondaryCursor(null, null);
				DatabaseEntry foundKey = new DatabaseEntry();
				OperationStatus retVal =
	                    c.getSearchKey(searchKey, foundKey,
	                        found, LockMode.DEFAULT);
				if (retVal != OperationStatus.SUCCESS)
				{
					return null;
				} else {
					List<NWSRecord> result = new ArrayList<NWSRecord>();
				    while(retVal == OperationStatus.SUCCESS) {
				    	NWSRecord r = (NWSRecord) dataBinding.entryToObject(found);
				    	result.add(r);
				    	retVal = c.getNextDup(searchKey, foundKey, found, LockMode.DEFAULT);
		             }
				    return result;
				}
			} catch (DatabaseException e) {
				throw new RuntimeException("Failed to access db", e);
			} finally {
			} 
		} else {
			List<NWSRecord> result = new ArrayList<NWSRecord>();
			list(key, value, result);
			return result;
		}
	}
	
	private void listRecords(Cursor c, String k, String v, List<NWSRecord> result)
	{
		int cnt = 0;
		NWSRecordMetadata meta = NWSRecordMetadata.getMetadata(metadbName);
		NWSRecord.Binding dataBinding = new NWSRecord.Binding(meta);
		try {
			DatabaseEntry foundKey = new DatabaseEntry();
		    DatabaseEntry foundData = new DatabaseEntry();
		    while (c.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
		    {
		    	try {
					String keyString = new String(foundKey.getData(), "UTF-8");
					NWSRecord r = (NWSRecord) dataBinding.entryToObject(foundData);
					boolean show = true;
					if (k != null)
					{
						String kv = r.getProperty(k);
						show = v.equals(kv);
					}
					if (show)
					{
						if (result != null)
							result.add(r);
						else
							System.out.println(keyString + " " + r.toString());
						cnt++;
					}
				} catch (UnsupportedEncodingException e) {
				}
		    }
		} catch (DatabaseException e) {
			throw new RuntimeException("Failed to access db", e);
		} finally {
		}
		System.out.println("Total " + cnt + " records");
	}
	
	public void list()
	{
		list(null, null, null);
	}
	
	public void list(String k, String v, List<NWSRecord> result)
	{
		Cursor c = null;
		try {
			c = db.openCursor(null, null);
			listRecords(c, k, v, result);
		} catch (DatabaseException e) {
			throw new RuntimeException("Failed to access db", e);
		} finally {
			if (c != null)
			{
				try {
					c.close();
				} catch (DatabaseException e) {
				}
			}
		}
	}
	
	private void meta()
	{
		Collection<NWSRecordMetadata> metas = NWSRecordMetadata.getAllMetadata();
		for (NWSRecordMetadata meta : metas)
			System.out.println(meta.toString());
	}
	
	public static void list(String[] args)
	{
		String dbpath = args[1];
		NWSGeocodeDB db = new NWSGeocodeDB(dbpath);
		db.open(false);
		db.list();
		db.close();
	}	

	public static void meta(String[] args)
	{
		String dbpath = args[1];
		NWSGeocodeDB db = new NWSGeocodeDB(dbpath);
		db.open(false);
		db.meta();
		db.close();
	}	
	
	public static void make(String[] args)
	{
		String srcGeojson = args[1];
		String primaryKey = args[2];
		String secondaryKey = args[3];
		String dbpath = args[4];
		NWSGeocodeDB db = new NWSGeocodeDB(dbpath);
		db.setKey(primaryKey, secondaryKey);
		db.open(true);
		db.load(srcGeojson);
		db.close();
	}
	
	public static void search(String[] args)
	{
		String dbpath = args[1];
		String key = args[2];
		String value = args[3];
		NWSGeocodeDB db = new NWSGeocodeDB(dbpath);
		db.open(false);
		List<NWSRecord> result = db.search(key, value);
		if (result == null)
			System.out.println("Could not find " + key + "="+value);
		else
		{
			for (NWSRecord r : result)
				System.out.println(r.toString());
		}
		db.close();
	}

	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage)");
			System.out.println("make geojson primarykey secondarykey dbpath");
			System.out.println("search dbpath key value");
			System.out.println("list dbpath");
			System.out.println("meta dbpath");
			System.exit(0);
		}
		String cmd = args[0];
		if (cmd.equals("make")) make(args);
		else if (cmd.equals("list")) list(args);
		else if (cmd.equals("search")) search(args);
		else if (cmd.equals("meta")) meta(args);
	}
	
}