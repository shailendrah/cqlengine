/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ArithExprInterp.java /main/7 2013/01/10 21:48:12 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPArithExprNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/04/13 - validate arithmetic expression for BI mode
    parujain    09/05/08 - 
    udeshmuk    02/15/08 - support for all nulls in function arguments
    parujain    11/06/07 - isOnDemand
    rkomurav    11/29/06 - fix aggrallowed
    dlenkov     11/02/06 - overloaded operators (functions)
    parujain    10/12/06 - built-in operator functions
    rkomurav    09/18/06 - bug 5446939
    anasrini    08/30/06 - set expr name
    anasrini    07/10/06 - support for user defined aggregations 
    najain      04/06/06 - cleanup
    anasrini    02/22/06 - Creation
    anasrini    02/22/06 - Creation
    anasrini    02/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ArithExprInterp.java /main/7 2013/01/10 21:48:12 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPArithExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.common.ArithOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.SQLType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPArithExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ArithExprInterp extends NodeInterpreter {

  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    assert node instanceof CEPArithExprNode;
    CEPArithExprNode aexprNode = (CEPArithExprNode)node;

    super.interpretNode( node, ctx);

    boolean isExternal =false;
    ArithOp op = aexprNode.getArithOp();
    String name = op.getFuncName();
    CEPExprNode[] tnodes = new CEPExprNode[2];
    tnodes[0] = aexprNode.getLeftOperand();
    tnodes[1] = aexprNode.getRightOperand();
  
    ValidFunc vfn = TypeCheckHelper.getTypeCheckHelper().
                                      validateExpr( name, tnodes, ctx, true);

    Expr[] params = vfn.getExprs();
    Expr expr = null;
    String fullName = null;
    
    // Validate Function for BI mode
    validate(params[0], params[1], op, ctx);
    
    boolean allParamsNull = params[0].bNull && params[1].bNull;
    if (allParamsNull && (vfn.getIsResultNull()))
    { //all inputs are null and function evaluates to null as per the
      //StaticMetadata.. so replace expression by a constant expression 
      //of allNullReturnType with bNull set to true
      expr = Expr.getExpectedExpr(vfn.getFn().getReturnType());
      fullName = new String("null");
      isExternal = false;
    }
    else
    {
      expr = new ComplexExpr( op, params[0], params[1],
                                         vfn.getFn().getReturnType());
      fullName = new String("(" + params[0].getName() + ")" + 
                                 op.getSymbol() + "(" +
                                 params[1].getName() + ")");
      isExternal = params[0].isExternal || params[1].isExternal ;
    }
    expr.setName(fullName, false);
    expr.setIsExternal(isExternal);
    ctx.setExpr(expr);
  }
  
  /**
   * A Method to perform following validation checks:
   * 1) Check if query is performing ADD/SUB on interval datatype attributes
   * @param left
   * @param right
   * @param op
   * @param ctx
   * @throws CEPException
   */
  private void validate(Expr left, Expr right, ArithOp op, SemContext ctx) 
    throws CEPException
  {
    SQLType targetSQLType = 
      ctx.getExecContext().getServiceManager().getConfigMgr().getTargetSQLType();
    // Semantic Check:
    // Addition and Subtraction of two interval types are not allowed.
    if(targetSQLType == SQLType.BI)
    {
      Datatype leftType = left.getReturnType();
      Datatype rightType = right.getReturnType();
      if(op == ArithOp.ADD || op == ArithOp.SUB)
      {
        if((leftType == Datatype.INTERVAL || leftType == Datatype.INTERVALYM) &&
           (rightType == Datatype.INTERVAL || rightType == Datatype.INTERVALYM))
          throw new CEPException(
            SemanticError.DATETIME_ARITHMETIC_OPERATION_NOT_SUPPORTED, 
            op.getFuncName());
      }
    }
  }
}
