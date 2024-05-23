/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlConcat.java /main/5 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    skmishra    06/12/08 - removing tabs
    skmishra    06/12/08 - cleanup getxmlplan2
    skmishra    05/15/08 - 
    mthatte     04/30/08 - Creation
 */

package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmlConcat.java /main/3 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

public class ExprXmlConcat extends Expr
{
  Expr[] concatExprs;

  public ExprXmlConcat(List<Expr> concatList)
  {
    super(ExprKind.XML_CONCAT_EXPR);
    setType(Datatype.XMLTYPE);
    concatExprs = (Expr[]) concatList.toArray(new Expr[concatList.size()]);
  }

  public Expr[] getConcatExprs()
  {
    return concatExprs;
  }
  
 /**
  * Method to calculate a concise String representation
  * of the Expression. The String is built recursively by calling 
  * this method on each type of Expr object which forms the expression.
  * 
  * @return 
  *      A concise String representation of the Xml Concat Expression.
  */
  public String getSignature()
  {
    StringBuilder regExpression = new StringBuilder();
    regExpression.append(this.getKind() +"(");
    
    boolean commaRequired = false;
    for(Expr expr : concatExprs)
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
  
  
  public boolean equals(Object o)
  {
    if(o==null)
      return false;
    if(this==o)
      return true;
    if(getClass() != o.getClass())
      return false;
    
    ExprXmlConcat other = (ExprXmlConcat)o;
    
    for (Expr e : concatExprs)
    {
      for (Expr f : other.getConcatExprs())
      {
        if (!e.equals(f))
          return false;
      }
    }
    
    return true;
  }

  public String getXMLPlan2()
  {
    StringBuilder xml = new StringBuilder();
    xml.append("XMLCONCAT (");
    for (Expr e : concatExprs)
    {
      xml.append(e.getXMLPlan2() + ",");
    }
    //remove the last "," 
    xml.deleteCharAt(xml.length()-1);
    xml.append(")");
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    for(Expr e : concatExprs)
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
