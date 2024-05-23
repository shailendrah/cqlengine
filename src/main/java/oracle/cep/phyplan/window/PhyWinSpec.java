/* $Header: PhyWinSpec.java 07-mar-2007.16:33:00 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/07/07 - Window spec
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: PhyWinSpec.java 07-mar-2007.16:33:00 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window;

public abstract class PhyWinSpec {
  
  protected WinKind windowKind;
  
  public abstract boolean equals(Object o);
  
  public abstract String toString();
  
  public abstract String getXMLPlan2();
  
  public void setWindowKind(WinKind kind)
  {
    this.windowKind = kind;
  }
  
  public WinKind getWindowKind()
  {
    return windowKind;
  }
  
}
