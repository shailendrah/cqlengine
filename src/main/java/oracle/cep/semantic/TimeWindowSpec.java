/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TimeWindowSpec.java /main/6 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Post semantic analysis representation of a window expresssion using a
    time specification

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/16/11 - support for variable duration range window
    parujain    08/26/08 - semantic exception offset
    sbishnoi    07/25/08 - support of nanosecond; changing various comments
    udeshmuk    02/05/08 - parameterize error.
    parujain    03/13/07 - slide less than range
    rkomurav    08/09/06 - slide and cleanup
    anasrini    06/05/06 - for NOW window rangeUnits should be 1 not 0 
    najain      05/31/06 - support for Now Window
    skaluska    04/04/06 - time is in msec 
    anasrini    03/23/06 - handling UNBOUNDED 
    anasrini    02/27/06 - fix xml closing in toString 
    anasrini    02/26/06 - implement toString 
    anasrini    02/21/06 - Add constructors for the special range units 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/TimeWindowSpec.java /main/5 2008/09/17 15:19:46 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.TimeUnit;
import oracle.cep.common.WindowType;
import oracle.cep.common.SplRangeType;
import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * Post semantic analysis representation of a window expresssion using a
 * time specification
 *
 * @since 1.0
 */

public class TimeWindowSpec implements WindowSpec {

  /** rangeUnits in nano-seconds */
  private long rangeUnits;

  /** slideUnits in nano-seconds */
  private long slideUnits;

  /** Is it an unbounded spec */
  private boolean isUnbounded = false;

  /** Is it an NOW spec */
  private boolean isNow = false;
  
  /** range expression */
  private Expr rangeExpr;
  
  /** Is it a variable duration window */
  private boolean isVariableDurationWindow = false;
  
  /** unit of range value */
  private TimeUnit rangeUnit;

  /**
   * Constructor for a "normal" RANGE specification
   * @param rangeUnits the range units in nano-seconds
   * @param slideUnits the slide units in nano-seconds
   */
  public TimeWindowSpec(long rangeUnits, long slideUnits)
  throws CEPException{
    this.rangeUnits = rangeUnits;
    this.slideUnits = slideUnits;
    if (this.rangeUnits == 0) {
      this.rangeUnits = 1;
      isNow = true;
    }
    if(this.slideUnits > this.rangeUnits)
      throw new SemanticException(SemanticError.SLIDE_GREATER_THAN_RANGE,
                             new Object[]{this.slideUnits, this.rangeUnits});
  }
  
  /**
   * Constructor for a normal RANGE specification with variable duration
   * @param rangeExpr the rangeExpr will evaluate to the number of range units
   * @param slideUnits the slide units in nanoseconds
   */
  public TimeWindowSpec(Expr rangeExpr, long slideUnits)
  {
    this.rangeExpr = rangeExpr;
    this.slideUnits = slideUnits;
    this.isVariableDurationWindow = true;
  }

  /**
   * Constructor for "special" RANGE specifications
   * @param splType the "special" RANGE specification
   */
  public TimeWindowSpec(SplRangeType splType) {
    switch(splType) {
    case UNBOUNDED:
      rangeUnits  = Constants.INFINITE;
      isUnbounded = true;
      break;
    case NOW:
      rangeUnits = 1;
      isNow = true;
      break;
    default:
      assert false;
    }
    this.slideUnits = 1;
  }

  public WindowType getWindowType() {
    if (isNow)
      return WindowType.NOW;
    return WindowType.RANGE;
  }

  /**
   * Get the number of range units in nanoseconds
   * @return the number of range units in nanoseconds
   */
  public long getRangeUnits() {
    return rangeUnits;
  }

  /**
   * Get the number of slide units in nanoseconds
   * @return the number of slide units in nanoseconds
   */
  public long getSlideUnits() {
    return slideUnits;
  }

  /**
   * Is this an UNBOUNDED window specification
   * @return true if and only if this is equivalent to an UNBOUNDED
   *         window specification
   */
  public boolean isUnboundedSpec() {
    return isUnbounded;
  }

  // toString
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (isUnboundedSpec()) {
      sb.append("<RangeSpec>");
      sb.append("<IsUnbounded/>");
    }
    else {
      if(isVariableDurationWindow)
        sb.append("<RangeSpec rangeUnits= Variable>");
      else
        sb.append("<RangeSpec rangeUnits=\"" + rangeUnits + "\" >");
    }
    sb.append("</RangeSpec>");
    sb.append("<SlideSpec slideUnits=\"" + slideUnits + "\" ></SlideSpec>");
    
    return sb.toString();
  }

  /**
   * Get the Range expression
   * @return rangeExpr which will evaluate to the number of range units
   */
  public Expr getRangeExpr()
  {
    return rangeExpr;
  }

  /**
   * Check if the windows is variable duration window
   * @return true if the window is a variable duration window
   */
  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }

  /**
   * @param rangeUnit the rangeUnit to set
   */
  public void setRangeUnit(TimeUnit rangeUnit)
  {
    this.rangeUnit = rangeUnit;
  }

  /**
   * @return the rangeUnit
   */
  public TimeUnit getRangeUnit()
  {
    return rangeUnit;
  }
}
