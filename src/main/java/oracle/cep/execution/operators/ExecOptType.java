/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecOptType.java /main/17 2012/07/16 08:14:06 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ExecOptType in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    udeshmuk  07/07/12 - add type for buffer operator
    sbishnoi  05/29/12 - adding new operator for slide
    anasrini  03/20/11 - add EXCHANGE
    sbishnoi  03/17/11 - adding new execution operator
    sbishnoi  12/31/09 - adding a type for table function operator
    sbishnoi  02/11/09 - adding new operator OrderByTop
    parujain  07/07/08 - Value based windows
    najain    12/14/07 - xmltable support
    sbishnoi  09/26/07 - add EXEC_MINUS
    hopark    07/03/07 - fix bug
    parujain  06/28/07 - order by support
    hopark    06/07/07 - add name
    rkomurav  05/15/07 - add classb
    najain    03/12/07 - bug fix
    rkomurav  03/02/07 - add pattern type
    najain    05/17/06 - view support 
    skaluska  03/14/06 - query manager 
    skaluska  02/15/06 - Creation
    skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecOptType.java /main/17 2012/07/16 08:14:06 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution.operators;

/**
 * @author skaluska
 *
 */
public enum ExecOptType
{
    EXEC_BIN_JOIN("binjoin"),
    EXEC_BIN_STREAM_JOIN("binstreamjoin"),
    EXEC_DISTINCT("distinct"),
    EXEC_DSTREAM("dstream"),
    EXEC_EXCEPT("except"),
    EXEC_GROUP_AGGR("groupaggr"),
    EXEC_ISTREAM("istream"),
    EXEC_OUTPUT("output"),
    EXEC_PARTN_WIN("partitionwin"),
    EXEC_PROJECT("project"),
    EXEC_RANGE_WIN("rangewin"),
    EXEC_VALUE_WIN("valuewin"),
    EXEC_RELN_SOURCE("relsrc"),
    EXEC_ROW_WIN("rowwin"),
    EXEC_RSTREAM("rstream"),
    EXEC_SELECT("select"),
    EXEC_SINK("sink"),
    EXEC_STREAM_SOURCE("strmsrc"),
    EXEC_VIEW_STRM_SRC("viewstrmsrc"),
    EXEC_VIEW_RELN_SRC("viewrelnsrc"),
    EXEC_UNION("union"),
    EXEC_PATTERN_STRM("patternstrm"),
    EXEC_PATTERN_STRM_CLASSB("patternstrmb"),
    EXEC_ORDER_BY("orderby"),
    EXEC_ORDER_BY_TOP("orderbytop"),
    EXEC_MINUS("minus"),
    EXEC_XMLTABLE("xmltable"),
    EXEC_TABLE_FUNC_SRC("tablefunctionsrc"),
    EXEC_VARIABLE_RANGE_WIN("variablerangewin"),
    EXEC_EXCHANGE("exchange"),
    EXEC_SLIDE("slide"),
    EXEC_BUFFER("buffer");
    
    String m_name;
    
    ExecOptType(String name) 
    {
      m_name = name;
    }

    public String getName() {return m_name;}
}
