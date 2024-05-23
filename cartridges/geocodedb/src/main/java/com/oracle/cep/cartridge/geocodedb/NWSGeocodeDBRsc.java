/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/geocodedb/src/main/java/com/oracle/cep/cartridge/geocodedb/NWSGeocodeDBRsc.java /main/1 2015/10/01 22:29:47 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/16/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/geocodedb/src/main/java/com/oracle/cep/cartridge/geocodedb/NWSGeocodeDBRsc.java /main/1 2015/10/01 22:29:47 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */


package com.oracle.cep.cartridge.geocodedb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NWSGeocodeDBRsc
{
	public static void unzipDB(String zipdb, String dbPath) throws Exception
	{
		InputStream is = NWSGeocodeDBRsc.class.getResourceAsStream(zipdb);
		if (is == null)
		{
			ClassLoader loader= Thread.currentThread().getContextClassLoader();
			is = loader.getResourceAsStream(zipdb);
		}
		if (is == null)
		{
			throw new RuntimeException("Cannot find "+zipdb + " resource");
		}
    	ZipInputStream zis =  new ZipInputStream(is);
    	ZipEntry ze = zis.getNextEntry();
    	byte[] buffer = new byte[4096];
    	 
    	while(ze!=null){
    	   String fileName = ze.getName();
           File newFile = new File(dbPath + File.separator + fileName);
           new File(newFile.getParent()).mkdirs();
           FileOutputStream fos = new FileOutputStream(newFile);             
 
           int len;
           while ((len = zis.read(buffer)) > 0) {
        	   fos.write(buffer, 0, len);
           }
 
           fos.close();   
           ze = zis.getNextEntry();
    	}
        zis.closeEntry();
    	zis.close();
    }
	
	
}