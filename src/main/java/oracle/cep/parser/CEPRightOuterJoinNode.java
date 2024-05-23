/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRightOuterJoinNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
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
    parujain    12/21/09 - qcxml support
    parujain    05/18/09 - right node of outer join
    parujain    05/18/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRightOuterJoinNode.java /main/2 2009/12/29 20:26:09 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import oracle.cep.common.OuterJoinType;

public class CEPRightOuterJoinNode extends CEPRelationNode {
  protected CEPRelationNode rightNode;

  protected OuterJoinType outerJoinType;
  
  protected CEPBooleanExprNode condition;

  public CEPRightOuterJoinNode( CEPRelationNode right, 
                                  CEPBooleanExprNode cond, OuterJoinType type)
  {
    this.rightNode = right;
    this.condition = cond;
    this.outerJoinType = type;
    setStartOffset(right.getStartOffset());
    setEndOffset(cond.getEndOffset());
  }

  /**
   * Get the condition
   * @return the condition
   */
  public CEPBooleanExprNode getCondition() {
    return condition;
  }

  /**
   * Get the right relation
   * @return the right relation
   */
  public CEPRelationNode getRightNode() {
    return rightNode;
  }

  /**
   * Get the outer join type
   * 
   * @return the outer join type
   */
  public OuterJoinType getOuterJoinType() {
    return outerJoinType;
  }

  @Override
  public int toQCXML(StringBuffer queryXml, int operatorID)
		throws UnsupportedOperationException {
    throw new UnsupportedOperationException("ANSI Outer Joins Not supported ");
  }


}
