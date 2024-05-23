/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/BooleanAttributeValue.java /main/5 2009/11/21 07:38:14 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/30/09 - add attrib name in toString
    hopark      10/15/08 - refactoring
    hopark      09/04/08 - fix TupleValue clone
    hopark      08/22/08 - fix externalization
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      11/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/BooleanAttributeValue.java /main/5 2009/11/21 07:38:14 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;

/**
 * Represents an Boolean attribute value
 * 
 * @author najain
 */
public class BooleanAttributeValue extends AttributeValue 
{
  static final long serialVersionUID = 4739793860874777869L;
	
  private boolean value;

  /**
   * @param attributeName Attribute Name
   */
  public BooleanAttributeValue()
  {
    super(Datatype.BOOLEAN);
    setBNull(true);
  }

  public BooleanAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.BOOLEAN);
    setBNull(true);
  }

  public BooleanAttributeValue(BooleanAttributeValue other)
  {
    super(other);
    value = other.value;
  }
   
  /**
   * Constructor for BooleanAttributeValue
   * 
   * @param attributeName
   *          Attribute name
   * @param value
   *          Attribute value
   */
  public BooleanAttributeValue(String attributeName, boolean value) {
    super(attributeName, Datatype.BOOLEAN);
    this.value = value;
    setBNull(false);
  }

  public BooleanAttributeValue(boolean value) {
    super(Datatype.BOOLEAN);
    this.value = value;
    setBNull(false);
  }

  public boolean boolValueGet() throws CEPException
  {
    return value;
  }
  
  /**
   * Sets the value of an boolean attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void boolValueSet(boolean v) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
    value = v;
  }
  
  public Object getObjectValue()
  {
    return value;
  }

  public void setObjectValue(Object val) {
    if (val == null) {
      setBNull(true);
      return;
    }
    if (val instanceof Boolean) {
      value = ((Boolean)val).booleanValue();
    } else  if (val instanceof Number) {
      value = ((Number)val).intValue() != 0;
    } else if (val instanceof String) {
      value = Boolean.parseBoolean( ((String)val).trim());
    }
    value = Boolean.parseBoolean(val.toString().trim());
  }

  public String getStringValue()
  {
    return String.valueOf(value);
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<BooleanAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + value + "</Value>");
    sb.append("</BooleanAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = in.readBoolean();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeBoolean(value);
  }

  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new BooleanAttributeValue(this);
  }
}
