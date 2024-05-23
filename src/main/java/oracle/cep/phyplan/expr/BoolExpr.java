/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/BoolExpr.java /main/2 2011/06/02 13:25:39 mjames Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Boolean Expression Physical Operator Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    10/31/06 - Base/Complex Boolean Exprs
 rkomurav    10/10/06 - add equals method
 parujain    09/28/06 - is null implementation
 rkomurav    08/22/06 - XML_Visualiser
 najain      04/20/06 - add setters
 anasrini    03/30/06 - add getter methods 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: BoolExpr.java 31-oct-2006.15:24:45 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import oracle.cep.extensibility.expr.BooleanExpression;
import oracle.cep.extensibility.expr.ExprKind;

/**
 * Boolean Expression Logical Operator Class Definition
 */
public abstract class BoolExpr
    extends Expr
    implements BooleanExpression
{
  
  
  public BoolExpr(ExprKind bool_expr)
	{
		super(bool_expr);
	}

	
}
