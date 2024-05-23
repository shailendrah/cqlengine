/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/BarClass.java /main/1 2011/03/17 10:20:06 alealves Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      alealves  03/16/11 - use jb introspector, improve jb error msgs
    alealves    Mar 8, 2011 - Creation
 */

/**
 *  @version $Header$
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

public class BarClass extends Bar2Class
{
  private String childProp1; // in
  
  public String childProp2; // in
  
  public String getChildProp1()
  {
    return childProp1;
  }

  public void setChildProp1(String childProp1)
  {
    this.childProp1 = childProp1;
  }
  
}
