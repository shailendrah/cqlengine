/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TkJDBCSpatial2.java /main/1 2010/10/05 12:03:21 hopark Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      05/21/09 - add getQueryPlan, toQXML test
 hopark      04/28/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TkJDBCSpatial2.java /main/1 2010/10/05 12:03:21 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */


/**
 * A sample jdbc program. A very simple test with integer datatypes. New
 * tests can be modelled on it.
 *
 * @author najain
 */
package oracle.cep.test.spatial;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.jdbc.CEPPreparedStatement;
import oracle.cep.test.jdbc.TkJDBCTestBase;
import oracle.spatial.geometry.JGeometry;

public class TkJDBCSpatial2 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCSpatial2 test = new TkJDBCSpatial2();
    test.init(args);
    test.run();
    test.exit();
  }

  private void sendGeom(JGeometry geom, String name, int i, CEPPreparedStatement pstmt)
  	throws Exception
  {
      long ts = 1000 + 1000 * i;
      AttributeValue[] attrs = new AttributeValue[1];
      attrs[0] = new ObjAttributeValue("geom", geom);
      TupleValue tuple = new TupleValue("Geometries", ts, attrs, false);
      pstmt.executeDML(tuple);
  }
  
  private void testBuffer() throws Exception
  {
      stmt.executeUpdate("register stream Geometries (geom oracle.spatial.geometry.JGeometry)");
      stmt.executeUpdate("alter stream Geometries add source push");

    stmt.executeUpdate("create view TriangleGeom as "+
     "SELECT  "+
     "     com.oracle.cep.cartridge.spatial.Geometry.createGeometry@spatial(   "+
     "     com.oracle.cep.cartridge.spatial.Geometry.GTYPE_POLYGON,   "+
     "     0,   "+
     "     com.oracle.cep.cartridge.spatial.Geometry.createElemInfo(1,2003,1),   "+
     "                 ordsgenerator@spatial(0.0, 0.0, 2.0, 4.0, 4.0, 0.0)) as geom  "+ 
     "     FROM Geometries  ");

    stmt.executeUpdate("create view SquareGeom as  "+
     "SELECT   "+
     "     com.oracle.cep.cartridge.spatial.Geometry.createGeometry@spatial(   "+
     "     com.oracle.cep.cartridge.spatial.Geometry.GTYPE_POLYGON,   "+
     "     0,   "+
     "     com.oracle.cep.cartridge.spatial.Geometry.createElemInfo(1,2003,1),   "+
     "                      ordsgenerator@spatial(6.0, 6.0, 6.0, 9.0, 9.0, 9.0, 9.0, 6.0)) as geom  "+
     " FROM Geometries  ");

    stmt.executeUpdate("create query helloworldRule as  "+
     "           SELECT \"Yellow\" as color, 100 as x, 100 as y, 100 as shapesize   "+
     "           FROM TriangleGeom [now] AS T, SquareGeom [now] AS S   "+
     "           WHERE WITHINDISTANCE@spatial(S.geom, T.geom, 100.0d) = true   ");

      String dest1 = getFileDest("qspatial12", ".log");
      stmt.executeUpdate("alter query helloworldRule add destination "+dest1);
      stmt.executeUpdate("alter query helloworldRule start");

      stmt.executeUpdate("alter system run");

      CEPPreparedStatement pstmt = (CEPPreparedStatement) con.prepareStatement("insert into Geometries values (?, ?)");
      
      int i = 0;
      int[] elemInfo = new int[] {1, 2, 1};
      double[] ords = new double[] {-82.414884, 28.0094323, -82.387158, 28.0116258,
		      -82.378891, 28.0131216,-82.377988, 28.0133894, -82.37555, 28.0143994,
		      -82.329352, 28.0661089, -82.313207, 28.1006725, -82.362246, 28.1261981,
		      -82.4453185, 28.113936, -82.428389, 28.0245891, -82.422103, 28.0117697,
		      -82.421382, 28.0109085, -82.419096, 28.0099741, -82.414884, 28.0094323};
      JGeometry geom1 = new JGeometry (2, 8265, elemInfo, ords);
      while (i < 5) {
        sendGeom(geom1, "Geometries", i++, pstmt);
      }
          
      pstmt.close();
      pstmt = null;
  }

  protected void runTest()
    throws Exception
  {
      testBuffer();
  }
}
