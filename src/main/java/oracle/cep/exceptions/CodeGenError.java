/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/CodeGenError.java /main/10 2012/11/25 20:07:46 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of the error codes for the Code Generation module

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/25/12 - XbranchMerge sbishnoi_bug-14626022_ps6 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    11/19/12 - adding cause in PREDICATE CLAUSE NOT SUPPORTED
    sbishnoi    03/29/10 - adding new error related to external predicate
    sbishnoi    12/09/08 - adding external query related errors
    skmishra    06/06/08 - adding error for xmlarg
    hopark      02/05/08 - parameterized error
    sbishnoi    05/22/07 - add cause,action
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error description 
    rkomurav    02/01/07 - add methods
    anasrini    03/14/06 - Creation
    anasrini    03/14/06 - Creation
    anasrini    03/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/CodeGenError.java /main/10 2012/11/25 20:07:46 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Code Generation module
 *
 * @since 1.0
 */

public enum CodeGenError implements ErrorCode {
  CONST_OVERFLOW_ERROR(
    1,
    "constant overflow error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Addition of new {0} constant attribute crossed Maximum Attribute limit {1} per table",
    "Use MAX {1} or less attributes per table"
  ),
  XML_ARG_ERROR(
    2,
    "error getting xmlarg",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "exception from attrval.getItem()",
    "Check xml functions"
  ),    
  PREDICATE_CLAUSE_NOT_SUPPORTED(
   3,
   "predicate clause {0} not supported",
   ErrorType.ERROR,
   1,
   false,
   "{1}",
   "please modify the predicate clause"
  ),    
  ERROR_STARTING_EXTERNAL_QUERY(
   4,
   "error starting query to external source",
   ErrorType.ERROR,
   1,
   false,
   "error starting query to external source",
   "verify either the connection or the query"
  ),
  RUNAWAY_PREDICATE_NOT_ALLOWED(
   5,
   "specified predicate requires full scan of external source which is not supported",
   ErrorType.ERROR,
   1,
   false,
   "specified predicate requires full scan of external source which is not supported",
   "please modify the join predicate"
  ),
  ARCHIVER_FINDER_SERVICE_NOT_AVAILABLE(
   6,
   "Archiver finder service is not available",
   ErrorType.ERROR,
   1,
   false,
   "Archiver Finder Service is not declared in the configuration",
   "Please specify the Archiver Finder Service in the configuration"
  ),
  ARCHIVER_NOT_FOUND(
   7,
   "No archiver with name {0} found",
   ErrorType.ERROR,
   1,
   false,
   "Specified archiver name {0} is not valid as there is no archiver with this name",
   "Make sure that the specified archiver name is correct"
  ),
  ARCHIVERNAME_NOT_SPECIFIED(
   8,
   "No archiver name specified",
   ErrorType.ERROR,
   1,
   false,
   "Archiver name is not specified",
   "Please specify a valid archiver name in the Archiver clause of the archived relation on which this query is dependent"
  ),
  INVALID_PATTERN_SYNTAX
  (
    9,
    "pattern expression {0} is invalid or not-supported",
    ErrorType.ERROR,
    1,
    false,
    "Failed to compile pattern expression {0}",
    "Please provide a valid pattern expression"    
  )
  ;
  
  private ErrorDescription ed;
  
  
  CodeGenError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.CqlEngine_Server_CodeGen + num, text, type, level,
        isDocumented, cause, action, "CodeGenError");    
  }

  public ErrorDescription getErrorDescription() {
    return ed;
  }
  
}
