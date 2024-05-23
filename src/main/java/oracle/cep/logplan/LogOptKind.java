/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptKind.java /main/13 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/25/12 - adding new operator kind for slide without window
    vikshukl    08/25/11 - subquery support
    anasrini    07/07/11 - XbranchMerge anasrini_bug-12640265_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    07/06/11 - add Kind for TABLE FUNCTION
    anasrini    04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
    anasrini    03/18/11 - add EXCHANGE operator
    parujain    07/01/08 - value based windows
    najain      12/07/07 - 
    sbishnoi    09/26/07 - add LO_MINUS
    parujain    06/27/07 - order by support
    parujain    03/07/07 - Extensible Windows
    rkomurav    02/26/07 - rename pattern to pattern-strm
    rkomurav    02/20/07 - add patternkind
    najain      02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptKind.java /main/13 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

public enum LogOptKind {
  LO_STREAM_SOURCE,
  LO_RELN_SOURCE,
  LO_ROW_WIN,
  LO_RANGE_WIN,
  LO_EXTENSIBLE_WIN,
  LO_NOW_WIN,
  LO_PARTN_WIN,
  LO_VALUE_WIN,
  LO_SELECT,
  LO_PROJECT,
  LO_CROSS,
  LO_GROUP_AGGR,
  LO_DISTINCT,
  LO_ISTREAM,
  LO_DSTREAM,
  LO_RSTREAM,
  LO_STREAM_CROSS,
  LO_UNION,
  LO_EXCEPT,
  LO_PATTERN_STRM,
  LO_ORDER_BY,
  LO_MINUS,
  LO_XMLTABLE,
  LO_TABLE_FUNCTION,
  LO_EXCHANGE,
  LO_SUBQUERY_SOURCE,
  LO_SLIDE;
}

