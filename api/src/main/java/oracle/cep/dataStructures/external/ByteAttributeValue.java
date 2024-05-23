/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/ByteAttributeValue.java /main/12 2013/07/09 05:43:25 aiqbal Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares ByteAttributeValue in package oracle.cep.dataStructures.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    02/21/10 - correct setValue implementation
 hopark    10/30/09 - add attrib name in toString
 hopark    05/14/09 - add constructor without value
 hopark    10/15/08 - refactoring
 hopark    09/04/08 - fix TupleValue clone
 hopark    08/22/08 - fix externalization
 hopark    04/08/08 - fix instantiationException
 najain    05/02/07 - add constructor
 najain    03/12/07 - bug fix
 najain    02/08/07 - coverage
 najain    10/29/06 - add toString
 skaluska  03/17/06 - change names to String 
 skaluska  02/18/06 - Creation
 skaluska  02/18/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/ByteAttributeValue.java /main/12 2013/07/09 05:43:25 aiqbal Exp $
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
 * Represents a byte attribute value
 * 
 * @author najain
 */
public class ByteAttributeValue extends AttributeValue
{
  static final long serialVersionUID = -6308120724323148677L;
	
  /** Attribute value */
  private byte value[];

  /** Attribute length */
  private int  length;

  public ByteAttributeValue()
  {
    super(Datatype.BYTE);
    setBNull(true);
  }
  
  /**
   * Constructor for ByteAttributeValue
   * 
   * @param attributeName
   *          Attribute name
   * @param value
   *          Attribute value
   */
  public ByteAttributeValue(byte[] value)
  {
    super(Datatype.BYTE);
    this.value = value;
    if (value == null)
      this.length = 0;
    else
      this.length = value.length;
    setBNull(false);
  }

  public ByteAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.BYTE);
    setBNull(true);
  }

  public ByteAttributeValue(String attributeName, byte[] value)
  {
    super(attributeName, Datatype.BYTE);
    this.value = value;
    if (value == null)
      this.length = 0;
    else
      this.length = value.length;
    setBNull(false);
  }


  public ByteAttributeValue(ByteAttributeValue other)
  {
    super(other);
    length = other.length;
    value = null;
    if (length > 0)
    {
      value = new byte[length];
      System.arraycopy(other.value, 0, value, 0, length);
    }
  }
     
  /**
   * Gets the value of an byte attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public byte[] bValueGet() throws CEPException
  {
    return getValue();
  }

  /**
   * Sets the value of an byte attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void bValueSet(byte[] v) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                                        //A call to ValueSet implies that this tuple is not null
    setValue(v);
  }

  /**
   * Gets the length of an byte attribute
   * 
   * @return Attribute length
   * @throws CEPException
   */
  public int bLengthGet() throws CEPException
  {
    return getLength();
  }

  /**
   * Sets the length of an byte attribute
   * 
   * @param l
   *          Attribute length to set
   * @throws CEPException
   */
  public void bLengthSet(int l) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                                        //A call to ValueSet implies that this tuple is not null
    setLength(l);
  }

  
  public Object getObjectValue()
  {
    if (value != null)
    {
      if (value.length == length)
        return value;
      byte[] v = new byte[length];
      System.arraycopy(value, 0, v, 0, length);
      return v;
    }
    return value;
  }

  public void setObjectValue(Object val)
  {
    if (val == null) {
      setBNull(true);
      return;
    }
    if (val instanceof byte[]) {
      value = ((byte[])val);
    } else if (val instanceof String) {
      value = ((String)val).getBytes();
    }
    throw new RuntimeException("Invalid value for byte array type : " + val.getClass().getName());
  }

  /**
   * Getter for length in ByteAttributeValue
   * 
   * @return Returns the length
   */
  public int getLength()
  {
    return length;
  }

  /**
   * Setter for length in ByteAttributeValue
   * 
   * @param length
   *          The length to set.
   */
  private void setLength(int length)
  {
    this.length = length;
  }

  /**
   * Getter for value in ByteAttributeValue
   * 
   * @return Returns the value
   */
  public byte[] getValue()
  {
    return value;
  }

  public String getStringValue()
  {
    return (value!= null) ? new String(value) : null;
  }

  /**
   * Setter for value in ByteAttributeValue
   * 
   * @param value
   *          The value to set.
   */
  private void setValue(byte[] value)
  {
    // NOTE: this would create problems if the byte array
    // passed by the user is modified.
    this.value = value;
    this.length = (value == null) ? 0 : value.length;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<ByteAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
    {  
      String val = new String(value, 0, length);
      sb.append("<Value>" + val + "</Value>");
      sb.append("<Length>" + length + "</Length>");
    }
    sb.append("</ByteAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    length = in.readInt();
    value = null;
    if (length > 0)
    {
      value = new byte[length];
      in.read(value, 0, length);
    }
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeInt(length);
    if (value != null)
    {
      out.write(value, 0, length);
    }
  }

  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new ByteAttributeValue(this);
  }
 }
