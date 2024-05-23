/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/exceptions/ExternalException.java /main/1 2010/06/24 06:26:52 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    06/21/10 - Creation
 */
package oracle.cep.extensibility.exceptions;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/exceptions/ExternalException.java /main/1 2010/06/24 06:26:52 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public abstract class ExternalException extends Exception
{
  private static final long serialVersionUID = -1871181089430204048L;

  /**
   * Get the error message
   */
  public abstract String getMessage();
  
  /**
   * Get the action message
   */
  public abstract String getAction();
 
  /**
   * Get the error cause
   */
  public abstract String getErrorCause(); 


  /**
   * Future set of methods once we decide up on the exposed set of APIs
   * public abstract ExternalErrorCode getErrorCode();
   * ...
   */
  
}
