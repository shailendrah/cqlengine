/* $Header: SemQueryExprFactoryContext.java 15-mar-2006.13:13:09 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 najain      03/15/06 - Creation
 */

/**
 *  @version $Header: SemQueryExprFactoryContext.java 15-mar-2006.13:13:09 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.semantic.Expr;
import oracle.cep.semantic.SemQuery;

/**
 * Context for logical operator expressions generation from semantic
 * query expressions
 *
 * @author najain
 */
public class SemQueryExprFactoryContext {
  Expr     expr;
  SemQuery query;

  public SemQueryExprFactoryContext(Expr expr, SemQuery query) {
    this.expr  = expr;
    this.query = query;
  }

  public Expr getExpr() {
    return expr;
  }
  
  public SemQuery getQuery() {
    return query;
  }
}
