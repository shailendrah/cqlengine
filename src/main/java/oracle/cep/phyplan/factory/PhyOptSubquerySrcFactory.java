package oracle.cep.phyplan.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptSubquerySrc;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSubquerySrc;
import oracle.cep.service.ExecContext;

/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptSubquerySrcFactory.java /main/1 2011/09/23 11:16:35 vikshukl Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    08/25/11 - subquery support
    vikshukl    08/25/11 - Creation
 */

/**
 *  @version $Header: PhyOptSubquerySrcFactory.java 25-aug-2011.15:38:30 vikshukl Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

/**
 * PhyOptExchangeFactory
 */
class PhyOptSubquerySrcFactory extends PhyOptFactory 
{
  PhyOpt newPhyOpt(Object ctx) throws CEPException 
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext ctx1 =  
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOptSubquerySrc phySubquerySrc;
    PhyOpt[]          phyChildren;
    LogOptSubquerySrc logSubquerySrc;
    LogOpt         logop;

    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert phyChildren != null;
    assert phyChildren.length == 1; // can have only child input
    assert logop != null;
    assert logop instanceof LogOptSubquerySrc : logop.getClass().getName();
    logSubquerySrc = (LogOptSubquerySrc)logop;

    phySubquerySrc = new PhyOptSubquerySrc(ec, phyChildren[0],
                                           logSubquerySrc);
    return phySubquerySrc;
  }
}