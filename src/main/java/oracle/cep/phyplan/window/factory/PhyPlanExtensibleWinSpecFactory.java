/* $Header: LogPlanExtensibleWinSpecFactory.java 07-mar-2007.17:15:09 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/07/07 - Extensible Window Spec Factory
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: LogPlanExtensibleWinSpecFactory.java 07-mar-2007.17:15:09 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window.factory;

import oracle.cep.phyplan.factory.LogPlanInterpreterFactoryContext;
import oracle.cep.phyplan.window.PhyExtensibleWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;

public class PhyPlanExtensibleWinSpecFactory extends PhyPlanWinSpecFactory {

  @Override
  public PhyWinSpec newWinSpec(Object ctx) {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lctx = (LogPlanInterpreterFactoryContext)ctx;
    
    PhyExtensibleWinSpec winspec = new PhyExtensibleWinSpec(lctx.getLogPlan());
    return winspec;
  }
  
}
