/* $Header: BExpr.java 28-may-2007.23:51:45 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Post semantic analysis representation of a boolean expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    05/28/07 - 
    parujain    10/31/06 - Separate Boolean Exprs
    parujain    10/13/06 - passing returntype to Logical
    parujain    09/28/06 - is null implementation
    najain      08/28/06 - expr is a abstract class
    anasrini    03/30/06 - make it extend Expr 
    anasrini    02/27/06 - fix xml closing in toString 
    anasrini    02/26/06 - implement toString 
    anasrini    02/24/06 - add javadoc comments 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: BExpr.java 28-may-2007.23:51:45 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;


/**
 * Post semantic analysis representation of a boolean expression
 * <p>
 * This is a boolean expression that does not involve any logical operators
 * such as AND. This involves only comparison operators.
 *
 * @since 1.0
 */

public abstract class BExpr extends Expr {
  
  
}
