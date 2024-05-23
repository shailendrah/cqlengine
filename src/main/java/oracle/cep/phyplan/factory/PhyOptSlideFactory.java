/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptSlideFactory.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/28/12 - Creation
 */

package oracle.cep.phyplan.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOptSlide;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSlide;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptSlideFactory.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyOptSlideFactory extends PhyOptFactory
{
  @Override
  PhyOpt newPhyOpt(Object ctx) throws CEPException
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lpctx = 
      (LogPlanInterpreterFactoryContext) ctx;
    
    ExecContext ec = lpctx.getExecContext();
    
    PhyOpt[]          phyChildren;
    PhyOpt            phyChild;

    // Get the logplan from the available context
    LogOptSlide logOpt = (LogOptSlide) lpctx.getLogPlan();

    phyChildren = lpctx.getPhyChildPlans();
    
    assert logOpt != null;
    assert logOpt instanceof LogOptSlide : logOpt.getClass().getName();
    assert logOpt.getNumInputs() == 1 : logOpt.getNumInputs();
    
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;
    
    // Get the slide interval
    long numSlideNanos = logOpt.getNumSlideNanos();
    
    // Construct physical slide operator
    PhyOpt phyOpt = new PhyOptSlide(ec, phyChild, logOpt, numSlideNanos);
    
    return phyOpt;
  }
  
}