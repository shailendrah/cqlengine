/* $Header: pcbpel/cep/src/oracle/cep/execution/statistics/UserFuncStats.java /main/2 2008/08/14 13:33:41 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    08/12/08 - fix bug
    parujain    05/02/07 - Function statistics
    parujain    05/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/execution/statistics/UserFuncStats.java /main/2 2008/08/14 13:33:41 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.statistics;

public class UserFuncStats {
  private  int       numInvokations;
  private  long      time;
  
  public UserFuncStats( long t)
  {
    this.numInvokations = 1;
    this.time = (t <= 0) ? 0 : t;
  }
  
  public void incrNumInvokations()
  {
    numInvokations++;
  }
  
  public void setTime(long t)
  {
    this.time = (t <= 0) ? 0 : t;
  }
  
  public void incrTime(long t)
  {
    if(t > 0)
      time += t;
  }
  
  public int getNumInvokations()
  {
    return this.numInvokations;
  }
  
  public long getTime()
  {
    return this.time;
  }
  
}
