/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/DataStructuresError.java /main/7 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares DataStructuresError in package oracle.cep.exceptions.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  08/24/11 - adding interval exceptions
    hopark    02/05/08 - parameterized error
    mthatte   12/06/07 - adding invalid_timestamp_format
    parujain  05/21/07 - add cause and action
    sbishnoi  05/08/07 - code cleanup
    skmishra  02/01/07 - add error descriptions
    anasrini  02/01/07 - Messages
    parujain  10/10/06 - Interval format error
    skaluska  03/06/06 - 
    anasrini  03/01/06 - 
    skaluska  02/18/06 - Creation
    skaluska  02/18/06 - Creation
 */

/**
 *  @version $Header: DataStructuresError.java 05-feb-2008.13:41:12 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * DataStructuresError
 * 
 * @author skaluska
 */

public enum DataStructuresError implements ErrorCode
{
  INVALID_POSITION(
    1,
    "invalid attribute position {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Attribute position({0}) found to be invalid",
    "internal error"
  ),

  TYPE_MISMATCH(
    2,
    "type mismatch: attribute type {0}, expected {1}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Attribute value cannot be read or set due to datatype mismatch. Attribute type is {0} but {1} is expected.",
    "Provide a valid attribute value."
  ),

  INVALID_TUPLE(
    3,
    "invalid tuple",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Incorrect input tuple.",
    "Provide a valid tuple."
  ),

  INVALID_FORMAT(
    4,
    "invalid format : {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Invalid interval attribute value format({0}) provided in tuple.",
    "Verify the interval attribute value for correct format. "
  ),
  DATETIME_INTERVAL_PRECISION_OUT_OF_RANGE(
    5,
    "datetime/interval precision is out of range",
    ErrorType.ERROR,
    1,
    true,
    "The specified datetime/interval precision was not between 0 and 9.",
    "Use a value between 0 and 9 for datetime/interval precision."      
  ),
  LEADING_PRECISION_TOO_SMALL(
    6,
    "the leading precision of the interval is too small",
    ErrorType.ERROR,
    1,
    true,
    "The leading precision of the interval is too small to store the specified interval.",
    "Increase the leading precision of the interval or specify an interval with a smaller leading precision."
  ),
  NOT_A_VALID_MONTH(
    7,
    "not a valid month",
    ErrorType.ERROR,
    1,
    true,
    "A date specified an invalid month. Valid months are: January-December, for format code MONTH, and Jan-Dec, for format code MON.",
    "Enter a valid month value in the correct format."
  ),
  INVALID_INTERVAL(
    8,
    "the interval is invalid",
    ErrorType.ERROR,
    1,
    true,
    "The character string you specified is not a valid interval.",
    "Please specify a valid interval."
  ),
  MISSING_OR_INVALID_DATETIME_FIELD(
    9,
    "missing or invalid datetime field",
    ErrorType.ERROR,
    1,
    true,
    "A datetime field(YEAR, MONTH, DAY, HOUR, MINUTE, SECOND) is expected but not found, or a datetime field specified the end field in an interval qualifier is more significant than its start field.",
    "Please specify a valid value"
  );

  private ErrorDescription ed;
  
  DataStructuresError(int num, String text, ErrorType type, int level,
                 boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.CqlEngine_Server_Common + num, text, type, level,
        isDocumented, cause, action, "DataStructuresError");
  
  }

  public ErrorDescription getErrorDescription() {
    return ed;
  }
  
}
