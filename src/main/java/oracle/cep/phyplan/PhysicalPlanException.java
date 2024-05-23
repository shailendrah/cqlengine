/* $Header: PhysicalPlanException.java 08-mar-2006.10:41:44 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 najain      02/20/06 - Creation
 */

/**
 *  @version $Header: PhysicalPlanException.java 08-mar-2006.10:41:44 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.PhysicalPlanError;
import oracle.cep.exceptions.CEPException;

/**
 * Exception class for Physical plan exceptions. 
 * This is needed to store context information for physical plan.
 *
 */
public class PhysicalPlanException extends CEPException {
  static final long serialVersionUID = 1;

  // TODO: Currently, no context is needed. It will be added on demand.
  // Appropriate constructors will be added.

  public PhysicalPlanException(PhysicalPlanError err) {
    super(err);
  }

  public PhysicalPlanException(PhysicalPlanError err, Object[] args) {
    super(err, args);
  }

  public PhysicalPlanException(PhysicalPlanError err, Throwable cause) {
    super(err, cause);
  }

  public PhysicalPlanException(PhysicalPlanError err, Throwable cause,
      Object[] args) {
    super(err, cause, args);
  }
}
