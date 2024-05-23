/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprAttr.java /main/16 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Attribute Physical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    05/13/12 - separate on . only for attr ref in getsqlequivalent
 udeshmuk    06/20/11 - support getSQLEquivalent
 udeshmuk    03/31/11 - archived relation support
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      10/05/09 - bigdecimal support
 sborah      04/24/09 - add length info
 sborah      04/20/09 - define getSignature
 parujain    11/07/07 - actual name
 rkomurav    06/18/07 - cleanup.
 rkomurav    03/05/07 - rework attr conversion
 rkomurav    11/28/06 - add equals method
 rkomurav    10/10/06 - add equals method
 rkomurav    09/11/06 - cleanup for xmldump
 rkomurav    08/23/06 - add getXMLPlan2
 anasrini    06/05/06 - instantiate aValue in dummy constructor 
 najain      03/24/06 - cleanup
 anasrini    03/23/06 - fix bug - set type and kind 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprAttr.java /main/16 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.AttributeExpression;
import oracle.cep.extensibility.expr.ExprKind;

/**
 * Attribute Physical Operator Expression Class Definition
 */
public class ExprAttr
    extends Expr
    implements AttributeExpression
{
  /** Attribute value */
  Attr aValue;

  /** String for actual name like S.c1 */
  String actualName;
  
  AttributeMetadata attrMetadata;
  
  /** length of the attribute value*/
  //int length;

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

  public String getAttributeName() {
    return getActualName();
  }

  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      The value of the Expr Attr in String format
   */
  public String getSignature()
  {
    /** 
     * Use the actual name of the attribute.
     * For A.c1 , where A is an alias name of stream
     * S, the actual name is S.c1
     */
    return (aValue.getInput() + "." + aValue.getPos()).toString();
  }

  /**
   * length of the attribute
   * 
   * @return length of the attribute
   */
  public int getLength()
  {
    if(attrMetadata.getLength() == -1)
      return super.getLength();
    
    return attrMetadata.getLength();
  }
  
  public ExprAttr(Datatype dt) 
  {
    super(ExprKind.ATTR_REF);
    setType(dt);
    // allocate memory for the attribute
    aValue      = new Attr();
    this.attrMetadata = new AttributeMetadata(dt);
  }

  public ExprAttr(Attr attr, Datatype dt) 
  {
    super(ExprKind.ATTR_REF);
    setType(dt);
    this.aValue       = attr;
    this.attrMetadata = new AttributeMetadata(dt);
    
  }
  
  public ExprAttr(Attr attr, AttributeMetadata attrMetadata)
  {
    super(ExprKind.ATTR_REF);
    setType(attrMetadata.getDatatype());
    this.aValue       = attr;
    this.attrMetadata = attrMetadata;
  }
  
  public ExprAttr(Attr attr, Datatype dt, int length) 
  {
    super(ExprKind.ATTR_REF);
    setType(dt);
    this.aValue       = attr;
    this.attrMetadata = new AttributeMetadata(dt, length, 0 , 0);
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprAttr other = (ExprAttr) otherObject;
    return (aValue.equals(other.getAValue())); 
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorAttributeExpression>");
    sb.append(super.toString());
    sb.append(aValue.toString());
    sb.append(actualName);
    sb.append("</PhysicalOperatorAttributeExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    return aValue.getXMLPlan2();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    attrs.add(aValue);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    /** 
     * Get the name of the attr from the actual name of the attribute.
     * For A.c1,generally the part of attrname that is of interest is c1.
     * Only when specifically we want fully qualified name e.g. join
     * then that flag in execcontext will be set.
     */
    
    if(actualName != null)
    {
      if((this.getKind() == ExprKind.ATTR_REF)
         && (!ec.shouldReturnFullyQualifiedAttrName()))
      {
        int idx = actualName.lastIndexOf(".");
        if(idx != -1)
         return actualName.substring(idx+1);
      }
      return actualName;
    }
    else
      return null;
  }

}
