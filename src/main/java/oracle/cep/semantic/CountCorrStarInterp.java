/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CountCorrStarInterp.java /main/7 2012/05/02 03:06:02 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
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
    udeshmuk    04/05/11 - propagate attrname
    parujain    03/12/09 - make interpreters stateless
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/08/08 - support offset
    parujain    08/27/08 - semantic offset
    rkomurav    01/28/08 - remove unused vars
    udeshmuk    09/27/07 - Change the error raised.
    udeshmuk    09/21/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CountCorrStarInterp.java /main/7 2012/05/02 03:06:02 pkali Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPAggrExprNode;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;


/**
 * The interpreter that is specific to the CEPCountCorrStarNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 
 */

public class CountCorrStarInterp extends AggrExprInterp {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
    throws CEPException {

    SymbolTableAttrEntry[] attrEntries = null; 
    SymbolTableAttrEntry   attrEntry;
    Attr                   attr;
    CEPAttrNode            attrNode;
    String                 varName;
    SymbolTableCorrEntry   corrEntry;
    int                    tableId;
    CEPExprNode            exprNode;
    
    super.interpretNode(node, ctx);
 
    exprNode = ((CEPAggrExprNode)node).getExprNode();
    AggrFunction   aggrFn = ((CEPAggrExprNode)node).getAggrFunction();
    
    assert exprNode instanceof CEPAttrNode;
    
    attrNode = (CEPAttrNode)exprNode;
    varName  = attrNode.getVarName();
    
    if(!ctx.isCountCorrStarAllowed())
    {
      throw new SemanticException(SemanticError.CORRELATION_ATTR_NOT_ALLOWED_HERE,
    		  exprNode.getStartOffset(), exprNode.getEndOffset(),
    		  new Object[]{varName});
    }
       
    assert attrNode.getAttrName() == null;
    try {
    corrEntry   = ctx.getSymbolTable().lookupCorr(varName);
    tableId     = corrEntry.getTableVarId();
    attrEntries = ctx.getSymbolTable().getAllAttrs(ctx.getExecContext(), tableId);
    }catch(CEPException e)
    {
      e.setStartOffset(attrNode.getStartOffset());
      e.setEndOffset(attrNode.getEndOffset());
      throw e;
    }
    attrEntry   = attrEntries[0];
    Datatype paramType   = attrEntry.getAttrType();
    attr        = new SemCorrAttr(corrEntry.varId, attrEntry.getAttrId(), 
                                  tableId, varName+".*", paramType);
    Expr expr   = new AttrExpr(attr, paramType);
    String argName  = varName + ".*";
        
    buildAggrExpr(ctx, expr, paramType, argName, aggrFn);
  }
}
