/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlColAttVal.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    parujain    05/29/08 - xmlcolattval support
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlColAttVal.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprXmlColAttVal extends Expr {
  Expr[] colExprs;
  
  public ExprXmlColAttVal(Expr[] exprs, Datatype dt)
  {
    super(ExprKind.XMLCOLATTVAL_EXPR);
    this.colExprs = exprs;
    setType(dt);
  }
  
  public Expr[] getColAttExprs()
  {
    return this.colExprs;
  }
  
  public int getNumColAttExprs()
  {
    return this.colExprs.length;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Xml ColAttrVal Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind() +"(");
     
     boolean commaRequired = false;
     for(Expr expr : colExprs)
     {
       if(commaRequired)
         regExpression.append(",");
       
       // process the base expression of the attributes recursively.
       if(expr != null)
         regExpression.append(expr.getSignature());
       else
         regExpression.append("null");
       
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
    
    ExprXmlColAttVal other = (ExprXmlColAttVal)otherObject;
    if(this.getNumColAttExprs() != other.getNumColAttExprs())
      return false;
    for(int i=0; i<getNumColAttExprs(); i++)
    {
      if(!this.colExprs[i].equals(other.colExprs[i]))
        return false;
    }
    return true;
  }
  
  public String toString() {
	StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalXmlColAttValExpression>");
    sb.append(super.toString());
    for(int j=0; j<getNumColAttExprs(); j++)
      sb.append(colExprs[j].toString());
    sb.append("</PhysicalXmlColAttValExpression>");
    return sb.toString();
  }
  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append("XMLCOLATTVAL(");
    for(int j=0; j<getNumColAttExprs(); j++)
      xml.append(colExprs[j].getXMLPlan2() + ",");
    xml.deleteCharAt(xml.length()-1);
    xml.append(")");
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    for(int i=0; i < colExprs.length; i++)
    {
      if(colExprs[i] != null)
        colExprs[i].getAllReferencedAttrs(attrs);
    }
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
