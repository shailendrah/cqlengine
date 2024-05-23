/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTableFunctionRelationNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sbishnoi    07/13/10 - XbranchMerge sbishnoi_bug-9860418_ps3_11.1.1.4.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    07/12/10 - XbranchMerge sbishnoi_bug-9860418_ps3 from main
    sbishnoi    07/07/10 - fix bug 9860418
    sbishnoi    09/11/09 - Creation
 */

package oracle.cep.parser;

import oracle.cep.common.Datatype;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTableFunctionRelationNode.java /main/3 2010/07/13 03:55:08 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */


public class CEPTableFunctionRelationNode extends CEPRelationNode
{
  /** Name of the Table column */
  private String      columnAlias;
  
  /** Name of the table relation */
  private String      tableAlias;
  
  /** DataType of the returned collection's components */
  private Datatype    componentDatatype;
  
  /** Table function expression */
  private CEPExprNode tableFunctionExprNode;
  
  /**
   * Constructor
   * @param tableFunctionExprNode table function expression node
   * @param columnAliasNode column name
   * @param tableAliasNode relation name
   */
  public CEPTableFunctionRelationNode(CEPExprNode tableFunctionExprNode,
                                      CEPStringTokenNode columnAliasNode,
                                      CEPStringTokenNode tableAliasNode)
  {
    // By Default, column dataType is OBJECT
    this(tableFunctionExprNode, columnAliasNode, null, tableAliasNode);
  }
  
  /**
   * Constructor
   * @param tableFunctionExprNode table function expression node
   * @param columnAliasNode column name
   * @param componentDatatype component data type
   * @param tableAliasNode relation name
   */
  public CEPTableFunctionRelationNode(CEPExprNode tableFunctionExprNode,
                                      CEPStringTokenNode columnAliasNode,
                                      Datatype           componentDatatype,
                                      CEPStringTokenNode tableAliasNode)
  {     
    this.tableFunctionExprNode = tableFunctionExprNode;
    this.columnAlias           = columnAliasNode.getValue();
    this.componentDatatype     = componentDatatype; 
    this.tableAlias            = tableAliasNode.getValue();
        
    setName(tableAlias);
    setAlias(tableAlias);
    setStartOffset(tableFunctionExprNode.getStartOffset());
    setEndOffset(tableAliasNode.getEndOffset());
  }
    
  /**
   * @return the columnAlias
   */
  public String getColumnAlias()
  {
    return columnAlias;
  }

  /**
   * @return the tableAlias
   */
  public String getTableAlias()
  {
    return tableAlias;
  }
 
  // TODO: Fix this method
  public int toQCXML(StringBuffer queryXml, int operatorID)
  throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Not Supported For TABLE function");
  }

  /**
   * @return the tableFunctionExprNode
   */
  public CEPExprNode getTableFunctionExprNode()
  {
    return tableFunctionExprNode;
  }

  /**
   * @return the componentDatatype
   */
  public Datatype getComponentDatatype()
  {
    return componentDatatype;
  }

}
