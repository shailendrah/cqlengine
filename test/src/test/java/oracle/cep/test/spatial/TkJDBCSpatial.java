/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TkJDBCSpatial.java /main/2 2009/12/30 21:49:27 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/spatial/TkJDBCSpatial.java /main/2 2009/12/30 21:49:27 hopark Exp $
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

public class TkJDBCSpatial extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCSpatial test = new TkJDBCSpatial();
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

      stmt.executeUpdate("create view vbuffer(bgeom com.oracle.cep.cartridge.spatial.Geometry, "+ 
	                     "  jbgeom oracle.spatial.geometry.JGeometry) as "+
			             "select com.oracle.cep.cartridge.spatial.Geometry.bufferPolygon@spatial8265(geom, 5.0), "+
                         "geom.buffer(5.0, 6378137, 298.257223563, 1.0) "+
			             "from Geometries");
      stmt.executeUpdate("create query qbuffer as "+
			             "select oracle.cep.test.spatial.TestSpatialBase.compareGeometry(bgeom,  "+
			             "com.oracle.cep.cartridge.spatial.Geometry.to_Geometry@spatial8265(jbgeom) ) "+
			             "from vbuffer ");
      String dest1 = getFileDest("g_buffer");  
      stmt.executeUpdate("alter query qbuffer add destination "+dest1);
      stmt.executeUpdate("alter query qbuffer start");

      CEPPreparedStatement pstmt = (CEPPreparedStatement) con.prepareStatement("insert into Geometries values (?, ?)");
      
      int i = 0;
      int[] elemInfo = new int[] {1, 2, 1};
      double[] ords = new double[] {-82.414884, 28.0094323, -82.387158, 28.0116258,
		      -82.378891, 28.0131216,-82.377988, 28.0133894, -82.37555, 28.0143994,
		      -82.329352, 28.0661089, -82.313207, 28.1006725, -82.362246, 28.1261981,
		      -82.4453185, 28.113936, -82.428389, 28.0245891, -82.422103, 28.0117697,
		      -82.421382, 28.0109085, -82.419096, 28.0099741, -82.414884, 28.0094323};
      JGeometry geom1 = new JGeometry (2, 8265, elemInfo, ords);
      sendGeom(geom1, "Geometries", i++, pstmt);
      
      pstmt.close();
      pstmt = null;
  }

  
  private void testCompoundGeom(int n, int gtype, int srid, int[] elemInfo, double[] ords) throws Exception
  {
	  String stream = "CompoungGeometries" + n;
	  String view = "vcgeom" + n;
	  String query = "qcgeom" + n;
      stmt.executeUpdate("register stream " + stream + "(geom oracle.spatial.geometry.JGeometry)");
      stmt.executeUpdate("alter stream  " + stream + " add source push");

      StringBuilder b = new StringBuilder();
      b.append("com.oracle.cep.cartridge.spatial.Geometry.createGeometry(");
      b.append(gtype);
      b.append(",");
      b.append(srid);
      b.append(",");
      b.append("EINFOGENERATOR@spatial");
      b.append("(");
      for (int i = 0; i < elemInfo.length; i++)
      {
    	  if (i > 0)
    		  b.append(",");
    	  b.append( elemInfo[i] );
      }
      b.append("),");
      b.append("ORDSGENERATOR@spatial");
      b.append("(");
      int dim = gtype / 1000;
      for (int i = 0; i < ords.length; i++)
      {
    	  if (i > 0)
    		  b.append(",");
    	  b.append( ords[i] );
    	  if (dim == 2 && (i % 2) == 1)
    	  {
        	  b.append( ",0.0");
    	  }
      }
      b.append(")");
      b.append(")");
      
      stmt.executeUpdate("create view "+ view + "(mgeom com.oracle.cep.cartridge.spatial.Geometry, "+
	                     "   jgeom oracle.spatial.geometry.JGeometry) as \n" +
                         " select "+
                      	     b.toString() + 
                      	 "," +
                         "  geom\n"+
			             " from " + stream);
      stmt.executeUpdate("create query "+query +" as "+
			             "select oracle.cep.test.spatial.TestSpatialBase.compareGeometry(mgeom,  "+
			             "com.oracle.cep.cartridge.spatial.Geometry.to_Geometry@spatial8265(jgeom) ) "+
			             "from " + view);
      String dest1 = getFileDest(query);  
      stmt.executeUpdate("alter query "+query+" add destination "+dest1);
      stmt.executeUpdate("alter query "+query+" start");

      CEPPreparedStatement pstmt = (CEPPreparedStatement) con.prepareStatement("insert into "+stream+" values (?, ?)");
      
      JGeometry geom1 = new JGeometry(gtype, srid, elemInfo, ords);
      System.out.println(geom1.toStringFull());
      sendGeom(geom1, stream, n, pstmt);
      
      pstmt.close();
      pstmt = null;
  }

  private void testCompound() throws Exception
  {
	  int srid = 8307;
	  
      int i = 0;
      //compound multiline
      int [] elemArray1 = {1,2,1, 5,4,3, 5,2,1, 7,2,2, 11,2,1, 15,2,1};
      double [] oArray1 = {1,1, 1,3, 2,1, 3,1, 5,2, 7,1, 8,1, 9,2, 10,3, 11,3};
      testCompoundGeom(i++, 2006, srid, elemArray1, oArray1);

      //2D LRS multiline
      int [] elemArray2 = {1,2,1,7,2,1};
      double [] oArray2 = {100,100,1000, 200,150,1000, 300,100,1000, 425,100,1000, 500,150,1000};
      testCompoundGeom(i++, 3306, srid, elemArray2, oArray2);
   
      //multipoly
      int [] elemArray3 = {1,1003,3, 5,2003,3, 9,2005,2,9,2,2,13,2,1, 19,1003,4};
      double [] oArray3 = {1,1, 7,5, 2,2, 3,3, 4,3, 5,4, 6,3, 5,2, 4,3, 10,1, 12,3, 10,5};
      testCompoundGeom(i++, 2007, srid, elemArray3, oArray3);
  
      //3D multipoint w/oriented pt
      int [] elemArray4 = {1,1,3, 10,1,1, 13,1,0, 16,1,1};
      double [] oArray4 = {1,1,1, 2,2,2, 3,3,3, 4,4,4, 1,1,1, 5,5,5};
      testCompoundGeom(i++, 3005, srid, elemArray4, oArray4);
   
      //4D multipoint w/oriented pt
      int [] elemArray5 = {1,1,1, 5,1,0, 8,1,2};
      double [] oArray5 = {1,1,1,1, 0,0,0, 2,2,2,2, 3,3,3,3};
      testCompoundGeom(i++, 4005, srid, elemArray5, oArray5);
  
      //3D LRS point w/orientation
      int [] elemArray6 = {1,1,1,5,1,0};
      double [] oArray6 = {10,10,10,100, 0,0,0};
      testCompoundGeom(i++, 4401, srid, elemArray6, oArray6);
    
      //collection 
      int [] elemArray7 = {1,1003,3, 5,2003,3, 9,2,1, 15,1003,4, 21,1,1};
      double [] oArray7 = {1,1, 7,5, 2,2, 3,3, 4,3, 5,2, 6,3, 10,1, 12,3, 10,5, 10,3};
      testCompoundGeom(i++, 2004, srid, elemArray7, oArray7);

  }

  protected void runTest()
    throws Exception
  {
      testBuffer();
      testCompound();
      
      stmt.executeUpdate("alter system run");

  }
}
