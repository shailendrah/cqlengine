/* $Header: TestSFileStorage.java 02-mar-2007.17:55:22 najain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      03/02/07 - 
    hopark      01/10/07 - Creation
 */

/**
 *  @version $Header: TestSFileStorage.java 02-mar-2007.17:55:22 najain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.storage.sfile;

import oracle.cep.test.storage.TestStorageBase;

/**
 *  @version $Header: TestSFileStorage.java 02-mar-2007.17:55:22 najain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class TestSFileStorage extends TestStorageBase
{
    public TestSFileStorage() throws Exception
    {
      super("SFileStorage");
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestSFileStorage.class);
    }
}
