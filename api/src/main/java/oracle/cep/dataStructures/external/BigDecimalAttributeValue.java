package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;

/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/BigDecimalAttributeValue.java /main/3 2013/07/09 05:43:25 aiqbal Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/01/11 - fix serialization
    sborah      06/17/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/BigDecimalAttributeValue.java /main/3 2013/07/09 05:43:25 aiqbal Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appe
 *  
 *  ar in)
 */

public class BigDecimalAttributeValue extends AttributeValue
{
  // Bug 28311047 INCOMPATIBLE SERIALVERSIONUID FOR BIGDECIMALATTRIBUTEVALUE CLASS
  static final long serialVersionUID = -4338839130194306670L;
  
  /** attribute value */ 
  private BigDecimal value;
  
  private int precision;
  
  private int scale;
  
  /**
   * @param attributeName Name of the attribute
   */
  public BigDecimalAttributeValue()
  {
    super(Datatype.BIGDECIMAL);
    setBNull(true);
  }

  public BigDecimalAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.BIGDECIMAL);
    setBNull(true);
  }

  /**
   * Constructor for DoubleAttributeValue
   * @param attributeName Name of the attribute
   * @param value Attribute value
   */
  public BigDecimalAttributeValue(BigDecimal value)
  {
    super(Datatype.BIGDECIMAL);
    this.value     = value;
    this.precision = value.precision();
    this.scale     = value.scale();
    setBNull(false);
  }

  public BigDecimalAttributeValue(String attributeName, BigDecimal value)
  {
    super(attributeName, Datatype.BIGDECIMAL);
    this.value     = value;
    this.precision = value.precision();
    this.scale     = value.scale();
    setBNull(false);
  }
  
  public BigDecimalAttributeValue(String attributeName, int precision, 
         int scale, double value)
  {
    super(attributeName, Datatype.BIGDECIMAL);
    
    this.value = new BigDecimal(Double.toString(value), 
                                new MathContext(precision)
                                ).setScale(scale, RoundingMode.HALF_UP);
    this.precision = precision;
    this.scale     = scale;
    setBNull(false);
    
  }

  public BigDecimalAttributeValue(BigDecimalAttributeValue other)
  {
    super(other);
    value     = other.value;
    precision = other.precision;
    scale     = other.scale;
    setBNull(other.bNull);
  }
  
  /**
   * Get the value of BigDecimal attribute
   * 
   * @return bigdecimal attribute value
   * @throws CEPException
   */
  public BigDecimal nValueGet() throws CEPException
  {
    return value;
  }
  
  public long lValueGet() throws CEPException
  {
	return value.longValue();
  }
  
  
  /**
   * Sets the value of an bigdecimal attribute
   * 
   * @param v
   *        The bigdecimal value to be set
   * @param precision
   *          Attribute precision to set
   * @param scale
   *          Attribute scale to set
   * @throws CEPException
   */
  public void nValueSet(BigDecimal v, int precision, int scale) throws CEPException
  {
    bNull          = false;  //In case bNull was set to true for the previous tuple, 
    //A call to ValueSet implies that this tuple is not null
    
    BigDecimal val          = new BigDecimal(v.toString(), 
                                    new MathContext(precision)
                                   ).setScale(scale, RoundingMode.HALF_UP);
   
     //  The given number should be of the given precision value
    if(val.precision() > precision)
    {
      throw new CEPException(ExecutionError.PRECISION_ERROR, v.toString() , precision);
    }
    
    this.value     = val;
    this.precision = precision;
    this.scale     = scale;
  }
  
  /**
   * Returns the precision of the bigdecimal value
   * 
   * @return bigdecimal precision value
   */
  public int nPrecisionGet()
  {
    return this.precision;
  }
  
  /**
   * Returns the scale of the bigdecimal value
   * 
   * @return bigdecimal scale value
   */
  public int nScaleGet()
  {
    return this.scale;
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
    if (val instanceof BigDecimal) {
      value = (BigDecimal) val;
    } else {
      MathContext mc = new MathContext(precision, this.getAttributeType().getRoundingMode());
      if (val instanceof Integer) {
        value = new BigDecimal( ((Integer)val).intValue(), mc);
      } else if (val instanceof Long) {
        value = new BigDecimal( ((Long)val).longValue(), mc);
      } else if (val instanceof Float) {
        value = new BigDecimal( ((Float)val).floatValue(), mc);
      } else if (val instanceof Double) {
        value = new BigDecimal(((Double) val).doubleValue(), mc);
      } else if (val instanceof String) {
        value = new BigDecimal( ((String)val).trim(), mc);
      }
      value = new BigDecimal( val.toString().trim(), mc);
    }
  }

  public String getStringValue()
  {
      return (value!= null) ? String.valueOf(value) : null;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<BigDecimalAttribute>");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + value + "</Value>");
    sb.append("</BigDecimalAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    value = (BigDecimal) in.readObject();
    precision = in.readInt();
    scale = in.readInt();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeObject(value);
    out.writeInt(precision);
    out.writeInt(scale);
  }

  public AttributeValue clone() throws CloneNotSupportedException
  {
    return new BigDecimalAttributeValue(this);
  }

  
}

