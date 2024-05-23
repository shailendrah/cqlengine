/* $Header: pcbpel/cep/server/src/oracle/cep/exceptions/ServerError.java /main/5 2009/03/17 23:10:41 skmishra Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    server error codes 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    03/13/09 - adding visualizer xml error
    rkomurav    05/22/07 - 
    skmishra    05/21/07 - 
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error descriptions
    anasrini    02/01/07 - Messages
    ayalaman    03/14/06 - server error codes 
    ayalaman    03/14/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/exceptions/ServerError.java /main/5 2009/03/17 23:10:41 skmishra Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Error code for the CEP server related errors 
 */

public enum ServerError implements ErrorCode
{
  INVALID_USE_ERROR(            
    1,
    "invalid use error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "This is a generic cep server error",
    "This is a generic cep server error"
  ),

  INVALID_REGISTER_TABLE_COMMAND(
    2,
    "invalid register table command",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "parse error on create STREAM or RELATION or VIEW named {0}",
    "edit and reissue create STREAM or RELATION or VIEW named {0}"
  ),

  INVALID_REGISTER_QUERY_COMMAND(
    3,
    "invalid create query command",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "parse error on create QUERY named {0}",
    "edit and reissue create QUERY named {0}"
  ),

  INVALID_QUERY( 
    4,
    "invalid query",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "parse error on create QUERY named {0}",
    "edit and reissue create QUERY named {0}"
  ),

  MAX_NUM_QUERIES_EXCEEDED(
    5,
    "max number of queries exceeded",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "create QUERY named {0} exceeds max number of queries",
    "drop QUERIES which are not required and call create QUERY named {0}"
  ),

  INVALID_QUERY_IDENTIFIER(
    6,
    "invalid query identifier",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "This identifier does not refer to any registered query",
    "Use a valid query identifier"
  ),

  INVALID_REGISTER_MONITOR_COMMAND(
    7,
    "invalid register monitor command",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "parse error on create MONITOR named {0}",
    "edit and reissue create MONITOR named {0}"
  ),

  INVALID_ATTRLIST_SPEC(
    8,
    "invalid attribute list specification",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Attribute list names not valid",
    "Specify a valid list of attributes"
  ),

  SERVER_WAIT_INTERRUPTED(
    9,
    "server wait interrupted",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Interrupt caused the server to come out of wait state",
    "Catch the interrupt and requeue the read"
  ),

  INVALID_REGISTER_VIEW_SPEC(
    10,
    "invalid register view specification",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "parse error on create VIEW named {0}",
    "edit and reissue create VIEW named {0}"
  ),

  INTERNAL_ERROR(
    11,
    "server internal error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "This is a generic server internal error",
    "This is a generic server internal error"
  ),
  
  UNSUPPORTED_QCXML_OPERATION(
    12,
    "This query is not supported by query constructor",
    ErrorType.ERROR,
    1,
    false,
    "This query is not supported by query constructor",
    "This action cannot be executed on the CQL engine"
  );

  private ErrorDescription ed;

  ServerError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_Parser + num, text, type, level,
        isDocumented, cause, action, "ServerError");
  }

  public ErrorDescription getErrorDescription()
  {
    return ed;
  }
}
