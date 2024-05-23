/* $Header: UDFException.java 11-feb-2008.22:40:47 sbishnoi Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/11/08 - error parameterization
    parujain    04/16/07 - User Defined Function Error
    parujain    04/16/07 - Creation
 */

/**
 *  @version $Header: UDFException.java 11-feb-2008.22:40:47 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.functions;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.UDFError;

public class UDFException extends CEPException {
  static final long serialVersionUID = 1;
  
  public UDFException(UDFError err) {
    super(err);
  }

  public UDFException(UDFError err, Object ... args) {
    super(err, args);
  }
  
  public UDFException(UDFError err, Throwable cause) {
    super(err, cause);
  }
  
  public UDFException(UDFError err, Throwable cause, Object ... args) {
    super(err, cause, args);
  }  
}
