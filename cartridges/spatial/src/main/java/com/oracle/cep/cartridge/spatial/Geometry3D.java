/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/Geometry3D.java /main/3 2015/10/01 22:29:47 hopark Exp $ */

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
 hopark      09/28/10 - add IllegalStateException if geom SRID is different from param SRID
 hopark      09/11/09 - Creation
 */
package com.oracle.cep.cartridge.spatial;

import com.oracle.cep.cartridge.spatial.Geometry.GeomType;

import oracle.cep.common.Datatype;

import oracle.spatial.geometry.J3D_Geometry;
import oracle.spatial.geometry.JGeometry;

/**
* Geometry is the class representing geometry types.
* It extends oracle.spatial.geometry.J3D_Geometry and supports spatial contexts.
* All geometries created using Geometry methods are in 3d.
*
* @version %I%, %G%
* @since   12.1.2.0.0
*/
public class Geometry3D extends J3D_Geometry
{
  private static final long serialVersionUID = 4975952011696068363L;

  private static final int GTYPE_3D = 3000;
  private static final int DIMS_3D = 3;
  
  final static int[] SOLIDBOX_SD_ELEMENT_INFO = new int[] {1,1007,3};
  
  public Geometry3D()
  {
	  super(0, 0, (int[])null, (double[])null);
  }
  
  /*
   * The following constructor is not really necessary.
   * But it's required because J3D_Geometry does not define the default constructor.
   */
  Geometry3D(int gtype, double x, double y, double z)
  {
    super(gtype, SpatialCartridge.getContextSRID(null), x, y, z );
  }

  Geometry3D(int gtype, int[] elemInfo, double[] ordinates)
  {
    super(gtype, SpatialCartridge.getContextSRID(null), elemInfo, ordinates);
  }
  
  Geometry3D(int gtype, int srid, int[] elemInfo, double[] ordinates)
  {
    super(gtype, srid, elemInfo, ordinates);
  }  
  
  Geometry3D(String s) throws Exception
  {
    super(GeometryParser.getGtype(s), GeometryParser.getSRID(s), GeometryParser.getElemInfo(s), 
        GeometryParser.getOrdinates(s));  
  }
  
  public static Datatype getGeometry3DType()
  {
    if (Geometry.s_Geometry3DType != null)
      return Geometry.s_Geometry3DType;

    Geometry.getGeometryType();

    return Geometry.s_Geometry3DType;
  }
  
  public static boolean isGeometry3DType(Datatype type)
  {
    assert (type != null);
    
    Datatype Geometry3DType = getGeometry3DType();
    boolean b = Geometry3DType.isAssignableFrom(type);
    if (b) 
      return b;
    
    // check super classes since isAssignableFrom will be false for super classes.
    if (type.name().equals(Geometry.s_J3DGeometryType.name())) 
      return true;

    return false;
  }
  
  /**
   * Create a 3d geometry that is a point.
   * The srid is picked up from the spatial context.
   *  
   * @param x  the x coordinate of the lower left   
   * @param y  the y coordinate of the lower left
   * @param z  the z coordinate of the upper right
   * @return a 3d point type geometry
   */
  public static Geometry3D createPoint(double x, double y, double z)
  {
    return createPoint(SpatialCartridge.getContextSRID(null), x, y, z);    
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
  public static Geometry3D createPoint(int srid, double x, double y, double z)
  {
    double[] ords = new double[] {x,y,z};
    return new Geometry3D(GTYPE_POINT + GTYPE_3D, srid, Geometry.POINT_SD_ELEMENT_INFO, 
        ords);    
  }
  
  /**
   * Create a 3d geometry that is a rectangle.
   * The srid is picked up from the spatial context.
   * 
   * @param minx  the x coordinate of the lower left   
   * @param miny  the y coordinate of the lower left
   * @param minz  the z coordinate of the lower left
   * @param maxx  the x coordinate of the upper right
   * @param maxy  the y coordinate of the upper right
   * @param maxz  the z coordinate of the upper right
   * @return a 3d rectangle type geometry
   */
  public static Geometry createRectangle(double x1, double y1, double z1, double x2, double y2, double z2)
  {
    return createRectangle(SpatialCartridge.getContextSRID(null), x1, y1, z1, x2, y2, z2);  
  }
  
  /**
   * Create a 3d geometry that is a rectangle with the given srid.
   *  
   * @param srid  the srid of the geometry
   * @param minx  the x coordinate of the lower left   
   * @param miny  the y coordinate of the lower left
   * @param minz  the z coordinate of the lower left
   * @param maxx  the x coordinate of the upper right
   * @param maxy  the y coordinate of the upper right
   * @param maxz  the z coordinate of the upper right
   * @return a 3d rectangle type geometry
   */
  public static Geometry createRectangle(int srid, double x1, double y1, double z1, double x2, 
      double y2, double z2)
  {
    double[] ords = new double[] {x1, y1, z1, x2, y2, z2};
    return new Geometry(GTYPE_POLYGON + GTYPE_3D, srid, Geometry.RECT_SD_ELEMENT_INFO, ords);
  }
  
  /**
   * Create a 3d geometry that is a solid box.
   * The srid is picked up from the spatial context.
   * 
   * @param minx  the x coordinate of the lower left   
   * @param miny  the y coordinate of the lower left
   * @param minz  the z coordinate of the lower left
   * @param maxx  the x coordinate of the upper right
   * @param maxy  the y coordinate of the upper right
   * @param maxz  the z coordinate of the upper right
   * @return a 3d rectangle type geometry
   */
  public static Geometry3D createSolidBox(double x1, double y1, double z1, double x2, double y2, double z2)
  {
    return createSolidBox(SpatialCartridge.getContextSRID(null), x1, y1, z1, x2, y2, z2);  
  }
  
  /**
   * Create a 3d geometry that is a solid box with the given srid.
   *  
   * @param srid  the srid of the geometry
   * @param minx  the x coordinate of the lower left   
   * @param miny  the y coordinate of the lower left
   * @param minz  the z coordinate of the lower left
   * @param maxx  the x coordinate of the upper right
   * @param maxy  the y coordinate of the upper right
   * @param maxz  the z coordinate of the upper right
   * @return a 3d solid box type geometry
   */
  public static Geometry3D createSolidBox(int srid, double x1, double y1, double z1, double x2, 
      double y2, double z2)
  {
    double[] ords = new double[] {x1, y1, z1, x2, y2, z2};
    return new Geometry3D(GTYPE_SOLID + GTYPE_3D, srid, SOLIDBOX_SD_ELEMENT_INFO, ords);
  }  
  
  /**
   * Create a 3d geometry that is a linear line string.
   * A linear line string contains 1 or more pairs of points forming lines. It does not contain
   *  arcs.
   * The srid is picked up from the spatial context.
   * The result geometry has the element info of {1, 2, 1}
   * 
   * @param coords the coordinates of the linear line string
   * @param dim  the dimensionality of the given coordinates.
   * @return a 3d linear line string type geometry
   */
  public static Geometry3D createLinearLineString(double[] coords)
  {
    int srid = SpatialCartridge.getContextSRID(null);
    return createLinearLineString(srid, coords);
  }
  
  /**
   * Create a 3d geometry that is a linear line string with the given srid.
   * A linear line string contains 1 or more pairs of points forming lines. It does not contain
   *  arcs.
   * The result geometry has the element info of {1, 2, 1}
   *
   * @param srid  the srid of the geometry
   * @param coords the coordinates of the linear line string
   * @param dim  the dimensionality of the given coordinates.
   * @return a 3d linear line string type geometry
   */
  public static Geometry3D createLinearLineString(int srid, double[] coords)
  {
    JGeometry g = JGeometry.createLinearLineString(coords, DIMS_3D, srid);
    return to_Geometry3D(g);
  }
  
  
  /**
   * Creates a 3d geometry that is a simple linear polygon without holes.
   * Note that if the supplied coordinate array does not close itself, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of the input coordinates array. the new coordinate
   * repeats the first one.
   * The srid is picked up from the spatial context.
   *
   * @param coords  the coordinates of the linear polygon
   * @param dim the dimensionality of the coordinates
   * @return a 3d linear polygon geometry without holes
   */
  public static Geometry3D createLinearPolygon(double[] coords)
  {
    int srid = SpatialCartridge.getContextSRID(null);
    return createLinearPolygon(srid, coords);
  }
  
  /**
   * Creates a 3d geometry that is a simple linear polygon without holes.
   * Note that if the supplied coordinate array does not close itslef, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of the input coordinates array. the new coordinate
   * repeats the first one.
   *
   * @param srid the srid of the polygon
   * @param coords  the coordinates of the linear polygon
   * @param dim the dimensionality of the coordinates
   * @return a 3d linear polygon geometry without holes
   */
  public static Geometry3D createLinearPolygon(int srid, double[] coords)
  {
    JGeometry g = JGeometry.createLinearPolygon(coords, DIMS_3D, srid);
    return to_Geometry3D(g);
  }
  
  /**
   * Creates a 3d geometry that is a linear polygon which may have
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
   * @return a 3d linear polygon geometry with holes
   */
  public static Geometry3D createLinearPolygon(double[][] coords)
  {
    int srid = SpatialCartridge.getContextSRID(null);
    return createLinearPolygon(srid, coords);
  }
  
  /**
   * Creates a 3d geometry that is a linear polygon which may have
   * holes. Each array in the double array parameter represents a single ring
   * of the polygon. The outer ring must be the first in the double array.
   * Note that for each ring, if its coordinate array does not close itslef, meaning
   * the last coordinate is not the same as the first, a new coordinate will be
   * appended to the end of that ring's coordinates array. The new coordinate
   * repeats the first one for the said ring.
   *
   * @param srid  the srid of the polygon
   * @param coords  an array of double-typed arrays that contains
   *                all the rings' coordinates
   * @return a 3d linear polygon geometry with holes
   */
  public static Geometry3D createLinearPolygon(int srid, double[][] coords)
  {
    JGeometry g =  JGeometry.createLinearPolygon(coords, DIMS_3D, srid);
    return to_Geometry3D(g);
  }  
  
  /**
   * Constructs a 3d geometry with given information
   *
   * @param gtype  the geometry type
   * @param srid  the srid of the geometry
   * @param elemInfo   geometry element info array
   * @param ordinates  geometry ordinates array
   * @return an arbitrary 3D geometry
   */
  public static Geometry3D createGeometry(int gtype, int[] elemInfo, double[] ordinates)
  {
	  return createGeometry(gtype, SpatialCartridge.getContextSRID(null), elemInfo, ordinates);
  }
  
  /**
   * Constructs a 3d geometry with given information
   * The srid is picked up from the spatial context.
   *
   * @param gtype  the geometry type
   * @param srid  the srid of the geometry
   * @param elemInfo   geometry element info array
   * @param ordinates  geometry ordinates array
   * @return an arbitrary 3D geometry
   */
  public static Geometry3D createGeometry(int gtype, int srid, int[] elemInfo, double[] ordinates)
  {
    return new Geometry3D(gtype, srid, elemInfo, ordinates);
  }
  
  /** 
   * Generates a new Geometry object which is the buffered 
   * version of the input geometry.
   * 
   * This takes the bufferWidth as the parameter and this bufferWidth is
   * assumed to be in the same unit as the Unit of Projection for projected
   * geometry. If the geometry is geodetic, this buffer width should be in meters.
   * 
   * This method picks up smx, flat, and arcT parameters from spatial context.
   *
   * @param bufferWidth is the distance value used for buffer
   * @return a Geometry object
   */
  /*
  public static Geometry3D bufferPolygon(J3D_Geometry polygon, double distance) 
  {
    GeodeticParam geoParam = SpatialCartridge.getContextGeodeticParam(null);
    return bufferPolygon(geoParam, polygon, distance);
  }
  */
  /** 
   * Generates a new Geometry object which is the buffered 
   * version of the input geometry.
   * 
   * This takes the bufferWidth as the parameter and this bufferWidth is
   * assumed to be in the same unit as the Unit of Projection for projected
   * geometry. If the geometry is geodetic, this buffer width should be in meters.
   *
   * @param bufferWidth is the distance value used for buffer
   * @return a Geometry object
   */
  /*
  public static Geometry3D bufferPolygon(GeodeticParam geoParam, J3D_Geometry polygon, double distance) 
  {
    if (geoParam != null && geoParam.getSRID() != polygon.getSRID()) 
    {
      String msg = SpatialCartridgeLogger.BufferingErrorSRID();
      throw new IllegalStateException(msg);
    }
          
    JGeometry bufferedPolygon = null;
    try 
    {
      double arctol = geoParam.isCartesian() ? distance / 10.0 : 0.0d;
      bufferedPolygon =  polygon.buffer(distance, geoParam.getSMA(), geoParam.getROF(), arctol);
      
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
    
    return to_Geometry3D(bufferedPolygon);
  }
  */
  
  /**
   * Returns true if geometry {@code geom1} is inside the 3D space of the geometry {@code geom2}.
   * Geometry {@code geom2} must either be a solid, or geometry {@code geom1} must be a point and 
   *  geometry {@code geom2} must be a 3D polygon.
   * Geometries must be 3D and cartesian.
   * 
   * The tolerance parameter is picked up from the spatial context.
   * 
   * @param geom1 - contained geometry
   * @param geom2 - containing geometry
   * @return true if geom1 is inside the 3D space of geom2.
   * @throws Exception if not cartesian 3D, or if not a solid, or if not a point in a 3d polygon. 
   */
  public static boolean inside3d(J3D_Geometry geom1, J3D_Geometry geom2) throws Exception
  {
    GeodeticParam geoParam = SpatialCartridge.getContextGeodeticParam(null);
    return inside3d(geom1, geom2, geoParam.getTol(), geoParam.isCartesian());
  }
  
  /**
   * Returns true if geometry {@code geom1} is inside the 3D space of the geometry {@code geom2}.
   * Geometry {@code geom2} must either be a solid, or geometry {@code geom1} must be a point and 
   *  geometry {@code geom2} must be a 3D polygon.
   * Geometries must be 3D.
   * 
   * @param geom1 - contained geometry
   * @param geom2 - containing geometry
   * @param tolerance - tolerance used for operation
   * @return true if geom1 is inside the 3D space of geom2.
   * @throws Exception if not cartesian 3D, or if not a solid, or if not a point in a 3d polygon. 
   */
  public static boolean inside3d(J3D_Geometry geom1, J3D_Geometry geom2, 
      double tolerance, boolean isCartesian) throws Exception
  {
    if (geom1.hasCircularArcs() || geom2.hasCircularArcs())
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.Compound3DGeometriesNotSupported("inside3d"));
    }
    /*
Geometry3D g1 = to_Geometry3D(geom1);    
System.out.println(g1.toStringFull());    
Geometry3D g2 = to_Geometry3D(geom2);    
System.out.println(g2.toStringFull());
    */
    // If solid, use inside3d, otherwise if point and geom2 is not solid, then use pointInPolygon.
    //Update) switching to isInside since when it is 3D data, the pointInPolygon is not supported for 12c.
    if (geom2.getType() % 1000 == Geometry3D.GTYPE_SOLID)
    	try{
      return geom1.inside3d(geom2, tolerance, isCartesian ? "FALSE": "TRUE");
    	} catch(Exception e)
    {
    		System.out.println(geom1.toStringFull());
    		System.out.println(geom2.toStringFull());
    		System.out.println(e);
    		e.printStackTrace();
   	      	return geom1.isInside(geom2, tolerance, isCartesian ? "FALSE": "TRUE");
    }
    else
    {
      return geom1.isInside(geom2, tolerance, isCartesian ? "FALSE": "TRUE");
    }
  }
 
  /**
   * Converts Geometry type to JGeometry
   *
   * @param geom  the geometry
   * @return a JGeometry object
   */
  public static JGeometry to_JGeometry(Geometry3D g)
  {
	  return g;
  }

  /**
   * Converts Geometry3D type to J3D_Geometry
   *
   * @param geom  the geometry
   * @return a JGeometry object
   */
  public static J3D_Geometry to_J3D_Geometry(Geometry3D g)
  {
	  return (J3D_Geometry) g;
  }
  
  /**
   * Converts JGeometry type to Geometry
   * If the given geometry is already a Geometry type and a 3d geometry, no conversion will be done.
   * Otherwise, the given geometry is converted to Geometry by padding z coordinates.
   *
   * @param geom  the geometry
   * @return a Geometry object
 */
  public static Geometry3D to_Geometry3D(JGeometry geom)
  {
	  if (geom instanceof Geometry3D)
	  {
		  return (Geometry3D) geom;
	  }
	  
	  return new Geometry3D(geom.getType(), geom.getSRID(), geom.getElemInfo(), geom.getOrdinatesArray());
  }

  public GeomType getGeomType()
  {
	  return Geometry.getGeomType(getType() + Geometry.G3D_MARK, getElemInfo());
  }
  
  public String getTypeName()
  {
	  GeomType type = getGeomType();
	  if (type != null) return type.name();
	  return Integer.toString(getType());
  }

  public String toJsonString()
  {
	  GeomType geomType = getGeomType(); 
	  int dim = getDimensions();
	  int srid = getSRID();
      double[] coords = getOrdinatesArray();
      if (coords == null)  coords = getPoint();
      return Geometry.toJsonString(geomType, srid, dim, getElemInfo(), coords, 0);
  }  

  public String toCsvString()
  {
	  String typeName = getTypeName(); 
	  int dim = getDimensions();
	  int srid = getSRID();
      double[] coords = getOrdinatesArray();
      if (coords == null)  coords = getPoint();
      return Geometry.toCsvString(typeName, srid, dim, coords, true);  
  }
  
}
