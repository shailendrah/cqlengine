/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/DirectClassLoader.java /main/1 2009/12/21 10:05:02 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    Dec 4, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/DirectClassLoader.java /main/1 2009/12/21 10:05:02 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import com.oracle.cep.cartridge.java.JavaCartridgeClassLoader;

public class DirectClassLoader implements JavaCartridgeClassLoader
{
  private static final String JAVA_LANG = "java.lang.";

  @Override
  public Class<?> loadClass(String javaTypeName, String applicationName)
      throws ClassNotFoundException
  {
    Class<?> javaClass = null;
    try
    {
      javaClass =
        getClass().getClassLoader().loadClass(javaTypeName);

    } catch (ClassNotFoundException e0)
    {
      // Let's try the default package.
      javaClass =
        getClass().getClassLoader().loadClass(JAVA_LANG + javaTypeName);
    }

    return javaClass;
  }

}
