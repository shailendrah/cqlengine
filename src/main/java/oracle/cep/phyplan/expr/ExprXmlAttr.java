/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlAttr.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    skmishra    06/12/08 - cleanup getxmlplan2
    parujain    05/19/08 - evalname
    parujain    04/25/08 - XmlAttr Expr
    parujain    04/25/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlAttr.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprXmlAttr extends Expr{

  private Expr attrExpr;
  
  private String attrName;
  
  private Expr nameExpr;
  
  public ExprXmlAttr(String name, Expr attr, Datatype dt)
  {
    super(ExprKind.XMLATTR_EXPR);
    setType(dt);
    this.attrName = name;
    this.attrExpr = attr;
    this.nameExpr = null;
  }
  
  public ExprXmlAttr(Expr name, Expr attr, Datatype dt)
  {
    super(ExprKind.XMLATTR_EXPR);
    setType(dt);
    this.attrName = null;
    this.attrExpr = attr;
    this.nameExpr = name;
  }
  
  public Expr getAttrExpr()
  {
    return attrExpr;
  }
  
  public String getAttrName()
  {
    return attrName;
  }
  
  public Expr getAttrNameExpr()
  {
    return nameExpr;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Xml Attr Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind());
     if(this.attrExpr != null)
      regExpression.append("(" + this.attrExpr.getSignature() + ")");
     else
      regExpression.append("null");
     
     if(this.attrName != null)
       regExpression.append( "#" + this.attrName);
     
     if(this.nameExpr != null)
       regExpression.append("(" + this.nameExpr.getSignature() + ")");
     
     
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
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalXmlAttrExpression>");
    sb.append(super.toString());
    sb.append(attrExpr.toString());
    if(attrName != null)
      sb.append("<AS " + attrName + "/>");
    else
      sb.append("<AS " + nameExpr.toString() + "/>");
    sb.append("</PhysicalXmlAttrExpression>");
    return sb.toString();
  }

  @Override
  public String getXMLPlan2() { 
    StringBuilder xml = new StringBuilder();

    xml.append(attrExpr.getXMLPlan2());
    
    if(attrName != null)
    {
      xml.append(" AS " + attrName);
    }
    else
      xml.append(" AS " + nameExpr.getXMLPlan2());
    
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    if(attrExpr != null)
      attrExpr.getAllReferencedAttrs(attrs);
    if(nameExpr != null)
      nameExpr.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
