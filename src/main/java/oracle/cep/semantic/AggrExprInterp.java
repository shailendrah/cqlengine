/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/AggrExprInterp.java /main/12 2009/03/19 20:24:41 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPAggrExprNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/12/09 - make interpreters stateless
    hopark      11/11/08 - use getFuncName instead of AggrFunc.name
    parujain    09/05/08 - 
    udeshmuk    04/15/08 - support for aggr distinct.
    udeshmuk    02/05/08 - parameterize error.
    parujain    11/09/07 - External source
    parujain    10/25/07 - is expr ondemand
    udeshmuk    09/20/07 - including semantic checks for built-in aggr.
                           functions.
    rkomurav    05/28/07 - restructure aggrfn
    anasrini    05/25/07 - inline view support
    anasrini    05/23/07 - symbol table reorg
    rkomurav    12/13/06 - introduction of COUNT_STAR
    rkomurav    11/25/06 - reset the previous isAggrAllowed
    rkomurav    09/19/06 - bug 5446939
    anasrini    08/30/06 - set expr name
    najain      05/16/06 - support views 
    najain      04/06/06 - cleanup
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/AggrExprInterp.java /main/12 2009/03/19 20:24:41 parujain Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPAggrExprNode;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.BuiltInAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;


/**
 * The interpreter that is specific to the CEPAggrExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class AggrExprInterp extends NodeInterpreter {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {
  
    assert node instanceof CEPAggrExprNode;
    CEPAggrExprNode aggrNode = (CEPAggrExprNode)node;

    super.interpretNode(node, ctx);

    if(!ctx.isAggrAllowed())
      throw new SemanticException(SemanticError.AGGR_FN_NOT_ALLOWED_HERE,
    		                 aggrNode.getStartOffset(), aggrNode.getEndOffset(),
                             new Object[]{aggrNode.getAggrFunction().getFuncName()});  
  }
  
  /**
   * Builds the aggrExpr and sets in the semantic context
   * @param ctx semantic context
   */
  protected void buildAggrExpr(SemContext ctx, Expr expr, Datatype paramType,
                               String argName, AggrFunction aggrFn)
  {
  
    String    exprName;
    AggrExpr  aggrExpr;
  
    Datatype dt = aggrFn.getReturnType(paramType);
    exprName = new String(aggrFn + "(" + argName + ")");
    aggrExpr = new AggrExpr(BuiltInAggrFn.get(aggrFn), dt, expr);
    boolean isExternal = expr.isExternal;
    aggrExpr.setName(exprName, false, isExternal);
    ctx.setExpr(aggrExpr);
  }

  protected void buildAggrExpr(SemContext ctx, Expr expr, boolean isDistinct,
                       Datatype paramType, String argName, AggrFunction aggrFn)
  {
  
    String    exprName;
    AggrExpr  aggrExpr;
  
    Datatype dt = aggrFn.getReturnType(paramType);
    exprName = new String(aggrFn + "(" + argName + ")");
    aggrExpr = new AggrExpr(BuiltInAggrFn.get(aggrFn), dt, expr);
    boolean isExternal = expr.isExternal;
    aggrExpr.setName(exprName, false, isExternal);
 // Set isDistinctAggrFlag if AggrExpr is of type AggrExpr(Distinct c1,..)
    aggrExpr.setIsDistinctAggr(isDistinct);
    ctx.setExpr(aggrExpr);
  }

}

