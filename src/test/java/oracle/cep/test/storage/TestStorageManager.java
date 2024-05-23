/* $Header: TestStorageManager.java 18-mar-2008.12:03:49 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/18/08 - reorg config
    najain      03/08/07 - cleanup
    hopark      03/06/07 - Creation
 */

/**
 *  @version $Header: TestStorageManager.java 18-mar-2008.12:03:49 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package

oracle.cep.test.storage;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.storage.StorageManager;

public class TestStorageManager extends TestCase
{
  public TestStorageManager(String name)
  {
      super(name);
  }
  
  public void testAdd() throws CEPException
  {
    StorageManager sm = new StorageManager();
    ConfigManager config = new ConfigManager();
    config.setSpillStorageName("MemStorage");
    config.setMetadataStorageName("SFileStorage");
    sm.init(config);
  }
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestStorageManager.class);
  }
}

