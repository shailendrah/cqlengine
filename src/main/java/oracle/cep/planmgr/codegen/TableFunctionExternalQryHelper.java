/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/TableFunctionExternalQryHelper.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/02/10 - Creation
 */

package oracle.cep.planmgr.codegen;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.ExternalInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.TableFunctionInfo;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/TableFunctionExternalQryHelper.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class TableFunctionExternalQryHelper
{
  public static void prepareStmtEval(IAEval stmtEval,
      EvalContextInfo evalCtxInfo, 
      Expr tableFunctionExpr, 
      ExecContext ec) throws CEPException
  {
    // Table function expression will always refer to STREAM attributes; 
    // and STREAM tuples are always bounded to OUTER role
    int[] inpRoles = new int[]{IEvalContext.OUTER_ROLE};
    
    // Newly constructed Output tuple after table function expression evaluation
    // will be bounded to NEW_OUTPUT_ROLE
    int outputRole = IEvalContext.NEW_OUTPUT_ROLE;
    
    ExprHelper.instExprDest(ec, 
        tableFunctionExpr,
        stmtEval,
        evalCtxInfo,
        outputRole,
        0,
        inpRoles);
    
  }
  
  /**
   * Create IExternalPrepared Statement
   * @param op 
   * @param execContext
   * @param externalPredicate
   * @return IExternalPreparedStatement
   * @throws CEPException
   */
  public static IExternalPreparedStatement getPreparedStmt(PhyOpt op, 
                                            ExecContext execContext,
                                            TableFunctionInfo tableFunction)
    throws CEPException
  {
    
    try
    {
      List<String> relAttrs = new LinkedList<String>();
     
      relAttrs.add(tableFunction.getColumnAlias());
      // In case of table function data source, obtain connection from
      // Cross/StrmCross operator only
      //Note: TO decide whether we should push back the table connection objects
      IExternalPreparedStatement extPreparedStatement 
        = op.getExtConnection().prepareStatement(tableFunction.getTableAlias(), 
            relAttrs, 
            null);
      
      if(extPreparedStatement == null)
        throw new Exception();
      
      LogUtil.info(LoggerType.TRACE, "IExternalPreparedStatement creation done");     
      return extPreparedStatement;
    }
    catch(Exception e)
    {
      LogUtil.info(LoggerType.TRACE, "Problems in creating IExternalPreparedStatement");
      throw new CEPException(CodeGenError.ERROR_STARTING_EXTERNAL_QUERY);
    }
  }
  
  public static AInstr getPreparedInstr()
  {
    AInstr instr = new AInstr();
    instr.op = AOp.PREP_STMT;    
    instr.extrInstr = new ExternalInstr();
    return instr;
  }
}
