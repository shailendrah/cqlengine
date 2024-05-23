/* $Header: TestBigintSemantic.java 02-nov-2007.12:45:14 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Unit test for bigint semantic check
    It checks the data range of num rows in partition window and rows window.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/02/07 - remove semicolon
    hopark      08/01/07 - fix bug
    parujain    03/22/07 - handle exception
    hopark      11/27/06 - Creation
 */

/**
 *  @version $Header: TestBigintSemantic.java 02-nov-2007.12:45:14 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.semantic;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.test.InterpDrv;

public class TestBigintSemantic extends TestCase
{
    InterpDrv m_driver = null;
    
    public TestBigintSemantic(String name)
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
      new InterpDrv.TableDesc("SBigInt", "register stream SBigInt (c1 bigint, c2 float)",
                              "inpSBigInt-l-f.txt"),
      new InterpDrv.QueryDesc("create query qBigInt2 as select * from SBigInt [rows 2147483647]", "outSBigInt-exc2.txt"),
      new InterpDrv.QueryDesc("create query qBigInt3 as select * from SBigInt [partition by c1 rows 2147483647]", "outSBigInt-exc3.txt"),
      new InterpDrv.QueryDesc("create query qBigIntE2 as select * from SBigInt [rows 2147483648]", "outSBigInt-exc2e.txt"),
      new InterpDrv.QueryDesc("create query qBigIntE3 as select * from SBigInt [partition by c1 rows 2147483648]", "outSBigInt-exc3e.txt"),
    };


    public void testBigint() throws CEPException
    {
        m_driver.setCql(s_cqls);

        int errcount = 0;
        for (InterpDrv.CqlDesc cql: s_cqls)
        {
            if (!cql.isBSuccess()) errcount++;
        } 
        assertEquals(2, errcount);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestBigintSemantic.class);
    }
}

