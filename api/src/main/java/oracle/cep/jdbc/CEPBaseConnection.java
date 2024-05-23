/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPBaseConnection.java /main/13 2014/01/28 20:39:51 ybedekar Exp $ */

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
    sbishnoi    03/01/12 - fix 13774367
    parujain    02/16/09 - getViewTypes
    hopark      11/17/08 - add setSchema
    hopark      11/03/08 - fix schema
    hopark      10/09/08 - remove statics
    hopark      10/13/08 - fix getCEPServer
    sbishnoi    09/23/08 - Removing println statements
    sbishnoi    09/22/08 - support for schema
    skmishra    08/04/08 - base class for CEPConnection and CEPClientConnection
    skmishra    08/04/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPBaseConnection.java /main/13 2014/01/28 20:39:51 ybedekar Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import oracle.cep.common.Constants;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.QueryInfo;
import oracle.cep.metadata.ViewInfo;
import oracle.cep.service.CEPServerRegistry;
import oracle.cep.service.CEPServerXface;
import oracle.cep.service.IQueryChgListener;

public abstract class CEPBaseConnection implements Connection
{
  protected CEPServerRegistry       cepServerRegistry;
  protected CEPServerXface          cepServer; //server which this is connected to
  protected String                  url;  //server url
  
  protected DatabaseMetaData        dbMetaData    = null; 
  protected boolean                 closed        = false;
  
  //list of resultsets that was created by this connection, for cleanup.
  protected List<Long>              resultSetList = new ArrayList<Long>();

   /** A flag to check whether this url is for single-task jdbc connection*/
  boolean                        isLocal = false;
  /** target host name for jdbc connection*/
  String                         host;
  /** target port num for jdbc connection*/
  int                            port = Constants.DEFAULT_JDBC_PORT;
  /** target service for jdbc connection*/
  String                         serviceName
                                    = Constants.DEFAULT_CEP_SERVICE_NAME;
  /** User name is Schema Name*/
  private String                 user;
 
  
  public abstract CEPServerRegistry getCEPServerRegistry();
  
  public CEPBaseConnection(String url, String user) throws SQLException
  {
    this.url = url;
    this.user = user;
    parseURL(url);
    cepServerRegistry = getCEPServerRegistry();
    try
    {
      cepServer = cepServerRegistry.openConnection(serviceName);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, "failed to create a service for " + serviceName + 
          " : " + e.toString() + "\n" + (ce == null ? "":ce.toString()));
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  public CEPServerXface getCEPServer() {return cepServer;}
  
  public void addResultSetID(long id)
  {
    resultSetList.add(id);
  }
  
  public String getURL() 
  {
    return url;
  }

  /**
   * Parses the URL and sets host name, port number and service name
   */
  public void parseURL(String url) throws SQLException
  {
    String[] parsedURL = url.split(":");
    if(parsedURL.length < 3)
    {
      LogUtil.info(LoggerType.TRACE, "wrong number of arguments ; " + parsedURL.length);
      throw new SQLException(url);
    }
    else if(!parsedURL[0].trim().equals("jdbc") ||
            !parsedURL[1].trim().equals("oracle"))
    {
      LogUtil.info(LoggerType.TRACE, "wrong prefix ; " + parsedURL[0] + ":" + parsedURL[1]);
      throw new SQLException(url);
    }
    else if(parsedURL[2].trim().equals("ceplocal"))
    {
      isLocal = true;
      String[] parts = url.split("@");
      if (parts.length > 1)
        setHostInfo(parts[1]);
    }
    else
    {
      setHostInfo((url.split("@"))[1]);
    }
  }

  /**
  * Fetch Host Details from hostInfo
  * e.g. hostName:port:service
  */
  public void setHostInfo(String hostInfo) throws SQLException
  {
    String[] hostDetails = hostInfo.split(":");
    int numDetails = hostDetails.length;
    if(numDetails < 1 || numDetails > 3)
    {
      LogUtil.info(LoggerType.TRACE, "wrong host information <" + hostInfo + ">" + numDetails);
      throw new SQLException(hostInfo);
    }
    switch(numDetails)
    {
    case 3:
      this.serviceName = hostDetails[2];
    case 2:
      this.port = Integer.valueOf(hostDetails[1]);
    case 1:
      this.host = hostDetails[0];
      break;
    default:
      assert false;
    }

  }

  /**
   * Returns the user of this connection
   * @return ther user name
   */
  public String getUser()
  {
    return this.user;
  }
  
  /**
   * Returns the service name
   * @return service name
   */
  public String getServiceName()
  {
    return this.serviceName;
  }  
  
  /**
   * Returns the schema name
   * Currently the user name is used as the schema.
   * @return
   */
  public String getSchemaName()
  {
    return user;
  }
  
  public void setSchema(String schemaName) throws SQLException
  {
    try
    {
      cepServer.setSchema(schemaName);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, "failed to set a schema , " + schemaName +", from a service " + serviceName + 
          " : " + e.toString() + "\n" + (ce == null ? "":ce.toString()));
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  public void dropSchema(String schemaName) throws SQLException
  {
    try
    {
      cepServer.dropSchema(schemaName);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, "failed to drop a schema , " + schemaName +", from a service " + serviceName + 
          " : " + e.toString() + "\n" + (ce == null ? "":ce.toString()));
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  public Map<String, ViewInfo> getViewInfo()
  throws SQLException
  {
    try{
      return cepServer.getViewInfo(getSchemaName());
    }catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, 
                     "failed to get informations about views for schema , " 
                     + user +", from a service " + serviceName + " : " + 
                     e.toString() + "\n" + (ce == null ? "":ce.toString()));
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
      
    }
  }
  
  /**
   * Get the information about all the registered query on current schema
   * @return a map of <queryName, QueryInfo>
   * @throws SQLException
   */
  public Map<String, QueryInfo> getQueryInfo()
      throws SQLException
      {
        try
        {
          return cepServer.getQueryInfo(getSchemaName());
        }
        catch(Exception e)
        {
          Throwable ce = e.getCause();
          LogUtil.severe(LoggerType.TRACE, 
                         "failed to get informations about queries for schema "
                         + user +", from a service " + serviceName + " : " + 
                         e.toString() + "\n" + (ce == null ? "":ce.toString()));
          SQLException se = new SQLException(serviceName);
          se.initCause(e);
          throw se;
        }
      }
  
  public Set<String> getSourceAttributeNamesForQueryOrView(String ruleId, boolean isView) throws SQLException
  {
    try
    {
      return cepServer.getSourceAttributeNamesForQueryOrView(ruleId, getSchemaName(), isView);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, "failed to retrieve source attributes for query/view '" + ruleId 
          + "' from service " + serviceName + " : " + e.toString() + "\n" + (ce == null ? "":ce.toString()));
      
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  public Set<String> getQuerySourceNames(String queryId, String schema) throws SQLException
  {
    try
    {
      return cepServer.getQuerySourceNames(queryId, schema);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, "failed to retrieve sources for query '" + queryId 
          + "' from service " + serviceName + " : " + e.toString() + "\n" + (ce == null ? "":ce.toString()));
      
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  public void addQueryChangeListener(IQueryChgListener notifier)
  throws SQLException
  {
    try
    {
      cepServer.addQueryChangeListener(getSchemaName(), notifier);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, 
                     "failed to add query notifier for schema "
                     + user +", from a service " + serviceName + " : " + 
                     e.toString() + "\n" + (ce == null ? "":ce.toString()));
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  public void removeQueryChangeListener(IQueryChgListener notifier)
  throws SQLException
  {
    try
    {
      cepServer.removeQueryChangeListener(getSchemaName(), notifier);
    }
    catch(Exception e)
    {
      Throwable ce = e.getCause();
      LogUtil.severe(LoggerType.TRACE, 
                     "failed to remove query notifier for schema "
                     + user +", from a service " + serviceName + " : " + 
                     e.toString() + "\n" + (ce == null ? "":ce.toString()));
      SQLException se = new SQLException(serviceName);
      se.initCause(e);
      throw se;
    }
  }
  
  @Override
  public String getSchema() throws SQLException
  {
    throw new UnsupportedOperationException("CEPBaseConnection.getSchema not supported yet.");
  }

  @Override
  public void abort(Executor executor) throws SQLException
  {
    throw new UnsupportedOperationException("CEPBaseConnection.abort not supported yet.");
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
  {
    throw new UnsupportedOperationException("CEPBaseConnection.setNetworkTimeout not supported yet.");
  }

  @Override
  public int getNetworkTimeout() throws SQLException
  {
    throw new UnsupportedOperationException("CEPBaseConnection.getNetworkTimeout not supported yet.");
  }
}

