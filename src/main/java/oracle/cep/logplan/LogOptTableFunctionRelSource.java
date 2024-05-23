/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptTableFunctionRelSource.java /main/5 2012/05/02 03:05:57 pkali Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    07/07/11 - XbranchMerge anasrini_bug-12640265_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    07/06/11 - setNumInputs(0) and setKind
    sbishnoi    07/29/10 - XbranchMerge
                           sbishnoi_bug-9947670_ps3_main_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
    sbishnoi    07/28/10 - adding validation for Tablefunction relation source
                           operator
    sborah      01/28/10 - support for multiple external tables in join
    sbishnoi    12/14/09 - Creation
 */

package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.Expr;


/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptTableFunctionRelSource.java /main/5 2012/05/02 03:05:57 pkali Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in) *  
 */

public class LogOptTableFunctionRelSource extends LogOpt
{  
  /** relation name */ 
  private String tableAlias;
  
  /** column name */
  private String columnAlias;
  
  /** column data type*/
  private Datatype returnCollectionType;
  
  /** component data type*/
  private Datatype componentType;
  
  /** Table Function Expression*/
  private Expr tableFunctionExpr;
  
  /** number of output attributes */
  private int numOutAttrs;
 
  /**
   * Constructor
   * @param tableAlias alias for table
   * @param columnAlias alias for column
   * @param columnType data type of column
   */
  public LogOptTableFunctionRelSource(String tableAlias,
                                      String columnAlias,
                                      Datatype returnCollectionType,
                                      Datatype componentType,
                                      int attrId,
                                      int varId,
                                      int numAttrs)
  {
    super(LogOptKind.LO_TABLE_FUNCTION);

    this.tableAlias    = tableAlias;
    this.columnAlias   = columnAlias;
    this.componentType = componentType;
    this.numOutAttrs   = numAttrs;
    this.returnCollectionType    = returnCollectionType;

    // Set the number of inputs to 0
    this.setNumInputs(0);

    setExternal(true);
    
    // Number of attributes in output 
    super.setNumOutAttrs(numOutAttrs);
    
    ArrayList<Attr> attr = getOutAttrs();
    
    AttrNamed outAttr = new AttrNamed(this.returnCollectionType);
    outAttr.setAttrId(attrId);
    outAttr.setVarId(varId);
    attr.set(0, outAttr);
  }
  
  @Override
  public void setExternal(boolean isExternal) 
  {
    // set the pull operator flag according to the isExternal flag
    this.setPullOperator(isExternal);
    
    // now call the super class method
    super.setExternal(isExternal);
  }

  
  
  /**
   * @return the tableFunctionExpr
   */
  public Expr getTableFunctionExpr()
  {
    return tableFunctionExpr;
  }

  /**
   * @param tableFunctionExpr the tableFunctionExpr to set
   */
  public void setTableFunctionExpr(Expr tableFunctionExpr)
  {
    this.tableFunctionExpr = tableFunctionExpr;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<TableFunctionRelationSourceLogicalOperator>");
    sb.append("Dummy Operator");
    sb.append("</TableFunctionRelationSourceLogicalOperator>");

    return sb.toString();
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

  @Override
  public void validate() throws LogicalPlanException{
    // Assert that table function is always an external relation   
    assert this.isExternal() : "Table function relation " + getTableAlias() + 
      " should be an external relation.";

    //Note: Check that Table function must be used with a join operation
    LogOpt output = this.getOutput();

    // This is possible for query like select * from external relation
    if(output == null)
    {
      throw new LogicalPlanException(
        LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, getTableAlias());
    }
    else if(output.getOperatorKind().compareTo(LogOptKind.LO_CROSS)!=0
       && output.getOperatorKind().compareTo(LogOptKind.LO_STREAM_CROSS)!=0)
    {
      throw new LogicalPlanException(
        LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, getTableAlias());
    }
  }

}
