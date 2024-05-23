package oracle.cep.test.ha.server;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import oracle.cep.extensibility.datasource.IExternalDataSource;
import oracle.cep.extensibility.datasource.JDBCExternalDataSource;
import oracle.cep.service.IDataSourceFinder;
import oracle.jdbc.pool.OracleDataSource;

public class CQLDataSourceFinder implements IDataSourceFinder
{
	private Map<String, IExternalDataSource> dataSourceMap;
	
	@Override
	public void addDataSource(String arg0, IExternalDataSource arg1) {
		dataSourceMap.put(arg0, arg1);
	}

	@Override
	public IExternalDataSource findDataSource(String arg0) {
		return dataSourceMap.get(arg0);
	}

	@Override
	public void init() {
        dataSourceMap = new HashMap<String,IExternalDataSource>();      
        //addDataSource("soainfra", "jdbc:oracle:thin:soainfra/soainfra@//adc01jky.us.oracle.com:1521/xe", "soainfra", "soainfra");
	}

	@Override
	public void removeDataSource(String arg0) {
		dataSourceMap.remove(arg0);
	}

	/**
	 * 
	 * @param dataSourceName
	 * @param dbURL jdbc:oracle:thin:@//myhost:1521/orcl
	 *              jdbc:oracle:thin:scott/tiger@//myhost:1521/orcl
	 */
	public void addDataSource(String dataSourceName, String dbURL)
	{
		OracleDataSource ods = null;
		try 
		{
			ods = new OracleDataSource();			
			ods.setURL(dbURL);
			ods.setDataSourceName(dataSourceName);
			ods.setDatabaseName(dataSourceName);				
			System.out.println("Adding datasource name:" + dataSourceName + " ds:" + ods.toString());
			IExternalDataSource ds = new JDBCExternalDataSource(ods);
			addDataSource(dataSourceName, ds);
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private DataSource setDataSource() 
	{
		OracleDataSource ods = null;
		try 
		{
			ods = new OracleDataSource();
			ods.setDriverType("thin");
			ods.setNetworkProtocol("tcp");
			
			String dsName = "soainfra";
			ods.setServerName("adc01jky.us.oracle.com");
			ods.setPortNumber(1521);
			ods.setDatabaseName("soainfra");
			ods.setUser("soainfra");
			ods.setPassword("soainfra");
			ods.setServiceName("xe");
			System.out.println("Adding datasource name:" + dsName + " ds:" + ods.toString());
			IExternalDataSource ds = new JDBCExternalDataSource(ods);
			addDataSource(dsName, ds);
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ods;
		
		/*Iterator<Object> i = props.keySet().iterator();
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
		}*/
		// allows column values with the TIMESTAMP WITH TIME ZONE data type to
		// be retrieved as a JDBC TIMESTAMP data type.
		// However, FetchTSWTSasTimestamp is not recognized by Oracle DataSource
		// So I had to use setString in JDBCExternalPreapredStatement.
		// Properties connprop = new Properties();
		// connprop.setProperty("FetchTSWTSasTimestamp", "true");
		// ods.setConnectionProperties(connprop);
	}
}
