/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/geocodedb/src/main/java/com/oracle/cep/cartridge/geocodedb/PlaceNamesRsc.java /main/1 2015/10/01 22:29:47 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/geocodedb/src/main/java/com/oracle/cep/cartridge/geocodedb/PlaceNamesRsc.java /main/1 2015/10/01 22:29:47 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.geocodedb;

import java.io.InputStream;

public class PlaceNamesRsc
{
	public static InputStream getResource(String rsc)
	{
		InputStream is = PlaceNamesRsc.class.getResourceAsStream(rsc);
		if (is == null)
		{
			ClassLoader loader= Thread.currentThread().getContextClassLoader();
			is = loader.getResourceAsStream(rsc);
		}
		if (is == null)
		{
			throw new RuntimeException("Cannot find "+rsc + " resource");
		}
		return is;
	}
}