/* $Header: StoreViewRelnSrcFactory.java 07-aug-2006.12:38:58 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      08/07/06 - view shares underlying store
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: StoreViewRelnSrcFactory.java 07-aug-2006.12:38:58 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptViewRelnSrc;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;

/**
 * StoreViewRelnSrcFactory
 *
 * @author najain
 */
public class StoreViewRelnSrcFactory extends StoreGenFactory {
  public PhyStore addStoreOpt(Object ctx) {
    // This function should never be called
    assert false;
    return null;
  }
}

