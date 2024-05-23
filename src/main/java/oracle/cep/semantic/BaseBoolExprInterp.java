/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/BaseBoolExprInterp.java /main/9 2009/06/04 17:45:06 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/28/09 - throw an exception if oracle outer join and ansi
                           outer join in same query
    parujain    09/05/08 - support offset
    udeshmuk    02/18/08 - handle nulls.
    parujain    10/25/07 - is expr ondemand
    sbishnoi    04/26/07 - support for having clause
    rkomurav    11/25/06 - cleanup
    rkomurav    11/08/06 - add outer join support
    dlenkov     11/09/06 - overloads
    parujain    11/03/06 - Tree representation for conditions
    parujain    10/31/06 - Comparison boolean interpreter
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/BaseBoolExprInterp.java /main/9 2009/06/04 17:45:06 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPBaseBooleanExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.common.CompOp;
import oracle.cep.common.OuterJoinType;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.BooleanOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;


/**
 * The interpreter that is specific to the CEPBaseBooleanExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class BaseBoolExprInterp extends NodeInterpreter {
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
      throws CEPException {

    boolean isExternal = false;

    assert node instanceof CEPBaseBooleanExprNode;
    CEPBaseBooleanExprNode bexprNode = (CEPBaseBooleanExprNode)node;

    super.interpretNode(node, ctx);

    OuterJoinType outerJoinType = bexprNode.getOuterJoinType();

    BooleanOp op;
    if (bexprNode.getCompOp() == null)
      op = bexprNode.getUnaryOp();
    else
      op = bexprNode.getCompOp();

    String name = null;
    CEPExprNode[] tnodes = null;
    if (op instanceof UnaryOp) {
      tnodes = new CEPExprNode[1];
      tnodes[0] = bexprNode.getUnaryOperand();
      name = ((UnaryOp)op).getFuncName();
    }
    else if (op instanceof CompOp) {
      tnodes = new CEPExprNode[2];
      tnodes[0] = bexprNode.getLeftOperand();
      tnodes[1] = bexprNode.getRightOperand();
      name = ((CompOp)op).getFuncName();
    }
    else
      // should not be here
      assert false;

    ValidFunc vfn = TypeCheckHelper.getTypeCheckHelper().
                    validateExpr( name, tnodes, ctx, ctx.isAggrAllowed());
    
    Expr[] params = vfn.getExprs();
    if (outerJoinType != null)
    { /* As per database behavior
       * - where clause with 'test.c1=null(+)' / 'null=null(+)' predicate gives 
       *   error ORA-00933 (sql command not properly ended).
       * - where clause with 'null(+)=test.c1' / 'null(+)=null' predicate gives 
       *   error ORA-00920 (invalid relational operator).
       * All other combinations involving nulls don't throw any error
       * and no rows are selected as result. */
      if ((outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER)&&(params[1].bNull)) 
        throw new SemanticException(SemanticError.CQL_COMMAND_NOT_ENDED_PROPERLY,
        		bexprNode.getStartOffset(), bexprNode.getEndOffset());
      if ((outerJoinType == OuterJoinType.RIGHT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER)&&(params[0].bNull)) 
        throw new SemanticException(SemanticError.INVALID_RELATIONAL_OPERATOR,
        		bexprNode.getStartOffset(), bexprNode.getEndOffset());
      if(ctx.isANSIOuterJoinInFromClause())
          throw new SemanticException(SemanticError.INVALID_OUTER_JOIN_USAGE,
          bexprNode.getStartOffset(), bexprNode.getEndOffset());
        
    }
    String exprName = null;
    BaseBExpr bexpr = null;
    if(op instanceof UnaryOp) {
      bexpr = new BaseBExpr( (UnaryOp)op, params[0]);
      exprName = new String( ((UnaryOp)op).getSymbol() +
                             "(" + params[0].getName() + ")");
      bexpr.setName( exprName, false);
      isExternal = params[0].isExternal;
      bexpr.setIsExternal(isExternal);
      ctx.setExpr( bexpr);
    }
    else if (op instanceof CompOp) {
      if(outerJoinType != null)
        bexpr = new BaseBExpr((CompOp)op, params[0], params[1], outerJoinType);
      else
        bexpr = new BaseBExpr( (CompOp)op, params[0], params[1]);
    
      exprName = new String("(" + params[0].getName());
      if(outerJoinType == OuterJoinType.RIGHT_OUTER ||outerJoinType == OuterJoinType.FULL_OUTER)
        exprName = exprName + "(+)";
      exprName = exprName + ")" +
                            ((CompOp)op).getSymbol() +
                            "(" + params[1].getName();
      if(outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER)
        exprName = exprName + "(+)";
      exprName = exprName + ")";
      
      bexpr.setName( exprName, false);
      isExternal = params[0].isExternal || params[1].isExternal ;
      bexpr.setIsExternal(isExternal);
      ctx.setExpr( bexpr);
    }
  }
}
