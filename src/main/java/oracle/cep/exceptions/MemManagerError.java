/* $Header: MemManagerError.java 21-may-2007.15:29:31 parujain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/21/07 - add cause and action
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error descriptions
    anasrini    02/01/07 - Messages
    rkomurav    11/25/06 - Creation
 */

/**
 *  @version $Header: MemManagerError.java 21-may-2007.15:29:31 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the MemManger Module
 * @since 1.0
 */
public enum MemManagerError implements ErrorCode
{
  NO_ACTUAL_ERROR(
    1,
    "no actual error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "An internal error occurred.",
    "An internal error occurred."
  );
  
  private ErrorDescription ed;

  MemManagerError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Test + num, text, type, level,
        isDocumented, cause, action, "MemManagerError");
  }

  public ErrorDescription getErrorDescription()
  {
    return ed;
  }
  
}
