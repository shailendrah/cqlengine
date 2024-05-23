/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/ComplexBoolExprInterp.java /main/8 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/08/08 - support offset
    udeshmuk    12/11/07 - add semantic check for outer join.
    parujain    11/09/07 - external source
    parujain    10/25/07 - is expr ondemand
    sbishnoi    05/03/07 - support for having
    rkomurav    11/26/06 - fix aggrallowed flag
    parujain    11/16/06 - NOT operator
    dlenkov     11/09/06 - overloads
    parujain    11/03/06 - Tree representation for conditions
    parujain    10/31/06 - Logical boolean interpreter
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/ComplexBoolExprInterp.java /main/8 2008/09/17 15:19:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPBaseBooleanExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPComplexBooleanExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.common.LogicalOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;


/**
 * The interpreter that is specific to the CEPComplexBooleanExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ComplexBoolExprInterp extends NodeInterpreter {

  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
      throws CEPException {

  CEPExprNode        rightOperand;
  ValidFunc          vfn;
  String             exprName;
  ComplexBExpr       cexpr;
  boolean            isExternal =false;
    
    assert node instanceof CEPComplexBooleanExprNode;
    CEPComplexBooleanExprNode bexprNode = (CEPComplexBooleanExprNode)node;

    super.interpretNode(node, ctx);

    LogicalOp op = bexprNode.getLogicalOp();
    String name = op.getFuncName();

    //In case of NOT operator Right Operand will be returned null.
    rightOperand = bexprNode.getRightOperand();

    if(rightOperand != null)
    {
      CEPExprNode[] tnodes = new CEPExprNode[2];
      tnodes[0] = bexprNode.getLeftOperand();
      tnodes[1] = bexprNode.getRightOperand();
      if (tnodes[0] instanceof CEPBaseBooleanExprNode)
      {
        if ((((CEPBaseBooleanExprNode) tnodes[0]).getOuterJoinType())!=null)
          throw new SemanticException(SemanticError.TOO_MANY_PREDICATES_IN_WHERE_ERROR,
                             tnodes[0].getStartOffset(), tnodes[0].getEndOffset());
      }
      if (tnodes[1] instanceof CEPBaseBooleanExprNode)
      {
        if ((((CEPBaseBooleanExprNode) tnodes[1]).getOuterJoinType())!=null)
          throw new SemanticException(SemanticError.TOO_MANY_PREDICATES_IN_WHERE_ERROR,
                             tnodes[1].getStartOffset(), tnodes[1].getEndOffset());
      }
      vfn = TypeCheckHelper.getTypeCheckHelper().
                            validateExpr( name, tnodes, ctx, ctx.isAggrAllowed());

    }
    else
    {
      CEPExprNode[] tnodes = new CEPExprNode[1];
      tnodes[0] = bexprNode.getLeftOperand();
      vfn = TypeCheckHelper.getTypeCheckHelper().
                            validateExpr( name, tnodes, ctx, ctx.isAggrAllowed());

    }


    Expr[] params = vfn.getExprs();

    if (params.length == 1)
    {
      exprName = new String("(" + op.getSymbol() + params[0].getName() + ")");
      cexpr = new ComplexBExpr(op, params[0]);
      isExternal = params[0].isExternal;
      
    }
    else 
    {
      assert params[1] != null;
      exprName = new String("(" + params[0].getName() + ")" + op.getSymbol() + 
                          "(" + params[1].getName() + ")");
      cexpr = new ComplexBExpr(op, params[0], params[1]);
      isExternal = params[0].isExternal || params[1].isExternal ;
    }
    
    cexpr.setIsExternal(isExternal);
    cexpr.setName(exprName, false);
    ctx.setExpr(cexpr);
  }
}
