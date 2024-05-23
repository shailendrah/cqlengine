/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/RelationStarInterp.java /main/3 2012/05/02 03:06:04 pkali Exp $ */

/* Copyright (c) 2010, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    04/01/11 - store name of attr
    sborah      07/27/10 - support for table specific star clause in select
    sborah      07/27/10 - Creation
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPRelationStarNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/RelationStarInterp.java /main/3 2012/05/02 03:06:04 pkali Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

public class RelationStarInterp extends NodeInterpreter
{
  
  /**
   * Interpret the CEPRelationStarNode and add all the attributes in the given 
   * Relation into the list of select expressions.
   */
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
  throws CEPException 
  {    
    int                    numAttrs;
    int                    varId;
    Expr                   attrExpr;
    Datatype               attrType;
    String                 varName;
    String                 tabName;
    String                 attrName;
    int                    attrId;
    SymbolTable            symbolTable;
    SymbolTableAttrEntry[] attrEntries;
    CEPRelationStarNode    relationStarNode;
    

    assert node instanceof CEPRelationStarNode;
    relationStarNode = (CEPRelationStarNode)node;

    // call the super class interpreter first.
    super.interpretNode(node, ctx);
    
    ExecContext ec = ctx.getExecContext();
    
    varName        = relationStarNode.getVarName();
    
    symbolTable    = ctx.getSymbolTable();
    
    // try to fetch the table from the symbol table
    try
    {
      varId  = symbolTable.lookupSource(varName).getVarId();
    }
    catch(CEPException e)
    {
      e.setStartOffset(relationStarNode.getStartOffset());
      e.setEndOffset(relationStarNode.getEndOffset());      
      throw e;
    }
    attrEntries        = symbolTable.getAllAttrs(ec, varId);
    
    assert attrEntries != null;
    
    numAttrs           = attrEntries.length;
    varName            = symbolTable.lookup(varId).getVarName();
    tabName            = symbolTable.getTableName(varId);
    boolean isExternal = symbolTable.lookupSource(varId).isExternal();
    
    // Iterate through the list of attributes of the table and add them 
    // to the list of relation star select list expressions.
    for (int j = 0; j < numAttrs; j++) 
    {
      attrName = attrEntries[j].getVarName();
      attrType = attrEntries[j].getAttrType();
      attrId   = attrEntries[j].getAttrId();
      attrExpr = new AttrExpr(new Attr(varId, attrId, 
                                 tabName+"."+attrName, attrType), attrType);
      attrExpr.setName(varName + "." + attrName, false, isExternal);
      ((AttrExpr)attrExpr).setActualName(tabName + "." + attrName);
      
      // add this entry to the list of relation star expressions
      ctx.addRelationStarExpr(attrExpr);
    }
    
    
  }
  
  
}
