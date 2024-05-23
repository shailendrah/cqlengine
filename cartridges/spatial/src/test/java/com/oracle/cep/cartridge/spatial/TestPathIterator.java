/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/TestPathIterator.java /main/2 2015/10/01 22:29:48 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/TestPathIterator.java /main/2 2015/10/01 22:29:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class TestPathIterator extends TestCase
{
	public void testPathIterator1()
	{
		double[] coords = new double[] { -122.46333357679525,37.74252712206515, -122.45749709007868,37.689567812596195}; 
		double step = 500;
		verifyPathIterator(coords, step, 12);
	}

	public void testPathIterator3()
	{
		double[] coords = new double[] { -122.46666650987119,37.75002592155564,-122.43628244667028,37.72518301468021,-122.45241861582782,37.702640708694595,-122.43508081705218,37.69177449794587 }; 
		double step = 500; 
		verifyPathIterator(coords, step, 18);
	}
		
	public void testPathIterator5()
	{
		double[] coords = new double[] { -122.47173052040468,37.68532197759119,-122.47001390666453,37.692521646691034,-122.47104387490863,37.69646079241454,-122.47155885903065,37.70162211486162,-122.47121553628264,37.70664726774964,-122.46761064742829,37.709770839720996,-122.45885591735346,37.71044985968516,-122.45422106025501,37.7115362786864,-122.44975786453058,37.71547441404279,-122.44821291216444,37.71859761394095,-122.44786958941641,37.72185646476395,-122.44666795979829,37.724979395606006,-122.44323473231796,37.72823796566669,-122.43756990697543,37.73054603271353,-122.43327837262503,37.732039449528074,-122.42881517690059,37.73231097662202,-122.42521028804623,37.73163215702024,-122.41920213995564,37.73190368560772,-122.41576891247531,37.732039449528074,-122.41302233049106,37.732718265395995,-122.40684252102645,37.735569224084685 }; 
		double step = 10; 
		verifyPathIterator(coords, step, 975);
	}
	
	@SuppressWarnings("unchecked")
	public void testCreatePath()
	{
		String json = 
				"{\"type\": \"FeatureCollection\", \"features\":  ["+
						"   { \"type\": \"Feature\", "+
						"     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [-122.45409844683863,37.7400264562131,263.69138951479243]}"+
						"     ,\"properties\": {\"id\":100, \"lng\":-13631527.884651467, \"lat\":4542765.0873976, \"radius\":263.69138951479243}"+
						"   }"+
						",   { \"type\": \"Feature\", "+
						"     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [-122.43143914546845,37.722648525745726,249.07629801564423]}"+
						"     ,\"properties\": {\"id\":101, \"lng\":-13629005.462761208, \"lat\":4540319.102534318, \"radius\":249.07629801564423}"+
						"   }"+
						",   { \"type\": \"Feature\", "+
						"     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [-122.43676064806297,37.70377256066792,299.09255124803354]}"+
						"     ,\"properties\": {\"id\":102, \"lng\":-13629597.849720284, \"lat\":4537662.915846849, \"radius\":299.09255124803354}"+
						"   }"+
						",   { \"type\": \"Feature\", "+
						"     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [-122.41959451066133,37.689374708347245,528.8320018476011]}"+
						"     ,\"properties\": {\"id\":103, \"lng\":-13627686.924045846, \"lat\":4535637.334631944, \"radius\":528.8320018476011}"+
						"   }"+
						"] }";		
		verifyCreatePath(json);
	}

	@SuppressWarnings("unchecked")
	public void testCreatePath2()
	{
		String json = 
				"{\"type\": \"FeatureCollection\", \"features\":  ["+
						"   { \"type\": \"Feature\", "+
						"     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [-122.45409844683863,37.7400264562131,263.69138951479243]}"+
						"     ,\"properties\": {\"id\":100, \"lng\":-13631527.884651467, \"lat\":4542765.0873976, \"radius\":263.69138951479243}"+
						"   }"+
						"] }";			
		verifyCreatePath(json);
	}

	private void verifyPathIterator(double[] coords, double step, int expectedN)
	{
		Geometry path = Geometry.createLinearLineString(8307, coords);
		PathIterator iter = new PathIterator(path, step);
		int n = 0;
		while(iter.hasNext())
		{
			Geometry p = iter.next();
			double[] pt = p.getPoint();
			//System.out.println(pt[0] +","+pt[1]);
			//System.out.println(p.toCsvString());
			n++;
		}
		//System.out.println(n);
		assertEquals("Number of seg", n, expectedN);
	}

	@SuppressWarnings("unchecked")
	private void verifyCreatePath(String json)
	{
		List<Object> objs = JsonUtil.parseJson(json);
		List<Geometry> geoms = new ArrayList<Geometry>();
		int srid = 8307;
		int dimension = 2;
		for (Object obj : objs)
		{
			Map<String, Object> m = (Map<String, Object>) obj;
			Object gv = m.get("geometry");
			Geometry geom=null;
			if (gv instanceof Geometry)
				geom = (Geometry) gv;
			else if (gv instanceof LinkedHashMap)
				geom = JsonUtil.fromMap(srid, dimension, (LinkedHashMap<String, Object>) gv);
			geoms.add(geom);
		}
		Geometry path = PathIterator.createPath(geoms, 0);
		System.out.println(path.toJsonString());
	}
}
