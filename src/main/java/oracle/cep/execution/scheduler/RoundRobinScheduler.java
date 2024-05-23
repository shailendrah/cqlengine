/* $Header: pcbpel/cep/server/src/oracle/cep/execution/scheduler/RoundRobinScheduler.java /main/19 2009/05/12 19:25:47 parujain Exp $ */
/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares RoundRobinScheduler in package oracle.cep.execution.scheduler.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  05/08/09 - lifecycle mgmt
 anasrini  01/28/09 - getNext returns ExecOpt
 hopark    11/15/08 - pass execContext
 hopark    10/09/08 - remove statics
 najain    07/09/07 - remove setThreaded
 najain    07/06/07 - remove valid
 skmishra  06/18/07 - add thread pooling code to round robin scheduler
 hopark    07/13/07 - dump stack trace on exception
 hopark    05/22/07 - logging support
 hopark    05/11/07 - remove System.out.println(use java.util.logging instead)
 parujain  05/03/07 - scheduler stats
 parujain  04/23/07 - runtime exceptions for multithreaded
 najain    04/20/07 - print debug info.
 parujain  04/17/07 - Runtime error handling
 hopark    03/25/07 - set thread name
 parujain  02/23/07 - thread synchronization
 parujain  02/12/07 - addoperator Sched not instantiated
 najain    11/06/06 - minor enhancements
 najain    10/25/06 - integrate with mds
 najain    10/25/06 - integrate with mds
 anasrini  08/14/06 - make it Runnable
 najain    07/25/06 - concurrency support 
 najain    06/09/06 - operator sharing ref-count 
 najain    06/09/06 - minor enhancements
 najain    03/31/06 - fix bug 
 skaluska  02/24/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/scheduler/RoundRobinScheduler.java /main/19 2009/05/12 19:25:47 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.scheduler;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.execution.ExecManager;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecOptTask;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * RoundRobinScheduler
 * 
 * @author skaluska
 */
public class RoundRobinScheduler implements Scheduler
{
  private Iterator<ExecOpt>         itr = null;
  boolean recurse;

  /**
   * Constructor for RoundRobinScheduler
   */
  public RoundRobinScheduler()
  {
    recurse = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.scheduler.Scheduler#run(long)
   */
  public ExecOptTask getNext(ExecContext ec)
  {
    ExecManager execMgr = ec.getExecMgr();
    if (itr == null)
    {
      Collection<ExecOpt> ops = execMgr.getOperatorArray();
      itr = ops.iterator();
    }
    while (true)
    {
      if (itr.hasNext())
      {
        ExecOpt op = null;
        try
        {
          op = itr.next();
	  if ((op == null) || (!op.canBeScheduled()) || 
	      (!op.shouldBeScheduled()))
	    continue;
        }
        catch (ConcurrentModificationException ex)
        {
	  LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
          Collection<ExecOpt> ops = execMgr.getOperatorArray();
          itr = ops.iterator();
          continue;
        }

        return op.getExecOpTask();
      }
      else
      {
	if (recurse) 
	{
	  recurse = false;
	  return null;
	}

	Collection<ExecOpt> ops = execMgr.getOperatorArray();
        itr = ops.iterator();
        if (itr.hasNext())
	{
	  recurse = true;
          ExecOptTask ret = getNext(ec);
	  recurse = false;
	  return ret;
	}
	else
	  return null;
      }
    }
  }
}
