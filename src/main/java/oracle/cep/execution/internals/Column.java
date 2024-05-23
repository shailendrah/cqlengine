/* $Header: Column.java 12-mar-2007.16:15:09 najain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares Column in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 najain    03/12/07 - bug fix
 najain    03/17/06 - Getter
 skaluska  02/12/06 - Creation
 skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: Column.java 12-mar-2007.16:15:09 najain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

/**
 * Column
 *
 * @author skaluska
 */
public class Column {
  /** column number */
  int colnum;

  /**
   * Constructor for Column
   * @param colnum Column number
   */
  public Column(int colnum) {
    this.colnum = colnum;
  }

  /**
   * @return Returns the colnum.
   */
  public int getColnum() {
    return colnum;
  }
}
