/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/ExtSource.java /main/19 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/14/11 - XbranchMerge udeshmuk_bug-11728864_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    05/25/11 - release connection when exception occurs
    sborah      10/12/09 - support for bigdecimal
    parujain    03/06/09 - schema.tbl support
    hopark      02/17/09 - support boolean as external datatype
    hopark      02/02/09 - change IDataSourceFinder name
    anasrini    02/13/09 - override supportsPushEmulation
    sbishnoi    01/08/09 - enable usage of IExternalDataSource in place of
                           DataSource
    sbishnoi    01/02/09 - overriding TableSource.end() to release datasource
                           connection
    hopark      12/04/08 - add toString
    sbishnoi    12/03/08 - support for generic data sources
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      06/26/08 - use datasource
    sbishnoi    03/24/08 - using DBHelper.validateSchema
    udeshmuk    03/13/08 - parameterize schema_mismatch error.
    sbishnoi    02/11/08 - error parametrization
    udeshmuk    01/30/08 - support for double data type.
    udeshmuk    01/17/08 - change in the return type of getoldestts
    parujain    11/09/07 - External Source
    parujain    11/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/ExtSource.java /main/19 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.input;

import java.sql.Connection;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.ExecException;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalDataSource;
import oracle.cep.interfaces.DBHelper;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IDataSourceFinder;

/**
 * ExtSource reads tuples from an external source like Db and returns them.
 * 
 * @author parujain
 */
public class ExtSource extends TableSourceBase
{
  private String dataSourceName;
  
  private Connection connection;
  
  /** External Connection Object*/
  private IExternalConnection extConnection;
  
  private int id;
  
  public ExtSource(ExecContext ec, String dsName, int tblId)
  {
    super(ec);
    this.dataSourceName = dsName;
    this.id = tblId;
  }
  
  public void start() throws CEPException
  {
	super.start();
	createConnection(true);
  }
  
  public IExternalConnection createConnection(boolean validateSchema) throws CEPException
  {
	  ConfigManager cm = null;
	  IDataSourceFinder service = null;
	  IExternalDataSource extDataSource = null;
	  extConnection = null;
	  
	  try 
	    {
	      
	      // Get Environment specific DataSource finder 
	      
	      cm = execContext.getServiceManager().getConfigMgr();
	      if(cm != null)
	    	LogUtil.info(LoggerType.TRACE, "configuration manager found");
	      else
	    	LogUtil.info(LoggerType.TRACE, "configuration manager NOT FOUND!");
	      
	      service = cm.getDataSourceFinder();
	      if(service == null)
	      {
	        LogUtil.warning(LoggerType.TRACE, "datasourcefinder service " +
	                            "feature not provided in the environement");
	        throw new CEPException(
	              InterfaceError.FEATURE_NOT_SUPPORTED_IN_THIS_ENVIRONMENT);
	      }

	      LogUtil.info(LoggerType.TRACE, "data source finder service detected");
	      
	      extDataSource = service.findDataSource(dataSourceName);
	      
	      if (extDataSource == null)
	      {
	        LogUtil.warning(LoggerType.TRACE, 
	                        "Failed to get DataSource "+ dataSourceName);
	        throw new CEPException(InterfaceError.INVALID_SOURCE, dataSourceName);
	      }
	      LogUtil.info(LoggerType.TRACE, "data source found "+dataSourceName );
	      
	      // Obtain IExternalConnection from IExternalDataSource
	      extConnection = extDataSource.getConnection();
	              
	      if(extConnection != null)
	        LogUtil.info(LoggerType.TRACE, "Connection obtained!");
	      else
	    	LogUtil.info(LoggerType.TRACE, "Connection cannot be obtained from the datasource!");
	      
	      // Validate Schema
	      // Note: Presently we will validate schema only for JDBC Data Source
	      // which can be extended to Generic Data sources
	      //TODO : Need to handle this validation in generic way , for time being commenting out 
	      if(validateSchema){      
	    	  extConnection.validateSchema(super.numAttrs,super.attrNames, super.attrMetadata);    	   
	          LogUtil.info(LoggerType.TRACE, "External Source: Schema Validated");
  	        }	      
	    } 
	    catch(Exception e) {
	
	      if(validateSchema)
	      {
		    // If function is called from ExtSource.start then throw error.
	    	LogUtil.warning(LoggerType.TRACE, "Create connection on datasource "+
	    	  dataSourceName+ "failed because of : " + e.toString());
	    	LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
	    	
	    	//If already an instance of CEPException don't wrap it again.
	    	if(e instanceof CEPException)
	    	  throw (CEPException) e;
	    	else
	    	  throw new CEPException(InterfaceError.INVALID_SOURCE, e, 
	    		                     dataSourceName);
	      }
	      else
	      {
	        //create connection is being called from ConnectionRecoveryContext
	    	//Throw error if -
	    	// 1. Configuration manager is null (NPE would occur)
	    	// 2. DataSourceFinderService is not accessible
	    	// 3. DataSource is not found.
	    	// Possibility of these three happening is very less but still 
	    	// we should throw error in such cases because a new connection
	    	// can never be created in these scenarios.
	    	// Only when the DataSource.getConnection() results in error
	    	// (possible if entire DB is down) then we do not want to throw 
	    	// error as we will keep looping in ConnectionRecoveryContext.
	    	// renewConnectio() waiting for the DB to come up.
	    	LogUtil.warning(LoggerType.TRACE, "Create connection on datasource "+
	    	  dataSourceName+ "failed because of : " + e.toString());
	    	LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
	    	System.out.println("STackTrace******");
		    e.printStackTrace();
		    System.out.println("**************************************");
		    
		    if((extDataSource == null) || (service == null) || (cm == null))
		    {
		      //if already CEPexception don't wrap it again
		      if(e instanceof CEPException)
		    	throw (CEPException) e;
		      else
		    	throw new CEPException(InterfaceError.INVALID_SOURCE, e, dataSourceName);
		    }
	      }
	      
	      if(extConnection != null)
	      {
	        end();
		    LogUtil.warning(LoggerType.TRACE, "External connection closed as exception was"+
		                               " encountered while starting the source");
	      }
	      extConnection = null;
	   }
	   return extConnection;
  }
  
  public Connection getConnection()
  {
    return this.connection;
  }
  
  /**
   * Get External Connection Object
   * @return IExternalConnection
   */
  public IExternalConnection getExtConnection()
  {
    return extConnection;
  }
  
  public String getDataSourceName()
  {
    return dataSourceName;
  }
  
  public TupleValue getNext() throws CEPException {
    assert false;
    return null;
  }

  public long getOldestTs() throws CEPException {
    assert false;
    return Constants.NULL_TIMESTAMP;
  }

  public boolean hasNext() throws ExecException {
    assert false;
    return false;
  }

  /**
   * This method is used in REGRESSION testing to support push mode
   * emulation for pull sources
   */
  public boolean supportsPushEmulation()
  {
    // The external source does not support push emulation
    return false;
  }

  public String toString()
  {
    return "ExtSource(" + dataSourceName + ")";
  }
  
  public void closeConnection()
  {
	try {
		extConnection.close();
		System.out.println("closing connection "+extConnection);
		extConnection = null;
	} catch (Exception e) {
		
		e.printStackTrace();
	}
  }
  
  @Override
  public void end() throws CEPException
  {
    try
    {      
      extConnection.close();
      extConnection = null;
    }
    catch(Exception e)
    {
      throw new CEPException(InterfaceError.EXT_CONNECTION_NOT_CLOSED, dataSourceName);
    }
  }
}
