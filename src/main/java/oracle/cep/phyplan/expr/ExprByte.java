/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprByte.java /main/7 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Byte Physical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    06/20/11 - support getSQLEquivalent
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      04/21/09 - override getVariableTypeLength()
 sborah      04/20/09 - define getSignature
 rkomurav    06/18/07 - cleanup
 parujain    03/08/07 - get Object
 rkomurav    10/10/06 - add equals method
 rkomurav    09/11/06 - cleanup for xmldump
 rkomurav    08/22/06 - add getXMLPlan2
 najain      03/24/06 - cleanup
 anasrini    03/14/06 - type should be byte[] 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprByte.java /main/5 2009/12/03 21:27:59 udeshmuk Exp $
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
 * Byte Physical Operator Expression Class Definition
 */
public class ExprByte extends Expr {
  /** byte value */
  byte[] bValue;

  public byte[] getBValue() {
    return bValue;
  }

  public void setBValue(byte[] bValue) {
    this.bValue = bValue;
  }
  
  public Byte[] getObject()
  {
    Byte[] obj = new Byte[bValue.length];
    for(int i=0; i<bValue.length; i++)
      obj[i] = bValue[i];
    return obj;
  }
  
  public ExprByte(byte[] bValue, Datatype dt)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.BYTE);
    this.bValue = bValue;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Byte in String format
   */
  public String getSignature()
  {
    return this.getObject().toString();
  }
  
  /**
   * Return the length of the byte
   * 
   * @return bValue.length
   */
  protected int getVariableTypeLength()
  {
    return this.bValue.length;
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprByte other = (ExprByte) otherObject;
    byte[] ob      = other.getBValue();
    if(ob.length != bValue.length)
      return false;
    for(int i=0; i < bValue.length; i++) {
      if(bValue[i] != ob[i])
        return false;
    }
    return true;
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorByteExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + bValue + "\" />");
    sb.append("</PhysicalOperatorByteExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    return new String(bValue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    //FIXME: what should be SQLEquivalent? DB doesn't have BYTE/raw literal.
    return null;
  }

}
