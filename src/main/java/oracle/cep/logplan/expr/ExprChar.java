/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprChar.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Char array(pointer) Logical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/11/11 - override getAllReferencedAttrs()
 hopark      07/12/07 - fix warning
 parujain    10/13/06 - getting returntype from Semantic
 rkomurav    09/25/06 - adding equals method
 najain      05/30/06 - add check_reference 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: ExprChar.java 12-jul-2007.10:49:20 hopark Exp $
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
 * Char Array (pointer) Logical Operator Expression Class Definition
 */
public class ExprChar extends Expr implements Cloneable {
  /** byte value */
  char[] cValue;

  public ExprChar(char[] cValue, Datatype dt) {
    // TODO Confirm that we can use the memory passed in, or do we need to allocate
    // the array and then copy.
  	assert dt == Datatype.CHAR;
    setType(dt);
    this.cValue = cValue;
  }

  // TODO: need to confirm if the clone needs to be overridden. Since cValue
  // is an array of primitive types, most probably the default clone should
  // be good enough.

  public char[] getCValue() {
    return cValue;
  }

  public void setCValue(char[] cValue) {
    this.cValue = cValue;
  }

  // TODO :: need to make sure that the memory of cValue is allocated

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
    sb.append("<LogicalCharExpression>");
    sb.append(super.toString());
    String str = new String(cValue);
    sb.append("<Value cValue=\"" + str + "\" />");

    sb.append("</LogicalCharExpression>");
    return sb.toString();
  }

}
