/* $Header: TestStoredDoublyList.java 20-mar-2007.15:45:31 najain Exp $ */

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
    najain      03/06/07 - bug fix
    najain      03/02/07 - 
    hopark      01/10/07 - Creation
 */

/**
 *  @version $Header: TestStoredDoublyList.java 20-mar-2007.15:45:31 najain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.dataStructures;

import oracle.cep.exceptions.CEPException;
import oracle.cep.test.storage.TestStorageBase;

public class TestStoredDoublyList extends TestDoublyList1
{
  public TestStoredDoublyList()
  {
    super();
  }

  protected void setUpSys()
  {
    TestStorageBase.setUpStorage(true);
  }
  
  public static void main(String[] args)
  {
      junit.textui.TestRunner.run(TestStoredDoublyList.class);
  }
}
