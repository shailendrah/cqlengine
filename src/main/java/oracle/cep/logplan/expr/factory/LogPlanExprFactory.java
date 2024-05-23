/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    03/16/09 - stateless factory
    najain      03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;

/**
 * LogPlanExprFactory: Factory for converting semantic query expressions to
 * logical operator expressions.
 *
 * @author najain
 */
public abstract class LogPlanExprFactory
{
  public abstract Expr newExpr(Object ctx);
}
