/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyStoreKind.java /main/6 2008/11/13 21:59:38 udeshmuk Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares PhyStoreKind in package oracle.cep.phyplan.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  11/04/08 - renaming patternpartnwindow.
 udeshmuk  10/10/08 - add new store type.
 hopark    12/19/07 - add external
 hopark    07/03/07 - add getDesc
 rkomurav  05/14/07 - add classB
 rkomurav  09/11/06 - cleanup of xmldump
 rkomurav  08/23/06 - add getName
 najain    07/19/06 - ref-count tuples 
 najain    07/05/06 - cleanup
 najain    03/08/06 - beautify
 skaluska  02/15/06 - Creation
 skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyStoreKind.java /main/6 2008/11/13 21:59:38 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

/**
 * @author skaluska
 *
 */
public enum PhyStoreKind {
  PHY_LIN_STORE("lineage", "Lineage Store"), 
  PHY_PARTN_WIN_STORE("partnwin", "Partition Window Store"),
  PHY_REL_STORE("relation", "Relation Store"), 
  PHY_WIN_STORE("window", "Window Store"), 
  PHY_BIND_STORE("binding", "Binding Store"),
  PHY_EXT_STORE("external", "External Store"),
  PHY_PVT_PARTN_STORE("privatepartnwin","Private Partition Window Store");
  
  private String name;
  private String desc;
  
  /**
   * Constructor
   * @param name the meaningful expanded name of the operator
   */
  PhyStoreKind(String name, String desc) {
    this.name = name;
    this.desc = desc;
  }
  
  /**
   * Get the meaningful name of the operator.
   */
  public String getName()
  {
    return name;
  }

  
  public String getDesc()
  {
    return desc;
  }
}
