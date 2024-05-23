/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptJoinFactory.java /main/7 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/17/12 - maintain phy attrs in phyoptjoin
    sbishnoi    09/30/09 - table function support
    sbishnoi    05/26/09 - interpret outerjoin predicates
    hopark      10/09/08 - remove statics
    hopark      07/13/07 - dump stack trace on exception
    najain      04/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptJoinFactory.java /main/7 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.Constants;

import oracle.cep.phyplan.attr.Attr;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptCross;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.PhyOptJoin;
import oracle.cep.phyplan.PhyOptJoinBase;
import oracle.cep.phyplan.TableFunctionInfo;
import oracle.cep.service.ExecContext;

/**
 * PhyOptJoinFactory
 *
 * @author najain
 */
public class PhyOptJoinFactory extends PhyOptFactory {

  /**
   * Constructor for PhyOptJoinFactory
   */
  public PhyOptJoinFactory() {
    super();
  }

  public PhyOpt newPhyOpt(Object ctx) {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lpctx = (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    PhyOpt op;
    try 
    {
      LogOptCross logPlan = (LogOptCross)lpctx.getLogPlan();
      op = new PhyOptJoin(ec, lpctx.getLogPlan(), lpctx.getPhyChildPlans());
      ((PhyOptJoin)op).setOuterJoinType(logPlan.getOuterJoinType());
      
      // get the outer join predicates (if any)
      List<oracle.cep.logplan.expr.BoolExpr> outerJoinPred 
        = logPlan.getPredicates();
      
      if(!outerJoinPred.isEmpty())
      {
        oracle.cep.logplan.expr.BoolExpr logPred;
        LogPlanExprFactoryContext        exprCtx;
        
        Iterator<oracle.cep.logplan.expr.BoolExpr> iter 
          = outerJoinPred.iterator();
        while(iter.hasNext())
        {
          logPred = iter.next();
          exprCtx = new LogPlanExprFactoryContext(logPred, logPlan);
          BoolExpr phyPred =
            (BoolExpr)LogPlanExprFactory.getInterpreter(logPred, exprCtx);
          ((PhyOptJoin)op).addAtomicPred(phyPred);
        }
      }
      
      // Handle Table Function Relation Source(If Exist)
      if(logPlan.isExternal())
      {
        TableFunctionInfo tableFunctionInfo 
          = TableFunctionHelper.getTableFunctionInfo(logPlan, op);
        ((PhyOptJoin)op).setTableFunctionInfo(tableFunctionInfo);
      }
      
      //convert logical attrs into phyattrs
      LogOpt outer = logPlan.getInput(Constants.OUTER);
      LogOpt inner = logPlan.getInput(Constants.INNER);
      
      List<Attr> phyOuterAttrs = new ArrayList<Attr>();
      List<oracle.cep.logplan.attr.Attr> logOuterAttrs = outer.getOutAttrs();
      for(oracle.cep.logplan.attr.Attr logattr : logOuterAttrs)
      {
        Attr phyattr = LogPlanAttrFactory.getInterpreter(logPlan, logattr);
        phyOuterAttrs.add(phyattr);
      }
      
      List<Attr> phyInnerAttrs = new ArrayList<Attr>();
      List<oracle.cep.logplan.attr.Attr> logInnerAttrs = inner.getOutAttrs();
      for(oracle.cep.logplan.attr.Attr logattr : logInnerAttrs)
      {
        Attr phyattr = LogPlanAttrFactory.getInterpreter(logPlan, logattr);
        phyInnerAttrs.add(phyattr);
      }
     
      ((PhyOptJoinBase) op).setOuterAttrs(phyOuterAttrs);
      ((PhyOptJoinBase) op).setInnerAttrs(phyInnerAttrs); 
    } 
    catch (CEPException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      op = null;
    }

    return op;
  }

}

