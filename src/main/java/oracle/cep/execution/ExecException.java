/* $Header: pcbpel/cep/server/src/oracle/cep/execution/ExecException.java /main/12 2009/04/27 10:18:28 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ExecException in package oracle.cep.execution.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  04/26/09 - changing parameter for OutOfOrderError
 sbishnoi  04/20/09 - modifying OutOfOrderException
 sborah    02/16/09 - fix for bug 8258442
 parujain  07/08/08 - value based windows
 hopark    02/05/08 - parameterized error
 hopark    10/22/07 - remove TimeStamp
 rkomurav  09/04/07 - fix params passing for out of order error
 hopark    05/16/07 - add out of order exception helper
 najain    03/14/07 - cleanup
 hopark    03/06/07 - spill support
 skaluska  02/18/06 - exception chaining 
 skaluska  02/13/06 - Creation
 skaluska  02/13/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/ExecException.java /main/12 2009/04/27 10:18:28 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * @author skaluska
 */
public class ExecException extends CEPException
{
  static final long serialVersionUID = 1;

  /* current operator */
  public ExecOpt    op;

  /* current tuple */
  public ITuplePtr      tup;

  /**
   * Constructor for ExecException
   * 
   * @param err
   *          ExecutionError code
   */
  public ExecException(ExecutionError err)
  {
    super(err);
  }

  /**
   * Constructor for ExecException
   * 
   * @param err
   *          ExecutionError code
   */
  public ExecException(ExecutionError err, Object ... args)
  {
    super(err, args);
  }
  
  /**
   * Constructor for ExecException
   * 
   * @param err
   *          ExecutionError code
   * @param cause
   *          Throwable error causing the exception
   */
  public ExecException(ExecutionError err, Throwable cause, Object ... args)
  {
    super(err, cause, args);
  }

  /**
   * Constructor for ExecException
   * 
   * @param err
   *          ExecutionError code
   * @param opt
   *          Current operator
   * @param t
   *          Current tuple
   */
  public ExecException(ExecutionError err, ExecOpt opt, ITuplePtr t)
  {
    super(err);
    op = opt;
    tup = t;
  }

  /**
   * Constructor for ExecException
   * 
   * @param err
   *          ExecutionError code
   * @param opt
   *          Current operator
   * @param t
   *          Current tuple
   */
  public ExecException(ExecutionError err, ExecOpt opt, ITuplePtr t, Object ... args)
  {
    super(err, args);
    op = opt;
    tup = t;
  }
  
  /**
   * Constructor for ExecException
   * 
   * @param err
   *          ExecutionError code
   * @param opt
   *          Current operator
   * @param t
   *          Current tuple
   */
  public ExecException(ExecutionError err, ExecOpt opt, Object ... args)
  {
    super(err, args);
    op = opt;
  }
  
  public static SoftExecException OutOfOrderException(ExecOpt opt,
      long ts0, long ts1, String elemstr)
  {
    Object[] args = new Object[4];
    args[0] = ts0;
    args[1] = ts1;
    args[2] = elemstr;
    args[3] = opt.getOptName();
    // bug no : 8258442
    // OutOfOrderException should not cause the query to be stopped
    // It should simply discard the offending tuple.
    return new SoftExecException(ExecutionError.OUT_OF_ORDER, args);
  }
  
  public static ExecException ValueOutOfOrderException(ExecOpt opt,
       long val0, long val1, String elemstr)
  {
    Object[] args = new Object[4];
    args[0] = val0;
    args[1] = val1;
    args[2] = elemstr;
    args[3] = opt.getOptName();
    
    return new ExecException(ExecutionError.VALUE_OUT_OF_ORDER, args);
  }
  
  public static ExecException ValueOutOfOrderException(ExecOpt opt,
	       double val0, double val1, String elemstr)
  {
    Object[] args = new Object[4];
    args[0] = val0;
    args[1] = val1;
    args[2] = elemstr;
    args[3] = opt.getOptName();
    
    return new ExecException(ExecutionError.VALUE_OUT_OF_ORDER, args);
  }
}
