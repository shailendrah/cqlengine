/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/Expr.java /main/13 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Abstract Physical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    05/10/12 - add name/alias
 sbishnoi    08/29/11 - support for interval year to month based operations
 udeshmuk    06/20/11 - support getSqlEquivalent to be used in archived query
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      06/23/09 - support for bigdecimal
 sborah      04/21/09 - adding methods to calculate expr lengths
 sborah      04/20/09 - define getSignature
 udeshmuk    01/11/08 - add a boolean flag to indicate when expr evaluates to
                        null.,
 parujain    11/07/07 - support isOnDemand
 parujain    03/08/07 - get Object
 rkomurav    11/28/06 - add equals abstract method
 rkomurav    09/11/06 - cleanup for xmldump
 rkomurav    08/22/06 - XML_Visualiser
 najain      03/21/06 - constructor with exprKind
 anasrini    03/14/06 - implement getKind 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/Expr.java /main/13 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.Expression;
import oracle.cep.extensibility.expr.ExprKind;

/**
 * Abstract Physical Operator Expression Class Definition
 */
public abstract class Expr 
    implements Expression
{
  ExprKind kind;

  String alias;
  
  boolean isExternal = false; 
  /**
   * boolean flag to indicate if expression evaluates to null
   */
  protected boolean bNull = false;
 
  public Expr() 
  {
    super();
    isExternal = false;
    bNull = false;
  }
  
  public Expr(ExprKind kind) 
  {
    this.kind = kind;
    bNull = false;
  }
  
  /** Output type of the expression */
  Datatype type;

  public ExprKind getKind() {
    return kind;
  }

  public Datatype getType() {
    return type;
  }

  public void setType(Datatype type) {
    this.type = type;
  }
 
  public boolean isExternal() {
    return isExternal;
  }

  public void setExternal(boolean isExternal) {
    this.isExternal = isExternal;
  }
  
  public String getAlias()
  {
    return this.alias;
  }
  
  public void setAlias(String alias)
  {
    this.alias = alias;
  }
  
  public void setbNull(boolean isNull)
  {
    this.bNull = isNull;
  }
  
  public boolean isNull()
  {
    return this.bNull;
  }
 
  public Object getObject()
  {
    return null;
  }
  
  public abstract boolean equals(Object o);
  
  /**
   * Calculates the length of the variable according to its type
   * @return
   *        the length of the variable
   */
  protected int getVariableTypeLength()
  {
    int attrLen = 1;
    Datatype attrTypes = this.getType();

    if (attrTypes == Datatype.CHAR)
      attrLen = Constants.MAX_CHAR_LENGTH;
    else if (attrTypes == Datatype.BYTE)
      attrLen = Constants.MAX_BYTE_LENGTH;
    else
      attrLen = Constants.MAX_XMLTYPE_LENGTH;

    return attrLen;
  }
  
  /**
   * Computes the length of the corresponding expr object
   * 
   * @return the length of the expr
   * 
   */
  public int getLength()
  {

    Datatype attrTypes = this.getType();
    int attrLen = 0;
    
    switch (attrTypes.getKind()) 
    {
    case INT:
      attrLen = Datatype.INT.getLength();
      break;
    case BIGINT:
      attrLen = Datatype.BIGINT.getLength();
      break;
    case FLOAT:
      attrLen = Datatype.FLOAT.getLength();
      break;
    case DOUBLE:
      attrLen = Datatype.DOUBLE.getLength();
      break;
    case BIGDECIMAL:
      attrLen = Datatype.BIGDECIMAL.getLength();
      break;
    case BOOLEAN:
      attrLen = Datatype.BOOLEAN.getLength();
      break;
    case INTERVAL:
      attrLen = 0;
      break;
    case INTERVALYM:
      attrLen = 0;
      break;
    case CHAR:
    case BYTE:
    case XMLTYPE:
      attrLen = this.getVariableTypeLength();
      break;
    case TIMESTAMP:
      attrLen = Datatype.TIMESTAMP.getLength();
      break;
    case OBJECT:
      attrLen = Datatype.OBJECT.getLength(); 
      break;
    default:
      assert false;
      break;
    
    }
    
    return attrLen;
 
  }
  
  /**
   * Collect all the attributes that are referenced by this expression.
   * @param attrs List where the attrs referenced, if any, by this expression
   *              will be collected 
   */
  public abstract void getAllReferencedAttrs(List<Attr> attrs);
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      A concise String representation of the Expression.
   */
  public abstract String getSignature();


  /**
   * Method to calculate a string representation of the expression
   * which will be used in the SQL query to be executed against the archiver
   * @param ec execcontext  needed for some expr types e.g.ExprUserdefFunc
   * 
   * @return String representation of the expression if SQL equivalent is
   *         available, null otherwise.
   */
  public abstract String getSQLEquivalent(ExecContext ec);
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorExpression>");

    sb.append("<ExpressionKind kind=\"" + kind + "\" />");
    sb.append("<Datatype type=\"" + type + "\" />");

    sb.append("</PhysicalOperatorExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML Plan
  public abstract String getXMLPlan2();
}
