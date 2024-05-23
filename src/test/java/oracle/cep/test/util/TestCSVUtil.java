/* $Header: TestCSVUtil.java 06-mar-2007.09:45:40 hopark   Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

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
 *  @version $Header: TestCSVUtil.java 06-mar-2007.09:45:40 hopark   Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.util;

import java.util.LinkedList;

import junit.framework.TestCase;

import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.util.CSVUtil;

public class TestCSVUtil extends TestCase
{
  public TestCSVUtil(String name)
  {
    super(name);
  }

  public void setUp() throws CEPException
  {
  }

  public void tearDown() throws CEPException
  {
  }

  public void testSingleRecordSingleField() throws CEPException
  {
    String line = "test";
    List columns = CSVUtil.parseStr(line);
    assertEquals(1, columns.size());
    assertEquals("test", columns.get(0));
  }

  public void testSingleFieldMultipleColumns() throws CEPException
  {
    String line = "test2col1,test2col2";
    List columns = CSVUtil.parseStr(line);
    assertEquals(2, columns.size());
    assertEquals("test2col1", columns.get(0));
    assertEquals("test2col2", columns.get(1));
  }

  public void testDoubleQuotedData() throws CEPException
  {
    String line = "100 Oracle Parkway,\"Redwood city, CA 94065\", US";
    List columns = CSVUtil.parseStr(line);
    assertEquals(3, columns.size());
    assertEquals("100 Oracle Parkway", columns.get(0));
    assertEquals("\"Redwood city, CA 94065\"", columns.get(1));
    assertEquals(" US", columns.get(2));
  }

  public void testMoreThanOneCommaInDoubleQuotedData() throws CEPException
  {
    String line = "100 Oracle Parkway,\"Laurel, MD 20707, US\"";
    List columns = CSVUtil.parseStr(line);
    assertEquals(2, columns.size());
    assertEquals("100 Oracle Parkway", columns.get(0));
    assertEquals("\"Laurel, MD 20707, US\"", columns.get(1));
  }

  public void testEmbeddedQuotesArePartOfString() throws CEPException
  {
    String line = "100 Oracle Parkway, Redwood \"City\" California, 94065";
    List columns = CSVUtil.parseStr(line);
    assertEquals(3, columns.size());
    assertEquals("100 Oracle Parkway", columns.get(0));
    assertEquals(" Redwood \"City\" California", columns.get(1));
    assertEquals(" 94065", columns.get(2));
  }

  public void testUnmatchedDoubleQuoteIsAnError() throws CEPException
  {
    String line = "\"jkl";
    try
    {
      List columns = CSVUtil.parseStr(line);
      fail("should have CEPException");
    } catch (CEPException e)
    {
    }
  }

  public void testEmptyFields() throws CEPException
  {
    String line = "";
    List columns = CSVUtil.parseStr(line);
    assertEquals(1, columns.size());

    line = ",";
    columns = CSVUtil.parseStr(line);
    assertEquals(2, columns.size());

    line = ",a,,,";
    columns = CSVUtil.parseStr(line);
    assertEquals(5, columns.size());
  }

  public void testNumbers() throws CEPException
  {
    String line = "101,102,103,104,560";
    List<Long> columns = CSVUtil.parseLong(line, true);
    assertEquals(5, columns.size());
    assertEquals(new Long(101), columns.get(0));
    assertEquals(new Long(102), columns.get(1));
    assertEquals(new Long(103), columns.get(2));
    assertEquals(new Long(104), columns.get(3));
    assertEquals(new Long(560), columns.get(4));
  }

  public void testNumberFormatError() throws CEPException
  {
    String line = "101,102,103,zyx,560 ";
    try
    {
      List<Long> columns = CSVUtil.parseLong(line, true);
      fail("should have CEPException");
    } catch (CEPException e)
    {
      pass();
    }
  }

  public void testConvertFromArray() throws CEPException
  {
    String strAr[] =
    { "1024", "ORA-600[kkk]", "11/4/03 3:30PM", "11/5/04 3:30PM" };
    String expect = "1024,ORA-600[kkk],11/4/03 3:30PM,11/5/04 3:30PM";
    String val = CSVUtil.fromArray(strAr);
    assertEquals(val, expect);
    Integer intAr[] =
    { new Integer(1024), new Integer(600), new Integer(11403),
      new Integer(110504) };
    String expect2 = "1024,600,11403,110504";
    val = CSVUtil.fromArray(intAr);
    assertEquals(val, expect2);
  }

  public void testGen()
  {
    LinkedList<Object> l = new LinkedList<Object>();
    l.add(new Integer(10));
    l.add(new String("ttt"));
    String r = CSVUtil.fromList(l);
    Object[] a =l.toArray();
    String r1 = CSVUtil.fromArray(a);
    assertEquals(r, r1);
  }
  
  void pass()
  {
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestCSVUtil.class);
  }

}


