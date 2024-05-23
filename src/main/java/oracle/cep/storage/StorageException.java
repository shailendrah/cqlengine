/* $Header: StorageException.java 05-feb-2008.14:08:06 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/05/08 - parameterized error
    hopark      05/17/07 - Creation
 */

/**
 *  @version $Header: StorageException.java 05-feb-2008.14:08:06 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage;

import oracle.cep.exceptions.CEPRuntimeException;
import com.oracle.osa.exceptions.ErrorCode;

public class StorageException extends CEPRuntimeException
{
  /**
   * Constructs a new CEPRuntimeException with the specified error code
   * @param errorCode specified ErrorCode
   */
  public StorageException(ErrorCode errorCode)
  {
    super(errorCode);
  }

  /**
   * Constructs a new CEPRuntimeException with the specified error code
   * @param errorCode specified ErrorCode
   * @param cause exception cause
   */
  public StorageException(ErrorCode errorCode, Throwable cause)
  {
    super(errorCode, cause);
  }

  /**
   * Constructs a new CEPRuntimeException with the specified error code
   * @param errorCode specified ErrorCode
   * @param cause exception cause
   * @param args arguments to replace placeholders in the message
   */
  public StorageException(ErrorCode errorCode, Throwable cause, Object ... args)
  {
    super(errorCode, cause, args);
  }
}
