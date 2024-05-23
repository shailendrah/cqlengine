/* $Header: SoftExecException.java 05-feb-2008.14:56:20 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares SoftExecException in package oracle.cep.execution.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    02/05/08 - parameterized error
    najain    03/14/07 - cleanup
    hopark    03/06/07 - spill support
    skaluska  02/25/06 - Creation
    skaluska  02/25/06 - Creation
 */

/**
 *  @version $Header: SoftExecException.java 05-feb-2008.14:56:20 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution;

import oracle.cep.exceptions.ExecutionError;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.operators.ExecOpt;

/**
 * SoftExecException is an exception that is due to temporary
 * conditions (such as resource problems). They don't imply a
 * user error or internal fatal problems. The high-level
 * operation getting this exception can be retried later.
 *
 * @author skaluska
 */
public class SoftExecException extends ExecException
{
  static final long serialVersionUID = 1;

  /**
   * Constructor for SoftExecException
   * @param err
   * @param opt
   * @param t
   */
  public SoftExecException(ExecutionError err, ExecOpt opt, ITuplePtr t)
  {
    // TODO Auto-generated constructor stub
    super(err, opt, t);
    
  }

  /**
   * Constructor for SoftExecException
   * @param err
   * @param cause
   */
  public SoftExecException(ExecutionError err, Throwable cause)
  {
    super(err, cause);
  }

  /**
   * Constructor for SoftExecException
   * @param err
   * @param cause
   */
  public SoftExecException(ExecutionError err, Throwable cause, Object ... args)
  {
    super(err, cause, args);
  }

  /**
   * Constructor for SoftExecException
   * @param err
   */
  public SoftExecException(ExecutionError err)
  {
    super(err);
  }

  /**
   * Constructor for SoftExecException
   * @param err
   */
  public SoftExecException(ExecutionError err, Object ... args)
  {
    super(err, args);
  }
}
