/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/StreamPseudoColumn.java /main/3 2013/12/11 05:32:56 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Enumeration of pseudo columns for Streams

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/08/13 - adding support of QUERYNAME pseudo column
    sbishnoi    12/22/08 - changing element time to long value
    anasrini    08/22/07 - pseudo columns for streams
    anasrini    08/22/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/StreamPseudoColumn.java /main/3 2013/12/11 05:32:56 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */


package oracle.cep.common;

/**
 * Enumeration of pseudo columns for streams
 *
 * @since 1.0
 */

public enum StreamPseudoColumn
{

  ELEMENT_TIME("element_time", Datatype.BIGINT, 0, false, true),
  QUERY_ID("query_id", Datatype.CHAR, 10, true, false);

  /** The name of the pseudo column */
  private String columnName;

  /** The datatype of the pseudo column */
  private Datatype columnType;

  /** The length of the pseudo column */
  private int columnLen;
  
  /** A flag to check if the value of pseudo column will vary with each tuple */
  private boolean isConstant;
  
  /** A flag to check if the source should be a stream or not*/
  private boolean isStreamSourceRequired;

  /**
   * Constructor
   * @param columnName name of the pseudo column
   * @param columnType the datatype of the pseudo column
   * @param columnLen the length of the pseudo column
   */
  StreamPseudoColumn(String columnName, Datatype columnType, int columnLen, 
                     boolean isConstant, boolean isStreamSourceRequired)
  {
    this.columnName = columnName;
    this.columnType = columnType;
    this.columnLen  = columnLen;
    this.isConstant = isConstant;
    this.isStreamSourceRequired = isStreamSourceRequired;
  }

  /**
   * Get the name of the pseudo column
   * @return the name of the pseudo column
   */
  public String getColumnName() 
  {
    return columnName;
  }

  /**
   * Get the datatype of the pseudo column
   * @return the datatype of the pseudo column
   */
  public Datatype getColumnType() 
  {
    return columnType;
  }

  /**
   * Get the length of the pseudo column
   * @return the length of the pseudo column
   */
  public int getColumnLen()
  {
    return columnLen;
  }
  
  public boolean isConstant()
  {
    return isConstant;
  }
  
  public boolean isStreamSourceRequired()
  {
    return isStreamSourceRequired;
  }

  /**
   * Get the pseudo column number
   * @return the pseudo column number
   */
  public int getColumnNumber()
  {
    return ordinal();
  }

}
 
  
