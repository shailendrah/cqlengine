/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/TestGeomGenerator.java /main/3 2015/10/01 22:29:52 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/01/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/junit_test/java/com/oracle/cep/cartridge/spatial/TestGeomGenerator.java /main/3 2015/10/01 22:29:52 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial;

import java.util.HashMap;

import com.oracle.cep.cartridge.spatial.GeomGenerator.VarResult;
import com.oracle.cep.cartridge.spatial.GeomSpec.Config;
import com.oracle.cep.cartridge.spatial.Geometry.GeomType;

import junit.framework.TestCase;

public class TestGeomGenerator extends TestCase
{
	int srid = 0;
	int dim = 2;
	
	private static final int TYPE_DONTCARE = -2;
	
	@SuppressWarnings("serial")
	private static HashMap<GeomType, Integer> s_typeMap = new HashMap<GeomType, Integer>() {{
		put(GeomType.Point, Geometry.GTYPE_POINT);
		put(GeomType.Circle, -1);
		put(GeomType.Rectangle, Geometry.GTYPE_POLYGON);
		put(GeomType.Polygon, Geometry.GTYPE_POLYGON);
		put(GeomType.LineString, Geometry.GTYPE_CURVE);
	}};

	private double toDouble(Object v)
	{
		double dv = 0.0;
		if (v == null) return dv;
		if (v instanceof String)
		{
			return Double.parseDouble((String) v);
		}
		if (v instanceof Number)
		{
			return ((Number)v).doubleValue();
		}
		return Double.parseDouble(v.toString());
	}

	private void verify(GeomType type, int flag, Object[] spec)
	{
		int ordsindex = 0;
		int expectedType = -1;
		verify(type, flag, spec, ordsindex, expectedType);
	}
	
	private void verify(GeomType type, int flag, Object[] spec, int ordsindex)
	{
		int expectedType = -1;
		verify(type, flag, spec, ordsindex, expectedType);
	}
	
	private void verify(GeomType type, int flag, Object[] spec, int ordsindex, int expectedType)
	{
		if (expectedType == -1)
			expectedType = s_typeMap.get(type);
		genNverifyGeometry(type, flag, spec, spec, ordsindex, expectedType);
		
		int flag1 = flag  | Config.FLAG_CSV_ORDS;
		Object[] spec1 = new Object[ordsindex+1];
		int i = 0;
		for (; i < ordsindex; i++)
			spec1[i] = spec[i];
		spec1[i] = toCsv(spec, ordsindex);
		genNverifyGeometry(type, flag1, spec1, spec, ordsindex, expectedType);

		int flag2 =flag | Config.FLAG_CSV;
		Object[] spec2 = new Object[1];
		spec2[0] = toCsv(spec, 0);
		genNverifyGeometry(type, flag2, spec2, spec, ordsindex, expectedType);
		
		int len = spec.length - ordsindex;
		Object[] ords = new Object[len];
		for (i = 0; i < len; i++)
			ords[i] = spec[i + ordsindex];
		
		GeomSpec gspec = genGeomSpec(type, ords);
		Geometry geom = GeomGenerator.create(gspec);
		verifyGeometry(type, geom, ords, 0, expectedType);
	}
	
	private void genNverifyGeometry(GeomType type, int flag, Object[] args, Object[] coords, 
			int ordsidx, int expectedType)
	{
		Config cfg = new Config();
		cfg.setDim(dim);
		cfg.setSrid(srid);
		cfg.setFlag(flag);
		VarResult r = GeomGenerator.create(cfg, args, 0);
		Geometry geom = (Geometry)r.result;
		verifyGeometry(type, geom, coords,ordsidx, expectedType);
	}
	
	private void verifyGeometry(GeomType type, Geometry geom, Object[] coords, 
			int ordsidx, int expectedType)
	{
		assertEquals("srid should be same ", srid, geom.getSRID());
		assertEquals("dimension should be same ", dim, geom.getDimensions());
		if (expectedType >= 0)
		{
			assertEquals("type should be same ", expectedType, geom.getType());
			double[] ords = geom.getOrdinatesArray();
			assertEquals("number of ordinates should be same ", (coords.length - ordsidx), ords.length);
			for (int i = 0; i < ords.length; i++)
			{
				double v = toDouble(coords[ordsidx + i]);
				assertEquals("ordinates ["+i+"]" , v, ords[i], 1e-15);
			}
		}
	}
	
	private String toCsv(Object[] args, int index)
	{
		StringBuilder b = new StringBuilder();
		
		int c = 0;
		for (int i = index; i < args.length; i++)
		{
			if (c++ > 0) b.append(" , ");
			Object v = args[i];
			if (v instanceof Number) b.append( ((Number)v).toString());
			else if (v instanceof String) b.append( ((String)v));
			else b.append(v.toString());
		}
		return b.toString();
	}
	
	private Object[] genGeomSpecArray(GeomType type, int nocords, Object[] args)
	{
		int len = 1;
		if (nocords>0) len++;
		len += args.length;
		Object[] spec = new Object[len];
		int pos = 0;
		spec[pos++] = type;
		if (nocords>0) spec[pos++]=nocords;
		for (int i = 0; i < args.length; i++)
		{
			spec[pos++] = args[i];
		}
		return spec;
	}
	
	private GeomSpec genGeomSpec(GeomType type, Object[] args)
	{
		GeomSpec spec = new GeomSpec();
		spec.setType(type);
		Config cfg = new Config();
		cfg.setDim(dim);
		cfg.setSrid(srid);
		spec.setConfig(cfg);
		double[] coords = new double[args.length];
		for (int i = 0; i < args.length; i++)
		{
			coords[i] = GeomSpec.doubleVal(args[i]);
		}
		spec.setCoords(coords);
		return spec;
	}	

	//allowed format
	//lng,lat   SIMPLE_POINT
	//'lng,lat' SIMPLE_POINT | CSV
	//type,"lng,lat,..."  CSV_COORDS
	//'type,"lng,lat,..."'  CSV | CSV_COORDS
	//type, lng,lat,... NO_COORDS
	//'type, lng,lat,...' CSV | NO_COORDS
	//type, nocords, lng,lat,... NO_COORDS
	//'type, nocords, lng,lat,...' CSV | NO_COORDS
	//string coords
	//double coords
	public void testCanCreateFromSimplePoint()
	{
		int flag = Config.FLAG_SIMPLE_POINT;
		Object[] ords = new Object[2];
		ords[0] = 1.0;
		ords[1] = 2.0;
		verify(GeomType.Point, flag, ords);
	}

	public void testCanCreateFromSimplePointWithMoreOrds()
	{
		int flag = Config.FLAG_SIMPLE_POINT;
		Object[] ords = new Object[3];
		ords[0] = 1.0;
		ords[1] = 2.0;
		ords[2] = 3.0;
		Object[] expectedords = new Object[2];
		expectedords[0] = 1.0;
		expectedords[1] = 2.0;
		verify(GeomType.Point, flag, expectedords);
	}
	
	public void testCantCreateFromSimplePointWithLessOrds()
	{
		Exception expected = null;
		try
		{
			int flag = Config.FLAG_SIMPLE_POINT;
			Object[] ords = new Object[1];
			ords[0] = 1.0;
			verify(GeomType.Point, flag, ords);
		} catch(Exception e)
		{
			expected = e;
		}
		assertTrue(expected != null && expected instanceof RuntimeException);
	}
		
	public void testCanCreateFromCoords()
	{
		int ordsindex = 1;
		int flag = 0;
		Object[] ords = new Object[2];
		ords[0] = 1.0;
		ords[1] = 2.0;
		Object[] spec = genGeomSpecArray(GeomType.Point, 0, ords);
		verify(GeomType.Point, flag, spec, ordsindex);
		
		Object[] ordsrect = new Object[4];
		ordsrect[0] = 1.0;
		ordsrect[1] = 2.0;
		ordsrect[2] = 3.0;
		ordsrect[3] = 4.0;
		Object[] specrect = genGeomSpecArray(GeomType.Rectangle, 0, ordsrect);
		verify(GeomType.Rectangle, flag, specrect, ordsindex);

		Object[] ordscircle = new Object[3];
		ordscircle[0] = 1.0;
		ordscircle[1] = 2.0;
		ordscircle[2] = 3.0;
		Object[] speccircle = genGeomSpecArray(GeomType.Circle, 0, ordscircle);
		verify(GeomType.Circle, flag, speccircle, ordsindex);

		Object[] ordspolygon = new Object[8];
		ordspolygon[0] = 1.0;
		ordspolygon[1] = 2.0;
		ordspolygon[2] = 3.0;
		ordspolygon[3] = 4.0;
		ordspolygon[4] = 5.0;
		ordspolygon[5] = 6.0;
		ordspolygon[6] = 1.0;
		ordspolygon[7] = 2.0;
		Object[] specpolygon = genGeomSpecArray(GeomType.Polygon, 0, ordspolygon);
		verify(GeomType.Polygon, flag, specpolygon, ordsindex);

		Object[] ordslinestr = new Object[6];
		ordslinestr[0] = 1.0;
		ordslinestr[1] = 2.0;
		ordslinestr[2] = 3.0;
		ordslinestr[3] = 4.0;
		ordslinestr[4] = 5.0;
		ordslinestr[5] = 6.0;
		Object[] speclinestr = genGeomSpecArray(GeomType.LineString, 0, ordslinestr);
		verify(GeomType.LineString, flag, speclinestr, ordsindex);
	}

	public void testCantCreatePolygonWithLessOrds()
	{
		Exception expected = null;
		try
		{
			int ordsindex = 1;
			int flag = 0;
			Object[] ordspolygon = new Object[7];
			ordspolygon[0] = 1.0;
			ordspolygon[1] = 2.0;
			ordspolygon[2] = 3.0;
			ordspolygon[3] = 4.0;
			ordspolygon[4] = 5.0;
			ordspolygon[5] = 6.0;
			ordspolygon[6] = 1.0;
			Object[] specpolygon = genGeomSpecArray(GeomType.Polygon, 0, ordspolygon);
			verify(GeomType.Polygon, flag, specpolygon, ordsindex);
		} catch(Exception e)
		{
			expected = e;
		}
		assertTrue(expected != null && expected instanceof RuntimeException);
	}

	public void testCanCreateFromNoOfCoords()
	{
		int ordsindex = 2;
		int flag = Config.FLAG_HAS_NO_ORDS;
		Object[] ords = new Object[2];
		ords[0] = 1.0;
		ords[1] = 2.0;
		Object[] spec = genGeomSpecArray(GeomType.Point, 2, ords);
		verify(GeomType.Point, flag, spec, ordsindex);

		Object[] ordsrect = new Object[4];
		ordsrect[0] = 1.0;
		ordsrect[1] = 2.0;
		ordsrect[2] = 3.0;
		ordsrect[3] = 4.0;
		Object[] specrect = genGeomSpecArray(GeomType.Rectangle, 4, ordsrect);
		verify(GeomType.Rectangle, flag, specrect, ordsindex);

		Object[] ordscircle = new Object[3];
		ordscircle[0] = 1.0;
		ordscircle[1] = 2.0;
		ordscircle[2] = 3.0;
		Object[] speccircle = genGeomSpecArray(GeomType.Circle, 3, ordscircle);
		verify(GeomType.Circle, flag, speccircle, ordsindex);

		Object[] ordspolygon = new Object[8];
		ordspolygon[0] = 1.0;
		ordspolygon[1] = 2.0;
		ordspolygon[2] = 3.0;
		ordspolygon[3] = 4.0;
		ordspolygon[4] = 5.0;
		ordspolygon[5] = 6.0;
		ordspolygon[6] = 1.0;
		ordspolygon[7] = 2.0;
		Object[] specpolygon = genGeomSpecArray(GeomType.Polygon, 8, ordspolygon);
		verify(GeomType.Polygon, flag, specpolygon, ordsindex);

		Object[] ordslinestr = new Object[6];
		ordslinestr[0] = 1.0;
		ordslinestr[1] = 2.0;
		ordslinestr[2] = 3.0;
		ordslinestr[3] = 4.0;
		ordslinestr[4] = 5.0;
		ordslinestr[5] = 6.0;
		Object[] speclinestr = genGeomSpecArray(GeomType.LineString, 6, ordslinestr);
		verify(GeomType.LineString, flag, speclinestr, ordsindex);
	}
	
	public void testCantCreatePolygonNoOfOrdsWithLessOrds()
	{
		Exception expected = null;
		try
		{
			int ordsindex = 1;
			int flag = Config.FLAG_HAS_NO_ORDS;
			Object[] ordspolygon = new Object[7];
			ordspolygon[0] = 1.0;
			ordspolygon[1] = 2.0;
			ordspolygon[2] = 3.0;
			ordspolygon[3] = 4.0;
			ordspolygon[4] = 5.0;
			ordspolygon[5] = 6.0;
			ordspolygon[6] = 1.0;
			Object[] specpolygon = genGeomSpecArray(GeomType.Polygon, 8, ordspolygon);
			verify(GeomType.Polygon, flag, specpolygon, ordsindex);
		} catch(Exception e)
		{
			expected = e;
		}
		assertTrue(expected != null && expected instanceof RuntimeException);
	}

	public void testCantCreateUnknownType()
	{
		Exception expected = null;
		try
		{
			int ordsindex = 1;
			int flag = 0;
			Object[] ords = new Object[2];
			ords[0] = 1.0;
			ords[1] = 2.0;
			Object[] spec = genGeomSpecArray(null, 0, ords);
			verify(GeomType.Point, flag, spec, ordsindex);
		} catch(Exception e)
		{
			expected = e;
		}
		assertTrue(expected != null && expected instanceof RuntimeException);
	}	

	private Object[] genGeomSpecMiddle(GeomType type, int nocords, Object[] ords)
	{
		int len = 1;
		if (nocords>0) len++;
		len += ords.length+nocords;
		Object[] spec = new Object[len];
		int pos = 0;
		spec[pos++] = type;
		if (nocords>0) spec[pos++]=nocords;
		for (int i = 0; i < ords.length; i++)
		{
			spec[pos++] = ords[i];
		}
		for (int j = ords.length-1; j >= 0; j--)
		{
			spec[pos++] = ords[j];
		}
		return spec;
	}
	
	public void testCanCreateFromMiddleOfRow()
	{
		int ordsindex = 2;
		int flag = Config.FLAG_HAS_NO_ORDS;
		Object[] ords = new Object[2];
		ords[0] = 1.0;
		ords[1] = 2.0;
		Object[] spec = genGeomSpecMiddle(GeomType.Point, 2, ords);
		verify(GeomType.Point, flag, spec, ordsindex, TYPE_DONTCARE);

		Object[] ordsrect = new Object[4];
		ordsrect[0] = 1.0;
		ordsrect[1] = 2.0;
		ordsrect[2] = 3.0;
		ordsrect[3] = 4.0;
		Object[] specrect = genGeomSpecMiddle(GeomType.Rectangle, 4, ordsrect);
		verify(GeomType.Rectangle, flag, specrect, ordsindex, TYPE_DONTCARE);

		Object[] ordscircle = new Object[3];
		ordscircle[0] = 1.0;
		ordscircle[1] = 2.0;
		ordscircle[2] = 3.0;
		Object[] speccircle = genGeomSpecMiddle(GeomType.Circle, 3, ordscircle);
		verify(GeomType.Circle, flag, speccircle, ordsindex, TYPE_DONTCARE);

		Object[] ordspolygon = new Object[8];
		ordspolygon[0] = 1.0;
		ordspolygon[1] = 2.0;
		ordspolygon[2] = 3.0;
		ordspolygon[3] = 4.0;
		ordspolygon[4] = 5.0;
		ordspolygon[5] = 6.0;
		ordspolygon[6] = 1.0;
		ordspolygon[7] = 2.0;
		Object[] specpolygon = genGeomSpecMiddle(GeomType.Polygon, 8, ordspolygon);
		verify(GeomType.Polygon, flag, specpolygon, ordsindex, TYPE_DONTCARE);

		Object[] ordslinestr = new Object[6];
		ordslinestr[0] = 1.0;
		ordslinestr[1] = 2.0;
		ordslinestr[2] = 3.0;
		ordslinestr[3] = 4.0;
		ordslinestr[4] = 5.0;
		ordslinestr[5] = 6.0;
		Object[] speclinestr = genGeomSpecMiddle(GeomType.LineString, 6, ordslinestr);
		verify(GeomType.LineString, flag, speclinestr, ordsindex, TYPE_DONTCARE);
	}
	
	public void testCanDetectFlagsFromObjects()
	{
		Object[] args = new Object[1];
		args[0] = "1.0,2.0";
		int flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_CSV|Config.FLAG_SIMPLE_POINT, flag);
		
		args = new Object[1];
		args[0] = "Point,1.0,2.0";
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_CSV, flag);
		
		args = new Object[1];
		args[0] = "Point,2,1.0,2.0";
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_CSV|Config.FLAG_HAS_NO_ORDS, flag);

		args = new Object[1];
		args[0] = "Point,\"1.0,2.0\"";
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_CSV|Config.FLAG_CSV_ORDS, flag);

		args = new Object[2];
		args[0] = 1.0;
		args[1] = 2.0;
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_SIMPLE_POINT, flag);

		args = new Object[3];
		args[0] = "Point";
		args[1] = 1.0;
		args[2] = 2.0;
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", 0, flag);
		
		args = new Object[2];
		args[0] = "Point";
		args[1] = "1.0,2.0";
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_CSV_ORDS, flag);

		args = new Object[4];
		args[0] = "Point";
		args[1] = 2;
		args[2] = 1.0;
		args[3] = 2.0;
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_HAS_NO_ORDS, flag);

		args = new Object[3];
		args[0] = "Point";
		args[1] = 2;
		args[2] = "1.0,2.0";
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_HAS_NO_ORDS|Config.FLAG_CSV_ORDS, flag);

		args = new Object[4];
		args[0] = "Point";
		args[1] = 2.0;
		args[2] = 1.0;
		args[3] = 2.0;
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", 0, flag);

		args = new Object[3];
		args[0] = "Point";
		args[1] = 2.0;
		args[2] = "1.0,2.0";
		flag = GeomSpec.autoFlag(args);
		assertEquals("flag ", Config.FLAG_CSV_ORDS, flag);
		
	}
}