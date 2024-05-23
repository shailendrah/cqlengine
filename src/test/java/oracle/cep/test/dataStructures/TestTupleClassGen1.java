/* $Header: TestTupleClassGen1.java 08-sep-2007.08:52:10 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/31/07 - Creation
 */

/**
 *  @version $Header: TestTupleClassGen1.java 08-sep-2007.08:52:10 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.dataStructures;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestTupleClassGen1 extends TestTupleClassGen
{

  public TestTupleClassGen1(String name)
  {
    super(name);
  }
  
  protected String getBaseClassPath()
  {
    return "oracle.cep.dataStructures.internal.stored.DynTupleBase";
  }

  public static Test suite()
  {
    if (SINGLE_TEST)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestTupleClassGen1(SINGLE_TEST_NAME));
      return suite;
    } else {
      return new TestSuite(TestTupleClassGen1.class);
    }
  }
  
  public static final boolean SINGLE_TEST = false;
  public static final String SINGLE_TEST_NAME = "testTuple1";
  
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestTupleClassGen1.suite());
  }
}
