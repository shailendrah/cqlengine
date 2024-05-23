/* $Header: pcbpel/cep/common/src/oracle/cep/extensibility/datasource/JDBCExternalDataSource.java /main/3 2009/03/12 22:23:25 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/06/09 - schema.tbl support
    sbishnoi    12/10/08 - Creation
 */

package oracle.cep.extensibility.datasource;

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/extensibility/datasource/JDBCExternalDataSource.java /main/3 2009/03/12 22:23:25 parujain Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class JDBCExternalDataSource implements IExternalDataSource, IExternalHasDataSource
{
  /** java DataSource object which'll be wrapped into JDBCExternalDataSource*/
  protected javax.sql.DataSource dataSource;
  
  protected String tableName;
  private boolean jdbcPrefMode = false;
  private IExternalConverter converter;
  
  /**
   * Constructs a Wrapper JDBCExternalDataSource object for java DataSource
   * @param paramDataSource
   */
  public JDBCExternalDataSource(javax.sql.DataSource paramDataSource)
  {
    dataSource = paramDataSource; 
    tableName = null;
  }
  
  public JDBCExternalDataSource(javax.sql.DataSource paramDataSource,
                                String name, IExternalConverter extConverter)
  {
    dataSource = paramDataSource;
    tableName = name;
    converter = extConverter;
  }
  
  /**
   * Attempts to get a connection to the data source
   * @return a connection to the data source
   */ 
  public IExternalConnection getConnection() throws java.sql.SQLException
  {
    return new JDBCExternalConnection(dataSource.getConnection(), tableName,jdbcPrefMode,converter);  
  }
  
  /**
   * Get Wrapped DataSource Object
   * @return Wrapped Java DataSource
   */
  public javax.sql.DataSource getJavaDataSource()
  {
    return dataSource;
  }
  
  public String getExtTableName()
  {
    if(tableName != null)
      return tableName;
    return null;
  }

  public boolean isJdbcPrefMode() {
    return jdbcPrefMode;
  }

  public void setJdbcPrefMode(boolean jdbcPrefMode) {
    this.jdbcPrefMode = jdbcPrefMode;
  }
}
