/* $Header: DerivedTimeSpec.java 26-mar-2008.12:58:12 mthatte Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     03/26/08 - 
    parujain    03/10/08 - Derived Timestamp
    parujain    03/10/08 - Creation
 */

/**
 *  @version $Header: DerivedTimeSpec.java 26-mar-2008.12:58:12 mthatte Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

public class DerivedTimeSpec {
	
  private int    tableId;
  
  private Expr   derivedTsExpr;
  
  public DerivedTimeSpec(int tblId, Expr tsExpr)
  {
    this.tableId = tblId;
    this.derivedTsExpr = tsExpr;
  }
  
  public int getTableId()
  {
    return this.tableId;
  }
  
  public Expr getDerivedTsExpr()
  {
    return this.derivedTsExpr;
  }
  
}