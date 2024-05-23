/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jdbc/CEPConnection.java /main/12 2014/01/28 20:39:54 ybedekar Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.CEPManager;
import oracle.cep.service.CEPServerRegistry;

public class CEPConnection extends CEPBaseConnection
{
  CEPConnection(String url, String user) throws SQLException
  {
    super(url, user);
  }

  public CEPServerRegistry getCEPServerRegistry()
  {
    CEPServerRegistry cepServerReg = null;

    //Single-task initialization. Get an instance of CEPServerRegistry
    if (isLocal)
    {
      try
      {
        CEPManager cepMgr = CEPManager.getInstance();
        cepServerReg = cepMgr.getServerRegistry();
      } catch (Exception e)
      {
        e.printStackTrace();
      }

    }

    //If remote, lookup RMI registry and get reference to remote CEPServerXFace
    else 
    {
      try
      {
        URI cepServURI = new URI(url);
        Registry cepRMIReg = LocateRegistry.getRegistry(host, port);
        cepServerReg = (CEPServerRegistry) (cepRMIReg.lookup(Constants.DEFAULT_CEP_SERVICE_NAME));
      }
      catch (URISyntaxException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }
      catch (RemoteException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }
      catch (NotBoundException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }    
    }
    
    return cepServerReg;
  }

  public String toQCXML(String qName, boolean isView) throws SQLException
  {
    try
    {
      return cepServer.getQCXML(qName, this.getSchemaName(), isView);
    }
    
    catch(RemoteException e)
    {
      throw new SQLException(e.getMessage(), e);
    }
  }
  
  
  public void clearWarnings() throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:clearWarnings() unsupported");
  }

   /**
   * Cleans up the state associated with this connection on the server. 
   */
  public void close() throws SQLException
  {
    try {
      cepServer.closeConnection(resultSetList);
      cepServerRegistry.closeConnection(serviceName);
      closed = true;
    }catch(RemoteException re)
    {
      closed = false;
      throw new SQLException("Error closing connection");
    }
  }
  
  public void commit() throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:commit() unsupported");
  }

  public Statement createStatement() throws SQLException
  {
    return new CEPStatement(this, this.getCEPServer());
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:createStatement(int, int) unsupported");
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:createStatement(int, int, int) unsupported");
  }

  public boolean getAutoCommit() throws SQLException
  {
    return false;  
  }

  public String getCatalog() throws SQLException
  {
    return null;
  }

  public int getHoldability() throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:getHoldability() unsupported");
  }

  public DatabaseMetaData getMetaData() throws SQLException
  { 
	if(dbMetaData==null) {
		dbMetaData = new CEPDatabaseMetaData(this,this.getCEPServer(), this.url);
	}
    return dbMetaData;
  }

  public int getTransactionIsolation() throws SQLException
  {
    	return Connection.TRANSACTION_NONE;
  }

  public Map<String,Class<?>> getTypeMap() throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:getTypeMap() unsupported");
  }

  public SQLWarning getWarnings() throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:getWarnings() unsupported");
  }

  public boolean isClosed() throws SQLException
  {
    return closed;
  }

  public boolean isReadOnly() throws SQLException
  {
	return false;
  }

  public String nativeSQL(String sql) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:nativeSQL(String) unsupported");
  }

  public CallableStatement prepareCall(String sql) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareCall(String) unsupported");
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareCall(String, int, int) unsupported");
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareCall(String, int, int, int) unsupported");
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
   		return new CEPPreparedStatement(sql, this, this.getCEPServer());
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareStatement(String, int) unsupported");
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareStatement(String, int[]) unsupported");
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareStatement(String, int, int) unsupported");
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareStatement(String, int, int, int) unsupported");
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:prepareStatement(String, String[]) unsupported");
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:releaseSavepoint(Savepoint) unsupported");
  }

  public void rollback() throws SQLException
  {
  		return;
  }

  public void rollback(Savepoint savepoint) throws SQLException
  {
    	return;
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException
  {
	throw new UnsupportedOperationException(
    "CEPConnection:setAutoCommit(boolean) unsupported");
  }

  public void setCatalog(String catalog) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:setCatalog(String) unsupported");
  }

  public void setHoldability(int holdability) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:setHoldability(int) unsupported");
  }

  public void setReadOnly(boolean readOnly) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:setReadOnly(boolean) unsupported");
  }

  public Savepoint setSavepoint() throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:setSavepoint() unsupported");
  }

  public Savepoint setSavepoint(String name) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:setSavepoint(String) unsupported");
  }

  public void setTransactionIsolation(int level) throws SQLException
  {
    throw new UnsupportedOperationException(
      "CEPConnection:setTransactionIsolation(int) unsupported");
  }


  public Array createArrayOf(String typeName, Object[] elements) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.createArrayOf(String,Object[]) unsupported");
  }

  public Blob createBlob() throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.createBlob() unsupported");
  }

  public Clob createClob() throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.createClob() unsupported");
  }

  public NClob createNClob() throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.createNClob() unsupported");
  }

  public SQLXML createSQLXML() throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.createSQLXML() unsupported");
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.createStruct(String,Object[]) unsupported");
  }
 
  public Properties getClientInfo() throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.getClientInfo() unsupported");
  }

  public String getClientInfo(String name) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.getClientInfo(String) unsupported");
  }

  public boolean isValid(int timeout) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.isValid(int) unsupported");
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException 
  {
	throw new UnsupportedOperationException("CEPConnection.setClientInfo() unsupported");	
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException 
  {
	throw new UnsupportedOperationException("CEPConnection.setClientInfo(String,String) unsupported");	
  }

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.setTypeMap(Map<String,Class<?>>) unsupported");	
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.isWrapperFor(Class<?>) unsupported");
  }

  public <T> T unwrap(Class<T> iface) throws SQLException 
  {
	throw new UnsupportedOperationException("CEPConnection.unwrap(Class<T>) unsupported");
  }
}
