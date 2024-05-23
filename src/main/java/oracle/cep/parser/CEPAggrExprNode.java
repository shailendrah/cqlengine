/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAggrExprNode.java /main/8 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
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
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/19/09 - changes to xml format
    skmishra    01/29/09 - setting isAggr to true
    parujain    08/15/08 - error offset
    mthatte     04/07/08 - adding toString()
    udeshmuk    09/21/07 - removing constructor for star argument.
    rkomurav    12/13/06 - remove isStar on addition of COUNT_STAR
    rkomurav    09/19/06 - bug 5446939
    anasrini    08/30/06 - getter for isStarArg
    anasrini    02/24/06 - add getter methods 
    anasrini    12/21/05 - parse tree node corresponding to an aggregate 
                           expression 
    anasrini    12/21/05 - parse tree node corresponding to an aggregate 
                           expression 
    anasrini    12/21/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPAggrExprNode.java /main/7 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.AggrFunction;

/**
 * Parse tree node corresponding to an aggregate expression 
 *
 * @since 1.0
 */

public class CEPAggrExprNode extends CEPExprNode {

  /** The aggregate function */
  protected AggrFunction aggrFn;

  /** The aggregate function argument */
  protected CEPExprNode arg;

  /**
   * Constructor for CEPAggrExprNode
   * @param aggrFn the aggregate function
   * @param arg argument to the aggregate function
   */
  public CEPAggrExprNode(AggrFunction aggrFn, CEPExprNode arg) {
    this.aggrFn = aggrFn;
    this.arg    = arg;
    this.isAggr = true;
    
    if(arg != null)
    {
      setStartOffset(arg.getStartOffset());
      setEndOffset(arg.getEndOffset());
    }
  }
  
  // getter methods

  /**
   * Get the aggregation function
   * @return the aggregation function
   */
  public AggrFunction getAggrFunction() {
    return aggrFn;
  }

  /**
   * Get the argument to the aggregation function
   * @return the argument to the aggregation function
   */
  public CEPExprNode getExprNode() {
    return arg;
  }
  
  public String toSelectString()
  {
    if(arg!=null)
      return arg.toString();
    else
      return "";
  }
  
  public String toString()
  {
    if(alias == null)
      return " " + aggrFn.toString() + "(" + arg.toString() + ") ";
    else
      return " " + aggrFn.toString() + "(" + arg.toString() + ") " + " AS " + alias; 
  }

  public String getExpression()
  {
    return " " + aggrFn.toString() + "(" + arg.toString() + ") ";
  }

  public boolean getIsDistinct()
  {
    return false;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPAggrExprNode other = (CEPAggrExprNode) obj;
    if (aggrFn != other.aggrFn)
      return false;
    if (arg == null)
    {
      if (other.arg != null)
        return false;
    } else if (!arg.equals(other.arg))
      return false;
    return true;
  }
}
