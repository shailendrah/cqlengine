/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PartnWindowSpec.java /main/7 2011/12/15 01:06:31 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Post semantic analysis representation of a window expresssion using a
    partition specification

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/27/11 - support of variable duration partition window
    sborah      12/16/08 - handle constants
    parujain    09/08/08 - support offset
    sbishnoi    07/25/08 - support for nanosecond; changing variable names
    udeshmuk    02/05/08 - parameterize errors.
    hopark      12/15/06 - add range
    ayalaman    07/31/06 - add implementation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/PartnWindowSpec.java /main/6 2009/02/23 06:47:36 sborah Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.common.TimeUnit;
import oracle.cep.common.WindowType;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.CEPException;

/**
 * Post semantic analysis representation of a window expresssion using a
 * partition specification
 *
 * @since 1.0
 */

public class PartnWindowSpec implements WindowSpec {

  private ArrayList<Attr> pbyAttrs;     // partition by attributes
  private int             numPbyAttrs;  // number of partition by attrs
  private int             numRows;      // num of rows in each partition

  /** rangeUnits in nano-seconds */
  private long rangeUnits;

  /** slideUnits in nano-seconds */
  private long slideUnits;
  
  /** range expression */
  private Expr rangeExpr;
  
  /** unit of range value */
  private TimeUnit rangeUnit;

  /** Is it a variable duration window */
  private boolean isVariableDurationWindow = false;
  
  /**
   *  Constructor for the partition window specification 
   */
  public PartnWindowSpec()
  {
    pbyAttrs = new ExpandableArray<Attr>(Constants.INITIAL_NUM_PARTN_ATTRS); 
    numRows = 0; 
    numPbyAttrs = 0; 
    rangeUnits = -1;
  }

  /**
   * Add an attribute to the PARTITION BY attribute list
   *
   * @param  attr   attribute to be added 
   * 
   * @throws CEPException if the limit on the number of attributes in the
   *                      PARTITION BY clause is exceeded
   */
  public void addPartnByAttr(Attr attr) throws CEPException
  {
    pbyAttrs.add(attr); 
    numPbyAttrs++; 
  }

  /**
   * Set the number of rows in each partition 
   * 
   * @param numRows     number of rows in each partition  
   */
  public void setNumRows(int numRows)
  {
    this.numRows = numRows; 
  }

  /**
   * Set the range units
   * 
   * @param rangeUnits     range units in ns 
   */
  public void setRangeUnits(long rangeUnits)
  {
    this.rangeUnits = rangeUnits; 
  }

  /**
   * Set the slide units
   * 
   * @param slideUnits     slide units in ns
   */
  public void setSlideUnits(long slideUnits)
  {
    this.slideUnits = slideUnits; 
  }
  
  /**
   * Get the window type specification 
   * 
   * @return   Constant for the PARTITION window type 
   */
  public WindowType getWindowType()
  {
    return WindowType.PARTITION;
  }

  /**
   * Get the number of rows in each partition 
   *
   * @return  the number of rows 
   */
  public int getNumRows()
  {
    return numRows;
  }

  /**
   * Get the range units in nano-seconds
   * @return the range units in nano-seconds
   */
  public long getRangeUnits() {
    return rangeUnits;
  }

  /**
   * Get the number of slide units in nano-seconds
   * @return the number of slide units in nano-seconds
   */
  public long getSlideUnits() {
    return slideUnits;
  }
  
  public boolean hasRange() 
  {
      return rangeUnits >= 0;
  }

  /**
   * Get the attributes on which the partitions are created
   *
   * @return  an array of the attributes 
   */
  public Attr[] getPartnAttrs() 
  {
    // return a copy of the attriute instead of a reference to the actual list
    Attr[] retarr = new Attr[numPbyAttrs]; 
     
    pbyAttrs.toArray(retarr);
    return retarr; 
  }

  /**
   * @return the rangeExpr
   */
  public Expr getRangeExpr()
  {
    return rangeExpr;
  }

  /**
   * @param rangeExpr the rangeExpr to set
   */
  public void setRangeExpr(Expr rangeExpr)
  {
    this.rangeExpr = rangeExpr;
  }

  /**
   * @return the rangeUnit
   */
  public TimeUnit getRangeUnit()
  {
    return rangeUnit;
  }

  /**
   * @param rangeUnit the rangeUnit to set
   */
  public void setRangeUnit(TimeUnit rangeUnit)
  {
    this.rangeUnit = rangeUnit;
  }

  /**
   * @return the isVariableDurationWindow
   */
  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }

  /**
   * @param isVariableDurationWindow the isVariableDurationWindow to set
   */
  public void setVariableDurationWindow(boolean isVariableDurationWindow)
  {
    this.isVariableDurationWindow = isVariableDurationWindow;
  }
}
