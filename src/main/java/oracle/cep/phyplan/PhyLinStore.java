/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyLinStore.java /main/2 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    Physical layer representation of a a lineage store

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    najain      04/06/06 - cleanup
    skaluska    04/05/06 - compile
    anasrini    03/21/06 - Creation
    anasrini    03/21/06 - Creation
    anasrini    03/21/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyLinStore.java /main/2 2008/10/24 15:50:17 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan;

import oracle.cep.service.ExecContext;

/**
 * Physical layer representation of a a lineage store
 *
 * @since 1.0
 */

public class PhyLinStore extends PhyStore {

  /* Number of attributes in the composite key (the lineage) */
  private int numLineages;

  /**
   * Default Constructor
   * @param ec TODO
   */
  public PhyLinStore(ExecContext ec) {
    super(ec, PhyStoreKind.PHY_LIN_STORE);
  }
   

  // Getter methods

  /**
   * Get the number of attributes in the composite key (the lineage) 
   * @return the number of attributes in the composite key (the lineage) 
   */
  public int getNumLineages() {
    return numLineages;
  }

  // Setter methods

  /**
   * Set the number of attributes in the composite key (the lineage) 
   * @param numLins the number of attributes in the composite key 
   *               (the lineage) 
   */
  public void setNumLineages(int numLins) {
    this.numLineages = numLins;
  }
  
}

