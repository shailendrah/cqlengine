/* $Header: TestMemPgDoublyList.java 18-mar-2008.11:48:43 hopark Exp $ */

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
    hopark      12/05/07 - 
    najain      03/12/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: TestMemPgDoublyList.java 18-mar-2008.11:48:43 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.dataStructures.pagedList;

import java.util.Properties;

import oracle.cep.common.Constants;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.test.dataStructures.TestDoublyList1;
import oracle.cep.test.storage.TestStorageBase;

public class TestMemPgDoublyList extends TestDoublyList1
{
  public TestMemPgDoublyList()
  {
    super();
  }

  protected void setUpSys()
  {
    ConfigManager config = new ConfigManager();
    config.setUsePagedList(true);
    TestStorageBase.setUpStorage(false, config);
  }
  
 
  public static void main(String[] args)
  {
      junit.textui.TestRunner.run(TestMemPgDoublyList.class);
  }
}
