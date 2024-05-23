/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/XmltypeAttributeValue.java /main/7 2013/07/09 05:43:25 aiqbal Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

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
    mthatte     04/17/08 - using pointer to char[] value
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      10/19/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/XmltypeAttributeValue.java /main/7 2013/07/09 05:43:25 aiqbal Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.interfaces.InterfaceException;

/**
 * Represents a xmltype attribute value
 *
 * @author najain
 */
public class XmltypeAttributeValue extends AttributeValue
{
  static final long serialVersionUID = 7437847149598276224L;
	
  /** attribute value */
  private  char  value[];

  /** attribute length */
  private   int  length;


  /**
   * @param attributeName name of the attribute
   */
  public XmltypeAttributeValue()
  {
    super(Datatype.XMLTYPE);
    setBNull(true);
  }

  public XmltypeAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.XMLTYPE);
    setBNull(true);
  }

  /**
   * Constructor for XmltypeAttributeValue
   * @param attributeName Attribute name
   * @param value Attribute value
   */
  public XmltypeAttributeValue(char[] value)
  {
    super(Datatype.XMLTYPE);
    this.value = value;
    if (value == null)
        this.length = 0;
    else
      this.length = value.length;
    setBNull(false);
  }

  public XmltypeAttributeValue(String attributeName, char[] value)
  {
    super(attributeName, Datatype.XMLTYPE);
    this.value = value;
    if (value == null)
        this.length = 0;
    else
      this.length = value.length;
    setBNull(false);
  }

  public XmltypeAttributeValue(XmltypeAttributeValue other)
  {
    super(other);
    length = other.length;
    value = null;
    if (length > 0)
    {
      value = new char[length];
      System.arraycopy(other.value, 0, value, 0, length);
    }
  } 
  
  /**
   * Gets the value of an xmltype attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public char[] xValueGet() throws CEPException
  {
    return getValue();
  }

  /**
   * Sets the value of an xmltype attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void xValueSet(char[] v) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple,

    setValue(v);
  }

  /**
   * Gets the length of an xmltype attribute
   * 
   * @return Attribute length
   * @throws CEPException
   */
  public int xLengthGet() throws CEPException
  {
    return getLength();
  }

  /**
   * Sets the length of an xmltype attribute
   * 
   * @param l
   *          Attribute length to set
   * @throws CEPException
   */
  public void xLengthSet(int l) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple,

    setLength(l);
  }  

  public Object getObjectValue()
  {
    if (value == null)
      return value;
    return new String(value, 0, length);
  }

  public void setObjectValue(Object val)
  {
    if (val == null) {
      setBNull(true);
      return;
    }
    if (val instanceof char[]) {
      value = ((char[])val);
    } else if (val instanceof String) {
      value = ((String)val).toCharArray();
    }
    value = (val.toString()).toCharArray();
  }

  /**
   * Getter for length in XmltypeAttributeValue
   * @return Returns the length
   */
  public int getLength()
  {
    return length;
  }

  /**
   * Setter for length in XmltypeAttributeValue
   * @param length The length to set.
   */
  public void setLength(int length)
  {
    this.length = length;
  }

  /**
   * Getter for value in XmltypeAttributeValue
   * @return Returns the value
   */
  public char[] getValue()
  {
    return value;
  }

  public String getStringValue()
  {
      return value != null ? String.valueOf(value) : null;
  }

  /**
   * Setter for value in XmltypeAttributeValue. 
   * Length is updated with value.
   * @param value The value to set.
   */
  public void setValue(char[] v) throws InterfaceException
  {
    int l = 0; //initializing length to 0
    if (v == null)
      setBNull(true);
    else
    {
      l = v. length;
      setBNull(false);
      if ((value == null) || (value.length < l))
        value = new char[l];
      for (int i = 0; i < l; i++)
        value[i] = v[i];
    }
    this.length = l; //length = 0 if v==null or length = v.length
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<XmltypeAttribute ");
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
    sb.append("</XmltypeAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    length = in.readInt();
    value = null;
    if (length > 0)
    {
      value = new char[length];
      for (int i = 0; i < length; i++) 
      {
        value[i] = in.readChar();
      }
    }
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeInt(length);
    if (value != null)
    {
      for (int i = 0; i < length; i++)
      {
        out.writeChar(value[i]);
      }
    }
  }
  
  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new XmltypeAttributeValue(this);
  }  
}
