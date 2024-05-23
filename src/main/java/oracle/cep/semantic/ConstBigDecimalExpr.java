package oracle.cep.semantic;

import java.math.BigDecimal;
import java.util.List;

import oracle.cep.common.Datatype;

/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ConstBigDecimalExpr.java /main/1 2009/11/09 10:10:57 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/17/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ConstBigDecimalExpr.java /main/1 2009/11/09 10:10:57 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */



public class ConstBigDecimalExpr extends Expr {

  private BigDecimal value;
 
  @Override
  public ExprType getExprType()
  {
    return ExprType.E_CONST_VAL;
  }

  @Override
  public Datatype getReturnType()
  {
    return dt;
  }

  /**
   * Constructor
   * @param value double constant value
   */
  public ConstBigDecimalExpr(BigDecimal value)
  {
    this.value = value;
    this.dt = Datatype.BIGDECIMAL;
    setName(String.valueOf(value), false);
  }
  
  /**
   * get the double constant value
   * @return double constant value
   */
  public BigDecimal getValue()
  {
    return value;
  }
  
  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    return;
  } 
  
  @Override
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ConstBigDecimalExpr other = (ConstBigDecimalExpr) otherObject;
    return (value == other.getValue());
  }
  
  // toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }
}


