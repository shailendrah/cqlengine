/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExternalQryHelper.java /main/15 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/31/15 - set connRecContext
    sbishnoi    11/25/12 - XbranchMerge sbishnoi_bug-14626022_ps6 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    11/19/12 - adding cause in PREDICATE CLAUSE NOT SUPPORTED
    pkali       08/30/12 - XbranchMerge pkali_bug-14465875_ps6 from
                           st_pcbpel_11.1.1.4.0
    pkali       08/27/12 - fixed err msg arg
    sbishnoi    02/26/10 - reorg predicate evaluation
    sborah      12/18/09 - support for multiple external relations in join
    sbishnoi    09/30/09 - table function support
    parujain    02/06/09 - bug fix
    sbishnoi    01/14/09 - fix datasource
    sbishnoi    12/04/08 - support for generic data source
    hopark      10/09/08 - remove statics
    parujain    11/13/07 - ExternalQueryHelper
    parujain    11/13/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExternalQryHelper.java /main/15 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.ArithOp;
import oracle.cep.common.Constants;
import oracle.cep.common.CompOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.LogicalOp;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.extensibility.expr.BooleanExpression;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.ExternalInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.extensibility.datasource.ExternalFunctionMetadata;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.extensibility.datasource.Predicate;
import oracle.cep.extensibility.exceptions.ExternalException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.SimpleFunction;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRelnSrc;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.ComplexBoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprComplex;
import oracle.cep.service.ExecContext;

public class ExternalQryHelper {

  public static AInstr getPreparedInstr(ExecContext ec,
                                       LinkedList<BoolExpr> exprList, 
                                       IAEval stmtEval, 
                                       int[] inpRoles, 
                                       EvalContextInfo evalCtxInfo,
                                       Predicate externalPredicate,
                                       int externalRelationNumber)
    throws CEPException
  {
    AInstr instr = new AInstr();
    instr.op = AOp.PREP_STMT;    
    instr.extrInstr = new ExternalInstr();
    
    // Return null; If no predicate
    if(exprList.isEmpty())
    {
      LogUtil.fine(LoggerType.TRACE, "Either the query doesn't have any Predicate or" +
      		" External Connection doesn't support calculation of given predicate");
      return instr;
    }
  
    LinkedList<String> attrNameList = new LinkedList<String>();
    LinkedList<BooleanExpression> externalExprs = new LinkedList<BooleanExpression>();
    StringBuilder predicateClause = new StringBuilder();   
    Iterator<BoolExpr> iter = exprList.iterator();
    BoolExpr expr = iter.next();
    boolean flag = true;
    while(flag)
    {
      //If expression does not reference any external table then no need of creating
      // prepared statement
      if(expr.isExternal())
      {
       if(expr.getKind() == ExprKind.COMP_BOOL_EXPR)
       {
         processComplexBoolExpr(ec, expr, stmtEval, instr, inpRoles, 
                                evalCtxInfo, predicateClause, attrNameList,
                                externalRelationNumber);
       }
       else if(expr.getKind() == ExprKind.BASE_BOOL_EXPR)
       {
         processBaseBoolExpr(ec, expr, stmtEval, instr, inpRoles, 
                             evalCtxInfo, predicateClause, attrNameList,
                             externalRelationNumber);
        }
        externalExprs.add(expr);
      }
      // The conditions are linked to each other with an AND
      if(iter.hasNext())
      {
        expr = iter.next();
        if(expr.isExternal())
        {
          predicateClause = predicateClause.append(" and ");
        }
       
      }
      else
        flag = false;
      
    }
    // After finishing above function calls, predicateClause will
    // have complete predicate
    LinkedList<String> finalAttrNameList = new LinkedList<String>();
    processAttrList(attrNameList, finalAttrNameList);
    
    // sets the Predicate's isEqualityPredicate flag 
    externalPredicate.setIsEqualityPredicate(
        evalCtxInfo.getIsEqualityPredicate());
        
    // sets the Predicate's comparison attribute list
    externalPredicate.setAttrNameList(finalAttrNameList);
    externalPredicate.setConditions(externalExprs);
    
    // sets the Predicate' PredicateClause to string representation of predicate
    externalPredicate.setPredicateClause(predicateClause.toString());
    
    LogUtil.fine(LoggerType.TRACE, "PredicateClause created is [" + 
                 predicateClause.toString() + "]");
    LogUtil.fine(LoggerType.TRACE, "PredicateClause is EqualityPredicate ? [" + 
                 evalCtxInfo.getIsEqualityPredicate() + "]");
    stmtEval.addInstr(instr);
    return instr;
  }
  
  private static void processComplexBoolExpr(ExecContext ec,
                                             BoolExpr bexpr,
                                             IAEval stmtEval, 
                                             AInstr instr, 
                                             int[] inpRoles, 
                                             EvalContextInfo evalCtxInfo,
                                             StringBuilder predicateClause,
                                             LinkedList<String> attrNameList,
                                             int externalRelationNumber)
    throws CEPException
  {
    BoolExpr left;
    BoolExpr right;
    
    ComplexBoolExpr expr = (ComplexBoolExpr)bexpr;
    
    left = (BoolExpr)expr.getLeft();
    right = (BoolExpr)expr.getRight();
  
    assert left != null;
    LogicalOp oper = expr.getOper();
        
    if(right != null)
    {
      // Operators like AND, OR
      predicateClause.append(" (");
      if(left.getKind() == ExprKind.COMP_BOOL_EXPR)
        processComplexBoolExpr(ec, left, stmtEval, instr,inpRoles, 
                               evalCtxInfo, predicateClause, attrNameList,
                               externalRelationNumber);
      else if(left.getKind() == ExprKind.BASE_BOOL_EXPR)
        processBaseBoolExpr(ec, left, stmtEval,instr, inpRoles,
                            evalCtxInfo, predicateClause, attrNameList,
                            externalRelationNumber);
     
      predicateClause.append(" " + oper.getSymbol() + " ");
     
      if(right.getKind() == ExprKind.COMP_BOOL_EXPR)
        processComplexBoolExpr(ec, right, stmtEval,instr, inpRoles, 
                               evalCtxInfo,predicateClause, attrNameList,
                               externalRelationNumber);
      else if(right.getKind() == ExprKind.BASE_BOOL_EXPR)
        processBaseBoolExpr(ec, right, stmtEval, instr,inpRoles, 
                            evalCtxInfo,predicateClause, attrNameList,
                            externalRelationNumber);
     
      predicateClause.append(") ");      
    }
    else
    {
      //Assumption: External Connection should recognize the NOT keyword
      // as unary Not operation;
      // JDBCExternalConnection recognizes this keyword.
         
     // predicateClause.append(" " + oper.getSymbol() + " ( ");
      if(left.getKind() == ExprKind.COMP_BOOL_EXPR)
      {
        processComplexBoolExpr(ec, left, stmtEval,instr, inpRoles, 
            evalCtxInfo, predicateClause, attrNameList,externalRelationNumber);
      }
      else if(left.getKind() == ExprKind.BASE_BOOL_EXPR)
      {
        processBaseBoolExpr(ec, left, stmtEval,instr, inpRoles,
            evalCtxInfo, predicateClause, attrNameList, externalRelationNumber);
      }
      //Change from function call is_null(expr) to special IS NULL expr. Same applies for IS NOT NULL.
      predicateClause.append(" " + oper.getSymbol());
     // predicateClause.append(") ");
    }
  }
  
  private static void processBaseBoolExpr(ExecContext ec, 
                                          BoolExpr bexpr, 
                                          IAEval stmtEval, 
                                          AInstr instr,  
                                          int[] inpRoles,
                                          EvalContextInfo evalCtxInfo,
                                          StringBuilder predicateClause,
                                          LinkedList<String> attrNameList,
                                          int externalRelationNumber)
    throws CEPException
  {
    BaseBoolExpr expr = (BaseBoolExpr)bexpr;    
        
    if(expr.getUnaryOper() != null)
    {
      //Assumption: External Connection should recognize the NOT keyword
      // as unary Not operation;
      // JDBCExternalConnection recognizes this keyword.

      //predicateClause.append(" (");
      if(expr.getUnary().getKind() == ExprKind.COMP_EXPR)
        processComplexExpr(ec, expr.getUnary(), stmtEval,instr, inpRoles, 
            evalCtxInfo,predicateClause, attrNameList, true, 
            externalRelationNumber);
      else
        processBaseExpr(ec, expr.getUnary(), stmtEval, instr, inpRoles, 
            evalCtxInfo,predicateClause, attrNameList, true, 
            externalRelationNumber);
      predicateClause.append(" " + expr.getUnaryOper().getSymbol().toUpperCase());
      
     // predicateClause.append(") ");
    }
    else
    {
      if(expr.getLeft().getKind() == ExprKind.COMP_EXPR)
        processComplexExpr(ec, expr.getLeft(), stmtEval,instr, 
                           inpRoles, evalCtxInfo,predicateClause, 
                           attrNameList, true, externalRelationNumber);
      else
        processBaseExpr(ec, expr.getLeft(), stmtEval, instr,
                        inpRoles, evalCtxInfo, predicateClause,
                        attrNameList, true, externalRelationNumber);
      
      predicateClause.append(" " + expr.getOper().getSymbol() + " ");
      evalCtxInfo.setIsEqualityPredicate(
          evalCtxInfo.getIsEqualityPredicate() && 
          (expr.getOper() == oracle.cep.common.CompOp.EQ));
        
          
      if(expr.getRight().getKind() == ExprKind.COMP_EXPR)
        processComplexExpr(ec, expr.getRight(), stmtEval,instr, 
                           inpRoles, evalCtxInfo, predicateClause,
                           attrNameList, false, externalRelationNumber);
      else
        processBaseExpr(ec, expr.getRight(), stmtEval,instr,
                        inpRoles, evalCtxInfo, predicateClause,
                        attrNameList, false, externalRelationNumber);
    }
  }
  
  private static void processComplexExpr(ExecContext ec, 
                                         Expr expr, 
                                         IAEval stmtEval,
                                         AInstr instr,  
                                         int[] inpRoles, 
                                         EvalContextInfo evalCtx, 
                                         StringBuilder predicateClause,
                                         LinkedList<String> attrNameList,
                                         boolean isLeftExpr, 
                                         int externalRelationNumber)
    throws CEPException
  {
    if(expr.isExternal())
    {
      ExprComplex compExpr = (ExprComplex) expr;
      Expr left = compExpr.getLeft();
      Expr right = compExpr.getRight();
      if(right != null)
      {
        if(left.getKind() == ExprKind.COMP_EXPR)
          processComplexExpr(ec, left, stmtEval, instr, 
                             inpRoles, evalCtx, predicateClause, 
                             attrNameList, true, externalRelationNumber);
        else
          processBaseExpr(ec, left, stmtEval, instr, 
                          inpRoles, evalCtx, predicateClause, 
                          attrNameList, true, externalRelationNumber);
        
        predicateClause.append(" " + compExpr.getOper().getSymbol() + " ");
        if(right.getKind() == ExprKind.COMP_EXPR)
          processComplexExpr(ec, right, stmtEval, instr, 
                             inpRoles, evalCtx, predicateClause, 
                             attrNameList, false, externalRelationNumber);
        else
          processBaseExpr(ec, right, stmtEval, instr, 
                          inpRoles, evalCtx, predicateClause, 
                          attrNameList, false, externalRelationNumber);
      }
      else
      {
        // TODO: To Provide a support of Unary ArithOp
        throw new CEPException(
            oracle.cep.exceptions.CodeGenError.PREDICATE_CLAUSE_NOT_SUPPORTED,
            compExpr.toString(),
            "Unary Operation Not Allowed."
            );
        /**
        predicateClause.append(compExpr.getOper().getSymbol() + " ");
        
        if(left.getKind() == ExprKind.COMP_EXPR)
          processComplexExpr(ec, left, stmtEval, instr, 
                             inpRoles, evalCtx, predicateClause, 
                             attrNameList, true, externalRelationNumber);
        else
          processBaseExpr(ec, left, stmtEval, instr, 
                          inpRoles, evalCtx, predicateClause, 
                          attrNameList, true, externalRelationNumber);
                          */
      }
    }
    else
    {
      // expr has all the internal streams/relns/view references
      // we will evaluate this at runtime and bind the values
      predicateClause.append(" ? ");
       // If non-external attribute is left operand; 
      //   then insert R to point Right operand;
      // else if non-external attribute is right operand;
      //   then insert L to point Left operand;
      if(isLeftExpr)
        attrNameList.add("R") ;
      else
        attrNameList.add("L");
      
      if(stmtEval != null && instr != null)
      {
        ExprHelper.Addr addr = ExprHelper.instExpr(ec, expr, stmtEval, evalCtx, inpRoles);
        instr.extrInstr.addExternalArg(expr.getType(), addr.role, addr.pos);
      }
    }
  }
  
  private static void processBaseExpr(ExecContext ec,
                                      Expr expr, 
                                      IAEval stmtEval, 
                                      AInstr instr, 
                                      int[] inpRoles,
                                      EvalContextInfo evalCtxInfo, 
                                      StringBuilder predicateClause,
                                      LinkedList<String> attrNameList,
                                      boolean isLeftExpr,
                                      int externalRelationNumber)
    throws CEPException
  {
    if(expr.isExternal() &&
        isCurrentExternalRelationExpr(expr, externalRelationNumber))
    {
      String exprName = null;
      
      //TODO : Currently we are allowing all user def functions but we need to check the external reference 
      // functions capabilities and allow only those user def functions
      if (expr instanceof oracle.cep.phyplan.expr.ExprUserDefFunc)
      {
         oracle.cep.phyplan.expr.ExprUserDefFunc exprUserDefFunc = (oracle.cep.phyplan.expr.ExprUserDefFunc) expr;
         StringBuilder regExpression = new StringBuilder();
         SimpleFunction simpleFunction = ec.getUserFnMgr().getSimpleFunction(exprUserDefFunc.getFuncId());
         String funcName = simpleFunction.getCreationText();
         boolean skipFun = skipFunction(funcName);
         if(!skipFun) {
           regExpression.append(funcName);
           String argSep = ", ";
           regExpression.append("(");
           boolean commaRequired = false;
           for (Expr attrs : exprUserDefFunc.getArgs()) {
             if (commaRequired)
               regExpression.append(argSep);
             // process the base expression of the attributes recursively.
             if (attrs instanceof ExprAttr) {
               String actualName = ((ExprAttr) attrs).getActualName();
               regExpression.append(actualName);
             }
             // comma require for any further attributes.
             commaRequired = true;
           }

           regExpression.append(") ");
         } else {
           //These functions needs to be skipped , will add the attribute name directly with out any function name
           Expr[] attrList = exprUserDefFunc.getArgs();
           if(attrList.length == 1) {
             Expr attrs = attrList[0];
             if (attrs instanceof ExprAttr) {
               String actualName = ((ExprAttr) attrs).getActualName();
               regExpression.append(actualName);
             }
           } else {
             LogUtil.info(LoggerType.TRACE, "Not able to construct predicate for un supported function : "+funcName);
           }
         }
         exprName = regExpression.toString();
      } else if(expr instanceof ExprAttr)
         exprName = ((ExprAttr)expr).getActualName();
      predicateClause.append(exprName);
      attrNameList.add(exprName);
    }
    else
    {
      predicateClause.append(" ? ");
      // If non-external attribute is left operand; 
      //   then insert R to point Right operand;
      // else if non-external attribute is right operand;
      //   then insert L to point Left operand;
      if(isLeftExpr)
        attrNameList.add("R") ;
      else
        attrNameList.add("L");
      
      if(stmtEval != null && instr != null)
      {
        ExprHelper.Addr addr = ExprHelper.instExpr(ec, expr, stmtEval, evalCtxInfo, inpRoles);
        instr.extrInstr.addExternalArg(expr.getType(), addr.role, addr.pos);
      }
    }
  }

  /**
   * Method check the given function is supported in the external references.
   * These listed functions are cql functions but not supported in the references.
   *
   * @param funcName function name
   * @return true  if the given function needs to be skipped
   *         false otherwise
   */
  private static boolean skipFunction(String funcName) {
    if(funcName.equals("to_double") || funcName.equals("to_bigint") || funcName.equals("to_float")
        || funcName.equals("to_boolean"))
      return true;
    return false;
  }

  /**
   * Method to find whether the given expression contains an attribute of a 
   * relation whose name is the same as the given external relation name.
   * 
   * @param expr  the expression which is to be evaluated.
   * @param externalRelationName The name of the external relation involved 
   *                             in the binary join.
   * @return true  if the given expression contains a reference to the given 
   *               external relation.
   *         false otherwise
   */
  private static boolean isCurrentExternalRelationExpr(Expr expr, 
                                                int externalRelationNumber)
  {
    int exprNumber        = -1;
           
    if(expr instanceof ExprAttr)
      exprNumber = ((ExprAttr)expr).getAValue().getInput();
    else if (expr instanceof oracle.cep.phyplan.expr.ExprUserDefFunc)
    {
        oracle.cep.phyplan.expr.ExprUserDefFunc exprFunc = (oracle.cep.phyplan.expr.ExprUserDefFunc) expr;
        if (exprFunc.getNumArgs()>0 && (exprFunc.getArgs()[0] instanceof ExprAttr))
            exprNumber = ((ExprAttr) exprFunc.getArgs()[0]).getAValue().getInput();
    }
    
    if(exprNumber == externalRelationNumber)
      return true;
    
    return false;
    
  }
  
  /**
   * Get Prepared statement from connection object
   * @param op RelationSource Operator
   * @param relName Name of relation
   * @param pred Predicate Object
   * @return Prepared Statement
   * @throws Exception
   */
  private static IExternalPreparedStatement prepareStatement(PhyOpt op, 
                                                      String relName,
                                                      List<String> attrs,
                                                      Predicate pred,
                                                      ConnectionRecoveryContext connRecCtx)
  throws Exception
  {
    
    PhyOptRelnSrc relSrcOp = (PhyOptRelnSrc)op.getInputs()[1];
    if(relSrcOp == null || relSrcOp.getExternalTableSource().getExtConnection() == null)
    {
      LogUtil.info(LoggerType.TRACE, "External Query was not started " +
      		"successfully because either plan is not formed correctly or" +
      		" the external connection is not instantiated.");
      throw new CEPException(CodeGenError.ERROR_STARTING_EXTERNAL_QUERY);
    }
    else
    {
      connRecCtx.setExtSource(relSrcOp.getExternalTableSource());
      IExternalConnection extConn = relSrcOp.getExternalTableSource().getExtConnection();
      return extConn.prepareStatement(relName,attrs,pred);
    }
  }
  
  public static IExternalPreparedStatement getPreparedStmt(PhyOpt op, 
                                             ExecContext execContext,
                                             Predicate externalPredicate,
                                             ConnectionRecoveryContext connRecCtx,
                                             String relName,
                                             List<String> relAttrs)
   throws CEPException
  {
    
      int relid = ((PhyOptRelnSrc)op.getInputs()[1]).getRelId();
      
      if(relName == null)
        relName = execContext.getTableMgr().getTableName(
          ((PhyOptRelnSrc)op.getInputs()[1]).getRelId());
      
      connRecCtx.setTableName(relName); 
      
      if(relAttrs == null)
      {
        relAttrs = new LinkedList<String>();
        String[] attrNames = execContext.getTableMgr().getAttrNames(relid);
        for(int i=0; i<attrNames.length; i++)
          relAttrs.add(attrNames[i]);
      }
      
      connRecCtx.setRelAttrs(relAttrs);
      
      try
      {
        IExternalPreparedStatement extPreparedStatement = 
          prepareStatement(op, relName, relAttrs, externalPredicate, connRecCtx);
          
        
        if(extPreparedStatement == null)
          throw new Exception();
        
        LogUtil.fine(LoggerType.TRACE, "IExternalPreparedStatement creation done");     
        return extPreparedStatement;
      }
      catch(CEPException e)
      {
        LogUtil.fine(LoggerType.TRACE, "Problems in creating external connection.");
        throw e;
      }
      catch(Exception e)
      {
        LogUtil.fine(LoggerType.TRACE, "Problems in creating IExternalPreparedStatement. \n Caused by:" + 
            e.getMessage());
        if(externalPredicate == null)
          throw new CEPException(CodeGenError.RUNAWAY_PREDICATE_NOT_ALLOWED);
        else
          throw new CEPException(CodeGenError.PREDICATE_CLAUSE_NOT_SUPPORTED, 
              externalPredicate.getPredicateClause(),
              e.getMessage());
      }
  
  }
  
  
  /**
   * Selects only valid external relation attributes from attrNameList and
   * insert it in finalAttrNameList 
   * @param attrNameList
   * @param finalAttrNameList
   */
  private static void processAttrList(LinkedList<String> attrNameList, LinkedList<String> finalAttrNameList)
  {
    // Expression: c1 = ? and c2 = c3 and c4 = ? and ? = c5
    // attrNameList: {c1, L , c2, c3, c4, L, R, c5}
    // finalAttrNameList: {c1, c4, c5}
    for(int i = 0; i < attrNameList.size();)
    {
      if(attrNameList.get(i) == "L")
        if(attrNameList.get(i-1) != "R")
          finalAttrNameList.add(attrNameList.get(i-1));
      
      if(attrNameList.get(i) == "R")
        if(attrNameList.get(i+1) != "L")
          finalAttrNameList.add(attrNameList.get(i+1));
      i++;
    }
  }

  /**
   * Split the given set of predicates into two parts
   * one part will be the predicates which are supported by external synopsis
   * other part will not be supported by external synopsis.
   * @param preds set of given predicates
   * @param supportedPredicates supported by external synopsis
   * @param nonSupportedPredicates not supported by external synopsis
   */
  public static void splitPredicates(LinkedList<BoolExpr> preds,
      LinkedList<BoolExpr> supportedPredicates,
      LinkedList<BoolExpr> nonSupportedPredicates,
      IExternalConnection externalConnection,
      EvalContextInfo evalCtxInfo,ExecContext ec)
    throws CEPException
  {
    assert preds != null;
    assert preds.size() > 0;
    assert supportedPredicates != null;
    assert nonSupportedPredicates != null;
    
    List<ExternalFunctionMetadata> capabilities = null;
    
    try
    {
      capabilities = externalConnection.getCapabilities();
    } 
    catch (Exception e)
    {
      LogUtil.fine(LoggerType.TRACE, 
          "Exception in IExternalConnection.getCapabilities() caused by:"
          + e.getMessage());
    }
    
    // If IExternalConnection doesn't have any capability to process the
    // predicate clause, copy all the predicates in nonSupportedPredicates list
    // and return back.
    if(capabilities == null || capabilities.size() == 0)
    {
      LogUtil.fine(LoggerType.TRACE, "External Connection doesn't support " +
      		"any capabilities.");
      nonSupportedPredicates.addAll(preds);
      return;
    }
    
    // Otherwise for each predicate, check whether that is supported by
    // IExternalConnection or not
    int     numSupportedPredicates = 0;
    boolean supportsANDCapability  = false;
    boolean isSupported            = false;

    for(BoolExpr currentPred : preds)
    {
      try
      {
        Predicate currentPredicate = getPredicate(currentPred, evalCtxInfo,ec);
        if(numSupportedPredicates == 0)
        {
          // If it is a first supported predicate; add into the list of
          // supported predicates;
          // Also check if Connection supports AND operation
          // If AND operation is not supported by connection; All the 
          // remaining predicate sub-clauses will go into the list of 
          // non-supported predicates
          
          isSupported = isSupported(currentPred, capabilities)  &&          
                      externalConnection.supportsPredicate(currentPredicate);
           
          LogUtil.fine(LoggerType.TRACE, 
            "The Predicate " + currentPredicate.toString() + "is supported" +
           	" by external Connection ? " + isSupported);
          
          if(isSupported)
          {
            numSupportedPredicates++;
            supportedPredicates.add(currentPred);
            supportsANDCapability = isSupportANDCapability(capabilities);
          }
          else
            nonSupportedPredicates.add(currentPred);
        }
        else if(supportsANDCapability)
        {
          isSupported  = isSupported(currentPred, capabilities) &&
                        externalConnection.supportsPredicate(currentPredicate);
          
          if(isSupported)
            supportedPredicates.add(currentPred);
          else
            nonSupportedPredicates.add(currentPred);
        }
        else
          nonSupportedPredicates.add(currentPred);
      }      
      catch(ExternalException ce)
      {
        String logMessage = "CAUSE: " + ce.getMessage() + 
          "\nACTION: " + ce.getAction();        
        LogUtil.fine(LoggerType.TRACE, logMessage);
        
        // As this is not a supported predicate, put it inside list of 
        // non supported predicates
        nonSupportedPredicates.add(currentPred);        
      }
      catch(Exception e)
      {
        throw new CEPException(CodeGenError.PREDICATE_CLAUSE_NOT_SUPPORTED, "",
          e.getMessage()); 
      }
    }
  }

  /**
   * Helper function to process the parameter predicate expression to construct
   * and return a Predicate object 
   * @param currentPred
   * @param evalCtxInfo
   * @return Predicate
   */
  private static Predicate getPredicate(BoolExpr currentPred, 
    EvalContextInfo evalCtxInfo,ExecContext ec)    
  {
    if(currentPred == null)
      return null;
    
    Predicate currentPredicate      = new Predicate();
    StringBuilder predicateClause   = new StringBuilder();
    LinkedList<String> attrNameList = new LinkedList<String>();
    
    try
    {
      // EvalContext has equality predicate flag should be set to TRUE
      // because it will be used to evaluate whether the predicate
      // is an equality predicate or not
      // Setting it to TRUE for each predicate.old values should be overwritten
      evalCtxInfo.setIsEqualityPredicate(true);
      
      if(currentPred instanceof ComplexBoolExpr)
      {
        processComplexBoolExpr(ec, currentPred, null, null, null,evalCtxInfo,
          predicateClause, attrNameList, Constants.INNER);        
      }
      else if(currentPred instanceof BaseBoolExpr)
      {
        processBaseBoolExpr(ec, currentPred, null, null, null, 
            evalCtxInfo, predicateClause, attrNameList, Constants.INNER);
      }
      LinkedList<String> finalAttrNameList = new LinkedList<String>();
      processAttrList(attrNameList, finalAttrNameList);
      currentPredicate.setAttrNameList(finalAttrNameList);
      currentPredicate.setPredicateClause(predicateClause.toString());
      currentPredicate.setIsEqualityPredicate(
        evalCtxInfo.getIsEqualityPredicate());
      LogUtil.finest(LoggerType.TRACE, "Number of attributes in originial" +
      		" list:" + attrNameList.size());
    }
    catch(Exception e)
    {
      LogUtil.fine(LoggerType.TRACE, 
          "Problems reported in the Predicate construction");
      LogUtil.logStackTrace(e);
      return null;
    }
      
    return currentPredicate;
  }

  private static boolean isSupportANDCapability(
      List<ExternalFunctionMetadata> capabilities)
  {
    // Prepare a temporary Logical AND IUserFunctionMetadata object
    ExternalFunctionMetadata currentOperation = new ExternalFunctionMetadata(
       LogicalOp.AND.getFuncName(), 
       new Datatype[]{Datatype.BOOLEAN, Datatype.BOOLEAN});    
    return isMatchingOperationFound(currentOperation, capabilities);
  }
  

  private static boolean isSupported(BoolExpr currentPred,
      List<ExternalFunctionMetadata> capabilities)
  {
    if(!currentPred.isExternal() || currentPred.isNull())
      return false;
    
    if(currentPred.getKind() == ExprKind.COMP_BOOL_EXPR)
    {
      return checkComplexBoolExpr(((ComplexBoolExpr)currentPred), capabilities); 
    }
    else if(currentPred.getKind() == ExprKind.BASE_BOOL_EXPR)
    {
      return checkBaseBoolExpr(((BaseBoolExpr)currentPred), capabilities); 
    }
    else
      return false;
    
  }
  
  private static boolean checkComplexBoolExpr(ComplexBoolExpr currentPred,
      List<ExternalFunctionMetadata> capabilities)
  {
    // External Synopsis will not support evaluation of complex boolean
    // expressions. To do that, join factory will create a BEval.
    Expr leftPred  = currentPred.getLeft();
    Expr rightPred = currentPred.getRight();
    
    LogicalOp operation = currentPred.getOper();
    boolean isUnary     = rightPred == null;
    
    Datatype leftType  = leftPred.getType();
    Datatype rightType = !isUnary ? rightPred.getType() : null;

    // Check if Both predicates are not referring to external relation
    // then the whole predicate will be unsupported by external relation
    if((isUnary && !leftPred.isExternal()) ||
       (!isUnary && !leftPred.isExternal() && !rightPred.isExternal()))
      return false;
    
    
    // Prepare a temporary IUSerFunctionMetadata object
    ExternalFunctionMetadata currentOperation = null;
    
    if(isUnary)
    {
      // Handle UNARY operation
      currentOperation = new ExternalFunctionMetadata(
        operation.getFuncName(), new Datatype[]{leftType});
    }
    else
    {
      currentOperation = new ExternalFunctionMetadata(
          operation.getFuncName(), new Datatype[]{leftType, rightType});
    }
    
    boolean isMatchingOperationFound
      = isMatchingOperationFound(currentOperation, capabilities);
    
    if(!isMatchingOperationFound)
      return false;
    else
    {      
      boolean isLeftPredSupported = false; 
      if(leftPred.getKind() == ExprKind.COMP_BOOL_EXPR)
      {
        isLeftPredSupported 
          = checkComplexBoolExpr(((ComplexBoolExpr)leftPred), capabilities); 
      }
      else if(leftPred.getKind() == ExprKind.BASE_BOOL_EXPR)
      {
        isLeftPredSupported 
          = checkBaseBoolExpr(((BaseBoolExpr)leftPred), capabilities); 
      }
      else
        isLeftPredSupported = false;
      
      if(isLeftPredSupported)
      {
        if(rightPred == null)
        {
          return true;
        }
        else if(rightPred.getKind() == ExprKind.COMP_BOOL_EXPR)
        {
          return checkComplexBoolExpr(((ComplexBoolExpr)rightPred), 
                                      capabilities); 
        }
        else if(rightPred.getKind() == ExprKind.BASE_BOOL_EXPR)
        {
          return checkBaseBoolExpr(((BaseBoolExpr)rightPred), capabilities); 
        }
        else
          return false;
      }
      else
        return false;
    }
  }

  private static boolean checkBaseBoolExpr(BaseBoolExpr currentPred,
      List<ExternalFunctionMetadata> capabilities)
  {
    if (currentPred.getUnaryOper() != null) {
      Expr unary = currentPred.getUnary();
      String op = currentPred.getUnaryOperator().getSymbol();
      // Prepare a temporary IUserFunctionMetadata object
      ExternalFunctionMetadata currentOperation = new ExternalFunctionMetadata(
              op,
              new Datatype[]{unary.getType()});
      boolean isMatchingOperationFound
              = isMatchingOperationFound(currentOperation, capabilities);
      // If External Connection doesn't have the required operation, return false
      if (!isMatchingOperationFound)
        return false;
      else {
        if (unary.getKind() == ExprKind.COMP_EXPR)
          return checkComplexExpr((ExprComplex) unary, capabilities);
        else if (!unary.isExternal())
          return true;
        else if (unary.getKind() == ExprKind.ATTR_REF ||
                unary.getKind() == ExprKind.CONST_VAL || unary.getKind() == ExprKind.USER_DEF)
          return true;
        else{
          return false;
        }
      }
    }
    Expr leftExpr    = currentPred.getLeft();
    Expr rightExpr   = currentPred.getRight();
    CompOp operation = currentPred.getOper();
        
    // Assertions
    assert leftExpr != null;
    assert rightExpr != null;
   
    // Check if both expressions are not referring to any external attribute
    // then we don't need to consider this expression to run on external
    // connection
    if(!leftExpr.isExternal() && !rightExpr.isExternal())
      return false;
    
    // Prepare a temporary IUserFunctionMetadata object
    ExternalFunctionMetadata currentOperation = new ExternalFunctionMetadata(
        operation.getFuncName(), 
        new Datatype[]{leftExpr.getType(), rightExpr.getType()});
    
    boolean isMatchingOperationFound 
        = isMatchingOperationFound(currentOperation, capabilities); 
      
    // If External Connection doesn't have the required operation, return false    
    if(!isMatchingOperationFound)
      return false;        
    else
    {
      boolean isLeftExprSupported = false;
      if(leftExpr.getKind() == ExprKind.COMP_EXPR)
      {
        isLeftExprSupported = checkComplexExpr((ExprComplex)leftExpr, capabilities); 
      }
      else if(!leftExpr.isExternal())
      {        
        isLeftExprSupported = true;
      }
      //TODO : Currently we are allowing all user def functions but we need to check the external reference 
      // functions capabilities and allow only those user def functions
      else if(leftExpr.getKind() == ExprKind.ATTR_REF || 
          leftExpr.getKind() == ExprKind.CONST_VAL || leftExpr.getKind() == ExprKind.USER_DEF)
      {
        isLeftExprSupported = true;
      }
      else 
        isLeftExprSupported = false;
      
      // Check right expression only if left is supported
      if(isLeftExprSupported)
      {    	 
        if(rightExpr.getKind() == ExprKind.COMP_EXPR)
        {
          return checkComplexExpr((ExprComplex)rightExpr, capabilities);
        }
        //TODO : Currently we are allowing all user def functions but we need to check the external reference 
        // functions capabilities and allow only those user def functions
        else if(rightExpr.getKind() == ExprKind.ATTR_REF || 
            rightExpr.getKind() == ExprKind.CONST_VAL || rightExpr.getKind() == ExprKind.USER_DEF)
        {
          return true;
        }
        else if(!rightExpr.isExternal())
        {
          assert leftExpr.isExternal();
          return true;
        }        
        else
          return false;
      }
      else
        return false;
    }   
  }

  private static boolean checkComplexExpr(ExprComplex paramExpr,
      List<ExternalFunctionMetadata> capabilities)
  {
    
    ArithOp operation = paramExpr.getOper();
    Expr leftExpr = paramExpr.getLeft();
    Expr rightExpr = paramExpr.getRight();
    
    // Assertions
    assert leftExpr != null;
    assert rightExpr != null;
        
    // Prepare a temporary IUserFunctionMetadata object
     ExternalFunctionMetadata currentOperation = new ExternalFunctionMetadata(
        operation.getFuncName(), 
        new Datatype[]{leftExpr.getType(), rightExpr.getType()});
    
    boolean isMatchingOperationFound 
        = isMatchingOperationFound(currentOperation, capabilities);
    
    // If External Connection doesn't have the required operation, return false    
    if(!isMatchingOperationFound)
      return false;        
    else
    {
      boolean isLeftExprSupported = false;
      if(leftExpr.getKind() == ExprKind.COMP_EXPR)
      {
        isLeftExprSupported = checkComplexExpr((ExprComplex)leftExpr, capabilities); 
      }
      else if(!leftExpr.isExternal())
      {
        isLeftExprSupported = true;
      }
      else if(leftExpr.getKind() == ExprKind.ATTR_REF || 
              leftExpr.getKind() == ExprKind.CONST_VAL)
      {
        isLeftExprSupported = true;
      }
      else 
        isLeftExprSupported = false;
      
      // Check the right Predicate only if left Predicate is supported
      if(isLeftExprSupported)
      {
        if(rightExpr.getKind() == ExprKind.COMP_EXPR)
        {
          return checkComplexExpr((ExprComplex)rightExpr, capabilities);
        }
        else if(!rightExpr.isExternal())
        {
          return true;
        }
        else if(rightExpr.getKind() == ExprKind.ATTR_REF || 
            rightExpr.getKind() == ExprKind.CONST_VAL)
          return true;
        else
          return false;
      }
      else
        return false;
    }
  }
  
  private static boolean isMatchingOperationFound(
      ExternalFunctionMetadata paramOperation,
      List<ExternalFunctionMetadata> capabilities
      )
  {
    boolean isMatchingOperationFound = false;
    for(int i = 0; i < capabilities.size(); i++)
    {
      ExternalFunctionMetadata cap = capabilities.get(i);
      isMatchingOperationFound = paramOperation.equals(capabilities.get(i));
      if(isMatchingOperationFound)     
        break;
    }
    return isMatchingOperationFound;
  }
}
