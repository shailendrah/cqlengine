/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/GeomGenerator.java /main/4 2015/10/01 22:29:44 hopark Exp $ */

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
 * @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/GeomGenerator.java /main/4 2015/10/01 22:29:44 hopark Exp $
 * @author hopark
 * @since release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial;

public class GeomGenerator {
    public static class VarResult {
        public Object result;
        public int consumed;
    }
    
	/**
	 * Generic method for constructing arbitrary geometry objects.
	 *
	 * @param srid 
	 *            the srid of the geometry
	 * @param dim
	 *            the dimension of the geometry
	 * @param flag
	 *            the creation flag
	 * @param args
	 *            the argument array
	 * @param idx
	 *            the starting index
	 *            
	 */
	public static VarResult create(GeomSpec.Config cfg,
			Object[] args, int startIndex) {
		VarResult r = GeomSpec.fromArray(cfg, args, startIndex);
		r.result = create((GeomSpec) r.result);
		return r;
	}
	
	/**
	 * Generic method for constructing arbitrary geometry objects.
	 *
	 * @param srid 
	 *            the srid of the geometry
	 * @param dim
	 *            the dimension of the geometry
	 * @param flag
	 *            the creation flag
	 * @param args
	 *            the argument array
	 * @param idx
	 *            the starting index
	 *            
	 */
	public static Geometry create(GeomSpec.Config cfg, String csv) {
		GeomSpec spec = GeomSpec.fromCsv(cfg, csv);
		return create(spec);
	}

	public static Geometry create(GeomSpec spec)
	{
		switch(spec.getType())
		{
		case Point: return createPoint(spec);
		case Circle: return createCircle(spec);
		case Rectangle: return createRectangle(spec);
		case Polygon: return createPolygon(spec);
		case LineString: return createLineString(spec);
		case MultiPolygon: return createMultiPolygon(spec);
		}
		throw new RuntimeException(SpatialCartridgeLogger
				.UnknownGeometryErrorLoggable(spec.getType().name()).getMessage());
	}

	private static Geometry createPoint(GeomSpec spec) 
	{
		double[] coords = spec.getCoords();
		return Geometry.createPoint(spec.getSrid(), coords[0], coords[1]);
	}

	private static Geometry createCircle(GeomSpec spec) 
	{
		double[] coords = spec.getCoords();
		return Geometry.createCircle(spec.getSrid(), coords[0], coords[1], coords[2], spec.getArcTol());
	}

	private static Geometry createRectangle(GeomSpec spec) 
	{
		double[] coords = spec.getCoords();
		return Geometry.createRectangle(spec.getSrid(), coords[0], coords[1], coords[2],
				coords[3]);
	}

	private static Geometry createPolygon(GeomSpec spec) {
		double[] coords = spec.getCoords();
		return Geometry.createLinearPolygon(spec.getSrid(), coords);
	}

	private static Geometry createMultiPolygon(GeomSpec spec) {
		double[][] coords = spec.getMultiCoords();
		return Geometry.createLinearPolygon(spec.getSrid(), coords);
	}

	private static Geometry createLineString(GeomSpec spec) {
		double[] coords = spec.getCoords();
		return Geometry.createLinearLineString(spec.getSrid(), coords);
	}
}
