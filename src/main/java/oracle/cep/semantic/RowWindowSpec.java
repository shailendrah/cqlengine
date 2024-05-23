/* $Header: RowWindowSpec.java 17-jun-2008.10:49:37 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Post semantic analysis representation of a window expression using a
    rows specification

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/17/08 - slide support
    anasrini    02/26/06 - implement toString 
    anasrini    02/24/06 - add javadoc comments 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: RowWindowSpec.java 17-jun-2008.10:49:37 parujain Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.WindowType;

/**
 * Post semantic analysis representation of a window expression using a
 * rows specification
 *
 * @since 1.0
 */

public class RowWindowSpec implements WindowSpec {

  private int numRows;
  
  private int slide;

  /**
   * Constructor for a ROW window specification
   * @param numRows the number of rows
   * @param slide the slide
   */
  public RowWindowSpec(int numRows, int slide) {
    this.numRows = numRows;
    this.slide = slide;
  }

  public WindowType getWindowType() {
    return WindowType.ROW;
  }

  /**
   * Get the number of rows
   * @return the number of rows
   */
  public int getNumRows() {
    return numRows;
  }
  
  /**
   * Get the slide
   * @return the slide
   */
  public int getSlide() {
    return slide;
  }

  // toString
  public String toString() {
    return "<RowSpec numRows=\"" + numRows + "\"" + " slide=\"" + slide + "\" />";
  }
}
