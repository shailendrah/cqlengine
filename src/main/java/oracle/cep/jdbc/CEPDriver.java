/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jdbc/CEPDriver.java /main/6 2014/01/28 20:39:54 ybedekar Exp $ */

/* Copyright (c) 2007, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/09/08 - support of username and password in connection
    skmishra    10/29/07 - 
    mthatte     09/14/07 - 
    parujain    05/09/07 - 
    najain      04/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jdbc/CEPDriver.java /main/6 2014/01/28 20:39:54 ybedekar Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class CEPDriver implements Driver
{
  private final static String URL_PREFIX = "jdbc";

  // This static block inits the driver when the class is loaded by the JVM.
  static
  {
    try
    {
      java.sql.DriverManager.registerDriver(new CEPDriver());
    }
    catch (SQLException e)
    {
      throw new RuntimeException(
          "FATAL ERROR: Could not initialise CEPDriver ! Message was: "
           + e.getMessage());
    }
  }
  
  public boolean acceptsURL(String url) throws SQLException
  {
    return url.startsWith(URL_PREFIX);
  }

  public Connection connect(String url, Properties info) throws SQLException
  {
    //Note: Right Now we are mentioning username and password
    // in DriverManager.getConnection(..). Also It is necessary to call
    // DriverManager.getConnection(url, username, password) to get a connection
    if(info.size() != 0)
    {
      String user     = info.getProperty("user");
      String password = info.getProperty("password");
      //TODO: Authentication Procedure
      return new CEPConnection(url, user); 
    }
    return null;
  }

  public int getMajorVersion()
  {
    return 1;
  }

  public int getMinorVersion()
  {
    return 0;
  }

  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
  {
    return new DriverPropertyInfo[0];
  }

  public boolean jdbcCompliant()
  {
    return true;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException
  {
    throw new UnsupportedOperationException("CEPDriver.getParentLogger not supported yet.");
  }
}
