/* $Header: XMLAggExpr.java 30-jun-2008.23:36:28 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk   05/30/08 - xmlagg support.
 mthatte    05/21/08 - Creation
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.BuiltInAggrFn;
import oracle.cep.common.Datatype;

/**
 *  @version $Header: XMLAggExpr.java 30-jun-2008.23:36:28 udeshmuk Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class XMLAggExpr extends AggrExpr {

  private OrderByExpr[] orderByExprs;

  public XMLAggExpr(BuiltInAggrFn fn,Datatype dt, Expr argExpr,
                    OrderByExpr[] orderByExprs) 
  {
    super(fn, dt, argExpr);
    this.orderByExprs = orderByExprs;
  }

  public OrderByExpr[] getOrderByExprs() 
  {
    return orderByExprs;
  }

  public void setOrderByExprs(OrderByExpr[] orderByExprs) 
  {
    this.orderByExprs = orderByExprs;
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrAndOrderByExprs)
  {
    if(includeAggrAndOrderByExprs)
    {
      super.getAllReferencedAttrs(attrs,type,true);
      for(int i=0; i < orderByExprs.length ; i++)
        orderByExprs[i].getAllReferencedAttrs(attrs, type, includeAggrAndOrderByExprs);
    }
    else
      return;
  }
	
  public boolean equals(Object otherObject) 
  {
    boolean isEqual = super.equals(otherObject);
    if(isEqual)
    {
      XMLAggExpr other = (XMLAggExpr)otherObject;
      
      if(orderByExprs != null && other.orderByExprs != null)
      {
        if(orderByExprs.length != other.orderByExprs.length) 
          return false;
        for (int i=0; i<orderByExprs.length; i++)
          isEqual = isEqual && this.orderByExprs[i].equals(other.orderByExprs[i]);
      }
      else
        isEqual = isEqual && (other.orderByExprs == null && this.orderByExprs==null);
      return isEqual;
    }
    else 
      return false;
  }

}
