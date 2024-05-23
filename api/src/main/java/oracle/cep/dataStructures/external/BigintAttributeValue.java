/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/BigintAttributeValue.java /main/7 2009/11/21 07:38:14 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 BigintAttributeValue represents an attribute value of BIGINT type 
 
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
    hopark    10/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/BigintAttributeValue.java /main/7 2009/11/21 07:38:14 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;

/**
 * Represents a BIGINT attribute value
 * 
 * @author skaluska
 */
public class BigintAttributeValue extends AttributeValue  
{
  static final long serialVersionUID = -1459419439592360306L;
	
  private long value;

  /**
   * @param attributeName Attribute Name
   */
  public BigintAttributeValue()
  {
    super(Datatype.BIGINT);
    setBNull(true);
  }

  public BigintAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.BIGINT);
    setBNull(true);
  }


  public BigintAttributeValue(BigintAttributeValue other)
  {
    super(other);
    value = other.value;
  }
 
  /**
   * Constructor for BigintAttributeValue
   * 
   * @param attributeName
   *          Attribute name
   * @param value
   *          Attribute value
   */
  public BigintAttributeValue(long value) {
    super(Datatype.BIGINT);
    this.value = value;
    setBNull(false);
  }

  public BigintAttributeValue(String attributeName, long value) {
    super(attributeName, Datatype.BIGINT);
    this.value = value;
    setBNull(false);
  }

  /**
   * Gets the value of an int attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public long lValueGet() throws CEPException
  {
    return value;
  }
  
  /**
   * Sets the value of an int attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void lValueSet(long v) throws CEPException
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
      value = ((Number)val).longValue();
    } else if (val instanceof String) {
      value = Long.parseLong( ((String)val).trim());
    }
    value = Long.parseLong(val.toString().trim());
  }

  public String getStringValue()
  {
    return String.valueOf(value);
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<BigintAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + value + "</Value>");
    sb.append("</BigintAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = in.readLong();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeLong(value);
  }

  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new BigintAttributeValue(this);
  }
}
