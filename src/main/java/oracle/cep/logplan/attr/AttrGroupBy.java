/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrGroupBy.java /main/2 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/13/12 - equivalence with Attr
    pkali       03/29/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrGroupBy.java /main/2 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.attr;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.common.Datatype;

/**
 * GroupBy Attribute Class definitions used by groupby expression
 */
public class AttrGroupBy extends Attr
{
  /** Expression Parameter */
  private Expr expr;
  
  public AttrGroupBy(Expr expr, Datatype dt)
  {
    this.expr = expr; 
    this.dt = dt;
    attrKind = AttrKind.GROUPBY;
  }

  public Expr getExpr() 
  {
    return expr;
  }
  
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    /**
     * For handling group by expressions, AttrGroupBy is created.
     * An instance of AttrGroupBy is created for all entries in GroupBy clause.
     * e.g. group by c1+3, c3
     * Here two AttrGroupBy is created. 
     * Since c3 is present in group by clause and so is already in the outAttrs
     * , we won't be adding it again while copying attrs from input.
     * The GroupAggr operator above project while converting the logical attr 
     * for c3 would therefore need to match it against the AttrGroupBy instance
     * containing c3.
     */
    if(otherObject instanceof Attr)
    {
      Attr argAttr = (Attr) otherObject;
      if(this.expr instanceof ExprAttr)
      {
        ExprAttr exprAttr = (ExprAttr) this.expr;
        if(exprAttr.getAValue().equals(argAttr))
          return true;
      }        
    }
    
    if (getClass() != otherObject.getClass())
      return false;

    AttrGroupBy other = (AttrGroupBy) otherObject;
    return (this.getExpr().equals(other.getExpr()));
    
  }

  public boolean isSame(Attr input)
  {
    if (!super.isSame(input))
      return false;

    AttrGroupBy inp = (AttrGroupBy) input;

    return this.equals(inp);
  }

  public boolean check_reference(LogOpt op)
  {
    for (int i = 0; i < op.getNumOutAttrs(); i++)
    {
      Attr attr = op.getOutAttr(i);
      if (attr instanceof AttrGroupBy)
      {
        if(compareAttr(attr))
          return true;
      }
    }
    return false;
  }

  protected boolean compareAttr(Attr attr)
  {
    if( attr instanceof AttrGroupBy)
    {
      AttrGroupBy attrGroupBy = (AttrGroupBy) attr;
      return this.equals(attrGroupBy);
    }
    return false;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<GroupByAttribute>");
    sb.append(super.toString());
    sb.append("<GroupBy GroupBy=\"" + this.expr.toString() + "\" />");
    sb.append("</GroupByAttribute>");
    return sb.toString();
  }

}
