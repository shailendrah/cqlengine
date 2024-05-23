/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/InterfaceError.java /main/22 2010/05/19 07:12:23 sborah Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares InputError in package oracle.cep.exceptions.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sborah    05/19/10 - removing invalid_child which as no reference.
    hopark    05/22/09 - add Date format error
    hopark    05/20/09 - add stable table source
    hopark    02/17/09 - add invalid_boolean
    hopark    02/02/09 - add invalid object value
    hopark    02/02/09 - add Invalid query output dest
    alealves  01/04/09 - adding error USERFUNC_LOCATOR_NOT_SUPPORTED_IN_THIS_ENVIRONMENT
    sbishnoi  01/02/09 - adding error CONNECTION_NOT_CLOSED
    parujain  07/03/08 - datasource support
    udeshmuk  05/11/08 - update error msg for invalid number
    udeshmuk  03/13/08 - parameterize some errors
    sbishnoi  02/20/08 - adding class related errors(CLASS_NOT_*)
    sbishnoi  02/11/08 - modifying error message formats for few errors to
                         incorporate arguments
    udeshmuk  09/17/07 - Adding new error code for empty(null) schema line.
    mthatte   07/20/07 - add SCHEMA_MISMATCH & ATTR_LENGTH_EXCEEDED
    rkomurav  05/22/07 - add detail
    sbishnoi  05/08/07 - code cleanup
    rkomurav  03/27/07 - add invalid_epr_query
    skmishra  02/01/07 - add error descriptions
    anasrini  02/01/07 - Messages
    parujain  11/30/06 - File Exception
    anasrini  10/25/06 - add FABRIC_DESTINATION_ERROR
    anasrini  09/12/06 - add XML_FORMAT_ERROR
    najain    09/06/06 - add more errors
    anasrini  08/18/06 - JMS support
    parujain  08/03/06 - Timestamp datastructure
    skaluska  03/25/06 - implementation
    skaluska  03/24/06 - Creation
    skaluska  03/24/06 - Creation
    skaluska  03/23/06 - implementation
    skaluska  03/22/06 - implementation
    skaluska  03/22/06 - Creation
    skaluska  03/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/InterfaceError.java /main/22 2010/05/19 07:12:23 sborah Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;
/**
 * InterfaceError
 *
 * @author skaluska
 */
public enum InterfaceError implements ErrorCode
{

  FILE_OPERATION_FAILURE(
    1,
    "file operation failure: file {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Error while reading or writing tuple to file or trying to open or " +
    "close the file",
    "Verify the file path and provide proper file permissions"
  ),

  UNEXPECTED_EOF(
    2,
    "unexpected end-of-file: file {0} line {1}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Either no input is specified or the last tuple has incorrect number " +
    "of attributes",
    "Provide atleast one input and check if the last tuple has correct " +
    "number of attributes specified"
    
  ),

  UNEXPECTED_EOL(
    3,
    "unexpected end-of-line: file {0} line {1}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Input row does not contain required number of attributes",
    "Verify the input rows to specify correct number of attributes"
  ),

  INVALID_ATTR_TYPE(
    4,
    "invalid attribute type {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The attribute type {0} is not valid",
    "Specify a valid attribute type"
  ),

  INVALID_SOURCE(
    5,
    "invalid data source {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Either the data source {0} is not readable or contains syntax error",
    "Check syntax and location of data source"
  ),

  INVALID_CHARACTER(
    6,
    "invalid character {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Invalid character was specified in the input data {0}",
    "Check input schema, tuple kind and filed separator among attributes"
  ),

  INVALID_TIMESTAMP(
    7,
    "invalid time stamp value in file {0} for tuple ending at line {1}",
    ErrorType.INTERNAL_ERROR,
    1,
    false, 
    "One of the input attributes contain an invalid timestamp value",
    "Verify the input file for valid attributes of type timestamp"
  ),

  INVALID_NUMBER(
    8,
    "invalid number format {2} in file {0} for tuple ending at line {1}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "One of the input attributes contain an invalid number value",
    "Verify the input file for valid attributes of type number"
  ),

  NUMBER_NOT_FOUND(
    9,
    "invalid row found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "One of the rows in the input data is invalid while expecting an input " +
    "tuple or heart beat",
    "Check the rows to provide either an input tuple or a heart beat"
  ),

  EMPTY_FIELD(
    10,
    "empty field",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "One or more fields missing in the input tuple",
    "Verify if any fields are missing in any of the input tuples"
  ),

  COMPLEX_EVENT_NOT_FOUND(
    11,
    "complex event not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Complex event is not found in the input {0}",
    "Provide complex event in the input {0}"
  ),

  TIMESTAMP_NOT_FOUND(
    12,
    "timestamp not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "TimeStamp is not found for one of the input tuples in {0}",
    "Check if every input tuple is provided with a timestamp"
  ),

  VALUE_NOT_FOUND(
    13,
    "value not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Value not found in the input {0}",
    "Provide with a valid value"
  ),

  ELEMENT_KIND_NOT_FOUND(
    14,
    "element kind not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "One of the input rows is not provided with element kind",
    "Check if all the input rows have a valid element kind"
  ),

  ATTRIBUTE_LIST_NOT_FOUND(
    15,
    "attribute list not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Attribute list is not found in {0}",
    "Provide with a valid attribute list"
  ),

  INVALID_ELEMENT_KIND(
    16,
    "invalid element kind {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "One of the input rows has been specified with an invalid " +
    "element kind {0}",
    "Verify if the input rows have been specified with a valid element kind"
  ),

  POSITION_NOT_FOUND(
    17,
    "position not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Position is not found",
    "Provide with a position"
  ),

  NAME_NOT_FOUND(
    18,
    "scheduler name {0} not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Scheduler name provided is invalid",
    "Verify the configuration file for a valid scheduler name"
  ),

  INVALID_NAME(
    19,
    "invalid name {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The name {0} is invalid",
    "Verify the given name. It should be either an attribute, element kind, " +
    "timestamp or element node"
  ),

  DUPLICATE_TIMESTAMP(
    21,
    "duplicate timestamp",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Time stamp is specified more than once for a particular input row in the input",
    "Verify the input and remove duplicate timestamp"
  ),

  DUPLICATE_ELEMENT(
    22,
    "duplicate element kind",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Element kind is specified more than once for a particular input " +
    "row in the input",
    "Verify the input and remove duplicate element kind"
  ),

  XML_FORMAT_ERROR(
    23,
    "xml format error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Input does not contain valid syntax",
    "Verify the input for correct syntax"
  ),

  JMS_OPERATION_FAILURE(
    24,
    "jms operation failure",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Error while creating session, creating producer, starting or " +
    "closing connection, trying to get next tuple",
    "Verify JMS connection and check for valid input to JMS queue"
  ),

  FABRIC_DESTINATION_ERROR(
    25,
    "fabric destination error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Unable to insert next element into the destination",
    "Verify the destination provided"
  ),

  INSUFFICIENT_INPUT_FILES(
    26,
    "insufficient input files",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Insufficient number of input files are provided",
    "Provide with a sufficient number of input files"
  ),

  INVALID_SCHEDULER_TYPE_MODIFICATION(
    27,
    "invalid scheduler type modification",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Scheduler type modification is invalid",
    "Provide with a valid scheduler type modification"
  ),
  
  INVALID_EPR_QUERY(
    28,
    "invalid query parameter {0} in EPR definition",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "EPR definition has an invalid paramater {0}",
    "Check for valid parameters in EPR provided in delay scale,etc"
  ),
  SCHEMA_MISMATCH(
    29,
    "Input schema for {0} source does not match expected input schema",
    ErrorType.ERROR,
    1,
    false,
    "Input source has a different schema than the registered schema",
    "Check DDL or check input source for schema "
  ),
  SCHEMA_NOT_FOUND( 
    30,
    "Schema not found in the input source file",
    ErrorType.ERROR,
    1,
    false,
    "Input source file either does not have a schema or it is not provided in the first line of the source file",
    "Ensure that first line of input source file is the schema for the relation/stream"
  ),
  
  CLASS_NOT_FOUND(
    31,
    "class {0} not found",
    ErrorType.ERROR,
    1,
    false,
    "class {0} may not be in classpath or package specification may be incorrect",
    "check classpath configuration, package specification and other security settings"
  ),
  CLASS_NOT_ACCESSIBLE(
    32,
    "class {0} is not accessible",
    ErrorType.ERROR,
    1,
    false,
    "class {0} or required constructor is not accessible",
    "modify access specifiers for class {0} or required constructor to be accessible"
  ),
  CLASS_NOT_INSTANTIATED(
    33,
    "class {0} cannot be instantiated",
    ErrorType.ERROR,
    1,
    false,
    "class {0} may represents an abstract class , an interface, a primitive type, or void; or class {0} has no" +
    " nullary constructor or appropriate constructor; or other unknown instantiation failure ",
    "do not make class {0} an abstract class, an interface, a primitive type, or void; or add a nullary" +
    "constructor or appropriate constructor to class"
  ),
  OUTPUT_CLASS_NOT_VALID(
    34,
    "destination epr contains invalid output class {0}",
    ErrorType.ERROR,
    1,
    false,
    "class {0} may not extending required class oracle.cep.interface.output.QueryOutputBase",
    "extend oracle.cep.interface.output.QueryOutputBase to make desired output class"
  ),
  CALLOUT_CLASS_NOT_VALID(
    35,
    "callout epr contains invalid output class {0}",
    ErrorType.ERROR,
    1,
    false,
    "class {0} may not implementing required interface java.lang.Runnable",
    "implement java.lang.Runnable to make desired callout class"
  ),
  INVALID_DESTINATION(
    36,
    "invalid output destination {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "either the output destination {0} is invalid or database access failure",
    "verify output destination syntax and check database connection"
  ),
  DB_ACCESS_FAILURE(
    37,
    "database access failure",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "database access failure",
    "verify database connection"
  ),
  FEATURE_NOT_SUPPORTED_IN_THIS_ENVIRONMENT(
    38,
    "datasource finder not supported in this environment",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "feature not supported in this environment",
    "datasource finder needs to be provided"
  ),
  EXT_CONNECTION_NOT_CLOSED(
    39,
    "failed to close connection to external datasource {0}",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "external datasource connection close was failed",
    "verify external datasource {0} connection"
  ),
  USERFUNC_LOCATOR_NOT_SUPPORTED_IN_THIS_ENVIRONMENT(
      40,
      "user function locator not supported in this environment",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "feature not supported in this environment",
      "user function locator needs to be provided"
  ),
  OUTPUT_DEST_NOT_VALID(
     41,
      "destination epr contains invalid output destination {0}",
      ErrorType.ERROR,
      1,
      false,
      "no instance of oracle.cep.interface.output.QueryOutput not found with {0}",
      "verify output destination {0} is added to the query destination locator"
  ),
  OUTPUT_DEST_LOCATOR_NOT_FOUND(
      42,
      "Query Destination Locator is not provided by the environment",
      ErrorType.ERROR,
      1,
      false,
      "no instance of oracle.cep.service.IQueryDestLocator found from the environment",
      "implement oracle.cep.service.IEnvConfig properly and provide IQueryDestLocator"
  ),
  INVALID_JAVADESTINATION_TYPE(
      43,
      "invalid java destination type {0} is used",
      ErrorType.ERROR,
      1,
      false,
      "only Type or Id is allowed for the java destination type",
      "use proper java destination type either Type or Id"
  ),
  INVALID_OBJ_ATTRIBUTE_VALUE(
    44,
    "invalid object {0} is used for object attribute value",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "object {0} may not implementing required interface java.io.Serializable",
    "implement java.io.Serializable to use for object attribute"
  ),
  INVALID_BOOLEAN(
      45,
      "invalid boolean format {2} in file {0} for tuple ending at line {1}",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "invalid boolean format is used : {0}",
      "verify the boolean format"
    ),
  INVALID_BOOLEAN_FORMAT(
      46,
      "invalid boolean format {0}",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "invalid boolean format is used : {0}",
      "verify the boolean format"
    ),
    STALE_TABLE_SOURCE(
        47,
        "stale table source is used for {0}",
        ErrorType.INTERNAL_ERROR,
        1,
        false,
        "stale table source is used for {0}",
        "close the prepared statement and reopen it"
    ),
    INVALID_TIMESTAMP_FORMAT(
        48,
        "invalid timestamp format {0}",
        ErrorType.INTERNAL_ERROR,
        1,
        false,
        "invalid timestamp format is used : {0}",
        "verify the timestamp format"
      ),
    INVALID_DURATION_FORMAT(
          49,
          "invalid duration format {0}",
          ErrorType.INTERNAL_ERROR,
          1,
          false,
          "invalid duration format is used : {0}",
          "verify the duration format"
      ),
    STALE_QUEUE_SOURCE(
        50,
        "stale queue source is used for {0}",
        ErrorType.INTERNAL_ERROR,
        1,
        false,
        "stale queue source is used for {0}",
        "reinstantiate the queue source for stream {0}"
    );

    
 
  private ErrorDescription ed;

  InterfaceError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.CqlEngine_Interfaces + num, text, type, level,
        isDocumented, cause, action, "InterfaceError");
  }

  public ErrorDescription getErrorDescription(){
    return ed;
  }
  
}
