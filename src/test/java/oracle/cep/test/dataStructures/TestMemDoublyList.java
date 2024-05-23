/* $Header: TestMemDoublyList.java 20-mar-2007.15:45:29 najain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      03/12/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: TestMemDoublyList.java 20-mar-2007.15:45:29 najain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.dataStructures;

import oracle.cep.test.storage.TestStorageBase;

public class TestMemDoublyList extends TestDoublyList1
{
  public TestMemDoublyList()
  {
    super();
  }

  protected void setUpSys()
  {
    TestStorageBase.setUpStorage(false);
  }
  
 
  public static void main(String[] args)
  {
      junit.textui.TestRunner.run(TestMemDoublyList.class);
  }
}
