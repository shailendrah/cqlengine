/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpWithinDistance.java /main/11 2015/10/01 22:29:49 hopark Exp $ */

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
    ybedekar    08/02/12 - Bug14385180
    alealves    11/27/09 - Data cartridge context, default package support
    hopark      06/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpWithinDistance.java /main/11 2015/10/01 22:29:49 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rtreeindex;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

/**
 * Given a set of locations, this operator returns all locations that are within
 * a specified distance from a query location.This operator is one of the
 * simplest spatial operators and one can start the proximity analysis with it.
 * This operator facilitates analysis such as the identification of customers
 * within a quarter-mile radius of a store site.
 *
 */
public class OpWithinDistance extends OpBase 
{
  public static final String NAME = "withindistance";

  public OpWithinDistance(int keyPos, ICartridgeContext ctx)
  {
    super(NAME, keyPos, ctx);
  }

  public static IndexFunctionMetadata getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
  {
    if (paramTypes == null)
    {
      Datatype cqlType = Geometry.getGeometryType();
      paramTypes = new Datatype[]{ cqlType, cqlType, Datatype.DOUBLE };
    }
    
    // Allow for an optional 4th param of double with the tolerance
    if (paramTypes.length != 3 && paramTypes.length != 4)
      return null;
    
    if (!isAllGeometryType(paramTypes[0]))
      return null;
    if (!isAllGeometryType(paramTypes[1]))
      return null;
    if (!paramTypes[2].equals(Datatype.DOUBLE) && !paramTypes[2].equals(Datatype.FLOAT))
      return null;
    if (paramTypes.length == 4 && 
        !paramTypes[3].equals(Datatype.DOUBLE) && !paramTypes[2].equals(Datatype.FLOAT))
      return null;
    
    return new IndexFunctionMetadata(paramTypes, Datatype.BOOLEAN, new OpWithinDistance(0, ctx) );
  }  

  public IIndexInfo[] getIndexInfo(int  paramPosition, ICartridgeContext ctx)
  {
    if (paramPosition == 0)
    {
      IndexInfo iinfo1     = new IndexInfo(IndexFactory.getInstance(), new OpWithinDistance(1, ctx), false);
      IIndexInfo[] indexInfo1 =  new IIndexInfo[] { iinfo1 };
      return indexInfo1;
    }else if (paramPosition == 1)
    {
      IndexInfo iinfo     = new IndexInfo(IndexFactory.getInstance(), new OpWithinDistance(0, ctx), false);
      IIndexInfo[] indexInfo0 =  new IIndexInfo[] { iinfo };
      return indexInfo0;
    }
    return null;
  }
 
  @Override
  protected double[][] getSearchMbr(RuntimeContext ctx, Object[] args)  throws Exception
  {
	long startTime = System.currentTimeMillis();
    JGeometry key = (JGeometry) args[m_keyPos];
    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double distance = ((Number) args[2]).doubleValue();
    return getSearchMbr(key, distance);
  }  

  @Override
  public Object execute(Object[] args) throws UDFException
  {
    JGeometry geom = (JGeometry) args[0];
    JGeometry key = (JGeometry) args[1];
    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double distance = ((Number) args[2]).doubleValue();
    double tolerance = m_geoParam.getTol();
    
    if (args.length == 4)
      tolerance = ((Number) args[3]).doubleValue();

    if (log.isDebugEnabled())
    {
      SpatialCartridge.debugLog(log, this, "execute-----");
  	  Geometry g = Geometry.to_Geometry(geom);
      SpatialCartridge.debugLog(log, this, "arg0:"+g.toJsonString());
  	  g = Geometry.to_Geometry(key);
      SpatialCartridge.debugLog(log, this, "arg1:"+g.toJsonString());
      SpatialCartridge.debugLog(log, this, "arg2:"+distance);
      SpatialCartridge.debugLog(log, this, "arg3:"+tolerance);
    }

    long startTime = System.nanoTime();
    try
    {
      if (log.isDebugEnabled())
      {
        log.debug(geom.toStringFull());
        log.debug(key.toStringFull());
      }
      double res = Geometry.distance(geom, key, tolerance, 
          m_geoParam.getSMA(), m_geoParam.getROF(), m_geoParam.isCartesian());
      if (log.isDebugEnabled())
      {
        log.debug("result="+res+ " ,reqDist="+distance);
      }
      return (res < distance);
    } 
    catch (Exception e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e);
    } finally {
        if (log.isDebugEnabled()) {
      	  long endTime = System.nanoTime();
      	  SpatialCartridge.debugLog(log, this, " execute took : "+(endTime - startTime));
        }
    }    
  }
}

