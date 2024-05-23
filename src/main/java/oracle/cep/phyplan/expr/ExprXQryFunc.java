/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXQryFunc.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    najain      02/08/08 - 
    mthatte     12/26/07 - 
    najain      10/31/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXQryFunc.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * XQuery Function Physical Operator Expression Class Definition
 */
public class ExprXQryFunc extends Expr
{
  /** unique identifier of the function */
  private int  funcId;

  /** xquery string */
  private String xqryStr;

  /** parameters of the function */
  private Expr[] params;

  /** names to be bound to the xquery string */
  private String[] names;

  // xmlQuery or xmlExists
  private ExprXQryFuncKind xmlQuery;

  public ExprXQryFunc(int funcId, String xqryStr, Expr[] params, 
                      String[] names, Datatype dt, int length, 
                      ExprXQryFuncKind xmlQuery)
  {
    super(ExprKind.XQRY_FUNC);
    setType(dt);
    this.funcId   = funcId;
    this.xqryStr  = xqryStr;
    this.params   = params;
    this.names    = names;
    this.xmlQuery = xmlQuery;
  }

  /**
   * @return Returns the funcId.
   */
  public int getFuncId()
  {
    return funcId;
  }

  public ExprXQryFuncKind getXmlQuery()
  {
    return xmlQuery;
  }

  /**
   * @return Returns the numParams.
   */
  public int getNumParams()
  {
    if (params == null)
      return 0;
    return params.length;
  }

  /**
   * @return Returns the parameters
   */
  public Expr[] getParams() 
  {
    return params;
  }

  public String getXQryStr()
  {
    return xqryStr;
  }

  public String[] getNames()
  {
    return names;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<XQryFunctionExpression funcId=\"" + funcId + "\">");
    sb.append(super.toString());
    int numParams = getNumParams();
    for (int i=0; i<numParams; i++) {
      sb.append(params[i].toString());
    }
    sb.append("</XQryFunctionExpression>");
    return sb.toString();
  }
  
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Xml XQuery Function.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind());
    
     regExpression.append("#" + this.funcId +"{");
     
     for(int i = 0; i < params.length; i++) 
     {
       regExpression.append("(");
       
       if(params[i] != null)
         regExpression.append(params[i].getSignature() +",");
       else
         regExpression.append("null,");
       if(names[i] != null)
         regExpression.append(names[i]);
       else
         regExpression.append("null");
       
       regExpression.append(")");
     }
     
     regExpression.append("}#" + this.xqryStr + "#" +this.xmlQuery);
     
          
     return regExpression.toString();
   }
   
  public boolean equals(Object otherObject) 
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprXQryFunc other = (ExprXQryFunc) otherObject;
    
    if (funcId != other.getFuncId())
      return false;
    
    if(params.length != other.getParams().length)
      return false;
    
    for(int i = 0; i < params.length; i++) 
    {
      if (!params[i].equals(other.getParams()[i]))
        return false;

      if (!names[i].equals(other.getNames()[i]))
        return false;
    }

    if (!xqryStr.equals(other.getXQryStr()))
      return false;

    if (xmlQuery != other.xmlQuery)
      return false;

    return true;
  }
  
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    xml.append(funcId);
    xml.append("(");
    if(params.length != 0) {
      for(i = 0; i < (params.length - 1); i++) {
        xml.append(params[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(params[i].getXMLPlan2());
    }
    xml.append(")");
    
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    if(params != null)
    {
      for(Expr p : params)
      {
        if(p != null)
          p.getAllReferencedAttrs(attrs);
      }
    }
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
