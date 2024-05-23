/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/comparator/ArchiverTupleComparator.java /main/1 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/03/12 - ArchiverTupleComparator used by StreamSource
    udeshmuk    06/03/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/comparator/ArchiverTupleComparator.java /main/1 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.comparator;

import java.util.Comparator;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;

public class ArchiverTupleComparator implements Comparator<ITuplePtr>
{
  int tsColNum;
  
  Datatype tsColType;
  
  //true, if the comparator should compare in ascending order.
  boolean isAscending;
  
  //Constructor
  public ArchiverTupleComparator(int colNum, Datatype colType, boolean isAscending)
  {
    tsColNum = colNum;
    tsColType = colType;
    this.isAscending = isAscending;
  }
  
  @Override
  public int compare(ITuplePtr o1, ITuplePtr o2)
  {
    long val1 = Long.MIN_VALUE, val2 = Long.MIN_VALUE;
    ITuple o1Tuple = null;
    ITuple o2Tuple = null;
    try
    {
      o1Tuple = o1.pinTuple(IPinnable.READ);
      o2Tuple = o2.pinTuple(IPinnable.READ);
      //tuple with null should go at the end irrespective of the ordering 
      if(o1Tuple.isAttrNull(tsColNum))
        return 1;
      if(o2Tuple.isAttrNull(tsColNum))
        return -1;
      //get the values to be compared
      if(tsColType == Datatype.TIMESTAMP)
      {
        val1 = o1Tuple.tValueGet(tsColNum);
        val2 = o2Tuple.tValueGet(tsColNum);
      }
      else if(tsColType == Datatype.BIGINT)
      {
        val1 = o1Tuple.lValueGet(tsColNum);
        val2 = o2Tuple.lValueGet(tsColNum);
      }
      //as per the ordering return appropriate value
      if(!isAscending)
      {
        val1 = -val1; 
        val2 = -val2;
      }
      if(val1 < val2) 
        return -1;
      else if (val1 > val2) 
        return 1;
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    finally
    {
      try {
        o1.unpinTuple();
        o2.unpinTuple();
      }
      catch(ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj)
  {
    //shouldn't be called
    assert false;
    return false;
  }
  
}