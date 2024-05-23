/* $Header: StoreOrderByFactory.java 28-jun-2007.16:44:43 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/28/07 - Order by store factory
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: StoreOrderByFactory.java 28-jun-2007.16:44:43 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyStore;

public class StoreOrderByFactory extends StoreGenFactory {
  
  public PhyStore addStoreOpt(Object ctx) {
    // This function should never be called
    // since we are using the store allocated by the input operator
    assert false;
    return null;
  }
  
}
