/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptStrJoinFactory.java /main/5 2010/01/25 00:32:43 sbishnoi Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/04/09 - table function support
    hopark      10/09/08 - remove statics
    hopark      07/13/07 - dump stack trace on exception
    najain      05/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptStrJoinFactory.java /main/5 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOptStrmCross;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.TableFunctionInfo;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.PhyOptStrJoin;
import oracle.cep.service.ExecContext;

/**
 * PhyOptStrJoinFactory
 *
 * @author najain
 */
public class PhyOptStrJoinFactory extends PhyOptFactory {

  /**
   * Constructor for PhyOptJoinFactory
   */
  public PhyOptStrJoinFactory() {
    super();
  }

  public PhyOpt newPhyOpt(Object ctx) {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lpctx = (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    PhyOpt op;
    try 
    {
      op = new PhyOptStrJoin(ec, lpctx.getLogPlan(), lpctx.getPhyChildPlans());
      assert lpctx.getLogPlan() instanceof LogOptStrmCross;
      LogOptStrmCross logPlan = ((LogOptStrmCross)lpctx.getLogPlan());
      
      // Handle Table Function Relation Source(If Exist)
      if(logPlan.isExternal())
      {
        TableFunctionInfo tableFunctionInfo 
          = TableFunctionHelper.getTableFunctionInfo(logPlan, op);
        ((PhyOptStrJoin)op).setTableFunctionInfo(tableFunctionInfo);
      }
    } 
    catch (CEPException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      op = null;
    }

    return op;
  }

}

