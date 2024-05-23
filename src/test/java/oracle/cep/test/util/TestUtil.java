/* $Header: pcbpel/cep/test/src/oracle/cep/test/util/TestUtil.java /main/3 2009/05/01 16:16:48 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/util/TestUtil.java /main/3 2009/05/01 16:16:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.util;

import junit.framework.TestCase;

import java.util.List;

import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MemManagerError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.util.DebugUtil;

public class TestUtil extends TestCase
{
    public TestUtil(String name)
    {
        super(name);
    }

    public void testDebugUtil()
    {
      String stack = DebugUtil.getCurrentStackTrace();
      assertNotNull(stack);
    }
    
    public static void main(String[] args) {
      junit.textui.TestRunner.run(TestUtil.class);
    }

}

