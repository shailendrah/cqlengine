/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/CharAttributeValue.java /main/12 2013/07/09 05:43:25 aiqbal Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares CharAttributeValue in package oracle.cep.dataStructures.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    02/16/10 - correct setValue implementation
 hopark    10/30/09 - add attrib name in toString
 skmishra  03/30/09 - getObjectValue adding isBNull check
 hopark    10/15/08 - refactoring
 hopark    09/04/08 - fix TupleValue clone
 hopark    08/22/08 - fix externalization
 najain    05/02/07 - add constructor
 najain    03/12/07 - bug fix
 najain    02/08/07 - coverage
 najain    10/29/06 - add toString
 najain    09/06/06 - add constructor
 skaluska  03/17/06 - change names to String 
 skaluska  02/18/06 - Creation
 skaluska  02/18/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/CharAttributeValue.java /main/12 2013/07/09 05:43:25 aiqbal Exp $
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
 * Represents a char attribute value
 *
 * @author najain
 */
public class CharAttributeValue extends AttributeValue
{
  static final long serialVersionUID = -700801106227110877L;
	
  /** attribute value */
  private char value[];

  /** attribute length */
  private int  length;

  /**
   * @param attributeName name of the attribute
   */
  public CharAttributeValue()
  {
    super(Datatype.CHAR);
    setBNull(true);
  }

  public CharAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.CHAR);
    setBNull(true);
  }

  /**
   * Constructor for CharAttributeValue
   * @param attributeName Attribute name
   * @param value Attribute value
   */
  public CharAttributeValue(char[] value)
  {
    super(Datatype.CHAR);
    this.value = value;
    if (value == null)
        this.length = 0;
    else
      this.length = value.length;
    setBNull(false);
  }

  public CharAttributeValue(String attributeName, char[] value)
  {
    super(attributeName, Datatype.CHAR);
    this.value = value;
    if (value == null)
        this.length = 0;
    else
      this.length = value.length;
    setBNull(false);
  }

  public CharAttributeValue(CharAttributeValue other)
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
   * Gets the value of an char attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public char[] cValueGet() throws CEPException
  {
    return getValue();
  }

  public long lValueGet() throws CEPException
  {
	return Long.parseLong(new String(value));
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
   * Sets the value of an char attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws CEPException
   */
  public void cValueSet(char[] v) throws CEPException
  {
    //In case bNull was set to true for the previous tuple, 
    //A call to ValueSet implies that this tuple is not null
    bNull = false;        
    
    setValue(v);
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
    //In case bNull was set to true for the previous tuple, 
    //A call to ValueSet implies that this tuple is not null
    bNull = false;   
    
    setValue(v);
  }

  /**
   * Gets the length of an char attribute
   * 
   * @return Attribute length
   * @throws CEPException
   */
  public int cLengthGet() throws CEPException
  {
    return getLength();
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
   * Sets the length of an char attribute
   * 
   * @param l
   *          Attribute length to set
   * @throws CEPException
   */
  public void cLengthSet(int l) throws CEPException
  {
    bNull = false;  // In case bNull was set to true for the previous tuple, 
                    // A call to ValueSet implies that this tuple is not null
    setLength(l);
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
    else if(bNull)
      return null;
    else
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
   * Getter for length in CharAttributeValue
   * @return Returns the length
   */
  public int getLength()
  {
    return length;
  }

  /**
   * Setter for length in CharAttributeValue
   * @param length The length to set.
   */
  private void setLength(int length)
  {
    this.length = length;
  }

  /**
   * Getter for value in CharAttributeValue
   * @return Returns the value
   */
  public char[] getValue()
  {
    return value;
  }

  public String getStringValue()
  {
    return (value!= null) ? String.valueOf(value) : null;
  }

  /**
   * Setter for value in CharAttributeValue
   * @param value The value to set.
   */
  private void setValue(char[] value)
  {
    // NOTE: this would create problems if the character array
    // passed by the user is modified.
    this.value = value;
    this.length = (value == null) ? 0 : value.length;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<CharAttribute ");
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
    sb.append("</CharAttribute>");
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
	  return new CharAttributeValue(this);
  }
}
