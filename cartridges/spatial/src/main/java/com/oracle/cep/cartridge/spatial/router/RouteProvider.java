/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/router/RouteProvider.java /main/1 2015/10/01 22:29:49 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      07/29/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/router/RouteProvider.java /main/1 2015/10/01 22:29:49 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.router;

import java.util.Collection;

import com.oracle.cep.cartridge.spatial.Geometry;

public interface RouteProvider
{
	public class Position
	{
		public double longitude;
		public double latitude;
		public Position() {}
		public Position(double lon, double lat) {longitude = lon; latitude = lat;}
		public Geometry toGeometry() {return Geometry.createPoint(8307, longitude, latitude); }

		public boolean equals(Object o)
		{
			Position p = (Position) o;
			double dx = (longitude - p.longitude);
			double dy = (latitude - p.latitude);
			double d = Math.sqrt(dx*dx + dy*dy);
			return ( d < 100);
		}
	}
		
	Geometry route(Collection<Position> points);
	Collection<Geometry> route(Collection<Position> requests[]);
}
