/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXmlAttr.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    parujain    05/16/08 - evalname
    parujain    04/23/08 - XML Attribute Expr
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: ExprXmlAttr.java 03-jun-2008.13:42:50 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprXmlAttr extends Expr implements Cloneable {

  private Expr attrExpr;
  
  private String attrName;
  
  private Expr nameExpr;
 
  public ExprXmlAttr(String name, Expr attr, Datatype dt)
  {
    this.attrExpr = attr;
    this.attrName = name;
    this.nameExpr = null;
    setType(dt);
  }
  
  public ExprXmlAttr(Expr name, Expr attr, Datatype dt)
  {
    this.nameExpr = name;
    this.attrName = null;
    this.attrExpr = attr;
    setType(dt);
  }
  
  public boolean check_reference(LogOpt op) {
    if(nameExpr != null)
    {
      if(!nameExpr.check_reference(op))
        return false;
    }  
    return attrExpr.check_reference(op);
  }

  public Expr getNameExpr()
  {
    return this.nameExpr;
  }
  
  public Expr getAttrExpr()
  {
    return this.attrExpr;
  }
  
  public String getAttrName()
  {
    return this.attrName;
  }
  
  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }
 
  public ExprXmlAttr clone() throws CloneNotSupportedException {
    ExprXmlAttr exp = (ExprXmlAttr)super.clone();
    
    exp.attrExpr = (Expr)this.attrExpr.clone();
    if(this.attrName == null)
    {
      exp.attrName = null;
      exp.nameExpr = (Expr)this.nameExpr.clone();
    }
    else
    {
      exp.attrName = new String(this.attrName);
      exp.nameExpr = null;
    }
    return exp;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs) 
  {
    getAllReferencedAttrs(attrs, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams) 
  {
    if(this.nameExpr != null)
      this.nameExpr.getAllReferencedAttrs(attrs, includeAggrParams);
    attrExpr.getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprXmlAttr other = (ExprXmlAttr)otherObject;
    if(!this.attrExpr.equals(other.attrExpr))
      return false;
    if(this.attrName == null) 
    { if(other.attrName != null)
       return false;
      if(!this.nameExpr.equals(other.nameExpr))
        return false;
    }
    else
    { if(other.attrName == null)
        return false;
      return (this.attrName.equalsIgnoreCase(other.attrName));
    }
    return true;
      
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalXmlAttrExpression>");
    sb.append(super.toString());
    sb.append(attrExpr.toString());
    if(attrName != null)
      sb.append("<AS " + attrName + "/>");
    else
      sb.append("<AS" + nameExpr.toString() + ">");
    sb.append("</LogicalXmlAttrExpression>");
    return sb.toString();
  }
	
}
