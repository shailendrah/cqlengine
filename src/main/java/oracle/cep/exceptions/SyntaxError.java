/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/SyntaxError.java /main/3 2010/02/26 16:37:52 parujain Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    02/24/10 - add error for reserved words
    parujain    08/21/08 - syntax error
    parujain    08/21/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/exceptions/SyntaxError.java /main/3 2010/02/26 16:37:52 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

public enum SyntaxError implements ErrorCode {

  SYNTAX_ERROR(
    1,
    "generic syntax error",
    ErrorType.ERROR,
    1,
    true,
    "This DDL command has syntax error",
    "The syntax expects {0} token"
  ),
  FUNCTION_ID_ERROR(
      2,
      "function name error",
      ErrorType.ERROR,
      1,
      true,
      "Functions may only have a single identifier in their name",
      "Correct the function name"
  ),
  ATTR_ID_ERROR(
      3,
      "attribute name error",
      ErrorType.ERROR,
      1,
      true,
      "Attributes may not have more than two identifiers in their name",
      "Correct the attribute name"
  ),
  RESERVED_WORD_ERROR(
      4,
      "Syntax Error. Invalid usage of Reserved CQL Keyword: \"{0}\"",
      ErrorType.ERROR,
      1,
      true,
      "Invalid usage of Reserved CQL Keyword: \"{0}\"",
      "The syntax expects following tokens: \"{1}\""
  );
  
  private ErrorDescription ed;
  
  SyntaxError(int num, String text, ErrorType type, int level, boolean isDocumented,
              String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Core_Parser_Syntex + num, text, type, level,
        isDocumented, cause, action, "SyntaxError");
  }

  public ErrorDescription getErrorDescription()
  {
    return ed;
  }

}
