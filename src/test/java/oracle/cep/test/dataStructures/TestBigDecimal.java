/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/dataStructures/TestBigDecimal.java /main/1 2011/05/19 15:28:45 hopark Exp $ */

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import junit.framework.TestCase;

import oracle.cep.dataStructures.external.BigDecimalAttributeValue;


public class TestBigDecimal extends TestCase
{

  public TestBigDecimal()
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

  private void verify(BigDecimalAttributeValue v, BigDecimal bdVal) throws Exception
  {
	  ByteArrayOutputStream fos = null;
	  ObjectOutputStream out = null;
	  fos = new ByteArrayOutputStream();
	  out = new ObjectOutputStream(fos);
	  out.writeObject(v);
	  out.close();
	  ByteArrayInputStream fis = null;
	  ObjectInputStream in = null;
	  byte[] buf = fos.toByteArray();
	  fis = new ByteArrayInputStream(buf);
	  in = new ObjectInputStream(fis);
	  BigDecimalAttributeValue newv = (BigDecimalAttributeValue) in.readObject();
	  in.close();
	  BigDecimal newbd = newv.nValueGet();
	  System.out.println(bdVal.toString() + "," + newbd.toString());
	  assertTrue(bdVal.equals(newbd));
  }
  
  public void testBigDecimal()
  {
	  BigDecimal bdVal = new BigDecimal("3.141592");
	  BigDecimalAttributeValue v = new BigDecimalAttributeValue(bdVal);
	  try
	  {
		  verify(v, bdVal);
	  }
	  catch(Exception e)
	  {
		  System.out.println(e.toString());
		  assertTrue(false);
	  }
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestBigDecimal.class);
  }
}
