/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptIStrmFactory.java /main/5 2009/12/24 20:10:21 vikshukl Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    IStream physical operator factory 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    09/08/09 - support for ISTREAM (R) DIFFERENCE USING (...)
    hopark      10/09/08 - remove statics
    anasrini    09/16/08 - pass in equiv logopt in constructor
    hopark      07/13/07 - dump stack trace on exception
    ayalaman    04/23/06 - IStream physical operator factory 
    ayalaman    04/23/06 - IStream physical operator factory 
    ayalaman    04/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptIStrmFactory.java /main/5 2009/12/24 20:10:21 vikshukl Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptIStream;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptIStrm;
import oracle.cep.service.ExecContext;

class PhyOptIStrmFactory extends PhyOptFactory {

  public PhyOpt newPhyOpt(Object ctx)
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOptIStrm                phyIStream;
    PhyOpt[]                   phyChildren;
    PhyOpt                     phyChild;
    LogOptIStream              logIStream;
    LogOpt                     logop;

    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptIStream : logop.getClass().getName();
    logIStream = (LogOptIStream)logop;

    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;

    try {
      phyIStream = new PhyOptIStrm(ec, phyChild, logop, 
                                   logIStream.getUsingExprListMap());
    } 
    catch (PhysicalPlanException e) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      phyIStream = null;
    }
    
    assert phyIStream != null;
    return phyIStream;
  }
}
