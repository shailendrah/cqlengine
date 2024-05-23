/* $Header: PhyPlanExprFactory.java 04-apr-2006.13:31:54 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      04/04/06 - cleanup
    najain      03/01/06 - Creation
 */

/**
 *  @version $Header: PhyPlanExprFactory.java 04-apr-2006.13:31:54 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;

/**
 * PhyOptFactory
 *
 * @author najain
 */
public abstract class PhyPlanExprFactory
{
  public abstract Expr newExpr(Object ctx);
}



