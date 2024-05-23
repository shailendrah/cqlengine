/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecOptTask.java /main/3 2013/06/11 08:46:11 udeshmuk Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    This is a wrapper class that implements Runnable and is used to
    run an ExecOpt on a thread. Typically, used by schedulers.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/21/13 - bug 16820093 - add new run method
    anasrini    01/28/09 - wrapper to run an ExecOpt
    anasrini    01/28/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecOptTask.java /main/3 2013/06/11 08:46:11 udeshmuk Exp $
 *  @author  anasrini
 *  @since   11.1
 */
package oracle.cep.execution.operators;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.execution.ExecManager;
import oracle.cep.service.ExecContext;

/**
 * This is a wrapper class that implements Runnable and is used to
 * run an ExecOpt on a thread. Typically, used by schedulers.
 *
 * @author anasrini
 */
public class ExecOptTask implements Runnable
{
  /** The execution operator to be run */
  private ExecOpt     op;

  /** Easy reference to Execution Manager */
  private ExecManager execMgr;

  /**
   * Constructor 
   *
   * @param execop The execution operator to run
   * @param ec     The global execution context
   */
  ExecOptTask(ExecOpt execop, ExecContext ec)
  {
    this.op      = execop;
    this.execMgr = ec.getExecMgr();
  }

  /**
   * Getter for the execution operator associated with this task
   * @return the execution operator associated with this task
   */
  public ExecOpt getExecOp()
  {
    return op;
  }

  /**
   * Run the execution operator
   */
  public void run()
  {
    execMgr.runOperator(op);
  }
  
  /**
   * Run the execution operator
   */
  public void run(TupleValue data, boolean overrideTime)
  {
    execMgr.runOperator((ExecSourceOpt)op, data, overrideTime);
  }

  public void run(TupleValue data)
  {    
    execMgr.runOperator((ExecSourceOpt) op, data);
  }
  
}

