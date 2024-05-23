/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/BDBStat.java /main/1 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - Creation
 */

/**
 *  @version $Header: BDBStat.java 03-apr-2011.12:41:34 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.BerkeleyDB;

import com.sleepycat.je.Environment;

import oracle.cep.storage.StorageStat;

public class BDBStat extends StorageStat
{
	Environment m_env;
	
	public BDBStat(Environment env)
	{
		m_env = env;
	}
	
	@Override
	public long getCacheMisses() {
		try {
			return m_env.getStats(null).getNCacheMiss();
		} catch (Exception e) {
			return 0;
		}
	}

}
