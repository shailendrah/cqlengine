/* $Header: pcbpel/cep/src/oracle/cep/phyplan/window/factory/PhyPlanValueWinSpecFactory.java /main/1 2008/07/14 22:57:01 parujain Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/phyplan/window/factory/PhyPlanValueWinSpecFactory.java /main/1 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window.factory;

import oracle.cep.phyplan.factory.LogPlanInterpreterFactoryContext;
import oracle.cep.phyplan.window.PhyValueWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;

public class PhyPlanValueWinSpecFactory extends PhyPlanWinSpecFactory {

  @Override
  public PhyWinSpec newWinSpec(Object ctx) {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lctx = (LogPlanInterpreterFactoryContext)ctx;
	    
    PhyValueWinSpec winspec = new PhyValueWinSpec(lctx.getLogPlan());
    return winspec;
  }
	  
	}