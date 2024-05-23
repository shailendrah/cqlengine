/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyPlanGen.java /main/5 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    05/13/12 - bypass useless project optimization if query is based
                        on archived relation
 hopark      10/09/08 - remove statics
 najain      06/19/06 - maintain list of queryIds 
 najain      06/05/06 - add query 
 najain      05/11/06 - Dyanmic Query Support 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - PhyOptOutput constructor cleanup 
 najain      04/04/06 - cleanup
 najain      02/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyPlanGen.java /main/5 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.factory.LogPlanInterpreterFactory;
import oracle.cep.phyplan.factory.LogPlanInterpreterFactoryContext;

public class PhyPlanGen {

  public PhyOpt genPhysicalPlan(LogOpt logPlan, PhyPlanGenContext ctx)
      throws CEPException {
    assert logPlan != null;

    // root operator in the logical plan
    LogOpt rootOp = logPlan;

    // physical plans for each child of the root
    PhyOpt[] p_childPlans = new PhyOpt[rootOp.getNumInputs()];

    // recursively produce the physical plan for each child logical plan
    for (int i = 0; i < rootOp.getNumInputs(); i++) {
      p_childPlans[i] = genPhysicalPlan(rootOp.getInput(i), ctx);
    }
    
    Query  qry = ctx.getQuery();
    PhyOpt op  = LogPlanInterpreterFactory.getInterpreter(logPlan,
      new LogPlanInterpreterFactoryContext(ctx.getExecContext(), logPlan, p_childPlans, qry));
    if (op.isNew())
      op.addQryId(new Integer(qry.getId()));
    else
      op.addQryId(new Integer(qry.getId()), true);
    
    // Propagate ordering constraint from logical plan to physical plan.
    op.setOrderingConstraint(logPlan.getOrderingConstraint());
    
    //if query is dependent on archived relation then we don't want the 
    //created phyoptproject to be removed in Useless project removal
    //optimization since it creates problems in archiver query generation

    if((op instanceof PhyOptProject) && (qry.isDependentOnArchivedRelation()))
    {
      ((PhyOptProject)op).setIsExemptFromUselessOpt(true);
    }

    return op;
  }
}
