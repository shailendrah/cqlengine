/* $Header: PhyOptFactory.java 31-jul-2006.17:40:49 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 najain      07/31/06 - silent relations
 najain      04/06/06 - cleanup
 najain      04/04/06 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: PhyOptFactory.java 31-jul-2006.17:40:49 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.PhyOpt;

/**
 * PhyOptFactory
 * 
 * @author najain
 */
abstract class PhyOptFactory
{
  abstract PhyOpt newPhyOpt(Object ctx) throws CEPException;

  PhyOpt getNewPhyOpt(Object ctx) throws CEPException
  {
    PhyOpt op = newPhyOpt(ctx);
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lpctx = (LogPlanInterpreterFactoryContext)ctx;
    
    for (int i = 0; i < lpctx.getPhyChildPlans().length; i++)
      if (!lpctx.getPhyChildPlans()[i].isSilentRelnDep())
        return op;

    op.setSilentRelnDep();
    for (int i = 0; i < lpctx.getPhyChildPlans().length; i++)
      op.addSilentRelnDep(lpctx.getPhyChildPlans()[i].getSilentRelnDep());

    return op;
  }
}
