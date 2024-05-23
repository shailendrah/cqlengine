/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/OuterJoinType.java /main/2 2009/12/29 20:26:09 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of Outer Join Types

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    11/07/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/OuterJoinType.java /main/2 2009/12/29 20:26:09 parujain Exp $
 *  @author  rkomurav
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of Join types
 *
 * @since 1.0
 */

public enum OuterJoinType {
  LEFT_OUTER("LEFT OUTER JOIN"), RIGHT_OUTER("RIGHT OUTER JOIN"), FULL_OUTER("FULL OUTER JOIN");
  
  private String name;
  
  /**
   * Constructor
   * @param name the meaningful name of the join type
   */
  OuterJoinType(String name) {
    this.name = name;
  }
  
  /**
   * Get the meaningfule name of the join type
   */
  public String getOuterJoinType() {
    return this.name;
  }
  
  public OuterJoinType clonedummy() throws CloneNotSupportedException {
    OuterJoinType op = (OuterJoinType) super.clone();
    return op;
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append("(" + name + ")");
    return xml.toString();
  }
  
}
