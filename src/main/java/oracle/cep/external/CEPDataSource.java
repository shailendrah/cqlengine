/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/external/CEPDataSource.java /main/4 2014/01/28 20:39:54 ybedekar Exp $ */

/* Copyright (c) 2008, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rxvenkat    08/06/08 - 
    jmaron      07/28/08 -
    hopark      05/13/08 -
    parujain    04/09/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/external/CEPDataSource.java /main/4 2014/01/28 20:39:54 ybedekar Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.external;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class CEPDataSource implements DataSource {
	
	private String url;
	private String driverName;
	private String userName;
	private String password;
	private int logintimeout;
	private transient PrintWriter logWriter;
	private Properties driverProperties;
	
	public CEPDataSource()
	{
      
	}
	
	public CEPDataSource(String url)
	{
      this.url = url;
	} 
	
	public void setURL(String url)
    {
      this.url = url;
    }

    public void setLogin(String username, String password)
    {
      this.userName = username;
      this.password = password;
    }

    public void setDriverName(String driver)
    {
      this.driverName = driver;
    }
	
	public Connection getConnection() throws SQLException {
		
     if(driverProperties == null)
       driverProperties = new Properties();
     Connection connection = null;

     if (this.userName != null)
       driverProperties.put("user", this.userName);

     if (this.password != null)
       driverProperties.put("password", this.password);
         
     try {
         Class.forName(driverName);
         connection = DriverManager.getConnection(url, driverProperties);
         }catch(Exception e)
         {
           LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
         }
   
     return connection;
	}

	public Connection getConnection(String username, String password) throws SQLException {
      if(driverProperties == null)
        driverProperties = new Properties();
      Connection connection = null;
      
      if ( username != null )
      {
        driverProperties.put("user", username);
      }
      if ( password != null )
      {
        driverProperties.put("password", password);
      }
      try {
      Class.forName(driverName);
      connection = DriverManager.getConnection(url, driverProperties);
      }catch(Exception e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }
	  return connection;
	}

	public PrintWriter getLogWriter() throws SQLException {
	  return this.logWriter;
	}

	public int getLoginTimeout() throws SQLException {
	  return this.logintimeout;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
	  this.logWriter = out;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
	  this.logintimeout = seconds;
	}

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException
  {
    throw new UnsupportedOperationException("CEPDataSource.getParentLogger not supported yet.");
  }
}
