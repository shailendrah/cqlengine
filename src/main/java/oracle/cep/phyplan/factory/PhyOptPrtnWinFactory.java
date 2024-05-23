/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptPrtnWinFactory.java /main/7 2008/10/24 15:50:17 hopark Exp $ */

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
    hopark      10/12/07 - use win factory
    hopark      07/13/07 - dump stack trace on exception
    rkomurav    06/18/07 - cleanup
    rkomurav    03/05/07 - rework on attr factory
    hopark      12/15/06 - add range
    ayalaman    08/08/06 - build physical operator
    ayalaman    08/01/06 - Partition window physical operator factory
    ayalaman    08/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptPrtnWinFactory.java /main/7 2008/10/24 15:50:17 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.phyplan.window.factory.LogPlanWinSpecFactory;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.PhyOptPrtnWin;
import oracle.cep.service.ExecContext;
import oracle.cep.logplan.LogOptPrtnWin;

/**
 * PhyOptPrtnWinFactory
 * 
 * @author ayalaamn
 */
class PhyOptPrtnWinFactory extends PhyOptFactory {

  PhyOpt newPhyOpt(Object ctx) throws CEPException {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lpctx = (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    LogOptPrtnWin    prtnLogPlan; 
    int              numPartnAttrs; 
    oracle.cep.logplan.attr.Attr[] logAttrs;
    Attr[] phyAttrs; 
   
    assert lpctx.getLogPlan() instanceof LogOptPrtnWin;

    prtnLogPlan = (LogOptPrtnWin)lpctx.getLogPlan();
    numPartnAttrs = prtnLogPlan.getNumPartnAttrs();
    logAttrs = prtnLogPlan.getPartnAttrs();
    phyAttrs = new Attr[numPartnAttrs];

    // create equivalent physical plan attributes for each logical one.
    for (int aidx = 0; aidx < numPartnAttrs ; aidx++)
    {
      phyAttrs[aidx] = LogPlanAttrFactory.getInterpreter((LogOpt)prtnLogPlan,
                                                  logAttrs[aidx]);
    }

    PhyOpt op;
    try 
    {
      PhyWinSpec winSpec = LogPlanWinSpecFactory.getWinSpec(lpctx.getLogPlan(), lpctx);
      op = new PhyOptPrtnWin(ec, winSpec, lpctx.getLogPlan(),
                             lpctx.getPhyChildPlans(), numPartnAttrs, phyAttrs);
    } 
    catch (CEPException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      op = null;
    }

    return op;
  }

}

