/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogicalPlanException.java /main/2 2010/07/29 05:51:55 sbishnoi Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Exceptions along with additional context in the logical plan component.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    07/29/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main_11.1.1.4.0
                        from st_pcbpel_11.1.1.4.0
 sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
 sbishnoi    07/28/10 - modifying constructor signature
 najain      02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogicalPlanException.java /main/2 2010/07/29 05:51:55 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.exceptions.CEPException;

/**
 * Exception class for Logical plan exceptions. 
 * This is needed to store context information for logical plan.
 *
 */
public class LogicalPlanException extends CEPException {
  static final long serialVersionUID = 1;

  // TODO: Currently, no context is needed. It will be added on demand.
  // Appropriate constructors will be added.

  public LogicalPlanException(LogicalPlanError err) {
    super(err);
  }

  public LogicalPlanException(LogicalPlanError err, Object ... args) {
    super(err, args);
  }

  public LogicalPlanException(LogicalPlanError err, Throwable cause) {
    super(err, cause);
  }

  public LogicalPlanException(LogicalPlanError err, Throwable cause,
      Object[] args) {
    super(err, cause, args);
  }
}
