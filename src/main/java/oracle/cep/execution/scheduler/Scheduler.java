/* $Header: pcbpel/cep/server/src/oracle/cep/execution/scheduler/Scheduler.java /main/8 2008/11/18 21:46:34 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares Scheduler in package oracle.cep.execution.scheduler.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    11/15/08 - pass execContext
 hopark    10/09/08 - remove statics
 najain    07/09/07 - remove setThreaded
 skmishra  06/18/07 - 
 parujain  05/03/07 - scheduler stats
 parujain  02/23/07 - thread synchronization
 parujain  02/12/07 - addoperator Sched not instantiated
 parujain  11/29/06 - cleanup
 najain    10/25/06 - integrate with mds
 anasrini  09/01/06 - getStatus
 anasrini  08/29/06 - implement runnable
 najain    06/09/06 - operator sharing ref-count 
 skaluska  02/23/06 - Creation
 skaluska  02/23/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/scheduler/Scheduler.java /main/8 2008/11/18 21:46:34 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.scheduler;

import oracle.cep.service.ExecContext;

/**
 * The Scheduler interface
 * 
 * @author skaluska
 */
public interface Scheduler
{
  /** Schedule the operators */
  Runnable getNext(ExecContext ec);
}
