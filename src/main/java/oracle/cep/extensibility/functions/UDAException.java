/* $Header: UDAException.java 17-jul-2006.03:45:00 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Exception class for user defined aggregation exceptions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    07/17/06 - Creation
 */

/**
 *  @version $Header: UDAException.java 17-jul-2006.03:45:00 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.extensibility.functions;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.UDAError;

/**
 * Exception class for user defined aggregation exceptions
 *
 * @since 1.0
 */

public class UDAException extends CEPException {
  static final long serialVersionUID = 1;

  public UDAException(UDAError err) {
    super(err);
  }

  public UDAException(UDAError err, Object[] args) {
    super(err, args);
  }
  
  public UDAException(UDAError err, Throwable cause) {
    super(err, cause);
  }
  
  public UDAException(UDAError err, Throwable cause, Object[] args) {
    super(err, cause, args);
  }  
}

