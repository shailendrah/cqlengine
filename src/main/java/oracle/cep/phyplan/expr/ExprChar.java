/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprChar.java /main/10 2013/09/03 21:56:39 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Char array(pointer) Physical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    09/01/13 - escape the single quote in a string with additional
                        single quote
 udeshmuk    06/20/11 - support getSQLEquivalent
 udeshmuk    04/05/11 - return quoted string in getSignature
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      04/21/09 - override getVariableTypeLength()
 sborah      04/20/09 - define getSignature
 rkomurav    06/18/07 - cleanup
 parujain    03/08/07 - get Object
 rkomurav    10/10/06 - add equals method
 rkomurav    09/11/06 - cleanup for xmldump
 rkomurav    08/22/06 - add getXMLPlan2
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprChar.java /main/10 2013/09/03 21:56:39 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.common.Datatype;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Char Array (pointer) Physical Operator Expression Class Definition
 */
public class ExprChar extends Expr {

  // TODO: NAMIT:: where is the memory for cValue allocated::::

  /** byte value */
  char[] cValue;

  public char[] getCValue() {
    return cValue;
  }

  public void setCValue(char[] cValue) {
    this.cValue = cValue;
  }

  public String getObject()
  {
    return(new String(cValue));
  }
  
  public ExprChar(char[] cValue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.CHAR);
    this.cValue = cValue;
  }
  
  /**
   * Return the length of the character
   * 
   * @return cValue.length
   */
  protected int getVariableTypeLength()
  {
    return this.cValue.length;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Char in String format
   */
  public String getSignature()
  {
    return this.getObject();
  }
  
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprChar other = (ExprChar)otherObject;
    char[] oc      = other.getCValue();
    if(cValue.length != oc.length)
      return false;
    for(int i=0; i < cValue.length; i++)
      if(cValue[i] != oc[i])
        return false;
    return true;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorCharExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + new String(cValue) + "\" />");
    sb.append("</PhysicalOperatorCharExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    return new String(cValue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    String sqlEquivalent = this.getObject().replaceAll("'", "''");
    return " '"+sqlEquivalent+"' ";
  }

}
