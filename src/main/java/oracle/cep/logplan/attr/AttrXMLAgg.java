/* $Header: AttrXMLAgg.java 30-jun-2008.23:36:01 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/04/08 - Creation
 */

/**
 *  @version $Header: AttrXMLAgg.java 30-jun-2008.23:36:01 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.attr;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprOrderBy;
  
public class AttrXMLAgg extends AttrAggr {
  
  /**
   * The order by expressions associated with xmlagg call
   */
  private ExprOrderBy[] orderByExprs;

  public AttrXMLAgg(Expr[] expr, BaseAggrFn function, boolean isDistinct, Datatype dt,
                    ExprOrderBy[] orderByExprs)
  {
    super(expr, function, isDistinct, dt);
    this.orderByExprs = orderByExprs;
  }
   
  public ExprOrderBy[] getOrderByExprs()
  {
    return this.orderByExprs;
  }

  public void setOrderByExprs(ExprOrderBy[] exprs)
  {
    this.orderByExprs = exprs;
  } 
  
  public boolean equals(Object otherObject)
  {
    boolean isEqual = super.equals(otherObject);
    return compareOrderByExprs(otherObject, isEqual);
  }
  
  public boolean isSame(Attr input)
  {
    boolean isSame = super.isSame(input);
    return compareOrderByExprs(input, isSame);
  }
    
  protected boolean compareAttr(Attr attr)
  {   
    boolean isEqual = super.compareAttr(attr);
    return compareOrderByExprs(attr, isEqual);
  }
  
  private boolean compareOrderByExprs(Object otherObject, boolean isEqual)
  {
    if(isEqual)
    {
      AttrXMLAgg other = (AttrXMLAgg) otherObject;
      if(orderByExprs != null && other.orderByExprs != null)
      {
        if(orderByExprs.length != other.orderByExprs.length)
          return false;
        for(int i=0; i < orderByExprs.length; i++)
          isEqual = isEqual && (orderByExprs[i].equals(other.orderByExprs[i])); 
      }
      else 
        isEqual = isEqual && (other.orderByExprs == null && this.orderByExprs == null);
      return isEqual;
    }
    else 
      return false;
  }
}
