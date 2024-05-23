/* $Header: TimeDuration.java 12-mar-2007.16:15:13 najain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares TimeDuration in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    najain    03/12/07 - bug fix
    skaluska  02/17/06 - Creation
    skaluska  02/17/06 - Creation
 */

/**
 *  @version $Header: TimeDuration.java 12-mar-2007.16:15:13 najain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

/**
 * Internal representation for time duration.
 *
 * @author skaluska
 */
public class TimeDuration {
  /** The value for timestamp represented as a number */
  private long value;

  /**
   * Constructor for TimeDuration
   * @param value Value for the time duration
   */
  public TimeDuration(long value) {
    this.value = value;
  }

  /**
   * Getter for value in TimeDuration
   * @return Returns the value
   */
  public long getValue()
  {
    return value;
  }

  /**
   * Setter for value in TimeDuration
   * @param value The value to set.
   */
  public void setValue(long value)
  {
    this.value = value;
  }
}
