/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXmlForest.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    parujain    06/03/08 - check_reference
    parujain    05/23/08 - xmlforest expr
    parujain    05/23/08 - Creation
 */

/**
 *  @version $Header: ExprXmlForest.java 03-jun-2008.11:59:12 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprXmlForest extends Expr implements Cloneable {

  private Expr[] forestExprs;
  
  public ExprXmlForest(Expr[] exprs, Datatype dt)
  {
    this.forestExprs = exprs;
    setType(dt);
  }
  
  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }
  
  public Expr[] getForestExprs()
  {
    return this.forestExprs;
  }
  
  public int getNumForestExprs()
  {
    return forestExprs.length;
  }
  
  public boolean check_reference(LogOpt op) {
    for(int i = 0; i < forestExprs.length; i++)
    {
      if(!forestExprs[i].check_reference(op))
        return false;
    }
    return true;
  }
  
  public ExprXmlForest clone() throws CloneNotSupportedException {
    ExprXmlForest expr = (ExprXmlForest)super.clone() ;
    for(int i=0; i<this.getNumForestExprs(); i++)
    {
      expr.forestExprs[i] = (Expr)this.forestExprs[i].clone();
    }
    return expr;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs) {
    getAllReferencedAttrs(attrs, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams) 
  {
    for(int i = 0; i < forestExprs.length; i++)
      forestExprs[i].getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprXmlForest other = (ExprXmlForest)otherObject;
    if(this.getNumForestExprs() != other.getNumForestExprs())
      return false;
    for(int i = 0; i < this.getNumForestExprs(); i++)
    {
      if(!this.forestExprs[i].equals(other.forestExprs[i]))
        return false;
    }
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalXmlForestExpression>");
    sb.append(super.toString());
    for(int j = 0; j < getNumForestExprs(); j++)
      sb.append(forestExprs[j].toString());
    sb.append("</LogicalXmlForestExpression>");
    return sb.toString();
	  }
}
