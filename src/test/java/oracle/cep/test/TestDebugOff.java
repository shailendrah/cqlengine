/* $Header: TestDebugOff.java 06-mar-2008.11:10:15 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
   This test is to make sure the debug flag is turned off.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/04/07 - Creation
 */

/**
 *  @version $Header: TestDebugOff.java 06-mar-2008.11:10:15 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test;

import junit.framework.TestCase;

import oracle.cep.util.DebugUtil;

public class TestDebugOff extends TestCase
{
    public TestDebugOff(String name)
    {
        super(name);
    }
    
    public void testOff()
    {
      boolean b = DebugUtil.isDebugModeOn();
      if (b)
      {
        System.out.println("***********************************");
        System.out.println("WARNING: Debug mode is turned on.");
        System.out.println("**********************************");
      }
      assertFalse(b);
    }

    public static void main(String[] args) {
      junit.textui.TestRunner.run(TestDebugOff.class);
    }
}
