/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/BoolExpr.java /main/3 2009/03/04 20:01:25 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Boolean Expression Logical Operator Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    02/20/09 - outerjoin for external relation
 parujain    12/18/07 - outerjoin with external relations
 parujain    10/31/06 - Separate Base/Complex BoolExprs
 parujain    10/13/06 - getting returntype from Semantic
 rkomurav    10/10/06 - expr in aggr
 parujain    09/28/06 - is null implementation
 najain      05/30/06 - add check_reference 
 anasrini    03/31/06 - 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/BoolExpr.java /main/3 2009/03/04 20:01:25 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;


/**
 * Boolean Expression Logical Operator Class Definition
 */
public abstract class BoolExpr extends Expr {

  public abstract boolean isValidOuterJoin();
  
  public abstract boolean isOuterJoin();

}
