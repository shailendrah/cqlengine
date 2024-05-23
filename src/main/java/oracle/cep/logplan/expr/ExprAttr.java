/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprAttr.java /main/5 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Attribute Logical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/11/11 - override getAllReferencedAttrs()
 sborah      03/09/09 - setting actual name to corresponding aggr fn.
 parujain    11/06/07 - actual Name
 mthatte     10/30/07 - adding onDemand
 hopark      07/13/07 - dump stack trace on exception
 rkomurav    09/25/06 - adding equals method
 najain      05/30/06 - add check_reference 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/ExprAttr.java /main/4 2009/04/06 23:26:51 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;

/**
 * Attribute Logical Operator Expression Class Definition
 */
public class ExprAttr extends Expr implements Cloneable {
  /** Attribute value */
  Attr aValue;

  String actualName;

  public ExprAttr(Datatype type, Attr value) {
    setType(type);
    aValue = value;
  }
  
  /**
  * Constructor used to set the actual name of the attribute 
  * in addition to its dataType and attribute value.
  * @param type The datatype of the attribute
  * @param value The value of the attribute
  * @param actualName The actual name of the attribute 
  *                    <StreamName.attributeName> 
  */  
  public ExprAttr(Datatype type, Attr value, String actualName) 
  {
    this(type, value);
    setActualName(actualName);
  }

  public Attr getAValue() {
    return aValue;
  }

  public void setAValue(Attr aValue) {
    this.aValue = aValue;
  }

  public void setActualName(String name) {
    this.actualName = name;
  }

  public String getActualName() {
    return this.actualName;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public ExprAttr clone() throws CloneNotSupportedException {
    ExprAttr exp = (ExprAttr) super.clone();

    // TODO: this can be further optimized to allocate the whole 
    // array in a batch
    exp.aValue = this.aValue.clone();
    exp.actualName = this.actualName;

    return exp;
  }
  
  public Attr getAttr() {
    Attr attr;
    try {
     attr = aValue.clone();
    } catch (CloneNotSupportedException ex)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      attr = null;
    }
    return attr;
  }

  public boolean check_reference(LogOpt op) {
    return aValue.check_reference(op);
  }

  
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    attrs.add(aValue);
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprAttr other = (ExprAttr)otherObject;
    return (aValue.equals(other.getAValue()));
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalAttributeExpression>");
    sb.append(super.toString());
    sb.append(aValue.toString());

    sb.append("</LogicalAttributeExpression>");
    return sb.toString();
  }

}
