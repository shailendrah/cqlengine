/* $Header: RowRangeWindow.java 25-oct-2007.14:45:25 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/12/07 - Creation
 */

/**
 *  @version $Header: RowRangeWindow.java 25-oct-2007.14:45:25 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.windows;

import oracle.cep.phyplan.window.PhyRowRangeWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;

public class RowRangeWindow extends RngWindow
{
  /** rowSize */
  private int   rowSize;
  
  public RowRangeWindow(PhyWinSpec spec)
  {
    super(spec);

    assert spec instanceof PhyRowRangeWinSpec;
    PhyRowRangeWinSpec partn = (PhyRowRangeWinSpec)spec;
    
    rowSize = partn.getRows();
  }
  
  public int getWindowRows() {return rowSize;}
  
}

