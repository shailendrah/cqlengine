/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TestRTreeIndex.java /main/1 2009/12/30 21:49:27 hopark Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/17/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TestRTreeIndex.java /main/1 2009/12/30 21:49:27 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.spatial;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.rtreeindex.RTreeIndex;

import junit.framework.TestCase;

public class TestRTreeIndex extends TestCase
{
	RTreeIndex m_index;
	
  public TestRTreeIndex()
  {
    super();
  }

  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
	  m_index = new RTreeIndex();
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
  }

  public void testRemove()
  {
	  Geometry g1 = Geometry.createRectangle(0, 1, 1, 5, 7);
	  String val1 = "g1";
	  m_index.insert(g1, val1);
	  Geometry g2 = Geometry.createRectangle(0, 8, 8, 10, 20);
	  String val2 = "g2";
	  m_index.insert(g2, val2);
	  int cnt = m_index.getEntryCount();
	  assertEquals (cnt, 2);
	  
	  m_index.delete(g1, val1);
	  int cnt2 = m_index.getEntryCount();
	  assertEquals (cnt2, 1);
  }
    
  public void main(String[] args)
  {
      junit.textui.TestRunner.run(TestRTreeIndex.class);
  }
}
