/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprUserDefFunc.java /main/6 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 User Defined Function Expression Physical Operator Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/11/11 - override getAllReferencedAttrs()
 alealves    11/27/09 - Data cartridge context, default package support
 udeshmuk    09/09/09 - add funcname and linkname.
 rkomurav    09/25/06 - adding equals method
 anasrini    06/20/06 - implement clone 
 anasrini    06/19/06 - support for function expressions 
 najain      05/30/06 - add check_reference 
 najain      04/27/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprUserDefFunc.java /main/5 2010/03/05 16:04:52 alealves Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Function Expression Logical Operator Expression Class Definition
 */
public class ExprUserDefFunc extends Expr implements Cloneable
{
  /** unique identifier of the function */
  private int  funcId;

  /** arguments to the function */
  private Expr[] args;
  
  /** runtime function implementation instance */
  private UserDefinedFunction funcImpl;
  
  /** name of the function */
  private String funcName;
  
  /** name of the cartridge link */
  private String cartridgeLinkName;

  /**
   * Constructor
   * @param funcId internal identifier of the function 
   * @param args arguments to the function
   * @param returnType return type of the function
   */
  public ExprUserDefFunc(int funcId, Expr[] args, Datatype returnType, 
      UserDefinedFunction sf, String funcName, String linkName) {
    this.funcId = funcId;
    this.args   = args;
    this.funcImpl = sf;
    this.funcName = funcName;
    this.cartridgeLinkName = linkName;
    setType(returnType);
  }
  
  /**
   * @return Returns the funcId.
   */
  public int getFuncId()
  {
    return funcId;
  }

  /**
   * @return Returns the numArgs.
   */
  public int getNumArgs()
  {
    if (args == null)
      return 0;
    return args.length;
  }

  /**
   * @return Returns the arguments
   */
  public Expr[] getArgs() 
  {
    return args;
  }
  
  /**
   * get name of the function
   * @return function name
   */
  public String getFuncName()
  {
    return this.funcName;
  }

  /**
   * get name of the cartridge link
   * @return link name
   */
  public String getCartridgeLinkName()
  {
    return this.cartridgeLinkName;
  }
  
  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public ExprUserDefFunc clone() throws CloneNotSupportedException {
    ExprUserDefFunc exp     = (ExprUserDefFunc) super.clone();
    int             numArgs = getNumArgs(); 

    for(int i=0; i<numArgs; i++) {
      exp.args[i] = (Expr) args[i].clone();
    }

    return exp;
  }

  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }

  public boolean check_reference(LogOpt op) {

    int numArgs = getNumArgs();
    for (int i=0; i<numArgs; i++) {
      if (!(args[i].check_reference(op)))
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
    for(int i = 0; i < args.length; i++)
    {
      args[i].getAllReferencedAttrs(attrs, includeAggrParams);
    }
  }
  
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprUserDefFunc other = (ExprUserDefFunc) otherObject;
    
    // A function is identified first by its name and its schema (e.g. link), 
    //  and then, if these are not available, by its id.
    if ((funcName != null) || (other.funcName != null))
    {
      if ((funcName == null) || (!funcName.equals(other.funcName)))
        return false;
      
      if ((cartridgeLinkName != null) || (other.cartridgeLinkName != null))
        if ((cartridgeLinkName == null) || (!cartridgeLinkName.equals(other.cartridgeLinkName)))
          return false;
    }
    else
    {
      if (funcId != other.getFuncId())
        return false;
    }
    
    if(args.length != other.getArgs().length)
      return false;
    
    for(int i = 0; i < args.length; i++) {
      if(!args[i].equals(other.getArgs()[i]))
        return false;
    }
    
    // No need to compare the funcImpl.
    
    return true;
  }
    

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<FunctionExpression funcId=\"" + funcId + "\">");
    sb.append(super.toString());
    int numArgs = getNumArgs();
    for (int i=0; i<numArgs; i++) {
      sb.append(args[i].toString());
    }
    sb.append("</FunctionExpression>");
    return sb.toString();
  }

  public UserDefinedFunction getFuncImpl()
  {
    return funcImpl;
  }

}
