/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TimestampAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/12/12 - adding timestamp format
 hopark      10/22/07 - remove TimeStamp
 hopark      09/04/07 - optimize
 najain      03/12/07 - bug fix
 parujain    08/10/06 - Constant timestamp handling
 parujain    08/03/06 - Timestamp datastructure
 parujain    08/03/06 - Creation
 */
package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.common.TimestampFormat;
import oracle.cep.execution.snapshot.IPersistenceContext;

/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TimestampAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public class TimestampAttrVal extends AttrVal
{
  /** Attribute value * */
  long time;

  /** timestamp format value*/
  TimestampFormat format;
  /**
   * Constructor of TimestampAttrVal
   *
   */
  public TimestampAttrVal()
  {
    super(Datatype.TIMESTAMP);
    time = 0;
    format = TimestampFormat.getDefault();
  }
  /**
   * Constructor of TimestampAttrVal
   * 
   * @param ts
   *          Timestamp given by the user
   */
  public TimestampAttrVal(long ts)
    {
      super(Datatype.TIMESTAMP);
      time = ts;
      format = TimestampFormat.getDefault();
    }

  /**
   * Get the timestamp value
   * 
   * @return Long value of timestamp
   */
  public long getTime()
  {
    return time;
  }

  /**
   * Sets the timestamp value
   * 
   * @param ts
   *          Timestamp value to be set
   */
  public void setTime(long ts)
  {
    time = ts;
  }
  
  /**
   * @return the format
   */
  public TimestampFormat getFormat()
  {
    return format;
  }
  
  /**
   * @param format the format to set
   */
  public void setFormat(TimestampFormat format)
  {
    this.format = format;
  }
  
  @Override
  public void writeExternal(ObjectOutput out)
      throws IOException
  {
    super.writeExternal(out);
    out.writeLong(this.time);
    out.writeObject(this.format);
  }
  
  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
  {
    super.readExternal(in);
    this.time = in.readLong();
    this.format = (TimestampFormat) in.readObject();       
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
