/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/standalone/TestStandalone.java /main/1 2011/05/19 15:28:45 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/01/11 - Creation
 */

/**
 *  @version $Header: TestStandalone.java 01-mar-2011.12:54:25 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.standalone;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.env.standalone.CQLEngine;
import oracle.cep.env.standalone.EnvConfig;
import oracle.cep.exceptions.CEPException;
import junit.framework.TestCase;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.server.CEPServer;

public class TestStandalone extends TestCase 
{
	EnvConfig m_config;
	CQLEngine m_engine;
	List<TupleValue> m_results;
	
	private class QueryOutputListener extends QueryOutputBase
	{
		public 	QueryOutputListener()
		{
			super(null);
		}
		
		@Override
		public void start() throws CEPException {
			m_results = new LinkedList<TupleValue>();
		}
		
		@Override
		public void end() throws CEPException {
		}

		@Override
		public void putNext(TupleValue tuple, Kind k) throws CEPException {
			if (k == Kind.E_PLUS)
			{
				m_results.add(tuple);
		        System.out.println("+"+tuple.toSimpleString());
			}
		}

	}
	
	public TestStandalone(String name) 
	{
		super(name);
	}

	/**
	 * Sets up the test fixture. (Called before every test case method.)
	 */
	public void setUp() 
	{
		m_engine = null;
		m_config = new EnvConfig();
		try
		{
			m_engine = new CQLEngine(m_config);
			m_engine.registerQueryOutput("test", new QueryOutputListener());
		}
		catch(Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Tears down the test fixture. (Called after every test case method.)
	 */
	public void tearDown() 
	{
		m_engine.close();
	}

	public void testHello() {
		assertTrue(m_engine != null);
		try
		{
		  m_engine.registerQueryOutput("test", new QueryOutputListener());
		  CEPServer server = m_engine.getServer();
	      String schema = "test";
	      String streamName = "S";
	      server.executeDDL("register stream " + streamName + " (c1 integer, c2 integer)", schema);
	      server.executeDDL("alter stream " + streamName + " add source push", schema);

	      server.executeDDL("create query q as select * from " + streamName + " [NOW]", schema);
	      String dest = "<EndPointReference><Address><Type>java</Type><Id>test</Id></Address></EndPointReference>";
	      server.executeDDL("alter query q add destination \""+dest+"\"", schema);
	      server.executeDDL("alter query q start", schema);
	      int n = 10;
	      for (int i = 0; i < n; i++)
	      {
	        long tm = System.currentTimeMillis();
	        AttributeValue[] attrval = new AttributeValue[2];
	        attrval[0] = new IntAttributeValue("c1", i);
	        attrval[1] = new IntAttributeValue("c2", i * 2);
	        TupleValue tv = new TupleValue(streamName, tm, attrval, false );
	        System.out.println(tv.toSimpleString());
	        server.executeDML(tv, schema);
	      }
	      assertTrue(m_results != null);
	      System.out.println("results=" + m_results.size());
	      assertTrue(m_results.size() == n);
	   }
		catch(Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestStandalone.class);
	}
}