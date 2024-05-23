/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/DoubleAttributeValue.java /main/5 2009/11/21 07:38:14 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
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
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/DoubleAttributeValue.java /main/5 2009/11/21 07:38:14 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;

/**
 * Represents a double precision floating point attribute value
 *
 * @author udeshmuk
 */
public class DoubleAttributeValue extends AttributeValue
{
  static final long serialVersionUID = 3748324720407006083L;
	 
  /** attribute value */
  private double value;

  /**
   * @param attributeName Name of the attribute
   */
  public DoubleAttributeValue()
  {
    super(Datatype.DOUBLE);
    setBNull(true);
  }

  public DoubleAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.DOUBLE);
    setBNull(true);
  }

  /**
   * Constructor for DoubleAttributeValue
   * @param attributeName Name of the attribute
   * @param value Attribute value
   */
  public DoubleAttributeValue(double value)
  {
    super(Datatype.DOUBLE);
    this.value = value;
    setBNull(false);
  }

  public DoubleAttributeValue(String attributeName, double value)
  {
    super(attributeName, Datatype.DOUBLE);
    this.value = value;
    setBNull(false);
  }

  public DoubleAttributeValue(DoubleAttributeValue other)
  {
    super(other);
    value = other.value;
  }
    
  /**
   * Get the value of double attribute
   * 
   * @return double attribute value
   * @throws CEPException
   */
  public double dValueGet() throws CEPException
  {
    return value;
  }
  
  public long lValueGet() throws CEPException
  {
	return (long) value;
  }
  
  public void dValueSet(double v) throws CEPException
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
      value = ((Number)val).doubleValue();
    } else if (val instanceof String) {
      value = Double.parseDouble(((String)val).trim());
    }
    value = Double.parseDouble(val.toString().trim());
  }

  public String getStringValue()
  {
    return String.valueOf(value);
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<DoubleAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + value + "</Value>");
    sb.append("</DoubleAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = in.readDouble();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeDouble(value);
  }

  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new DoubleAttributeValue(this);
  }
 }
