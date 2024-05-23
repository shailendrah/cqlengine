/* $Header: SystemState.java 08-feb-2007.15:12:12 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    02/08/07 - System states
    parujain    02/08/07 - Creation
 */

/**
 *  @version $Header: SystemState.java 08-feb-2007.15:12:12 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

/**
 * Possible system states
 * @author parujain
 *
 */
public enum SystemState {
  
  /** System not yet seeded */
  ZERO,
  /** System seeding completed */
  SEEDING_COMPLETED,
  /** Scheduler is running */
  SCHEDULER_RUNNING,
  /** Scheduler has been stopped */
  SCHEDULER_STOPPED;
  
}
