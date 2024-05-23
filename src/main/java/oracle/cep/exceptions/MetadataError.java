/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/MetadataError.java /main/43 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Enumeration of the error codes for the Metadata module

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    08/31/12 - add archived view related errors
 udeshmuk    04/03/12 - make the error message common for event/txn/worker
                        identifier
 udeshmuk    01/08/12 - error for incorrect type of column desginated as
                        timestamp column
 udeshmuk    08/24/11 - invalid type for event identifier column
 sborah      05/19/10 - Typo
 parujain    12/07/09 - synonym
 alealves    11/27/09 - Data cartridge context, default package support
 parujain    09/22/09 - dependency support
 alealves    01/30/09 - support for user function instances
 sborah      12/23/08 - adding ALIAS_NAME_REQUIRED
 parujain    12/15/08 - stop view
 sbishnoi    08/18/08 - 
 sborah      08/11/08 - modifying error string
 sborah      08/07/08 - modifying VIEW_ATTRIBUTE_NUMBER_MISMATCH error message
 mthatte     04/09/08 - adding cannot-enable-statats-for-derived-ts
 parujain    03/10/08 - derived ts
 mthatte     02/26/08 - parametrizing errors
 parujain    02/07/08 - parameterizing error
 udeshmuk    12/19/07 - add new error code.
 sbishnoi    11/23/07 - adding PRIMARY_KEY_NOT_DEFINED
 sbishnoi    10/28/07 - Add primary key related error
 skmishra    07/12/07 - Add new exception (bug 6206009)
 anasrini    05/29/07 - 
 parujain    05/21/07 - add cause and action
 anasrini    05/04/07 - fix schema violation issues
 parujain    03/19/07 - drop window exception
 parujain    03/05/07 - Window related errors
 parujain    02/28/07 - Stop Query Error
 parujain    02/14/07 - System startup Error
 parujain    01/31/07 - drop function
 parujain    02/05/07 - backend store exception
 parujain    02/01/07 - format error messages
 anasrini    02/01/07 - Messages
 dlenkov     09/07/06 - added MAXIMUM_QUERIES_EXCEEDED
 najain      08/21/06 - add PULL_SRC_EXISTS
 najain      06/20/06 - more errors
 anasrini    06/20/06 - support for functions 
 anasrini    06/12/06 - support for user defined functions 
 najain      05/09/06 - add more errors 
 anasrini    02/21/06 - add TABLE_NOT_FOUND error 
 najain      02/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/MetadataError.java /main/43 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Metadata module
 * 
 * @since 1.0
 */
public enum MetadataError implements ErrorCode
{
  QUERY_NOT_FOUND(1, "query with name {0} not found", ErrorType.ERROR, 1,
      false, "Query {0} not found", "Provide the correct query"),

  TABLE_ALREADY_EXISTS(2, "duplicate STREAM or RELATION creation", ErrorType.ERROR, 1,
      false, "Stream or Relation with name {0} already exists",
      "Create stream or relation with different name"),

  VIEW_ALREADY_EXISTS(3, "duplicate view creation", ErrorType.ERROR, 1, false,
      "View name {0} is already used by an existing VIEW or STREAM or RELATION",
      "Create view with different name"),

  FUNCTION_ALREADY_EXISTS(4, "duplicate function creation", ErrorType.ERROR, 1,
      false, "Function with name {0} already exists",
      "Create function with different name"),

  ATTRIBUTE_NOT_FOUND_AT_DEF_POS(5,
      "no attribute with given position {0} in metadata", ErrorType.ERROR, 1,
      false,
      "Attribute at position {0} is invalid for STREAM or RELATION or VIEW.",
      "Provide a valid attribute position."),

  PARAMETER_NOT_FOUND_AT_DEF_POS(6,
      "no parameter with given position {0} in metadata", ErrorType.ERROR, 1,
      false,
      "Parameter at position {0} is invalid for STREAM or RELATION or VIEW.",
      "Provide a valid parameter position"),

  INVALID_TABLE_IDENTIFIER(7, "stream/relation identifier {0} not valid",
      ErrorType.INTERNAL_ERROR, 1, false, "stream/relation identifier {0} is not valid.",
      "internal error"),

  INVALID_VIEW_IDENTIFIER(8, "view identifier {0} not valid",
      ErrorType.INTERNAL_ERROR, 1, false,
      "Attempting to access an invalid view identifier {0}.", "internal error"),

  INVALID_FUNCTION_IDENTIFIER(9, "function identifier {0} not valid",
      ErrorType.INTERNAL_ERROR, 1, false,
      "Attempting to access an invalid function identifier {0}.",
      "internal error"),

  ATTRIBUTE_ALREADY_EXISTS(11, "duplicate attribute name {0}", ErrorType.ERROR,
      1, false,
      "Attribute name {0} already used in this STREAM or RELATION or VIEW definition.",
      "Provide a different attribute name for the attribute."),

  PARAMETER_ALREADY_EXISTS(12,
      "duplicate parameter name {0} for a user function", ErrorType.ERROR, 1,
      false,
      "Parameter name {0} already used in this FUNCTION or WINDOW definition.",
      "Provide unique names for all the parameters."),

  STREAM_NOT_FOUND(13, "no stream with the given name or id {0}",
      ErrorType.ERROR, 1, false, "Stream name or id {0} provided is invalid.",
      "Provide a valid stream name."),

  RELATION_NOT_FOUND(14, "no relation with the given name or id {0}",
      ErrorType.ERROR, 1, false,
      "Relation name or id {0} provided is invalid.",
      "Provide a valid relation name."),

  VIEW_NOT_FOUND(16, "no view with given name {0}", ErrorType.ERROR, 1, false,
      "View name {0} provided is invalid.", "Provide a valid view name."),

  FUNCTION_NOT_FOUND(17, "no function with given name {0}", ErrorType.ERROR, 1,
      false, "Function name {0} provided is invalid, or arguments are of the wrong type.",
      "Provide a valid function name, or correct the type of the arguments."),

  ATTRIBUTE_NOT_FOUND(18, "no attribute with given name {0} in metadata",
      ErrorType.ERROR, 1, false,
      "Attribute name {0} is invalid for STREAM, RELATION, VIEW or SUBQUERY.",
      "Provide a valid attribute name."),

  PARAMETER_NOT_FOUND(19, "no parameter with given name {0} in user function",
      ErrorType.ERROR, 1, false,
      "Parameter name {0} is invalid for WINDOW or FUNCTION",
      "Provide a valid parameter name."),

  QUERY_ALREADY_EXISTS(20, "duplicate query creation with name {0}",
      ErrorType.ERROR, 1, false,
      "Query name {0} is already used by an existing query",
      "Create query with different name."),

  INVALID_QUERY_IDENTIFIER(
      21,
      "wrong query identifier {0}",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "An attempt to access an invalid query identifier {0} has been encountered.",
      "internal error"),

  INVALID_QUERY_OPERATION(22, "operation {0} cannot be performed on the query",
      ErrorType.ERROR, 1, false, "Error while operation {0} on query",
      "internal error"),

  INVALID_ENDPOINTREF(23, "wrong endpointref in source or destination",
      ErrorType.ERROR, 1, false,
      "EPR provided in source or destination is invalid.",
      "Verify the validity of the EPR provided."),

  INVALID_XML_ERROR(24,
      "wrong xml format in source or destination {0} for stream/relation {1}",
      ErrorType.ERROR, 1, false,
      "Error in XML format of source or destination {0} for STREAM/RELATION {1}.",
      "Check for the XML format of the source or destination."),

  NO_QUERY_DESTINATION(
      25,
      "query result destination does not exist for query {0}",
      ErrorType.ERROR,
      1,
      false,
      "Error while starting the query {0}. Query should have a valid destination for the results.",
      "Provide a valid query destination. Then retry to start the query."),

  VIEW_DESTINATION_EXISTS(26, "cannot delete query {0} as view exits",
      ErrorType.ERROR, 1, false,
      "Error while dropping the query {0}. Query has a destination view.",
      "Drop the destination view. Then retry to drop the query."),

  VIEW_ATTRIBUTE_NUMBER_MISMATCH(
      27,
      "mismatch between number of select list expressions ({0})  in the view definition query and the number of attributes ({1}) in the view schema for view {2}",
      ErrorType.ERROR,
      1,
      false,
      "The number of select list expressions in the view definition query is not equal to the number of attributes in the view schema declaration",
      "Ensure that the number of select list expressions in the view definition query is equal to the number of attributes in the view schema declaration"
      ),

  VIEW_ATTRIBUTE_DATATYPE_MISMATCH(
      28,
      "attribute datatype mismatch between query {0} and view {1}",
      ErrorType.ERROR,
      1,
      false,
      "Error while creation of view. Attribute datatype {1} should be same as referenced query output datatype {0}.",
      "Check for the view attribute datatype and referenced query outputs datatypes."),

  FUNCTION_IMPL_CLASS_NOT_FOUND(
      29,
      "implementation class {0} for function not found",
      ErrorType.ERROR,
      1,
      false,
      "Error while registering the function. Implementation class {0} not found.",
      "Provide an implementation class in function definition."),

  INVALID_IMPL_CLASS_FOR_FUNCTION(30,
      "invalid implementation class for function {0}", ErrorType.ERROR, 1,
      false,
      "Error while instantiating the {0} function implementation class.",
      "Provide a valid implementation class in function definition."),

  OBJECT_NOT_FOUND(31, "object metadata cannot be found for object {0}",
      ErrorType.INTERNAL_ERROR, 1, false,
      "Error while retrieving the object metadata for object {0}.",
      "Check for the object name provided."),

  MAXIMUM_QUERIES_EXCEEDED(
      33,
      "count limit exceeded for reference queries",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "The number of queries have exceeded the maximum number of queries supported",
      "Increase the limit of reference queries"),

  CANNOT_DROP_TABLE(35, "Stream/relation {0} cannot be dropped", ErrorType.ERROR, 1,
      false,
      "Attempt was made to drop stream/relation {0} currently referenced by some query.",
      "Make sure no query references the stream or relation, then repeat the command."),

  PULL_SRC_EXISTS(
      36,
      "Pull source {0} already exists",
      ErrorType.ERROR,
      1,
      false,
      "Error while setting the stream or relation source. Found an already existing pull source {0}",
      "Stream or relation cannot have multiple sources."),

  PUSH_SRC_EXISTS(
      37,
      "Push source {0} already exists",
      ErrorType.ERROR,
      1,
      false,
      "Error while setting the stream or relation source. Found an already existing push source {0}",
      "Stream or relation cannot have multiple sources."),

  CANNOT_DROP_VIEW(38, "View {0} cannot be dropped", ErrorType.ERROR, 1, false,
      "Attempt was made to drop view {0} currently referenced by some query.",
      "Make sure no query references the view, then repeat the command."),

  QUERY_FETCH_ITERATOR_NOT_INITIALIZED(40,
      "Backend store iterator initialization not done",
      ErrorType.INTERNAL_ERROR, 1, false, "Error in system startup.",
      "This is an internal error."),

  CANNOT_DROP_BUILTIN_FUNCTION(41, "Cannot drop the built-in function {0}",
      ErrorType.ERROR, 1, false, "Attempted to drop a built-in function {0}",
      "Built-in function can never be dropped."),

  CANNOT_DROP_FUNCTION_QUERY_EXISTS(42,
      "Cannot drop function {0} since query exists", ErrorType.ERROR, 1, false,
      "Attempted to drop function {0} currently referenced by a query.",
      "Drop all the queries currently referencing the function. Retry the command."),

  SYSTEM_STATE_ALREADY_EXISTS(43, "System state object already exists",
      ErrorType.INTERNAL_ERROR, 1, false,
      "Error while initializing the system.", "This is an internal error."),

  SYSTEM_STATE_NOT_FOUND(44, "system state object does not exist",
      ErrorType.INTERNAL_ERROR, 1, false,
      "An error occurred while initializing and updating the system.",
      "This is an internal error."),

  CANNOT_STOP_QUERY_NOT_RUNNING(45, "Query {0} cannot be stopped ",
      ErrorType.ERROR, 1, false,
      "Attempted to stop a query {0} that was not running.",
      "Query is not running."),

  WINDOW_ALREADY_EXISTS(46,
      "Duplicate window creation: window {0} already exists.", ErrorType.ERROR,
      1, false, "Window name {0} already used by an existing window.",
      "Create window with a different name"),

  WINDOW_IMPL_CLASS_NOT_FOUND(
      47,
      "Implementation class {0} for window not found",
      ErrorType.ERROR,
      1,
      false,
      "Error while registering the user defined window. Implementation class {0} not found.",
      "Provide an implementation class in window definition."),

  INVALID_IMPL_CLASS_FOR_WINDOWS(48,
      "Invalid implementation class {0} for windows", ErrorType.ERROR, 1,
      false, "Error while instantiating the window implementation class {0}.",
      "Provide a valid implementation class in window definition."),

  WINDOW_NOT_FOUND(49, "Window object {0} not found ", ErrorType.ERROR, 1,
      false, "Window name {0} provided is invalid.",
      "Provide a valid window name."),

  VALID_WINDOW_NOT_FOUND(
      50,
      "Valid window {0} not found with correct parameters",
      ErrorType.ERROR,
      1,
      false,
      "Error occurred while finding a valid window {0}. Parameters provided did not match with any defined window.",
      "Provide a valid window already defined."),

  INVALID_WINDOW_IDENTIFIER(
      51,
      "Window identifier {0} not valid",
      ErrorType.INTERNAL_ERROR,
      1,
      false,
      "An attempt to access an invalid window identifier {0} has been encountered.",
      "This is an internal error"),

  CANNOT_DROP_WINDOW_QUERY_EXISTS(
      52,
      "Cannot drop window {0} since query exists",
      ErrorType.ERROR,
      1,
      false,
      "Error occurred while dropping the window. Window {0} is currently referenced by one or more queries.",
      "Drop all the dependent queries. Then retry."),

  CANNOT_ENABLE_OR_DISABLE_STATS(53,
      "Cannot enable or disable stats since query {0} is not running",
      ErrorType.ERROR, 1, false,
      "Attempt made to enable or disable a non-running query {0} statistics",
      "Start the query. Then retry."),

  CANNOT_ENABLE_QUERY_STATS(
      54,
      "Cannot enable stats for query {0} since source operators not enabled",
      ErrorType.ERROR,
      1,
      false,
      "Error while enabling query {0} statistics. Statistics for reference stream or relation not yet enabled.",
      "Enable referenced stream or relation statistics. Then retry."),

  CANNOT_DISABLE_TABLE(
      55,
      "cannot disable Stream/Relation {0} stats since Query {1} is enabled",
      ErrorType.ERROR,
      1,
      false,
      "Error while disabling stream/relation {0} statistics. Dependent query {1} statistics are enabled.",
      "Disable statistics for all dependent queries. Then retry."),

  VIEW_SCHEMA_NOT_DEFINED(
      56,
      "View schema for {0} not defined in query",
      ErrorType.ERROR,
      1,
      false,
      "Error in view definition. View schema for {0} not defined",
      "Rewrite view creation statement with schema, e.g. CREATE view V (integer c1,float c2) as ..."),

  DUPLICATE_COLUMN_NAME(57, "Duplicate Column Name {0}", ErrorType.ERROR, 1,
      true, "Column Name {0} is Duplicate", "Remove Duplicate Column Name"),

  ONLY_ONE_PRIMARY_KEY(58, "Stream or relation can have only One Primary Key",
      ErrorType.ERROR, 1, true, "Self-Evident", "Remove the Extra Primary Key"),

  PRIMARY_KEY_NOT_DEFINED(59, "Primary Key is not defined in query {0}",
      ErrorType.ERROR, 1, true, "Primary key is not defined in query {0}",
      "Define a primary key in query"),

  NOT_A_SYSTS_SOURCE(
      60,
      "Source {0} is not declared as system timestamped",
      ErrorType.ERROR,
      1,
      true,
      "Attempt to set heartbeat timeout duration on a non system timestamped source",
      "Either recreate the source as a system timestamped source or do not set a heartbeat timeout"),

  INVALID_DERIVED_TIMESTAMP(61, "Derived timestamp for stream/relation {0} is not valid",
      ErrorType.ERROR, 1, true,
      "Derived timestamp expression is not a valid expression",
      "check the expression for validity"),

  CANNOT_ENABLE_STATS_FOR_DERIVED_TS(62,
      "Cannot enable stats for derived timestamped source {0}",
      ErrorType.ERROR, 1, true,
      "Source {0} has a derived timestamp. Cannot enable stats.",
      "Cannot enable stats for source {0}"),
     
  CANNOT_STOP_QUERY_VIEW_DESTINATION_EXISTS(63,
      "cannot stop query {0} as destination view has a running query",
      ErrorType.ERROR, 1, false,
      "Error while stopping the query {0}. Query has a destination view which is currently referenced by the running queries.",
      " Stop all the queries referencing the destination view. Then retry to stop the query."),
  
  ALIAS_NAME_REQUIRED(64,
      "Must name expression {1} with a column alias",
      ErrorType.ERROR,1, false,
      "No alias name provided for expression  number {1}.",
      "Provide an alias name for expression number {1}."),
      
  FUNCTION_IMPL_INSTANCE_NOT_FOUND(65,
      "implementation instance {0} for function not found",
      ErrorType.ERROR,
      1,
      false,
      "Error while registering the function. Implementation instance {0} not found.",
      "Register an implementation instance in user function locator feature."),
      
  TYPE_NOT_FOUND(66,
      "Type {0} not found",
      ErrorType.ERROR,
      1,
      false,
      "Type {0} was not found when handling access to non-native type",
      "Correct type name."),  
      
  TYPE_NOT_OF_COMPLEX_TYPE(67,
      "Type {0} must be a complex type",
      ErrorType.ERROR,
      1,
      false,
      "Type {0} must be a complex type",
      "Correct complex type name."),
      
  CARTRIDGE_NOT_FOUND(68,
      "Cartridge {0} not found",
      ErrorType.ERROR,
      1,
      false,
      "Cartridge {0} was not found",
      "Register cartridge or use valid cartridge name."),

  INVALID_SYMBOLIC_EXPRESSION(69,
      "Invalid expression: {0}",
      ErrorType.ERROR,
      1,
      false,
      "1){1}; or 2){2}; or 3){3}",
      "Verify symbols reference to valid variable name, attribute name, function name, " +
      "complex type name, method name, or field name."), 
      
  FIELD_NOT_FOUND(70,
      "Type {0} does not define field {1}",
      ErrorType.ERROR,
      1,
      false,
      "Type {0} does not define field {1}",
      "Select proper field."),

  CONSTRUCTOR_NOT_FOUND(71,
      "Type {0} does not define constructor {1}",
      ErrorType.ERROR,
      1,
      false,
      "{2}",
      "Select proper constructor."),
      
  METHOD_NOT_FOUND(72,
      "Type {0} does not define method {1}",
      ErrorType.ERROR,
      1,
      false,
      "{2}",
      "Select proper method."),
      
  METHOD_NOT_STATIC(73,
      "Unknown static method {1} for type {0}",
      ErrorType.ERROR,
      1,
      false,
      "Type {0} does not define static method {1}",
      "Select proper static method."),
      
  TYPELOCATOR_NOT_FOUND(74,
      "Type locator not present for cartridge {0}",
      ErrorType.ERROR,
      1,
      false,
      "Error while accessing type locator for cartridge {0}",
      "Provide type locator for cartridge {0}."),
      
  TYPE_FOR_STATIC_REF_NOT_FOUND(75,
      "Invalid expression {1} for static reference",
      ErrorType.ERROR,
      1,
      false,
      "Expression {1} is not a valid reference to a constructor or static method.",
      "Correct expression to reference to valid constructor or static method, or remove 'link' if expression " +
      "is referencing to an instance field, or method."), 
      
  NOT_OF_ARRAY_TYPE(76,
      "Type {0} is not of an array type",
      ErrorType.ERROR,
      1,
      false,
      "Type {0} is not of an array type, hence the index operator '[]' cannot be used",
      "Remove the index operator '[]' or select a different type."), 
      
  INVALID_CALL_TO_FUNCTION_OR_CONSTRUCTOR_OR_SYNONYM(77,
      "Invalid call to function or constructor: {0}",
      ErrorType.ERROR,
      1,
      false,
      "1){1}; or 2){2}; or 3){3}",
      "Verify function or constructor for complex type exists, is not ambiguous, and has the correct number of parameters."),
      
  SYNONYM_ALREADY_EXISTS(78, 
      "Duplicate SYNONYM creation", ErrorType.ERROR, 1,
      false, "SYNONYM with name {0} already exists.",
      "Create synonym with different name"),
      
  SYNONYM_NOT_FOUND(79,
	  "No synonym with the given name or id: {0}", ErrorType.ERROR, 1, 
	  false, 
	  "Synonym name or id {0} provided is invalid",
	  "Provide a valid synonym name."),
	  
  INVALID_SYMBOLIC_OR_SYNONYM_EXPRESSION(80,
	  "Invalid expression: {0}",
	   ErrorType.ERROR,
	   1,
	   false,
	   "1){1}; or 2){2}; or 3){3}; or 4){4}",
	   "Verify symbols reference to valid variable name, attribute name, function name, " +
	   "complex type name, method name, field name or synonym."),
	   
	AMBIGUOUS_TYPE(81,
	   "Ambiguous type: {0}",
	   ErrorType.ERROR,
	   1,
	   false,
	   "{1}", 
	   "Please try to fully qualify type {0} to avoid ambiguity."),
	   
	INVALID_CALL_TO_FUNCTION_OR_CONSTRUCTOR(82,
	   "Invalid call to function or constructor: {0}",
	   ErrorType.ERROR,
	   1,
	   false,
	   "1){1}; or 2){2}",
	   "Verify function or constructor for complex type exist, is not ambiguous, and have the correct number of parameters."),
	
	MISSING_GET_ACCESSOR_FOR_PROPERTY(83,
	   "Type {0} does not define get accessor for property {1}",
	   ErrorType.ERROR,
	   1,
	   false,
	   "Type {0} does not define get accessor for property {1}",
	   "Verify get accessor exists for property {1} in type {0}."),
	   
 INCORRECT_TYPE_FOR_COLUMN(84,
	   "Type of the specified {0} identifier column {1} is not bigint",
	   ErrorType.ERROR,
	   1,
	   false,
	   "{0} identifier column must be of bigint type.",
	   "Change the type of the specified {0} identifier column to bigint."),
	   
 INCORRECT_TYPE_FOR_TIMESTAMP_COLUMN(85,
     "Type of the column {0} specified as timestamp column is not correct",
     ErrorType.ERROR,
     1,
     false,
     "The column which is specified as timestamp column should be either of type timestamp or bigint",
     "Change the type of the specified column to timestamp or bigint"),
     
 QUERY_SHOULD_BE_ARCHIVED_DEPENDENT(86,
     "Query defining view {0} is not based on archived relation/stream",
     ErrorType.ERROR,
     1,
     false,
     "View {0} is declared as archived but the query defining it is not based on archived relation/stream",
     "Either change the query defining view to make use of archived relation/stream or don't mark the view as archived"
     ),
     
 QUERY_SHOULD_NOT_BE_ARCHIVED_DEPENDENT(87,
     "Query defining view {0} is based on archived relation/stream",
     ErrorType.ERROR,
     1,
     false,
     "View {0} is not declared as archived but the query defining it is based on archived relation/stream",
     "Either mark the view as archived and also provide event identifier clause or remove the reference to archived relation/stream in the query"
     ),
     
 CANNOT_ALTER_ORDERING_CONSTRAINT(88,
     "Cannot alter the ordering constraint of query or view {0} to {1}",
     ErrorType.ERROR,
     1,
     false,
     "Query or view {0} is dependent on a partitioned stream",
     "Do not specify the ordering constraint explicitly for the query or view {0}");	   

  private ErrorDescription ed;

  MetadataError(int num, String text, ErrorType type, int level,
      boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Metadata + num, text,
        type, level, isDocumented, cause, action, "MetadataError");
  }

  public ErrorDescription getErrorDescription()
  {
    return ed;
  }

}
