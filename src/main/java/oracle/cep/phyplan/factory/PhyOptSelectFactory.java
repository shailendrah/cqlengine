/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptSelectFactory.java /main/6 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
 All rights reserved. */

/*
   DESCRIPTION
    Factory for physical representation of the select operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    anasrini    09/16/08 - pass equiv logopt to constructor
    sbishnoi    07/13/07 - cleanup of ViewSrc(strm|reln) operator from phyplan
    rkomurav    06/18/07 - cleanup
    rkomurav    03/06/07 - restructure logexprfactory
    najain      04/06/06 - cleanup
    anasrini    04/06/06 - constructor cleanup 
    najain      04/04/06 - cleanup
    anasrini    03/30/06 - support for select operator 
    najain      03/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptSelectFactory.java /main/6 2008/10/24 15:50:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptSelect;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptSelect;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;

/**
 * Factory for physical representation of the select operator
 *
 * @author najain
 * @since 1.0
 */
class PhyOptSelectFactory extends PhyOptFactory {

  PhyOpt newPhyOpt(Object ctx) throws CEPException {
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    LogPlanExprFactoryContext        phyExprCtx;
    PhyOptSelect                     phySelect;
    PhyOpt[]                         phyChildren;
    PhyOpt                           phyChild;
    BoolExpr                         phyPred;
    Expr                             phyExpr;
    LogOptSelect                     logSelect;
    LogOpt                           logop;
    oracle.cep.logplan.expr.BoolExpr logPred;

    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptSelect : logop.getClass().getName();
    logSelect = (LogOptSelect)logop;

    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;

    logPred    = logSelect.getBExpr();
    phyExprCtx = new LogPlanExprFactoryContext(logPred, logSelect);
    phyExpr    = LogPlanExprFactory.getInterpreter(logPred, phyExprCtx);
    assert phyExpr != null;
    assert phyExpr instanceof BoolExpr;
    phyPred    = (BoolExpr)phyExpr;

    // If the physical plan corresponding to child of this select operator
    // (S1) is a (physical) select operator (S2), we just add the
    // predicate of logical select operator (S1) to (S2). In logical
    // plans the select operators only contain atomic predicates to help
    // us push down select predicates as much as possible. In physical
    // plans select operators can contain a conjunction of atomic
    // predicates. We get better efficiency by packing as much
    // functionality into a single physical select operator.
    // Note: If child Select operator is a view, we should create new 
    // select operator
    
    if (phyChild.getOperatorKind() == PhyOptKind.PO_SELECT 
        && !phyChild.getIsView() ) 
    {
      assert phyChild instanceof PhyOptSelect : phyChild.getClass().getName();
      phySelect = (PhyOptSelect)phyChild;
    }
    else {
      phySelect = new PhyOptSelect(ec, phyChild, logop);
    }
    assert phySelect != null;

    // Add the atomic predicate
    phySelect.addAtomicPred(phyPred);
    
    return phySelect;
  }

}
