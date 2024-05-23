/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/OuterRelationSpec.java /main/1 2009/06/04 17:45:06 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/18/09 - from clause outer relation spec
    parujain    05/18/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/OuterRelationSpec.java /main/1 2009/06/04 17:45:06 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.OuterJoinType;

public class OuterRelationSpec extends RelationSpec
{
  RelationSpec  leftSpec;

  OuterJoinType  outerJoinType;
  
  BExpr          condition;


  public OuterRelationSpec(RelationSpec left, OuterJoinType type,
                           int rightId, BExpr condition)
  {
    super(rightId);
    this.leftSpec = left;
    this.outerJoinType = type;
    this.condition = condition;
    setOuterRelation(true);
  }

  public OuterJoinType getOuterJoinType()
  {
    return this.outerJoinType;
  }

  public RelationSpec getLeftSpec()
  {
    return this.leftSpec;
  }

  public int getRightId()
  {
    return this.varId;
  }
  
  public BExpr getCondition()
  {
    return this.condition;
  }
  
  public String toString()
  {
    return "leftRelSpec: " + leftSpec +
           " "+ outerJoinType +
           " rightRelSpec: " + varId;
  }
}
