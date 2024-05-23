/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/JDBCExternalPreparedStatement.java /main/9 2015/11/04 04:57:20 udeshmuk Exp $ */

/* Copyright (c) 2008, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/02/15 - add isClosed
    sbishnoi    10/04/13 - bug 17216701
    hopark      12/01/09 - workaround for timestamp with tz
    sborah      06/30/09 - support for bigdecimal
    hopark      02/23/09 - add setBoolean
    sbishnoi    01/02/09 - Creation
 */

package oracle.cep.extensibility.datasource;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Calendar;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.CEPSimpleDateFormat;
import oracle.cep.common.TimeZoneHelper;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * @version $Header:
 *          cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/JDBCExternalPreparedStatement.java
 *          /main/9 2015/11/04 04:57:20 udeshmuk Exp $
 * @author sbishnoi
 * @since release specific (what release of product did this appear in)
 */

public class JDBCExternalPreparedStatement
  implements IExternalPreparedStatement, IExternalHasResultSet
{
  /** java PreparedStatement Object */
  protected PreparedStatement pstmt;

  private boolean jdbcPrefMode = false;

  private IExternalConverter converter;

  private Map<String, Object> stats = new HashMap<>();

  /** Equivalent SQL Statement */
  private String sql;

  public static final String RUNNING_EXEC_TIME = "RUNNING_EXEC_TIME";

  public static final String NO_OF_EXECUTION = "NO_OF_EXECUTION";

  public PreparedStatement getPstmt()
  {
    return pstmt;
  }

  /** Result Set obtained after executing PreparedStatement */
  protected ResultSet resultSet;

  public JDBCExternalPreparedStatement(PreparedStatement pstmt, String sql)
  {
    this.pstmt = pstmt;
    this.sql = sql;
  }

  public JDBCExternalPreparedStatement(PreparedStatement pstmt, String sql,
      boolean jdbcPrefMode, IExternalConverter converter)
  {
    this.pstmt = pstmt;
    this.sql = sql;
    this.jdbcPrefMode = jdbcPrefMode;
    this.converter = converter;
  }

  /**
   * Executes the statement and returns an Iterator over result set
   * 
   * @return an Iterator over a collection of TupleValue objects
   */
  public Iterator<TupleValue> executeQuery() throws SQLException
  {
    try
    {
      resultSet = pstmt.executeQuery();
    }
    catch(SQLException e)
    {
      LogUtil.severe(LoggerType.TRACE, "Failed to execute SQL Query:"+sql +
        " Reason:" + e.getMessage());
      throw e;
    }

    // Returns null to make optimized implementation
    // Reason: ExternalSynopsis will call executeQuery and needs an
    // Iterator of tuple value; Then it will process all records in
    // iteration and again prepare a TupleIterator.
    // This might be inefficient if we are dealing millions of records.
	if(jdbcPrefMode)
		return null;
	
	// This is for jdbc cache flow , which requires the tuple values to load to cache
	// Performance will be better in cache mode 
	return converter.toTupleList(resultSet);
  }

  /**
   * Sets the designated parameter to the given Java array of bytes.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setBytes(int paramIndex, byte[] x) throws SQLException
  {
    pstmt.setBytes(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java double value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setDouble(int paramIndex, double x) throws SQLException
  {
    pstmt.setDouble(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java double value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setBigDecimal(int paramIndex, BigDecimal x) throws SQLException
  {
    pstmt.setBigDecimal(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java float value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setFloat(int paramIndex, float x) throws SQLException
  {
    pstmt.setFloat(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java int value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setInt(int paramIndex, int x) throws SQLException
  {
    pstmt.setInt(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java long value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setLong(int paramIndex, long x) throws SQLException
  {
    pstmt.setLong(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java boolean value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setBoolean(int paramIndex, boolean x) throws SQLException
  {
    pstmt.setBoolean(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given Java String value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setString(int paramIndex, String x) throws SQLException
  {
    pstmt.setString(paramIndex, x);
  }

  /**
   * Sets the designated parameter to the given java.sql.Timestamp value.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  public void setTimestamp(int paramIndex, Timestamp x) throws SQLException {
    TimeZone tz = null;
    if (x instanceof CEPDate) {
      CEPDate dateVal = (CEPDate) x;
      if (dateVal.getFormat() != null)
        tz = dateVal.getFormat().getTimeZone();
      if (tz == null)
        tz = CEPDateFormat.getInstance().getDefaultTimeZone();
      pstmt.setTimestamp(paramIndex, Timestamp.from(x.toInstant()), Calendar.getInstance(tz));
    }
  }

  /**
   * Sets the designated parameter to Null.
   * 
   * @param paramIndex
   *          the first parameter is 1, the second is 2, ...
   * @param SQLType
   *          the parameter value
   */
  public void setNull(int paramIndex, int SQLType) throws SQLException
  {
    pstmt.setNull(paramIndex, SQLType);
  }

  /**
   * Release this IExternalPreparedStatement object's resources immediately
   * without wait.
   */
  public void close() throws SQLException
  {
    pstmt.close();
  }

  public boolean isClosed() throws SQLException
  {
    return pstmt.isClosed();
  }

  /**
   * Get Current Result of the PreparedStatement execution
   * 
   * @return current ResultSet
   */
  public ResultSet getResultSet()
  {
    return resultSet;
  }

  public Map<String, Object> getStat()
  {
    return stats;
  }

  public boolean execOnEachEvt()
  {
    return false;
  }
}
