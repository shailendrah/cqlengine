/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprFactoryKind.java /main/14 2009/11/09 10:10:58 sborah Exp $ */

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
    sborah      06/23/09 - support for bigdecimal
    udeshmuk    06/05/08 - support for xmlagg.
    mthatte     05/19/08 - adding xmlparse
    mthatte     05/13/08 - adding xmltype
    mthatte     04/28/08 - adding xmlconcat
    parujain    04/25/08 - xmlelement
    udeshmuk    02/21/08 - add constTimestamp and constByte.
    udeshmuk    01/30/08 - support for double data type.
    najain      10/31/07 - add xmltype
    parujain    06/27/07 - order by support
    parujain    03/29/07 - support for CASE
    hopark      11/16/06 - add bigint datatype
    parujain    10/31/06 - Base or Complex Booleans
    parujain    10/09/06 - Interval datatype
    parujain    08/10/06 - Timestamp datatype
    anasrini    06/19/06 - support for user defined function 
    anasrini    03/29/06 - add BOOL factory kind 
    najain      03/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprFactoryKind.java /main/14 2009/11/09 10:10:58 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

/**
 * Enumeration of Logical layer expression types supported
 *
 * @since 1.0
 */

public enum LogPlanExprFactoryKind {
  INT, BIGINT, FLOAT, DOUBLE, BIGDECIMAL, CHAR, BYTE, TIMESTAMP, INTERVAL, XMLTYPE, BOOLEAN, ATTR, COMPLEX, AGGR, BASE_BOOL, COMPLEX_BOOL, FUNC, SEARCH_CASE, 
  CASE_CONDITION, SIMPLE_CASE, CASE_COMPARISON, ORDER_BY, XQRY_FUNC, XCONCAT_EXPR, XPARSE_EXPR, XMLELEMENT, XML_ATTR, XML_FOREST, XML_COLATTVAL, XMLAGG;
}

