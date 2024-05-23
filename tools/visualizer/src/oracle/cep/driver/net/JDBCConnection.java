/* $Header: pcbpel/cep/server/src/oracle/cep/driver/net/JDBCConnection.java /main/3 2008/09/29 02:59:36 sborah Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      09/26/08 - 
    sbishnoi    09/24/08 - incorporating jdbc changes to connect to cepserver
    rkomurav    05/26/08 - pass url as argument
    rkomurav    04/17/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/driver/net/JDBCConnection.java /main/3 2008/09/29 02:59:36 sborah Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.driver.net;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.cep.driver.util.FatalException;
import oracle.cep.driver.util.InitManager;

public class JDBCConnection extends IServerConnection
{
  Connection con;
  
  Statement  stmt;
  
  /**
   * Constructor
   */
  public JDBCConnection(String host, int port, String url) throws FatalException
  {
    try
    {
      this.host = host;
      this.port = port;
      // Load the JDBC-ODBC bridge
      Class.forName("oracle.cep.jdbc.CEPDriver");
      
      // connect
      con = DriverManager.getConnection(url, "system", "oracle");
      stmt = con.createStatement();
    }
    catch (Exception err)
    {
      err.printStackTrace();
      throw new FatalException("JDBC connection failed");
    }
  }
  
  private void jdbcExecute(String cmd) throws FatalException
  {
    try
    {
      stmt.executeUpdate(cmd);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new FatalException ("JDBC Execute Failed");
    }
  }
  
  public RegInputRet registerInput (String regInputStr) throws FatalException
  {
    RegInputRet ret = new RegInputRet ();
    jdbcExecute(regInputStr);
    return ret;
  }
  
  public void startNamedQuery(String queryName) throws FatalException
  {
    jdbcExecute("alter query " + queryName + " start");
  }
  
  public void bindSrcDest(String name, String scheme, String path, int type)
  throws FatalException
  {
    String kind    = new String();
    String keyword = new String();;
    
    if(type == 1)
    {
      kind    = "stream";
      keyword = "source";
    }
    else if(type == 2)
    {
      kind    = "relation";
      keyword = "source";
    }
    else if(type == 3)
    {
      kind    = "query";
      keyword = "destination";
    }
    jdbcExecute("alter " + kind + " " + name + " add " + keyword + 
        " \"<EndPointReference> <Address>file://" + path + "</Address> </EndPointReference>\"");
  }
  
  /** 
   * Register a query for which we want an output.
   */ 
  public RegQueryRet registerOutQuery(String query) throws FatalException
  {
    RegQueryRet ret = new RegQueryRet();
    jdbcExecute(query);
    return ret;
  }
  
  /**
   * Register a query for which we do not need an output (pure view)
   */
  public RegQueryRet registerQuery(String query) 
    throws FatalException
  {
    RegQueryRet ret = new RegQueryRet();
    jdbcExecute(query);
    return ret;
  }
  
  public GenPlanRet genPlan () throws FatalException
  {
    GenPlanRet ret = new GenPlanRet ();
    ResultSet  rs;
    try
    {
      ret.errorCode = getErrorCode ();
      try
      {
        rs = stmt.executeQuery("explain plan");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        throw new FatalException ("JDBC Execute Failed");
      }
      assert rs != null;
      if(rs.next())
      {
        ret.planString = rs.getString("TABLE_NAME");
      }
      else
        assert false;
      
      return ret;
    }
    catch (Exception e)
    {
      throw new FatalException (e.getMessage());
    }
  }
  
  public ExecRet execute () throws FatalException
  {
    ExecRet ret = new ExecRet ();
    jdbcExecute("alter system run");
    ret.errorCode = getErrorCode ();
    return ret;
  }
}
