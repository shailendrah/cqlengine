/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/windows/GenericTimeWindow.java /main/3 2011/10/03 01:51:59 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/13/11 - cleanup
    sbishnoi    07/30/08 - support for nanosecond timestamp;
    parujain    03/02/07 - Generic Window Interface
    parujain    03/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/extensibility/windows/GenericTimeWindow.java /main/2 2008/08/18 21:52:48 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.windows;

import java.io.IOException;

import oracle.cep.common.EventTimestamp;

public interface GenericTimeWindow {
  
  /**
   * Sets the input parameters for a particular instance
   * 
   * @param obj Objects with constant values
   * @throws IOException 
   */
  public void setInputParams(Object[] obj) throws IOException;
  
  /**
   * This returns the timestamp when the given timestamp will 
   * become visible
   * 
   * If an element will never become visible, the returns false
   * 
   * @param t
   *         given timestamp (in nanosecond)
   * @param visTs
   *            timestamp(nanosecond) when given timestamp will become visible
   */
  public boolean visibleW(EventTimestamp t, EventTimestamp visTs);
  
  /**
   * This returns the timestamp when the given timestamp will
   * get expired for the first time
   * 
   * If an element with timestamp t will never expire then 
   * it will return false.
   * 
   * @param t
   *        given timestamp (in nanosecond)
   * @param expTs
   *          Timestamp(nanosecond) when given timestamp will get expired
   */
  public boolean expiredW(EventTimestamp t, EventTimestamp expTs);  
  
}
