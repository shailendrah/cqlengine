/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TableFunctionRelationSpec.java /main/2 2010/01/25 00:32:43 sbishnoi Exp $ */

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
    sbishnoi    12/14/09 - added review comments
    sbishnoi    09/16/09 - Creation
 */

package oracle.cep.semantic;


import oracle.cep.common.Datatype;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TableFunctionRelationSpec.java /main/2 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionRelationSpec
{
  /** table function expression */
  private Expr tableFunctionExpr ;
  
  /** meta-data identifier for the inline view of table function relation */
  private int varId;
  
  /** meta-data identifier for the lone output attribute of table function*/
  private int attrId;
  
  /** table name */
  private String tableAlias;
  
  /** column name */
  private String columnAlias;
  
  /** column data type */
  private Datatype returnCollectionDatatype;
  
  /** component data type*/
  private Datatype componentDatatype;
  
  /**
   * Constructor
   * @param tableFunctionExpr
   * @param varId
   * @param attrId
   * @param symTable
   */
  public TableFunctionRelationSpec(Expr tableFunctionExpr,
                                   String tableAlias,
                                   String columnAlias,
                                   Datatype returnCollectionType,
                                   Datatype componentDatatype,
                                   int varId,
                                   int attrId)                                   
  {
    this.tableFunctionExpr = tableFunctionExpr;
    this.varId             = varId;
    this.attrId            = attrId;    
    this.tableAlias = tableAlias;
    this.columnAlias = columnAlias;
    this.returnCollectionDatatype = returnCollectionType;
    this.componentDatatype = componentDatatype;
  }

  /**
   * @return the tableFunctionExpr
   */
  public Expr getTableFunctionExpr()
  {
    return tableFunctionExpr;
  }

  /**
   * @return the varId
   */
  public int getVarId()
  {
    return varId;
  } 
  
  /** 
   * @return the tableId
   */
  public int getTableId()
  {
    return -1;
  }

  /**
   * @return the attrId
   */
  public int getAttrId()
  {
    return attrId;
  }

  /**
   * Get the Table function's alias
   * @return alias for table function
   */
  public String getRelationName()
  {
    return tableAlias;
  }
  
  /**
   * Get the number of attributes in Table function relation
   * @return ONE Always 
   */
  public int getNumAttrs()
  {
    return 1;
  }
  
  /**
   * Table Function is an external relation source
   * @return TRUE always
   */
  public boolean isExternal()
  {
    return true;
  }

  /**
   * @return the tableAlias
   */
  public String getTableAlias()
  {
    return tableAlias;
  }

  /**
   * @return the columnAlias
   */
  public String getColumnAlias()
  {
    return columnAlias;
  }

  /**
   * @return the componentDatatype
   */
  public Datatype getComponentDatatype()
  {
    return componentDatatype;
  }

  /**
   * @param componentDatatype the componentDatatype to set
   */
  public void setComponentDatatype(Datatype componentDatatype)
  {
    this.componentDatatype = componentDatatype;
  }

  /**
   * @return the returnCollectionDatatype
   */
  public Datatype getReturnCollectionDatatype()
  {
    return returnCollectionDatatype;
  }
  
}
