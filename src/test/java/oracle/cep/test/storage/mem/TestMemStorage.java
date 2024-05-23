/* $Header: pcbpel/cep/test/src/oracle/cep/test/storage/mem/TestMemStorage.java /main/1 2008/09/23 19:03:01 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/16/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/storage/mem/TestMemStorage.java /main/1 2008/09/23 19:03:01 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.storage.mem;

import oracle.cep.test.storage.TestStorageBase;

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/storage/mem/TestMemStorage.java /main/1 2008/09/23 19:03:01 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class TestMemStorage extends TestStorageBase
{
    public TestMemStorage() throws Exception
    {
      super("MemStorage");
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestMemStorage.class);
    }
}
