/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/TableFunctionInfo.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $ */

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
    sbishnoi    12/28/09 - Creation
 */

package oracle.cep.phyplan;

import oracle.cep.common.Datatype;
import oracle.cep.phyplan.expr.Expr;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/TableFunctionInfo.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionInfo
{
  /** relation name */ 
  private String tableAlias;
  
  /** column name */
  private String columnAlias;
  
  /** column data type*/
  private Datatype returnCollectionType;
  
  /** component data type */
  private Datatype componentType;
  
  /** table function expression */
  private Expr   tableFunctionExpr;
  
  /**
   * @param tableAlias
   * @param columnAlias
   * @param returnCollectionType
   * @param componentType
   * @param tableFunctionExpr
   */
  public TableFunctionInfo(String tableAlias, String columnAlias,
      Datatype returnCollectionType, Datatype componentType, 
      Expr tableFunctionExpr)
  {   
    this.tableAlias = tableAlias;
    this.columnAlias = columnAlias;
    this.returnCollectionType = returnCollectionType;
    this.componentType = componentType;
    this.tableFunctionExpr = tableFunctionExpr;
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
   * @return the tableFunctionExpr
   */
  public Expr getTableFunctionExpr()
  {
    return tableFunctionExpr;
  }
  
  /**
   * Get the String representation of TableFunction
   */
  public String toString()
  {
    return
      this.tableAlias + "(" + this.columnAlias+ " " + this.returnCollectionType
         + ")";    
  }

  /**
   * @return the componentType
   */
  public Datatype getComponentType()
  {
    return componentType;
  }

  /**
   * @return the returnCollectionType
   */
  public Datatype getReturnCollectionType()
  {
    return returnCollectionType;
  }
  
}
