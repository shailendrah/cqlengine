/* $Header: pcbpel/cep/test/src/oracle/cep/test/execution/xml/TestXMLSpill.java /main/3 2008/10/24 15:50:20 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      09/17/08 - support schema
    hopark      03/06/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/execution/xml/TestXMLSpill.java /main/3 2008/10/24 15:50:20 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.execution.xml;

import java.util.LinkedList;

import oracle.cep.execution.xml.XmlManager;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.cep.test.storage.TestStorageBase;
import junit.framework.Test;
import junit.framework.TestSuite;


public class TestXMLSpill extends TestXML
{
  public TestXMLSpill(String name)
  {
    super(name);
    TestStorageBase.setUpStorage(true);
    CEPManager cepMgr = CEPManager.getInstance();
    XmlManager xmlMgr = cepMgr.getSystemExecContext().getXmlMgr();
    xmlMgr.setMemMode(false);
  }

  public void test2() throws Exception
  {
    prepareTest2(itemstr0, true);
    CEPManager cepMgr = CEPManager.getInstance();
    IStorage storage = cepMgr.getStorageManager().getMetadataStorage();
    assert (storage != null);
    String ns = NameSpace.TXMLTUPLE.toString();
    storage.addNameSpace(null, ns, false, null, null, null);
    LinkedList<QryRes> ref = new LinkedList<QryRes>();
    //copy all
    int i;
    for (i = 0; i < N_QUERIES; i++)
    {
      Qry q = queries[i];
      for (QryRes r : q.res)
      {
        ref.add(r.copy());
      }
    }
        //serialize all
    int pos = 0;
    for (i = 0; i < N_QUERIES; i++)
    {
      Qry q = queries[i];
      for (QryRes r : q.res)
      {
        boolean b = storage.putRecord(null, ns, null, new Integer(pos), r);
        assertTrue(b);
        pos++;
      }
    }
    
    for (i = 0; i < ref.size(); i++)
    {
      QryRes r = (QryRes)  storage.getRecord(null, ns, new Integer(i));
      assertTrue (r != null);
      QryRes s = ref.get(i);
      
      assertTrue(s.equals(r));
    }
  }
  
  public static Test suite()
  {
    if (SINGLE_TEST_NAME != null)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestXMLSpill(SINGLE_TEST_NAME));
      return suite;
    } else {
      return new TestSuite(TestXMLSpill.class);
    }
  }
  
  public static final String SINGLE_TEST_NAME = "test2";
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestXMLSpill.suite());
  }
 }
  