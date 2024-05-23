package com.oracle.cep.cartridge.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;

import com.bea.wlevs.ede.api.BatchRelationSender;
import com.bea.wlevs.ede.api.BatchRelationSink;
import com.bea.wlevs.ede.api.ConfigurationException;
import com.bea.wlevs.ede.api.DisposableBean;
import com.bea.wlevs.ede.api.EventProcessingException;
import com.bea.wlevs.ede.api.EventSinkManager;
import com.bea.wlevs.ede.api.EventType;
import com.bea.wlevs.ede.api.EventTypeRepository;
import com.bea.wlevs.ede.api.InitializingBean;
import com.bea.wlevs.ede.api.RelationSource;
import com.bea.wlevs.ede.api.StreamSender;
import com.bea.wlevs.ede.api.Type;
import com.bea.wlevs.util.ErrorMessage;
import com.bea.wlevs.util.ExceptionUtils;
import com.bea.wlevs.util.Service;
import com.bea.wlevs.ede.spi.TableStage;
import com.bea.wlevs.ede.spi.EventManagerAccessor;

import com.bea.wlevs.configuration.internal.Configuration;
import com.bea.wlevs.processor.impl.EventSenderImpl;

import java.sql.DatabaseMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * Table stage implementation.
 * This implementation handles the 3 roles that a table stage may have:
 * - relation source to a cql processor
 * - event sink that saves incoming events to a database table
 * - event source that sends incoming events to the stages
 *   connected downstream from it
 */
public class TableExternalSourceStage
    extends EventSenderImpl
    implements TableStage,
               RelationSource,
               BatchRelationSender,
               BatchRelationSink,
               IArchiver,
               EventManagerAccessor,
               InitializingBean,
               DisposableBean
{
    private String m_id;
    private String m_eventTypeName;
    private String m_jdbcDataSourceName;
    private String m_tableName;
    private String[] m_keyPropNames;
    private long m_externalRowsThreshold = Long.MIN_VALUE;

    private Configuration m_configuration; // used to get to the datasource
    private EventTypeRepository m_etr;
    private EventType m_eventType;
    private String[] m_propNames;
    private HashMap<String, Integer> m_sqlTypes = new HashMap<String, Integer>();

    private DataSource m_dataSource;
    private String m_insertStr;
    private String m_updateStr;
    private String m_deleteStr;
    private boolean m_booleanTypeSupported;
    
    public void afterPropertiesSet()
    {
        validate();

        m_dataSource = JdbcCartridgeContext.getDataSource(m_jdbcDataSourceName, m_configuration);
        
        validateTableAndColumns();
    }

    private void validate()
    {
        assert m_id != null;

        validateEventType();

        if (m_jdbcDataSourceName == null)
        {
            ErrorMessage err = ExceptionUtils.getErrorMessage(
                      JdbcCartridgeLogger.noTableDataSourceLoggable(), 
                      JdbcCartridgeContext.LOCALIZER_CLASS,
                      JdbcCartridgeContext.CLASS_LOADER);
            throw new ConfigurationException(err.getDetailedMessage());
        }
        
    }


    private void validateEventType()
    {
        if (m_eventTypeName == null ||
           m_etr.getEventType(m_eventTypeName)==null)
        {
            ErrorMessage err = ExceptionUtils.getErrorMessage(
                  JdbcCartridgeLogger.invalidTableEventTypeLoggable(), 
                  JdbcCartridgeContext.LOCALIZER_CLASS,
                  JdbcCartridgeContext.CLASS_LOADER,
                  m_id, m_eventTypeName);
            throw new ConfigurationException(err.getDetailedMessage());
        }
        m_eventType= m_etr.getEventType(m_eventTypeName);
        
        // The order in this array will be used in SQL statements
        m_propNames = m_eventType.getPropertyNames();

        // Verify that the property types are all supported
        for (int i=0; i<m_propNames.length; i++)
        {
            Type type = m_eventType.getProperty(m_propNames[i]).getType();

            switch(type)
            {
                case INT:
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.INTEGER);
                    break;
                case BIGINT : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.BIGINT);
                    break;
                case FLOAT : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.FLOAT);
                    break;
                case DOUBLE : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.DOUBLE);
                    break;
                case BYTE : // deal with separately at insertion, as there
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.BINARY); //is no sql BYTE type
                    break;
                case CHAR : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.CHAR);
                    break;
                case BOOLEAN : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.BOOLEAN);
                    break;
                case TIMESTAMP : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.TIMESTAMP);
                    break;
                case INTERVAL : 
                case INTERVALYM : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.CHAR);
                    break;
                case XMLTYPE : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.CHAR);
                    break;
                case BIGDECIMAL : 
                    m_sqlTypes.put(m_propNames[i], java.sql.Types.NUMERIC);
                    break;
                case OBJECT : 
                    // Has to be checked for Integer, Float, Double, Boolean
                    // and for BigDecimal for backward compatibility
                    // When event-type class is defined, then this block is used for all properties
                    m_sqlTypes.put(m_propNames[i], getTypeFromClass(m_eventType.getPropertyType(m_propNames[i]), m_propNames[i]));
                    break;
                case UNKNOWN : 
                    ErrorMessage err = ExceptionUtils.getErrorMessage(
                        JdbcCartridgeLogger.unsupportedTablePropTypeLoggable(), 
                        JdbcCartridgeContext.LOCALIZER_CLASS,
                        JdbcCartridgeContext.CLASS_LOADER,
                        m_propNames[i], m_eventTypeName);
                    throw new ConfigurationException(err.getDetailedMessage());
            }
        }

        // make sure that key properties are specified and are actual properties
        if (m_keyPropNames==null || m_keyPropNames.length==0)
            return;
            // Throwing exception here causes issues with tables
            // that are just external sources with 12c schema
            //throw new ConfigurationException("Key property names must be specified for table");
        for (int i=0; i<m_keyPropNames.length; i++)
        {
            if (m_sqlTypes.get(m_keyPropNames[i])==null)
            {
                ErrorMessage err = ExceptionUtils.getErrorMessage(
                    JdbcCartridgeLogger.invalidKeyPropertyLoggable(), 
                    JdbcCartridgeContext.LOCALIZER_CLASS,
                    JdbcCartridgeContext.CLASS_LOADER,
                    m_keyPropNames[i], m_id);
                throw new ConfigurationException(err.getDetailedMessage());
            }
        }
    }

    private int getTypeFromClass(Class classType, String propName)
    {
        if (classType==null)
            return java.sql.Types.OTHER;
        else if (classType.equals(java.math.BigInteger.class))
            return java.sql.Types.BIGINT;
        else if (classType.equals(java.lang.Integer.class))
            return java.sql.Types.INTEGER;
        else if (classType.equals(java.lang.Long.class))
            return java.sql.Types.BIGINT;
        else if (classType.equals(java.lang.Float.class))
            return java.sql.Types.FLOAT;
        else if (classType.equals(java.lang.Double.class))
            return java.sql.Types.DOUBLE;
        else if (classType.equals(java.lang.Boolean.class))
            return java.sql.Types.BOOLEAN;
        else if (classType.equals(java.math.BigDecimal.class))
            return java.sql.Types.NUMERIC;
        else if (classType.equals(java.sql.Date.class))
            return java.sql.Types.DATE;            
        else if (classType.equals(java.sql.Timestamp.class))
            return java.sql.Types.TIMESTAMP;
        else if (classType.equals(java.sql.Time.class))
            return java.sql.Types.TIME;
        else if (classType.equals(java.lang.Character.class))
            return java.sql.Types.CHAR;
        else if (classType.equals(java.lang.String.class))
            return java.sql.Types.CHAR;  
        else if (classType.equals(char[].class))
            return java.sql.Types.CHAR;
        else
        {
            ErrorMessage err = ExceptionUtils.getErrorMessage(
                JdbcCartridgeLogger.unsupportedTablePropTypeLoggable(), 
                JdbcCartridgeContext.LOCALIZER_CLASS,
                JdbcCartridgeContext.CLASS_LOADER,
                propName, m_eventTypeName);
            throw new ConfigurationException(err.getDetailedMessage());
        }
    }
    
    private void validateTableAndColumns(){
        
        if(m_tableName == null) {
            //no validation done when table-name value is not set. It is assumed
            //that this instance of  <wlevs:table ..> is used as a table source
            return;
        }
        
        Connection connection = null;
        HashMap<String, Integer> dbColumnMap = new HashMap<String, Integer>();
        HashMap<String, List<String>> dbUniqueKeyMap = new HashMap<String, List<String>>();
        
        try{
            connection = m_dataSource.getConnection();
            ResultSet resultset = null;
            DatabaseMetaData meta = connection.getMetaData();
            
            String tableName = m_tableName.toUpperCase();
            
            resultset = meta.getTables(null, null, tableName, null);
            
            if(resultset == null || !resultset.next()){
                //To handle the case, where db table names are case sensitive, try again with actual specified table name
                tableName = m_tableName;
                resultset = meta.getTables(null, null, tableName, null);
                
                if(resultset == null || !resultset.next()){
                    //Throw configuration error if table does not exists
                    ErrorMessage err = ExceptionUtils.getErrorMessage(
                              JdbcCartridgeLogger.invalidTableTableNameLoggable(), 
                              JdbcCartridgeContext.LOCALIZER_CLASS,
                              JdbcCartridgeContext.CLASS_LOADER, m_tableName);
                    throw new ConfigurationException(err.getDetailedMessage());
                }

            }
            
            resultset = meta.getColumns(null, null, tableName, null);

            while (resultset != null && resultset.next()) {
                int type = resultset.getInt("DATA_TYPE");
                String columnName = resultset.getString("COLUMN_NAME");
                dbColumnMap.put(columnName.toUpperCase(), type);
            }
            
            //collect primary key info
            resultset = meta.getPrimaryKeys(null,null, tableName);
            
            while (resultset != null && resultset.next()) {
                String keyName = "PRIMARY_KEY";
                String columnName = resultset.getString("COLUMN_NAME");
                if(dbUniqueKeyMap.get(keyName) != null){
                    List<String> keyList = dbUniqueKeyMap.get(keyName);
                    keyList.add(columnName.toUpperCase());
                    dbUniqueKeyMap.put(keyName, keyList);
                }
                else{
                    List<String> keyList = new ArrayList<String>();
                    keyList.add(columnName.toUpperCase());
                    dbUniqueKeyMap.put(keyName, keyList);
                }
            }
            
            //collect all unique indexes info
            resultset = meta.getIndexInfo(null,null, tableName, true, false);
            
            while (resultset != null && resultset.next()) {
                String keyName = resultset.getString("INDEX_NAME");
                String columnName = resultset.getString("COLUMN_NAME");
                
                if(keyName == null || columnName == null)continue;
                
                if(dbUniqueKeyMap.get(keyName) != null){
                    List<String> keyList = dbUniqueKeyMap.get(keyName);
                    keyList.add(columnName.toUpperCase());
                    dbUniqueKeyMap.put(keyName, keyList);
                }
                else{
                    List<String> keyList = new ArrayList<String>();
                    keyList.add(columnName.toUpperCase());
                    dbUniqueKeyMap.put(keyName, keyList);
                }

            }
            
            //check if BOOLEAN type is supported
            resultset = meta.getTypeInfo();
            while (resultset != null && resultset.next()) {
                int type = resultset.getInt("DATA_TYPE");
                
                if(type == java.sql.Types.BOOLEAN){
                    m_booleanTypeSupported = true;
                }
            }
            
            
        }
        catch(Exception e){
            if(e instanceof ConfigurationException) throw (ConfigurationException)e;
            
            throw new ConfigurationException(e);
        }
        finally{

            try {
                if(connection != null) connection.close();
            } catch (SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing connection: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);

            }
        }
        
        //validate columns
        for (int i=0; i<m_propNames.length; i++)
        {
            if(dbColumnMap.get(m_propNames[i].toUpperCase()) == null){
                //Throw configuration error if column does not exists in table
                ErrorMessage err = ExceptionUtils.getErrorMessage(
                          JdbcCartridgeLogger.invalidTableEventPropertyLoggable(), 
                          JdbcCartridgeContext.LOCALIZER_CLASS,
                          JdbcCartridgeContext.CLASS_LOADER, m_propNames[i], m_tableName);
                throw new ConfigurationException(err.getDetailedMessage());                
            }
        }
        
        

        //validate key-properties
        if(m_keyPropNames == null || m_keyPropNames.length == 0) return;
        
        boolean keyMatchFound =false;
        
        for(List<String> keyList : dbUniqueKeyMap.values()){
            if(keyList.size() != m_keyPropNames.length){
                continue;
            }
            
            for(String eventKey : m_keyPropNames){
                if(keyList.contains(eventKey.toUpperCase())){
                   keyMatchFound = true ;
                }
                else{
                    keyMatchFound = false;
                }
            }
            
            if(keyMatchFound) break;
        }
        
        if(!keyMatchFound){
            //Log a warning message if key-properties does not match any primary key 
            // or unique index
            if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER)){
                ErrorMessage err = ExceptionUtils.getErrorMessage(
                          JdbcCartridgeLogger.invalidTableKeyPropertiesValueLoggable(), 
                          JdbcCartridgeContext.LOCALIZER_CLASS,
                          JdbcCartridgeContext.CLASS_LOADER, "[" + arrayToString(m_keyPropNames) + "]", m_tableName);

                LogUtil.warning(LoggerType.CUSTOMER, err.getDetailedMessage());                
            }

        }
        
    }
    
    private String arrayToString(String[] array){
        if(array == null) return null;
        
        StringBuffer buf = null;
        
        for(String str : array ){
            if(buf == null){
                buf = new StringBuffer();
            }
            else{
                buf.append(", ");
            }
            
            buf.append(str);
        }
        
        return buf.toString();
    }

    public String getJDBCDataSource() {
        return m_jdbcDataSourceName;
    }

    public void setJDBCDataSource(String dataSource) {
        m_jdbcDataSourceName = dataSource;
    }

    public String getEventType() {
        return m_eventTypeName;
    }

    public void setEventType(String eventTypeName) {
        m_eventTypeName = eventTypeName;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        m_id = id;
    }

    public Map<?,?> getCacheDataSource() {
        return null;
    }
    
    public String getTableName() {
      return m_tableName;
    }

    public void setTableName(String tableName) {
        m_tableName = tableName;
    }

    public Class getKeyClass() {
      return null;
    }

    public String[] getKeyPropertyNames() {
      return m_keyPropNames;
    }

    public void setKeyPropertyNames(String[] keyPropertyNames) {
        m_keyPropNames = keyPropertyNames;
    }

    public long getExternalRowsThreshold() {
      return m_externalRowsThreshold;
    }

    public void setExternalRowsThreshold(long externalRowsThreshold) {
        m_externalRowsThreshold = externalRowsThreshold;
    }

    public void onInsertEvent(Object event)
    {
        Connection connection = null;
        PreparedStatement insertPS = null;
        try
        {
            // We can't reuse the connection because there could be
            // multiple threads doing this.
            connection = m_dataSource.getConnection();
            insertPS = prepareInsertStatement(connection);
            setStatementParameters(insertPS, m_propNames, event, 0);
            insertPS.executeUpdate();
            connection.commit();
        }
        catch(SQLException e) {
            throw new EventProcessingException(e);
        }
        finally
        {
            try {
                if (insertPS!=null) insertPS.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing prepared statement: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
            try {
                if (connection!=null) connection.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing connection: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
        }

        sendInsertEvent(event);
    }

    private PreparedStatement prepareInsertStatement(Connection connection)
        throws SQLException
    {
        if(m_tableName == null) {
            ErrorMessage err = ExceptionUtils.getErrorMessage(
                      JdbcCartridgeLogger.noTableTableNameLoggable(), 
                      JdbcCartridgeContext.LOCALIZER_CLASS,
                      JdbcCartridgeContext.CLASS_LOADER);
            throw new ConfigurationException(err.getDetailedMessage());            
        }

        if (m_insertStr==null)
        {
            StringBuffer sb = new StringBuffer("INSERT INTO " + m_tableName + "(");
            StringBuffer paramsSB = new StringBuffer();
            for (int i=0; i<m_propNames.length; i++)
            {
                sb.append(m_propNames[i]);
                paramsSB.append("?");
                if (i < m_propNames.length-1)
                {
                    sb.append(", ");
                    paramsSB.append(", ");
                }
            }
            sb.append(") VALUES (" + paramsSB + ")");
            m_insertStr = sb.toString();
            if (LogUtil.isFineEnabled(LoggerType.TRACE))
                LogUtil.fine(LoggerType.TRACE, "Insert stmt:\n" + m_insertStr);
        }

        return connection.prepareStatement(m_insertStr);
    }

    public void onDeleteEvent(Object event)
    {
        validateKeyValuesForUpdateOrDelete(event);
        
        Connection connection = null;
        PreparedStatement deletePS = null;
        try
        {
            // We can't reuse the connection because there could be
            // multiple threads doing this.
            connection = m_dataSource.getConnection();
            deletePS = prepareDeleteStatement(connection);
            setStatementParameters(deletePS, m_keyPropNames, event, 0);
            deletePS.executeUpdate();
            connection.commit();
        }
        catch(SQLException e) {
            throw new EventProcessingException(e);
        }
        finally
        {
            try {
                if (deletePS!=null) deletePS.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing prepared statement: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
            try {
                if (connection!=null) connection.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing connection: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
        }

        sendDeleteEvent(event);
    }

    private PreparedStatement prepareDeleteStatement(Connection connection)
        throws SQLException
    {
        if(m_tableName == null) {
            ErrorMessage err = ExceptionUtils.getErrorMessage(
                      JdbcCartridgeLogger.noTableTableNameLoggable(), 
                      JdbcCartridgeContext.LOCALIZER_CLASS,
                      JdbcCartridgeContext.CLASS_LOADER);
            throw new ConfigurationException(err.getDetailedMessage());            
        }

        if (m_deleteStr==null)
        {
            StringBuffer sb =
                new StringBuffer("DELETE FROM " + m_tableName + " WHERE ");
            for (int i=0; i<m_keyPropNames.length; i++)
            {
                if (i>0)
                    sb.append(" AND ");
                sb.append(m_keyPropNames[i] + " = ?");
            }

            m_deleteStr = sb.toString();
            if (LogUtil.isFineEnabled(LoggerType.TRACE))
                LogUtil.fine(LoggerType.TRACE, "Delete stmt:\n" + m_deleteStr);
        }
        
        return connection.prepareStatement(m_deleteStr);
    }
    
    private void validateKeyValuesForUpdateOrDelete(Object event){
        if (m_keyPropNames==null || m_keyPropNames.length==0)
            throw new EventProcessingException("Table key properties must be specified for update and delete operations");

        for (int i=0; i<m_keyPropNames.length; i++){
            Object value = m_eventType.getPropertyValue(event, m_keyPropNames[i]);
            if(value == null){
                throw new EventProcessingException("Table key property " +  m_keyPropNames[i]+ " value in event is null");
            }
        }
    }

    public void onUpdateEvent(Object event)
    {
        validateKeyValuesForUpdateOrDelete(event);
        
        Connection connection = null;
        PreparedStatement updatePS = null;
        try
        {
            // We can't reuse the connection because there could be
            // multiple threads doing this.
            connection = m_dataSource.getConnection();
            updatePS = prepareUpdateStatement(connection);
            setStatementParameters(updatePS, m_propNames, event, 0);
            setStatementParameters(updatePS, m_keyPropNames, event, m_propNames.length);
            updatePS.executeUpdate();
            connection.commit();
        }
        catch(SQLException e) {
            throw new EventProcessingException(e);
        }
        finally
        {
            try {
                if (updatePS!=null) updatePS.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing prepared statement: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
            try {
                if (connection!=null) connection.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing connection: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
        }

        sendUpdateEvent(event);
    }

    private PreparedStatement prepareUpdateStatement(Connection connection)
        throws SQLException
    {
        if(m_tableName == null) {
            ErrorMessage err = ExceptionUtils.getErrorMessage(
                      JdbcCartridgeLogger.noTableTableNameLoggable(), 
                      JdbcCartridgeContext.LOCALIZER_CLASS,
                      JdbcCartridgeContext.CLASS_LOADER);
            throw new ConfigurationException(err.getDetailedMessage());            
        }


        if (m_updateStr==null)
        {
            StringBuffer sb = new StringBuffer("UPDATE " + m_tableName + " SET ");
            for (int i=0; i<m_propNames.length; i++)
            {
                if (i>0)
                    sb.append(", ");
                sb.append(m_propNames[i] + " = ?");
            }

            sb.append(" WHERE ");
            for (int i=0; i<m_keyPropNames.length; i++)
            {
                if (i>0)
                    sb.append(" AND ");
                sb.append(m_keyPropNames[i] + " = ?");
            }

            m_updateStr = sb.toString();
            if (LogUtil.isFineEnabled(LoggerType.TRACE))
                LogUtil.fine(LoggerType.TRACE, "Update stmt:\n" + m_updateStr);
        }

        return connection.prepareStatement(m_updateStr);
    }

    public void onEvents(Collection<Object> insertEvents,
                         Collection<Object> deleteEvents,
                         Collection<Object> updateEvents)
    {
        Connection connection = null;
        try
        {
            connection = m_dataSource.getConnection();
            connection.setAutoCommit(false);
            int[] counts = null;
            
            if(insertEvents != null && !insertEvents.isEmpty()){
                PreparedStatement insertPS = prepareInsertStatement(connection);
                for (Object event : insertEvents)
                {
                    setStatementParameters(insertPS, m_propNames, event, 0);
                    insertPS.addBatch();
                }
                
                counts = insertPS.executeBatch();
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, getBatchUpdateStatus(counts, "insert"));
            }
            else{
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, "insertEvents collection is null or empty");
            }

            
            if(updateEvents != null && !updateEvents.isEmpty()){
                PreparedStatement updatePS = prepareUpdateStatement(connection);
                for (Object event : updateEvents)
                {
                    setStatementParameters(updatePS, m_propNames, event, 0);
                    setStatementParameters(updatePS, m_keyPropNames, event, m_propNames.length);
                    updatePS.addBatch();
                }
                counts = updatePS.executeBatch();
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, getBatchUpdateStatus(counts, "update"));
            }
            else{
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, "updateEvents collection is null or empty");
            }

            if(deleteEvents != null && !deleteEvents.isEmpty()){
                PreparedStatement deletePS = prepareDeleteStatement(connection);
                for (Object event : deleteEvents)
                {
                    setStatementParameters(deletePS, m_keyPropNames, event, 0);
                    deletePS.addBatch();
                }
                counts = deletePS.executeBatch();
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, getBatchUpdateStatus(counts, "delete"));
            }
            else{
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, "deleteEvents collection is null or empty");
            }

            connection.commit();
        }
        catch(SQLException e) {
            throw new EventProcessingException(e);
        }
        finally
        {
            try { // This will also close the prepared statements
                if (connection!=null) connection.close();
            } catch(SQLException e) {
                if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                    LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing connection: " + e.getMessage());
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.logStackTrace(e);
            }
        }

        sendEvents(insertEvents, deleteEvents, updateEvents);
    }

    private void setStatementParameters(PreparedStatement ps,
                                        String[] propNames,
                                        Object event,
                                        int startIndex)
        throws SQLException
    {
        for (int i=0; i<propNames.length; i++)
        {
            Object value = m_eventType.getPropertyValue(event, propNames[i]);
            if (LogUtil.isFineEnabled(LoggerType.TRACE))
                LogUtil.fine(LoggerType.TRACE, "Setting value [" + value + "] for property [" + propNames[i]);

            int sqlType = m_sqlTypes.get(propNames[i]);
            
            // bytes needs special handling
            if (sqlType ==java.sql.Types.BINARY )
            {
                if(value!=null) 
                    ps.setBytes(i+1+startIndex, (byte[]) value);
                else
                    ps.setBytes(i+1+startIndex, null);
                
                continue;
            }
            
            if(value != null && value instanceof java.lang.Character){
                value = value.toString();
            }
            else if(value != null && value instanceof char[]){
                value = new String((char[])value);
            }
            else if(value !=null && 
                    sqlType == java.sql.Types.TIMESTAMP && 
                    value instanceof java.lang.Long){
                
                long time = ((Long)value).longValue();
                value = new java.sql.Timestamp(time);      
                
            }
            
            if(sqlType == java.sql.Types.BOOLEAN && !m_booleanTypeSupported){
                //if underlying db does not support BOOLEAN type, use BIT
                sqlType = java.sql.Types.BIT;           
            }
            
            if (value==null)
                ps.setNull(i+1+startIndex, sqlType);
            else
                ps.setObject(i+1+startIndex, value, sqlType);
        }
    }
    

    @Service
    public void setConfiguration(Configuration configuration)
    {
        m_configuration = configuration;
    }

    @Service
    public void setEventTypeRepository(EventTypeRepository etr)
    {
        m_etr = etr;
    }

    public void destroy()
    {
       // Nothing to do as of now
    }

    private String getBatchUpdateStatus(int[] counts, String op)
    {
        StringBuffer sb = new StringBuffer("Batch execute on " + op + " statements returned:");
        if (counts==null || counts.length==0)
            sb.append("NULL");
        else
        for (int i=0; i<counts.length; i++)
            sb.append(counts[i] + " ");

        return sb.toString();
    }

    //IArchiver interface impl
    public IArchiverQueryResult execute(QueryRequest[] queries)
    {
        // Don't execute this code for now because the sql that comes in
        // is messed up.
        if (0==0)
            throw new UnsupportedOperationException("Table does not support archiving feature currently");

        if (LogUtil.isFineEnabled(LoggerType.TRACE))
            LogUtil.fine(LoggerType.TRACE, "Table Archiver execute");
        Connection connection = null;
        ResultSet[] resultSets = new ResultSet[queries.length];
        // Cannot close the connection when we leave this method because
        // user of this method needs to access the ResultSets. However,
        // close the connection, if there was an error.
        boolean closeConnection = true;
        try
        {
            connection = m_dataSource.getConnection();
            for (int i=0; i<queries.length; i++)
            {
                if (LogUtil.isFineEnabled(LoggerType.TRACE))
                    LogUtil.fine(LoggerType.TRACE, "Table Archiver executing query [" + queries[i].getQuery() + "]");
                PreparedStatement ps = connection.prepareStatement(queries[i].getQuery());
                Object[] params = queries[i].getParams();
                for (int j=0; j<params.length; j++)
                    ps.setObject(j+1, params[j]);
                
                resultSets[i] = ps.executeQuery();
            }

            IArchiverQueryResult retVal = new ArchiverResultImpl(connection, resultSets);
            closeConnection = false;
            return retVal;
        }
        catch(SQLException e)
        { // Archiver API expects only Runtime exceptions
            throw new RuntimeException(e);
        }
        finally
        { // if there were errors, need to close the connection.
            if (closeConnection)
            {
              for (int i=0; i<resultSets.length; i++)
              {
                  if (resultSets[i]==null)
                      continue;
                  try {
                      resultSets[i].close();
                  } catch(SQLException e) {
                      if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                          LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing ResultSet: " + e.getMessage());
                      if (LogUtil.isFineEnabled(LoggerType.TRACE))
                          LogUtil.logStackTrace(e);
                  }
              }
              if (connection!=null)
              {
                  try {
                      connection.close();
                  } catch(SQLException e) {
                      if (LogUtil.isWarningEnabled(LoggerType.CUSTOMER))
                          LogUtil.warning(LoggerType.CUSTOMER, "Unexpected error closing connection: " + e.getMessage());
                      if (LogUtil.isFineEnabled(LoggerType.TRACE))
                          LogUtil.logStackTrace(e);
                  }
              }
            }
        }
    }

    public void closeResult(IArchiverQueryResult result)
    {
        if (result==null)
            return;
    }

    public EventSinkManager getEventSinkManager()
    {
        return this;
    }

    // Not used, because this extends EventSenderImpl
    public void setEventSender(StreamSender sender) { }
}
