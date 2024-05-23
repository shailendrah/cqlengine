/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/EventTimestamp.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2008, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/01/08 - Creation
 */
package oracle.cep.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/EventTimestamp.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class EventTimestamp implements Externalizable
{
  /** Represents a time value in nanosecond unit */
  private long timeVal;
  
  /**
   * Zero Argument Constructor
   * Invoked while deserialization of instances of EventTimestamp type
   */
  public EventTimestamp()
  {    
  }
  
  /**
   * Constructs an EventTimestamp object with given timeVal
   * @param timeVal
   */
  public EventTimestamp(long timeVal) {
    this.timeVal = timeVal;
  }  
  
  /**
   * Get a nanosecond time value represented by this EventTimestamp object 
   * @return nanosecond time 
   */
  public long getTime(){
    return timeVal;
  }
  
  /**
   * Set EventTimestamp Object to a nanosecond time value
   * @param timeVal
   */
  public void setTime(long timeVal){
   this.timeVal = timeVal; 
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(this.timeVal);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.timeVal = in.readLong();    
  }
}
