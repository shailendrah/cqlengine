/* $Header: PhySynPos.java 29-nov-2007.11:09:26 parujain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Declares the Physical Synopsis Position in package oracle.cep.phyplan

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/29/07 - External synopsis
    rkomurav    09/12/06 - Position of Synopsis for displaying the plan in the
                           visualiser
    rkomurav    09/12/06 - Creation
 */

/**
 *  @version $Header: PhySynPos.java 29-nov-2007.11:09:26 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

public enum PhySynPos {
  
  CENTER("center"),OUTPUT("output"),RIGHT("right"),LEFT("left");
   
  private String name;
   
  /**
   * Constructor
   * @param name the meaningful name of Synopsis Position
   */
  PhySynPos (String name) {
    this.name = name;
  }
   
  /**
   * Get the meaningful name of the position
   */
  public String getName() {
    return this.name;
  }
 
}