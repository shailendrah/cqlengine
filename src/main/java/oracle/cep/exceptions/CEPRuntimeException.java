/* $Header: CEPRuntimeException.java 03-mar-2006.16:19:52 ayalaman Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    The base class for all CEP Runtime Exceptions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    ayalaman    03/03/06 - The base class for all CEP Runtime Exceptions
    ayalaman    03/03/06 - Creation
 */

/**
 *  @version $Header: CEPRuntimeException.java 03-mar-2006.16:19:52 ayalaman Exp $
 *  @author  ayalaman
 *  @since   1.0
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;

/**
 * The base class for all CEP Runtime Exceptions
 */
public class CEPRuntimeException extends RuntimeException
{
  private ErrorCode errorCode;
  private Object[]  args;

  /**
   * Constructs a new CEPRuntimeException with the specified error code
   * @param errorCode specified ErrorCode
   */
  public CEPRuntimeException(ErrorCode errorCode)
  {
    this.errorCode = errorCode;
  }

  /**
   * Constructs a new CEPRuntimeException with the specified error code
   * @param errorCode specified ErrorCode
   * @param args arguments to replace placeholders in the message
   */
  public CEPRuntimeException(ErrorCode errorCode, Object[] args)
  {
    this.errorCode = errorCode;
    this.args      = args;
  }
  
  /**
   * Constructs a new CEPRuntimeException with the specified error code and
   * cause
   * @param errorCode specified ErrorCode
   * @param cause the cause. A null value is permitted, and indicates that
   * the cause is nonexistent or unknown.
   */
  public CEPRuntimeException(ErrorCode errorCode, Throwable cause)
  {
    super(cause);
    this.errorCode = errorCode;
  }

  /**
   * Constructs a new CEPRuntimeException with the specified error code and 
   * cause
   * @param errorCode specified ErrorCode
   * @param cause the cause. A null value is permitted, and indicates that
   * the cause is nonexistent or unknown.
   * @param args arguments to replace placeholders in the message
   */
  public CEPRuntimeException(ErrorCode errorCode, Throwable cause,
                             Object[] args)
  {
    super(cause);
    this.errorCode = errorCode;
    this.args = args;
  }

  /**
   * Override this method to return the locale specific error message 
   * corresponding to the error code.
   * @return locale specific error message corresponding to the error code
   */
  public String getMessage()
  {

    // TEMPORARY IMPLEMENTATION !
    // For the moment, just return the key to the error message
    return ErrorHelper.getMessageKey(errorCode);
  }

  /**
   * get the error code which resulted in this exception being raised
   * @return the error code associated with this exception
   */
  public ErrorCode  getErrorCode()
  {
    return errorCode;
  }

}
