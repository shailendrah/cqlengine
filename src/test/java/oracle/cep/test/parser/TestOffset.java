/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/parser/TestOffset.java /main/8 2010/02/18 08:25:52 alealves Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves	02/10/09 - change offset for uda
    hopark      08/26/08 - verify offset
    parujain    08/19/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/parser/TestOffset.java /main/8 2010/02/18 08:25:52 alealves Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.parser;

import junit.framework.TestCase;
import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.MetadataException;
import oracle.cep.parser.Parser;
import oracle.cep.parser.SyntaxException;
import oracle.cep.semantic.SemanticException;

public class TestOffset extends TestCase
{
  private Parser parser;

  public void setUp()
  {
    parser = new Parser();
  }

  public void tearDown()
  {
    parser = null;
  }

  private static class ErrDesc 
  {
    String s;
    int soffset;
    int eoffset;
    ErrDesc(String s, int soffset, int eoffset) 
    {
      this.s = s;
      this.soffset = soffset;
      this.eoffset = eoffset;
    }
  }

  private static ErrDesc[] ddls = {
    new ErrDesc("create stream S", 14, 15),
    new ErrDesc("create stream        S", 21, 22),
    new ErrDesc("alter stream S", 13,14),
    new ErrDesc("create stream S()", 15,16),
    new ErrDesc("create stream S(c1 int, c2 times)", 27,31),
    new ErrDesc("create view v1 as select from S ", 18,28),
    new ErrDesc("create query q1 as select c1, c2 from", 33,37),
    new ErrDesc("create query q2 as select c1, c2 from S where c1 >", 49,50),
    new ErrDesc("create query q3 as select * from S[rows]", 35,39),
    new ErrDesc("create query q4 as select * from S[partition by]", 45,47),
    new ErrDesc("create function var(c1 int) returns float aggregate using ", 26,34),
    new ErrDesc("create query tkpattern_q1 as select T.Ac1,T.Bc1,T.Dc1 from tkpattern_S0 MATCH_RECOGNIZE ( MEASURES A.c1 as Ac1, B.c1 as Bc1, D.c1 as Dc1 PATTERN(A B C-- D) DEFINE A as A.c1 = 30, B as B.c2 = 10.0, C as C.c1 = 7, D as D.c1 = 40) as T", 149,150)
  };

  public void testDDL()
  {
    
    for (ErrDesc desc: ddls)
    {
      boolean hasexception = false;
      try
      {
        parser.parseCommand(null, desc.s);
      }
      catch(Exception e)
      {
        // String errMsg = e.getMessage();
        if (e instanceof CEPException)
        {
          if(e instanceof SyntaxException)
          {
            System.out.println("SYNTAX EXCEPTION");
            SyntaxException se = (SyntaxException)e;
            System.out.println("STARTOFFSET :" + se.getStartOffset());
            System.out.println("ENDOFFSET :" +se.getEndOffset());
            System.out.println("ACTION :" +se.getAction());
            hasexception = true;
            assertEquals(desc.soffset, se.getStartOffset());
            assertEquals(desc.eoffset, se.getEndOffset());
          }
          else if(e instanceof SemanticException)
          {
            System.out.println("SEMANTIC EXCEPTION");
            SemanticException se = (SemanticException)e;
            System.out.println("STARTOFFSET :" + se.getStartOffset());
            System.out.println("ENDOFFSET :" +se.getEndOffset());
            System.out.println("ACTION :" +se.getAction());
            hasexception = true;
            assertEquals(desc.soffset, se.getStartOffset());
            assertEquals(desc.eoffset, se.getEndOffset());
          }
          else if(e instanceof MetadataException)
          {
            System.out.println("METADATA EXCEPTION");
            MetadataException se = (MetadataException) e;
            System.out.println("STARTOFFSET :" + se.getStartOffset());
            System.out.println("ENDOFFSET :" +se.getEndOffset());
            System.out.println("ACTION :" +se.getAction());
            hasexception = true;
            assertEquals(desc.soffset, se.getStartOffset());
            assertEquals(desc.eoffset, se.getEndOffset());
          }
          else
            System.out.println("Generic Parser Error = " + e + ", ddl = " + desc.s);
        }
      }
      assertTrue(hasexception);
    }
  } 
    
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestOffset.class);
  }
}
