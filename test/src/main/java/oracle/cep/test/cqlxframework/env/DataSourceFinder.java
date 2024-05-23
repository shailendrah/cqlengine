/* $Header: cep/cqlengine/test/src/main/java/oracle.cep.test.cqlxframework/DataSourceFinder.java st_pcbpel_hopark_cqlmaven/2 2011/03/28 10:00:22 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      03/12/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/standaloneEnv/src/oracle.cep.test.cqlxframework/env/standalone/DataSourceFinder.java /main/3 2009/12/05 13:43:52 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cqlxframework.env;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;

import oracle.cep.service.IDataSourceFinder;
import oracle.cep.extensibility.datasource.IExternalDataSource;
import oracle.cep.extensibility.datasource.JDBCExternalDataSource;

public class DataSourceFinder implements IDataSourceFinder {
	private Map<String, IExternalDataSource> dataSourceMap;

	public DataSourceFinder() {
		dataSourceMap = new HashMap<String, IExternalDataSource>();
	}

	public void init() {
		dataSourceMap.clear();
	}

	public void addDataSource(String name, IExternalDataSource ds) {
		dataSourceMap.put(name, ds);
	}

	public void removeDataSource(String name) {
		dataSourceMap.remove(name);
	}

	public IExternalDataSource findDataSource(String name) {
		return dataSourceMap.get(name);
	}

	public void setDataSource0(Properties props) throws Exception {
		setDataSource(props);
	}

	public void setDataSource1(Properties props) throws Exception {
		setDataSource(props);
	}

	public void setDataSource2(Properties props) throws Exception {
		setDataSource(props);
	}

	public void setDataSource3(Properties props) throws Exception {
		setDataSource(props);
	}

	public void setDataSource4(Properties props) throws Exception {
		setDataSource(props);
	}

	public void setDataSource5(Properties props) throws Exception {
		setDataSource(props);
	}

	private void setDataSource(Properties props) throws Exception {
		OracleDataSource ods = new OracleDataSource();
		ods.setDriverType("thin");
		ods.setNetworkProtocol("tcp");
		ods.setPortNumber(1521);
		String dsName = null;
		Iterator<Object> i = props.keySet().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			String val = props.getProperty(name);
			if (name.equals("name")) {
				dsName = val;
			} else if (name.equals("server")) {
				ods.setServerName(val);
			} else if (name.equals("port")) {
				Integer portNo = Integer.parseInt(val);
				ods.setPortNumber(portNo);
			} else if (name.equals("dbname")) {
				ods.setDatabaseName(val);
			} else if (name.equals("user")) {
				ods.setUser(val);
			} else if (name.equals("passwd")) {
				ods.setPassword(val);
			}
		}
		// allows column values with the TIMESTAMP WITH TIME ZONE data type to
		// be retrieved as a JDBC TIMESTAMP data type.
		// However, FetchTSWTSasTimestamp is not recognized by Oracle DataSource
		// So I had to use setString in JDBCExternalPreapredStatement.
		// Properties connprop = new Properties();
		// connprop.setProperty("FetchTSWTSasTimestamp", "true");
		// ods.setConnectionProperties(connprop);
		IExternalDataSource ds = new JDBCExternalDataSource(ods);
		addDataSource(dsName, ds);
	}
}
