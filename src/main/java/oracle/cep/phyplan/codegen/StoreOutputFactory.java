/* $Header: StoreOutputFactory.java 22-mar-2006.12:15:46 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 najain      03/22/06 - Creation
 */

/**
 *  @version $Header: StoreOutputFactory.java 22-mar-2006.12:15:46 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyStore;

/**
 * StoreOutputFactory
 *
 * @author najain
 */
public class StoreOutputFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {
    // This function should never be called
    assert false;
    return null;
  }
}
