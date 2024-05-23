package com.oracle.cep.cartridge.spatial.ha;

import com.oracle.cep.cartridge.spatial.ha.CQLSpatialProcessor;

import oracle.cep.test.ha.BaseCQLTestCase;

/**
 * Unit test to validate spatial query for aggregated speed function on
 * cqlengine in HA mode.
 * 
 * @author kmulay
 *
 */
public class HATestAggrSpeedTest extends BaseCQLTestCase {
  public void testSpatialAggrSpeed() throws Exception {
    String testSchema = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String[] setupDDLs = new String[] {
        "register stream PosStream(id char(100), lat double, lng double)",
        "create view PosGeomStream(id char(100), eventTime bigint, geom com.oracle.cep.cartridge.spatial.Geometry) as "
            + "select p.id as id, p.ELEMENT_TIME as eventTime, com.oracle.cep.cartridge.spatial.Geometry.createPoint(p.lat, p.lng) as geom from PosStream as p",
        "create query q1 as "
            + "IStream( "
            + "SELECT pos.id as id, aggspeed@spatial(pos.id,pos.eventTime,pos.geom) as speed "
            + "FROM  PosGeomStream[PARTITION BY id rows 4] as pos group by id)" };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop view PosGeomStream", "drop stream PosStream" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("PosStream", "inpAggrSpeed_PosStream");
    testMetadata.addDestinationMetadata("q1", "outAggrSpeed_q1");
    runFullSnapshotTestBase(new CQLSpatialProcessor(), testMetadata, testSchema);
  }

}
