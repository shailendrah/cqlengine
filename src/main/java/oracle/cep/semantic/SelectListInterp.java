/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SelectListInterp.java /main/14 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPSelectListNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/11/12 - set alias for select list entries wherever available
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    04/01/11 - store name of attr
    sborah      07/27/10 - support for table specific star clause in select
    parujain    03/12/09 - make interpreters stateless
    sborah      02/02/09 - fix for bug 7693965
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/26/08 - semantic exception offset
    parujain    06/06/08 - invalid usage of xmltype
    udeshmuk    01/14/08 - handle null literal.
    parujain    11/09/07 - external source
    mthatte     10/31/07 - moving ondemand to srcmgr from tblmgr
    parujain    10/25/07 - if using ondemand
    anasrini    05/25/07 - inline view support
    anasrini    05/23/07 - symbol table reorg
    anasrini    08/30/06 - project expression alias support
    anasrini    07/10/06 - support for user defined aggregations 
    najain      04/16/06 - support views
    najain      04/06/06 - cleanup
    anasrini    02/26/06 - use getTableId to get tableId given varId 
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SelectListInterp.java /main/14 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSelectListNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;


/**
 * The interpreter that is specific to the CEPSelectListNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SelectListInterp extends NodeInterpreter {


  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPSelectListNode selNode;
    CEPExprNode[]     selExprs;
    CEPExprNode       selExprNode;
    boolean           isStar;
    boolean           isDistinct;
    int               numExprs;
    NodeInterpreter   exprInterp;
    Expr              expr;

    assert node instanceof CEPSelectListNode;
    selNode = (CEPSelectListNode)node;

    super.interpretNode(node, ctx);

    isStar = selNode.isStar();
    isDistinct = selNode.isDistinct();

    if (isStar) {
      try {
        interpretStarInSelect(selNode, ctx);
      }catch(CEPException e)
      {
        e.setStartOffset(selNode.getStartOffset());
        e.setEndOffset(selNode.getEndOffset());
        throw e;
      }
    }
    else {
      // The select clause does not have a *
      selExprs = selNode.getSelectListExprs();
      numExprs = selExprs.length;

      for (int i=0; i<numExprs; i++) {
        // Aggregation Function is permitted here
        ctx.setIsAggrAllowed(true);
        
        selExprNode = selExprs[i];
        exprInterp  = InterpreterFactory.getInterpreter(selExprNode);
        exprInterp.interpretNode(selExprNode, ctx);
        
        // Relation Star case would not produce a single expression for the node 
        // but would add one or more expressions added into the list of 
        // relation star select expressions.
        if(exprInterp instanceof RelationStarInterp)
        {
          Expr[] relationStarList = ctx.getRelationStarList();
          for(Expr relationStarExpr : relationStarList)
          {
            // validate the select expression
            relationStarExpr = 
              validateSelectExpr(selExprNode, relationStarExpr, isDistinct);
            
            ctx.addSelectListExpr(relationStarExpr);
          }
        }
        else
        {
          expr = ctx.getExpr();
          
          // validate the select expression
          expr = validateSelectExpr(selExprNode, expr, isDistinct);
          
          if (selExprNode.getAlias() != null)
          {
            expr.setName(selExprNode.getAlias(), true);
            //set the alias field in expr
            //it will be set only when user gives alias to an expr in CQL query
            expr.setAlias(selExprNode.getAlias());
          }
          
          ctx.addSelectListExpr(expr);
        }
      }
    }
    ctx.setIsDistinct(isDistinct);
  }

  /**
   * Validate the given expression
   * @param selExprNode The select expression node that contains the expression
   * @param expr        The expression to be validated
   * @param isDistinct  whether the given select node has a Distinct clause
   * @return the expression object
   * @throws CEPException
   */
  private Expr validateSelectExpr(CEPParseTreeNode selExprNode , Expr expr,
                                  boolean isDistinct)
  throws CEPException
  {
    //handle invalid usage of xmltype
    if((expr.getReturnType() == Datatype.XMLTYPE) 
        && (isDistinct))
      throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE, 
          selExprNode.getStartOffset(),
          selExprNode.getEndOffset(), 
          new Object[]{expr.getName()});
    // handle null in the select list
    if (expr.getReturnType() == Datatype.UNKNOWN)
    {
      expr = new ConstIntExpr(0);
      expr.setbNull(true);
    }
    
    return expr;
  }
  
  /**
   * converts the "*" in SELECT * to the correct list of project
   * attributes. This list is the just the concatenation of all the
   * attributes of all the tables / inline views occurring in the FROM clause.
   */
  void interpretStarInSelect(CEPSelectListNode selNode, SemContext ctx) 
  throws CEPException {
    
    SFWQuery               sfwQuery  = (SFWQuery)ctx.getSemQuery();
    int[]                  varIds    = sfwQuery.getFromClauseTables();
    int                    numAttrs;
    int                    varId;
    Expr                   attrExpr;
    Datatype               attrType;
    int                    numRels   = varIds.length;
    String                 varName;
    String                 tabName;
    String                 attrName;
    int                    attrId;
    SymbolTable            symbolTable;
    SymbolTableAttrEntry[] attrEntries;

    ExecContext ec = ctx.getExecContext();
    symbolTable    = ctx.getSymbolTable();
    
    for (int i = 0; i < numRels; i++)
    {
      varId              = varIds[i];
      attrEntries        = symbolTable.getAllAttrs(ec, varId);
      
      assert attrEntries != null;
      
      numAttrs           = attrEntries.length;
      varName            = symbolTable.lookup(varId).getVarName();
      tabName            = symbolTable.getTableName(varId);
      boolean isExternal = symbolTable.lookupSource(varId).isExternal();
        
      for (int j = 0; j < numAttrs; j++) 
      {
        attrName = attrEntries[j].getVarName();
        attrType = attrEntries[j].getAttrType();
        attrId   = attrEntries[j].getAttrId();
        attrExpr = new AttrExpr(new Attr(varId, attrId, 
                                  varName+"."+attrName, attrType), attrType);
        attrExpr.setName(varName + "." + attrName, false, isExternal);
        ((AttrExpr)attrExpr).setActualName(tabName + "." + attrName);
        
        // validate the select expression
        attrExpr = validateSelectExpr(selNode, attrExpr, selNode.isDistinct());
        
        ctx.addSelectListExpr(attrExpr);
      }
    }
  }
}
