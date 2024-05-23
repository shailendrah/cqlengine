/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/AttributeMetadata.java /main/3 2012/01/20 11:47:14 sbishnoi Exp $ */

package oracle.cep.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/03/11 - timestamp with timezone
    sbishnoi    08/29/11 - support for interval year to month based operations
    sborah      10/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/AttributeMetadata.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

public class AttributeMetadata implements Externalizable
{
  private static final long serialVersionUID = -5223985636589089746L;

  private Datatype dt;
  private int length;
  private int precision;
  private int scale;
  
  /** Used for interval data type only to store the format of interval value */
  private IntervalFormat intervalFormat;
  
  /** Used for timestamp datatype only to store the format of timestamp value*/
  private TimestampFormat timestampFormat;
  
  public AttributeMetadata()
  {
  }

  public AttributeMetadata(Datatype dt)
  {
    this.dt        = dt;
    this.length    = -1;
    this.precision = 1;
    this.scale     = 0;
    this.intervalFormat    = dt.getIntervalFormat();
    this.timestampFormat   = dt.getTimestampFormat();
  }
  
  public AttributeMetadata(AttributeMetadata other)
  {
    this.dt        = other.dt;
    this.length    = other.length;
    this.precision = other.precision;
    this.scale     = other.scale;
    this.intervalFormat    = other.getIntervalFormat();    
    this.timestampFormat   = other.getTimestampFormat();
  }
  
  public AttributeMetadata(Datatype dt, int length , int precision ,int scale)
  {
    this.dt        = dt;
    this.length    = length;
    this.precision = precision;
    this.scale     = scale;
    this.intervalFormat    = dt.getIntervalFormat();
    this.timestampFormat   = dt.getTimestampFormat();
  }
  
  public Datatype getDatatype()
  {
    return this.dt;
  }
  
  public void setDatatype(Datatype type)
  {
    this.dt = type;
  }
  
  public int getLength()
  {
    return this.length;
  }
  
  public void setLength(int length)
  {
    this.length = length;
  }
  
  public int getPrecision()
  {
    return this.precision;
  }
  
  public int getScale()
  {
    return this.scale;
  }

  /**
   * @return the format
   */
  public IntervalFormat getIntervalFormat()
  {
    return intervalFormat;
  }

  /**
   * @param format the format to set
   */
  public void setIntervalFormat(IntervalFormat format)
  {
    this.intervalFormat = format;
  }

  /**
   * @return the timestampFormat
   */
  public TimestampFormat getTimestampFormat()
  {
    return timestampFormat;
  }

  /**
   * @param timestampFormat the timestampFormat to set
   */
  public void setTimestampFormat(TimestampFormat timestampFormat)
  {
    this.timestampFormat = timestampFormat;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(length);
      out.writeInt(precision);
      out.writeInt(scale);
      out.writeObject(dt);
      out.writeObject(intervalFormat);
      out.writeObject(timestampFormat);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
		ClassNotFoundException {
      length = in.readInt();
      precision = in.readInt();
      scale = in.readInt();
      dt = (Datatype) in.readObject();
      intervalFormat = (IntervalFormat) in.readObject();
      timestampFormat = (TimestampFormat) in.readObject();
 }
  
}