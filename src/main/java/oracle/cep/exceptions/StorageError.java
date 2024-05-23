/* $Header: StorageError.java 05-feb-2008.14:03:34 hopark Exp $ */

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
    hopark      05/21/07 - add cause, action
    sbishnoi    05/08/07 - code cleanup
    najain      02/01/07 - Creation
 */

/**
 *  @version $Header: StorageError.java 05-feb-2008.14:03:34 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Error code for the Storage related errors 
 */

public enum StorageError implements ErrorCode
{
  INVALID_STORAGE_CLASS_CONFIG(
    1, 
    "storage class not specified correctly",
    ErrorType.INTERNAL_ERROR, 
    1, 
    false, 
    "internal error", 
    "internal error"),
    
  INIT_FAILED(
    2, 
    "not able to init the storage on {0}",
    ErrorType.ERROR, 
    1, 
    true, 
    "The storage subsystem failed to initialize on {0}.", 
    "Check the storage folder({0}) if it exists or report to Oracle as an internal error"),
    
  INVALID_TOOMANY_STORAGE_CLASS(
    3, 
    "too many storage managers specified",
    ErrorType.INTERNAL_ERROR, 
    1, 
    false, 
    "internal error", 
    "internal error"),

  OBJECT_NOT_WRITTEN(
    4, 
    "unable to write the object({0})",
    ErrorType.INTERNAL_ERROR, 
    1, 
    false, 
    "The storage subsystem failed to write the object({0}).", 
    "Check the storage disk if it is out-of-disk or report to Oracle as an internal error"),
 
  OBJECT_NOT_READ(
    5, 
    "unable to read the object for {0}",
    ErrorType.INTERNAL_ERROR, 
    1, 
    false, 
    "The storage subsystem failed to read the object for {0}.", 
    "Check the storage disk for hardware failure or report to Oracle as an internal error"),
 
  OBJECT_NOT_DELETED(
    6, 
    "unable to delete the object for {0}",
    ErrorType.INTERNAL_ERROR, 
    1, 
    false, 
    "The storage subsystem failed to delete the object for {0}.", 
    "Check the storage disk for hardware failure or report to Oracle as an internal error"),
    
  TRANSACTION_FAILED(
    7, 
    "unable to perform transaction",
    ErrorType.INTERNAL_ERROR, 
    1, 
    false, 
    "internal error", 
    "internal error");

  private ErrorDescription ed;
  
  StorageError(int num, String text, ErrorType type, int level, 
	       boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Exceptions + num, text, type, level,
        isDocumented, cause, action, "StorageError");
  }

  public ErrorDescription getErrorDescription(){
    return ed;
  }
  
}
