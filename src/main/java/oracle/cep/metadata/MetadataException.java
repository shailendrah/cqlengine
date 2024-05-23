/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/MetadataException.java /main/2 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/05/08 - 
    skaluska    03/15/06 - 
    najain      02/14/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/MetadataException.java /main/2 2008/09/17 15:19:47 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.CEPException;

/**
 * Exception class for Metadata exceptions. 
 * This is needed to store context information for metadata.
 *
 */
public class MetadataException extends CEPException {
  static final long serialVersionUID = 1;
  
  // TODO: Currently, no context is needed. It will be added on demand.
  // Appropriate constructors will be added.
	
  public MetadataException(MetadataError err) {
    super(err);
  }

  public MetadataException(MetadataError err, int start, int end, Object[] args)
  {
    super(err, start, end, args);
  }
  
  public MetadataException(MetadataError err, Object[] args) {
    super(err, args);
  }
  
  public MetadataException(MetadataError err, Throwable cause) {
	super(err, cause);
  }
  
  public MetadataException(MetadataError err, Throwable cause, 
		       int start, int end, Object[] args) {
     super(err, cause, start, end, args);
  } 

  public MetadataException(MetadataError err, Throwable cause, Object[] args) {
	super(err, cause, args);
  }  
}
