/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/LogicalPlanError.java /main/17 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/25/12 - adding new exception for invalid evaluation clause
                           usage
    vikshukl    06/16/11 - subquery support (not yet implemented at logical
                           level)
    sbishnoi    07/29/10 - XbranchMerge
                           sbishnoi_bug-9947670_ps3_main_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
    sbishnoi    07/28/10 - parametrize BAD_JOIN_WITH_EXTERNAL_RELN
    sborah      05/19/10 - Typo
    sbishnoi    05/27/09 - modifying error message
    sbishnoi    02/09/09 - adding new error for logplan
    anasrini    03/05/08 - 
    rkomurav    02/28/08 - parameterize errors
    parujain    12/18/07 - add exception
    parujain    11/09/07 - external source
    parujain    10/25/07 - db join
    mthatte     10/22/07 - adding exceptions related to ondemand relations
    parujain    06/27/07 - order by error
    sbishnoi    05/22/07 - add cause,action
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error descriptions
    anasrini    02/01/07 - Messages
    hopark      12/06/06 - add UNBOUND_STREAM_NOT_ALLOWED
    anasrini    04/20/06 - errors related to GROUP BY/Aggr operator 
    najain      03/22/06 - add more errors
    najain      02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/LogicalPlanError.java /main/17 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Logical Plan module
 * @since 1.0
 */
public enum LogicalPlanError implements ErrorCode {
  TABLE_ATTR_NOT_FOUND(
    1,
    "table attribute not found",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Required Stream/Relation Schema {0} not exists",
    "Retry with correct Stream/Relation Schema"
  ),

  LOGOPT_NOT_CREATED(
    2,
    "logical operator not created",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "LogOptFactory failed to create Logical Operator",
    "Retry with proper arguments to Factory Constructor"
  ),

  LOGOPT_RELNSRC_NOT_CREATED(
    3,
    "logical operator relation source not created",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "LogOptRelSrcFactory failed to create Logical Operator Relation Source ",
    "Retry with proper arguments to Factory Constructor"
  ),

  LOGOPT_STRMSRC_NOT_CREATED(
    4,
    "logical operator stream source not created",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "LogOptStreamSrcFactory failed to create Logical Operator Stream Source ",
    "Retry with proper arguments to Factory Constructor"
  ),

  TOO_MANY_GROUP_ATTRS(
    5,
    "too many group attributes",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The group-by list contains attributes more than MAX number of attributes {0} for a table",
    "Use MAX {0} or less attributes in group-by list"
  ),

  TOO_MANY_AGGR_ATTRS(  
    6,
    "too many aggregation attributes",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The select list contains aggregation expressions more than MAX number of expressions {0}",
    "Use MAX {0} or less attributes in select list"
  ),

  TOO_MANY_OUT_ATTRS(
    7,
    "too many out attributes",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The group-by list and select list contains attributes more than MAX number of attributes {0}",
    "Use MAX {0} or less attributes in group-by list and select list"
  ),

  UNBOUND_STREAM_NOT_ALLOWED(
    8,
    "unbound stream not allowed",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "A stream input was applied to Logical Operator {0} ",
    "Do not use stream input for Logical Operator {0}"
  ),

  DUMMY_ERROR(
    9,
    "dummy error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "this is a dummy error",
    "this is a dummy error"
  ),
  
  BAD_JOIN_WITH_EXTERNAL_RELN(
    11,
    "external relation {0} must be joined with a stream having NOW window",
    ErrorType.ERROR,
    1,
    false,
    "external relation {0} was used without joining with stream having NOW window",
    "examine query containing external relation {0}"),

  TABLE_NOT_FOUND(
    12,
    "table should be already registered",
    ErrorType.ERROR,
    1,
    false,
    "Table {0} should already be registered before using in a query",
    "Register the table {0} before using in a query"),
    
  NOT_VALID_OUTER_JOIN_WITH_EXTERAL_RELATION(
    13,
    "invalid outer join with external relation",
    ErrorType.ERROR,
    1,
    false,
    "External relation was used as outer join table",
    "Do not use external relation as outer join table"),
    
  INVALID_ORDER_BY_USAGE(
    14,
    "invalid use of order-by clause",
    ErrorType.ERROR,
    1,
    false,
    "Input to order-by is relation",
    "Either specify the number of ordered window rows or use order-by on a stream"),

  SUBQUERY_NOT_IMPLEMENTED(
    15,
    "Subqyery feature not yet implemented beyond semantic analysis",
    ErrorType.ERROR,
    1,
    false,
    "subqyery feature not yet implemented",
    "....Coming soon...."),
     
   INVALID_SLIDE_USAGE(
     16,
     "slide is not allowed in value window over stream",
     ErrorType.ERROR,
     1,
     false,
     "slide is specified in value window over stream",
     "please use slide with value window over relation"),
     
   INVALID_EVALUATE_CLAUSE_USAGE(
     17,
     "evaluate clause can only be applied on a relation",
     ErrorType.ERROR,
     1,
     false,
     "evaluate clause is specified on a stream",
     "use evaluate clause with a relation input."
     );


  private ErrorDescription ed;

  LogicalPlanError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    this.ed = new ErrorDescription(ErrorNumberBase.Server_Logplan + num, text, type, level,
        isDocumented, cause, action, "LogicalPlanError");
  }

  public ErrorDescription getErrorDescription()  {
    return ed;
  }

}
