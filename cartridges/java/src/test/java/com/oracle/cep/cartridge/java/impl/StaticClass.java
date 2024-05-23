/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/StaticClass.java /main/1 2011/03/17 10:20:06 alealves Exp $ */

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
    alealves    Mar 9, 2011 - Creation
 */

/**
 *  @version $Header$
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

public class StaticClass
{
  public static String staticProp;              // in - static field is supported

  public static String getMyProp1()             // out - static properties not supported
  {
    return staticProp;
  }

  public static void setMyProp1(String myProp1)
  {
    StaticClass.staticProp = myProp1;
  }
  
}
