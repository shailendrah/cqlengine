/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlForest.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    parujain    05/23/08 - XMLForest expr
    parujain    05/23/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlForest.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprXmlForest extends Expr {
  Expr[] forestExprs;
  
  public ExprXmlForest(Expr[] exprs, Datatype dt)
  {
    super(ExprKind.XMLFOREST_EXPR);
    this.forestExprs = exprs;
    setType(dt);
  }
  
  public Expr[] getForestExprs()
  {
    return this.forestExprs;
  }
  
  public int getNumForestExprs()
  {
    return this.forestExprs.length;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Xml Forest Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind() +"(");
     
     boolean commaRequired = false;
     for(Expr expr : forestExprs)
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
    
    ExprXmlForest other = (ExprXmlForest)otherObject;
    if(this.getNumForestExprs() != other.getNumForestExprs())
      return false;
    for(int i=0; i<getNumForestExprs(); i++)
    {
      if(!this.forestExprs[i].equals(other.forestExprs[i]))
        return false;
    }
    return true;
  }
  
  public String toString() {
	StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalXmlForestExpression>");
    sb.append(super.toString());
    for(int j=0; j<getNumForestExprs(); j++)
      sb.append(forestExprs[j].toString());
    sb.append("</PhysicalXmlForestExpression>");
    return sb.toString();
  }
  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append("XMLFOREST(");
    for(int j=0; j<getNumForestExprs(); j++)
      xml.append(forestExprs[j].getXMLPlan2() + ",");
    //REMOVE LAST ,
    xml.deleteCharAt(xml.length()-1);
    xml.append(")");
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    for(Expr e:forestExprs)
    {
      if(e != null)
        e.getAllReferencedAttrs(attrs);
    }
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
