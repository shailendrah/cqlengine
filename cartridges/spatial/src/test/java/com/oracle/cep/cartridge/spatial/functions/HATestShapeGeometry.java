package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.ha.CQLSpatialProcessor;
import oracle.cep.test.ha.BaseCQLTestCase;

/**
 * Unit test to validate spatial query for shape spatial function.
 * @author santkumk
 *
 */
public class HATestShapeGeometry extends BaseCQLTestCase
{
  public void testShape() throws Exception
  {
    String testSchema = new Object()
    {}.getClass().getEnclosingMethod().getName();
    String[] setupDDLs = new String[] {
            "register stream PosStream(id char(100), lat double, lng double)",
            "create view PosGeomStream(id char(100), lat double, lng double, width double, height double) as "
                    + "select p.id as id, lat as lat, lng as lng, 500.0d as width, 1000.0d as height from PosStream as p",
            "create view ShapeGeom(id char(100), geom com.oracle.cep.cartridge.spatial.Geometry)  as " + "IStream( "
                    + "SELECT pos.id as id, shape@spatial(pos.lat, pos.lng, pos.width, pos.height) as geom "
                    + "FROM  PosGeomStream[NOW] as pos )",
            "create query q1 as IStream(select  id, java.util.Arrays.toString(geom.getOrdinatesArray()) as coordinates from ShapeGeom[NOW])"};

    String[] tearDownDDLs = new String[] { "drop query q1",
            "drop view ShapeGeom","drop view PosGeomStream", "drop stream PosStream" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("PosStream", "inpShape_PosStream");
    testMetadata.addDestinationMetadata("q1", "outShape_q1");
    runFullSnapshotTestBase(new CQLSpatialProcessor(), testMetadata, testSchema);
  }

  @Override
  public int getNumFullSnapshotTestPhases() {
    return 1;
  }
}
