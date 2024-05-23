/* $Header: pcbpel/cep/server/src/oracle/cep/execution/scheduler/FIFOScheduler.java /main/29 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    parujain    05/08/09 - lifecycle mgmt
    anasrini    01/28/09 - getNext returns ExecOptTask
    hopark      11/15/08 - pass execContext
    hopark      10/09/08 - remove statics
    najain      03/25/08 - 
    sbishnoi    12/14/07 - 
    parujain    12/05/07 - bug fix
    hopark      10/22/07 - remove TimeStamp
    najain      07/26/07 - fix concurrency bug
    najain      07/25/07 - fix stack trace problem
    hopark      05/22/07 - logging support
    hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
    parujain    05/03/07 - scheduler stats
    parujain    04/23/07 - runtime exception handling for multithreaded
    najain      04/20/07 - print debug info.
    parujain    04/17/07 - Runtime error handling
    hopark      03/25/07 - set thread name
    parujain    03/16/07 - debug level
    parujain    02/23/07 - thread synchronization
    parujain    02/13/07 - interfaces with ConfigManager
    rkomurav    11/22/06 - throw excpetions correctly
    najain      11/06/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/scheduler/FIFOScheduler.java /main/29 2009/05/12 19:25:47 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecOptTask;
import oracle.cep.execution.ExecManager;
import oracle.cep.service.ExecContext;

/**
 * FIFOScheduler
 * 
 * @author najain
 */
public class FIFOScheduler implements Scheduler
{
  private boolean opRunning;
  
  /** Constructor for FIFOScheduler */
  public FIFOScheduler()
  {
  }

  public ExecOptTask getNext(ExecContext ec)
  {
    ExecManager execMgr = ec.getExecMgr();
    Collection<ExecOpt> ops = execMgr.getOperatorArray();
    Iterator<ExecOpt>  itr = ops.iterator();
    ExecOpt   op = null;
    long ts = -1;

    while (itr.hasNext())
    {
      ExecOpt curr = null;
      try
      {
        curr = itr.next();
      }
      catch (ConcurrentModificationException ex)
      {
        ops = execMgr.getOperatorArray();
        itr = ops.iterator();
        op = null;
        ts = -1;
        continue;
      }
      
      if (curr != null)
      {
        if (curr.canBeScheduled() && curr.shouldBeScheduled())
        {
          if(op == null)
          {
            op = curr;
            ts = curr.getOldestTs();
          }
          else if (curr.getOldestTs() < ts)
          {
            op = curr;
            ts = curr.getOldestTs();
          }
	}
      }
    }

    if (op != null)
      return op.getExecOpTask();
    else
      return null;
  }
}
