/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryRefNode.java /main/11 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    udeshmuk    05/12/11 - support for alter query start time DDL
    sborah      03/17/11 - add arith_expr to ordering constraints
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/01/10 - add variables and methods for setting propagation of
                           heartbeat
    sborah      06/16/10 - ordering constraint
    sbishnoi    08/25/09 - support of new syntax for batching output
    skmishra    02/04/09 - adding toQCXML
    sborah      11/24/08 - support for altering base timeline
    parujain    08/11/08 - 
    sbishnoi    11/05/07 - support for update semantics
    dlenkov     08/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryRefNode.java /main/8 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.Map;
import oracle.cep.common.Constants;
import oracle.cep.common.OrderingKind;

/**
 * Parse tree node corresponding to a DDL for a query reference
 *
 * @since 1.0
 */
public class CEPQueryRefNode implements CEPQueryNode {

  /** The name of the query */
  protected String name;

  /** query reference kind */
  protected CEPQueryRefKind refKind;

  /** string associated with a query like query destination */
  protected String value;
  
  /** boolean indicates whether we use update semantics for output*/
  private boolean isUpdateSemantics;
  
  /** flag indicates whether the output tuples will be batched on time-stamp*/
  private boolean batchOutputTuples;

  /** flag indicates that the output operator of the query should propagate heartbeat*/
  private boolean propagateHeartbeat;
  
  /**
   * boolean indicates whether to use milliseconds or nanoseconds 
   * as base timeline. 
   * true = use Millisecond as base timeline
   * false = use Nanosecond as base timeline
   * Default is Nanosecond
   **/
  private boolean isBaseTimelineMillisecond;
  
  /**
   * start time value for the query. 
   * Used only in testing archived relation based query 
   */
  private long startTimeValue;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Holds the query ordering constraint 
   */
  private OrderingKind orderingConstraint;
  
  private CEPExprNode parallelPartitioningExpr;
  /**
   * Constructor 
   *
   * @param name name of the query
   */
  public CEPQueryRefNode( CEPStringTokenNode nameToken)
  {
    this.name                 = nameToken.getValue();
    value                     = null;
    isUpdateSemantics         = false;
    isBaseTimelineMillisecond = false;
    batchOutputTuples         = false;
    propagateHeartbeat        = false;
    parallelPartitioningExpr  = null;   
    startTimeValue            = Long.MIN_VALUE;
    orderingConstraint        = OrderingKind.TOTAL_ORDER;
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return reference kind.
   */
  public CEPQueryRefKind getKind()
  {
    return refKind;
  }

  /**
   * Sets reference kind.
   */
  public void setKind( CEPQueryRefKind kind)
  {
    refKind = kind;
  }

  /**
   * @return Returns the associated string.
   */
  public String getValue()
  {
    return value;
  }

  /**
   * @return Sets the associated string.
   */
  public void setValue(CEPStringTokenNode valToken)
  {
    value = valToken.getValue();
    setEndOffset(valToken.getEndOffset());
  }
  
  /**
   * @return Returns the start time value for this query
   */
  public long getStartTimeValue()
  {
    return startTimeValue;
  }

  /**
   * @param starttime value for this query
   */
  public void setStartTimeValue(long starttime)
  {
    this.startTimeValue = starttime;
  }

  
  /**
   * @return isUpdateSemantics flag
   */
  public boolean getIsUpdateSemantics()
  {
    return isUpdateSemantics;
  }
  
  /**
   * Set isUpdateSemantics Flag
   * @param isUpdateSemantics
   */
  public void setIsUpdateSemantics(boolean isUpdateSemantics)
  {
    this.isUpdateSemantics = isUpdateSemantics;
  }
  
  /**
   * @return propagateHeartbeat flag
   */
  public boolean getPropagateHeartbeat()
  {
    return propagateHeartbeat;
  }

  /*
   * Set propagateHeartbeat flag
   * @param propagateHeartbeat
   */
  public void setPropagateHeartbeat(boolean propagateHeartbeat)
  {
    this.propagateHeartbeat = propagateHeartbeat;
  }
  
  /**
   * @return the isBaseTimelineMillisecond
   */
  public boolean getIsBaseTimelineMillisecond()
  {
    return isBaseTimelineMillisecond;
  }

  /**
   * @param isBaseTimelineMillisecond the isBaseTimelineMillisecond to set
   */
  public void setBaseTimelineMillisecond(boolean isBaseTimelineMillisecond)
  {
    this.isBaseTimelineMillisecond = isBaseTimelineMillisecond;
  }

  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }


  public int toQCXML(StringBuffer qXml, int operatorID) throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Query Ref Node does not support toXml()");
  }

  /**
   * @return the batchOutputTuples
   */
  public boolean isBatchOutputTuples()
  {
    return batchOutputTuples;
  }

  /**
   * @param batchOutputTuples the batchOutputTuples to set
   */
  public void setBatchOutputTuples(boolean batchOutputTuples)
  {
    this.batchOutputTuples = batchOutputTuples;
  }

  /**
   * @return the ordering constraint for the query
   */
  public OrderingKind getOrderingConstraint()
  {
    return orderingConstraint;
  }

  /**
   * Set the ordering constraint for the query
   * @param orderingConstraint The value of the ordering constraint.
   */
  public void setOrderingConstraint(OrderingKind orderingConstraint)
  {
    this.orderingConstraint = orderingConstraint;
  }

  public void setDestProperties(Map props)
  {
    if(props.containsKey(Constants.USE_UPDATE_SEMANTICS))
      setIsUpdateSemantics(true);
    if(props.containsKey(Constants.BATCH_OUTPUT))
      setBatchOutputTuples(true);
    if(props.containsKey(Constants.PROPAGATE_HB))
      setPropagateHeartbeat(true);
  }

  public CEPExprNode getParallelPartitioningExpr()
  {
    return parallelPartitioningExpr;
  }

  public void setParallelPartitioningExpr(CEPExprNode parallelPartitioningExpr)
  {
    this.parallelPartitioningExpr = parallelPartitioningExpr;
  }

  /**
   * Returns true if Query Reference DDL represented by this node contains
   * any logical CQL syntax.
   */
  @Override
  public boolean isLogical()
  {
    //TODO: In existing scope of implementation, There is no handling of
    // logical syntax in set operations.
    return false;
  }
}
