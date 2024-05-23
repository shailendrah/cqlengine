/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/junit_test/java/com/oracle/cep/cartridge/java/impl/FooClass.java /main/1 2011/03/17 10:20:06 alealves Exp $ */

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
    alealves    Mar 8, 2011 - Creation
 */

/**
 *  @version $Header$
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

public class FooClass
{
  public String myProp1;  // in - field
  
  private String myProp2; // in - proper property
  
  private String myProp3; // in - (only has getter)
  
  String myProp4; // out - not public
  
  String myProp5; // in - overloades 'myProp6'
  
  public String myProp6;  // out - overloaded by 'myProp5'
  
  private String myProp7; // in - (only has setter)
  
  private boolean myProp8; // in - (boolean property)
  
  private String myProp9; // out
  
  private String [] myProp10; // out - (we don't support indexed properties)
  
  // in - class property

  public String getMyProp2()
  {
    return myProp2;
  }

  public void setMyProp2(String myProp2)
  {
    this.myProp2 = myProp2;
  }

  public String getMyProp3()
  {
    return myProp3;
  }

  String getMyProp4()
  {
    return myProp4;
  }

  void setMyProp4(String myProp4)
  {
    this.myProp4 = myProp4;
  }

  public String getMyProp6()
  {
    return myProp5;
  }

  public void setMyProp6(String myProp5)
  {
    this.myProp5 = myProp5;
  }

  public void setMyProp7(String myProp7)
  {
    this.myProp7 = myProp7;
  }

  public boolean isMyProp8()
  {
    return myProp8;
  }
  
  public void outMyProp9(String myProp9)
  {
    this.myProp9 = myProp9;
  }

  public String getMyProp10(int index)
  {
    return myProp10[index];
  }

  public void setMyProp10(int index, String myProp10)
  {
    this.myProp10[index] = myProp10;
  }
  
}
