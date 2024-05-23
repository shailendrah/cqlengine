/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/SemanticException.java /main/2 2008/09/17 15:19:46 parujain Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    08/21/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/SemanticException.java /main/2 2008/09/17 15:19:46 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

public class SemanticException extends CEPException
{
  static final long serialVersionUID = 1;

  public SemanticException(SemanticError err, int start, int end) {
    super(err, start, end);
  }
  
  public SemanticException(SemanticError err)
  {
    super(err);
  }

  public SemanticException(SemanticError err, int start, int end, Object[] args) {
    super(err, start, end, args);
  }
  
  public SemanticException(SemanticError err, Object[] args)
  {
    super(err, args);
  }

  public SemanticException(SemanticError err, Throwable cause) {
    super(err, cause);
  }
  
  public SemanticException(SemanticError err, Throwable cause, int start, int end)
  {
    super(err, cause, start, end);
  }

  public SemanticException(SemanticError err, Throwable cause,
      Object[] args) {
    super(err, cause, args);
  }

}
