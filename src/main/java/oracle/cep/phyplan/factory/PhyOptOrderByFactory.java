/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptOrderByFactory.java /main/5 2009/03/31 02:50:09 sbishnoi Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/24/09 - interpret output attributes
    sbishnoi    03/06/09 - adding support for partition by attributes in order
                           by clause
    sbishnoi    02/10/09 - support for order by top
    hopark      10/09/08 - remove statics
    parujain    06/28/07 - Order by operator factory
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptOrderByFactory.java /main/5 2009/03/31 02:50:09 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.ArrayList;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptOrderBy;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOrderBy;
import oracle.cep.phyplan.PhyOptOrderByTop;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

class PhyOptOrderByFactory extends PhyOptFactory {

  @Override
  PhyOpt newPhyOpt(Object ctx) throws CEPException {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOpt            phyOrder;
    LogOptOrderBy     logOrder;
    LogOpt            logop;
    PhyOpt[]          phyChildren;
    PhyOpt            phyChild;
    // expressions corresponding to order by expressions
    Expr[]            orderExprs;    

    logop       = ctx1.getLogPlan();
    logOrder    = (LogOptOrderBy)logop;
    phyChildren = ctx1.getPhyChildPlans();
    
    assert logop != null;
    assert logop instanceof LogOptOrderBy : logop.getClass().getName();
    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;
    
    
    // interpret order-by expressions
    ArrayList<oracle.cep.logplan.expr.ExprOrderBy> logOrderByExprs
      = logOrder.getOrderByExprs();
    
    int numOrderByExprs = logOrderByExprs.size();
    
    // initialize the array for physical order by expression
    orderExprs = new Expr[numOrderByExprs];
    
    for(int i=0; i < numOrderByExprs; i++)
    {
      oracle.cep.logplan.expr.Expr logExpr = logOrderByExprs.get(i);
      Expr orderExp = LogPlanExprFactory.getInterpreter(logExpr,
                          new LogPlanExprFactoryContext(logExpr, logop));
      orderExprs[i] = orderExp;
    }
    
    // if there is no specification of number of rows
    //   return simple order-by
    // else return order-by with support of top
    
    if(logOrder.getNumOrderByRows() <= 0)
      phyOrder = new PhyOptOrderBy(ec, phyChild, orderExprs, logOrder);
    else
    {
      phyOrder = new PhyOptOrderByTop(ec, 
                                      phyChild, 
                                      orderExprs, 
                                      logOrder);
      
      // Process partition by attributes and update PhyOptOrderByTop
      ArrayList<oracle.cep.logplan.attr.Attr> partitionByAttrs
        = logOrder.getPartitionByAttrs();      
      Attr phyPartitionAttr = null;
       
      // transform each logical partition attribute to physical partitionAttr
      if(partitionByAttrs != null)
      {
        for(int j = 0; j < partitionByAttrs.size(); j++)
        {        
          phyPartitionAttr 
           = LogPlanAttrFactory.getInterpreter(logop, partitionByAttrs.get(j));
          ((PhyOptOrderByTop)phyOrder).addPartitionByAttr(phyPartitionAttr);
        }
      }
    }
    return phyOrder;
  }
  
}
