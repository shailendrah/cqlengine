package com.oracle.cep.cartridge.spatial.rtreeindex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.CartridgeRegistry;
import com.oracle.cep.cartridge.spatial.GeodeticParam;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;
import com.oracle.cep.cartridge.spatial.SpatialCartridgeLogger;
import com.oracle.cep.cartridge.spatial.SpatialContext;

public class OpContainTest extends TestCase {
	private ICartridgeContext cartridgeContext;
	private OpContain opContain;
	private RTreeIndex rtreeIndex;
	private int srid;

	@Override
	protected void setUp() throws Exception {
		SpatialCartridge.createInstance(new CartridgeRegistry());
		srid = GeodeticParam.LAT_LNG_WGS84_SRID;
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put(SpatialContext.GEO_PARAM, GeodeticParam.get(srid));
		cartridgeContext = new ICartridgeContext() {
			@Override
			public Map<String, Object> getProperties() {
				return props;
			}

			@Override
			public String getApplicationName() {
				return "Test";
			}
		};

		opContain = new OpContain(0, cartridgeContext);
		rtreeIndex = new RTreeIndex();
	}

	/**
	 * should create the proper MBR for a given point (geometry) that can be
	 * searched correctly.
	 * 
	 * @throws Exception
	 */
	public void testGetSearchMbrJGeometryDouble() throws Exception {
		JGeometry point = Geometry.createPoint(srid, -121.56482309103012,
				37.024538556883286);
		JGeometry poly = JGeometry.createLinearPolygon(new double[] {
				-121.56482309101001, 37.024538556883000, -121.56482309105012,
				37.024538556883000, -121.56482309105012, 37.024538556883800,
				-121.56482309101001, 37.024538556883800, -121.56482309101001,
				37.024538556883000 }, 2, srid);

		rtreeIndex.insert(poly, poly);
		// double[][] ptMBR = opContain.getSearchMbr(point, -0.01);

		for (double i = 1; i < 2000; i++) {
			ArrayList res = new ArrayList();
			double tol = i / 100;
			double[][] pointMBR = opContain.getSearchMbr(point, tol);
			rtreeIndex.rtree.search(pointMBR, res);
			assertEquals(1, res.size());
			assertTrue(poly.equals(res.get(0)));
		}

	}

	public void testNegativeTolerance() {
		JGeometry point = Geometry.createPoint(srid, -121.56482309103012,
				37.024538556883286);
		try {
			double[][] ptMBR = opContain.getSearchMbr(point, -0.01);
			fail("non positive tolerance should throw InvalidArgument Exception.");
		} catch (Exception e) {
			assertEquals(e.getMessage(), SpatialCartridgeLogger.NonPositiveTolerance());
		}
	}

}
