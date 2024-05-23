/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/UserFuncStats.java /main/1 2008/12/31 11:57:37 parujain Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    12/08/08 - user function stats
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/UserFuncStats.java /main/1 2008/12/31 11:57:37 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

public class UserFuncStats {
  private  int       numInvokations;
  private  long      time;

  public UserFuncStats()
  {
    numInvokations = 0;
    time = 0;
  }

  public void setTime(long time)
  {
    this.time = time;
  }

  public void setNumInvokations(int num)
  {
    this.numInvokations = num;
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

