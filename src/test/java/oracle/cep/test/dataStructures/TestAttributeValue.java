/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestAttributeValue.java /main/9 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      02/16/10 - correct setValue implementation
    hopark      01/31/08 - fix timestamp diff
    udeshmuk    01/17/08 - change in the data type of time field in
                           TupleValue.java
    parujain    12/13/07 - interval converter
    najain      03/12/07 - bug fix
    hopark      03/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestAttributeValue.java /main/8 2010/02/25 04:17:04 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.dataStructures;

import java.sql.Timestamp;

import java.text.ParseException;

import junit.framework.TestCase;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.ByteAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.IntervalAttributeValue;
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.exceptions.CEPException;


public class TestAttributeValue extends TestCase
{

  public TestAttributeValue()
  {
    super();
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

  private void checkVGet(AttributeValue attr)
  {
    try {
      int vv = 555;
      attr.iValueSet(vv);
      int v = attr.iValueGet();
      assertEquals(vv, v);
    } catch(CEPException e) 
    {
    }
    try {
      long vv = 555;
      attr.lValueSet(vv);
      long v = attr.lValueGet();
      assertEquals(vv, v);
    } catch(CEPException e) 
    {
    }
    try {
      float vv = 555;
      attr.fValueSet(vv);
      float v = attr.lValueGet();
      assertEquals(vv, v);
    } catch(CEPException e) 
    {
    }
    try {
      attr.vValueSet("1 12:00:30:0");
    } catch(CEPException e) {}
    try {
      attr.tValueSet(123456);
    } catch(CEPException e) 
    {
    }    
    try {
      String t = "hello";
      char[] vv = new char[t.length()];
      t.getChars(0, t.length(), vv, 0);
      attr.cValueSet(vv);
    } catch(CEPException e) 
    {
    }
    try {
      String t = "hello";
      char[] vv = new char[t.length()];
      t.getChars(0, t.length(), vv, 0);
      attr.cLengthSet(1);
    } catch(CEPException e) 
    {
    }
      try {
        String t = "hello";
        byte[] vv = t.getBytes();
        attr.bValueSet(vv);
      } catch(CEPException e) 
      {
      }
      try {
        String t = "hello";
        byte[] vv = t.getBytes();
        attr.bValueSet(vv);
        attr.bLengthSet(1);
      } catch(CEPException e) 
      {
      }

    try {
      int v = attr.iValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      long v = attr.lValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      float v = attr.lValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      long v = attr.tValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      String v = attr.vValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      long v = attr.intervalValGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      char[] v = attr.cValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      int v = attr.cLengthGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    } 
    try {
      byte[] v = attr.bValueGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    }
    try {
      int v = attr.bLengthGet();
      System.out.println(v);
    } catch(CEPException e) 
    {
    } 
  }
  
  protected void checkDump(AttributeValue attr)
  {
    String dump = attr.toString();
    assertNotNull(dump);

    attr.setBNull(true);
    String dump2 = attr.toString();
    assertNotNull(dump2);
  }

  public void testIntAttr()
  {
    IntAttributeValue attr = new IntAttributeValue("test");  
    IntAttributeValue attr1 = new IntAttributeValue("test1", 1234567);
    checkVGet(attr1);
    checkDump(attr1);
  }

  public void testFloatAttr()
  {
    FloatAttributeValue attr = new FloatAttributeValue("test");  
    FloatAttributeValue attr1 = new FloatAttributeValue("test1", 1234567.123f);
    checkVGet(attr1);
    checkDump(attr1);
  }
    
  public void testBigintAttr()
  {
    BigintAttributeValue attr = new BigintAttributeValue("test");
    BigintAttributeValue attr1 = new BigintAttributeValue("test1", 1234567);
    checkVGet(attr1);
    checkDump(attr1);
  }
  
  public void testTimestampAttr()
  {
    TimestampAttributeValue attr = new TimestampAttributeValue("test");
    long ts = 12345678;
    TimestampAttributeValue attr1 = new TimestampAttributeValue("test1", ts);
    attr1.setTime(12345678);
    
    long ts1 = attr1.getTime();
    assertEquals(ts, ts1);
    
    checkVGet(attr1);
    checkDump(attr1);
  }  

  public void testIntervalAttr()
  {
    IntervalAttributeValue attr = new IntervalAttributeValue("test");
    String istr = "1 2:10:20:30";
    IntervalAttributeValue attr1 = null;
    long iv = 0;
    try {
      attr1 = new IntervalAttributeValue("test1", istr);
      iv = attr1.intervalValGet();
      attr1.setInterval(istr);
      attr1.setInterval(iv);
    } 
    catch(CEPException e) {
    }
    String v = attr1.getInterval();
    
    checkVGet(attr1);
    checkDump(attr1);
    
    try {
      attr1 = new IntervalAttributeValue("test1", "1 2:10:20");
      iv = attr1.intervalValGet();
    
      attr1 = new IntervalAttributeValue("test1", "1 2:10");
      iv = attr1.intervalValGet();
   
      attr1 = new IntervalAttributeValue("test1", "1 2"); 
      iv = attr1.intervalValGet();
    
      attr1 = new IntervalAttributeValue("test1", " 1"); 
      iv = attr1.intervalValGet();
    }
    catch(CEPException e) {
    }
  }  
  
  public void testByteAttr()
  {
    String t= "hello";
    byte[] val = t.getBytes();
    ByteAttributeValue attr = new ByteAttributeValue("test", val);
    int len = attr.getLength();
    assertEquals(t.length(), len);
    byte[] v = attr.getValue();
    assertNotNull(v);
    
    String t1= "hello1";
    byte[] val1 = t1.getBytes();
    ByteAttributeValue attr1 = new ByteAttributeValue("test", val1);
    try
    {
      attr1.bLengthSet(t1.length());
      int len1 = attr1.getLength();
      assertEquals(t1.length(), len1);
      attr1.bValueSet(val1);
      byte[] val2 = attr1.getValue();
      assertEquals(val1, val2);
    }
    catch(Exception e)
    { 
    }
      
      checkVGet(attr1);
      checkDump(attr1);
    }

  public void testCharAttr()
  {
    CharAttributeValue attr0 = new CharAttributeValue("test");
    CharAttributeValue attr01 = new CharAttributeValue("test", null);
    String t= "hello";
    char[] val = new char[t.length()];
    t.getChars(0, t.length(), val, 0);
    CharAttributeValue attr = new CharAttributeValue("test", val);
    int len = attr.getLength();
    assertEquals(t.length(), len);
    char[] v = attr.getValue();
    assertNotNull(v);
    
    String t1= "hello1";
    char[] val1 = new char[t1.length()];
    t1.getChars(0, t1.length(), val1, 0);
    CharAttributeValue attr1 = new CharAttributeValue("test", val1);
    try
    {
      attr1.cLengthSet(t1.length());
    }
    catch(Exception e)
    {}
    int len1 = attr1.getLength();
    assertEquals(t1.length(), len1);
    try
    {
      attr1.cValueSet(val1);
    }
    catch(Exception e)
    {}
    char[] val2 = attr1.getValue();
    assertEquals(val1, val2);
    
    checkVGet(attr1);
    checkDump(attr1);
  }
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestAttributeValue.class);
  }
}
