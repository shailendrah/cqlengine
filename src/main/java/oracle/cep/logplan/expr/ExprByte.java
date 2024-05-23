/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprByte.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Byte Logical Operator Expression Class Definition


 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/11/11 - override getAllReferencedAttrs()
 parujain    10/13/06 - getting returntype from Semantic
 rkomurav    09/25/06 - adding equals method
 najain      05/30/06 - add check_reference 
 najain      03/17/06 - fix compiation issues
 anasrini    03/14/06 - type should be byte[] 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: ExprByte.java 13-oct-2006.11:05:33 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Byte Logical Operator Expression Class Definition
 */
public class ExprByte extends Expr implements Cloneable {
  /** byte value */
  byte[] bValue;

  public ExprByte(byte[] bValue, Datatype dt) {
    assert dt == Datatype.BYTE;
    setType(dt);
    this.bValue = bValue;
  }

  public byte[] getBValue() {
    return bValue;
  }

  public void setBValue(byte[] bValue) {
    this.bValue = bValue;
  }

  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());
    return (Attr) attr;
  }

  public boolean check_reference(LogOpt op) {
    return true;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    return;
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
    sb.append("<LogicalByteExpression>");
    sb.append(super.toString());
    sb.append("<Value bValue=\"" + bValue + "\" />");

    sb.append("</LogicalByteExpression>");
    return sb.toString();
  }

}
