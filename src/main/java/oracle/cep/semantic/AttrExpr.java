/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/AttrExpr.java /main/6 2012/05/02 03:06:01 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Class representing an attribute reference expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/05/12 - added getRewrittenExprForGroupBy method
    vikshukl    01/31/12 - group by expr.
    parujain    11/09/07 - External source
    parujain    10/25/07 - db join
    rkomurav    06/25/07 - cleanup
    rkomurav    05/11/07 - add isClassB
    rkomurav    05/28/07 - add equals method
    parujain    10/13/06 - passing returntype to Logical
    najain      08/28/06 - expr is a abstract class
    anasrini    02/27/06 - fix xml closing in toString 
    anasrini    02/26/06 - implement toString 
    anasrini    02/22/06 - add constructor etc. 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/AttrExpr.java /main/6 2012/05/02 03:06:01 pkali Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

/**
 * Class representing an attribute reference expression
 *
 * @since 1.0
 */

public class AttrExpr extends Expr {

  private Attr     attr;
 
  // Ex: S.c1 is the actual  name of A.c1 where A is alias of S
  private String   actualName;

  /**
   * Constructor
   * @param attr the semantic representation of the attribute being referenced
   * @param datatype the datatype of the referenced attribute
   */
  public AttrExpr(Attr attr, Datatype datatype) {
    this.attr = attr;
    this.dt = datatype;
    this.actualName = null;
    this.isExternal = false;
  }
 
  public void setActualName(String actual)
  {
    this.actualName = actual;
  }

  public String getActualName()
  {
    return this.actualName;  
  }
  
  public ExprType getExprType() {
    return ExprType.E_ATTR_REF;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Get the semantic representation of the attribute being referenced
   * @return the semantic representation of the attribute being referenced
   */
  public Attr getAttr() {
    return attr;
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    return;
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    for (int i=0; i < gbyExprs.length; i++)
    {
      if (this.equals(gbyExprs[i])) 
        return this;
      //GroupByExpr wrapper not required since it is attribute not expr
    }
    return null;
  }
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    if(attr.getSemAttrType() == type)
    {
      //if(!attrs.contains(attr))
        attrs.add(attr);
    }
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    AttrExpr other = (AttrExpr)otherObject;
    return (attr.equals(other.attr));
  }
  
  // toString
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("<AttrExpr datatype=\"" + dt + "\" >");
    sb.append(attr.toString());
    sb.append("</AttrExpr>");

    return sb.toString();
  }
}
