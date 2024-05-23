/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSGeocode.java /main/2 2015/11/16 15:32:38 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/geocode/NWSGeocode.java /main/2 2015/11/16 15:32:38 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.geocode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.oracle.cep.cartridge.geocodedb.NWSGeocodeDBRsc;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.common.util.SecureFile;

public class NWSGeocode
{
	NWSGeocodeDB stateDB;
	NWSGeocodeDB countyDB;
	NWSGeocodeDB zoneDB;
	private static final String GEOCODEDB_PATH = "geocodedb";
	private static final String STATEDB_NAME = "statedb";
	private static final String COUNTYDB_NAME = "countydb";
	private static final String ZONEDB_NAME = "zonedb";
	
	private static final String FIPS_KEY = "FIPS";
	private static final String ZONE_KEY = "ZONE";
	private static final String STATE_KEY = "STATE";

	public static final String FIPS_NAME= "FIPS6";
	public static final String UGC_NAME= "UGC";
	
	static NWSGeocode s_instance = null;

	public static synchronized  NWSGeocode getInstance() throws IOException
	{
		if (s_instance == null)
			s_instance = new NWSGeocode();
		return s_instance;
	}
	
	private NWSGeocode() throws IOException
	{
		File temp = SecureFile.getFile(System.getProperty("java.io.tmpdir"));
		String basepath = temp.getAbsolutePath();
		basepath = basepath.replace("\\","/");
		basepath += File.separator;
		basepath += GEOCODEDB_PATH;
		File f = new File(basepath);
		if (!f.exists())
		{
			f.mkdirs();
		}
		stateDB = getDB(basepath, STATEDB_NAME);
		countyDB = getDB(basepath, COUNTYDB_NAME);
		zoneDB = getDB(basepath, ZONEDB_NAME);
	}
	
	protected void finalize() {
		try
		{
			stateDB.close();
		} catch (Throwable e) {}
		try
		{
			countyDB.close();
		} catch (Throwable e) {}
		try
		{
			zoneDB.close();
		} catch (Throwable e) {}
	}
	
	private NWSGeocodeDB getDB(String dbpath, String dbname)
	{
		dbpath += File.separator;
		dbpath += dbname;
		File f = new File(dbpath);
		if (!f.exists())
		{
			File folder = new File(dbpath);
	    	if(!folder.exists()){
	    		folder.mkdirs();
	    	}
			String zipdb = "/"+dbname+".zip";
			try {
				NWSGeocodeDBRsc.unzipDB(zipdb, dbpath);
			} catch (Exception e) {
			    throw new RuntimeException("Failed to unzip db file " + zipdb, e);
			}
		}
		
		NWSGeocodeDB db = new NWSGeocodeDB(dbpath);
		db.open(false);
		return db;
	}
	
	public static class DecodeResult
	{
		public String fips;
		public Geometry geom;
	}
	public DecodeResult decode(String name, String code)
	{
		name = name.toUpperCase();
		if (name.equals(FIPS_NAME)) return decodeFIPS6(code);
		else if (name.equals(UGC_NAME)) return decodeUGC(code);
		throw new RuntimeException("Unknown geocode valuename:"+ name);
	}
	
	public DecodeResult decodeFIPS6(String code)
	{
		List<NWSRecord> cresult = null;
		DecodeResult r = new DecodeResult();
		String c = code.substring(1, 6);
		r.fips = c;
		cresult = countyDB.search(FIPS_KEY, c);
		if (cresult == null)
		{
			String state = code.substring(1, 3);
			r.fips = state;
			cresult = stateDB.search(FIPS_KEY, state);
		}
		r.geom = getGeometry(cresult);
		return r;
	}
	
	public DecodeResult decodeUGC(String code)
	{
		String s = code.substring(0, 2);
		String cz = code.substring(2, 3);
		String v = code.substring(3, 6);
		DecodeResult r = new DecodeResult();
		List<NWSRecord> cresult = null;
		if (cz.equals("C"))
		{
			List<NWSRecord> sresult = stateDB.search(STATE_KEY, s);
			if (sresult == null) throw new RuntimeException("Unknown state name :"+s);
			String stateCode = sresult.get(0).getProperty(FIPS_KEY);
			v = stateCode + v;
			r.fips = v;
			cresult = countyDB.search(FIPS_KEY, v);
		}
		else 
		{
			cresult = zoneDB.search(ZONE_KEY, v);
		}
		r.geom = getGeometry(cresult);
		return r;
	}


	private Geometry getGeometry(List<NWSRecord> cresult)
	{
		if (cresult == null || cresult.size()==0) return null;
		//take only the first one 
		NWSRecord r = cresult.get(0);
		return r.getGeometry();
	}

	/* Test code - should be in a unit test
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage)");
			System.out.println("fips6 code");
			System.out.println("ugc code");
			System.exit(0);
		}

		NWSGeocode geocode = NWSGeocode.getInstance();
		String cmd = args[0];
		String code = args[1];

		DecodeResult r = geocode.decode(cmd, code);
		if (r != null)
		{
			System.out.println(r.geom.toJsonString());
		}
	}
	*/
}
