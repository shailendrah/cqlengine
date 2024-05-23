/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/IntAttributeValue.java /main/7 2009/11/21 07:38:14 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares IntAttributeValue in package oracle.cep.dataStructures.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    10/30/09 - add attrib name in toString
    hopark    10/15/08 - refactoring
    hopark    09/04/08 - fix TupleValue clone
    hopark    08/22/08 - fix externalization
    najain    05/01/07 - add constructor
    najain    03/12/07 - bug fix
    najain    10/29/06 - add toString
    najain    09/06/06 - add constructor
    skaluska  03/17/06 - change names to String 
    skaluska  02/17/06 - Creation
    skaluska  02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/IntAttributeValue.java /main/7 2009/11/21 07:38:14 hopark Exp $
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
 * Represents an Integer attribute value
 * 
 * @author skaluska
 */
public class IntAttributeValue extends AttributeValue 
{
  static final long serialVersionUID = -2137037858332604437L;
	
  private int value;

  /**
   * @param attributeName Attribute Name
   */
  public IntAttributeValue()
  {
    super(Datatype.INT);
    setBNull(true);
  }

  public IntAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.INT);
    setBNull(true);
  }

  /**
   * Constructor for IntAttributeValue
   * 
   * @param attributeName
   *          Attribute name
   * @param value
   *          Attribute value
   */
  public IntAttributeValue(String attributeName, int value) {
    super(attributeName, Datatype.INT);
    this.value = value;
    setBNull(false);
  }

  public IntAttributeValue(int value) {
    super(Datatype.INT);
    this.value = value;
    setBNull(false);
  }

  public IntAttributeValue(IntAttributeValue other)
  {
    super(other);
    value = other.value;
  }
    
  /**
   * Gets the value of an int attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public int iValueGet() throws CEPException
  {
    return value;
  }

  public long lValueGet() throws CEPException
  {
	return (long) value;
  }
  
  /**
   * Sets the value of an int attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void iValueSet(int v) throws CEPException
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
      value = ((Number)val).intValue();
    } else if (val instanceof String) {
      value = Integer.parseInt( ((String)val).trim());
    }
    value = Integer.parseInt(val.toString().trim());
  }

  public String getStringValue()
  {
    return String.valueOf(value);
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<IntAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + value + "</Value>");
    sb.append("</IntAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = in.readInt();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeInt(value);
  }

  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new IntAttributeValue(this);
  }  

}
