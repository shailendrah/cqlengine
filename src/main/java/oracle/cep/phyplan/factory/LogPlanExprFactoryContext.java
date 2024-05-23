/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/LogPlanExprFactoryContext.java /main/6 2009/04/28 10:24:10 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/28/09 - removing unused constructor
 sborah      04/24/09 - add constructor
 rkomurav    06/18/07 - cleanup
 rkomurav    06/11/07 - add setattrfactory method
 anasrini    05/28/07 - add corr factory for AttrAggr as well
 rkomurav    03/06/07 - add instances of LogPlanAttrFactory
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/LogPlanExprFactoryContext.java /main/6 2009/04/28 10:24:10 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.phyplan.PhyOpt;

/**
 * Context for physical operators generation from logical operators
 *
 * @author najain
 */
public class LogPlanExprFactoryContext
{
  Expr     logExpr;

  LogOpt   logPlan;
  
  PhyOpt[] phyChildren;
  
  boolean  makeCorr;
  
  public Expr getLogExpr()
  {
    return logExpr;
  }

  public LogOpt getLogPlan()
  {
    return logPlan;
  }
  
  public boolean isMakeCorr()
  {
    return makeCorr;
  }
  
  public PhyOpt[] getPhyChildren()
  {
    return this.phyChildren;
  }

  public LogPlanExprFactoryContext(Expr logExpr, LogOpt logPlan)
  {
    this.logPlan     = logPlan;
    this.logExpr     = logExpr;
    this.phyChildren = null;
    this.makeCorr    = false;
  }  
  
  public LogPlanExprFactoryContext(Expr logExpr, LogOpt logPlan,
                                   PhyOpt[] phyChildren)
  {
    this.logPlan     = logPlan;
    this.logExpr     = logExpr;
    this.phyChildren = phyChildren;
    this.makeCorr    = false;
  }   
   
  public LogPlanExprFactoryContext(Expr logExpr, LogOpt logPlan,
                                   PhyOpt[] phyChildren, boolean makeCorr)
  {
    this.logPlan     = logPlan;
    this.logExpr     = logExpr;
    this.phyChildren = phyChildren;
    this.makeCorr    = makeCorr;
  }
}
