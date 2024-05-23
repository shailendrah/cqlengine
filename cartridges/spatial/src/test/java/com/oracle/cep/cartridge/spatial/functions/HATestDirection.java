package com.oracle.cep.cartridge.spatial.functions;

import com.oracle.cep.cartridge.spatial.ha.CQLSpatialProcessor;
import oracle.cep.test.ha.BaseCQLTestCase;

/**
 * Unit test to validate spatial query for direction function on cqlengine in HA mode.
 * @author santkumk
 *
 */
public class HATestDirection extends BaseCQLTestCase
{
  public void testDirection() throws Exception
  {
    String testSchema = new Object()
      {}.getClass().getEnclosingMethod().getName();
    String[] setupDDLs = new String[] {
        "register stream PosStream(id char(100), lat double, lng double)",
        "create view PosGeomStream(id char(100), geom com.oracle.cep.cartridge.spatial.Geometry) as "
            + "select p.id as id, com.oracle.cep.cartridge.spatial.Geometry.createPoint(p.lat, p.lng) as geom from PosStream as p",
        "create query q1 as " + "IStream( "
            + "SELECT pos.id as id, direction@spatial(pos.id,pos.geom) as dir "
            + "FROM  PosGeomStream[NOW] as pos )" };

    String[] tearDownDDLs = new String[] { "drop query q1",
        "drop view PosGeomStream", "drop stream PosStream" };
    TestMetadata testMetadata = new TestMetadata(setupDDLs, tearDownDDLs);
    testMetadata.addSourceMetadata("PosStream", "inpDirection_PosStream");
    testMetadata.addDestinationMetadata("q1", "outDirection_q1");
    runFullSnapshotTestBase(new CQLSpatialProcessor(), testMetadata, testSchema);
  }
 
}
