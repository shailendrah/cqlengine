/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/expr/ExprKind.java /main/1 2011/06/02 13:25:39 mjames Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of kind of expressions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    06/18/08 - 
    mthatte     05/20/08 - adding xparse_expr
    mthatte     04/30/08 - adding Xml_concat
    parujain    04/25/08 - XMLElement
    najain      10/31/07 - 
    parujain    06/28/07 - orderby expr
    parujain    03/30/07 - Case conditions
    parujain    10/31/06 - Complex/Base Boolean Exprs
    rkomurav    10/10/06 - add equals method
    najain      04/27/06 - add user-defined functions 
    anasrini    03/31/06 - add BOOL_EXPR 
    anasrini    03/14/06 - Creation
    anasrini    03/14/06 - Creation
    anasrini    03/14/06 - Creation
 */

/**
 *  @version $Header: ExprKind.java 18-jun-2008.11:33:47 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.extensibility.expr;

/**
 * Enumeration of kind of expressions
 *
 * @since 1.0
 */

public enum ExprKind {
  CONST_VAL, ATTR_REF, COMP_EXPR, BASE_BOOL_EXPR, COMP_BOOL_EXPR, USER_DEF, SEARCH_CASE, CASE_CONDITION, SIMPLE_CASE, CASE_COMPARISON, ORDER_BY_EXPR, XML_CONCAT_EXPR, XML_PARSE_EXPR, XQRY_FUNC, XMLELEMENT_EXPR, XMLATTR_EXPR, XMLFOREST_EXPR, XMLCOLATTVAL_EXPR;
}
