/* $Header: TestBDBStorage.java 21-jun-2007.14:11:41 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/20/07 - cleanup
    hopark      06/13/07 - fix package
    parujain    03/22/07 - handle exception
    najain      03/02/07 - 
    hopark      01/10/07 - Creation
 */

/**
 *  @version $Header: TestBDBStorage.java 21-jun-2007.14:11:41 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.storage.BerkeleyDB;

import oracle.cep.test.storage.TestStorageBase;

/**
 *  @version $Header: TestBDBStorage.java 21-jun-2007.14:11:41 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class TestBDBStorage extends TestStorageBase
{
    public TestBDBStorage() throws Exception
    {
      super("BDBStorage");
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestBDBStorage.class);
    }
}
