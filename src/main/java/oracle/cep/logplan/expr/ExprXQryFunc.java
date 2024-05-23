/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXQryFunc.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah      04/11/11 - override getAllReferencedAttrs()
    najain      02/08/08 - 
    mthatte     12/26/07 - 
    najain      10/31/07 - Creation
 */

/**
 *  @version $Header: ExprXQryFunc.java 08-feb-2008.12:36:11 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;
import oracle.cep.common.Datatype;

/**
 * Function Expression Logical Operator Expression Class Definition
 */
public class ExprXQryFunc extends Expr implements Cloneable
{
  /** unique identifier of the function */
  private int  funcId;

  /** xquery string */
  private String xqryStr;

  /** parameters of the function */
  private Expr[] params;

  /** names to be bound to the xquery string */
  private String[] names;

  // xmlquery or xmlexists
  protected ExprXQryFuncKind xmlQuery;

  // length: optional
  private int length;

  /**
   * Constructor
   * @param funcId internal identifier of the function 
   * @param params arguments to the function
   * @param returnType return type of the function
   */
  public ExprXQryFunc(int funcId, Expr[] params, String[] names, String xqryStr, Datatype returnType, int length, ExprXQryFuncKind xmlQuery) {
    this.funcId  = funcId;
    this.params  = params;
    this.names   = names;
    this.xqryStr = xqryStr;
    setType(returnType);
    this.length = length;
    this.xmlQuery = xmlQuery;
  }

  /**
   * @return Returns the funcId.
   */
  public int getFuncId()
  {
    return funcId;
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
   * @return Returns the arguments
   */
  public Expr[] getParams() 
  {
    return params;
  }
  
  public String getXQryStr()
  {
    return xqryStr;
  }

  public int getLength()
  {
    return length;
  }

  public String[] getNames()
  {
    return names;
  }

  public ExprXQryFuncKind getXmlQuery()
  {
    return xmlQuery;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public ExprXQryFunc clone() throws CloneNotSupportedException {
    ExprXQryFunc exp       = (ExprXQryFunc) super.clone();
    int          numParams = getNumParams(); 

    for(int i=0; i<numParams; i++) {
      exp.params[i] = (Expr) params[i].clone();
      exp.names[i] = new String(names[i]);
    }

    exp.xqryStr = new String(xqryStr);
    return exp;
  }

  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }

  public boolean check_reference(LogOpt op) {

    int numParams = getNumParams();
    for (int i=0; i<numParams; i++) {
      if (!(params[i].check_reference(op)))
        return false;
    }
    return true;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    if(params != null)
    {
      for(int i = 0; i < params.length; i++) 
      {
        params[i].getAllReferencedAttrs(attrs, includeAggrParams);
      }
    }
  }
  
  public boolean equals(Object otherObject) {
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
    

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<XQryFunctionExpression funcId=\"" + funcId + "\">");
    sb.append(super.toString());
    sb.append("<XQryString>");
    sb.append(xqryStr);
    sb.append("</XQryString>");
    int numParams = getNumParams();
    for (int i=0; i<numParams; i++) {
      sb.append("<Name>");
      sb.append(names[i]);
      sb.append("</Name>");
      sb.append(params[i].toString());
    }
    sb.append("</XQryFunctionExpression>");
    return sb.toString();
  }

}
