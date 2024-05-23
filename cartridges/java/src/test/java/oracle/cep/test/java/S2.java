/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/java/S2.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/java/S2.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.java;

public class S2
{
  public int c1;
  public int d2;
  public int d3;
  
  public S2(int c1, int d2, int d3)
  {
    this.c1 = c1;
    this.d2 = d2;
    this.d3 = d3;
  }
  
  // Call it getMe to intentionally 
  // not be Java bean compliant
  public int getMeC1()
  {
   return this.c1; 
  }
  
  public int getMeD1()
  {
   return this.d2; 
  }
  
  public int getMeD2()
  {
   return this.d3; 
  }
}
