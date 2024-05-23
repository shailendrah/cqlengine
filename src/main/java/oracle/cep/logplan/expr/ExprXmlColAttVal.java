/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXmlColAttVal.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      04/11/11 - override getAllReferencedAttrs()
    parujain    05/29/08 - xmlcolattval support
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: ExprXmlColAttVal.java 29-may-2008.11:53:36 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;
import oracle.cep.logplan.LogOpt;

public class ExprXmlColAttVal extends Expr implements Cloneable {

  private Expr[] colExprs;
  
  public ExprXmlColAttVal(Expr[] exprs, Datatype dt)
  {
    this.colExprs = exprs;
    setType(dt);
  }
  
  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }
  
  public Expr[] getColAttExprs()
  {
    return this.colExprs;
  }
  
  public int getNumColAttExprs()
  {
    return colExprs.length;
  }
  
  public boolean check_reference(LogOpt op) {
    for(int i=0; i<colExprs.length; i++)
    {
      if(!colExprs[i].check_reference(op))
        return false;
    }
    return true;
  }
  
  public ExprXmlColAttVal clone() throws CloneNotSupportedException {
    ExprXmlColAttVal expr = (ExprXmlColAttVal)super.clone() ;
    for(int i=0; i<this.getNumColAttExprs(); i++)
    {
      expr.colExprs[i] = (Expr)this.colExprs[i].clone();
    }
    return expr;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs) 
  {
    getAllReferencedAttrs(attrs, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams) 
  {
    for(int i=0; i<colExprs.length; i++)
      colExprs[i].getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprXmlColAttVal other = (ExprXmlColAttVal)otherObject;
    if(this.getNumColAttExprs() != other.getNumColAttExprs())
      return false;
    for(int i=0; i<this.getNumColAttExprs(); i++)
    {
      if(!this.colExprs[i].equals(other.colExprs[i]))
        return false;
    }
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalXmlColAttValExpression>");
    sb.append(super.toString());
    for(int j=0; j<getNumColAttExprs(); j++)
      sb.append(colExprs[j].toString());
    sb.append("</LogicalXmlColAttValExpression>");
    return sb.toString();
	  }
}
