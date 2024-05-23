/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptProjectFactory.java /main/5 2009/04/27 11:34:36 sborah Exp $ */

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
    sborah      04/24/09 - pass phyChildren to constructor
    hopark      10/09/08 - remove statics
    rkomurav    06/18/07 - cleanup
    rkomurav    03/06/07 - restructure exprfactorycontext
    najain      04/06/06 - cleanup
    anasrini    04/06/06 - cleanup
    najain      04/04/06 - cleanup
    najain      03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptProjectFactory.java /main/5 2009/04/27 11:34:36 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptProject;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptProject;
import oracle.cep.exceptions.CEPException;

/**
 * LogOptProjectFactory
 *
 * @author najain
 */
class PhyOptProjectFactory extends PhyOptFactory {

  PhyOpt newPhyOpt(Object ctx) throws CEPException {

    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOptProject                    phyProject;
    PhyOpt[]                         phyChildren;
    PhyOpt                           phyChild;
    Expr[]                           projs;
    LogOptProject                    logProject;
    LogOpt                           logop;

    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptProject : logop.getClass().getName();
    logProject = (LogOptProject)logop;

    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;

    // project expressions
    int numProjExprs = logop.getNumOutAttrs();
    projs = new Expr[numProjExprs];

    for (int p = 0; p < numProjExprs; p++) {
      oracle.cep.logplan.expr.Expr logExpr = logProject.getBexpr().get(p);
      Expr projExpr = LogPlanExprFactory.getInterpreter(logExpr,
          new LogPlanExprFactoryContext(logExpr, logop, phyChildren));
      projs[p] = projExpr;
    }

    phyProject = new PhyOptProject(ec, phyChild, projs);
    return phyProject;
  }

}

