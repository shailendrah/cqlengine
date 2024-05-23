/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/BigDecimalAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2009, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/26/13 - set bNull to false in nValueSet - bug 16164263
    sborah      06/17/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/BigDecimalAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * Internal representation of the BigDecimal attribute value
 * @author sborah
 *
 */
public class BigDecimalAttrVal extends AttrVal
{
  /** Attribute value */
  private BigDecimal value;
  
  private int scale;
  
  private int precision;

  /**
   * Empty constructor for BigDecimalAttrVal.
   * Invoked while deserialization of instances
   */
  public BigDecimalAttrVal()
  {
    super(Datatype.BIGDECIMAL);
  }
  
  /**
   * Constructor for BigDecimalAttrVal
   * @param val Attribute value
   */
  public BigDecimalAttrVal(BigDecimal val)
  {
    super(Datatype.BIGDECIMAL);
    value          = val;
    this.scale     = val.scale();
    this.precision = val.precision();
  }
  
  public BigDecimalAttrVal(double v, int precision, int scale) throws ExecException
  {
    super(Datatype.BIGDECIMAL);
    BigDecimal val           = new BigDecimal(Double.toString(v), 
                                   new MathContext(precision)
                                   ).setScale(scale, RoundingMode.HALF_UP);
    
 // The given number should be of the given precision value
    if(val.precision() > precision)
    {
      throw new ExecException(ExecutionError.PRECISION_ERROR, val.toString() , precision);
    }
    
    this.value     = val;
    this.scale     = scale;
    this.precision = precision;
  }

  /**
   * Getter for value in DoubleAttrVal
   * @return Returns the value
   */
  public BigDecimal nValueGet()
  {
    return value;
  }
  
  public int nPrecisionGet()
  {
    return this.precision;
  }

  public int nScaleGet()
  {
    return this.scale;
  }

  /**
   * Setter for value in DoubleAttrVal
   * @param value The value to set.
   */
  public void nValueSet(BigDecimal v, int precision, int scale) throws ExecException
  {
    // Refactor the bigdecimal
    
    
    BigDecimal val         = new BigDecimal(v.toString(), 
                                new MathContext(precision)
                                ).setScale(scale, RoundingMode.HALF_UP);
                                
       
    // The given number should be of the given precision value
    if(val.precision() > precision)
    {
      throw new ExecException(ExecutionError.PRECISION_ERROR, v.toString() , precision);
    }
    
    this.value     = val;
    this.precision = precision;
    this.scale     = scale;

    /* Bug 16164263 
     * When the value, precision and scale are set correctly without raising
     * any exception, then we should set the bNull flag to false. This will
     * prevent the attr from being treated as null. Not setting this was
     * causing the problem of getting null result in the above bug.
     */
    setBNull(false);    
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeObject(this.value);
    out.writeInt(this.precision);
    out.writeInt(this.scale);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.value = (BigDecimal) in.readObject();
    this.precision = in.readInt();
    this.scale = in.readInt();
  }

  
  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    writeExternal(out);
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    readExternal(in);    
  }

}

