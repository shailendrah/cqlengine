/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeFunctionMetadata.java /main/11 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2010, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/02/15 - handle connectivity down issues bug 21678893
    udeshmuk    10/28/10 - XbranchMerge udeshmuk_fix_timestamp_issue from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    10/25/10 - fix timestamp issue for native timestamp return type
    sbishnoi    09/27/10 - XbranchMerge sbishnoi_bug-10145105_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/26/10 - Adding BIGDECIMAL as compatible with OBJECT
    udeshmuk    07/23/10 - XbranchMerge udeshmuk_bug-9916298_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    06/22/10 - add alias to fieldMetadata mapping to constructor
    udeshmuk    06/11/10 - support cql types as parameter
    udeshmuk    05/11/10 - add return type to the config
    udeshmuk    04/29/10 - add logging
    udeshmuk    01/07/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeFunctionMetadata.java /main/11 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IConstructor;
import oracle.cep.extensibility.type.IConstructorMetadata;
import oracle.cep.extensibility.type.IField;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IType;
import oracle.cep.metadata.MetadataException;

public class JdbcCartridgeFunctionMetadata implements ISimpleFunctionMetadata
{
  private String funcName;
  
  private String sql;
  
  private Connection connection;
  
  private JdbcCollectionType returnType;
  
  private List<JdbcCartridgeFuncParamMetadata> paramInfos;
  
  private List<Integer> prepStmtParamToFnParamMapping;
  
  private String cartridgeContextName;
  
  private Map<String, IFieldMetadata> aliasToFieldMap;
  
  private ICartridgeContext cartridgeCtx;

  //used for recovery in case of connection failure
  protected JdbcCartridgeContext jdbcCtx;
  
  public JdbcCartridgeFunctionMetadata(String funcName, 
                                       JdbcCollectionType returnType, 
                                       String sql, 
                                       List<JdbcCartridgeFuncParamMetadata> paramInfos,
                                       List<Integer> prepStmtParamToFnParamMapping,
                                       String contextName,
                                       Connection connection,                                       
                                       Map<String, IFieldMetadata> aliasToFieldMap,
                                       ICartridgeContext cartridgeCtx,
                                       JdbcCartridgeContext jdbcCtx)
  {
    this.funcName = funcName;
    this.returnType = returnType;
    this.sql = sql;
    this.paramInfos = paramInfos; 
    this.prepStmtParamToFnParamMapping = prepStmtParamToFnParamMapping;
    this.cartridgeContextName = contextName;
    this.connection = connection;
    this.aliasToFieldMap = aliasToFieldMap;
    this.cartridgeCtx = cartridgeCtx;
    this.jdbcCtx = jdbcCtx;
  }
  
  @Override
  public int getNumParams()
  {
    return paramInfos.size();
  }

  @Override
  public IAttribute getParam(int pos) throws MetadataException
  {
    try
    {
      return paramInfos.get(pos);
    }
    catch(IndexOutOfBoundsException e)
    {
      throw new MetadataException(MetadataError.PARAMETER_NOT_FOUND_AT_DEF_POS,
                                  new Object[]{pos});
    }
  }

  @Override
  public Datatype getReturnType()
  {
    return returnType; 
  }

  @Override
  public String getName()
  {
    return funcName;
  }

  @Override
  public String getSchema()
  {
    return cartridgeContextName; 
    //FIXME: is this correct? - might need to be changed to something
    //that uniquely identifies the cartridge e.g. cartridgeName + appName.
  }
  
  public String getSql()
  {
    return sql;
  }

  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }
  
  @Override
  public SingleElementFunction getImplClass()
  {
    try 
    {
      return new SingleElementFunction() 
      {
        // Each JDBC cartridge function instance should have its own prepared
        // statement which will enable multiple threads to run instances of same 
        // JDBC Cartridge function in parallel.
        // If there is a single PreparedStatement object for all instances of a
        // JDBC Cartridge function, then the parallel invocation of the execute()
        // function will result into an invalid state of PreparedStatement object.
        PreparedStatement prepStmt = connection.prepareStatement(sql);
      
        @Override
        public Object execute(Object[] args) throws UDFException
        {
        
          try
          {
            if(prepStmt == null)
            {
              throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
                "Null PreparedStatement object found. JDBC Cartridge Function "+
                funcName+"@"+cartridgeContextName
                + " is not initialized properly." );
            }

            assert prepStmt != null: "PreparedStatment should not be null when" +
              " execute is called.";
            
            if(prepStmt.isClosed())
            {
              LogUtil.warning(LoggerType.TRACE, "PreparedStatement already closed! Attempting to renew..");
              prepStmt = connection.prepareStatement(sql);
              LogUtil.warning(LoggerType.TRACE, "PreparedStatement renewed..");
            } 

            String logString = null;

            if(LogUtil.isFineEnabled(LoggerType.TRACE))
            {
              logString = funcName+"@"+cartridgeContextName+" about to be"+
                          " called with "+ prepStmtParamToFnParamMapping.size()+
                          " param values : ";
            }
            
            //set the parameters
            for(int pos=0; pos < prepStmtParamToFnParamMapping.size(); pos++)
            {
              Integer fnArgListPos = prepStmtParamToFnParamMapping.get(pos);
              Datatype type = paramInfos.get(fnArgListPos).getType();           
              String paramVal = setParam(prepStmt, pos+1, args[fnArgListPos], 
                                         type);
              if(logString != null);
              {
                if(pos != prepStmtParamToFnParamMapping.size() - 1)
                  logString = logString + paramVal+ ", ";
                else
                  logString = logString + paramVal;
              }
            }
            
            if(logString != null)
              LogUtil.fine(LoggerType.TRACE, logString);
            
            //call execute
            
  
            IType compType = returnType.getComponentType();
            
            //If IComplexType then get the constructor
            IConstructor constructor = null;
            if(compType instanceof IComplexType)
            {           
              try
              {
                //get the default constructor (one without args)
                IConstructorMetadata constructorMeta = 
                  ((IComplexType) compType).getConstructor();
                constructor = 
                  constructorMeta.getConstructorImplementation();
              }
              catch(MetadataNotFoundException me)
              {
                throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
                                       "Default constructor not found for "+
                                       compType.name());
              }
            }
            
            ResultSetMetaData resultSetMeta = prepStmt.getMetaData();
            
            List<Object> listOfRecords = new ArrayList<Object>();
            
            int numCols = resultSetMeta.getColumnCount();
            ResultSet resultSet = prepStmt.executeQuery();
            while(resultSet.next())
            {
              //If IComplexType create an instance of compType for every record   
              if(compType instanceof IComplexType)
              {
                assert constructor != null : "Constructor not found in " +
                                             compType.name();
                Object instance = null;
                try
                {
                  instance = constructor.instantiate(null, cartridgeCtx);  
                }
                catch(RuntimeInvocationException re)
                {
                  throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
                                         re.getMessage());
                }
                
                assert instance != null : "Object of return-component-type could not"
                  +" be created.";
                
                //set the values in the created instance
                for(int col=1; col <= numCols; col++)
                {
                  String alias = resultSetMeta.getColumnName(col);
                  IFieldMetadata fieldMeta = aliasToFieldMap.get(alias);
                  IField field = fieldMeta.getFieldImplementation();
                  try 
                  {
                    switch(fieldMeta.getType().getKind())
                    {
                      case INT:
                        field.set(instance, resultSet.getInt(col), cartridgeCtx);
                        break;
                      case BIGINT:
                        field.set(instance, resultSet.getLong(col), cartridgeCtx);
                        break;
                      case FLOAT:
                        field.set(instance, resultSet.getFloat(col), cartridgeCtx);
                        break;
                      case DOUBLE:
                        field.set(instance, resultSet.getDouble(col), cartridgeCtx);
                        break;
                      case BIGDECIMAL:
                        field.set(instance, resultSet.getBigDecimal(col), cartridgeCtx);
                        break;
                      case BOOLEAN:
                        field.set(instance, resultSet.getBoolean(col), cartridgeCtx);
                        break;
                      case CHAR:
                        field.set(instance, resultSet.getString(col), cartridgeCtx);
                        break;
                      case BYTE:
                        field.set(instance, resultSet.getBytes(col), cartridgeCtx);
                        break;
                      case TIMESTAMP:
                        field.set(instance, resultSet.getTimestamp(col), cartridgeCtx);
                        break;
                      case OBJECT:
                        if(fieldMeta.getType().name() != null &&
                            fieldMeta.getType().name().equals("java.sql.Timestamp"))
                          field.set(instance, resultSet.getTimestamp(col), cartridgeCtx);
                        else if(fieldMeta.getType().name() != null &&
                            fieldMeta.getType().name().equals("java.sql.Time"))
                          field.set(instance, resultSet.getTime(col), cartridgeCtx);
                        else if(fieldMeta.getType().name() != null &&
                            fieldMeta.getType().name().equals("java.math.BigDecimal"))
                          field.set(instance, resultSet.getBigDecimal(col), cartridgeCtx);
                        else
                          field.set(instance, resultSet.getObject(col), cartridgeCtx);
                        break;
                      default:
                        assert false;
                    }
                  }
                  catch(RuntimeInvocationException re)
                  {
                    throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
                      re.getMessage());
                  }
                }
                
                //add instance to list
                listOfRecords.add(instance);
              }
              else //CQL native type
              {
                //no. of columns in ResultSet would be 1 here.
                assert numCols == 1 : "return-component-type is native CQL" +
                  " but resultset has more than one columns.";
                switch(compType.getKind())
                {
                  case INT:
                    listOfRecords.add(resultSet.getInt(1));
                    break;
                  case BIGINT:
                    listOfRecords.add(resultSet.getLong(1));
                    break;
                  case FLOAT:
                    listOfRecords.add(resultSet.getFloat(1));
                    break;
                  case DOUBLE:
                    listOfRecords.add(resultSet.getDouble(1));
                    break;
                  case BIGDECIMAL:
                    listOfRecords.add(resultSet.getBigDecimal(1));
                    break;
                  case BOOLEAN:
                    listOfRecords.add(resultSet.getBoolean(1));
                    break;
                  case CHAR:
                    listOfRecords.add(resultSet.getString(1));
                    break;
                  case BYTE:
                    listOfRecords.add(resultSet.getBytes(1));
                    break;
                  case TIMESTAMP:
                    java.sql.Timestamp tsValue = resultSet.getTimestamp(1);
                    if(tsValue != null)
                      listOfRecords.add(tsValue.getTime());
                    else
                      listOfRecords.add(Long.MIN_VALUE);
                    break;
                  case OBJECT:
                    listOfRecords.add(resultSet.getObject(1));
                    break;
                  default:
                    assert false;
                }
              }
            }
            
            //clear parameters
            prepStmt.clearParameters();
            
            return listOfRecords;
          }
          catch(SQLRecoverableException se)
          {
            //connection related issues must be the cause so should
            //try to renew the connection and re-execute for same args
            LogUtil.warning(LoggerType.TRACE, "Exception encountered while running JDBC cartridge function "+se.getMessage());
            LogUtil.info(LoggerType.TRACE, "Trying to recreate database connection.. ");
            try{
              prepStmt.close();
            }
            catch(SQLException sqe)
            {
            }
            prepStmt = null;
            do {
              boolean renewed = jdbcCtx.renewConnection();
              if(!renewed)
                throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
                se, new Object[]{funcName+"@"+cartridgeContextName});          

              LogUtil.info(LoggerType.TRACE, "Database connection obtained from datasource! Attempting to run the cartridge function on the earlier arguments");
              try{
                prepStmt = connection.prepareStatement(sql);
              }
              catch(SQLException sqle)
              {
                prepStmt = null;
              }
            } while(prepStmt == null);

            return execute(args);                  
            
          }
          catch(SQLException e)
          {           
            throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
              e, new Object[]{funcName+"@"+cartridgeContextName});          
          }
        }
      };
    }
    catch(SQLRecoverableException se1)
    {
      //connection related issues must be the cause so should
      //try to renew the connection and re-execute for same args
      
      LogUtil.warning(LoggerType.TRACE, "Exception encountered while trying to get instance of jdbc cartridge function "+se1.getMessage());
      LogUtil.info(LoggerType.TRACE, "Trying to recreate database connection.. ");
      boolean isRenewed = false;
      isRenewed = jdbcCtx.renewConnection();
      if(!isRenewed)
      {
        String errorMsg = 
          "Invalid Initialization of JDBCCartridgeFunction for function " 
          + this.funcName + " Cause:" + se1.getMessage();
        LogUtil.info(LoggerType.TRACE, errorMsg);
      }
      else
      {
        LogUtil.info(LoggerType.TRACE, "Database connection obtained from datasource! Attempting to run getImplClass()");
        return getImplClass();
      }
    
    }
    catch (SQLException e1) 
    {
      // Log Error Message
      String errorMsg = 
        "Invalid Initialization of JDBCCartridgeFunction for function " 
        + this.funcName + " Cause:" + e1.getMessage();
      LogUtil.info(LoggerType.TRACE, errorMsg);
    }
    return null;
  }
  
  private String setParam(PreparedStatement prepStmt,
                          int prepStmtParamPos,
                          Object value,
                          Datatype paramType) throws SQLException
  {
    switch(paramType.kind)
    {   
      case BOOLEAN :
        prepStmt.setBoolean(prepStmtParamPos, ((Boolean) value));
        return ((Boolean)value).toString();
        
      case CHAR : 
        prepStmt.setString(prepStmtParamPos, ((String) value));
        return ((String)value);
        
      case BYTE: //TODO: verify if this works 
        prepStmt.setBytes(prepStmtParamPos, ((byte[]) value));
        return ((byte[])value).toString();
        
      case INT :
        prepStmt.setInt(prepStmtParamPos, ((Integer) value));
        return ((Integer)value).toString();
        
      case BIGINT :
        prepStmt.setLong(prepStmtParamPos, ((Long) value));
        return ((Long)value).toString();
        
      case FLOAT :
        prepStmt.setFloat(prepStmtParamPos, ((Float) value));
        return ((Float)value).toString();
        
      case DOUBLE :
        prepStmt.setDouble(prepStmtParamPos, ((Double) value));
        return ((Double)value).toString();
        
      case BIGDECIMAL: //TODO: verify if it works.
        prepStmt.setBigDecimal(prepStmtParamPos, ((BigDecimal) value));
        return ((BigDecimal)value).toString();
        
      case TIMESTAMP: //TODO : verify if it works.
        prepStmt.setTimestamp(prepStmtParamPos, ((Timestamp) value));
        return ((Timestamp)value).toString();
        
      //TODO: add remaining types like interval and object?
      default:
        throw new SQLException("Unsupported parameter type "+
          paramType.toString() + " for prepared stmt");
    }
    
  }
}
