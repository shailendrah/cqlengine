/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/TestJsonUtil.java /main/2 2015/10/01 22:29:47 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/15/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/TestJsonUtil.java /main/2 2015/10/01 22:29:47 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class TestJsonUtil extends TestCase
{
	private static final int srid = 8307;
	private static final int dimension = 2;
	
	public static class ExpectedMetadata 
	{
		Map<String, Integer> columns;
		public ExpectedMetadata()
		{
			columns = new HashMap<String, Integer>();
		}
		
		public void addColumn(String col)
		{
			columns.put(col, columns.size());
		}
		
		public int getNoColumns() { return columns.size();}
		public int getColumn(String col)
		{
			Integer v = columns.get(col);
			return v.intValue();
		}
		
		public boolean hasColumn(String col)
		{
			Integer v = columns.get(col);
			return v != null;
		}
	}
	
	public static class ExpectedRows 
	{
		List<Object[]> rows;
		public ExpectedRows()
		{
			rows = new ArrayList<Object[]>();
		}
		
		public void addRow(Object ... row)
		{
			rows.add(row);
		}

		public Object getNoRows() {
			return rows.size();
		}	
		public Object[] getRow(int n)
		{
			return rows.get(n);
		}
	}
	
	public void testGeoJsonFeatureCollection() throws Exception
	{
		String geojson = "{\"type\": \"FeatureCollection\", \"features\":  [\r\n   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\":\"Rectangle\",\"srid\":8307,\"coordinates\":[-13633568.7252231,4541034.200305872,-13632269.295764482,4542467.3945617005]}\r\n     ,\"properties\": {\"id\":0}\r\n   }\r\n,   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\":\"Polygon\",\"srid\":8307,\"coordinates\":[[-13632498.606845414,4539429.022739343,-13630071.731238877,4538951.291320734,-13630434.80711702,4537155.021186762,-13633511.397452867,4537728.298889093,-13632498.606845414,4539429.022739343]]}\r\n     ,\"properties\": {\"id\":1}\r\n   }\r\n] }";
		
		ExpectedMetadata expectedMeta = new ExpectedMetadata();
		expectedMeta.addColumn("geometry");
		expectedMeta.addColumn("id");
		
		ExpectedRows expectedRows = new ExpectedRows();
		expectedRows.addRow(Geometry.createRectangle(srid, -13633568.7252231,4541034.200305872,-13632269.295764482,4542467.3945617005), 0);
		expectedRows.addRow(Geometry.createLinearPolygon(srid, new double[] { -13632498.606845414,4539429.022739343,-13630071.731238877,4538951.291320734,-13630434.80711702,4537155.021186762,-13633511.397452867,4537728.298889093,-13632498.606845414,4539429.022739343 }), 1);

		verifyJson(geojson, expectedMeta, expectedRows);
	}	

	public void testGeoJsonFeatures() throws Exception
	{
		String geojson = "[\r\n   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [[-13633511.397452867,4542582.050102167],100]}\r\n     ,\"properties\": {\"id\":0, \"lng\":-13633511.397452867, \"lat\":4542582.050102167, \"radius\":100}\r\n   }\r\n,   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\": \"Circle\", \"srid\":8307, \"coordinates\": [[-13632899.901237046,4541855.89834588],682.366597351118]}\r\n     ,\"properties\": {\"id\":1, \"lng\":-13632899.901237046, \"lat\":4541855.89834588, \"radius\":682.366597351118}\r\n   }\r\n,   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\":\"Rectangle\",\"srid\":8307,\"coordinates\":[[-13632555.934615647,4539448.131996088],[-13630931.647792375,4541091.528076105]]}\r\n     ,\"properties\": {\"id\":2}\r\n   }\r\n,   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\":\"Polygon\",\"srid\":8307,\"coordinates\":[[[-13630721.445968186,4539123.274631433],[-13629116.268401658,4538855.745037012],[-13629326.470225846,4537842.954429559],[-13630721.445968186,4539123.274631433]]]}\r\n     ,\"properties\": {\"id\":3}\r\n   }\r\n,   { \"type\": \"Feature\", \r\n     \"geometry\": {\"type\":\"LineString\",\"srid\":8307,\"coordinates\":[[-13631543.144008195,4536925.710105829],[-13628523.881442582,4535798.26395791],[-13628218.133334672,4536639.071254663],[-13627988.82225374,4535626.280647211]]}\r\n     ,\"properties\": {\"id\":4}\r\n   }\r\n]";
		ExpectedMetadata expectedMeta = new ExpectedMetadata();
		expectedMeta.addColumn("geometry");
		expectedMeta.addColumn("id");
		expectedMeta.addColumn("lng");
		expectedMeta.addColumn("lat");
		expectedMeta.addColumn("radius");
		
		ExpectedRows expectedRows = new ExpectedRows();
		expectedRows.addRow(Geometry.createCircle(srid, -13633511.39745286, 4542582.050102167, 100.0), 0, -13633511.39745286, 4542582.050102167, 100 );
		expectedRows.addRow(Geometry.createCircle(srid, -13632899.901237046, 4541855.89834588, 682.366597351118), 1, -13632899.901237046, 4541855.89834588, 682.366597351118);
		expectedRows.addRow(Geometry.createRectangle(srid, -13632555.934615647,4539448.131996088,-13630931.647792375,4541091.528076105),  2 );
		expectedRows.addRow(Geometry.createLinearPolygon(srid, new double[] { -13630721.445968186,4539123.274631433,-13629116.268401658,4538855.745037012,-13629326.470225846,4537842.954429559,-13630721.445968186,4539123.274631433}), 3);
		expectedRows.addRow(Geometry.createLinearLineString(srid, new double[] { -13631543.144008195,4536925.710105829,-13628523.881442582,4535798.26395791,-13628218.133334672,4536639.071254663,-13627988.82225374,4535626.280647211}), 4);

		verifyJson(geojson, expectedMeta, expectedRows);
	}
	
	@SuppressWarnings("unchecked")
	private void verifyJson(String json, ExpectedMetadata expmeta, ExpectedRows exprows)
	{
		List<Object> objs = JsonUtil.parseJson(json);
		assertEquals("Number of rows should match", objs.size(), exprows.getNoRows() );
		int n = 0;
		for (Object obj : objs)
		{
			Object[] row = exprows.getRow(n);
			if (obj instanceof Map)
			{
				Map<String, Object> ev = ( Map<String, Object>) obj;
				JsonUtil.processFlattenIgnore(ev);
				if (n == 0)
				{
					assertEquals("Number of columns should match", ev.size(), expmeta.getNoColumns() );
				}
				for (String key : ev.keySet())
				{
					assertTrue("Key does not exist " + key, expmeta.hasColumn(key) );
					Object v = ev.get(key);
					if (key.equals("geometry"))
					{
						v = JsonUtil.fromMap(srid, dimension, (Map<String,Object>) v);
					}
					int pos = expmeta.getColumn(key);
					Object expv = row[pos];
					if (expv instanceof Geometry)
					{
						Geometry gv = (Geometry) v;
						Geometry expgv = (Geometry) expv;
						assertTrue("Val does not match\n"+gv.toString() +"\nexpected:" +expgv.toString(), gv.compareTo(expgv));
					}
					else if (expv instanceof Number)
						assertEquals("Val does not match" , ((Number)v).intValue(), ((Number)expv).intValue());
						
				}
			}
			else fail("Unknown json obj " + obj.getClass().toString());
			n++;
		}
	}
	
	public void testToJson()
	{
		double[] coords = new double[] {-106.87,45.86,-106.9,46.09,-106.23,45.99,-106.28,45.74,-106.87,45.86};
		Geometry g = Geometry.createLinearPolygon(8307, coords);
		String json = g.toJsonString();
		String expected = "{\"type\":\"Polygon\",\"srid\":\"8307\",\"coordinates\":[[-106.87,45.86],[-106.9,46.09],[-106.23,45.99],[-106.28,45.74],[-106.87,45.86]]}";
		assertEquals("json does not match ", json, expected);
	}

	public void testToJson3D()
	{
		double[] coords = new double[] {-106.87,45.86,1.0,-106.9,46.09,2.0,-106.23,45.99,3.0,-106.28,45.74,4.0,-106.87,45.86,5.0};
		Geometry3D g = Geometry3D.createLinearPolygon(8307, coords);
		String json = g.toJsonString();
		String expected = "{\"type\":\"Surface\",\"srid\":\"8307\",\"coordinates\":[[-106.87,45.86,1.0],[-106.9,46.09,2.0],[-106.23,45.99,3.0],[-106.28,45.74,4.0],[-106.87,45.86,5.0],[-106.87,45.86,1.0]]}";
		assertEquals("json does not match ", json, expected);
	}

	public void testToJsonMultiPolygon()
	{
	    double[][] coords = new double[2][];
	    coords[0] = new double[]{-122.47912642320475,37.67869968597003,-122.42367979939745,37.73954068140123};
	    coords[1] = new double[]{-122.4586987196968,37.70152091255539,-122.43947264580696,37.71768169859588};
	    Geometry g = Geometry.createLinearPolygon(8307, coords);
		String json = g.toJsonString();
		String expected = "{\"type\":\"MultiPolygon\",\"srid\":\"8307\",\"coordinates\":[ [[-122.47912642320475,37.67869968597003],[-122.42367979939745,37.73954068140123],[-122.47912642320475,37.67869968597003]], [[-122.4586987196968,37.70152091255539],[-122.43947264580696,37.71768169859588],[-122.4586987196968,37.70152091255539]] ]}";
		assertEquals("json does not match ", json, expected);
	}
	public void testPolyline()
	{
		double[] coords = new double[] {-120.2,38.5,-120.95,40.7,-126.453,43.252};
		String s = JsonUtil.encode(coords, 0, coords.length, 5);
		System.out.println(s);;
		String expected = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
		assertEquals("Polyline does not match", s, expected);
	}
	
	public void testPolyline1()
	{
		double[] coords = new double[] {-122.4629902540472,37.733295916130636,-122.41767165130686,37.73003756862968,-122.40393874138556,37.696087924187786,-122.45131728061409,37.67299328205161,-122.48805281465361,37.70450888644088,-122.4629902540472,37.733295916130636};
	    //Geometry g = Geometry.createLinearPolygon(8307, coords);
	    //System.out.println(g.toJsonString());;
		String s = JsonUtil.encode(coords, 0, coords.length-2, 5);
		String expected = "cxheFtpmjVjSgzGdsEytAjoCbgH_dEpdF";
		assertEquals("Polyline does not match", s, expected);
		
	}
}