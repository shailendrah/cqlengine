/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/SemanticError.java /main/62 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Enumeration of the error codes for the Semantic Analysis module

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/14 - adding new error for partitioned stream
    sbishnoi    05/12/13 - adding new semantic error for not conversion from
                           extensible to native bigint or int
    sbishnoi    01/04/13 - validate arithmetic expression for BI mode
    pkali       08/30/12 - XbranchMerge pkali_bug-14465875_ps6 from
                           st_pcbpel_11.1.1.4.0
    pkali       08/27/12 - removed arg in pred_clause_not_support
    pkali       06/29/12 - error codes for multi stream list validation
    vikshukl    09/28/11 - edit error message
    sbishnoi    09/06/11 - adding new semantic checks for value window
    alealves    07/21/11 - XbranchMerge alealves_bug-12685685_cep_main from
                           main
    alealves    06/21/11 - Support for concurrent views
    alealves    06/21/11 - XbranchMerge alealves_bug-12584321_cep from main
    vikshukl    06/17/11 - subquery support
    vikshukl    03/20/11 - support for n-ary set operations
    sbishnoi    03/16/11 - adding new error to check whether range expression
                           evaluates to INT or BIGINT
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sborah      06/23/10 - invalid max rows value
    sborah      06/02/10 - invalid_outer_join_condition
    sborah      05/19/10 - Typo
    sbishnoi    01/05/10 - adding table function errors
    vikshukl    08/24/09 - semantic checking for ISTREAM (r) DIFFERENCE USING
    sbishnoi    09/23/09 - support for table function
    sbishnoi    05/28/09 - adding new semantic error
    sbishnoi    03/17/09 - modifying one error message
    sbishnoi    02/09/09 - adding new errors for orderby
    udeshmuk    02/04/09 - the expr in duration clause should evaluate to int,
                           add an error code for that
    sbishnoi    01/15/09 - adding PREDICATE_CLAUSE_NOT_SUPPORTED
    udeshmuk    09/17/08 - add error code for correlation variable
    udeshmuk    07/12/08 - 
    rkomurav    07/04/08 - add reruing non event error
    skmishra    09/09/08 - adding exception for order by col in xmlagg
    parujain    07/08/08 - Value based windows
    skmishra    06/12/08 - 
    parujain    06/06/08 - xmltype support
    parujain    05/19/08 - xml publishing fn
    rkomurav    05/13/08 - add aggr param not a group var. error
    udeshmuk    04/26/08 - parameterize remaining errors.
    udeshmuk    04/24/08 - support for aggr distinct
    rkomurav    04/21/08 - add first and last error
    rkomurav    04/01/08 - add sem checks for FIRS and LAST with second param
    rkomurav    03/21/08 - add subset name already registered error
    udeshmuk    02/19/08 - add error codes related to join expression involving
                           null.
    udeshmuk    02/05/08 - parameterize error.
    udeshmuk    12/19/07 - add new error code.
    sbishnoi    12/18/07 - added errors related to update semantics
    najain      12/03/07 - add xmltype related errors
    udeshmuk    12/11/07 - add error for multiple predicates in outer join.
    rkomurav    11/27/07 - add semantic checks for PREV
    udeshmuk    10/01/07 - new error codes for number of attr. mismatch and
                           schema mismatch in set operations like intersect.
    udeshmuk    09/27/07 - rename attr invalid error
    sbishnoi    09/25/07 - add NO_VALID_COMPARISON_ATTRS
    udeshmuk    09/18/07 - Removing error code STREAM_NOT_ALLOWED_HERE.
    udeshmuk    09/13/07 - Adding error code for stream not allowed in union
                           all.
    rkomurav    09/05/07 - add prevnotallowedhere
    rkomurav    08/20/07 - add invalide stream/attr combination error
    hopark      08/01/07 - add no_events_allowed
    parujain    06/26/07 - 
    hopark      06/13/07 - add logging errors
    hopark      05/29/07 - add logging error
    anasrini    05/27/07 - pattern aggregation support
    anasrini    05/23/07 - add RECOGNIZE_OVER_REL_ERROR
    anasrini    05/21/07 - add cause, action
    sbishnoi    04/27/07 - Added arguments to groupby error messages
    parujain    03/29/07 - Case Exceptions
    parujain    03/13/07 - slide greater than range
    sbishnoi    03/06/07 - Add WRONG_NUMBER_OR_TYPES_OF_ARGUMENTS
    rkomurav    02/01/07 - add methods.
    rkomurav    12/05/06 - add group by error codes
    parujain    11/21/06 - Type conversion exceptions
    rkomurav    11/14/06 - add error for outer join multiple preds
    ayalaman    07/29/06 - add semantic errors for PARTITION BY
    anasrini    07/10/06 - support for user defined aggregations 
    anasrini    06/13/06 - support for user functions 
    anasrini    02/26/06 - add more error codes 
    anasrini    02/22/06 - Add TYPE_MISMATCH_ERROR etc. 
    anasrini    02/15/06 - Creation
    anasrini    02/15/06 - Creation
    anasrini    02/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/SemanticError.java /main/62 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;


import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Semantic Analysis module
 *
 * @since 1.0
 */

public enum SemanticError implements ErrorCode {
  
  SEMANTIC_ERROR(
    1,
    "generic semantic error",
    ErrorType.ERROR,
    1,
    true,
    "This is a generic semantic analysis error",
    "This is a generic semantic analysis error"
  ),

  UNKNOWN_TABLE_ERROR(
    2,
    "STREAM or RELATION or VIEW {0} does not exist",
    ErrorType.ERROR,
    1,
    true,
    "No STREAM or RELATION or VIEW named {0} has been created ",
    "Create a STREAM or RELATION or VIEW named {0}"
  ),

  NOT_A_RELATION_ERROR(
    3,
    "{0} is not a RELATION",
    ErrorType.ERROR,
    1,
    true,
    "{0} is either a STREAM or a VIEW that evaluates to a STREAM",
    "Create a relation, view or subquery {0} that evaluates to a RELATION"
  ),

  NOT_A_STREAM_ERROR(
    4,
    "{0} is not a STREAM",
    ErrorType.ERROR,
    1,
    true,
    "{0} is either a RELATION or a VIEW that evaluates to a RELATION",
    "Create a STREAM named {0} or create a VIEW named {0} whose defining query evaluates to a STREAM"
  ),

  AMBIGUOUS_TABLE_ERROR(
    5,
    "duplicate alias {0}. Attributes may be ambiguously defined",
    ErrorType.ERROR,
    1,
    true,
    "There are two entities (STREAM or RELATION or VIEW) in the FROM clause with the same name or alias {0}",
    "Use different aliases for the entities in the FROM clause"
  ),

  WINDOW_OVER_REL_ERROR(
    6,
    "window applied over the relation {0}",
    ErrorType.ERROR,
    1,
    true,
    "A window is being applied over the relation {0}.",
    "Either create a STREAM, VIEW or subquery block named {0} that evaluates to a STREAM or remove the window clause"
  ),

  SYMBOL_TABLE_FULL_ERROR(
    7,
    "symbol table is full",
    ErrorType.ERROR,
    1,
    true,
    "The maximum capacity of the symbol table has been reached",
    "Break up the query into multiple smaller queries"
  ),

  UNKNOWN_ATTR_ERROR(
    9,
    "invalid attribute {0}",
    ErrorType.ERROR,
    1,
    true,
    "There is no attribute named {0} that is valid in this scope",
    "Use a valid attribute name"
  ),

  AMBIGUOUS_ATTR_ERROR(
    10,
    "attribute {0} ambiguously defined",
    ErrorType.ERROR,
    1,
    true,
    "There are two entities in the FROM clause both of which have an attribute named {0}",
    "Prefix the attribute {0} with the alias of the entity"
  ),

  UNKNOWN_VAR_ERROR(
    11,
    "symbol {0} is unknown",
    ErrorType.ERROR,
    1,
    false,
    "There is no symbol named {0}",
    "Reference a symbol that has been defined appropriately"
  ),

  TOO_MANY_GROUPBY_ATTRS_ERROR(
    14,
    "too many group-by attributes",
    ErrorType.ERROR,
    1,
    true,
    "The group-by list contains more than the maximum number({0}) of allowed attributes",
    "Use {0} or less attributes in the group-by list"
  ),

  TOO_MANY_SEL_LIST_EXPRS_ERROR(
    15,
    "too many select list expressions",
    ErrorType.ERROR,
    1,
    true,
    "The select list contains more than the maximum number({0}) of allowed expressions",
    "Use {0} or less expressions in the select list"
  ),

  TOO_MANY_PARTNBY_ATTRS_ERROR(
    16,
    "too many partition-by attributes",
    ErrorType.ERROR,
    1,
    true,
    "The partition-by list contains more than the maximum number({0}) of allowed attributes",
    "Use {0} or less attributes in the partition-by list"
  ),

  AGGR_FN_NOT_ALLOWED_HERE(
    18,
    "Aggregate function {0} is not allowed here",
    ErrorType.ERROR,
    1,
    true,
    "An aggregate function is not allowed in this context",
    "Do not use an aggregation function in this context"
  ),

  TOO_MANY_FUNCS_TYPE_CONVERSION_ERROR(
    21,
    "too many functions named {0} match this call",
    ErrorType.ERROR,
    1,
    true,
    "More than one registered function matched the call after implicit conversions of the parameter datatypes",
    "Check the spelling of the registered function. Also confirm that its call is correct and its parameters are of correct datatypes"
  ),

  NOT_A_GROUP_BY_EXPRESSION(
    22,
    "not a GROUP BY expression",
    ErrorType.ERROR,
    1,
    true,
    "An expression that is not a GROUP BY expression is present in the SELECT list or HAVING clause or ORDER BY clause",
    "Include the expression in the GROUP BY list or remove the expression from the SELECT list or HAVING clause or ORDER BY clause"
  ),

  NOT_A_SINGLEGROUP_GROUP_FUNCTION(
    23,
    "not a single-group group function",
    ErrorType.ERROR,
    1,
    true,
    "An attribute is referenced in the select list but not in the context of an aggregate function. This is not permitted when there is no GROUP BY clause",
    "Include a GROUP BY clause containing the attribute or remove the reference to the attribute in the select list "
  ),

  WRONG_NUMBER_OR_TYPES_OF_ARGUMENTS(
    25,
    "wrong number or types of arguments in call to {0}",
    ErrorType.ERROR,
    1,
    true,
    "This error occurs when the named function call cannot be matched to any declaration for that function name. The function name might be misspelled, a parameter might have the wrong datatype, or the function declaration might be faulty ",
    "Check the spelling of the registered function. Also confirm that its call is correct and its parameters are of correct datatypes."
  ),

  SLIDE_GREATER_THAN_RANGE(
    26,
    "slide value {0} is greater than range value {1}",
    ErrorType.ERROR,
    1, 
    true, 
    "The number of slide units is greater than the number of range units", 
    "Make the slide units equal to or smaller than the range units"
  ),

  NOT_ALL_RESULTS_CAN_BE_NULL_IN_CASE(
    27, 
    "All result expressions cannot be the literal NULL in CASE", 
    ErrorType.ERROR,
    1, 
    true, 
    "All the result expressions in the CASE statement are the literal NULL", 
    "Have at least one result expression that is not the literal NULL"
  ),

  RETURN_TYPE_MISMATCH_IN_CASE(
    28, 
    "inconsistent datatypes: expected {0} got {1}",
    ErrorType.ERROR, 
    1, 
    true, 
    "Return expression for each condition and the else expression(if provided) are not all of the same datatype", 
    "Ensure that the return expression of each condition and the else expression(if provided) are all of the same datatype" 
  ),

  RECOGNIZE_OVER_REL_ERROR(
    29,
    "MATCH_RECOGNIZE clause may not be applied over a relation {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "The MATCH_RECOGNIZE clause is being applied over a relation", 
    "Create the entity over which the MATCH_RECOGNIZE clause is being applied as a stream"
  ),

  AGGR_DOES_NOT_REF_CORR(
    30,
    "Aggregation does not reference a correlation name",
    ErrorType.ERROR,
    1,
    true,
    "An aggregation in the MATCH_RECOGNIZE clause does not reference a correlation name",
    "Reference a correlation attribute in the parameter to the aggregation"
  ),

  AGGR_REFS_MORE_THAN_ONE_CORR(
    31,
    "Aggregation references more than one correlation name",
    ErrorType.ERROR,
    1,
    true,
    "An aggregation in the MATCH_RECOGNIZE references more than one correlation name",
    "Reference exactly one correlation name in the parameter to the aggregation"  
  ),

  CORR_VAR_ALREADY_EXISTS(
    32,
    "duplicate correlation name {0}",
    ErrorType.ERROR,
    1,
    true,
    "Correlation name {0} is already defined in the DEFINE sub clause",
    "Use a different correlation name"
  ),

  INVALID_LOGGING_DRILLDOWN(
    33, 
    "index and queue does not have type/id drilldowns", 
    ErrorType.ERROR,
    1, 
    true, 
    "type or id drilldown is used for logging index or queue", 
    "Remove type or id drilldown"
  ),

  INVALID_LOGGING_TYPE(
    34, 
    "invalid logging type is used for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "invalid logging type is used", 
    "Use proper logging types"
  ),

  LOG_NO_IDS_SPECIFIED(
    35, 
    "no id(s) specified for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "id(s) are required", 
    "Use id(s) in the logging ddl"
  ),

  LOG_NO_TYPES_ALLOWED(
    36, 
    "type(s) not allowed for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "types are not allowed", 
    "Remove types from the logging ddl"
  ),

  LOG_NO_IDS_ALLOWED(
    37, 
    "id(s) not allowed for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "id(s) are not allowed", 
    "Remove id(s) from the logging ddl"
  ),
    
  LOG_UNKNOWN_EVENT(
    38, 
    "unknown event number is used for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "unknown event number is used", 
    "Use known event numbers."
  ),
    
  LOG_UNKNOWN_LEVEL(
    39, 
    "unknown level number is used for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "unknown level number is used", 
    "Use known level numbers."
  ),
  
  LOG_INVALID_EVENT(
    40, 
    "invalid event {0} is used for {1}", 
    ErrorType.ERROR,
    1, 
    true, 
    "invalid event is used", 
    "Use proper event for area."
  ),

  LOG_INVALID_LEVEL(
    41, 
    "invalid level {0} is used for {1}", 
    ErrorType.ERROR,
    1, 
    true, 
    "invalid level is used", 
    "Use proper level for area."
  ),
  
  LOG_UNKNOWN_QUERY(
    42, 
    "unknown query name {0} is used for {1}", 
    ErrorType.ERROR,
    1, 
    true, 
    "unknown query name is used", 
    "Use proper query name."
  ),

  LOG_NO_NAMES_SPECIFIED(
    43, 
    "no query name(s) specified for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "query name(s) are required", 
    "Use query name(s) in the logging ddl"
  ),

  LOG_NO_LEVELS_SPECIFIED(
    44, 
    "no level(s) specified for {0}", 
    ErrorType.ERROR,
    1, 
    true, 
    "level(s) are required for the logging ddl", 
    "Use level(s) in the logging ddl"
  ),
   
  TOO_MANY_ORDERBY_ATTRS_ERROR(
    45,
    "too many order-by attributes",
    ErrorType.ERROR,
    1,
    true,
    "The order-by list contains more than {0} attributes",
    "Use {0} or less attributes in the order-by list"
  ),
     
  ORDER_BY_POSITION_NOT_A_VALID_POSITION(
    46,
    "The provided position ({0}) of order by attribute in project list is not valid",
    ErrorType.ERROR,
    1,
    true,
    "Order by position is not valid in project list",
    "Use a valid project list position"
  ),
     
  ORDER_BY_EXPRESSION_NOT_AN_ATTRIBUTE(
    47,
    "Specified order by expression pointing to position {0} in project list is not a valid attribute",
    ErrorType.ERROR,
    1,
    true,
    "Order by expression should be an attribute",
    "Ensure that order by expression is an attribute"
  ),
    
  ORDER_BY_ATTRIBUTE_NOT_IN_PROJECT_LIST(
    48,
    "order by attribute not present in project list",
    ErrorType.ERROR,
    1,
    true,
    "order by attribute is not present in project list",
    "order by attribute should be present in project list"
  ),
      
  LOG_NO_EVENTS_ALLOWED(
    49, 
    "event(s) not allowed for dump", 
    ErrorType.ERROR,
    1, 
    true, 
    "events are not allowed", 
    "Remove events from the logging ddl" 
  ),
      
  CORRELATION_ATTR_NOT_ALLOWED_HERE(
    50,
    "correlation attr {0} not allowed here",
    ErrorType.ERROR,
    1,
    true,
    "A correlation name is not allowed in this context",
    "Do not use the correlation name in this context"
  ),
  
  FN_NOT_ALLOWED_HERE(
    51,
    "function {0} not allowed here",
    ErrorType.ERROR,
    1,
    true,
    "A function is not allowed in this context",
    "Do not use the function in this context"
  ),
  
  INVALID_COMPARISON_ATTRS(
    52,
    "Invalid Comparison Attributes",
    ErrorType.ERROR,
    1,
    true,
    "There are no valid comparison attributes",
    "Use atleast one valid comparison attribute both inside input schemas"
  ),
  
  NUMBER_OF_ATTRIBUTES_MISMATCH(
    53,
    "Number of attributes in {0} and {1} do not match",
    ErrorType.ERROR,
    1,
    true,
    "Number of attributes in left and right input do not match",
    "Ensure that the left and right inputs to UNION/UNION ALL/INTERSECT/MINUS operation have same number of attributes"
  ),
  
  SCHEMA_MISMATCH_IN_SETOP(
    54,
    "Attribute number {0} of {1} must have same datatype as corresponding attribute of {2}",
    ErrorType.ERROR,
    1,
    true,
    "Schemas of left and right input do not match",
    "Ensure that the left and right inputs to UNION/UNION ALL/INTERSECT/MINUS operation have same schema"
  ),
  
  INVALID_PREV_PARAM(
    55,
    "Invalid parameter to the PREV function",
    ErrorType.ERROR,
    1,
    true,
    "Correlation name specified as parameter to PREV is invalid",
    "Ensure that the correlation name parameter to PREV is same as the defining correlation name"
  ),
  
  TIMEOUT_NOT_SPECIFIED_EARLIER(
    56,
    "Timeout duration not set earlier for source {0}",
    ErrorType.ERROR,
    1,
    true,
    "Attempt to remove heartbeat timeout when it is not set earlier",
    "Heartbeat timeout can be removed only if it is set earlier for that source"
  ),
       
  INVALID_TIMEOUT_VALUE(
    57,
    "Invalid timeout value {0} specified",
    ErrorType.ERROR,
    1,
    true,
    "Timeout duration should be strictly greater than zero",
    "Provide timeout duration that is greater than zero"
  ),

  DUPLICATE_COLUMN_NAME(
    58,
    "Duplicate column name in primary key attributes",
    ErrorType.ERROR,
    1,
    true,
    "Duplicate column name specified in primary key attributes",
    "Remove duplicate column name in primary key attributes"
  ),

  STREAM_NOT_ALLOWED_HERE(
    59,
    "stream not allowed here",
    ErrorType.ERROR,
    1,
    true,
    "primary key constraints defined over stream",
    "define primary key constraints only over a relation"
  ),

  XMLTABLE_OVER_REL_ERROR(
    60,
    "XMLTable clause may not be applied over a relation", 
    ErrorType.ERROR,
    1, 
    true, 
    "The XmlTable clause is being applied over a relation", 
    "Create the entity over which the XmlTable clause is being applied as a stream"
  ),

  TOO_MANY_PREDICATES_IN_WHERE_ERROR(
    61,
    "too many predicates in the where clause",
    ErrorType.ERROR,
    1,
    true,
    "Query having outer join predicate cannot have any other predicate in where clause",
    "Specify only one predicate in where clause when the query has outer join predicate"
  ),
    
  CQL_COMMAND_NOT_ENDED_PROPERLY(
    62,
    "CQL command not end properly",
    ErrorType.ERROR,
    1,
    true,
    "The CQL statement ends with an inappropriate clause",
    "Correct the syntax by removing the inappropriate clause(s)"
  ),
  
  INVALID_RELATIONAL_OPERATOR(
    63,
    "invalid relational operator",
    ErrorType.ERROR,
    1,
    true,
    "A search condition was entered with an invalid or missing relational operator",
    "Include only valid relational operators"
  ),
  
  INCONSISTENT_DATATYPES_IN_CASE(
    64,
    "Inconsistent datatypes in case statement: expected {0} got {1}",
    ErrorType.ERROR,
    1,
    true,
    "An attempt was made to perform an operation on incompatible datatypes",
    "Make sure that all the comparison expressions in case statement are of same type"
  ),
  
  SUBSETNAME_ALREADY_REGISTERED(
    65,
    "Subset name {0} already registered",
    ErrorType.ERROR,
    1,
    true,
    "Subset name {0} is already registered as either a correlation name or a subset name",
    "Use some other name for the subset"
  ),
  
  SUBSETDEF_REFERS_SUBSETNAME(
    66,
    "Subset definition for {0} refers subset name {1}",
    ErrorType.ERROR,
    1,
    true,
    "Subset Definition for {0} contains reference to a subset name {1}",
    "Subset names are not allowed in the definition of a SUBSET"
  ),
  
  INVALID_FIRST_LAST_ARGUMENT(
    67,
    "Invalid second argument to aggregate function {0}",
    ErrorType.ERROR,
    1,
    true,
    "The second argument provided for {0} aggregate function is invalid",
    "Provide a non negative second argument to {0}"
  ),

  DISTINCT_NOT_ALLOWED_HERE(
    68,
    "DISTINCT option not allowed for this function {0}",
    ErrorType.ERROR,
    1,
    true,
    "DISTINCT option is not allowed for non-aggregate function",
    "Use DISTINCT option only for aggregate function"
  ),
  AGGR_PARAM_NOT_GROUP_VAR(
    69,
    "Aggregate parameter is not a group variable",
    ErrorType.ERROR,
    1,
    true,
    "Parameter to the aggregate function is not a group variable",
    "Use group variables as parameters to aggregate functions"
  ),
  
  INVALID_XML_ATTRIBUTE_NAME_EXPR(
    70,
    "Invalid Name Expression for given XML attribute",
    ErrorType.ERROR,
    1,
    true,
    "Name Expression is invalid, It should return a string",
    "Use expression which evaluates to a string"
  ),
   
  INVALID_XML_ELEMENT_NAME_EXPR(
    71,
    "Invalid Name Expression for given XML Element",
    ErrorType.ERROR,
    1,
    true,
    "Name Expression is invalid. It should return string",
    "Use element name expression which evaluates to a string"
  ),
     
  INVALID_CHARS_IN_XMLCDATA_ARG(
    72,
    "Invalid argument {0} to XMLCDATA",
    ErrorType.ERROR,
    1,
    true,
    "Argument contains characters that are not allowed in XMLCDATA expressions",
    "Remove invalid characters from args and rewrite."
  ),
  
  INVALID_XML_PARSE_EXPR(
    73,
    "Invalid argument {0} for XMLPARSE",
    ErrorType.ERROR,
    1,
    true,
    "Argument must be a singly rooted XML document",
    "Check argument to XMLPARSE"
  ),
  
  XML_PUB_FUNC_ARG_ERROR(
    74,
    "Invalid argument {0} to Xml publishing function {1}",
    ErrorType.ERROR,
    1,
    true,
    "Rewrite statement",
    "Refer documentation for xml publishing functions"
  ),
  
  INVALID_CHARS_IN_XMLCOMMENT_ARG(
    75,
    "Invalid argument {0} to XMLCOMMENT",
    ErrorType.ERROR,
    1,
    true,
    "The literal '--' is not allowed in XMLcomment expressions",
    "Remove '--' from args and rewrite."
  ),
  
  INVALID_XMLCONCAT_ARGUMENT(
    76,
    "Invalid argument {0} to XmlConcat",
    ErrorType.ERROR,
    1,
    true,
    "The argument {0} is not of Xmltype",
    "All arguments to XmlConcat must return Xmltype"
  ),
  
  INVALID_XMLTYPE_USAGE(
    77,
    "XMLInstances cannot be used as an operand in comparison operations",
    ErrorType.ERROR,
    1,
    true,
    "Expression {0} of XMLType cannot be used in comparison operations",
    "Ensure that none of the operands to comparison operators are instances of XMLtype"
  ),
  
  INVALID_DATATYPE_FOR_VALUE_BASED_WINDOWS(
    78,
    "Datatype {0} is not valid for value based windows",
    ErrorType.ERROR,
    1,
    true,
    "Datatype {0} cannot be used for value based windows",
    "Use a valid datatype for value based windows"
  ),
  
  INCONSISTENT_DATATYPES_IN_VALUE_BASED_WINDOWS(
    79,
    "Datatype {0} and {1} are not consistent with each other",
    ErrorType.ERROR,
    1,
    true,
    "Datatype {0} and {1} are not consistent with each other in windows",
    "Datatypes should be consistent with each other"),

  INVALID_AGGR_FUN_INPUT_TYPE(
    80,
    "invalid input type {0} for aggregate function {1}",
    ErrorType.ERROR,
    1,
    true,
    "An input type {0} other than the data type(s) " +
    "allowed for the aggregate function {1} has been specified",
    "Use only the allowed input type(s) for the aggregate function {1} Allowed input type(s) for this functions are: {2}"
  ),

  INVALID_AGGR_FUN_RETURN_TYPE(
    81,
    "invalid return type {0} for aggregate function {1}", 
    ErrorType.ERROR,
    1,
    true,
    "A return type {0} other than the allowed return type(s) for the" +
    " aggregate function {1} has been specified",
    "Use only the allowed return type(s) for the aggregate function {1} Allowed return type(s) for this function are: {2}"
  ),

  INVALID_NUM_EXPRESSIONS(
    82,
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
    83,
    "not enough arguments for DECODE function",
    ErrorType.ERROR,
    1,
    true,
    "Number of arguments are less than three",
    "Specify atleast three arguments"
  ),
  
  INVALID_ORDER_BY_IN_XMLAGG(
    84,
    "Order By within Xmlagg cannot contain column numbers",
    ErrorType.ERROR,
    1,
    true,
    "xmlagg order by contains column numbers",
    "re-write xmlagg statement with column NAMEs instead of NUMBERS"
  ),
  
  INCORRECT_RECURRING_NON_EVENT_USAGE(
     85,
     "Multiple Duration queries without ALL MATCHES clause are incorrect",
     ErrorType.ERROR,
     1,
     true,
     "Multiple Duration queries should have ALL MATCHES clause",
     "Use All Matches in Multiple Duration queries"
  ),

  CORR_ATTR_NOT_PRESENT_IN_PATTERN_CLAUSE(
    86,
    "Correlation attr {0} not present in PATTERN clause",
    ErrorType.ERROR,
    1,
    true,
    "Correlation attr {0} present in DEFINE clause is not present in PATTERN clause",
    "Include the correlation attr {0} as a part of PATTERN clause or remove it from DEFINE clause"
  ),
  PREDICATE_CLAUSE_NOT_SUPPORTED(
    87,
    "predicate clause not supported",
    ErrorType.ERROR,
    1,
    true,
    "predicate clause is not supported for the external query",
    "please modify the query clause"
  ),
  DURATION_EXPR_DOES_NOT_EVALUATE_TO_INT(
    88,
    "Duration expression in the pattern query does not evaluate to integer datatype",
    ErrorType.ERROR,
    1,
    true,
    "Duration Expression present in the current query evaluates to {0}",
    "Ensure that the expression provided in duration clause evaluates to integer"
  ),
  INVALID_ORDER_BY_USAGE_1(
    89,
    "invalid use of order-by clause",
    ErrorType.ERROR,
    1,
    true,
    "number of rows is less than or equal to zero",
    "ensure that number of rows is greater than zero"
  ),
  INVALID_OUTER_JOIN_USAGE(
    90,
    "old style outer join (+) cannot be used with ANSI joins",
    ErrorType.ERROR,
    1,
    true,
    "invalid usage of outer join",
    "use either old style outer join(+) or ANSI joins"
  ),
  INVALID_TABLE_CLAUSE_RETURN_TYPE(
    91,
    "invalid return type of parameter table expression {0}",
    ErrorType.ERROR,
    1,
    true,
    "invalid return type of table clause expression {0}",
    "ensure that table clause expression type is either a collection or an iterable object"
  ),
  USING_CLAUSE_EXPR_NOT_AN_ATTRIBUTE(
    92,
    "Specified USING clause expression is not a valid attribute",
    ErrorType.ERROR,
    1,
    true,
    "USING clause expression should be an attribute",
    "Ensure that USING clause expression is an attribute."
  ),
  USING_CLAUSE_EXPR_NOT_A_VALID_POSITION(
    93,
    "The specified position {0} in the USING clause is not a valid position in the SELECT list",
    ErrorType.ERROR,
    1,
    true,
    "USING clause expression specifies an invalid position",
    "Ensure that USING clause expression refers to a valid position in SELECT list."
  ),
  USING_CLAUSE_EXPR_NOT_A_VALID_SELEXPR(
    94,
    "USING clause expression does not refer to a valid SELECT expression",
    ErrorType.ERROR,
    1,
    true,
    "USING expression (alias or position) refers to a non-existent SELECT expression.",
    "Specify an expression in USING clause that refers to an existing SELECT list expression."
  ),
  USING_CLAUSE_SELEXPR_NOT_ALIASED(
    95,
    "SELECT list item is not an attribute, and does not specify an alias",
    ErrorType.ERROR,
    1,
    true,
    "USING clause identifier can not be matched against non-attribute SELECT list item that does not use an alias.",
    "Specify an alias for SELECT list item."
  ),
  TABLE_CLAUSE_RETURN_TYPE_MISMATCH(
      96,
      "data type {1} of attribute {0} does not match with specified type {2} ",
      ErrorType.ERROR,
      1,
      true,
      "type mismatch for output attribute of table clause",
      "ensure that data type of table clause attribute matches with specified type {2}"
  ),
  INVALID_OUTER_JOIN_CONDITION(
      97,
      "Invalid outer join condition {0}",
      ErrorType.ERROR,
      1,
      true,
      "Outer Join condition references relations not involved in the outer join",
      "Ensure that only relations involved in the outer join are referenced in the " +
      "outer join condition."
      ),
  INVALID_MAX_ROWS_VALUE(
      98,
      "Invalid max rows value {0} specified",
      ErrorType.ERROR,
      1,
      true,
      "Max rows should be strictly greater than zero",
      "Provide Max rows that is greater than zero"
        ),
  RANGE_EXPR_DOES_NOT_EVALUATE_TO_INT_BIGINT(
      99,
      "Range expression does not evaluate to integer or bigint datatype",
      ErrorType.ERROR,
      1,
      true,
      "Range Expression present in the current query evaluates to {0}",
      "Ensure that the expression provided in range clause evaluates to integer or bigint"
  ),
   INVALID_UNION_ALL_SET_OP(
       100,
       "UNION ALL operands do not evaluate to the same type",
       ErrorType.ERROR,
       1,
       true,
       "Both UNION ALL operands must evaluate to either STREAMs or RELATIONs",
       "Convert one of the operands to a STREAM or a RELATION " +
       "so that both operands are of the same type"
   ),
   INCORRECT_RECURRING_NON_EVENT_USAGE_1(
       101,
       "variable duration cannot be specified with recurring non-event detection",
       ErrorType.ERROR,
       1,
       true,
       "variable duration cannot be specified with recurring non-event detection",
       "make sure that you used fixed (constant) duration along with recurring non-event detection"
    ), 
    MISMATCHED_ORDERING_CONSTRAINT(
        102,
        "ordering constraint does not match with the ordering constraint of underlying view",
        ErrorType.ERROR,
        1,
        true,
        "ordering constraint must match with that of the underlying view",
        "change the ordering constraint of the derived query/view or of the underlying view"
   ),
   SUBQUERY_SELECT_EXPR_NOT_ALIASED(
       103,
       "Subquery select expression at {0} is not aliased",
       ErrorType.ERROR,
       1,
       true,
       "Subquery select items need to be aliased",
       "Specify an alias like AS aliasname for the SELECT expression at {0}"
    ),
   SUBQUERY_ALIAS_NOT_PROVIDED(
       104,
       "Subquery or inline view is not aliased",
       ErrorType.ERROR,
       1,
       true,
       "Unnamed inline view or subquery is not supported (yet)",
       "Specify a subquery alias like AS aliasname in the FROM clause"       
   ),
   INVALID_VALUE_WINDOW_PERIOD(
       105,
       "invalid period specified in CurrentPeriod window",
       ErrorType.ERROR,
       1,
       true,
       "period specified in CurrentPeriod window is either not a valid clock " +
       "time value or in decreasing order of time values",
       "use a period between 0000 and 2359"       
   ),
   INVALID_VALUE_WINDOW_SLIDE(
       106,
       "invalid slide specified in value window",
       ErrorType.ERROR,
       1,
       true,
       "specified slide value is neither a positive fixed length integer nor a timestamp value",       
       "use a positive fixed length slide value or use a timestamp value"       
   ),
   INVALID_SOURCE_INPUT(
       107,
       "invalid source input",
       ErrorType.ERROR,
       1,
       true,
       "Input source need to be of type stream or subquery",       
       "Make sure the input source is either stream or subquery"       
   ),
   DATETIME_ARITHMETIC_OPERATION_NOT_SUPPORTED(
       108,
       "given target doesnot support datetime arithmetic operation {0} on interval type attributes",
       ErrorType.ERROR,
       1,
       true,
       "given target does not allow arithmetic operation {0} on interval datatype",       
       "modify query to use appropriate arithmetic expression"
      ),
  EXTENSIBLE_RANGE_EXPR_DOES_NOT_EVALUATE_TO_INT_BIGINT(
      109,
      "extensible range expression does not evaluate to integer or bigint datatype",
       ErrorType.ERROR,
       1,
       true,
       "extensible range expression can not be converted to cql native type integer or bigint",
       "ensure that the expression provided in range clause evaluates to cql native type integer or bigint"
  ),
  MULTIPLE_PARTITIONED_SOURCES_NOT_ALLOWED (
      110,
      "invalid query {0} as the from clause contains more than one partition stream or view depending on a partition stream",
      ErrorType.ERROR,
      1,
      true,
      "query {0} is either defined using more than one partition stream or views dependending on partition stream in its from clause",
      "modify query to use only one partitioned source"
  );

  private ErrorDescription ed;
  
  SemanticError(int num, String text, ErrorType type, int level, 
                boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Semantic + num,
                              text, type, level, isDocumented, cause, action,
                              "SemanticError");
  }

  public ErrorDescription getErrorDescription() {
    return ed;
  }
  
}
