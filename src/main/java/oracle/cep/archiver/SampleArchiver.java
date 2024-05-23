/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/archiver/SampleArchiver.java /main/7 2015/07/07 18:30:34 sbishnoi Exp $ */

/* Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    06/30/15 - changing db host to adc01jky
    sbishnoi    05/29/14 - changing db to slc01eav
    udeshmuk    04/07/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/archiver/SampleArchiver.java /main/7 2015/07/07 18:30:34 sbishnoi Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.archiver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;

/*
 * Sample archiver class that uses oracle database
 * for persistence.
 */
public class SampleArchiver implements IArchiver 
{
  private String dbHost;
  private String dbPort;
  private String dbSID;
  private String username;
  private String password;
  private Connection conn = null;
  private String url = null;
 
  private static final String TEST_DB_HOST = "adc01jky.us.oracle.com";
  private static final String TEST_DB_PORT = "1521";
  private static final String TEST_DB_SID = "xe";
  private static final String TEST_DB_USER = "soainfra";
  private static final String TEST_DB_PASSWORD = "soainfra";
  private static final String DB_CONNECTION_PREFIX = "jdbc:oracle:thin:@";

  public SampleArchiver()
  {
    // Initialize the db host, port and SID from environment variables
    dbHost = System.getenv("test.db.host");
    dbPort = System.getenv("test.db.port");
    dbSID = System.getenv("test.db.sid");

    // Initialize the db user and password from environment variables
    String uname = System.getenv("test.db.user");
    String passwd = System.getenv("test.db.password");

    StringBuffer sb = new StringBuffer(DB_CONNECTION_PREFIX);
    sb.append(dbHost != null ? dbHost : TEST_DB_HOST);
    sb.append(":");
    sb.append(dbPort != null ? dbPort : TEST_DB_PORT);
    sb.append(":");
    sb.append(dbSID != null ? dbSID : TEST_DB_SID);
    url = sb.toString();
    username = uname != null ? uname : TEST_DB_USER;
    password = passwd != null ? passwd : TEST_DB_PASSWORD;
  }

  public Connection getConnection() {
    if (conn != null) return conn;
    try
    {
      DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
      System.out.println("url: "+url+" username: "+username+" password: "+password);
      conn = DriverManager.getConnection(url, username, password);
      //System.out.println("Connection Established...");
    }
    catch(SQLException se)
    {
      System.out.println("Error while establishing DB connection...");
      System.out.println(se.getMessage());
    }
    return conn;
  }

  public void setURL(String url) {
      this.url = url;
  }
  
  public void setUsername(String username) {
      this.username = username;
  }

  public void setPassword(String password) {
      this.password = password;
  }

  public IArchiverQueryResult execute(QueryRequest[] queries)
  {
    getConnection();

	Result results = new Result(queries.length);  
    for(int i=0; i < queries.length; i++)
    {
      QueryRequest query = queries[i];
      try
      {
        if(conn != null)
        {
          //create and populate prepared stmt
          PreparedStatement pstmt = conn.prepareStatement(query.getQuery());
          if(query.getParams() != null)
          {
            //FIXME: call appropriate setter based on the type of parameter
            Object[] params = query.getParams();
            for(int j=1; j <= params.length; j++)
            {
              pstmt.setObject(j, params[j-1]);
            }
          }
          if(pstmt != null)
          {
            //execute
            results.setResult(i, pstmt.executeQuery());
          }
          else
            System.out.println("Problem in query : "+query.getQuery());
        }
        else
        {
          System.out.println("Connection is null");
          return null;
        }
      }
      catch(SQLException se)
      {
        System.out.println(se.getMessage());
        return null;
      }
    }
    return results;
  }

  public void closeResult(IArchiverQueryResult result)
  {
	  //TODO cleanup..
  }

  private static class Result implements IArchiverQueryResult
  {
	ResultSet rs[];
	
	public Result(int count)
	{
		rs = new ResultSet[count];
	}
	
    @Override
	public int getResultCount() {
		return rs.length;
	}
	@Override
	public ResultSet getResult(int idx) 
	{
		return rs[idx];
	}

	public void setResult(int idx, ResultSet r)
	{
		rs[idx] = r;
	}
  }

}
