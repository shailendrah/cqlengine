/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/LogPlanInterpreterFactoryContext.java /main/2 2008/10/24 15:50:14 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/07/08 - use execContext to remove statics
 najain      06/05/06 - add query 
 najain      04/06/06 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/LogPlanInterpreterFactoryContext.java /main/2 2008/10/24 15:50:14 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

/**
 * Context for physical operators generation from logical operators
 * 
 * @author najain
 */
public class LogPlanInterpreterFactoryContext {
  ExecContext execContext;
  LogOpt   logPlan;
  PhyOpt[] phyChildPlans;
  Query    query;

  public ExecContext getExecContext() {
    return execContext;
  }
  
  public LogOpt getLogPlan() {
    return logPlan;
  }

  public Query getQuery() {
    return query;
  }

  public PhyOpt[] getPhyChildPlans() {
    return phyChildPlans;
  }

  public LogPlanInterpreterFactoryContext(ExecContext ec,
                                          LogOpt   logPlan, 
					  PhyOpt[] phyChildPlans,
					  Query    query) {
    this.execContext   = ec;
    this.logPlan       = logPlan;
    this.phyChildPlans = phyChildPlans;
    this.query         = query;
  }

}
