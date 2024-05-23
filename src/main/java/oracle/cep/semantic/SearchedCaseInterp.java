/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/SearchedCaseInterp.java /main/4 2008/09/17 15:19:46 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    08/26/08 - semantic exception offset
    udeshmuk    02/05/08 - parameterize errors.
    parujain    11/09/07 - external source
    parujain    10/26/07 - is expr ondemand
    parujain    03/28/07 - Searched Case Expression interpreter
    parujain    03/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/SearchedCaseInterp.java /main/4 2008/09/17 15:19:46 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSearchedCaseExprNode;
import oracle.cep.parser.CEPCaseConditionExprNode;

public class SearchedCaseInterp extends NodeInterpreter {
  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    SearchedCaseExpr searchExpr;
    String             exprName;
    // This flag keeps track of whether all results are null or not
    // In Case not all results and else can be null
    boolean            isResultNull = true;
    // Currently all the return types of the result should be same
    Datatype   returnType = null;
    boolean            isExternal = false;
    
    assert node instanceof CEPSearchedCaseExprNode;
    CEPSearchedCaseExprNode searchNode = (CEPSearchedCaseExprNode)node;
    
    super.interpretNode(node, ctx);
    
    // handling of else clause
    if(!searchNode.isElse())
      searchExpr = new SearchedCaseExpr();
    else
    {
      NodeInterpreter interp = InterpreterFactory.getInterpreter(searchNode.getElseExpr());
      interp.interpretNode(searchNode.getElseExpr(), ctx);
      isExternal =  isExternal || ctx.getExpr().isExternal;
      searchExpr = new SearchedCaseExpr(ctx.getExpr());
      isResultNull = false;
      returnType = searchExpr.getReturnType();
    }
    
    CEPCaseConditionExprNode[] condNodes = searchNode.getCaseExprs();
    exprName = new String("(" + "Case");
    for(int i=0; i<condNodes.length; i++)
    {
      NodeInterpreter interp = InterpreterFactory.getInterpreter(condNodes[i]);
      interp.interpretNode(condNodes[i], ctx);
      assert ctx.getExpr() instanceof CaseConditionExpr;
      CaseConditionExpr cond = (CaseConditionExpr)ctx.getExpr();
      isExternal =  isExternal || ctx.getExpr().isExternal;
      searchExpr.addCaseCondition(cond);
      if(cond.resultExpr != null)
      {
        exprName = exprName + "(" + "WHEN" + cond.conditionExpr.getName() + "THEN" + cond.resultExpr.getName() + ")" ;
        isResultNull = false;
        if((returnType != null) && (!returnType.equals(cond.getReturnType())))
          throw new SemanticException(SemanticError.RETURN_TYPE_MISMATCH_IN_CASE,
                       condNodes[i].getStartOffset(), condNodes[i].getEndOffset(),
                                 new Object[]{returnType, cond.getReturnType()});
        
        returnType = cond.getReturnType();
      }
      else
        exprName=  exprName + "(" + "WHEN" + cond.conditionExpr.getName() + "THEN null" + ")" ;
    }
    
    if(searchNode.isElse())
      exprName= exprName + "(" + "ELSE" + searchExpr.elseExpr.getName() + ")" ;
    
    //End of Case statement
    exprName = exprName + ")";
    
    if(isResultNull)
      throw new SemanticException(SemanticError.NOT_ALL_RESULTS_CAN_BE_NULL_IN_CASE,
    	  searchNode.getStartOffset(), searchNode.getEndOffset());
    
    searchExpr.setName(exprName, false, isExternal);
    searchExpr.setReturnType(returnType);
    ctx.setExpr(searchExpr);
  }
  
}
