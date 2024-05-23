/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpAnyinteract.java /main/10 2013/03/27 13:00:14 ybedekar Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    ybedekar    08/02/12 - Bug14385180
    hopark      12/08/09 - Geometry method change
    alealves    11/27/09 - Data cartridge context, default package support
    hopark      06/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpAnyinteract.java /main/10 2013/03/27 13:00:14 ybedekar Exp $
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

/**
 * This operator determines if a Geometry has any interaction with another
 * Geometry. This interaction is true if either the boundary or interior of 'GeometryA'
 * intersects with the boundary or interior of 'GeometryB'. In other words, if there is
 * any intersection between the two geometries (i.e., any of the 
 * interactions inside,contains,touch,covers etc. are true).
 *
 */
public class OpAnyinteract extends OpBase 
{
  public static final String NAME = "anyinteract";

  public OpAnyinteract(int keyPos, ICartridgeContext ctx)
  {
    super(NAME, keyPos, ctx);
  }

  public IIndexInfo[] getIndexInfo(int  paramPosition, ICartridgeContext ctx)
  {
    // We need to pickup geometric paramters from the context, so create new OP.
    if (paramPosition == 0)
    {
      IndexInfo iinfo2     = new IndexInfo(IndexFactory.getInstance(), new OpAnyinteract(1, ctx), false);
      IndexInfo[] indexInfo1 =  new IndexInfo[] { iinfo2 };
      return indexInfo1;
    }
    else if (paramPosition == 1)
    {
      IndexInfo iinfo     = new IndexInfo(IndexFactory.getInstance(), new OpAnyinteract(0, ctx), false);
      IndexInfo[] indexInfo0 =  new IndexInfo[] { iinfo };
      return indexInfo0;
    }
    return null;
  }

  public static IndexFunctionMetadata getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
  {
    if (paramTypes == null)
    {
      Datatype cqlType = Geometry.getGeometryType();
      paramTypes = new Datatype[]{ cqlType, cqlType, Datatype.DOUBLE };
    }
    
    // Allow for tolerance to be optional, in which case the one from the context is used.
    if (paramTypes.length != 2 && paramTypes.length != 3)
      return null;
    if (!isAllGeometryType(paramTypes[0]))
      return null;
    if (!isAllGeometryType(paramTypes[1]))
      return null;
    if (paramTypes.length == 3 && 
        !paramTypes[2].equals(Datatype.DOUBLE) && !paramTypes[2].equals(Datatype.FLOAT))
      return null;
    
    return new IndexFunctionMetadata(paramTypes, Datatype.BOOLEAN, new OpAnyinteract(0, ctx) ); 
  }  

  @Override
  protected double[][] getSearchMbr(RuntimeContext ctx, Object[] args)
  {
    JGeometry key = (JGeometry) args[m_keyPos];
    
    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double tolerance = m_geoParam.getTol();
    if (args.length == 3)
      tolerance = ((Number) args[2]).doubleValue();

    if (!m_geoParam.isCartesian() && tolerance > m_geoParam.getTol())
    {
      key = bufferPolygon(key, tolerance);
    }
    return Geometry.get2dMbr(key);
  }  

  //ISingleElementFunction
  @Override
  public Object execute(Object[] args) throws UDFException
  {
    JGeometry geom = (JGeometry) args[0];
    JGeometry key = (JGeometry) args[1];

    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double tolerance = m_geoParam.getTol();
    if (args.length == 3)
      tolerance = ((Number) args[2]).doubleValue();
    
    try
    {
      return anyInteract(geom, key, tolerance);
    } catch (Exception e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e);
    }
  }
}

