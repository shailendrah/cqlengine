/* $Header: pcbpel/cep/test/src/oracle/cep/test/parser/TestInterval.java /main/1 2009/02/17 17:42:53 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Unit test for interval parsing.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/05/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/parser/TestInterval.java /main/1 2009/02/17 17:42:53 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.parser;

import junit.framework.TestCase;

import oracle.cep.exceptions.CEPException;
import com.oracle.osa.exceptions.ErrorCode;
import oracle.cep.exceptions.ParserError;
import oracle.cep.parser.CEPIntervalConstExprNode;

public class TestInterval extends TestCase
{
    public TestInterval(String name)
    {
        super(name);
    }

    /**
     * Sets up the test fixture.
     * (Called before every test case method.)
     */
    public void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     * (Called after every test case method.)
     */
    public void tearDown()
    {
    }

    // Cannot use cqlx, because I need to check if there was an exception for
    // each query.
    private static class TestDesc
    {
      String interval;
      ErrorCode  errCode;
      long   expectedVal;
      
      TestDesc(String d, long ev, ErrorCode ec)
      {
        interval = d;
        expectedVal = ev;
        errCode = ec;
      }
    };
    
    private static TestDesc s_tests[] = 
    {
      new TestDesc("6 1:03:45.100", 522225100, null),
      new TestDesc("6 1.03.45.100", 522225100, null),
      new TestDesc("6 1:03:45", 522225000, null),
      new TestDesc("6 1:03", 522180000, null),
      new TestDesc("6 1", 522000000, null),
      new TestDesc("6", 518400000, null),
      new TestDesc("6 1:03:45 100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("01:03:45.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("01:03:45 100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("0 24:03:45.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("0 0:61:45.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("-1 0:03:45.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("-1 0:03:61.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("-1 0:03:-1.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("-1 0:03:45.-1", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("-1 0:03:45.99999", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("ab 0:03:45.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("6 1:ab:45.100", 0, ParserError.INVALID_INTERVAL_FORMAT),
      new TestDesc("6 1:03:45.ab", 0, ParserError.INVALID_INTERVAL_FORMAT),
    };

    public void testInterval() throws CEPException
    {
        for (TestDesc td : s_tests)
        {
            CEPIntervalConstExprNode parser = new CEPIntervalConstExprNode(td.interval);
            try
            {
              Long v = parser.getValue();
              System.out.println(td.interval + " " + v.longValue());
              assertNull(td.errCode);
              //assertEquals(v.longValue(), td.expectedVal);
            } catch (Throwable e)
            {
              if (e instanceof CEPException)
              {
                CEPException cepe = (CEPException) e;
                assertEquals(cepe.getErrorCode(), td.errCode);
              }
              else
              {
                System.out.println(e);
                fail("Unknown exception " + e.toString());
              }
            }
        } 
    }
}

