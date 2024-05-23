/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/factory/LogOptStrmSrcFactory.java /main/3 2008/10/24 15:50:16 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    mthatte     04/10/08 - 
    parujain    03/11/08 - derived timestamp
    najain      04/06/06 - cleanup
    najain      03/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/factory/LogOptStrmSrcFactory.java /main/3 2008/10/24 15:50:16 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.factory;

import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptStrmSrc;
import oracle.cep.logplan.LogicalPlanException;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;


/**
 * LogOptSrcStrmFactory
 * 
 * @author najain
 */
public class LogOptStrmSrcFactory extends LogOptFactory {

  /**
   * Constructor for SelectFactory
   */
  public LogOptStrmSrcFactory() {
    super();
  }

  public LogOpt newLogOpt(Object ctx) throws LogicalPlanException {
    assert ctx instanceof SemQueryInterpreterFactoryContext;
    SemQueryInterpreterFactoryContext lpctx = (SemQueryInterpreterFactoryContext) ctx;
    LogOpt op;
    try {
      op = new LogOptStrmSrc(lpctx.getExecContext(), lpctx.getQuery(), lpctx.getVarId(), lpctx.getTableId());
      if(lpctx.dSpec != null)
      {
        Expr dtExpr = SemQueryExprFactory.getInterpreter(lpctx.dSpec.getDerivedTsExpr(),
    	          new SemQueryExprFactoryContext(lpctx.dSpec.getDerivedTsExpr(), lpctx.getQuery()));
        ((LogOptStrmSrc)op).setDerivedTSExpr(dtExpr);
        ((LogOptStrmSrc)op).setDerivedTS(true);
      }
    } catch (LogicalPlanException ex) {
      throw new LogicalPlanException(
          LogicalPlanError.LOGOPT_STRMSRC_NOT_CREATED);
    }

    return op;
  }

}

