/* $Header: NetworkError.java 22-may-2007.00:37:43 skmishra Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
     Error codes for the Network related errors/exceptions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    05/22/07 - add error cause and action
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error descriptions
    anasrini    02/01/07 - Messages
    ayalaman    03/14/06 - add new error codes 
    ayalaman    03/03/06 - Network data source related errors 
    ayalaman    03/03/06 - Creation
 */

/**
 *  @version $Header: NetworkError.java 22-may-2007.00:37:43 skmishra Exp $
 *  @author  ayalaman
 *  @since   1.0
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Network events module
 * @since 1.0
 */
public enum NetworkError implements ErrorCode
{
  GENERIC_NETWORK_ERROR(
    1,
    "generic network error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "This is the generic network error",
    "This is the generic network error"
  ),

  NETWORK_MANAGER_FAILURE( 
    2,
    "network manager failure",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Network manager initialization error ",
    "Check for bind errors, clear the port and restart the server"
  ),

  NET_MANAGER_OUT_OF_RESOURCES(
    3,
    "network manager out of resources",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Maximum number of connects to network manager exceeded",
    "Either increase the max socket connections or shut some clients down"
  ),

  MESSAGE_FORMAT_ERROR(
    4,
    "message format error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Message is not in prescribed format",
    "Recreate the message in the proper format"
  ),

  UNSUPPORTED_COMMAND(
    5,
    "unsupported command",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Remote command identifier is not understood by the server",
    "Specify correct remote command identifier"
  ),

  COMMAND_PROCESSING_ERROR(
    6,
    "command processing error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The arguments to the remote command has an error",
    "Specify right list of arguments to the remote command"
  ),

  INVALID_INPUT_CONN_KEY( 
    7,
    "command processing error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The input connection key to the remote command has an error",
    "Specify the correct input connection key"
  ),

  INVALID_OUTPUT_CONN_KEY(
    8,
    "command processing error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "The output connection key to the remote command has an error",
    "Specify the correct output connection key"
  ),

  MAX_INPUT_CONN_EXCEEDED(
    9,
    "command processing error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Number of input connections have exceeded",
    "Either increase the max input connections or shutdown some connected clients"
  ),

  MAX_OUTPUT_CONN_EXCEEDED(
    10,
    "max output connection exceeded",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Number of output connections exceeded",
    "Age out output connections in order to add a new connection"
  ),

  INVALID_CONN_TYPE_OR_ID(
    11,
    "invalid connection type or id",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Connection type or id is bad",
    "Either specify right connection id or close and recreate the connection"
  ),

  INVALID_CONNECTION_STATE(
    12,
    "invalid connection state",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Connection state of id is bad",
    "Either specify right connection state or close and recreate the connection"
  ),

  INVALID_OPER_ON_OUTPUT_CONN(
    13,
    "invalid operation on output connection",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Operation code on the output socket is incorrect",
    "Specify the correct operation code"
  ),

  INVALID_OPER_ON_INPUT_CONN(
    14,
    "invalid operation on input connection",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Operation code read on the input socket has an error",
    "Specify the correct operation code"
  ),

  INVALID_SCHEMA_SPEC(
    15,
    "invalid schema specification",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Schema specification on the server is incorrect",
    "Specify the correct schema"
  ),

  FAILED_NETWORK_READ(
    16,
    "failed network read",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Network read error on socket id {0}",
    "Requeue the read and if that fails again, disconnect and reconnect"
  ),

  FAILED_NETWORK_WRITE(
    17,
    "failed network write",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Network write error on socket id {0}",
    "Disconnect from the socket and re-connect with a new socket id"
  ),

  NETWORK_TUPLE_QUEUE_FULL(
    18,
    "network tuple queue full",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Input message queue is full",
    "Wait for the input queue to be drained out"
  ),

  INVALID_TUPLE_VALUE_ERROR(
    19,
    "invalid tuple value error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Tuple value is not as per the format",
    "Specify the correct value for this tuple"
  ),

  QUEUE_FULL_ERROR(
    20,
    "queue full error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "Output message queue is full",
    "Wait for the output queue to be drained out"
  ),


  INTERNAL_ERROR( 
    21,
    "network internal error",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "This is a generic network internal error",
    "This is a generic network internal error"
  );

  private ErrorDescription ed;

  NetworkError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Network + num, text, type, level,
        isDocumented, cause, action, "NetworkError");
  }

  public ErrorDescription getErrorDescription() {
    return ed;
  }
}
