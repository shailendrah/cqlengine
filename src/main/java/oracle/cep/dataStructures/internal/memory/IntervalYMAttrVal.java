/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/IntervalYMAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2011, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/27/11 - Creation
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/IntervalYMAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class IntervalYMAttrVal extends AttrVal
{
  /** number of months*/
  long interval;
  
  /** format of the interval string value */
  IntervalFormat format;
  
  /**
   * Zero Argument Constructor for IntervalYMAttrVal
   * Invoked while deserialization of instances of IntervalYMAttrVal type
   */
  public IntervalYMAttrVal()
  {
    super(Datatype.INTERVALYM);
  }
  
  /**
   * Constructor for interval value
   * @param val number of nanoseconds
   */
  public IntervalYMAttrVal(long val, IntervalFormat format)
  {
    super(Datatype.INTERVALYM);
    this.interval = val;
    this.format   = format;
  }
  
  /**
   * Return interval value 
   * @return number of nanoseconds interval
   */
  public long getInterval()
  {
    return interval;
  }
  
  /**
   * Set the interval value
   * @param value number of nanoseconds interval
   */
  public void setInterval(long value)
  {
    interval = value;
  }

  /**
   * @return the format
   */
  public IntervalFormat getFormat()
  {
    return format;
  }

  /**
   * @param format the format to set
   */
  public void setFormat(IntervalFormat format)
  {
    this.format = format;
  }

  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeLong(this.interval);
    out.writeObject(this.format);
  }

  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.interval = in.readLong();
    this.format = (IntervalFormat) in.readObject();
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