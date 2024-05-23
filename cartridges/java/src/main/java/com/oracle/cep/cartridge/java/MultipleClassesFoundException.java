/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/MultipleClassesFoundException.java /main/1 2010/02/18 08:25:49 alealves Exp $ */

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
    alealves    Feb 11, 2010 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/MultipleClassesFoundException.java /main/1 2010/02/18 08:25:49 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java;

/**
 * Raised when multiple classes are found for a java type name.
 * 
 * @author Alex Alves
 *
 */
public class MultipleClassesFoundException extends Exception
{
  private static final long serialVersionUID = -2360923460110951838L;
  private String javaTypeName;

  public MultipleClassesFoundException(String javaTypeName, String message)
  {
    super(message);
    this.javaTypeName = javaTypeName;
  }
  
  public String getJavaTypeName() 
  {
    return javaTypeName;
  }

}
