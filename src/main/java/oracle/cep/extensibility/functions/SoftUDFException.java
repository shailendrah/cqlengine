/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/SoftUDFException.java /main/1 2013/10/16 07:04:05 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/15/13 - Creation
 */

package oracle.cep.extensibility.functions;

import oracle.cep.exceptions.UDFError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/SoftUDFException.java /main/1 2013/10/16 07:04:05 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SoftUDFException extends UDFException
{
  // Generated serial version UID
  private static final long serialVersionUID = 7223977029788096764L;

  public SoftUDFException(UDFError err) {
    super(err);
  }

  public SoftUDFException(UDFError err, Object ... args) {
    super(err, args);
  }
  
  public SoftUDFException(UDFError err, Throwable cause) {
    super(err, cause);
  }
  
  public SoftUDFException(UDFError err, Throwable cause, Object ... args) {
    super(err, cause, args);
  }  
}
