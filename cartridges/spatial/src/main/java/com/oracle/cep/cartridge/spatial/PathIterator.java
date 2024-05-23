/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/PathIterator.java /main/3 2015/11/22 02:12:09 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    PathIterator iterates intermediate points along a geodetic path.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/17/15 - add FindDist
    hopark      06/08/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/PathIterator.java /main/3 2015/11/22 02:12:09 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.geodesic.Geodesic;
import com.oracle.cep.cartridge.spatial.geodesic.GeodesicPath;
import com.oracle.cep.cartridge.spatial.geodesic.ReferenceSystem;

public class PathIterator implements Iterator<Geometry>
{
	int curSegN;
	int curInterval;
	Seg[] segs;
	Seg curSeg;
	int		maxSeg;
	
	private static class Seg
	{
		GeodesicPath path;
		double lng1, lat1, lng2, lat2;
		double interval;
		int intervals;
	}

	public static double[] SimplyfyPath(double[] coords, double distInMeters)
	{
		int i = 0;
		int total = coords.length/2 + 1;
		double lng1 = coords[i++];
		double lat1 = coords[i++];
		double nextDist = distInMeters;
		Geometry lastPt = Geometry.createPoint(GeodeticParam.LAT_LNG_WGS84_SRID, lng1, lat1);
		List<Double> points = new ArrayList<Double>();
		int skipCount = 0;
		while(i < coords.length)
		{
			double lng2 = coords[i++];
			double lat2 = coords[i++];
			Geometry pt = Geometry.createPoint(GeodeticParam.LAT_LNG_WGS84_SRID, lng2, lat2);
			double dist = 0;
			try {
				dist = Geometry.distance(lastPt, pt, GeodeticParam.WGS84_TOL, 
						GeodeticParam.WGS84_SMA, GeodeticParam.WGS84_SMA, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dist > nextDist || (i >= coords.length))
			{
				double[] c= lastPt.getFirstPoint();
				points.add(c[0]);
				points.add(c[1]);
				nextDist = distInMeters;
				lastPt = pt;
			} else {
				nextDist -= dist;
				skipCount++;
			}
		}
		//System.out.println(skipCount + " / " + total);		
		double[] r = new double[points.size()];
		i = 0;
		for (Double d : points)
		{
			r[i++] = d;
		}
		return r;
	}
	
	public PathIterator(Geometry path, double distInMeters)
	{
		//if (!path.isMultiPoint())
		//      throw new IllegalArgumentException(SpatialCartridgeLogger.InvalidGeometryType(path.toString(), "Path"));
		validateGeom(path);
		double[] coords = path.getOrdinatesArray();
		int i = 0;
		double lng1 = coords[i++];
		double lat1 = coords[i++];
		int n = coords.length / 2 - 1;
		segs = new Seg[n];

		double blat = lat1;
		double blng = lng1;
		double nextDist = distInMeters;
		maxSeg = 0;
		ReferenceSystem wgs = ReferenceSystem.WGS84;
		while(i < coords.length)
		{
			double lng2 = coords[i++];
			double lat2 = coords[i++];
			Geodesic g = wgs.Inverse(lat1, lng1, lat2, lng2,
					ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
			GeodesicPath line = new GeodesicPath(wgs, lat1, lng1, g.azi1,
					ReferenceSystem.DISTANCE_IN | ReferenceSystem.LONGITUDE);
			if (g.s12 < distInMeters)
			{
				lng1 = lng2;
				lat1 = lat2;
				continue;
			}
			Seg seg = new Seg();
			seg.lng1 = lng1;
			seg.lat1 = lat1;
			seg.lng2 = lng2;
			seg.lat2 = lat2;
			seg.path = line;
			seg.intervals = (int) (Math.ceil(g.s12 / distInMeters)); 
			seg.interval = g.s12 / seg.intervals;
			segs[maxSeg++] = seg;
			/*
			if (g.s12 > distInMeters)
			{
				Seg seg = new Seg();
				seg.lng1 = lng1;
				seg.lat1 = lat1;
				seg.lng2 = lng2;
				seg.lat2 = lat2;
				seg.path = line;
				seg.intervals = (int) (Math.ceil(g.s12 / distInMeters)); 
				seg.interval = g.s12 / seg.intervals;
				segs[maxSeg++] = seg;
				nextDist = distInMeters;
				blng = lat2;
				blng = lng2;
			} else {
				if (g.s12 > nextDist)
				{
					g = wgs.Inverse(blat, blng, lat2, lng2,
							ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
					line = new GeodesicPath(wgs, blat, blng, g.azi1,
							ReferenceSystem.DISTANCE_IN | ReferenceSystem.LONGITUDE);
					Seg seg = new Seg();
					seg.lng1 = blng;
					seg.lat1 = blat;
					seg.lng2 = lng2;
					seg.lat2 = lat2;
					seg.path = line;
					seg.intervals = (int) (Math.ceil(g.s12 / distInMeters)); 
					seg.interval = g.s12 / seg.intervals;
					segs[maxSeg++] = seg;
					nextDist = distInMeters;
					blng = lat2;
					blng = lng2;
				}
				else
				{
					nextDist -= g.s12;
				}
			}
			*/
			lng1 = lng2;
			lat1 = lat2;
		}
		resetIterator();
	}

	public void resetIterator()
	{
		curSegN = 0;
		curInterval = 0;
		curSeg = segs[curSegN];
	}

	public Geometry getFullPath()
	{
		resetIterator();
		List<Double> pts = new ArrayList<Double>();
		while (hasNext())
		{
			Geometry g = next(); 
			double[] c = g.getFirstPoint();
			pts.add(c[0]);
			pts.add(c[1]);
		}
		double[] coords = new double[pts.size()];
		int i = 0;
		for (Double v : pts) coords[i++] = v;
		return Geometry.createLinearLineString(GeodeticParam.LAT_LNG_WGS84_SRID, coords);
	}

	public Geometry getPathApproximated()
	{
		double[] coords = new double[(maxSeg+1)*2];
		int j = 0;
		for (int i = 0; i < maxSeg; i++)
		{
			coords[j++] = segs[i].lng1;
			coords[j++] = segs[i].lat1;
		}
		coords[j++] = segs[maxSeg-1].lng2;
		coords[j++] = segs[maxSeg-1].lat2;
		return Geometry.createLinearLineString(GeodeticParam.LAT_LNG_WGS84_SRID, coords);
	}
	
	@Override
	public boolean hasNext() {
		if (curSeg != null && curInterval >= curSeg.intervals)
		{
			return curSegN < maxSeg;
		}
		return (curSeg != null);
	}

	@Override
	public Geometry next() {
		Geodesic g = curSeg.path.Position(curInterval * curSeg.interval, ReferenceSystem.LATITUDE
						| ReferenceSystem.LONGITUDE);
		curInterval++;
		if (curInterval >= curSeg.intervals)
		{
			curInterval = 0;
			curSegN++;
			if (curSegN < maxSeg)
				curSeg = segs[curSegN];
			else curSeg = null;
		}
		return Geometry.createPoint(GeodeticParam.LAT_LNG_WGS84_SRID, g.lon2, g.lat2);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("No remove operation");
	}
	
	private static void validateGeom(Geometry g)
	{
		if (g.getSRID() !=  GeodeticParam.LAT_LNG_WGS84_SRID)
		{
			if (g.getSRID() == GeodeticParam.LAT_LNG_MERCATOR_SRID)
			{
				g.tfm_PopularMercator_to_8307(false); //Use Spherical Mercator
			}
			else
			{
				throw new IllegalArgumentException(SpatialCartridgeLogger.InvalidSRID(g.toString(), GeodeticParam.LAT_LNG_WGS84_SRID));
			}						
		}
		if (g.getDimensions() != 2)
		{
			throw new IllegalArgumentException(SpatialCartridgeLogger.InvalidDimension(g.toString(), 2));
		}
	}

	public static Geometry createPath(List<Geometry> geoms, int startDistance)
	{
		return createPath(geoms, startDistance, 0);
	}

	public static double[] createPath(Geometry geom, int startDistance, int side)
	{
		double[] coords = new double[4];
		ReferenceSystem wgs = ReferenceSystem.WGS84;
		double[] mbr = geom.getMBR();
		double lat1,lon1, lat2,lon2;
		switch(side)
		{
		case 0: lon1=mbr[0]; lat1= mbr[1]; lon2=mbr[2];	lat2=mbr[3];break;
		case 1: lon1=mbr[2]; lat1= mbr[3]; lon2=mbr[0];	lat2=mbr[1];break;
		case 2: lon1=mbr[2]; lat1= mbr[1]; lon2=mbr[0];	lat2=mbr[3];break;
		default: lon1=mbr[0]; lat1= mbr[3]; lon2=mbr[2];	lat2=mbr[1];break;
		}
		Geodesic g = wgs.Inverse(lat1, lon1, lat2, lon2,
				ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
		Geodesic g1 = wgs.Direct(g.lat1, g.lon1, g.azi1, -(g.s12/2 + startDistance));
		coords[0] = g1.lon2; 
		coords[1] = g1.lat2;
		g1 = wgs.Direct(g.lat2, g.lon2, g.azi2, (g.s12/2+startDistance));
		coords[2] = g1.lon2; 
		coords[3] = g1.lat2;
		return coords;
	}
	
	public static Geometry createPath(List<Geometry> geoms, int startDistance, int side)
	{
		return createPath(geoms, startDistance, side, 0.0);
	}
	
	public static Geometry createPath(List<Geometry> geoms, int startDistance, int side, double sazi)
	{
		int n = geoms.size(); 
		double[] coords = new double[n*2 + 4];
		int pos = 2;
		for (Geometry g : geoms)
		{
			validateGeom(g);
			double[] mbr = g.getMBR();
			coords[pos++] = (mbr[0] + mbr[2]) / 2;
			coords[pos++] = (mbr[1] + mbr[3]) / 2;
		}
		ReferenceSystem wgs = ReferenceSystem.WGS84;
		pos = 2;
		if (n == 1)
		{
			Geometry geom = geoms.get(0);
			double[] mbr = geom.getMBR();
			double lat1,lon1, lat2,lon2;
			switch(side)
			{
			case 0: lon1=mbr[0]; lat1= mbr[1]; lon2=mbr[2];	lat2=mbr[3];break;
			case 1: lon1=mbr[2]; lat1= mbr[3]; lon2=mbr[0];	lat2=mbr[1];break;
			case 2: lon1=mbr[2]; lat1= mbr[1]; lon2=mbr[0];	lat2=mbr[3];break;
			default: lon1=mbr[0]; lat1= mbr[3]; lon2=mbr[2];	lat2=mbr[1];break;
			}
			Geodesic g = wgs.Inverse(lat1, lon1, lat2, lon2,
					ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
			double saz = g.azi1 + sazi;
			double eaz = g.azi2 - sazi;
			Geodesic g1 = wgs.Direct(g.lat1, g.lon1, saz, -(g.s12/2 + startDistance));
			coords[0] = g1.lon2; 
			coords[1] = g1.lat2;
			g1 = wgs.Direct(g.lat2, g.lon2, eaz, (g.s12/2+startDistance));
			pos = (n+1)*2;
			coords[pos++] = g1.lon2; 
			coords[pos++] = g1.lat2;
		}
		else 
		{
			double lat1,lon1, lat2,lon2;
			switch(side)
			{
			case 0: lon1=coords[pos+0]; lat1= coords[pos+1]; lon2=coords[pos+2]; lat2=coords[pos+3];break;
			case 1: lon1=coords[pos+2]; lat1= coords[pos+3]; lon2=coords[pos+0]; lat2=coords[pos+1];break;
			case 2: lon1=coords[pos+2]; lat1= coords[pos+1]; lon2=coords[pos+0]; lat2=coords[pos+3];break;
			default: lon1=coords[pos+0]; lat1= coords[pos+3]; lon2=coords[pos+2]; lat2=coords[pos+1];break;
			}
			Geodesic g = wgs.Inverse(lat1, lon1, lat2, lon2,
					ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
			double saz = g.azi1 + sazi;
			double eaz = g.azi2 - sazi;
			Geodesic g1 = wgs.Direct(g.lat1, g.lon1, saz, -(g.s12/2 + startDistance));
			coords[0] = g1.lon2; 
			coords[1] = g1.lat2;
			pos = (n-1)*2;
			g = wgs.Inverse(coords[pos+1], coords[pos], coords[pos+3], coords[pos+2],
					ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
			g1 = wgs.Direct(g.lat2, g.lon2, eaz, (g.s12/2 + startDistance));
			pos = (n+1)*2;
			coords[pos++] = g1.lon2; 
			coords[pos++] = g1.lat2;
		}		
		return Geometry.createLinearLineString(GeodeticParam.LAT_LNG_WGS84_SRID, coords);
	}

	
	public static double[] createPath(double lon1, double lat1, double lon2, double lat2, int startDistance)
	{
		double[] coords = new double[4];
		ReferenceSystem wgs = ReferenceSystem.WGS84;
		Geodesic g = wgs.Inverse(lat1, lon1, lat2, lon2,
				ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
		Geodesic g1 = wgs.Direct(g.lat1, g.lon1, g.azi1, -(g.s12/2 + startDistance));
		coords[0] = g1.lon2; 
		coords[1] = g1.lat2;
		g1 = wgs.Direct(g.lat2, g.lon2, g.azi2, (g.s12/2+startDistance));
		coords[2] = g1.lon2; 
		coords[3] = g1.lat2;
		return coords;
	}
	
	private static double normal(double azi1, double azi2, double s)
	{
		//[-180 - 180] -> [0-360]
		azi1 += 180;
		azi2 += 180;
		double d  = (azi2-azi1)/2 + (s * 90);
		if (d < 0) d+=360;
		return d - 180;
	}
	
	/*
	 * Find geodesic positions from the given linestring
	 * 
	 * @param path  the input linestring
	 * @param distances the array of distance pairs, normal distance (the sign of value indicates the direction of normal) 
	 * @return the array of longitue,latitude,startIndex tuples
	 * 
	 */
	public static double[] findPos(Geometry path, int[] distances)
	{
		double[] r = new double[distances.length*2];
		double[] coords = path.getOrdinatesArray();
		int i = 0;
		double lng1 = coords[i++];
		double lat1 = coords[i++];
		int pos = 0;
		int dpos = 0;
		double curDist = 0.0;
		int nextDist = distances[pos];
		int nextDist2 = distances[pos+1];
		double nextS = Math.signum(nextDist2);
		nextDist2 = Math.abs(nextDist2);
		ReferenceSystem wgs = ReferenceSystem.WGS84;
		while(i < coords.length)
		{
			double lng2 = coords[i];
			double lat2 = coords[i+1];
			Geodesic g = wgs.Inverse(lat1, lng1, lat2, lng2,
					ReferenceSystem.DISTANCE | ReferenceSystem.AZIMUTH);
			boolean adv = true;
			if ((curDist  + g.s12) >= nextDist)
			{
				double dist = nextDist - curDist;
				Geodesic g1 = wgs.Direct(g.lat1, g.lon1, g.azi1, dist);
//Geometry pt = Geometry.createPoint(GeodeticParam.LAT_LNG_WGS84_SRID, g1.lon2, g1.lat2);
//System.out.println(pt.toJsonString());
				if (nextDist2 > 0)
				{
					Geodesic g2 = wgs.Direct(g1.lat2, g1.lon2, normal(g.azi1, g.azi2, nextS), nextDist2 );
					r[dpos++] = g2.lon2;
					r[dpos++] = g2.lat2;
					r[dpos++] = i-2;
					adv = false;	//check next
//pt = Geometry.createPoint(GeodeticParam.LAT_LNG_WGS84_SRID, g2.lon2, g2.lat2);
//System.out.println(pt.toJsonString());
				}
				else
				{
					r[dpos++] = g1.lon2;
					r[dpos++] = g1.lat2;
					r[dpos++] = i;
				}
				pos += 2;
				if (pos >= distances.length)
					break;
				nextDist = distances[pos];
				nextDist2 = distances[pos+1];
				nextS = Math.signum(nextDist2);
				nextDist2 = Math.abs(nextDist2);
			}
			if (adv)
			{
				curDist += g.s12;
				i+=2;
				lng1 = lng2;
				lat1 = lat2;
			}
		}
		return r;
	}
}
