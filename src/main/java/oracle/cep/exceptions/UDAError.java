/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/UDAError.java /main/8 2013/09/16 01:55:02 pkali Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Enumeration of error codes corresponding to user defined aggregations
    runtime

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       09/11/13 - added invalid quantile range
    pkali       12/17/12 - added invalid numeric error
    sbishnoi    02/07/11 - cleanup
    sbishnoi    02/03/11 - adding errors related to Median
    sborah      05/19/10 - Typo
    rkomurav    05/22/07 - add detail
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error descriptions
    anasrini    07/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/UDAError.java /main/8 2013/09/16 01:55:02 pkali Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of error codes corresponding to user defined aggregations
 * runtime
 *
 * @since 1.0
 */

public enum UDAError implements ErrorCode {
  HANDLER_STORAGE_ELEMENT_ALLOCATION_ERROR(
    1,
    "handler storage element allocation error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Error in allocating handler for UDA",
    "Verify the UDA handler implementation for allocation of new handler"
  ),
  INVALID_OBJECT_PARAM_IN_MEDIAN(
      2,
      "input parameter value for the median aggregate function is not comparable",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "an incomparable input parameter value passed to median aggregate function",
      "use comparable input values to calculate the median aggregate function"
    ),
  INVALID_NUMERIC_VALUE(
      3,
      "invalid numeric value '{0}'",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "input value is not of numeric type",
      "provide input type as int, long, float, double"
    ),
  INVALID_DATA_TYPE(
        4,
        "invalid data type value '{0}'",
        ErrorType.INTERNAL_ERROR,
        1,
        false,
        "input value is not of expected data type",
        "provide input type as '{1}'"
      ),
  INVALID_QUANTILE_RANGE(
        5,
        "invalid quantile range value, sepcify range between 0 and 1 inclusive",
        ErrorType.INTERNAL_ERROR,
        1,
        false,
        "specified qunatile value is not with the range of 0 to 1",
        "provide qunatile range value between 0 and 1 inclusive"
      ),
 INVALID_UNIT(
        6,
        "invalid unit received, unit can either be kph for km/hr or mph for miles/hr",
        ErrorType.INTERNAL_ERROR,
        1,
        false,
        "invalid unit received, unit can either be kph for km/hr or mph for miles/hr",
        "provide unit either in kph or mph");
  
  private ErrorDescription ed;

  UDAError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Extensibility_BuiltinFunctions + num, text, type, level,
        isDocumented, cause, action, "UDAError");
  }

  public ErrorDescription getErrorDescription() {
    return ed;
  }

}
