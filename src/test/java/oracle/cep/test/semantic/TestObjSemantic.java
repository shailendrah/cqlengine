/* $Header: pcbpel/cep/test/src/oracle/cep/test/semantic/TestObjSemantic.java /main/1 2009/02/25 14:23:52 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Unit test for bigint semantic check
    It checks the data range of num rows in partition window and rows window.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
s    hopark      02/24/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/semantic/TestObjSemantic.java /main/1 2009/02/25 14:23:52 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.semantic;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.test.InterpDrv;

public class TestObjSemantic extends TestCase
{
    InterpDrv m_driver = null;
    
    public TestObjSemantic(String name)
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
            if (!m_driver.setUp(null)) fail();
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

    private static InterpDrv.CqlDesc s_cqls[] =
    { 
      new InterpDrv.TableDesc("SObj1", "register stream SObj1 (c1 object, c2 float)",
                              "inpSObj-l-f.txt"),
      new InterpDrv.TableDesc("SObj2", "register stream SObj2 (c1 object, c2 float)",
                              "inpSObj-l-f.txt"),
      new InterpDrv.QueryDesc("create query qObj1 as select * from SObj1 [rows 1], SObj2 [rows 2] where SObj1.c1 = SObj2.c1", "outSObj-q1.txt"),
      new InterpDrv.QueryDesc("create query qObj2 as select * from SObj1 [rows 1], SObj2 [rows 2] where SObj1.c1 < SObj2.c1", "outSObj-q2.txt"),
      new InterpDrv.QueryDesc("create query qObj3 as select * from SObj1 [rows 1], SObj2 [rows 2] where SObj1.c1 > SObj2.c1", "outSObj-q3.txt"),
      new InterpDrv.QueryDesc("create query qObj4 as select * from SObj1 [rows 1], SObj2 [rows 2] where SObj1.c1 != SObj2.c1", "outSObj-q1.txt"),
    };


    public void testObjEq() throws CEPException
    {
        m_driver.setCql(s_cqls);

        int errcount = 0;
        for (InterpDrv.CqlDesc cql: s_cqls)
        {
            if (!cql.isBSuccess()) 
            {
              Exception e = cql.getException();
              assertTrue(e instanceof CEPException);
              CEPException ce = (CEPException) e;
              assertEquals(ce.getErrorCode(), SemanticError.WRONG_NUMBER_OR_TYPES_OF_ARGUMENTS);
              errcount++;
            }
        } 
        assertEquals(4, errcount);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestObjSemantic.class);
    }
}

