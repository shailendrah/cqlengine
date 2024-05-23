/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptRStrmFactory.java /main/5 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
 All rights reserved. */

/*
   DESCRIPTION
    Factory for physical representation of the RSTREAM operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    sborah      09/23/08 - pass in equiv logopt in constructor
    hopark      07/13/07 - dump stack trace on exception
    rkomurav    06/18/07 - cleanup
    anasrini    04/04/06 - Creation
    anasrini    04/04/06 - Creation
    anasrini    04/04/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptRStrmFactory.java /main/5 2008/10/24 15:50:17 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRStream;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRStrm;
import oracle.cep.service.ExecContext;

/**
 * Factory for physical representation of the RSTREAM operator
 *
 * @since 1.0
 */

class PhyOptRStrmFactory extends PhyOptFactory {

  public PhyOpt newPhyOpt(Object ctx) {

    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOptRStrm                      phyRStream;
    PhyOpt[]                         phyChildren;
    PhyOpt                           phyChild;
    LogOpt                           logop;

    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptRStream : logop.getClass().getName();
    
    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;

    try 
    {
      phyRStream = new PhyOptRStrm(ec, phyChild, logop);
    } 
    catch (PhysicalPlanException e) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      phyRStream = null;
    }
    
    assert phyRStream != null;
    return phyRStream;
  }
}

