/* Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
All rights reserved.*/
package com.oracle.cep.cartridge.spatial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.rtreeindex.OpAnyinteract;
import com.oracle.cep.cartridge.spatial.rtreeindex.OpContain;
import com.oracle.cep.cartridge.spatial.rtreeindex.OpInside;
import com.oracle.cep.cartridge.spatial.rtreeindex.OpInside3d;
import com.oracle.cep.cartridge.spatial.rtreeindex.OpWithinDistance;

/**
 * Changes from 11g to 12c spatial:
 * - Using 2D geometry instead of padded 3D geometry
 * 
 * @author alealves
 *
 */
public class TestGeometry extends TestCase
{
  private Geometry point1;
  private Geometry point2;
  private Geometry rect1;
  private Geometry point3;
  private Geometry point4;
  private Geometry rect2;
  private Geometry point5;
  private Geometry point6;
  private CartridgeContext cartesianContext;

  private Geometry circle1;
  private Geometry circle2;

  private Geometry lineString1;
  private Geometry lineString2;
  private Geometry compoundLineString1;
  private Geometry compoundPolygon;

  private Geometry multiPolygon;
  
  private Geometry mpPointHole;
  private Geometry mpPointInOuterRing;
  private Geometry mpPointOutside;

  private Geometry3D point3d;
  private Geometry3D point3d2;
  private Geometry3D point3d3;
     
  private Geometry3D line3d;
  private Geometry3D polygon3d;
  private Geometry3D cube;
  private Geometry3D compoundPolygon3d;

  public void testPoint_anyInteract_Circle() throws Exception
  {
    //        JGeometry circle1 = new JGeometry(2003, 0, new int[] {1,2003,4}, new double[] {15.0,145.0, 10.0,150.0, 20.0,150.0});
    //        System.out.println("Is circle = " + circle1.isCircle());

    //        boolean ret = point1.isInside(circle1, 0d, "FALSE");
    //        System.out.println(ret);
    //        
    //        ret = point1.anyInteract(circle1, 0d, "FALSE");
    //        System.out.println(ret);
    assertTrue(point1.anyInteract(circle1, 0d, "FALSE"));
  }

  public void testPoint1_isInside_Circle1() throws Exception
  {
    assertTrue(point1.isInside(circle1, 0d, "FALSE"));
  }

  public void testPoint2_anyInteract_Circle1() throws Exception
  {
    assertFalse(point2.anyInteract(circle1, 0d, "FALSE"));
  }

  public void testCircle2_isInside_Circle1() throws Exception
  {
    assertTrue(circle2.isInside(circle1, 0d, "FALSE"));
  }

  public void testCircle1_isInside_Circle2() throws Exception
  {
    assertFalse(circle1.isInside(circle2, 0d, "FALSE"));
  }

  public void testCircle2_isInside_Rect1() throws Exception
  {
    assertTrue(circle2.isInside(rect1, 0d, "FALSE"));
  }

  public void testCircle1_isInside_Rect1() throws Exception
  {
    assertFalse(circle1.isInside(rect1, 0d, "FALSE"));
  }

  /*
   * We use the following operations on JGeometry/J3D_Geometry/RTree:
   * - distance
   * - anyinteract
   * - search 
   * - nnSearch
   * - bufferPolygon
   */
  
  //
  // Test validation: these hold true and our operators' results must compare to them.
  //
  public void testPoint_isInside_Rect() throws Exception
  {
    assertTrue(point1.isInside(rect1, 0.1d, "FALSE")); 
    assertFalse(point2.isInside(rect1, 0.1d, "FALSE"));
    assertFalse(point5.isInside(rect1, 0.1d, "FALSE"));
    //Tolerance N/A for inside, as tolerance grows the thickness of the boundary.
    assertFalse(point6.isInside(rect1, 10.0d, "FALSE")); 
  }

  public void testPoint_anyInteract_Rect() throws Exception
  {
    assertTrue(point1.anyInteract(rect1, 0.1d, "FALSE"));
    assertFalse(point2.anyInteract(rect1, 0.1d, "FALSE"));
    assertFalse(point2.anyInteract(rect1, 6d, "FALSE"));
    assertFalse(rect1.anyInteract(point2, 6d, "FALSE"));
    assertTrue(point2.anyInteract(rect1, 7d, "FALSE"));
    assertTrue(rect1.anyInteract(point2, 7d, "FALSE"));
    assertTrue(point5.anyInteract(rect1, 0.1d, "FALSE"));
  }

  public void testPoint_distance() throws Exception
  {
    double dist = point4.distance(point1, 0.1d, "FALSE");
    assertTrue(10.0 < dist && dist < 10.5);
    dist = point4.distance(point3, 0.1d, "FALSE");
    assertTrue(dist == 9.0);
    dist = point1.distance(rect1, 0.1d, "FALSE");
    assertTrue(dist == 0.0);
    dist = point6.distance(rect1, 10d, "FALSE");
    assertTrue(dist == 0.0);
  }
  
  //
  // Now test our operators
  //
  
  // Contain is the exact opposite of inside
  public void testPoint_OpInside_Rect() throws Exception
  {
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point1, rect1, 0.1d}));
    assertFalse((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point2, rect1, 0.1d}));
    assertFalse((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point5, rect1, 0.1d}));
    assertFalse((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point6, rect1, 10d}));
  }
  
  public void testRect_OpContain_Point() throws Exception
  {
    assertTrue((Boolean) new OpContain(0, cartesianContext).execute(new Object[] {rect1, point1, 0.1d}));
    assertFalse((Boolean) new OpContain(0, cartesianContext).execute(new Object[] {rect1, point2, 0.1d}));
    assertFalse((Boolean) new OpContain(0, cartesianContext).execute(new Object[] {rect1, point5, 0.1d}));
    assertFalse((Boolean) new OpContain(0, cartesianContext).execute(new Object[] {rect1, point6, 10d}));
  }
  
  public void testPoint_OpAnyInteract_Rect() throws Exception
  {
    // AnyInteract are geometries that are NOT disjoint. This is different than 'inside'. 
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point1, rect1, 0.1d}));
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point2, rect1, 0.1d}));
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point2, rect1, 6d}));
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {rect1, point2, 6d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point2, rect1, 7d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {rect1, point2, 7d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point5, rect1, 0.1d}));
  }

  public void testPoint_OpWithinDistance_Rect() throws Exception
  {
    assertFalse((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point4, point1, 10d}));
    assertTrue((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point4, point1, 10.5d}));
    assertTrue((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point4, point3, 9.1d}));
    assertFalse((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point4, point3, 9.0d}));
    assertTrue((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point1, rect1, 0.1d}));
    assertFalse((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point4, point3, 0d}));
    assertTrue((Boolean) new OpWithinDistance(0, cartesianContext).execute(new Object[] {point6, rect1, 0.1d, 10d}));
  }

  public void testLineString_anyInteract() throws Exception
  {
    //
    // Validate Spatial API
    //
    assertFalse(point1.anyInteract(lineString1, 0d, "FALSE"));
    assertFalse(point3.anyInteract(lineString1, 0d, "FALSE"));
    assertTrue(point3.anyInteract(lineString1, 0.1d, "FALSE"));
    assertTrue(lineString2.anyInteract(rect1, 0d, "FALSE"));
  }

  public void testLineString_isInside() throws Exception
  {
    // INSIDE means that boundary must not touch
    assertTrue(lineString1.isInside(rect1, 0d, "FALSE"));
    assertFalse(lineString2.isInside(rect1, 0d, "FALSE"));
  }

    //
    // Validate cartridge
    //
  public void testLineString_OpAnyInteract() throws Exception
  {
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point1, lineString1, 0d}));
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point3, lineString1, 0d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point3, lineString1, 0.1d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {lineString2, rect1, 0d}));
  }

  public void testLineString_OpInside() throws Exception
  {
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {lineString1, rect1, 0d}));
    assertFalse((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {lineString2, rect1, 0d}));
  }

  public void testCompoundLineString() throws Exception
  {

    // For any geometry involving an arc, a non-zero tolerance must be specified as internally
    //  the object is densified.
    assertTrue(point1.anyInteract(compoundLineString1, 0.1d, "FALSE"));
    assertFalse(point2.anyInteract(compoundLineString1, 0.1d, "FALSE"));

    assertTrue(compoundLineString1.isInside(rect1, 0.1d, "FALSE"));
    assertTrue(compoundLineString1.isInside(rect2, 0.1d, "FALSE"));
    
    Throwable excep = null;
    try {
      // 0 tolerance for arcs should fail...
      new OpAnyinteract(0, cartesianContext).execute(new Object[] {point1, compoundLineString1, 0d});
    } catch (Throwable e)
    {
      excep = e;
    }
    assertNotNull(excep);
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point1, compoundLineString1, 0.1d}));
    
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point2, compoundLineString1, 0.1d}));
    
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {compoundLineString1, rect1, 0.1d}));
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {compoundLineString1, rect2, 0.1d}));
    
  }

  public void testCompoundPolygon() throws Exception
  {
    assertTrue(point4.anyInteract(compoundPolygon, 0.1d, "FALSE"));
    assertTrue(point2.anyInteract(compoundPolygon, 0.1d, "FALSE"));
    assertFalse(point1.anyInteract(compoundPolygon, 0.1d, "FALSE"));

    assertTrue(compoundPolygon.isInside(rect2, 0.1d, "FALSE"));
    
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point4, compoundPolygon, 0.1d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point2, compoundPolygon, 0.1d}));
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point1, compoundPolygon, 0.1d}));
    
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {compoundPolygon, rect2, 0.1d}));
  }

  /**
   * - AnyInteract works for both 2d and 3d, and for both cartesian and geodetic
   * - isInside works for both 2d and 3d, however flattens the 3d to 2d geometries. Works for both cartesian and geodetic
   * - inside3d only works for 3d and cartesians. 
   * 
   * @throws Exception
   */
  public void testLine3d_anyInteract_Point3d() throws Exception
  {
    assertTrue(line3d.anyInteract(point3d, 0.01d, "FALSE"));
    assertTrue(line3d.anyInteract(polygon3d, 0.01d, "FALSE"));
    assertFalse(point3d2.anyInteract(polygon3d, 0.01d, "FALSE"));
  }
  
  public void testLine3d_OpAnyInteract_Point3d() throws Exception
  {
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {line3d, point3d, 0.1d}));
    assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {line3d, polygon3d, 0.1d}));
    assertFalse((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point3d2, polygon3d, 0.1d}));
  }
  
  public void testPoint3d_inside3d_Line3d() throws Exception
  {
    // inside3d only works for solid, use pointInPolygon instead.
    assertFalse(point3d.inside3d(line3d, 0.01d));
    assertFalse(point3d.inside3d(polygon3d, 0.01d));
  }
  
  public void testPoint3d_pointInPolygon_Polygon3d() throws Exception
  {
    assertTrue(point3d.pointInPolygon(polygon3d, 0.1d));
    assertFalse(point3d2.pointInPolygon(polygon3d, 0.01d));
  }
  
  public void testPoint3d_inside3d_Cube() throws Exception
  {
    assertTrue(point3d.inside3d(cube, 0.01d));
    assertFalse(point3d2.inside3d(cube, 0.01d));
  }
  
  public void testPoint3d_OpInside3d_Line3d() throws Exception
  {
    assertFalse((Boolean) new OpInside3d(0, cartesianContext).execute(new Object[] {point3d, line3d, 0.1d}));
    assertTrue((Boolean) new OpInside3d(0, cartesianContext).execute(new Object[] {point3d, polygon3d, 0.1d}));
    assertTrue((Boolean) new OpInside3d(0, cartesianContext).execute(new Object[] {point3d, cube, 0.1d}));
    assertFalse((Boolean) new OpInside3d(0, cartesianContext).execute(new Object[] {point3d2, polygon3d, 0.1d}));
  }
  
  public void testPoint3d_isInside_Cube() throws Exception
  {
    assertTrue(point3d.isInside(cube, 0.01d, "FALSE"));
    assertFalse(point3d2.isInside(cube, 0.01d, "FALSE"));
    // before 12c, isInside removes the height and then performs the usual 2d check.
    // with 12c, isInside works the same way as isInside3d
    assertFalse(point3d3.isInside(cube, 0.01d, "FALSE"));
  }
  
  public void testPoint3d_isInside_Polygon3d() throws Exception
  {
    assertTrue(point3d.isInside(polygon3d, 0.01d, "FALSE"));
  }
  
  public void testPoint3d2_isInside_Polygon3d() throws Exception
  {
    assertFalse(point3d2.isInside(polygon3d, 0.01d, "FALSE"));
  }
  
  public void testPoint3d_OpInside_Cube() throws Exception
  {
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point3d, cube, 0.1d}));
    assertFalse((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point3d2, cube, 0.1d}));
  }
  
  public void testPoint3d_OpInside_Polygon3d() throws Exception
  {
    assertTrue((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point3d, polygon3d, 0.1d}));
  }
  
  public void testPoint3d2_OpInside_Polygon3d() throws Exception
  {
    assertFalse((Boolean) new OpInside(0, cartesianContext).execute(new Object[] {point3d2, polygon3d, 0.1d}));
  }
  
  public void testPolygon3d_closestPoints_Point3d2() throws Exception
  {
    // Closest point for non interacting geometries.
    @SuppressWarnings("unchecked")
    List<JGeometry> points = polygon3d.closestPoints(point3d2, 0.01d);
    double [] closestPoint = points.get(0).getOrdinatesArray();

    assertTrue(closestPoint[0] == 2.0d);
    assertTrue(closestPoint[1] == 2.0d);
    assertTrue(closestPoint[2] == 0.0d);

    closestPoint = points.get(1).getOrdinatesArray();

    assertTrue(closestPoint[0] == 3.0d);
    assertTrue(closestPoint[1] == 3.0d);
    assertTrue(closestPoint[2] == 0.0d);
  }

  /**
   * Compound 3D polygons are NOT supported as of yet.
   * That is, no arcs can be drawn on 3D geometries. 
   *
   * @throws Exception
   */
  public void test3DCompoundPolygon() throws Exception
  {
    //assertTrue(point4.anyInteract(compoundPolygon, 0.01d, "FALSE")); // REVIEW
    Throwable excep = null;
    try {
      assertTrue((Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {point4, 
          compoundPolygon3d, 0.1d}));
    } catch (Throwable e)
    {
      excep = e;
    }
    assertNotNull(excep);
  }

  public void testMultiPolygon() throws Exception
  {
    boolean b = mpPointHole.anyInteract(multiPolygon, 0.0, "FALSE");
    assertFalse(b);
    b = mpPointInOuterRing.anyInteract(multiPolygon, 0.0, "FALSE");
    assertTrue(b);
    b = mpPointOutside.anyInteract(multiPolygon, 0.0, "FALSE");
    assertFalse(b);

    b = multiPolygon.anyInteract(mpPointInOuterRing, 0d, "FALSE");
    assertTrue(b);

    b = (Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {mpPointHole, 
    		multiPolygon, 0.1d});
    assertFalse(b);
    b = (Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {mpPointInOuterRing, 
    		multiPolygon, 0.0});
    assertTrue(b);
    b = (Boolean) new OpAnyinteract(0, cartesianContext).execute(new Object[] {mpPointOutside, 
    		multiPolygon, 0.0});
    assertFalse(b);
    
    b = mpPointHole.isInside(multiPolygon, 0.0, "FALSE");
    assertFalse(b);
    b = mpPointInOuterRing.isInside(multiPolygon, 0.0, "FALSE");
    assertTrue(b);
    b = mpPointOutside.isInside(multiPolygon, 0.0, "FALSE");
    assertFalse(b);
    b = (Boolean) new OpInside(0, cartesianContext).execute(new Object[] {mpPointHole, multiPolygon, 0.0});
    assertFalse(b);
    b = (Boolean) new OpInside(0, cartesianContext).execute(new Object[] {mpPointInOuterRing, multiPolygon, 0.0});
    assertTrue(b);
    b = (Boolean) new OpInside(0, cartesianContext).execute(new Object[] {mpPointOutside, multiPolygon, 0.0});
    assertFalse(b);
  }

  @Override
  protected void setUp() throws Exception
  {
    SpatialCartridge.createInstance(new CartridgeRegistry());
    
    Map<String, Object> props = new HashMap<String, Object>();
    props.put(SpatialContext.GEO_PARAM, GeodeticParam.get(GeodeticParam.CARTESIAN_SRID));
    cartesianContext = new CartridgeContext(props);
    
    point1 = Geometry.createPoint(0, 0, 0);
    point2 = Geometry.createPoint(0, 10,10);
    point3 = Geometry.createPoint(0, 1,1);
    point4 = Geometry.createPoint(0, 10,1);
    point5 = Geometry.createPoint(0, 3,3);
    point6 = Geometry.createPoint(0, 3.5,2.0);

    rect1 = Geometry.createRectangle(0, -5,-5, 3,3);
    rect2 = Geometry.createRectangle(0, -5,-5, 20,20);

    circle1 =  Geometry.createCircle(0, 0,0, 5.0);
    circle2 = Geometry.createCircle(0, 0,0, 4.0);

    lineString1 = Geometry.createLinearLineString(0, new double [] {1,1, 2,1});
    lineString2 = Geometry.createLinearLineString(0, new double [] {1,1, 3,3});

    // Creates a linear line, and then an arc.
    compoundLineString1 = Geometry.createGeometry(2002, 0, new int[] {1,4,2, 1,2,1, 3,2,2}, 
          new double [] {0,0, 0,2, -2,0, 2,0});

    point3d = Geometry3D.createPoint(0,  0, 0, 0);
    point3d2 = Geometry3D.createPoint(0,  3, 3, 0);
    point3d3 = Geometry3D.createPoint(0,  0, 0, 3);
     
    line3d =  Geometry3D.createLinearLineString(0, new double [] {0,0,0, 1,1,0});
    polygon3d = Geometry3D.createLinearPolygon(0, new double [] {-2,-2,0, 2,-2,0, 2,2,0, -2,2,0, -2,-2,0});
    cube = Geometry3D.createSolidBox(0,  -1, -1, -1, 2, 2, 2);

    // Creates a cone: two line strings, connected by an arc.
    compoundPolygon = Geometry.createGeometry(2003, 0, new int[] {1,1005,2, 1,2,1, 5,2,2}, 
          new double [] {6,10, 10,1, 14,10, 10,14, 6,10});

    compoundPolygon3d = new Geometry3D(3003, 0, new int[] {1,1006,2, 1,2,1, 5,2,2}, 
          new double [] {6,10,0, 10,1,0, 14,10,0, 10,14,0, 6,10,0});

    double[][] coords = new double[2][];
    coords[0] = new double[]{2,4, 4,3, 10,3, 13,5, 13,9, 11,13, 5,13, 2,11, 2,4 };
    coords[1] = new double[]{7,5, 7,10, 10,10, 10,5, 7,5};
    multiPolygon = Geometry.createLinearPolygon(0, coords);
    
    mpPointHole = Geometry.createPoint(0, 8, 8);
    mpPointInOuterRing = Geometry.createPoint(0, 4,8);
    mpPointOutside = Geometry.createPoint(0, 9, 9);
  }
}
