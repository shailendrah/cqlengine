/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPOtherAggrExprNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    skmishra    02/19/09 - removing toaggrxml, inherits from super
    skmishra    01/29/09 - override toVisualizerString()
    parujain    08/15/08 - error offset
    udeshmuk    04/15/08 - support for aggregate distinct.
    mthatte     04/07/08 - adding toString()
    udeshmuk    09/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPOtherAggrExprNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.AggrFunction;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
* Parse tree node corresponding to SUM, MAX, MIN, AVG and COUNT aggregate functions.
*
*/

public class CEPOtherAggrExprNode extends CEPAggrExprNode {

  /** Is it Aggregate distinct e.g. SUM (distinct c1)*/
  protected boolean isDistinctAggr;

  /**
  *  Constructor for CEPOtherAggrExprNode
  *  @param aggrFn the aggregate function
  *  @param arg argument to the aggregate function
  */
  public CEPOtherAggrExprNode(AggrFunction aggrFn, CEPExprNode arg) {
    super(aggrFn, arg);
    this.isDistinctAggr = false;
  }
  
  /**
   * Constructor for CEPOtherAggrExprNode
   * @param aggrFn the aggregate function
   * @param arg    argument to the aggregate function
   * @param isDistinct check whether distinct is used inside aggregate function
   */
  public CEPOtherAggrExprNode(AggrFunction aggrFn, CEPExprNode arg, boolean isDistinctAggr)
  {
    super(aggrFn, arg);
    this.isDistinctAggr = isDistinctAggr;
  }

  /**
   * Getter for isDistinct
   * @return isDistinct
   *         true if distinct is used inside this aggregate 
   *         else false
   */
  @Override
  public boolean getIsDistinct()
  {
    return this.isDistinctAggr;
  }
  
  public String getExpression()
  {
    if (isDistinctAggr)
      return " distinct " + aggrFn.toString() + "(" + arg.toString() + ")";
    else
      return aggrFn.toString() + "(" + arg.toString() + ")";
  }

  @Override
  public boolean equals(Object obj)
  {
    if(!super.equals(obj))
      return false;
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPOtherAggrExprNode other = (CEPOtherAggrExprNode) obj;
    if (isDistinctAggr != other.isDistinctAggr)
      return false;
    return true;
  }

  public String toString()
  {
    if(isDistinctAggr)
    {
      if(alias == null)
        return " distinct " + aggrFn.toString() + "(" + arg.toString() + ")"; 
      else
        return " distinct " + aggrFn.toString() + "(" + arg.toString() + ") AS " + alias + " ";
    }
    else
      return super.toString();
  }
  
}
