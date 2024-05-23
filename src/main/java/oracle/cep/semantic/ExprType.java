/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ExprType.java /main/9 2012/05/02 03:06:03 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of type of expressions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    02/06/12 - group by expr
    skmishra    06/12/08 - adding xmlconcat
    parujain    04/23/08 - XMLElement support
    parujain    06/26/07 - orderby Expr
    rkomurav    05/28/07 - remove funcExprAggr
    parujain    03/29/07 - Case Expressions
    parujain    08/10/06 - Timestamp datatype
    anasrini    07/10/06 - support for user defined aggregations 
    anasrini    06/13/06 - support for function expressions 
    anasrini    03/30/06 - add BOOLEAN 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ExprType.java /main/9 2012/05/02 03:06:03 pkali Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

/**
 * Enumeration of type of expressions.
 *
 * @since 1.0
 */

public enum ExprType {
  E_CONST_VAL, 
  E_ATTR_REF, 
  E_COMP_EXPR, 
  E_FUNC_EXPR,
  E_BOOL_EXPR,
  E_TIME_EXPR,
  E_AGGR_EXPR,
  E_SEARCHED_CASE_EXPR,
  E_CASE_CONDITION_EXPR,
  E_SIMPLE_CASE_EXPR,
  E_CASE_COMPARISON_EXPR,
  E_ORDER_BY_EXPR,
  E_ELEMENT_EXPR,
  E_XML_CONCAT_EXPR,
  E_XML_PARSE_EXPR,
  E_XML_ATTR_EXPR,
  E_XMLFOREST_EXPR,
  E_XMLCOLATTVAL_EXPR,
  E_GROUP_BY_EXPR;
}

