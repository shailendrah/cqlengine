/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/ObjAttributeValue.java /main/4 2010/05/24 21:08:09 hopark Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/20/10 - remove _Serializable constraint since queue spilling
                           is not used with directinterop
    hopark      10/30/09 - add attrib name in toString
    hopark      02/02/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/ObjAttributeValue.java /main/4 2010/05/24 21:08:09 hopark Exp $
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
 * Represents an object attribute value
 *
 * @author najain
 */
public class ObjAttributeValue extends AttributeValue
{
  static final long serialVersionUID = 7437847149598276224L;
	
  /** attribute value */
  private  Object value;
  
  /**
   * @param attributeName name of the attribute
   */
  public ObjAttributeValue()
  {
    super(Datatype.OBJECT);
    setBNull(true);
  }

  public ObjAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.OBJECT);
    setBNull(true);
  }

  /**
   * Constructor for XmltypeAttributeValue
   * @param attributeName Attribute name
   * @param value Attribute value
   */
  public ObjAttributeValue(Object value)
  {
    super(Datatype.OBJECT);
    this.value = value;
    setBNull(false);
  }

  public ObjAttributeValue(String attributeName, Object value)
  {
    super(attributeName, Datatype.OBJECT);
    this.value = value;
    setBNull(false);
  }

  public ObjAttributeValue(String attributeName, Datatype dt)
  {
    super(attributeName, dt);
    setBNull(true);
  }

  public ObjAttributeValue(ObjAttributeValue other)
  {
    super(other);
    value = other.value;
    setBNull(other.isBNull());
  } 
  
  /**
   * Gets the value of an object attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public Object oValueGet() throws CEPException
  {
    return value;
  }

  /**
   * Sets the value of an object attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void oValueSet(Object v) throws CEPException
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
    value = val;
  }

  public String getStringValue()
  {
    if (value == null)
      return "null";
    return value.toString();
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<ObjAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
    {  
      String val = (value == null) ? "null":value.toString();
      sb.append("<Value>" + val + "</Value>");
    }
    sb.append("</ObjAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = in.readObject();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeObject(value);
  }
  
  public AttributeValue clone() throws CloneNotSupportedException
  {
    return new ObjAttributeValue(this);
  }  
}
