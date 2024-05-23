/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptDStrmFactory.java /main/4 2008/10/24 15:50:16 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
 All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    sborah      09/23/08 - pass in equiv logopt in constructor
    hopark      07/13/07 - dump stack trace on exception
    ayalaman    04/23/06 - DStream physical operator factory 
    ayalaman    04/23/06 - DStream physical operator factory 
    ayalaman    04/23/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptDStrmFactory.java /main/4 2008/10/24 15:50:16 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptDStream;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptDStrm;
import oracle.cep.service.ExecContext;

class PhyOptDStrmFactory extends PhyOptFactory {

  public PhyOpt newPhyOpt(Object ctx)
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    PhyOptDStrm                      phyDStream;
    PhyOpt[]                         phyChildren;
    PhyOpt                           phyChild;
    LogOptDStream                    logDStream;
    LogOpt                           logop;

    ExecContext ec = ctx1.getExecContext();
    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptDStream : logop.getClass().getName();
    logDStream = (LogOptDStream)logop;

    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;

    try {
      phyDStream = new PhyOptDStrm(ec, phyChild, logop);
    } 
    catch (PhysicalPlanException e) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      phyDStream = null;
    }
    
    assert phyDStream != null;
    return phyDStream;
  }
}
