/* $Header: pcbpel/cep/server/src/oracle/cep/parser/SyntaxException.java /main/2 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/05/08 - offset
    parujain    08/21/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/SyntaxException.java /main/2 2008/09/17 15:19:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SyntaxError;

public class SyntaxException extends CEPException
{
  static final long serialVersionUID = 1;

  public SyntaxException(SyntaxError err, int start, int end, String errorMsg) {
    super(err, start, end, new Object[]{errorMsg});
  }

  public SyntaxException(SyntaxError err, int start, int end, Object[] args) {
    super(err, start, end, args);
  }

  public SyntaxException(SyntaxError err, Throwable cause) {
    super(err, cause);
  }

  public SyntaxException(SyntaxError err, Throwable cause,
      Object[] args) {
    super(err, cause, args);
  }
}
