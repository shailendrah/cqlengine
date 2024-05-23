/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprElement.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    parujain    04/23/08 - XML Element Expr
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: ExprElement.java 03-jun-2008.11:59:02 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprElement extends Expr implements Cloneable {
  
  private String elementName;
  
  private Expr elementNameExpr;
  
  private Expr[] attrExprs;
  
  private Expr[] childExprs;
  
  public ExprElement(String name, Expr[] attrs, Expr[] child, Datatype dt)
  {
    this.elementName = name;
    this.elementNameExpr = null;
    this.attrExprs = attrs;
    this.childExprs = child;
    setType(dt);
  }
  
  public ExprElement(Expr name, Expr[] attrs, Expr[] child, Datatype dt)
  {
    this.elementName = null;
    this.elementNameExpr = name;
    this.attrExprs = attrs;
    this.childExprs = child;
    setType(dt);
  }
  
  public Expr getElementNameExpr()
  {
    return this.elementNameExpr;
  }
  
  public String getElementName()
  {
    return elementName;
  }
  
  public void setAttrExprs(Expr[] attrs)
  {
    this.attrExprs = attrs;
  }
  
  public void setChildExprs(Expr[] child)
  {
    this.childExprs = child;
  }
  
  public Expr[] getAttrExprs()
  {
    return this.attrExprs;
  }
  
  public Expr[] getChildExprs()
  {
    return this.childExprs;
  }
  
  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }
  
  public int getNumAttrs()
  {
    if(attrExprs == null)
      return 0;
    return attrExprs.length;
  }
  
  public int getNumChildren()
  {
    if(childExprs == null)
      return 0;
    return childExprs.length;
  }
  
  public boolean check_reference(LogOpt op) {
    if(elementNameExpr != null)
    {
      if(!elementNameExpr.check_reference(op))
        return false;
    }
    for(int i=0; i<getNumAttrs(); i++)
    {
      if(!attrExprs[i].check_reference(op))
        return false;
    }
    for(int j=0; j<getNumChildren(); j++)
    {
      if(!childExprs[j].check_reference(op))
        return false;
    }
    return true;
  }
  
  public ExprElement clone() throws CloneNotSupportedException {
    ExprElement elem = (ExprElement)super.clone();
    int attrlen = getNumAttrs();
    int childlen = getNumChildren();
    if(elementName != null)
      elem.elementName = new String(elementName);
    else
      elem.elementNameExpr = (Expr)elementNameExpr.clone();
    for(int i=0; i<attrlen; i++)
    {
      elem.attrExprs[i] = (Expr)attrExprs[i].clone();
    }
    for(int j=0; j<childlen; j++)
    {
      elem.childExprs[j] = (Expr)childExprs[j].clone();
    }
    return elem;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs) {
    getAllReferencedAttrs(attrs, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams) 
  {
    if(this.elementNameExpr != null)
      this.elementNameExpr.getAllReferencedAttrs(attrs, includeAggrParams);
    for(int i=0; i<getNumAttrs(); i++)
      attrExprs[i].getAllReferencedAttrs(attrs, includeAggrParams);
    for(int j=0; j<getNumChildren(); j++)
      childExprs[j].getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprElement other = (ExprElement)otherObject;
    if(this.elementName == null)
    {
      if(other.elementName != null)
        return false;
      if(!this.elementNameExpr.equals(other.elementNameExpr))
        return false;
    }
    else
    {
      if(other.elementName == null)
        return false;
      if(!this.elementName.equalsIgnoreCase(other.elementName))
        return false;
    }
    if(this.getNumAttrs() != other.getNumAttrs())
      return false;
    if(this.getNumChildren() != other.getNumChildren())
      return false;
    for(int i=0; i<getNumAttrs(); i++)
    {
      if(!(this.attrExprs[i].equals(other.attrExprs[i])))
        return false;
    }
    for(int j=0; j<getNumChildren(); j++)
    {
       if(!(this.childExprs[j].equals(other.childExprs[j])))
         return false;
    }
    
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalElementExpression>");
    sb.append(super.toString());
    if(elementName != null)
     sb.append("<ElementName = "+elementName +"/>");
    else
     sb.append("<ElementName = " + elementNameExpr.toString() + ">");
    for(int i=0; i<getNumAttrs(); i++)
      sb.append(attrExprs[i].toString());
    for(int j=0; j<getNumChildren(); j++)
      sb.append(childExprs[j].toString());
    sb.append("</LogicalElementExpression>");
    return sb.toString();
  }
}
