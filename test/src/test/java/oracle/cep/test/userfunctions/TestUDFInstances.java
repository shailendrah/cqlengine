/* $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TestUDFInstances.java /main/1 2009/02/12 03:52:39 alealves Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
   alealves	02/10/09 - creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TestUDFInstances.java /main/1 2009/02/12 03:52:39 alealves Exp $
 *  @author  alealves
 */
package oracle.cep.test.userfunctions;

import junit.framework.TestCase;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.test.InterpDrv;

public class TestUDFInstances extends TestCase
{
    InterpDrv m_driver = null;
    
    public TestUDFInstances(String name)
    {
        super(name);
    }

    /**
     * Sets up the test fixture.
     * (Called before every test case method.)
     */
    public void setUp()
    {
        m_driver = InterpDrv.getInstance();
        try {
            if (!m_driver.setUp(null)) 
              fail();
        } catch (Exception e) 
        {
            System.out.println(e);
        }
        
        // Further initalization
    }

    /**
     * Tears down the test fixture.
     * (Called after every test case method.)
     */
    public void tearDown()
    {
        // Destroy previous run status..
        m_driver.tearDown();
        m_driver = null;
    }

    // Cannot use cqlx, because I need to check if there was an exception for
    // each query.
    private static InterpDrv.CqlDesc s_cqls[] =
    { 
      new InterpDrv.FuncDesc("register function f1 (c1 integer) return integer as language java instance \"f1\""),
      new InterpDrv.FuncDesc("register function f2 (c1 integer) return integer aggregate using instance \"f2\"")
    };


    public void testNotFoundUserFunctionLocator() throws CEPException
    {
        m_driver.setCql(s_cqls);

        int errcount = 0;
        for (InterpDrv.CqlDesc cql: s_cqls)
        {
            Exception e;
            e = cql.getException();
            if (e instanceof CEPException) {
                CEPException cepe = (CEPException) e;
                if (cepe.getErrorCode() == InterfaceError.USERFUNC_LOCATOR_NOT_SUPPORTED_IN_THIS_ENVIRONMENT)
                    errcount++;
            }
        } 
        assertEquals(2, errcount);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestUDFInstances.class);
    }
}

