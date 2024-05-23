/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/java/S3.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       06/22/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/java/S3.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.java;

public class S3
{
  public int c1;
  public int e2;
  public int e3;
  
  public S3(int c1, int e2, int e3)
  {
    this.c1 = c1;
    this.e2 = e2;
    this.e3 = e3;
  }
  
  // Call it getMe to intentionally 
  // not be Java bean compliant
  public int getMeC1()
  {
   return this.c1; 
  }
  
  public int getMeE2()
  {
   return this.e2; 
  }
  
  public int getMeE3()
  {
   return this.e3; 
  }
}
