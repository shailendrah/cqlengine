/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprAttrFactory.java /main/6 2009/11/09 10:10:59 sborah Exp $ */

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
 sborah      10/12/09 - support for bigdecimal
 sborah      04/24/09 - add length info
 parujain    11/07/07 - on demand support
 rkomurav    06/18/07 - cleanup
 rkomurav    03/06/07 - restructure AttrFactory
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprAttrFactory.java /main/6 2009/11/09 10:10:59 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;

/**
 * PhyPlanExprAttrFactory
 *
 * @author najain
 */
public class PhyPlanExprAttrFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    LogOpt                       op;
    LogPlanExprFactoryContext    lpctx;
    PhyOpt[]                     inputOpts;
    
    int                          length;
    Expr                         phyExpr;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    op  = lpctx.getLogPlan();
    
    
    assert src instanceof oracle.cep.logplan.expr.ExprAttr;
    oracle.cep.logplan.expr.ExprAttr srcExpr = (oracle.cep.logplan.expr.ExprAttr) src;
    
    oracle.cep.logplan.attr.Attr lAttr = srcExpr.getAValue();
    
    Attr aValue = LogPlanAttrFactory.getInterpreter(op, lAttr, lpctx.isMakeCorr());
    
    int input  = aValue.getInput();
    int pos    = aValue.getPos();
    
    inputOpts = lpctx.getPhyChildren();
    
    if(inputOpts != null && input >= 0 )
    {
      // get the length information from its input
      length = inputOpts[input].getAttrLen(pos);
      phyExpr = new ExprAttr(aValue, src.getType(), length);
    }
    else
    {
      phyExpr = new ExprAttr(aValue, src.getType());
    }
     
    phyExpr.setExternal(src.isExternal());    
    ((ExprAttr)phyExpr).setActualName(srcExpr.getActualName());
    return phyExpr;
  }

}
