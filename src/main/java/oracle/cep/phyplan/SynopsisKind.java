/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/SynopsisKind.java /main/6 2008/11/13 21:59:38 udeshmuk Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares SynopsisKind in package oracle.cep.phyplan.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    udeshmuk  11/05/08 - rename patternpartnwin
    udeshmuk  10/10/08 - add pattern specific type.
    parujain  11/16/07 - external source
    hopark    07/03/07 - add getDesc
    rkomurav  05/14/07 - classB
    rkomurav  09/11/06 - cleanup of xmldump
    rkomurav  08/24/06 - add getName
    skaluska  02/15/06 - Creation
    skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/SynopsisKind.java /main/6 2008/11/13 21:59:38 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

/**
 * @author skaluska
 *
 */
public enum SynopsisKind
{
  // Lineage synopsis
  LIN_SYN("lineage", "Lineage Synopsis"),
  
  // Partition window synopsis
  PARTN_WIN_SYN("partnwin", "Partition Window Synopsis"),

  // Private specific partition window synopsis
  PRIVATE_PARTN_WIN_SYN("privatepartnwin","Private Partition Window Synopsis"),
  
  // Relation synopsis
  REL_SYN("relation", "Relation Synopsis"),

  // Window Synopsis
  WIN_SYN("window", "Window Synopsis"),

  // Binding Synopsis
  BIND_SYN("binding", "Binding Synopsis"),

  // External Synopsis when we access external source
  EXT_SYN("external", "External Synopsis");
  
  private String name;
  private String desc;
  
  /**
   * Constructor
   * @param name the meaningful name of the operator
   */
  SynopsisKind(String name, String desc) {
    this.name = name;
    this.desc = desc;
  }
  //get the name of the Synopsis
  public String getName()
  {
    return name;
  }
  
  public String getDesc()
  {
    return desc;
  }
}
