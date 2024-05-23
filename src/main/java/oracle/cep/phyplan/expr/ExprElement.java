/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprElement.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    udeshmuk    06/20/11 - support getSQLEquivalent
    udeshmuk    11/08/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    skmishra    06/18/08 - 
    parujain    05/19/08 - evalname
    parujain    04/25/08 - Element Expr
    parujain    04/25/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprElement.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprElement extends Expr {

  private String elementName;
  
  private Expr elementNameExpr;
  
  private Expr[] attrExprs;
  
  private Expr[] childExprs;
  
  public ExprElement(String name, Expr[] attrs, Expr[] child, Datatype dt)
  {
    super(ExprKind.XMLELEMENT_EXPR);
    this.elementName = name;
    this.elementNameExpr = null;
    this.attrExprs = attrs;
    this.childExprs = child;
    setType(dt);
  }
  
  public ExprElement(Expr name, Expr[] attrs, Expr[] child, Datatype dt)
  {
    super(ExprKind.XMLELEMENT_EXPR);
    this.elementName = null;
    this.elementNameExpr = name;
    this.attrExprs = attrs;
    this.childExprs = child;
    setType(dt);
  }
  
  public String getElementName()
  {
    return this.elementName;
  }
  
  public Expr getElementNameExpr()
  {
    return this.elementNameExpr;
  }
  
  public int getNumChildren()
  {
    if(childExprs == null)
      return 0;
    return childExprs.length;
  }
  
  public int getNumAttrs()
  {
    if(attrExprs == null)
      return 0;
    return attrExprs.length;
  }
  
  public Expr[] getChildExprs()
  {
    return childExprs;
  }
  
  public Expr[] getAttrExprs()
  {
    return attrExprs;
  }
  
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Element Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind() +  this.getType().toString());
     
     if(this.elementName != null)
       regExpression.append( "#" + this.elementName);
     else
       regExpression.append( "#null");
     
     if(this.elementNameExpr != null)
       regExpression.append("#" + this.elementNameExpr.getSignature() + "(");
     else
       regExpression.append( "#null" + "(");
          
     boolean commaRequired = false;
     
     for(int i = 0; i < getNumAttrs(); i++)
     {
       if(commaRequired)
         regExpression.append(",");
       
       // process the base expression of the attributes recursively.
       regExpression.append(attrExprs[i].getSignature());
       
       // comma require for any further attributes.
       commaRequired = true;
     }
     
     regExpression.append(")" + "(");
     commaRequired = false;
     
     for(int j = 0; j < getNumChildren(); j++)
     { 
       if(commaRequired)
       regExpression.append(",");
     
       // process the base expression of the attributes recursively.
       regExpression.append(childExprs[j].getSignature());
       
       // comma require for any further attributes.
       commaRequired = true;
     }
     regExpression.append(")"); 
     
     return regExpression.toString();
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
    sb.append("<PhysicalElementExpression>");
    sb.append(super.toString());
    if(elementName != null)
     sb.append("<ElementName = "+elementName +"/>");
    else
      sb.append("<ElementName = " + elementNameExpr.toString() + "/>");
    for(int i=0; i<getNumAttrs(); i++)
      sb.append(attrExprs[i].toString());
    for(int j=0; j<getNumChildren(); j++)
      sb.append(childExprs[j].toString());
    sb.append("</PhysicalElementExpression>");
    return sb.toString();
  }
  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    if(elementName != null)
      xml.append(elementName);
    else
      xml.append(elementNameExpr.getXMLPlan2());
    if(getNumAttrs() > 0)
      xml.append("XMLATTRIBUTES(");
    for(int i=0; i<getNumAttrs(); i++)
      xml.append(attrExprs[i].getXMLPlan2());
    if(getNumAttrs() > 0)
      xml.append(")");
    for(int j=0; j<getNumChildren(); j++)
      xml.append(childExprs[j].getXMLPlan2());
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    if(elementNameExpr != null)
      elementNameExpr.getAllReferencedAttrs(attrs);
    for(int i=0; i < attrExprs.length; i++)
      attrExprs[i].getAllReferencedAttrs(attrs);
    for(int i=0; i < childExprs.length; i++)
      childExprs[i].getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    //FIXME: for now returning null, don't know the SQL Equivalent
    return null;
  }

}
