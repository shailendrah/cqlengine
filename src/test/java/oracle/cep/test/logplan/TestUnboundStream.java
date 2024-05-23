/* $Header: pcbpel/cep/test/src/oracle/cep/test/logplan/TestUnboundStream.java /main/5 2009/02/17 17:42:53 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Unit test for unbound semantic check
    It checks if unbound stream is used.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/02/07 - remove semicolon
    hopark      09/25/07 - fix dif
    parujain    03/22/07 - handle exception
    hopark      11/27/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/logplan/TestUnboundStream.java /main/5 2009/02/17 17:42:53 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.logplan;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.test.InterpDrv;

public class TestUnboundStream extends TestCase
{
    InterpDrv m_driver = null;
    
    public TestUnboundStream(String name)
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

    // Cannot use cqlx, because I need to check if there was an exception for
    // each query.
    private static InterpDrv.CqlDesc s_cqls[] =
    { 
      new InterpDrv.TableDesc("UnbS2", "register stream UnbS2 (c1 integer, c2 float)",
                              "inpS2.txt"),
      new InterpDrv.TableDesc("UnbS9", "register stream UnbS9 (c1 integer, c2 float)",
                              "inpS9.txt"),
      new InterpDrv.ViewDesc("register view UnbV1 as select c1 from UnbS2 [range 1000]"),
      new InterpDrv.ViewDesc("register view UnbV9 as select c1 from UnbS9 [range 1000]"),
      new InterpDrv.QueryDesc("create query qUnbsAgg0 as select c2, min(c1) from UnbS9 [range 1000] group by c2", "outUnbs-agg0.txt"),
      new InterpDrv.QueryDesc("create query qUnbsJoin0 as select * from UnbS2 [range 1000], UnbS9[range 1000]", "outUnbs-join0.txt"),
      new InterpDrv.QueryDesc("create query qUnbsExc0 as UnbV1  except UnbV9", "outUnbs-except0.txt"),
      new InterpDrv.QueryDesc("create query qUnbsAgg as select c2, min(c1) from UnbS9 group by c2", "outUnbs-agg.txt"),
      new InterpDrv.QueryDesc("create query qUnbsJoin as select * from UnbS2, UnbS9", "outUnbs-join.txt"),
      new InterpDrv.QueryDesc("create query qUnbsExc as UnbS2 except UnbS9", "outUnbs-except.txt"),
    };


    public void testUnboundStream() throws CEPException
    {
        m_driver.setCql(s_cqls);

        int errcount = 0;
        int erridx = -1;
        int idx = 0;
        for (InterpDrv.CqlDesc cql: s_cqls)
        {
            Exception e;
            e = cql.getException();
            if (e instanceof CEPException) {
                CEPException cepe = (CEPException) e;
                if (cepe.getErrorCode() == LogicalPlanError.UNBOUND_STREAM_NOT_ALLOWED)
                {
                    errcount++;
                    erridx = idx;
                }
            }
            idx++;
        } 
        assertEquals(8, erridx);
        assertEquals(1, errcount);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestUnboundStream.class);
    }
}

