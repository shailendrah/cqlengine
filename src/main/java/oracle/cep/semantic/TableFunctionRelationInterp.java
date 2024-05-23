/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TableFunctionRelationInterp.java /main/6 2010/07/13 03:55:08 sbishnoi Exp $ */

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
    sbishnoi    07/13/10 - XbranchMerge sbishnoi_bug-9860418_ps3_11.1.1.4.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    07/12/10 - XbranchMerge sbishnoi_bug-9860418_ps3 from main
    udeshmuk    01/29/10 - use IIterableType.getComponenttype to get
                           collection's component type
    sbishnoi    01/05/10 - table function cleanup
    sbishnoi    09/15/09 - Creation
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IIterableType;
import oracle.cep.extensibility.type.IType;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPObjExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPTableFunctionRelationNode;
import oracle.cep.semantic.TableFunctionRelationSpec;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TableFunctionRelationInterp.java /main/6 2010/07/13 03:55:08 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionRelationInterp extends NodeInterpreter
{
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
  throws CEPException 
  {
    assert node instanceof CEPTableFunctionRelationNode;
    CEPTableFunctionRelationNode tableFunctionRelNode =
      (CEPTableFunctionRelationNode)node;
    
    // Get the table function expression node
    CEPExprNode tableFunctionExprNode 
      = tableFunctionRelNode.getTableFunctionExprNode();
    
    // Table function expression should be a object expression
    assert tableFunctionExprNode instanceof CEPObjExprNode;
    
    // Get the factory to interpret table function expression node
    NodeInterpreter tableFunctionExprInterp 
      = InterpreterFactory.getInterpreter(tableFunctionExprNode);
    
    // Interpret the table function expression    
    tableFunctionExprInterp.interpretNode(tableFunctionExprNode, ctx);
    
    // SemCtx will set interpreted table function expression
    Expr tableFunctionSemExpr = ctx.getExpr();
    
    // expression shouldn't be null
    assert tableFunctionSemExpr != null;
    
    // Get the alias information
    String columnName      = tableFunctionRelNode.getColumnAlias();        
    String tableName       = tableFunctionRelNode.getTableAlias();
    Datatype returnCollectionType = tableFunctionSemExpr.getReturnType();
    
    // Check if the table function returns a IIterableType object
    Datatype componentType = 
      validateAndGetComponentType(tableFunctionSemExpr, 
             ctx, 
             tableFunctionRelNode.getComponentDatatype(),
             columnName);
    
    // Register the table alias
    int varId = addTableSource(tableFunctionRelNode, ctx);
    
    // Register the column name
    int attrId = addTableAttribute(tableFunctionRelNode, ctx, componentType);
      
    // Create a TableFunctionSpec
    TableFunctionRelationSpec tableFuncSpec 
      = new TableFunctionRelationSpec(tableFunctionSemExpr,
                                      tableName,
                                      columnName,
                                      returnCollectionType,
                                      componentType,
                                      varId,
                                      attrId);
    
    // set the table function specification
    ctx.setTableFunctionSpec(tableFuncSpec);
    
    // Set the relation spec object
    ctx.setRelationSpec(new RelationSpec(varId));
  }
  
  /**
   * Helper function to add a view source entry into metadata
   * @param tableFunctionRelNode
   * @param ctx
   * @return
   * @throws CEPException
   */
  private int addTableSource(CEPTableFunctionRelationNode tableFunctionRelNode,
                             SemContext ctx) throws CEPException
  {
    String tableAlias = tableFunctionRelNode.getTableAlias();
    int varId = 0;
    try
    {
      varId = ctx.getSymbolTable().addInlineSourceEntry
              (tableAlias,tableAlias, false, true);      
    }
    catch(CEPException ce)
    {
      ce.setStartOffset(tableFunctionRelNode.getStartOffset());
      ce.setEndOffset(tableFunctionRelNode.getEndOffset());
      throw ce;
    }
    return varId;
  }
  
  /** 
   * Helper function to add the table attributes in symbol table
   * @param tableFunctionRelNode
   * @param ctx
   * @return
   * @throws CEPException
   */
  private int addTableAttribute(CEPTableFunctionRelationNode 
    tableFunctionRelNode, SemContext ctx, Datatype componentType) 
  throws CEPException
  {
    int columnId = 0;
    String columnAlias = tableFunctionRelNode.getColumnAlias();
    String tableAlias  = tableFunctionRelNode.getTableAlias();
    try
    {
      if(componentType == null)
      {
        ctx.getSymbolTable().addAttrEntry(
            columnAlias, 
            tableAlias,
            columnId,
            Datatype.OBJECT,
            Datatype.OBJECT.getLength());
      }
      else
      {
        ctx.getSymbolTable().addAttrEntry(
            columnAlias, 
            tableAlias,
            columnId,
            componentType,
            componentType.getLength());
      }
      
    }
    catch(CEPException ce)
    {
      ce.setStartOffset(tableFunctionRelNode.getStartOffset());
      ce.setEndOffset(tableFunctionRelNode.getEndOffset());
      throw ce;
    }
    return columnId;
  }  
  
  /**
   * Validate the parameter expression of the table function
   * @param tableFunctionSemExpr
   * @param ctx
   * @throws CEPException
   */
  private Datatype validateAndGetComponentType(
    Expr tableFunctionSemExpr, 
    SemContext ctx, 
    Datatype userSpecifiedComponentType,
    String columnName) throws CEPException
  {
    // Note: Only Expressions allowed are of type 
    // oracle.cep.extensibility.type.IIterableType    
    
    // Get the expression type
    Datatype exprType = tableFunctionSemExpr.getReturnType();    
   
    boolean isIterableType =
      exprType instanceof oracle.cep.extensibility.type.IIterableType; 
    
    if(!isIterableType)
      throw new CEPException(SemanticError.INVALID_TABLE_CLAUSE_RETURN_TYPE,
                             tableFunctionSemExpr);
    
    IType componentType = null;
    
    // Check if user specified type is equivalent to component type of 
    // table function expressions's resultant collection's
    
    if(userSpecifiedComponentType != null)
    {
      componentType = ((IIterableType)exprType).getComponentType();
      if(componentType != null)
      {
        if(!userSpecifiedComponentType.isAssignableFrom(componentType))
           throw new CEPException(
           SemanticError.TABLE_CLAUSE_RETURN_TYPE_MISMATCH,
            columnName,
            componentType,
            userSpecifiedComponentType);
      }
      else
        componentType = userSpecifiedComponentType;
    }
    else
    {
      componentType = ((IIterableType)exprType).getComponentType();
    }
    
    //Note: componentType will be null only if user does not specify explicitly
    // and table function expression's return type is IIterable and 
    // IIterableType.getCompoentType() returns null;
    // e.g. Java's Iterable datatype
    if(componentType != null)
      assert componentType instanceof Datatype;
    return (Datatype)componentType;
  }
  
}
