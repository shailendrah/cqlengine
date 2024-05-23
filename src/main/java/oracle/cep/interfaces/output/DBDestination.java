/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/DBDestination.java /main/11 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/29/11 - support for interval year to month based operations
    sborah      10/12/09 - support for bigdecimal
    hopark      03/06/09 - add opaque type
    hopark      02/02/09 - change IDataSourceFinder name
    sbishnoi    01/08/09 - enable usage of IExternalDataSource in place of java
                           DataSource
    sbishnoi    12/10/08 - support for generic data sources
    hopark      12/04/08 - add toString
    hopark      10/15/08 - TupleValue refactoring
    hopark      10/10/08 - remove statics
    hopark      06/26/08 - use datasource
    sbishnoi    03/10/08 - Creation
 */

package oracle.cep.interfaces.output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import javax.sql.DataSource;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.extensibility.datasource.IExternalDataSource;
import oracle.cep.extensibility.datasource.IExternalHasDataSource;
import oracle.cep.interfaces.DBHelper;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IDataSourceFinder;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/DBDestination.java /main/10 2009/11/09 10:10:59 sborah Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class DBDestination extends QueryOutputBase {
  
  /** DataSource name used to connect to Database*/
  private String            dataSourceName;
  
  /** Table Name where DBDestination will insert Tuple*/
  private String            tableName;
  
  /** SQL Connection Object */
  private Connection con;
  
 
  /** SQL Prepared Statements*/
  private PreparedStatement insertStmt;
  private PreparedStatement deleteStmt;
  private PreparedStatement updateStmt;
  private int numSetAttrs;
  
  
  /**
   * Constructor
   * @param ec TODO
   * @param tableName : name of table which user wants to manipulate
   * @param connectString: a string which used to Connect to required Database
   */
  public DBDestination(ExecContext ec, String dsName, String tableName)
  {
    super(ec);
    this.dataSourceName     = dsName;
    this.tableName         = tableName;
    this.numSetAttrs       = 0;
  }
  
  
  
  /**
   * Insert Tuple Value into Database Table
   */
  public void putNext(TupleValue tuple, QueueElement.Kind k) 
    throws CEPException 
  {
    try 
    {
      if(k == QueueElement.Kind.E_PLUS) {
        setInsertPreparedStatement(tuple);
        insertStmt.execute();
        con.commit();
      }
      else if(k == QueueElement.Kind.E_MINUS && isPrimaryKeyExist) {
        setDeletePreparedStatement(tuple);
        deleteStmt.execute();
        con.commit();
      }
      else if(k == QueueElement.Kind.E_UPDATE && isPrimaryKeyExist) {
        setUpdatePreparedStatement(tuple);
        updateStmt.execute();
        con.commit();
      }
      else
        assert isPrimaryKeyExist : isPrimaryKeyExist;
       
    }
    catch(SQLException e){
      closeConnections();
      throw new CEPException(InterfaceError.DB_ACCESS_FAILURE, e);      
    }
  }
  
  
  /**
   * Establish the Connection &
   * Validate Schema(//Validate Schema is pending)
   */
  public void start() throws CEPException {
    try 
    {
      ConfigManager cm = execContext.getServiceManager().getConfigMgr();
      IDataSourceFinder service = cm.getDataSourceFinder();
      
      if(service == null)
      {
        LogUtil.warning(LoggerType.TRACE, "datasourcefinder service " +
                        "feature not provided in the environement");
        throw new CEPException(
              InterfaceError.FEATURE_NOT_SUPPORTED_IN_THIS_ENVIRONMENT);  
      }
      
      IExternalDataSource extDataSrc = service.findDataSource(dataSourceName);
        
      // Note: Presently we don't support all kind of Data Sources for 
      // DB Destination; These are limited to External Sources.
      // So Following is a check whether the given data source is an instance
      // of JDBC Data source
      if (extDataSrc == null || !(extDataSrc instanceof IExternalHasDataSource))
      {
        throw new CEPException(InterfaceError.INVALID_DESTINATION,
                               dataSourceName);
      }      
      
      DataSource javaDataSource 
        = ((IExternalHasDataSource)extDataSrc).getJavaDataSource();
      
      //Obtain Connection from java.sql.DataSource 
      con = javaDataSource.getConnection();
      LogUtil.info(LoggerType.TRACE, "DBDestination: Connection Established");
            
      // Validate Schema
      DBHelper.validateSchema(super.numAttrs, super.attrNames, super.attrMetadata,
                              this.tableName, con);
      
      // Check: IF Output is Relation and Primary key must be defined over that
      if(!super.isStream && super.primaryKeyAttrPos.size() == 0)
        throw new InterfaceException(InterfaceError.SCHEMA_MISMATCH);
      LogUtil.info(LoggerType.TRACE, "DBDestination: Schema Validated");
            
      /* Initialize Prepared Statements*/
      initializePreparedStatements();
      LogUtil.info(LoggerType.TRACE, "DBDestination: Prepared Statement Created");   
      
    }
    catch(SQLException e){
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.DB_ACCESS_FAILURE);
    }
    catch (Exception e) {
      closeConnections();
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new CEPException(InterfaceError.INVALID_DESTINATION, e, 
          new Object[]{dataSourceName});
    }
  }
  
  public void end() throws CEPException {
    
  }
  
  /**
   * Initialize Prepared statement for INSERT, DELETE & UPDATE DML
   * @throws SQLException
   */
  private void initializePreparedStatements() throws SQLException
  {
    prepareInsertStatement();
    
    /** SQL Delete and Update Statements in string format*/
    StringBuffer deleteSql = new StringBuffer();
    StringBuffer updateSql = new StringBuffer();
    if(!isStream)
    {
      boolean isFirstSetClause = true;
      boolean isFirstWhereClause =  true;
      StringBuffer tWhereClause  = new StringBuffer(" where ");
      StringBuffer tSetClause    = new StringBuffer(" set ");
      
      for(int i = 0; i < numAttrs; i++)
      {
        if(isPrimaryKeyAttr[i])
        {
          if(isFirstWhereClause)
          {
            tWhereClause.append(super.attrNames[i] + " = ? ");
            isFirstWhereClause = false;
          }
          else
            tWhereClause.append(" and " + super.attrNames[i] + " = ? ");  
        }
        else
        {
          if(isFirstSetClause)
          {
            tSetClause.append(super.attrNames[i] + " = ? ");
            isFirstSetClause = false;
          }
          else
            tSetClause.append(" , " + super.attrNames[i] + "= ? ");
          numSetAttrs++;
        }
      }
      // If All Attributes are primary key; Then SET Clause will
      // set one arbitrary(first primary key attribute) attribute
      if(numSetAttrs == 0)
        tSetClause.append(super.attrNames[0] + " = ? ");
      deleteSql.append("delete from " + this.tableName + tWhereClause);
      updateSql.append("update " + this.tableName + tSetClause + tWhereClause);
      deleteStmt = con.prepareStatement(deleteSql.toString());
      updateStmt = con.prepareStatement(updateSql.toString());
    }
    
  }
  
  /**
   * Prepare Insert statement
   * @throws SQLException
   */
  private void prepareInsertStatement() throws SQLException
  {
    StringBuffer insertSql 
      = new StringBuffer("insert into " + this.tableName + " ( ");
    StringBuffer tInsertClause = new StringBuffer();
    insertSql.append(super.attrNames[0]);
    tInsertClause.append(") values( ?");
    for(int i = 1; i < numAttrs; i++) {
      insertSql.append(", " + super.attrNames[i]);
      tInsertClause.append(", ? ");
    }
    insertSql.append(tInsertClause + ")");
    insertStmt = con.prepareStatement(insertSql.toString());
  }
  
  /**
   * Set Insert Prepared Statement
   * @param tv Current Output Tuple Value
   * @throws SQLException
   * @throws CEPException
   */
  private void setInsertPreparedStatement(TupleValue tv) 
    throws SQLException, CEPException
  {
    boolean isAttrNull = false;
    for(int i = 0; i < numAttrs; i++)
    {
      AttributeValue av = tv.getAttribute(i);
      isAttrNull = av.isBNull();
      setPreparedStatement(i, i+1, isAttrNull, av.getAttributeType(),
                           tv, insertStmt);
    }
  }
  
  /**
   * Set Delete Prepared Statement
   * @param tv Current Output Tuple Value
   * @throws SQLException
   * @throws CEPException
   */
  private void setDeletePreparedStatement(TupleValue tv)
    throws SQLException, CEPException
  {
    int destPos = 1;
    for(int i =0; i < numAttrs; i++)
    {
      if(isPrimaryKeyAttr[i])
      {
        AttributeValue av = tv.getAttribute(i);
        assert !(av.isBNull()) : false;
        setPreparedStatement(i, destPos, false, av.getAttributeType(),
                             tv, deleteStmt);
        destPos++;
      }
    }
  }
  
  /**
   * Set Update Prepared Statement
   * @param tv Current Output Tuple Value
   * @throws SQLException
   * @throws CEPException
   */
  private void setUpdatePreparedStatement(TupleValue tv)
    throws SQLException, CEPException
  {
    int destSetPos  = 1;
    int destWherePos;
    // If All Attributes are primary key; Then SET Clause will
    // set one arbitrary(first primary key attribute) attribute
    if(numSetAttrs == 0)
      destWherePos = destSetPos + 1;
    else
      destWherePos = numSetAttrs + 1;
    boolean isAttrNull = false;
    for(int i = 0; i < numAttrs; i++)
    {
      isAttrNull = tv.getAttribute(i).isBNull();
      if(isPrimaryKeyAttr[i])
      {
        assert !isAttrNull : isAttrNull;
        setPreparedStatement(i, destWherePos, isAttrNull,
                             tv.getAttribute(i).getAttributeType(), tv, updateStmt);
        destWherePos++;
      }
      else
      {
        setPreparedStatement(i, destSetPos, isAttrNull,
                             tv.getAttribute(i).getAttributeType(), tv, updateStmt);
        destSetPos++;
      }
    }
    // If All Attributes are primary key; Then SET Clause will
    // set one arbitrary(first primary key attribute) attribute
    if(numSetAttrs == 0)
    {
      assert destSetPos == 1 : destSetPos;
      setPreparedStatement(0, destSetPos, isAttrNull,
                           tv.getAttribute(0).getAttributeType(), tv, updateStmt);
    }
  }
  
  /**
   * Set Prepared Statement
   * @param srcPos source attribute data position inside TupleValue
   * @param destPos destination data position for target Prepared Statement
   * @param isAttrNull is attribute null ?
   * @param dataType
   * @param tv Tuple Value
   * @param stmt target statement where values will be set
   * @throws SQLException
   * @throws CEPException
   */
  private void setPreparedStatement(int srcPos, 
                                   int destPos, 
                                   boolean isAttrNull,
                                   Datatype dataType,
                                   TupleValue tv,
                                   PreparedStatement stmt)
  throws SQLException, CEPException
  {
    if(isAttrNull)
      stmt.setNull(destPos, dataType.getSqlType());
    else
    {
      switch(dataType.getKind())
      {
      case INT:
        stmt.setInt(destPos, tv.iValueGet(srcPos));
        break;
      case BIGINT:
        stmt.setLong(destPos, tv.lValueGet(srcPos));
        break;
      case FLOAT:
        stmt.setFloat(destPos, tv.fValueGet(srcPos));        
        break;
      case DOUBLE:
        stmt.setDouble(destPos, tv.dValueGet(srcPos));
        break;
      case BIGDECIMAL:
        stmt.setBigDecimal(destPos, tv.nValueGet(srcPos));
        break;
      case BOOLEAN:
        stmt.setBoolean(destPos, tv.boolValueGet(srcPos));
        break;
      case BYTE:
        stmt.setBytes(destPos, tv.bValueGet(srcPos));
        break;
      case CHAR:
        stmt.setString(destPos, new String(tv.cValueGet(srcPos)));
        break;
      case TIMESTAMP:
        stmt.setTimestamp(destPos, new Timestamp(tv.tValueGet(srcPos)));
        break;
      case INTERVAL:
        stmt.setString(destPos, tv.vValueGet(srcPos));
        break;
      case INTERVALYM:
        stmt.setString(destPos, tv.vymValueGet(srcPos));
        break;
      /*case OBJECT:
        break;
      case VOID:
        break;
      case XMLTYPE:
        break;
      case UNKNOWN:
        break;*/
      default:
        assert false;
      } // End of Switch
    }
  }

  /**
   * Close all Connections & Statements
   * @throws CEPException
   */
  private void closeConnections() throws CEPException
  {
    try
    {
      if(con != null) con.close();
      if(insertStmt != null) insertStmt.close();
      if(deleteStmt != null) deleteStmt.close();
      if(updateStmt != null) updateStmt.close();
    }
    catch(SQLException e)
    {
      throw new CEPException(InterfaceError.DB_ACCESS_FAILURE);
    }
  }
  
  public String toString()
  {
    return toString("DBDestination(" + dataSourceName +","+ tableName + ")");
  }
}
