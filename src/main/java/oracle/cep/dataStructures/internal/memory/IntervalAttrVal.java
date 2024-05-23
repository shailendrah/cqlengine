/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/IntervalAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

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
    hopark      09/04/07 - optimize
    najain      03/12/07 - bug fix
    parujain    10/06/06 - Interval datatype
    parujain    10/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/IntervalAttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.execution.snapshot.IPersistenceContext;

public class IntervalAttrVal extends AttrVal
{
  long interval;
  
  IntervalFormat format;
  
  /**
   * Zero Argument Constructor for IntervalAttrVal.
   * Invoked while deserialization of instances of IntervalAttrVal type.
   */
  public IntervalAttrVal()
  {
    super(Datatype.INTERVAL);
  }
  
	public IntervalAttrVal(long val, IntervalFormat format)
	{
		super(Datatype.INTERVAL);
		this.interval = val;
		this.format = format;
	}
	
	public long getInterval()
	{
		return interval;
	}
	
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