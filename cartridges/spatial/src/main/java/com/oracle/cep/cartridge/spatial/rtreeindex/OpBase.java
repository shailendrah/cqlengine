/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpBase.java /main/12 2016/01/29 11:04:52 rxvenkat Exp $ */

/* Copyright (c) 2009, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      09/28/10 - add logging
 hopark      12/28/09 - remove getCQLType
 hopark      12/08/09 - Geometry method change
 alealves    11/27/09 - Data cartridge context, default package support
 anasrini    09/10/09 - Creation
 anasrini    09/10/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpBase.java /main/12 2016/01/29 11:04:52 rxvenkat Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.GeodeticParam;
import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;
import com.oracle.cep.cartridge.spatial.SpatialCartridgeLogger;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A note on tolerance:
 * The concept of  tolerance is generally used as a measure of accuracy of the data. So when you specify a tolerance for these operations,
 * think of it as a fuzzy boundary around one of the geometries.
 * That is, usually you think of the boundary of a polygon as a line with zero thickness. With tolerance, you can imagine a boundary line with a thickness 
 * equal to the tolerance. 
 * Therefore, tolerance won't influence 'isInside', as this operation checks uses the boundary.
 * Also, a tolerance > 0 must be specified when dealing with arcs, as the arcs need to be densified.
 *
 */
public abstract class OpBase implements SingleElementFunction
{
  protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);
    //LogFactory.getLog(SpatialCartridge.LOGGER_NAME);

  static Datatype s_JGeometryType = null;
  
  int m_id;
  String m_opName;
  protected int   m_keyPos;
  protected GeodeticParam m_geoParam = null;
  
  private ICartridgeContext m_catCtx;
  static int s_nextId = 0;
  
  OpBase(String opName, int keyPos, ICartridgeContext cartridgeContext)
  {
    this.m_opName = opName;
    this.m_keyPos = keyPos;
    this.m_catCtx = cartridgeContext;
    this.m_geoParam = SpatialCartridge.getContextGeodeticParam(m_catCtx);
    m_id = s_nextId++;
  }

  public int getId() { return m_id;}
  public String getOpName() {return m_opName;}

  public static boolean isAllGeometryType(Datatype typ)
  {
    return Geometry.isAllGeometryType(typ);
  }

  @SuppressWarnings("rawtypes")
  protected Iterator startScan(RuntimeContext ctx, Object[] args)  throws Exception
  {
    ArrayList res = new ArrayList();
    if (ctx.rtree.getEntryCount() > 0)
    {
	    double[][] mbr = getSearchMbr(ctx, args);
	    synchronized(ctx.rtree)
	    {
	      ctx.rtree.search(mbr, res);
	    }  
	    
	    logScan(ctx, args, res);
    }    
    return res.iterator();
  }

  public static void debugLog(Log log, Object obj, int id, String str)
  {
    log.debug(obj.getClass().getSimpleName()+id + " : " + str);
  }
  
  @SuppressWarnings("rawtypes")
  private void logScan(RuntimeContext ctx, Object[] args, ArrayList res)
  {
    if (log.isDebugEnabled())
    {
      debugLog(log, this, m_id, "startScan ------------------------------------------------");
      debugLog(log, this, m_id, "rtree=" + ctx.id+ " count=" + ctx.rtree.getEntryCount());
      JGeometry tg = (JGeometry) args[m_keyPos];
	  Geometry g1 = Geometry.to_Geometry(tg);
      log.debug("key arg :"+g1.toJsonString());
      int n = 0;
      Iterator x = res.iterator();
      while (x.hasNext())
      {
        Object o =  x.next();
        SpatialCartridge.debugLog(log, this, "scanresult:" + o.toString());
        n++;
      }
      log.debug("scanresult:"+n);
    }
  }
  
  @SuppressWarnings("rawtypes")
  protected Object getNext(Iterator iterator)
  {
    return iterator.next();
  }

  protected void releaseScan(RuntimeContext rctx)
  {
  }

  protected double[][] getSearchMbr(JGeometry key, double tolerance) throws Exception
  {
	if(tolerance <= 0)
		throw new IllegalArgumentException(SpatialCartridgeLogger.NonPositiveTolerance());
	  
	long startTime = System.nanoTime();
	try {
		if (log.isDebugEnabled())
		{
	  		Geometry g1 = Geometry.to_Geometry(key);
			log.debug("key (" +m_keyPos + ") : "+ g1.toJsonString());
		}
	
		if (key.isPoint() && key.getDimensions()==2 && tolerance >= 5)
		{
			//Creating Circle is faster than buffering the point
			//Circle: 234638,  Buffer:344355
			double[] pt = key.getPoint();
			key = Geometry.createCircle(key.getSRID(), pt[0], pt[1], tolerance);
			if (log.isDebugEnabled())
			{
		  		Geometry g = Geometry.to_Geometry(key);
				log.debug("Circle.. " + tolerance + ": "+ g.toJsonString());
			}
		} 
		else
		{
	//		long buffers = System.nanoTime();
			key = bufferPolygon(key, tolerance);
	//		long buffere = System.nanoTime();
	//		System.out.println("Circle: " + (circlee-circles) + ", " + " Buffer:"+(buffere-buffers));
			if (log.isDebugEnabled())
			{
		  		Geometry g = Geometry.to_Geometry(key);
				log.debug("Buffered.. " + tolerance + ": "+ g.toJsonString());
			}
		}
	
	    return Geometry.get2dMbr(key);
	} finally {
	      if (log.isDebugEnabled()) {
	    	  long endTime = System.nanoTime();
	    	  SpatialCartridge.debugLog(log, this, " getSearchMbr took : "+(endTime - startTime));
	      }
	}
  }  
  
  protected double[][] getSearchMbr(RuntimeContext ctx, Object[] args) throws Exception
  {
    JGeometry key = (JGeometry) args[m_keyPos];
    return Geometry.get2dMbr(key);
  }

  public abstract IIndexInfo[] getIndexInfo(int  paramPosition, ICartridgeContext context);

  public JGeometry bufferPolygon(JGeometry polygon, double distance) 
  {
	if (polygon.getDimensions() == 2)
		return Geometry.bufferPolygon(m_geoParam, polygon, distance);
	else 
	{
		return polygon;
		//buffer is not supported by 3d
	    //return Geometry3D.bufferPolygon(m_geoParam, (J3D_Geometry) polygon, distance);
	}
  }
  
  // REVIEW should consider making this public static in Geometry class
  protected boolean anyInteract(JGeometry geom, JGeometry key, double tolerance) throws Exception
  {
    JGeometry bgeom = geom;
    
    double t = m_geoParam.isCartesian() ? tolerance : m_geoParam.getAnyinteractTol();
    
    // All geometries that have arcs must use a non-zero tolerance.
    // Ideally, the spatial API would check this, but currently it doesn't and simply returns false always.
    if ((geom.hasCircularArcs() || key.hasCircularArcs()) && tolerance == 0)
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.ZeroToleranceForArcs());
    }
    
    if ((geom.getDimensions() == 3 && geom.hasCircularArcs()) || 
        (key.getDimensions() == 3 && key.hasCircularArcs()))
    {
      throw new IllegalArgumentException(SpatialCartridgeLogger.Compound3DGeometriesNotSupported("anyInteract"));
    }
    
    // REVIEW Should we really be buffering for contains? This seems like something we should rely entirely
    //  on the spatial API...
    
    //For geodetic coordinates, we do buffering for distance as the anyinteract tolerance is not precise enough.
    if (!m_geoParam.isCartesian() && tolerance > m_geoParam.getTol())
    {
      bgeom = bufferPolygon(geom, tolerance);
    }
    
    if (log.isDebugEnabled())
    {
      SpatialCartridge.debugLog(log, this, "anyInteract "+tolerance);
      SpatialCartridge.debugLog(log, this, "key = " + key.toStringFull());
      SpatialCartridge.debugLog(log, this, "geom = " + geom.toStringFull());
      SpatialCartridge.debugLog(log, this, "bgeom = " + bgeom.toStringFull());
    }

    // Third arg is 'isGeodetic', therefore the negation of isCartesian
    boolean r = bgeom.anyInteract(key, t, m_geoParam.isCartesian() ? "FALSE" : "TRUE");

    if (log.isDebugEnabled())
    {
      SpatialCartridge.debugLog(log, this, "res = " + r);
    }

    return r;
  }
  
}
