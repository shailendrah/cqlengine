/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/QueryRequest.java /main/1 2011/05/18 04:38:12 udeshmuk Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/06/11 - Creation
 */

/**
 *  @version $Header: QueryRequest.java 06-apr-2011.23:54:28 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.datasource;

/*
 * This class encapsulates a request sent to the archiver.
 * 'sql' is the query to be executed with 0 or more bind parameters
 * 'params' are the bind parameters.
 */

public class QueryRequest
{
  private String sql;
  private Object[] params;

  public QueryRequest()
  {
  }

  public QueryRequest(String sql, Object[] params)
  {
    this.sql = sql;
    this.params = params;
  }

  public void setQuery(String sql)
  {
    this.sql = sql;
  }

  public void setParams(Object[] params)
  {
    this.params = params;
  }

  public String getQuery()
  {
    return this.sql;
  }

  public Object[] getParams()
  {
    return this.params;
  }
     
}
