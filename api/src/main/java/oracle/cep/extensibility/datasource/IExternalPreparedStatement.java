/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IExternalPreparedStatement.java /main/6 2011/02/07 03:36:25 sborah Exp $ */

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
    sborah      06/30/09 - support for bigdecimal
    hopark      02/23/09 - add setBoolean
    sbishnoi    01/14/09 - removing unused import
    sbishnoi    12/10/08 - Creation
 */

package oracle.cep.extensibility.datasource;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

import oracle.cep.dataStructures.external.TupleValue;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IExternalPreparedStatement.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:43 anasrini Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public interface IExternalPreparedStatement
{
  /**
   * Executes the statement and returns an Iterator over result set
   * @return an Iterator over a collection of TupleValue objects
   */
  Iterator<TupleValue> executeQuery() throws Exception;
  
  /**
   * Sets the designated parameter to the given Java array of bytes.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setBytes(int paramIndex, byte[] x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java double value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setDouble(int paramIndex, double x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java BigDecimal value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setBigDecimal(int paramIndex, BigDecimal x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java float value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setFloat(int paramIndex, float x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java int value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setInt(int paramIndex, int x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java long value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setLong(int paramIndex, long x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java boolean value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setBoolean(int paramIndex, boolean x) throws Exception;
  
  /**
   * Sets the designated parameter to the given Java String value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setString(int paramIndex, String x) throws Exception;
  
  /**
   * Sets the designated parameter to the given java.sql.Timestamp value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  void setTimestamp(int paramIndex, Timestamp x) throws Exception;
  
  /**
   * Sets the designated parameter to Null.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param SQLType the parameter value
   */
  void setNull(int paramIndex, int SQLType) throws Exception;
  
  /**
   * Release this IExternalPreparedStatement object's resources immediately
   * without wait.
   */
  void close() throws Exception;
  
  Map<String,Object> getStat();
  
  boolean execOnEachEvt();

  //TODO we need better handling of stat from external
  //One idea is to use bytebuddy to create ExtStatClass

  public static final String RUNNING_EXEC_TIME = "RUNNING_EXEC_TIME";
  public static final String NO_OF_EXECUTION = "NO_OF_EXECUTION";

  public static final String CACHE_MISS_ENTRY_KEY = "cache_miss";
  public static final String CACHE_HIT_ENTRY_KEY = "cache_hits";
  public static final String CACHE_NAME_ENTRY_KEY = "cache_name";

}
