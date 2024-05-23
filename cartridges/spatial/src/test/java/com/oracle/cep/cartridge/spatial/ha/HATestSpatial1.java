package com.oracle.cep.cartridge.spatial.ha;

import oracle.cep.test.ha.BaseCQLTestCase;

/**
 * Unit test to validate spatial query on cqlengine in HA mode. In this test,
 * the spatial join is done by BinStreamJoin execution operator.
 * 
 * @author santkumk
 *
 */
public class HATestSpatial1 extends BaseCQLTestCase
{
  public void testSpatial1() throws Exception
  {
    String testSchema = new Object()
    {
    }.getClass().getEnclosingMethod().getName();
    String[] setupDDLs = new String[] {
        "register stream PosStream(id char(100), lat double, lng double)",
        "create view PosGeomStream(id char(100), geom com.oracle.cep.cartridge.spatial.Geometry) as "
            + "select id, com.oracle.cep.cartridge.spatial.Geometry.createPoint(lat, lng) from PosStream ",
        "register relation FenceRelation(id char(100), nCoords int, "
            + "lat1 double, lng1 double, lat2 double, lng2 double, lat3 double, lng3 double, lat4 double, lng4 double, "
            + "lat5 double, lng5 double, lat6 double, lng6 double, lat7 double, lng7 double)",
        "register view FenceGeomRelation(id char(100), geom com.oracle.cep.cartridge.spatial.Geometry) as "
            + "select id, "
            + " com.oracle.cep.cartridge.spatial.Geometry.createGeometry("
            + " com.oracle.cep.cartridge.spatial.Geometry.GTYPE_POLYGON,"
            + " 8307,"
            + " com.oracle.cep.cartridge.spatial.Geometry.createElemInfo(1, 1003, 1),"
            + " ORDSGENERATOR@spatial( 2, nCoords,"
            + " lat1,lng1,"
            + " lat2,lng2,"
            + " lat3,lng3,"
            + " lat4,lng4,"
            + " lat5, lng5,"
            + " lat6, lng6," + " lat7, lng7 ) )" + " from FenceRelation ",
        "create query q1 as " + "RStream( "
            + "SELECT fence.id as fenceId, pos.id as posId "
            + "FROM  PosGeomStream[NOW] as pos, FenceGeomRelation as fence "
            + "WHERE contain@spatial(fence.geom,pos.geom,0.001d) = true)" };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop view PosGeomStream", "drop stream PosStream",
        "drop view FenceGeomRelation", "drop relation FenceRelation" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("PosStream", "inpSpatial_PosStream");
    testMetadata.addSourceMetadata("FenceRelation", "inpSpatial_FenceRelation");
    testMetadata.addSourceType("FenceRelation", SourceType.RELATION);
    testMetadata.addDestinationMetadata("q1", "outSpatial1_q1");
    runFullSnapshotTestBase(new CQLSpatialProcessor(), testMetadata, testSchema);
  }
}
