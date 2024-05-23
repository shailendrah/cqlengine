/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/FloatAttributeValue.java /main/7 2009/11/21 07:38:14 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares FloatAttributeValue in package oracle.cep.dataStructures.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    10/30/09 - add attrib name in toString
 hopark    10/15/08 - refactoring
 hopark    09/04/08 - fix TupleValue clone
 hopark    08/22/08 - fix externalization
 najain    05/02/07 - add constructor
 najain    03/12/07 - bug fix
 najain    10/29/06 - add toString
 najain    09/06/06 - add constructor
 skaluska  03/17/06 - change names to String 
 skaluska  02/17/06 - Creation
 skaluska  02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/FloatAttributeValue.java /main/7 2009/11/21 07:38:14 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;

/**
 * Represents a floating point attribute value
 *
 * @author skaluska
 */
public class FloatAttributeValue extends AttributeValue
{
  static final long serialVersionUID = -695487631679310353L;
	
  /** attribute value */
  private float value;

  /**
   * @param attributeName Name of the attribute
   */
  public FloatAttributeValue()
  {
    super(Datatype.FLOAT);
    setBNull(true);
  }

  public FloatAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.FLOAT);
    setBNull(true);
  }

  /**
   * Constructor for FloatAttributeValue
   * @param attributeName Name of the attribute
   * @param value Attribute value
   */
  public FloatAttributeValue(float value)
  {
    super(Datatype.FLOAT);
    this.value = value;
    setBNull(false);
  }

  public FloatAttributeValue(String attributeName, float value)
  {
    super(attributeName, Datatype.FLOAT);
    this.value = value;
    setBNull(false);
  }

  public FloatAttributeValue(FloatAttributeValue other)
  {
    super(other);
    value = other.value;
  }
  
  /**
   * Gets the value of a float attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public float fValueGet() throws CEPException
  {
    return value;
  }

  public long lValueGet() throws CEPException
  {
	return (long) value;
  }
  
  /**
   * Sets the value of an float attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void fValueSet(float v) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                                        //A call to ValueSet implies that this tuple is not null
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
    if (val instanceof Number) {
      value = ((Number)val).floatValue();
    } else if (val instanceof String) {
      value = Float.parseFloat(((String)val).trim());
    }
    value = Float.parseFloat(val.toString().trim());
  }

  public String getStringValue()
  {
    return String.valueOf(value);
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<FloatAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + value + "</Value>");
    sb.append("</FloatAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = in.readFloat();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeFloat(value);
  }
  
  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new FloatAttributeValue(this);
  }  
}
