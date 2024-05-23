/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/datasource/table/TableFunctionDataSource.java /main/2 2010/01/25 00:32:43 sbishnoi Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/05/09 - Creation
 */

package oracle.cep.planmgr.codegen.datasource.table;

import oracle.cep.common.Datatype;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.extensibility.datasource.IExternalDataSource;
import oracle.cep.extensibility.datasource.IExternalConnection;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/datasource/table/TableFunctionDataSource.java /main/2 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionDataSource implements IExternalDataSource
{
  /** table function return type */
  Datatype returnCollectionType;
  
  /** data type of each element of the table function result set*/
  Datatype componentType;
  
  /** table function evaluator */
  IAEval tableFunctionPrepStatementEval;
  
  /**
   * Constructor
   * @param stmtEval
   * @param columnType
   * @param componentType
   */
  public TableFunctionDataSource(IAEval stmtEval, 
                                 Datatype returnCollectionType,
                                 Datatype componentType)
  {
    this.returnCollectionType = returnCollectionType;
    this.tableFunctionPrepStatementEval = stmtEval;
    this.componentType = componentType;
  }
  
  /**
   * Attempts to get a connection to the data source
   * @return a connection to the data source
   */  
  public IExternalConnection getConnection() throws Exception
  {
    return new TableFunctionConnection(tableFunctionPrepStatementEval, 
                                       this.returnCollectionType,
                                       this.componentType);
  }
  
}
