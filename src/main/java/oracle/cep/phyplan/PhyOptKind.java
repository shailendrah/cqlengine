/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptKind.java /main/15 2012/07/16 08:14:06 udeshmuk Exp $ */

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
 udeshmuk    07/06/12 - add buffer operator
 sbishnoi    05/28/12 - support slide without window
 vikshukl    08/25/11 - subquery support
 anasrini    03/20/11 - add PO_EXCHANGE
 sbishnoi    12/26/09 - adding kind for table function operator
 sbishnoi    02/10/09 - adding operator PhyOptOrderByTo[
 parujain    07/07/08 - value based windows
 najain      12/11/07 - 
 sbishnoi    09/26/07 - add PO_MINUS
 hopark      07/03/07 - add getName
 parujain    06/28/07 - order by support
 rkomurav    05/14/07 - classB pattern
 rkomurav    02/27/07 - add patternstream
 najain      05/17/06 - add view source 
 najain      04/06/06 - cleanup
 najain      03/24/06 - cleanup
 najain      02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptKind.java /main/15 2012/07/16 08:14:06 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

public enum PhyOptKind {
  PO_SELECT("select"), PO_PROJECT("project"), PO_JOIN("binjoin"), /** Relation-relation join */
  PO_STR_JOIN("binstreamjoin"), /** Stream-relation join */
  PO_JOIN_PROJECT("joinproject"), /** Combination of a join followed by project */
  PO_STR_JOIN_PROJECT("strjoinproject"), /** Combination of a str-join followed by project */
  PO_GROUP_AGGR("groupaggr"), /** Group-by aggregation */
  PO_DISTINCT("distinct"), PO_ROW_WIN("rowwin"), /** Row Window */
  PO_RANGE_WIN("rangewin"), /** Range Window */
  PO_VALUE_WIN("valuewin"), /** Value window */
  PO_PARTN_WIN("partitionwin"), /** Partitioned Window */
  PO_ISTREAM("istream"), PO_DSTREAM("dstream"), PO_RSTREAM("rstream"), PO_UNION("union"), PO_EXCEPT("except"), /** Anti-semijoin */
  PO_STREAM_SOURCE("strmsrc"), /** Source for a base stream */
  PO_RELN_SOURCE("relsrc"), /** Base relation source */
  PO_QUERY_SOURCE("querysrc"), /** "No-op" operator that sits on top of query views - 
   other queries that use the output of this query 
   read from the query source operator. This is a 
   dummy operator found at the metadata level */
  PO_OUTPUT("output"), /** Output operator that interfaces with the external world */
  PO_SINK("sink"), /** Sink operator to consume tuples from unused ops */
  PO_SS_GEN("sysstrmgen"), /** System stream generator */
  PO_VIEW_STRM_SRC("viewstrmsrc"),
  PO_VIEW_RELN_SRC("viewrelnsrc"),
  PO_PATTERN_STRM("patternstrm"), /** Pattern Stream Operator */
  PO_PATTERN_STRM_CLASSB("patternstrmb"), /** Pattern Stream Operator for ClassB patterns */
  PO_ORDER_BY("orderby"),
  PO_MINUS("minus"),
  PO_XMLTABLE("xmltable"),
  PO_ORDER_BY_TOP("orderbytop"),
  PO_TABLE_FUNCTION("tablefunction"), /** operator for table function source*/
  PO_EXCHANGE("exchange"), /** operator for exchange parallelism support */
  PO_SUBQUERY_SRC("subquerysrc"), /** operator for subquery source */
  PO_SLIDE("slide"), /** operator for slide */
  PO_BUFFER("buffer") /** operator for buffer */;
  
  private String name;
  
  /**
   * Constructor
   * @param name the meaningful expanded name of the operator
   */
  PhyOptKind(String name) {
    this.name = name;
  }
  
  /**
   * Get the meaningful name of the operator.
   */
  public String getName()
  {
    return name;
  }

}
