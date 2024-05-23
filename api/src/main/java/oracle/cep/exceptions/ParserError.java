/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/ParserError.java /main/11 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of the error codes for the Parser module

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/15/11 - adding new error INVALID_TIME_SPEC_SPECIFICATION
    sborah      06/10/10 - Typo
    hopark      02/04/09 - add invalid interval
    parujain    08/21/08 - use semantic error
    parujain    08/20/08 - support supplementalmsg
    udeshmuk    02/04/08 - parameterize errors wherever needed.
    sbishnoi    06/29/07 - add TOO_FEW_ARG_DECODE
    sbishnoi    05/29/07 - add error INVALID_RELATIONAL_OPERATOR
    sbishnoi    05/21/07 - add cause,action
    sbishnoi    05/08/07 - code cleanup
    rkomurav    02/01/07 - add methods
    rkomurav    07/23/06 - bug-5396879
    anasrini    02/09/06 - Creation
    anasrini    02/09/06 - Creation
    anasrini    02/09/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/ParserError.java /main/10 2010/06/11 00:21:16 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Parser module
 *
 * @since 1.0
 */


public enum ParserError implements ErrorCode {

  PARSER_ERROR(
    1,
    "generic parser error",
    ErrorType.ERROR,
    1,
    true,
    "This is a generic parser error",
    "This is a generic parser error"
  ),
  INVALID_AGGR_FUN_INPUT_TYPE(
    2,
    "invalid input type {0} for aggregate function {1}",
    ErrorType.ERROR,
    1,
    true,
    "An input type {0} other than the data type(s) " +
    "allowed for the aggregate function {1} has been specified",
    "Use only the allowed input type(s) for the aggregate function {1}"+
    "Allowed input type(s) for this functions are: {2}"
  ),
  INVALID_AGGR_FUN_RETURN_TYPE(
    3,
    "invalid return type {0} for aggregate function {1}", 
    ErrorType.ERROR,
    1,
    true,
    "A return type {0} other than the allowed return type(s) for the" +
    " aggregate function {1} has been specified",
    "Use only the allowed return type(s) for the aggregate function {1}"+
    "Allowed return type(s) for this function are: {2}"
  ),
  INVALID_RELATIONAL_OPERATOR(
    4,
    "Mismatch in the number of expressions in the two ExprLists",
    ErrorType.ERROR,
    1,
    true, 
    "Number of expressions({0}) in the ExprList to be matched "+
    "differs from the number of expressions({1}) for a member of ExprList set in which " +
    "membership is being checked",
    "Ensure that for every member of ExprList set, the number of expressions in it is " +
    "equal to the number of expressions in the ExprList to be matched"
  ),
  NOT_ENOUGH_ARG_IN_DECODE(
    5,
    "not enough arguments for DECODE function",
    ErrorType.ERROR,
    1,
    true,
    "Number of arguments are less than three",
    "Specify atleast three arguments"
  ),
  INVALID_INTERVAL_FORMAT(
    6,
    "invalid interval format is used for {0}",
    ErrorType.ERROR,
    1,
    true,
    "Interval {0} is not written in the correct format",
    "Use proper interval format"
  ),
  INVALID_TIME_SPEC(
    7,
    "invalid time duration. time should be specified as a constant value",
    ErrorType.ERROR,
    1,
    true,
    "time duration is specified as a constant value",
    "specify time as a constant value"
  ),
  NOT_ENOUGH_ARG_FOR_FUNCTION(
    8,
    "not enough arguments for the {0} function",
    ErrorType.ERROR,
    1,
    true,
    "Number of arguments are less than exepected",
    "Specify atleast {1} arguments"
  );
  
  private ErrorDescription ed;
  
  ParserError(int num, String text, ErrorType type, int level, boolean isDocumented,
              String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Core_Parser + num, text, type, level,
        isDocumented, cause, action, "ParserError");
  }

  public ErrorDescription getErrorDescription()
  {
    return ed;
  }
    
}
