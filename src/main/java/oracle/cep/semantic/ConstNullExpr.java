/* $Header: ConstNullExpr.java 10-jan-2008.22:51:39 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/10/08 - Creation
 */

/**
 *  @version $Header: ConstNullExpr.java 10-jan-2008.22:51:39 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;
import oracle.cep.common.Datatype;

/**
 * Class representing Null constant/literal expression
 */

public class ConstNullExpr extends Expr {

  public ConstNullExpr()
  {
    this.dt = Datatype.UNKNOWN;
    setName("NULL", false);
  }
  
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
  
  @Override
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;
    if (otherObject == null)
      return false;
    if (getClass() != otherObject.getClass())
      return false;
    return true;
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
}