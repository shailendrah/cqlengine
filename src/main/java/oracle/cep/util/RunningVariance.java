/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/RunningVariance.java /main/2 2013/09/27 08:50:42 pkali Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       09/20/13 - exposed the count & variance info, to check the input status
                           (Bug 17189426)
    pkali       12/16/12 - incremental variance computation
    pkali       12/16/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/RunningVariance.java /main/2 2013/09/27 08:50:42 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/*
 * Running Variance is calculated based on the 'Method for Calculating
 * Corrected Sums of Squares and Products' by B. P. Welford
 * http://zach.in.tu-clausthal.de/teaching/info_literatur/Welford.pdf
 * 
 */

public class RunningVariance implements Cloneable, Externalizable
{
  private static final long serialVersionUID = 1053731525533519433L;
  
  // we can't parameterize this class, since we do not know the data type while 
  // initializing the class. Based on the input we will know for which data type
  // the variance is being computed. So keep separate variables to handle different 
  // data types. Each instance will compute variance for a particular data type.
  
  //single counter will be enough since there will be a single instance
  //for each data type
  private long count;
  
  private float fmean;
  private float fsumsqr;
  private float fVariance;
  
  //to handle double data type
  private double dmean;
  private double dsumsqr;
  private double dVariance;
  
  //to handle big decimal data type
  private BigDecimal bdmean;
  private BigDecimal bdsumsqr;
  private BigDecimal bdvariance;
  
  //store the variance in an object to handle the null input cases
  //where the input data type is unknown
  private Object objVariance;
  
  private boolean isSample = false;

  public RunningVariance()
  {      
  }
  
  public RunningVariance(boolean isSampleVariance)
  {
    count = 0L;
    fmean = 0.0f;
    fsumsqr = 0.0f;
    
    dmean = 0.0d;
    dsumsqr = 0.0d;
    
    bdmean = new BigDecimal(0);
    bdsumsqr = new BigDecimal(0);
    bdvariance = new BigDecimal(0);
    
    isSample = isSampleVariance;
  }

  
  public float add(float n)
  {
    count++;
    float delta = n - fmean;
    fmean = fmean + delta / count;
    fsumsqr = fsumsqr + delta * (n - fmean);

    if (count <= 0)
      return 0.0f;
    if(isSample)
      fVariance = fsumsqr / (count - 1);
    else
      fVariance = fsumsqr / (count);
    objVariance = (Float) fVariance;
    return fVariance;
  }

  public float remove(float n)
  {
    if (count <= 0)
      return 0.0f;
    count--;
    if (count == 0)
      return 0.0f;
    float delta = n - fmean;
    fmean = fmean - delta / count;
    fsumsqr = fsumsqr - delta * (n - fmean);
    if (count <= 0)
      return 0.0f;
    if(isSample)
      fVariance = fsumsqr / (count - 1);
    else
      fVariance = fsumsqr / (count);
    objVariance = (Float) fVariance;
    return fVariance;
  }
    
  public double add(double n)
  {
    count++;
    double delta = n - dmean;
    dmean = dmean + delta / count;
    dsumsqr = dsumsqr + delta * (n - dmean);

    if (count <= 0)
      return 0.0d;
    if(isSample)
      dVariance = dsumsqr / (count - 1);
    else
      dVariance = dsumsqr / (count);
    objVariance = (Double) dVariance;
    return dVariance;
  }

  public double remove(double n)
  {
    if (count <= 0)
      return 0.0d;
    count--;
    if (count == 0)
      return 0.0d;
    double delta = n - dmean;
    dmean = dmean - delta / count;
    dsumsqr = dsumsqr - delta * (n - dmean);
    if (count <= 0)
      return 0.0d;
    if(isSample)
      dVariance = dsumsqr / (count - 1);
    else
      dVariance = dsumsqr / (count);
    objVariance = (Double) dVariance;
    return dVariance;
  }
  
  public BigDecimal add(BigDecimal n)
  {
    if(n != null)
    {
      count++;
      BigDecimal delta = n.subtract(bdmean);
      bdmean = bdmean.add(delta.divide(new BigDecimal(count), RoundingMode.HALF_UP));
      bdsumsqr = bdsumsqr.add(delta.multiply(n.subtract(bdmean)), 
                                         new MathContext(n.precision()));

      //BigDecimal doesn't have a representation for NaN
      //so for sample variance return 0 instead of NaN when the count is 1
      if (count == 1)
      {
        return getBigDecimalZero(n);
      }
      if(isSample)
        bdvariance = bdsumsqr.divide(new BigDecimal(count - 1), n.scale(), RoundingMode.HALF_UP);
      else
        bdvariance = bdsumsqr.divide(new BigDecimal(count), n.scale(), RoundingMode.HALF_UP);
    }
    objVariance = bdvariance;
    return bdvariance;
  }
  
  public BigDecimal remove(BigDecimal n)
  {
    if(n != null)
    {
      if (count <= 0)
        return getBigDecimalZero(n);
      count--;
      if (count == 0)
        return getBigDecimalZero(n);
      BigDecimal delta = n.subtract(bdmean);
      bdmean = bdmean.subtract(delta.divide(new BigDecimal(count), RoundingMode.HALF_UP));
      bdsumsqr = bdsumsqr.subtract(delta.multiply(n.subtract(bdmean)), 
                                             new MathContext(n.precision()));
      
      if (count == 1)
        return getBigDecimalZero(n);
      if(isSample)
        bdvariance = bdsumsqr.divide(new BigDecimal(count - 1), n.scale(), RoundingMode.HALF_UP);
      else
        bdvariance = bdsumsqr.divide(new BigDecimal(count), n.scale(), RoundingMode.HALF_UP);
    }
    objVariance = bdvariance;
    return bdvariance;
  }
  
  //returns zero value of big decimal based on the 
  //input precision and scale settings.
  //Need to do divide operation to propagate the settings
  private BigDecimal getBigDecimalZero(BigDecimal n)
  {
      objVariance = new BigDecimal(0, new MathContext(n.precision()))
                  .divide(new BigDecimal(1), n.scale(), RoundingMode.HALF_UP);
      return (BigDecimal)objVariance;
  }
  
  public long getCount()
  {
    return count;  
  }
  
  public float getFloatVariance()
  {
    return fVariance;
  }
  
  public double getDoubleVariance()
  {
    return dVariance;
  }
  
  public BigDecimal getBigDecimalVariance()
  {
    return bdvariance;
  }
  
  public Object getVariance()
  {
    return objVariance;
  }
  
  public Object clone()
  {
    RunningVariance myClone = new RunningVariance(this.isSample);
    myClone.count = this.count;
    myClone.fmean = this.fmean;
    myClone.fsumsqr = this.fsumsqr;
    myClone.fVariance = this.fVariance;
    myClone.dmean = this.dmean;
    myClone.dsumsqr = this.dsumsqr;
    myClone.dVariance = this.dVariance;
    myClone.bdmean = this.bdmean;
    myClone.bdsumsqr = this.bdsumsqr;
    myClone.bdsumsqr = this.bdvariance;
    myClone.objVariance = this.objVariance;
    return myClone;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(count); 
    out.writeFloat(fmean);
    out.writeFloat(fsumsqr);
    out.writeFloat(fVariance);
    out.writeDouble(dmean); 
    out.writeDouble(dsumsqr); 
    out.writeDouble(dVariance);
    out.writeObject(bdmean);
    out.writeObject(bdsumsqr);
    out.writeObject(bdvariance);
    out.writeObject(objVariance); 
    out.writeBoolean(isSample);
  }


  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    count = in.readLong();
    fmean = in.readFloat();
    fsumsqr = in.readFloat();
    fVariance = in.readFloat();
    dmean = in.readDouble();
    dsumsqr = in.readDouble();
    dVariance = in.readDouble();
    bdmean = (BigDecimal) in.readObject();
    bdsumsqr = (BigDecimal) in.readObject();
    bdvariance = (BigDecimal) in.readObject();
    objVariance = in.readObject();
    isSample = in.readBoolean();
  }
}

