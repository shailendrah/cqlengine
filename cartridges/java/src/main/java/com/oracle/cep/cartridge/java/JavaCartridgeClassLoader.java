/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/JavaCartridgeClassLoader.java /main/2 2010/02/18 08:25:48 alealves Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/JavaCartridgeClassLoader.java /main/2 2010/02/18 08:25:48 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java;

/**
 * A Java class loader for the Java Cartridge
 * 
 * It is application-specific, as the application potentially provides the container for class-loading.
 * 
 * @author Alex Alves
 *
 */
public interface JavaCartridgeClassLoader
{
  String CLASS_SPACE_HEADER = "OCEP-Java-Cartridge-Class-Space";
  
  String APPLICATION_CLASS_SPACE = "APPLICATION_CLASS_SPACE";
  
  String APPLICATION_NO_AUTO_IMPORT_CLASS_SPACE = "APPLICATION_NO_AUTO_IMPORT_CLASS_SPACE";
  
  /**
   * @exclude
   */  
  String SERVER_CLASS_SPACE = "SERVER_CLASS_SPACE";
  
  /**
   * Loads class <code>javaTypeName</code> from application <code>applicationName</code>
   * 
   * @param javaTypeName 
   * @param applicationName
   * @return
   * @throws ClassNotFoundException if class cannot be found
   * @throws MultipleClassesFoundException if multiple classes are found for javaTypeName
   * @throws IllegalArgumentException if either arguments are null
   * @throws IllegalStateException if application cannot be found.
   * 
   */
  Class<?> loadClass(String javaTypeName, String applicationName) 
    throws ClassNotFoundException, MultipleClassesFoundException;
  
}
