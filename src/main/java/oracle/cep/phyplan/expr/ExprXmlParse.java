/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlParse.java /main/5 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    udeshmuk   06/20/11 - support getSQLEquivalent
    udeshmuk   11/08/09 - API to get all referenced attrs
    sborah     04/20/09 - define getSignature
    skmishra   06/12/08 - equals
    skmishra   06/12/08 - cleanup getxmlplan2
    mthatte    05/19/08 - Creation
 */

package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.XMLParseKind;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlParse.java /main/3 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class ExprXmlParse extends Expr
{

  Expr value;
  boolean isWellformed;
  XMLParseKind parseKind;
  
  public ExprXmlParse(Expr value, boolean isWellformed, XMLParseKind _kind)
  {
    super(ExprKind.XML_PARSE_EXPR);
    setType(Datatype.XMLTYPE);
    this.value = value;
    this.isWellformed = isWellformed;
    this.parseKind = _kind;
  }
  
  public boolean isWellformed()
  {
    return isWellformed;
  }

  public void setWellformed(boolean isWellformed)
  {
    this.isWellformed = isWellformed;
  }

  public XMLParseKind getParseKind()
  {
    return parseKind;
  }

  public void setParseKind(XMLParseKind kind)
  {
    this.parseKind = kind;
  }

  public Expr getValue()
  {
    return value;
  }

  public void setValue(Expr value)
  {
    this.value = value;
  }

  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Xml Parse Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind() +"#");
    
     if(this.value != null)
       regExpression.append(this.value.getSignature());
     else
       regExpression.append("null");
          
     return regExpression.toString();
   }
   
  public boolean equals(Object o)
  {
    if(o==null)
      return false;
    if(this==o)
      return true;
    if(getClass() != o.getClass())
      return false;

    ExprXmlParse other  = (ExprXmlParse)o;
    return other.getValue().equals(value);
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorXmlParseExpression>");
    sb.append(super.toString());
    sb.append("<Value>");
    sb.append("<![CDATA[");
    sb.append(value.toString());
    sb.append("]]>");
    sb.append("</PhysicalOperatorXmlCommentExpression>");
    return sb.toString();
  }
  
  public String getXMLPlan2()
  {
    StringBuilder val = new StringBuilder();
    val.append("XMLPARSE(");
    val.append(value.toString());
    val.append(")");
    return val.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    if(value != null)
      value.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
