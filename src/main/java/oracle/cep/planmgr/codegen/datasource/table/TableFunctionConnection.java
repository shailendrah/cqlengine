/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/datasource/table/TableFunctionConnection.java /main/4 2010/06/24 06:26:52 sbishnoi Exp $ */

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
    sbishnoi    03/31/10 - adding supportsPredicate
    sbishnoi    02/24/10 - modifying supportsPredicate to return false always
    sbishnoi    10/05/09 - Creation
 */

package oracle.cep.planmgr.codegen.datasource.table;

import java.util.List;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.extensibility.datasource.Predicate;
import oracle.cep.extensibility.datasource.ExternalFunctionMetadata;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/datasource/table/TableFunctionConnection.java /main/4 2010/06/24 06:26:52 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionConnection implements IExternalConnection
{
  /** table function expression */
  private IAEval prepStmtEval;
  
  /** table function return type */
  private Datatype returnCollectionDatatype;
  
  /** data type of each element of the table function result set*/
  private Datatype componentType;
  
  
  /**
   * Constructor
   * @param stmtEval
   * @param columnDatatype
   * @param componentType
   */
  public TableFunctionConnection(IAEval stmtEval, 
                                 Datatype returnCollectionType,
                                 Datatype componentType)
  {
    this.prepStmtEval = stmtEval;
    this.returnCollectionDatatype = returnCollectionType;
    this.componentType = componentType;
  }
  
  /**
   * Creates a IExternalPreparedStatement object for sending SQL statements to
   * the database.
   * @param relName name of relation
   * @param relAttrs list of attributes
   * @param pred Predicate Clause
   * @return a new IExternalPreparedStatement object
   */
  public IExternalPreparedStatement prepareStatement(String relName,  
                                                 List<String> relAttrs,
                                                    Predicate pred) 
    throws Exception
  {
    return new TableFunctionPreparedStatement(
      prepStmtEval,relName, relAttrs, returnCollectionDatatype, componentType);
  }
  
  /**
   * Releases all the resources acquired by Connection without any wait.
   */
  public void close() throws Exception
  {}

  @Override
  /**
   * Table Function connection doesn't support predicate evaluation
   */
  public List<ExternalFunctionMetadata> getCapabilities() throws Exception
  {    
    return null;
  }

  @Override
  public boolean supportsPredicate(Predicate pred) throws Exception
  {    
    return true;
  }

@Override
public void validateSchema(int numAttrs,String[] attrNames, AttributeMetadata[] attrMetadata) throws Exception {}

}
