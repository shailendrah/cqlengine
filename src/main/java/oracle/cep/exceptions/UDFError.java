/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/UDFError.java /main/6 2013/10/16 07:04:05 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/15/13 - adding parameters in the error
    sbishnoi    09/04/11 - adding new exception for numtodsinterval function
    sbishnoi    02/05/08 - parameterization of error message
    rkomurav    05/22/07 - add detail
    sbishnoi    05/08/07 - code cleanup
    parujain    04/16/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/UDFError.java /main/6 2013/10/16 07:04:05 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

public enum UDFError implements ErrorCode {
  USERDEFINED_FUNCTION_RUNTIME_ERROR(
      1,
      "user defined function {0} runtime error",
      ErrorType.ERROR,
      1,
      false,
      "Runtime error while using user defined function {0}",
      "Verify the implementation code for user defined function {0}"
  ),
  ILLEGAL_ARGUMENT_FOR_FUNCTION(
      2,
      "illegal argument for function",
      ErrorType.ERROR,
      1,
      false,
      "The argument or arguments specified for the function are not valid in this context.",
      "Check the definition of the function and correct the arguments. "
  ),
  ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED(
      3,
      "illegal argument {0} for function {1}",
      ErrorType.ERROR,
      1,
      false,
      "The argument or arguments {0} specified for the function {1} are not valid in this context.",
      "Check the definition of the function{1} and correct the arguments {0}. "
  ),
  ILLEGAL_TRIM_MODE_FUNCTION_TRIM(
      4,
      "Illegal trim mode {0} for function {1}.",
      ErrorType.ERROR,
      1,
      false,
      "The trim mode {0} specified for the function {1} are not valid in this context.",
      "TRIM mode should be one of LEADING, TRAILING, BOTH."
  ),
  ILLEGAL_TRIM_CHAR_FUNCTION_TRIM(
      5,
      "Illegal trim set {0} for function {1}.",
      ErrorType.ERROR,
      1,
      false,
      "Trim set {0} specified for the function {1} contains more or less than 1 character.",
      "Trim set should have exactly one character."
  ),
  ILLEGAL_OCCURENCE_FUNCTION_INSTR(
      6,
      "Illegal occurence argument {0} for function {1}.",
      ErrorType.ERROR,
      1,
      false,
      "Occurence argument {0} specified for the function {1} contains non-positive value. Argument value is out of range.",
      "Ocuurence argument should be a positive integer value."
  ),
  ILLEGAL_ARGUMENT_FUNCTION_LN(
      7,
      "Illegal  argument {0} for function {1}.",
      ErrorType.ERROR,
      1,
      false,
      "Argument {0} specified for the function {1}  is out of range.",
      "The input argument should greater than zero(0)."
  );

  private ErrorDescription ed;
  
  UDFError(int num, String text, ErrorType type, 
      int level, boolean isDocumented, String cause, String action)
  { 
    ed = new ErrorDescription(ErrorNumberBase.Server_Colt_BuiltinFunctions + num, text, type, level,
        isDocumented, cause, action, "UDFError");
  }
  
  public ErrorDescription getErrorDescription()
  {
    return ed;
  }
    
}
