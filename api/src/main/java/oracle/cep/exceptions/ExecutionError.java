/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/ExecutionError.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares ExecutionError in package oracle.cep.exceptions.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  12/21/15 - adding support for ha snapshot generation
 udeshmuk  09/17/13 - add null to invalid value for duration expression
 sbishnoi  11/29/12 - adding exception for total ordering guarantee
 sbishnoi  10/09/12 - XbranchMerge
                      sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0 from
                      st_pcbpel_11.1.1.4.0
 vikshukl  08/08/12 - throw an exception when a change is found to a slow
                      changing archived dimension
 udeshmuk  02/02/12 - add error for durationexpr <=0
 udeshmuk  10/10/11 - XbranchMerge udeshmuk_bug-11933156_ps5 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0 from
                      st_pcbpel_pt-11.1.1.7.0
 sbishnoi  10/01/12 - adding new error for external relationss
 udeshmuk  09/29/11 - add object clone error
 sbishnoi  08/25/11 - XbranchMerge sbishnoi_bug-11675469_ps5 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  08/16/11 - adding new error code while fixing bug 11675469
 sbishnoi  03/02/11 - adding error for value window operator over relation
 sborah    01/12/11 - propagate error message
 sborah    05/19/10 - Typo
 sborah    04/08/10 - char to number functions
 sbishnoi  02/02/10 - fix NPE bug 9273983
 sbishnoi  01/29/10 - adding new execution error to distinguish between various
                      downstream exceptions
 sbishnoi  01/21/10 - new error message for downstream component failure
                      message
 sborah    10/28/09 - support for bigdecimal
 anasrini  01/17/09 - add GENERIC_ERROR
 udeshmuk  01/13/09 - add error for invalid value in to_timestamp
 hopark    12/04/08 - add populate err
 sborah    11/13/08 - correcting spelling mistake
 parujain  07/08/08 - 
 skmishra  06/26/08 - adding xmlagg_runtime_error
 skmishra  06/05/08 - adding param to invalid-xml-pub
 skmishra  05/21/08 - adding invalid_xml_pub_arg_error
 parujain  06/06/08 - softexception
 sbishnoi  03/17/08 - adding DIVIDE_BY_ZERO
 hopark    02/05/08 - parameterized error
 najain    11/02/07 - add XQRY_FUNC_ERROR
 parujain  11/19/07 - OPEN CONNECTION FAILURE
 sbishnoi  11/07/07 - add INVALID_DUPLICATE_TUPLE and
                      INVALID_UPDATE_RELATION_TUPLE
 sbishnoi  07/23/07 - modified action message in PUSH_SRC_NOT_INITIALIZED
 parujain  05/24/07 - Tuple input error
 najain    05/22/07 - add cause/action
 hopark    05/16/07 - add arguments for out of order exception
 hopark    05/16/07 - add arguments for out of order exception
 sbishnoi  05/08/07 - code cleanup
 parujain  04/23/07 - operator not valid error
 parujain  04/16/07 - 
 anasrini  02/01/07 - Error Messages
 najain    09/07/06 - add more errors
 najain    08/29/06 - add more errors
 parujain  08/04/06 - Timestamp datastructure
 anasrini  07/17/06 - errors related to user defined aggregation (UDA)
 najain    06/14/06 - add more errors 
 najain    06/07/06 - add INVALID_POSITION 
 najain    05/08/06 - add more errors 
 skaluska  02/18/06 - more errors 
 skaluska  02/13/06 - Creation
 skaluska  02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/ExecutionError.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * @author skaluska
 */
public enum ExecutionError implements ErrorCode
{
  AEVAL_OVERFLOW(
    1,
    "arithmetic evaluator overflow.\n{0}\nThe maximum number is {1}.",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The maximum number of instructions({1}) for a arithmetic expression was exceeded",
    "internal error: contact Oracle to support more complex instructions"
  ),

  BEVAL_OVERFLOW(
    2,
    "boolean evaluator overflow.\n{0}\nThe maximum number is {1}.",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The maximum number of instructions({1}) for a boolean expression was exceeded",
    "internal error: contact Oracle to support more complex instructions"
  ),

  HEVAL_OVERFLOW(
    3,
    "hash evaluator overflow.\n{0}\nThe maximum number is {1}.",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The maximum number of instructions({1})  for a hash expression was exceeded",
    "internal error: contact Oracle to support more complex instructions"
  ),

  INVALID_ROLE(
    4,
    "invalid evaluation context role({0}). The number of roles is {1}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  INVALID_ATTR(
    5,
    "invalid attribute({0}). The possible maximum value is {1}.",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  INVALID_TUPLE(
    6,
    "invalid tuple({0}) used for Index",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  INVALID_NEGATIVE_RELATION_TUPLE(
    7,
    "invalid negative tuple({0})",
    ErrorType.ERROR,
    1,
    true,
    "this negative tuple({0}) does not have a corresponding positive tuple",
    "fix the input"
  ),

  TUPLESPEC_OVERFLOW(
    10,
    "tuple specification overflow. The maximum number of attributes is {0}.",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The number of attributes exceeded the allowed limit({0})",
    "internal error: contact oracle support to allow more attributes"
  ),

  TYPE_MISMATCH(
    11,
    "type mismatch: type({0}), expected({1})",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),


  OUT_OF_ORDER(
    12,
    "event timestamp is out of order timestamp0={0} timestamp1={1} event={2} in {3}",
    ErrorType.ERROR,
    1,
    true,
    "timestamp of event is smaller than timestamp of a previously received event",
    "fix the input"
  ),

  EMPTY_STORE(
    13,
    "store is empty",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  INSERT_BITVECTOR_ABSENT(
    15,
    "insert bit vector({0}) is absent",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  INVALID_STUBID(
    16,
    "invalid subscriber id({0})",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  INVALID_POSITION(
    17,
    "invalid position",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  MAX_STUBS_EXCEEDED(
    18,
    "maximum number of subscribers exceeded the limit({0})",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The maximum number of supported stubs are exceeded the limit({0})",
    "internal error: contact oracle support to increase the maximum number of supported stubs"
  ),

  AGGR_FN_HANDLER_ALLOCATION_ERROR(
    19,
    "unable to allocate user defined aggregation handler",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  UDA_ERROR(
    20,
    "error while executing user defined aggregation",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  ITERATOR_UNINIT(
    21,
    "error while uninitializing the iterator",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  PUSH_SRC_NOT_INITIALIZED(
    22,
    "push source({0}) not initialized for {1}",
    ErrorType.ERROR,
    1,
    false,
    "push source({0}) for the stream has not been initialized for {1}",
    "initialize the push source({0}) first using ALTER STREAM STREAM_NAME ADD SOURCE PUSH"
  ),

  USERDEFINED_FUNCTION_RUNTIME_ERROR(
    23,
    "user defined function({0}) runtime error while execution",
    ErrorType.ERROR,
    1,
    false,
    "error thrown by a user defined function({0})",
    "fix the user defined function"
  ),
  
  OPERATOR_RUNTIME_ERROR(
    24,
    "runtime error occurred in execution operator({0})",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error used to disable queries in case of an error",
    "internal error: should never be thrown to the user"
  ),
    
  OPERATOR_NOT_VALID(
    25,
    "execution operator({0}) is no longer valid because of runtime error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error used to disable queries in case of an error",
    "internal error: should never be thrown to the user" 
  ),
     
  INCORRECT_INPUT_TIMESTAMP(
    26,
    "input tuple({0}) for operator({1}) contains unexpected timestamp({2})",
    ErrorType.ERROR,
    1,
    false,
    "input tuple has timestamp greater than system time",
    "input tuple should have correct timestamp"
  ),

  INVALID_TIMEFORMAT(
    27,
    "invalid time format string {0} provided",
    ErrorType.ERROR,
    1,
    false,
    "the format string provided is not valid",
    "pass valid string as format argument"
  ),
    
  USERDEFINED_AGGREGATION_FUNCTION_RUNTIME_ERROR(
    28,
    "user defined aggregation function({0}) runtime error while execution",
    ErrorType.ERROR,
    1,
    false,
    "error thrown by a user defined aggregation function({0})",
    "fix the user defined aggregation function"
  ),

  INCORRECT_INPUT_TUPLE(
    29,
    "input tuple passed by user({0}) found to be incorrect for operator({1})",
    ErrorType.ERROR,
    1,
    false,
    "Error found in format while processing input tuple ({0}) in operator ({1})",
    "Verify timestamp, attribute value datatypes, xml format and kind of input tuple."
  ),

  INVALID_UPDATE_RELATION_TUPLE(
    30,
    "invalid update or upsert tuple({0})",
    ErrorType.ERROR,
    1,
    false,
    "An attempt was made to update a relation for which primary key is not defined for tuple({0})",
    "Either define a primary key for this relation or do not update a relation "
  ),
      
  UNIQUE_CONSTRAINT_VIOLATION(
    31,
    "Unique Constraint {0} violated",
    ErrorType.ERROR,
    1,
    false,
    "An attempt was made to insert a duplicate key",
    "Either remove the unique restriction or do not insert the duplicate key"
  ),
  
  CANNOT_INSERT_NULL(
    32,
    "Cannot insert NULL into {0}",
    ErrorType.ERROR,
    1,
    false,
    "An attempt was made to insert NULL value for key",
    "Either remove the unique restriction or do not insert NULL value for key"
  ),

  OPEN_CONNECTION_FAILURE(
    33,
    "input url to external source invalid operator={0}, url={1}, sql={2}",
    ErrorType.ERROR,
    1,
    false,
    "Error in opening the connection to external source",
    "Verify the url provided"
  ),
    
  CLOSE_CONNECTION_FAILURE(
    34,
    "error while closing the connection to external source",
    ErrorType.ERROR,
    1,
    false,
    "Error while closing the connection to external source",
    "Verify whether connection was already opened or not"
  ),
  
  ERROR_RUNNING_EXTERNAL_QUERY(
    35,
    "Error running query {0} to external source {1}",
    ErrorType.ERROR,
    1,
    false,
    "Error running query {0} to external source {1}",
    "Verify either the external connection or the query involving external source {1}"
  ),

  XQRY_FUNC_ERROR(
    36,
    "Error in invocation of xquery function",
    ErrorType.ERROR,
    1,
    false,
    "Error in invocation of xquery function",
    "Verify xquery function, bindings and syntax."
  ),
  
  DIVIDE_BY_ZERO(
    37,
    "divisor is equal to zero",
    ErrorType.ERROR,
    1,
    false,
    "divide by zero situation encountered",
    "divisor should not be zero"
  ),
  
  INVALID_XML_PUB_ARG(
    38,
    "Invalid input {0} to xml publishing function {1}",
    ErrorType.ERROR,
    1,
    false,
    "Invalid input {0} to xml publishing function {1}",
    "Check documentation for input to xml publishing functions"),
  
  NAME_EXPR_CANNOT_BE_NULL_FOR_XML_PUB_FUNCS(
    39,
    "name expression for xml publishing function should not be null",
    ErrorType.ERROR,
    1,
    false,
    "name expression for xml publishing function evaluated to null",
    "ensure that the name expression should not evaluate to null"),
   
  XML_AGG_RUNTIME_ERROR(
    40,
    "Runtime exception from xmlagg",
    ErrorType.ERROR,
    1,
    false,
    "Cause unknown",
    "Check xmlagg expression, check input data."),
    
  VALUE_OUT_OF_ORDER(
    41,
    "event value is out of order value0={0} value1={1} event={2} in {3}",
    ErrorType.ERROR,
    1,
    true,
    "value of event is smaller than value of a previously received event",
    "fix the input"
  ),
  
  STREAM_SRC_NOT_INITIALIZED(
    42,
    "stream source({0}) not initialized for {1}",
    ErrorType.ERROR,
    1,
    false,
    "source({0}) for the stream has not been initialized for {1}",
    "initialize the source first using ALTER STREAM STREAM_NAME ADD SOURCE"
  ),

  NO_PLUS_TUPLE_IN_SYNOPSIS(
    43,
    "No plus tuple found in synopsis for given minus tuple({0}) in operator {1}.",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: No plus tuple found in synopsis for given minus tuple({0}) in operator {1}",
    "internal error: contact Oracle to get support"
  ),
  FAILED_TO_GET_TUPLE(
    44,
    "Failed to get tuple from the source {0} in operator {1} : {2}",
    ErrorType.ERROR,
    1,
    false,
    "The operator {1} has failed to get a tuple from the source {0}, cause: {2}",
    "Check the source configuration"
  ),
  ILLEGAL_ARGUMENT_PROVIDED(
    45,
    "Illegal argument value {0} provided for {1} function",
    ErrorType.ERROR,
    1,
    false,
    "The value provided as input argument for the function is illegal/invalid",
    "Provide correct argument value"
  ),
  GENERIC_ERROR(
    46,
    "Generic execution error",
    ErrorType.ERROR,
    1,
    false,
    "Generic execution error",
    "Generic execution error"
  ),
  OPERATOR_ERROR(
    47,
    "Operator({0}) execution error, {1}",
    ErrorType.ERROR,
    1,
    false,
    "Operator execution error",
    "Operator execution error"
  ),
  PRECISION_ERROR(
    48,
    "value ({0}) larger than specified precision ({1}) allowed for this column",
    ErrorType.ERROR,
    1,
    false,
    "When inserting or updating records, a numeric value was entered that exceeded the precision defined for the column.",
    "Enter a value that complies with the numeric column's precision, or use the MODIFY option with the ALTER TABLE command to expand the precision."
  ),
  DOWNSTREAM_CHANNEL_EXCEPTION(
    49,
    "downstream components reported failure: {0}",
    ErrorType.ERROR,
    1,
    false,
    "downstream component of processor failed to handle output event and throws: {0}",
    "resolve downstream component failure"
  ),
  TABLE_FUNCTION_OUTPUT_TYPE_MISMATCH(
    50,
    "table function outputs an attribute of invalid type",
    ErrorType.ERROR,
    1,
    false,
    "data type of output attribute value does not match with the specified type {0}",
    "table function should output attribute value of data type {0}"
  ),
  DOWNSTREAM_CHANNEL_SOFT_EXCEPTION(
    51,
    "downstream components reported exception: {0}",
    ErrorType.ERROR,
    1,
    false,
    "downstream component of processor failed to handle output event and throws: {0}",
    "resolve downstream component failure"
  ),
  INVALID_NUMBER(
    52,
    "The attempted conversion of a character string {0} to {1} failed " +
    "because the character string was not a valid literal.",
    ErrorType.ERROR,
    1,
    false,
    "The attempted conversion of a character string {0} to {1} failed " +
    "because the character string was not a valid literal.",
    
    "Check the character strings in the function or expression. " +
    "Check that they contain only numbers, a sign, a decimal point, and the " +
    "character \"E\" or \"e\" and retry the operation."
  ),
  PROPAGATE_ERROR
  (
    53,
    "{0}",
    ErrorType.ERROR,
    1,
    false,
    "{1}",
    "{2}"
  ),
  INVALID_MINUS_TUPLE
  (
    54,
    "Invalid minus tuple : {0} encountered in stream operator {1}",
    ErrorType.ERROR,
    1,
    false,
    "Minus tuples are not allowed in streams",
    "Ensure that there are no minus tuples as input to the stream."
  ),
  INVALID_UPDATE_TUPLE
  (
    55,
    "Invalid update tuple : {0} encountered in stream operator {1}",
    ErrorType.ERROR,
    1,
    false,
    "Update tuples are not allowed in streams",
    "Ensure that there are no update tuples as input to the stream."
  ),
  INVALID_TIMESTAMP_COLUMN_VALUE
  (
    56,
    "invalid value specified for a timestamp column",
    ErrorType.ERROR,
    1,
    false,
    "either null or invalid value used for a timestamp column",
    "specify valid timestamp values"
  ),
  NEGATIVE_RANGE_VALUE
  (
    57,
    "range is a negative value {0}",
    ErrorType.ERROR,
    1,
    false,
    "range value should be greater than 0",
    "use valid input and range expression"
  ),
  SLIDE_GREATER_THAN_RANGE
  (
    58,
    "slide value {0} is greater than range value {1}",
    ErrorType.ERROR,
    1,
    false,
    "slide value was greater than range value",
    "slide should be less than range value"
  ),
  NON_DETERMINISTIC_FUNCTION_NOT_ALLOWED_IN_PREDICATE
  (
    59,
    "Encountered a non deterministic function in a predicate",
    ErrorType.ERROR,
    1,
    false,
    "A non deterministic function is not allowed in a predicate that forms the WHERE clause or HAVING clause",
    "Replace the non-deterministic function with a deterministic function"
  ),
  OBJECT_CLONE_ERROR
  (
    60,
    "error while cloning an object field",
    ErrorType.ERROR,
    1,
    false,
    "An error occurred while invoking or executing clone on object field",
    "Recheck the implementation of clone method"
  ),
  ARCHIVER_QUERY_RESULTSET_ACCESS_ERROR
  (
    61,
    "SQLException \"{0} \" occurred while accessing the result set returned by archiver query",
    ErrorType.ERROR,
    1,
    false,
    "Internal Error",
    "Please contact customer support"
  ),
  INVALID_DURATION_VALUE
  (
    62,
    "The specified duration clause expression evaluates to invalid value {0}",
    ErrorType.ERROR,
    1,
    false,
    "Duration expression evaluated to invalid value e.g. zero or negative or null value",
    "Ensure that duration expression should not evaluate to zero, negative, null or such invalid value"
  ),
  TYPE_NOT_SUPPORTED_IN_EXTERNAL_RELATION
  (
    63,
    "type {0} is not supported by external relation",
    ErrorType.ERROR,
    1,
    false,
    "An error occurred while setting value of type {0}.",
    "check the supported types of external relation."
  ),
  ARCHIVED_DIMENSION_CHANGE_DETECTED
  (
    64,
    "Change to archived dimension detected",
    ErrorType.ERROR,
    1,
    false,
    "Internal error only",
    "This exception should never be seen by customers. Handle it in CQS and restart the query"    
  ),
  TOTAL_ORDER_NOT_OBSERVED_FOR_INPUT_TUPLE
  (
    65,
    "an event from total ordered application timestamped input source has timestamp" +
    " {0} which is equal to or less than previous input event timestamp {1}",
    ErrorType.ERROR,
    1,
    true,
    "current input event timestamp should be higher than previous input " +
    "timestamp {1} in case of total ordered application timestamp input source.",
    "provide the strictly increasing timestamp values to input events from " +
    "total ordered application timestamped input source."
  ),
  SNAPSHOT_CREATION_ERROR
  (
    66,
    "Unable to create snapshot for opreator {1}",
    ErrorType.ERROR,
    1,
    false,
    "Failed to write snapshot for operator {1} due to {0}",
    "Please ensure that java process should be able to write to given output stream"    
  ),
  SNAPSHOT_LOAD_ERROR_CNF
  (
    67,
    "Unable to load snapshot for operator {0}",
    ErrorType.ERROR,
    1,
    false,
    "Failed to load snapshot for operator {0} due to java.lang.ClassNotFoundException for class {1}",
    "Please check the server classpath"    
  ),  
  SNAPSHOT_LOAD_ERROR
  (
    68,
    "Unable to load snapshot for operator {1}",
    ErrorType.ERROR,
    1,
    false,
    "Failed to load snapshot for operator {1} due to {0}",
    "Please ensure that java process should be able to read from a java io stream"    
  ),
  SNAPSHOT_PROCESSING_ERROR
  (
    69,
    "Unable to process snapshot for query {0}",
    ErrorType.ERROR,
    1,
    false,
    "Failed to do snapshot processing for query {0} due to {1}",
    "Ensure that all operators finish snapshot processing successfully"    
  ),
  INVALID_TIME_ZONE_ID
  (
    70,
    "timezone id {0} is invalid or not-supported",
    ErrorType.ERROR,
    1,
    false,
    "Failed to parse timezone string",
    "Please provide a valid timezone id"    
  ),
  INVALID_UPSERT_TUPLE
  (
    71,
    "Invalid upsert tuple : {0} encountered in stream operator {1}",
    ErrorType.ERROR,
    1,
    false,
    "Upsert tuples are not allowed in streams",
    "Ensure that there are no upsert tuples as input to the stream."
  ),
  INVALID_PATTERN_SYNTAX
  (
    72,
    "pattern expression {0} is invalid or not-supported",
    ErrorType.ERROR,
    1,
    false,
    "Failed to compile pattern expression {0}",
    "Please provide a valid pattern expression"    
  )
  ;
  
  private ErrorDescription ed;

  ExecutionError(int num, String text, ErrorType type, int level, 
                 boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Common + num, text, type, level,
        isDocumented, cause, action, "ExecutionError");
  }
  
  public ErrorDescription getErrorDescription() {
    return ed;
  }

}
