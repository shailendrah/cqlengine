/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/Geometry.java /main/16 2015/10/01 22:29:44 hopark Exp $ */

/* Copyright (c) 2009, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      05/15/15 - add toJsonString
 hopark      04/21/15 - add general creategeometry
 hopark      09/28/10 - add IllegalStateException if geom SRID is different from param SRID
 hopark      09/11/09 - Creation
 */
package com.oracle.cep.cartridge.spatial;

import com.oracle.cep.cartridge.java.JavaTypeSystem;

import oracle.cep.common.Datatype;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ICartridgeRegistry;
import oracle.spatial.geometry.J3D_Geometry;
import oracle.spatial.geometry.JGeometry;

import java.sql.Struct;

import org.apache.commons.logging.Log;

import java.sql.SQLException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.*;
/*
TODO_JTS enable it after third party license is approved

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
*/

/**
* Geometry is the class representing geometry types.
* It extends oracle.spatial.geometry.JGeometry and supports spatial contexts.
* 
* All geometries created using Geometry methods are in 2d.
*
* @version %I%, %G%
* @since   11.1.1.3
*/
public class Geometry extends JGeometry
{
  protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);//LogFactory.getLog(SpatialCartridge.LOGGER_NAME);

  private static final long serialVersionUID = 6059743664877615477L;

  static Datatype s_GeometryType = null;
  static Datatype s_JGeometryType = null;
  static Datatype s_J3DGeometryType = null;
  static Datatype s_Geometry3DType = null;
  
  static final boolean DEBUG_BUFFER = false;
  
  final static int[] RECT_SD_ELEMENT_INFO = new int[] {1,1003,3};
  final static int[] CIRCLE_SD_ELEMENT_INFO = new int[] {1,1003,4};
  final static int[] POINT_SD_ELEMENT_INFO = new int[] {1,1,1};
	public final static int G3D_MARK =  10000;

	public static final String JSON_TAG_TYPE = "type";
	public static final String JSON_TAG_SRID = "srid";
	public static final String JSON_TAG_COORDS = "coordinates";

	public enum GeomType {Point, Circle, Rectangle, Polygon, LineString, MultiPolygon, GeometryCollection, Solid, Surface, MultiSolid, MultiSurface};
	
	public static String[] typeNames = null;
	public static Map<String,GeomType> typeNameMap = null;
	
	public static String[] getTypeNames() {
		if (typeNames == null)
		{
			GeomType[] v = GeomType.values();
			typeNames = new String[v.length];
			for (int i = 0; i < v.length; i++)
				typeNames[i] = v[i].name();
		}
		return typeNames;
	}

	public static GeomType stringToGeomType(String s)
	{
		if (typeNameMap == null)
		{
			typeNameMap = new HashMap<String, GeomType>();
			for (GeomType v : GeomType.values())
				typeNameMap.put(v.name().toUpperCase(), v);
		}
		return typeNameMap.get(s.toUpperCase());
	}
	  
  @SuppressWarnings("serial")
final static HashMap<Integer,GeomType> s_typeNameMap = new HashMap<Integer,GeomType>() {{
	  put(JGeometry.GTYPE_POINT, GeomType.Point);
	  put(JGeometry.GTYPE_POLYGON, GeomType.Polygon);
	  put(JGeometry.GTYPE_CURVE, GeomType.LineString);
	  put(JGeometry.GTYPE_MULTIPOINT, GeomType.LineString);
	  put(JGeometry.GTYPE_MULTIPOLYGON, GeomType.MultiPolygon);
	  put(JGeometry.GTYPE_MULTICURVE, GeomType.LineString);
	  put(JGeometry.GTYPE_COLLECTION, GeomType.GeometryCollection);
	  put(J3D_Geometry.GTYPE_SOLID + G3D_MARK, GeomType.Solid);
	  put(J3D_Geometry.GTYPE_SURFACE + G3D_MARK, GeomType.Surface);
	  put(J3D_Geometry.GTYPE_MULTISOLID + G3D_MARK, GeomType.MultiSolid);
	  put(J3D_Geometry.GTYPE_MULTISURFACE + G3D_MARK, GeomType.MultiSurface);
  }};

  public static Datatype getGeometryType()
  {
    if (s_GeometryType != null)
      return s_GeometryType;

    SpatialCartridge spatial = SpatialCartridge.getInstance();
    assert (spatial != null);
    
    ICartridgeRegistry reg = spatial.getCartridgeRegistry();
    JavaTypeSystem javaTypeLocator = (JavaTypeSystem) reg.getJavaTypeSystem();
    
    s_GeometryType = (Datatype) javaTypeLocator.getCQLType(Geometry.class);
    s_Geometry3DType = (Datatype) javaTypeLocator.getCQLType(Geometry3D.class);
    s_JGeometryType  = (Datatype) javaTypeLocator.getCQLType(oracle.spatial.geometry.JGeometry.class);
    s_J3DGeometryType  = (Datatype) javaTypeLocator.getCQLType(oracle.spatial.geometry.J3D_Geometry.class);

    return s_GeometryType;
  }
  
  /**
   * Check if the given type is assignable to Geometry type.
   * It checks if the type is one of oracle.cep.cartridge.spatial.Geometry, oracle.spatial.geometry.JGeometry, and oracle.spatial.geometry.J3D_Geometry.
   * @param typ  the datatype to check
   * @return
   */
  public static boolean isAllGeometryType(Datatype typ)
  {
    assert (typ != null);
	  
    Datatype GeometryType = getGeometryType();
    boolean b = GeometryType.isAssignableFrom(typ);
    if (b) return b;
    // check super classes since isAssignableFrom will be false for super classes.
    if (typ.name().equals(s_JGeometryType.name())) return true;
    
    b = s_J3DGeometryType.isAssignableFrom(typ);
    if (b) return b;
    // check super classes since isAssignableFrom will be false for super classes.
    if (typ.name().equals(s_J3DGeometryType.name())) return true;
    
    return false;
  }
  
  /**
   * Load Geometry from struct
   */
  public static Geometry toGeometry(Struct struct) throws SQLException
  {
	  JGeometry jgeom = JGeometry.loadJS(struct);
	  return to_Geometry(jgeom);	  
  }
  
  public Geometry()
  {
	  super(0, 0, (int[])null, (double[])null);
  }
  
  /*
   * The following constructor is not really necessary.
   * But it's required because JGeometry does not define the default constructor.
   * 
   * Also note that anything less than 2000 is considered as 2D, therefore
   *    one can use 1 instead of 2001, 2, for 2002, etc.
   */
  Geometry(int gtype, int[] elemInfo, double[] ordinates)
  {
    super(gtype, SpatialCartridge.getContextSRID(null), elemInfo, ordinates);
  }
  
  Geometry(int gtype, int srid, int[] elemInfo, double[] ordinates)
  {
    super(gtype, srid, elemInfo, ordinates);
  }  
  
  Geometry(String s) throws Exception
  {
    super(GeometryParser.getGtype(s), GeometryParser.getSRID(s), 
        GeometryParser.getElemInfo(s), GeometryParser.getOrdinates(s));  
  }
  
  
  /**
   * Create a 2d geometry that is a point.
   * The srid is picked up from the spatial context.
   *  
   * @param x  the x coordinate of the lower left   
   * @param y  the y coordinate of the lower left
   * @return a 2d point type geometry
   */
  public static Geometry createPoint(double x, double y)
  {
    return createPoint(SpatialCartridge.getContextSRID(null), x, y);    
  }
  
  /**
   * Create a 3d geometry that is a point with the given srid.
   *  
   * @param srid  the srid of the geometry
   * @param x  the x coordinate of the lower left   
   * @param y  the y coordinate of the lower left
   * @param z  the z coordinate of the upper right
   * @return a 3d point type geometry
   */
  public static Geometry createPoint(int srid, double x, double y)
  {
    double[] ords = new double[] {x,y};
    return new Geometry(JGeometry.GTYPE_POINT, srid, POINT_SD_ELEMENT_INFO, ords);
  }
  
  /**
   * Create a 2d geometry that is a rectangle.
   * The srid is picked up from the spatial context.
   * 
   * @param minx  the x coordinate of the lower left   
   * @param miny  the y coordinate of the lower left
   * @param maxx  the x coordinate of the upper right
   * @param maxy  the y coordinate of the upper right
   * @return a 2d rectangle type geometry
   */
  public static Geometry createRectangle(double x1, double y1, double x2, double y2)
  {
    return createRectangle(SpatialCartridge.getContextSRID(null), x1, y1, x2, y2);  
  }
  
  /**
   * Create a 2d geometry that is a rectangle with the given srid.
   *  
   * @param srid  the srid of the geometry
   * @param minx  the x coordinate of the lower left   
   * @param miny  the y coordinate of the lower left
   * @param maxx  the x coordinate of the upper right
   * @return a 2d rectangle type geometry
   */
  public static Geometry createRectangle(int srid, double x1, double y1, double x2, double y2)
  {
    double[] ords = new double[] {x1, y1, x2, y2};
    return new Geometry(JGeometry.GTYPE_POLYGON, srid, RECT_SD_ELEMENT_INFO, ords);
  }
  
  /**
   * Create a 2d geometry that is a circle.
   * 
   * @param srid
   * @param x1
   * @param y1
   * @param radius
   * @return
   */
  public static Geometry createCircle(double x1, double y1, double radius)
  {
    int srid = SpatialCartridge.getContextSRID(null);
    GeodeticParam geoParam = SpatialCartridge.getContextGeodeticParam(null);
	double arcTol = geoParam.getArcTol();
    return createCircle(srid, x1, y1, radius, arcTol);
  }
  
  public static Geometry createCircle(int srid, double x1, double y1, double radius)
  {
	  double arcTol = GeodeticParam.getDefaultArcTol(srid);
	  return createCircle(srid, x1, y1, radius, arcTol);  
  }
  
  /**
   * Create a 3d geometry that is a circle with the given srid.
   * 
   * @param srid
   * @param x1 - center x ordinate
   * @param y1 - center y ordinate
   * @param radius - circle radius in meters
   * @param arcTol - circle arc tolerance in meters
   * @return circle geometry type
   */
  public static Geometry createCircle(int srid, double x1, double y1, double radius, double arcTol)
  {
	if (srid == GeodeticParam.CARTESIAN_SRID)
	{
	    JGeometry g = JGeometry.createCircle(x1, y1, radius, srid);
	    return to_Geometry(g);
	}
	if (srid != GeodeticParam.LAT_LNG_WGS84_SRID)
	{
	    throw new IllegalArgumentException(SpatialCartridgeLogger.InvalidSRIDForCircleLoggable(srid).getMessage());
	}
    JGeometry g = JGeometry.circle_polygon(x1, y1, radius, arcTol);
    return to_Geometry(g);
  }
  
  /**
   * Create a 2d geometry that is a linear line string.
   * A linear line string contains 1 or more pairs of points forming lines. It does not contain
   *  arcs.
   * The srid is picked up from the spatial context.
   * The result geometry has the element info of {1, 2, 1}
   * 
   * @param coords the coordinates of the linear line string
   * @param dim  the dimensionality of the given coordinates.
   * @return a 2d linear line string type geometry
   */
  public static Geometry createLinearLineString(double[] coords)
  {
	  int srid = SpatialCartridge.getContextSRID(null);
	  return createLinearLineString(srid, coords);
  }
  
  /**
   * Create a 2d geometry that is a linear line string with the given srid.
   * A linear line string contains 1 or more pairs of points forming lines. It does not contain
   *  arcs.
   * The result geometry has the element info of {1, 2, 1}
   *
   * @param srid  the srid of the geometry
   * @param coords the coordinates of the linear line string
   * @param dim  the dimensionality of the given coordinates.
   * @return a 2d linear line string type geometry
   */
  public static Geometry createLinearLineString(int srid, double[] coords)
  {
    JGeometry g = JGeometry.createLinearLineString(coords, 2, srid);
    return to_Geometry(g);
  }
  
  
  /**
   * Creates a 2d geometry that is a simple linear polygon without holes.
   * Note that if the supplied coordinate array does not close itself, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of the input coordinates array. the new coordinate
   * repeats the first one.
   * The srid is picked up from the spatial context.
   *
   * @param coords  the coordinates of the linear polygon
   * @param dim the dimensionality of the coordinates
   * @return a Geometry object that is a linear polygon
   */
  public static Geometry createLinearPolygon(double[] coords)
  {
  	int srid = SpatialCartridge.getContextSRID(null);
  	return createLinearPolygon(srid, coords);
  }
  
  /**
   * Creates a 3d geometry that is a simple linear polygon without holes.
   * Note that if the supplied coordinate array does not close itself, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of the input coordinates array. the new coordinate
   * repeats the first one.
   *
   * @param srid the srid of the polygon
   * @param coords  the coordinates of the linear polygon
   * @param dim the dimensionality of the coordinates
   * @return a Geometry object that is a linear polygon
   */
  public static Geometry createLinearPolygon(int srid, double[] coords)
  {
	  JGeometry g = JGeometry.createLinearPolygon(coords, 2, srid);
    return to_Geometry(g);
  }
  
  /**
   * Creates a 2d geometry that is a linear polygon which may have
   * holes. Each array in the double array parameter represents a single ring
   * of the polygon. The outer ring must be the first in the double array.
   * Note that for each ring, if its coordinate array does not close itself, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of that ring's coordinates array. The new coordinate
   * repeats the first one for the said ring.
   * The srid is picked up from the spatial context.
   *
   * @param coords  an array of double-typed arrays that contains
   *                all the rings' coordinates
   * @param dim  the dimensionality of the coordinates
   * @return a Geometry object that is a linear polygon
   */
  public static Geometry createLinearPolygon(double[][] coords)
  {
  	int srid = SpatialCartridge.getContextSRID(null);
  	return createLinearPolygon(srid, coords);
  }
  
  /**
   * Creates a 2d geometry that is a linear polygon which may have
   * holes. Each array in the double array parameter represents a single ring
   * of the polygon. The outer ring must be the first in the double array.
   * Note that for each ring, if its coordinate array does not close itself, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of that ring's coordinates array. The new coordinate
   * repeats the first one for the said ring.
   *
   * @param srid  the srid of the polygon
   * @param coords  an array of double-typed arrays that contains
   *                all the rings' coordinates
   * @param dim  the dimensionality of the coordinates
   * @return a Geometry object that is a linear polygon
   */
  public static Geometry createLinearPolygon(int srid, double[][] coords)
  {
	  JGeometry g =  JGeometry.createLinearPolygon(coords, 2, srid);
	  return to_Geometry(g);
  }
  
  public static Geometry createCollection(List<Geometry> geoms)
  {
	  int srid = 0;
	  int totalords = 0;
	  List<Geometry> glist = new ArrayList<Geometry>();
	  for (Geometry g : geoms)
	  {
		  srid = g.srid;
		  if (g.getType() == JGeometry.GTYPE_COLLECTION)
		  {
			  for (JGeometry e : g.getElements())
			  {
				  Geometry eg = to_Geometry(e);
				  glist.add(eg);
				  double[] ords = eg.getOrdinatesArray();
				  if (ords != null)
					  ords = eg.getFirstPoint();
				  totalords = ords.length;
			  }
		  }
		  else
		  {
			  double[] ords = g.getOrdinatesArray();
			  if (ords != null)
				  ords = g.getFirstPoint();
			  glist.add(g);
			  totalords = ords.length;
		  }
	  }
	  int[] elemInfo = new int[glist.size()*3];
	  double[] ordinates = new double[totalords];
	  int elempos = 0;
	  int ordpos = 0;
	  for (Geometry g : glist)
	  {
		  int[] srcelem = g.getElemInfo();
		  System.arraycopy(srcelem, 0, elemInfo, elempos, 3);
		  elemInfo[elempos] = ordpos;
		  double[] ords = g.getOrdinatesArray();
		  if (ords != null)
			  ords = g.getFirstPoint();
		  System.arraycopy(ords, 0, ordinates, ordpos, ords.length);
		  elempos += 3;
		  ordpos = ords.length;
	  }
	  return createGeometry(JGeometry.GTYPE_COLLECTION, srid, elemInfo, ordinates);
  }
  
  /**
   * Generic method for constructing arbitrary geometry objects.
   *
   * @param gtype  the geometry type
   * @param srid  the srid of the geometry
   * @param elemInfo   geometry element info array
   * @param ordinates  geometry ordinates array
   */
  public static Geometry createGeometry(int gtype, int[] elemInfo, double[] ordinates)
  {
    return createGeometry(gtype, SpatialCartridge.getContextSRID(null), elemInfo, ordinates);
  }
  
  /**
   * Generic method for constructing arbitrary geometry objects.
   *
   * @param gtype  the geometry type
   * @param srid  the srid of the geometry
   * @param elemInfo   geometry element info array
   * @param ordinates  geometry ordinates array
   */
  public static Geometry createGeometry(int gtype, int srid, int[] elemInfo, double[] ordinates)
  {
    return new Geometry(gtype, srid, elemInfo, ordinates);
  }  

  /**
   * Creates a single element info from the given arguments.
   *
   * @param soffset   the starting offset within the ordinates array
   * @param etype     the type of the element
   * @param interp    the interpretation
   * @return element info of an int array
   */
  public static int[] createElemInfo(int soffset, int etype, int interp)
  {
    int[] elemInfo = new int[3];
    elemInfo[0] = soffset;
    elemInfo[1] = etype;
    elemInfo[2] = interp;
    return elemInfo;
  }

  /**
   * Gets the MBR of the given geometry
   *
   * @param geom  the geometry
   * @return a two dimensional double array of [0][0]=minX, [0][1]=maxX, [1][0]=minY, [1][1]=maxY
   */
  public static double[][] get2dMbr(JGeometry geom)
  {
    double[][] mbr = new double[2][2];
    double[] mbrArray = geom.getMBR();
    if (geom.getDimensions() == 2)
    {
      mbr[0][0] = mbrArray[0];
      mbr[0][1] = mbrArray[2];
      mbr[1][0] = mbrArray[1];
      mbr[1][1] = mbrArray[3];
    } else
    {
      mbr[0][0] = mbrArray[0];
      mbr[0][1] = mbrArray[3];
      mbr[1][0] = mbrArray[1];
      mbr[1][1] = mbrArray[4];
    }

    return mbr;
  }

  /** 
   * Generates a new Geometry object which is the buffered 
   * version of the input geometry.
   * 
   * This takes the bufferWidth as the parameter and this bufferWidth is
   * assumed to be in the same unit as the Unit of Projection for projected
   * geometry. If the geometry is geodetic, this buffer width should be in meters.
   * This method picks up smx, flat, and arcT parameters from spatial context.
   *
   * @param bufferWidth is the distance value used for buffer
   * @return a Geometry object
   */
  public static Geometry bufferPolygon(JGeometry polygon, double distance) 
  {
    GeodeticParam geoParam = SpatialCartridge.getContextGeodeticParam(null);
    Geometry g = bufferPolygon(geoParam, polygon, distance);
    return g;
  }
 
  public static Geometry bufferPolygon(GeodeticParam geoParam, JGeometry polygon, double distance) 
  {
    if (geoParam != null && geoParam.getSRID() != polygon.getSRID()) 
    {
      String msg = SpatialCartridgeLogger.BufferingErrorSRID();
      throw new IllegalStateException(msg);
    }
          
    JGeometry bufferedPolygon = null;
    try 
    {
      double arctol = geoParam.isCartesian() ? distance / 10.0 : geoParam.getArcTol();
      long stime;
      if (DEBUG_BUFFER)
      {
   		  stime = System.currentTimeMillis();
	      System.out.println("begin buffer: "+arctol + ", size:"+polygon.getSize());	
	      if (polygon.getSize()>50000)
	      {
	      	Geometry g = to_Geometry(polygon);
	      	System.out.println(g.toJsonString());
	      }
      }
      bufferedPolygon =  polygon.buffer(distance, geoParam.getSMA(), geoParam.getROF(), arctol);
      if (bufferedPolygon == null)
      {
    	  Geometry g = to_Geometry(polygon);
    	  log.warn("Failed to buffer polygon : distance=" + distance + ", sma=" + geoParam.getSMA() + ", rof="+ geoParam.getROF() + ", arctol=" +arctol 
    			  +g.toJsonString());
    	  bufferedPolygon = polygon;
      }
      if (DEBUG_BUFFER)
      {
  		long etime = System.currentTimeMillis();
  		System.out.println("end buffer:"+ (etime-stime)/1000 + " seconds");	
      }      
      // NOTE No longer need to densifyArcs if cartesian, this has been fixed with the latest API.
    } 
    catch(Exception e) 
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      
      String msg = SpatialCartridgeLogger.BufferingErrorUnknown();
      msg += "\n";
      msg += e.getMessage();
      throw new IllegalStateException(msg, e);
    }
    
	
    return to_Geometry(bufferedPolygon);
  }
  
  /** 
   * Calculates the distance from two 2d geometries.
   * This method picks up smx, flat, and arcT parameters from spatial context.
   *
   * @param geom  other geometry 
   * @return the distance in double
   */
  public static double distance(Geometry g1, Geometry g2)
    throws Exception
  {
	  GeodeticParam geoParam = SpatialCartridge.getContextGeodeticParam(null);
	  return distance(geoParam, g1, g2);
  }
	  
  /** 
   * Calculates the distance from two 2d geometries.
   *
   * @param geom  other geometry 
   * @return the distance in double
   */
  public static double distance(GeodeticParam geoParam, Geometry g1, Geometry g2)
  throws Exception
  {
    return distance(g1, g2, geoParam.getTol(), geoParam.getSMA(), geoParam.getROF(), 
        geoParam.isCartesian());
  }
  
  /**
   * Calculates the distance from two 2d geometries.
   * 
   * @param g1
   * @param g2
   * @param tolerance
   * @param sma
   * @param rof
   * @param isCartesian
   * @return
   * @throws Exception
   */
  public static double distance(JGeometry g1, JGeometry g2, Double tolerance, Double sma, 
      Double rof, boolean isCartesian)
  throws Exception
  {
    // All geometries that have arcs must use a non-zero tolerance.
    // Ideally, the spatial API would check this, but currently it doesn't and simply returns false always.
    if ((g1.hasCircularArcs() || g2.hasCircularArcs()) && tolerance == 0)
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.ZeroToleranceForArcs());
    }
    
    if ((g1.getDimensions() == 3 && g1.hasCircularArcs()) || 
        (g2.getDimensions() == 3 && g2.hasCircularArcs()))
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.Compound3DGeometriesNotSupported("distance"));
    }
    
    // NOTE Only need to 'projectToLTP' for geodetic 3D geometries, and not for geodetic 2D geoms...
    if (!isCartesian && (g1.getDimensions() == 3 || g2.getDimensions() == 3))
    {
    	/*
      JGeometry geom2d = JGeometry.make_2d(g1, true, 0);
      JGeometry key2d = JGeometry.make_2d(g2, true, 0);
      
      double[] geomord = geom2d.getOrdinatesArray();
      double[] keyord = key2d.getOrdinatesArray();
      double[] xycol = new double[geomord.length + keyord.length];
      int p = 0;
      int i;
      for (i = 0; i < keyord.length; i++)
        xycol[p++] = keyord[i];
      for (i = 0; i < geomord.length; i++)
        xycol[p++] = geomord[i];
  
      p = 0;
      int[] elemInfo = new int[6];
      int[] keleminfo = key2d.getElemInfo();
      for (i = 0; i < keleminfo.length; i++)
        elemInfo[p++] = keleminfo[i];
      int[] geleminfo = geom2d.getElemInfo();
      for (i = 0; i < geleminfo.length; i++)
        elemInfo[p++] = geleminfo[i];
      elemInfo[3] = keyord.length / 2 + 1;
  
      // Two geometries should be projected to LTP at the same time.
      JGeometry col2d = new JGeometry(JGeometry.GTYPE_COLLECTION, g1.getSRID(), elemInfo, xycol);
      JGeometry colltp = col2d.projectToLTP(sma, 1.0/rof);
      p=0;
      xycol = colltp.getOrdinatesArray();
      for (i = 0; i < keyord.length; i++)
        keyord[i] = xycol[p++];
      for (i = 0; i < geomord.length; i++)
        geomord[i] = xycol[p++];
  
      JGeometry gltp = new JGeometry(geom2d.getType(), colltp.getSRID(), geom2d.getElemInfo(), geomord);
      JGeometry tltp = new JGeometry(key2d.getType(), colltp.getSRID(), key2d.getElemInfo(), keyord);
      
      return gltp.distance(tltp, tolerance, "FALSE");
      */
        return g1.distance(g2, tolerance, isCartesian ? "FALSE" : "TRUE");
    } 
    else 
    {  
      // Third arg is 'isGeodetic', therefore simply negate isCartesian
      return g1.distance(g2, tolerance, isCartesian ? "FALSE" : "TRUE");
    }
  }
  
  /**
   * Returns true if geometry {@code geom1} is inside of geometry {@code geom2}.
   * A geometry is only considered to be inside if it does not touch the boundary of the containing geometry.
   * 
   * The spatial parameters are picked up from the spatial context.
   * 
   * @param geom1 - contained geometry
   * @param geom2 - containing geometry
   *
   * @return true if inside geom1 is inside geom2.
   * 
   * @throws Exception
   */
  public static boolean isInside(JGeometry geom1, JGeometry geom2) 
    throws Exception
  {
    GeodeticParam geoParam = SpatialCartridge.getContextGeodeticParam(null);
    return isInside(geom1, geom2, geoParam.getTol(), geoParam.getSMA(), geoParam.getROF(), 
        geoParam.isCartesian());
  }
  
  /**
   * Returns true if geometry {@code geom1} is inside of geometry {@code geom2}.
   * A geometry is only considered to be inside if it does not touch the boundary of the containing geometry.
   * 
   * @param geom1 - contained geometry
   * @param geom2 - containing geometry
   * @param tolerance - tolerance value
   * @param isCartesian - true if geometries are using the cartesian plane.
   * @return true if inside geom1 is inside geom2.
   * @throws Exception
   */
  public static boolean isInside(JGeometry geom1, JGeometry geom2, double tolerance, 
      double sma, double rof, boolean isCartesian) 
    throws Exception
  {
    // All geometries that have arcs must use a non-zero tolerance.
    // Ideally, the spatial API would check this, but currently it doesn't and simply returns false always.
    if ((geom1.hasCircularArcs() || geom2.hasCircularArcs()) && tolerance == 0)
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.ZeroToleranceForArcs());
    }
    
    if ((geom1.getDimensions() == 3 && geom1.hasCircularArcs()) || 
        (geom2.getDimensions() == 3 && geom2.hasCircularArcs()))
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.Compound3DGeometriesNotSupported("isInside"));
    }
    
    // NOTE Only need to 'projectToLTP' for geodetic 3D geometries, and not for geodetic 2D geoms...
    if (!isCartesian && (geom1.getDimensions() == 3 || geom2.getDimensions() == 3))
    {
      JGeometry geom2d = JGeometry.make_2d(geom1, true, 0);
      JGeometry key2d = JGeometry.make_2d(geom2, true, 0);
      
      double[] geomord = geom2d.getOrdinatesArray();
      double[] keyord = key2d.getOrdinatesArray();
      double[] xycol = new double[geomord.length + keyord.length];
      int p = 0;
      int i;
      for (i = 0; i < keyord.length; i++)
        xycol[p++] = keyord[i];
      for (i = 0; i < geomord.length; i++)
        xycol[p++] = geomord[i];
  
      p = 0;
      int[] elemInfo = new int[6];
      int[] keleminfo = key2d.getElemInfo();
      for (i = 0; i < keleminfo.length; i++)
        elemInfo[p++] = keleminfo[i];
      int[] geleminfo = geom2d.getElemInfo();
      for (i = 0; i < geleminfo.length; i++)
        elemInfo[p++] = geleminfo[i];
      elemInfo[3] = keyord.length / 2 + 1;
  
      // Two geometries should be projected to LTP at the same time.
      JGeometry col2d = new JGeometry(JGeometry.GTYPE_COLLECTION, geom1.getSRID(), elemInfo, xycol);
      JGeometry colltp = col2d.projectToLTP(sma, 1.0/rof);
      p=0;
      xycol = colltp.getOrdinatesArray();
      for (i = 0; i < keyord.length; i++)
        keyord[i] = xycol[p++];
      for (i = 0; i < geomord.length; i++)
        geomord[i] = xycol[p++];
  
      JGeometry gltp = new JGeometry(geom2d.getType(), colltp.getSRID(), geom2d.getElemInfo(), geomord);
      JGeometry tltp = new JGeometry(key2d.getType(), colltp.getSRID(), key2d.getElemInfo(), keyord);
      
      return gltp.isInside(tltp, tolerance, "FALSE");
    }
    else
      return geom1.isInside(geom2, tolerance, isCartesian ? "FALSE" : "TRUE");
  }
  
  public static Geometry densify(JGeometry g, double tol)
  {
	try {
		JGeometry dg;
		if (tol < 1.0)
			dg = g.densifyGeodesic();
		else
			dg = g.densifyGeodesic(tol);
		return to_Geometry(dg);
	} catch (Exception e) {
		log.error("Failed to densify with "+ tol);
		e.printStackTrace();
	}
	return to_Geometry(g);
  }

  public static Geometry simplify(JGeometry geom, double vertexThreshold)
  {
	boolean taller_triangles = true;
	double M=1;
	double N=0;
	double KS=1;
	double KH=1;
	double SM=1;
	double SK=1;
	JGeometry dg = null;
	final int MAX_ITERATION = 1;
	for (int i = 0; i < MAX_ITERATION; i++)
	{
		try {
			if (geom.getNumPoints() < 30 && vertexThreshold > 50)
			{
				//if the polygon is not big enough
				vertexThreshold = 40;
//	log.info("small polygon " + to_Geometry(geom).toJsonString());			
			}
			dg = JGeometry.simplifyVW(geom, vertexThreshold, taller_triangles, M, N, KS, KH, SM, SK,
					GeodeticParam.WGS84_SMA, GeodeticParam.WGS84_ROF);
			if (dg.getType () == geom.getType())
			{
				return to_Geometry(dg);
			}
			if (dg.getType() == JGeometry.GTYPE_COLLECTION)
			{
				JGeometry[] elems = dg.getElements();
				if (elems == null || elems.length == 0)
				{
					dg = null;
				}
				else 
				{
					int maxc = 0;
					JGeometry maxg = null;
					//choose the biggest one with the same type
					for (int j = 0; j < elems.length; j++)
					{
						dg = elems[j];
						if (dg.getType() == geom.getType())
						{
							if (maxc < dg.getNumPoints())
							{
								maxc = dg.getNumPoints();
								maxg = dg;
							}
						}
					}
					if (maxg != null)
					{
						return to_Geometry(maxg);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to simplify with "+vertexThreshold);
			e.printStackTrace();
		}
//		log.info(i + " : " + vertexThreshold + " " + to_Geometry(dg).toJsonString());			
		//try again with half threshold
		vertexThreshold -= vertexThreshold / 4;
	}
	if (geom.getType() == JGeometry.GTYPE_POLYGON)
	{
		//if the result is still not good, just use the original one.
	//	log.info(vertexThreshold + " still not good : use mbr" + geom.getNumPoints() + " pts, " + to_Geometry(geom).toJsonString());
		//use boundingbox
	    double[] mbrArray = geom.getMBR();
	    Geometry mbr=Geometry.createRectangle(geom.getSRID(), mbrArray[0], mbrArray[1], mbrArray[2], mbrArray[3]);
	 //   log.info(mbr.toJsonString());
	    return mbr;
	}
	return to_Geometry(geom);
  }
  
  public static Geometry simplifyBufferPolygon(JGeometry polygon, double distance, double threshold) 
  {
	  Geometry g = simplify(polygon, threshold);
	  return bufferPolygon(g, distance);
  }
  
  /**
   * Converts Geometry type to JGeometry
   * Given geometry is converted to 2d geometry.
   *
   * @param geom  the geometry
   * @return a JGeometry object
   */
  public static JGeometry to_JGeometry(Geometry g)
  {
	  return g;
  }

  /**
   * Converts JGeometry type to Geometry
   * If the given geometry is already a Geometry type no conversion will be done.
   * Otherwise, the given geometry is converted to Geometry by constructing a new Geometry with 
   *   the same information.
   *
   * @param geom  the geometry
   * @return a Geometry object
 */
  public static Geometry to_Geometry(JGeometry geom)
  {
	  if (geom instanceof Geometry)
	  {
		  return (Geometry) geom;
	  }
	  if (geom.getElemInfo() == null)
	  {
		  double[] p = geom.getPoint();
		  if (geom.isPoint())
			  return Geometry.createPoint(geom.getSRID(), p[0],p[1]);
		  else if (geom.isRectangle())
			  return Geometry.createRectangle(geom.getSRID(), p[0], p[1], p[2], p[3]);
	  }
	  double[] ords = geom.getOrdinatesArray();
	  if (ords == null) ords = geom.getPoint();
	  return new Geometry(geom.getType(), geom.getSRID(), geom.getElemInfo(), ords);
  }
  
  public static String get2dMbrStr(JGeometry geom)
  {
    double[][] mbr = get2dMbr(geom);
    return mbr[0][0] + "," + mbr[0][1] + " "
        + mbr[1][0] + "," + mbr[1][1];
  }  

  public static final boolean compareElemInfo(int[] eleminfo, int[] eleminfo2)
  {
	  return (eleminfo[0] == eleminfo2[0] &&
			  eleminfo[1] == eleminfo2[1] &&
			  eleminfo[2] == eleminfo2[2]);
	  
  }
  
  public static boolean isRectangle(int gtype, int[] elemInfo)
  {
    if(elemInfo==null) return false;
    return (gtype)==GTYPE_POLYGON &&
           (elemInfo[1]%100)==3 && elemInfo[2]==3 && elemInfo.length==3;
  }

  public static boolean isCircle(int gtype, int[] elemInfo)
  {
    if(elemInfo==null) return false;
    return (gtype)==GTYPE_POLYGON &&
           (elemInfo[1]%100)==3 && elemInfo[2]==4 && elemInfo.length==3;
  }
  
  public GeomType getGeomType()
  {
	  return getGeomType(getType(), getElemInfo());
  }
  
  public String getTypeName()
  {
	  return getTypeName(getType(), getElemInfo());
  }

  public static GeomType getGeomType(int type, int[] elemInfo)
  {
	  if (type == GTYPE_POLYGON)
	  {
		  if (isRectangle(type, elemInfo))
			  return GeomType.Rectangle;
		  if (isCircle(type, elemInfo))
			  return GeomType.Circle;
		  if (elemInfo.length > 3)
		  {
			  //JGeometry never sets multipolygon type
			  return GeomType.MultiPolygon;
		  }
	  }
	  return s_typeNameMap.get(type);
  }
  
  public static String getTypeName(int gtype, int[] elemInfo)
  {
	  GeomType type = getGeomType(gtype, elemInfo);
	  if (type != null) return type.name();
	  return Integer.toString(gtype);
  }

  public String toString()
  {
	  StringBuilder b = new StringBuilder();
	  b.append("type="); b.append(getTypeName());
	  b.append(", elem="); b.append(Arrays.toString(getElemInfo()));
	  b.append(", srid="); b.append(this.getSRID());
	  b.append(", dim="); b.append(this.getDimensions());
	  //b.append(", nopoints="); b.append(this.getNumPoints());
      double[] coords = getOrdinatesArray();
      if (coords == null)  coords = getPoint();
      b.append(", nopoints="); b.append(coords==null? 0: (coords.length/this.getDimensions()));
      //b.append(getCoordsStr(getDimensions(), coords, false));
	  return b.toString();
  }
  
  public String toCsvString()
  {
	  String typeName = getTypeName(); 
	  int dim = getDimensions();
	  int srid = getSRID();
      double[] coords = getOrdinatesArray();
      if (coords == null)  coords = getPoint();
      return toCsvString(typeName, srid, dim, coords, true);  
  }
  
  public static String toCsvString(String typeName, int srid, int dim, double[] coords, boolean needQuote)
  {
	  StringBuilder b = new StringBuilder();
	  if (needQuote) b.append("\"");
	  b.append(typeName);
	  b.append(","); 
	  b.append(srid);
	  b.append(","); 
	  b.append(dim);
	  b.append(",");
	  b.append(getCoordsStr(dim, coords, false));
	  if (needQuote) b.append("\"");
	  return b.toString();
  }

  private static void quote(StringBuilder b, String v)
  {
      b.append("\"");
      b.append(v);
      b.append("\"");
  }
  
  private static void nameValue(StringBuilder b, String n, String v, boolean needQuote)
  {
      quote(b, n);
      b.append(":");
      if (!needQuote) b.append(v);
      else quote(b, v);
  }
  
  private String getJsonStringForCollection(GeomType geomType, int srid, int dim, int precision)
  {
	  String typeName = geomType.toString();
	  StringBuilder b = new StringBuilder();
	  b.append("{");
	  nameValue(b, JSON_TAG_TYPE, typeName, true);
	  b.append(",");
	  nameValue(b, JSON_TAG_SRID, Integer.toString(srid), true);
	  b.append(",");
	  quote(b, "geometries");
	  b.append(": [");
	  JGeometry[] elems = getElements();
	  int i = 0;
	  for (JGeometry elem : elems)
	  {
		  if (i > 0) b.append(",\n");
		  GeomType elemtype = getGeomType(elem.getType(), elem.getElemInfo()); 
		  int elemdim = getDimensions();
		  int elemsrid = getSRID();
		  double[] coords = elem.getOrdinatesArray();
		  int[] elemInfo = elem.getElemInfo();
		  b.append(toJsonString(elemtype, elemsrid, elemdim, elemInfo, coords, precision));
		  i++;
	  }
	  b.append("]");
	  b.append("}");
	  return b.toString();
  }
  
  public static String toJsonString(GeomType geomType, int srid, int dim, int[] elemInfo, double[] coords, int precision)
  {
	  String typeName = geomType.toString();
	  StringBuilder b = new StringBuilder();
	  b.append("{");
	  nameValue(b, JSON_TAG_TYPE, typeName, true);
	  b.append(",");
	  nameValue(b, JSON_TAG_SRID, Integer.toString(srid), true);
	  b.append(",");
	  String coordsStr = null;
	  boolean needQuote = (precision != 0);
      quote(b, JSON_TAG_COORDS);
      b.append(":");
      if (coords != null)
      {
    	  boolean multi = (elemInfo.length > 3);
    	  if (multi) b.append("[ ");
    	  for (int einfopos = 0; einfopos < elemInfo.length; einfopos+=3)
    	  {
    		  if (einfopos > 0) b.append(", ");
    		  int cstart = elemInfo[einfopos];
    		  int cend = (einfopos+3) >= elemInfo.length ? coords.length+1 : elemInfo[einfopos+3];
    		  cstart--; cend--;
	    	  if (precision == 0)
	    	  {
	    	      StringBuilder c = new StringBuilder();
	    		  c.append("[");
			      int dimcnt = 0;
			      for (int i = cstart; i < cend; i++)
			      {
			        if (dimcnt++ == dim) 
			        {
			            c.append("]");
			            dimcnt = 1;
			        }
			        if (i > cstart) c.append(",");
			        if (dimcnt == 1) c.append("[");
			        c.append(Double.toString(coords[i]));
			      }
			      c.append("]");
	    		  c.append("]");
	    		  coordsStr = c.toString();
	    	  } else {
	    	      //we don't need to encode the last points for polygons for the polyline.
	    	      cend = (geomType == GeomType.Polygon) ? cend - dim : cend;
	    		  coordsStr = JsonUtil.encode(coords, cstart, cend, precision);
	    	  }
	          if (!needQuote) b.append(coordsStr);
	          else quote(b, coordsStr);
    	  }
    	  if (multi) b.append(" ]");
      }
	  b.append("}");
	  return b.toString();
  }

  public String toJsonString()
  {
	  return toJsonString(0);
  }
  

  public String toJsonString(int precision)
  {
	  GeomType geomType = getGeomType(); 
	  int dim = getDimensions();
	  int srid = getSRID();
	  if (getType() == JGeometry.GTYPE_COLLECTION)
	  {
		  return getJsonStringForCollection(geomType, srid, dim, precision);
	  }
      double[] coords = getOrdinatesArray();
      if (coords == null)  coords = getPoint();
      return toJsonString(geomType, srid, dim, getElemInfo(), coords, precision);
  }
  
  public static String getCoordsStr(int dim, double[] coords, boolean endmark)
  {
    StringBuilder b = new StringBuilder();
    if (coords != null)
    {
	    for (int i = 0; i < coords.length; i++)
	    {
	      if (i > 0) b.append(",");
	      b.append(Double.toString(coords[i]));
	    }
	
	    if (endmark)
	    {
	      b.append(dim == 2 ? "0,0" : "0,0,0");
	    }
    }
    return b.toString();
  }
  
  static double[] paddTo3D(double[] twoD)
  {
    double[] threeD = new double[twoD.length + (twoD.length / 2)];
    int idx3 = 0;
    for (int idx2 = 0; idx2 < twoD.length; idx2 += 2)
    {
      threeD[idx3++] = twoD[idx2];
      threeD[idx3++] = twoD[idx2 + 1];
      threeD[idx3++] = 0;
    }
    return threeD;
  }
  
  /**
   * find the MBR of a collection of MBRs
   * @param mbrs -list of MBR, where each MBR is an array of min position (array of min longitude , min latitude) & max position (array of max longitude, max latitude)
   * @return - MBR, an array of min position (array of min longitude, min latitude) & max position (array of max longitude, max latitude)
   */
  public static double[][] getMBRofMBRs(List<double[][]> mbrs) {
		Iterator<double[][]> iter = mbrs.iterator();
		if (!iter.hasNext()){
			log.warn("mbr list is empty size:" + mbrs.size());
			return null;
		}
		double[][] mbr = iter.next();
		double m_minLng = mbr[0][0];
		double m_minLat = mbr[0][1];
		double m_maxLng = mbr[1][0];
		double m_maxLat = mbr[1][1];
		if(log.isDebugEnabled()){
			log.debug("MBR - minLng," + m_minLng + " maxLng," + m_maxLng + " minLat," + m_minLat + " maxLat," + m_maxLat);
		}
		
		while (iter.hasNext()) {
			double[][] mbr1 = iter.next();
			if(log.isDebugEnabled()){
				log.debug("MBR - minLng," + mbr1[0][0] + " minLat," + mbr1[0][1] + " maxLng," + mbr1[1][0] + " maxLat," + mbr1[1][1]);
			}
			if (mbr1[0][0] < m_minLng)
				m_minLng = mbr1[0][0];
			if (mbr1[0][1] < m_minLat)
				m_minLat = mbr1[0][1];
			if (mbr1[1][0] > m_maxLng)
				m_maxLng = mbr1[1][0];
			if (mbr1[1][1] > m_maxLat)
				m_maxLat = mbr1[1][1];
		}
		if(log.isDebugEnabled()){
			log.debug("MBRofMBR- minLng," + m_minLng + " maxLng," + m_maxLng + " minLat," + m_minLat + " maxLat," + m_maxLat);
		}
		return new double[][] { { m_minLng, m_minLat }, { m_maxLng, m_maxLat } };
	}
  
  /**
   * find the boundary(mbr) of a list of geometries (mbrs). for geodetic only.
   * @param mbrs - list of MBR, where each MBR is an array of min position (array of min longitude , min latitude) & max position (array of max longitude, max latitude)
   * @param buffer - widen the boundary by buffer distance 
   * @return - MBR of boundary - an array of min position (array of min longitude, min latitude) & max position (array of max longitude, max latitude)
   * @throws SQLException
   * @throws Exception
   */
  public static double[][] getMBRBoundary(List<double[][]> mbrs, double buffer) throws Exception{
	  	double[][] smbr = getMBRofMBRs(mbrs);
		Geometry rect = Geometry.createRectangle(smbr[0][0], smbr[0][1], smbr[1][0], smbr[1][1]);
		JGeometry boundary = rect.buffer(buffer,GeodeticParam.WGS84_SMA,GeodeticParam.WGS84_ROF,GeodeticParam.WGS84_TOL);
		double[][] m = Geometry.get2dMbr(boundary);
		return new double[][]{{m[0][0],m[1][0]},{m[0][1],m[1][1]}};
  }
  
  
  /**
   * calculate MBR of the given geometry and then apply the buffer for the given buffer size (distance) on the MBR rectangle. 
   * @param geom - geometry
   * @param buffer - distance
   * @return - rectangle geometry
   * @throws Exception
   */
  public static JGeometry bufferedMBR(Geometry geom, double buffer) throws Exception {
	  double[][] mbr = get2dMbr(geom);
	  Geometry rect = Geometry.createRectangle(mbr[0][0], mbr[1][0], mbr[0][1], mbr[1][1]);
	  JGeometry g =  rect.buffer(buffer, GeodeticParam.WGS84_SMA, GeodeticParam.WGS84_ROF, GeodeticParam.WGS84_TOL);
      mbr = get2dMbr(g);
      return Geometry.createRectangle(mbr[0][0], mbr[1][0], mbr[0][1], mbr[1][1]);
  }

  public boolean compareTo(Geometry other)
  {
	 if (getSRID() != other.getSRID()) return false;
	 if (getDimensions() != other.getDimensions()) return false;
	 double[] expords = other.getOrdinatesArray();
	 if (expords != null && !other.isCircle() && !isCircle())
	 {
		double[] ords = getOrdinatesArray();
		if (expords.length != ords.length) return false;
		for (int i = 0; i < ords.length; i++)
		{
			if (Math.abs(expords[i]-ords[i]) > 0.0000001)
			{
				return false;
			}
		}
	 }
	 return true;
  }

  public int hashCode()
  {
	  int[] hs = new int[7];
	  hs[0] = this.getType();
	  hs[1] = this.getDimensions();
	  hs[2] = this.getNumPoints();
	  hs[3] = this.getSRID();
	  int[] eleminfo = this.getElemInfo();
	  hs[4] = Arrays.hashCode(eleminfo);
	  double[] pts = this.getPoint();
	  hs[5] = pts == null ? 0 : Arrays.hashCode(pts);
	  pts = this.getOrdinatesArray();
	  hs[6] = pts == null ? 0 : Arrays.hashCode(pts);
	  return Arrays.hashCode(hs);
  }

  public static void writeObject(ObjectOutput out, JGeometry geom) throws IOException
  {
      int srid = geom.getSRID();
      out.writeInt(srid);
      int typ = geom.getType();
      out.writeInt(typ);
      int dim = geom.getDimensions();
      out.writeInt(dim);
      int[] eleminfo = geom.getElemInfo();
      out.writeObject(eleminfo);
      double[] pts = geom.getOrdinatesArray();
      out.writeObject(pts);
  }

  public static JGeometry readObject(ObjectInput in) throws IOException,
  ClassNotFoundException
  {
      int srid = in.readInt();
      int typ = in.readInt();
      int dim = in.readInt();
      int[] eleminfo = (int[])in.readObject();
      double[] pts = (double[])in.readObject();
      return (dim == 2) ? new JGeometry(typ, srid, eleminfo, pts) :
          new J3D_Geometry(typ, srid, eleminfo, pts);
  }

/*
TODO_JTS enable it after third party license is approved

  private com.vividsolutions.jts.geom.Geometry toJtsGeometry(GeometryFactory fac)
  {
	  if (getType() != JGeometry.GTYPE_POLYGON || getDimensions() != 2)
	  {
		  return null;
	  }
	  double[] pts = this.getOrdinatesArray();
	  Coordinate[] coords = new Coordinate[pts.length / 2];
	  int i = 0;
	  int pos = 0;
	  while(i < pts.length)
	  {
		  coords[pos++] = new Coordinate(pts[i], pts[i+1]);
		  i+=2;
	  }
	  return fac.createPolygon(coords);
  }
  
  private static Geometry fromJtsGeometry(com.vividsolutions.jts.geom.Geometry g)
  {
	  if (g instanceof Polygon)
	  {
		  g = ((Polygon)g).getExteriorRing();
	  }
	  Coordinate[] coords = g.getCoordinates();
	  double[] pts = new double[coords.length*2];
	  int i = 0;
	  for (Coordinate c : coords)
	  {
		  pts[i++] = c.x;
		  pts[i++] = c. y;
	  }
	  return Geometry.createLinearPolygon(GeodeticParam.LAT_LNG_WGS84_SRID, pts);
  }
  
  public static Geometry union(Collection<Geometry> geoms)
  {
	  GeometryFactory factory = new GeometryFactory(new PrecisionModel(), GeodeticParam.LAT_LNG_WGS84_SRID);
	  List<com.vividsolutions.jts.geom.Geometry> geomColl = new ArrayList<com.vividsolutions.jts.geom.Geometry>();
	  for (Geometry g : geoms)
	  {
		  com.vividsolutions.jts.geom.Geometry jg = g.toJtsGeometry(factory);
		  if (jg != null)
		  {
			  geomColl.add(jg);
		  }
	  }
	  GeometryCollection geometryCollection = (GeometryCollection) factory.buildGeometry(geomColl);
	  com.vividsolutions.jts.geom.Geometry merged = geometryCollection.union();
	  return fromJtsGeometry(merged);
  }

  public static Geometry union(Geometry g1, Geometry g2)
  {
	  GeometryFactory factory = new GeometryFactory(new PrecisionModel(), GeodeticParam.LAT_LNG_WGS84_SRID);
	  List<com.vividsolutions.jts.geom.Geometry> geomColl = new ArrayList<com.vividsolutions.jts.geom.Geometry>();
	  com.vividsolutions.jts.geom.Geometry jg = g1.toJtsGeometry(factory);
	  geomColl.add(jg);
	  com.vividsolutions.jts.geom.Geometry jg2 = g2.toJtsGeometry(factory);
	  geomColl.add(jg2);
	  GeometryCollection geometryCollection = (GeometryCollection) factory.buildGeometry(geomColl);
	  com.vividsolutions.jts.geom.Geometry jgm = geometryCollection.buffer(0);
	  //com.vividsolutions.jts.geom.Geometry jgm = jg.union(jg2);
	  return fromJtsGeometry(jgm);
  }
*/
}
