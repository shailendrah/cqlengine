/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprUserDefFunc.java /main/13 2015/05/13 20:43:48 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 User Defined Function Expression Physical Operator Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    05/12/15 - construct sql equivalent
 udeshmuk    04/30/12 - use BI or ORACLE mode to get proper equivalent
 udeshmuk    06/20/11 - support getSQLEquivalent
 alealves    11/27/09 - Data cartridge context, default package support
 udeshmuk    11/08/09 - API to get all referenced attrs
 udeshmuk    09/09/09 - add funcname and linkname.
 sborah      04/20/09 - define getSignature
 rkomurav    06/18/07 - cleanup
 rkomurav    03/06/07 - restrucuter exprFactorycontext
 rkomurav    10/10/06 - add equals method
 rkomurav    09/11/06 - cleanup for xmldump
 anasrini    06/19/06 - 
 najain      04/27/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprUserDefFunc.java /main/13 2015/05/13 20:43:48 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.SQLType;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.SimpleFunction;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * User Defined Function Physical Operator Expression Class Definition
 */
public class ExprUserDefFunc extends Expr
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
  
  public ExprUserDefFunc(int funcId, Expr[] args, Datatype dt,
    UserDefinedFunction sf, String funcName, String linkName)
  {
    super(ExprKind.USER_DEF);
    setType(dt);

    this.funcId = funcId;
    this.args   = args;
    this.funcImpl = sf;
    this.funcName = funcName;
    this.cartridgeLinkName = linkName;
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
   * get the name of the function
   * @return function name
   */
  public String getFuncName()
  {
    return this.funcName;
  }

  /**
   * get the name of the cartridge link
   * @return link name
   */
  public String getCartridgeLinkName()
  {
    return this.cartridgeLinkName;
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
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * The signature of a user defined function contains its unique function id
   * and the list of its parameters.
   * 
   * @return 
   *      A concise String representation of the User defined function.
   */
  public String getSignature()
  {
    StringBuilder regExpression = new StringBuilder();
    // Handle user defined methods.
        
    // Add the function name@link or the id
    regExpression.append("Fn#");
    if (getFuncName() != null) 
    {
      regExpression.append(getFuncName() + "@" + this.getCartridgeLinkName());
    }
    else
    {
      regExpression.append(getFuncId());
    }
    regExpression.append("(");
    
    // boolean flag to figure out if commas are required
    // in the expression.
    boolean commaRequired = false;
    for(Expr attrs : this.getArgs())
    {
      if(commaRequired)
        regExpression.append(",");
      
      // process the base expression of the attributes recursively.
      if(attrs != null)
        regExpression.append(attrs.getSignature());
      else
        regExpression.append("null");
      
      // comma require for any further attributes.
      commaRequired = true;
    }

    regExpression.append(")");
    
    return regExpression.toString();
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
  
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    xml.append(funcId);
    xml.append("(");
    if(args.length != 0) {
      for(i = 0; i < (args.length - 1); i++) {
        xml.append(args[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(args[i].getXMLPlan2());
    }
    xml.append(")");
    
    return xml.toString();
  }

  public UserDefinedFunction getFuncImpl()
  {
    return funcImpl;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  { 
    for(int i=0; i < args.length; i++)
      args[i].getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    
    StringBuilder regExpression = new StringBuilder();
    // Handle user defined methods.
        
    //Get sql equivalent of the function if it exists
    
    /* 
     * FIXME: For now we handle only those simple functions which are seeded by
     * Install.java
     * For Aggr functions we won't come here. GroupAggr has array of BaseAggrFn. 
     * Using switch-case on each entry in that array one can determine if it is 
     * possible to query archiver or not on a case-by-case basis.
     * 
     * Cartridge/Extensible functions and User defined ones are NOT handled.
     * 
     * If we decide to support these as well and we plan to add a method 
     * getSQLEquivalent in UserDefinedFunction then we will have to change all 
     * the existing implementations to support this method. Also it is not 
     * mandatory for users writing the UDF to implement SingleElementFunction. 
     * There are wrapper classes in CQLProcessor for SingleElementFunction and 
     * those will also need modification. Colt function conversions might also 
     * need some change.
     * 
     */
    //If cartridge/extensible function return null
    if(this.cartridgeLinkName != null)
      return null;
    
    SimpleFunction sf = null;
    try
    {
      sf = ec.getUserFnMgr().getSimpleFunction(funcId);
    }
    catch(MetadataException me)
    {
      return null;
    }
    
    String temp = null; 
    
    SQLType sqlMode = ec.getServiceManager().getConfigMgr().getTargetSQLType();
      
    if(sf != null)
    {
      temp = sf.getSQLEquivalent(sqlMode);
    }
   
    if(temp == null)
      return null;
    
    String funcName = temp.toString();
    String argSep = ", ";
    boolean addExtraRightParen = false;
    if(sqlMode == SQLType.BI)
    { //some BI functions have a different syntax
      //e.g. substring (strExpr from startPos)
      //Here the arguments are separated by 'from'.
      //so separate such arguments by $ symbol initially.
      //When an operator is marked as query operator then replace $ by FROM.
      //We don't put FROM directly here because certain operators e.g. Distinct
      //extract child operator's project clause by looking up for FROM
      if(funcName.indexOf("#") != -1)
      {
        argSep = " $ ";
	//remove # from name
	String actualName = funcName.replace('#',' ');
	funcName = actualName;
      }

      //check if funcName as '(' e.g. trim function
      //Extra right parenthesis should be added when BI SQL equivalent
      //function name in Install.java has left parenthesis in it.
      if(funcName.indexOf("(") != -1)
        addExtraRightParen = true;
    }
    else if (sqlMode == SQLType.ORACLE)
    {
       //Extract has a different syntax in oracle. extract(hour from dateTimeCol)
       if(funcName.equalsIgnoreCase("extract"))
       {
          argSep = " $ ";
          regExpression.append(funcName+"(");
          //extract will have two arguments
          assert this.getArgs().length == 2;
          Expr[] args = this.getArgs();
          String firstArg = args[0].getSQLEquivalent(ec);
          if(firstArg == null)
            return null;
          else
            regExpression.append(firstArg.replace("'", "")+argSep+ " ");
          String secondArg = args[1].getSQLEquivalent(ec);
          if(secondArg == null)
            return null;
          else
            regExpression.append(secondArg + ")");
          return regExpression.toString();
       }
    }

    regExpression.append(funcName);
    
    if(((this.getArgs() != null) && (this.getArgs().length > 0)) 
       && (!funcName.equalsIgnoreCase("")))
      regExpression.append("(");
    
    // boolean flag to figure out if commas are required
    // in the expression.
    boolean commaRequired = false;
    for(Expr attrs : this.getArgs())
    {
      if(commaRequired)
        regExpression.append(argSep);
      
      // process the base expression of the attributes recursively.
      if(attrs != null)
      {
        temp = attrs.getSQLEquivalent(ec);
        if(temp == null)
          return null;
        regExpression.append(temp);
      }
      
      // comma require for any further attributes.
      commaRequired = true;
    }

    if(((this.getArgs() != null) && (this.getArgs().length > 0)) 
       && (!funcName.equalsIgnoreCase("")))
      regExpression.append(") ");

    if(addExtraRightParen)
      regExpression.append(") ");

    return regExpression.toString();
  }

}
