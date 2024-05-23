/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpContain.java /main/9 2013/03/27 13:00:15 ybedekar Exp $ */

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
    alealves    11/27/09 - Data cartridge context, default package support
    hopark      06/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpContain.java /main/9 2013/03/27 13:00:15 ybedekar Exp $
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
 * This operator determines if the boundary and interior of 'GeometryA' are inside the interior of 'GeometryB'
 * (data geometry). This relationship is the reverse of INSIDE (that is, if GeometryB is
 * inside GeometryA, then GeometryA contains GeometryB).
 * 
 */
public class OpContain extends OpBase 
{
  public static final String NAME = "contain";
  
  public static IIndexInfo[] getKeyIndexInfo(int keypos, ICartridgeContext ctx)
  {
    // We need to pickup geometric paramters from the context, so create new OP.
    if (keypos == 0)
    {
      IndexInfo iinfo     = new IndexInfo(IndexFactory.getInstance(), new OpContain(0, ctx), false);
      IndexInfo[] indexInfo0 =  new IndexInfo[] { iinfo };
      return indexInfo0;
    }
    else
    {
      IndexInfo iinfo2     = new IndexInfo(IndexFactory.getInstance(), new OpContain(1, ctx), false);
      IndexInfo[] indexInfo1 =  new IndexInfo[] { iinfo2 };
      return indexInfo1;
    }
  }

public OpContain(int keyPos, ICartridgeContext ctx)
  {
    super(NAME, keyPos, ctx);
  }

  public IIndexInfo[] getIndexInfo(int  paramPosition, ICartridgeContext ctx)
  {
    if (paramPosition == 0)
    {
      IndexInfo iinfo2     = new IndexInfo(IndexFactory.getInstance(), new OpContain(1, ctx), false);
      IndexInfo[] indexInfo1 =  new IndexInfo[] { iinfo2 };
      return indexInfo1;
    }
    else if (paramPosition == 1)
      return OpInside.getKeyIndexInfo(0, ctx);
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
    
    return new IndexFunctionMetadata(paramTypes, Datatype.BOOLEAN, new OpContain(0, ctx) ); 
  }  

  @Override
  protected double[][] getSearchMbr(RuntimeContext ctx, Object[] args)  throws Exception
  {
    JGeometry key = (JGeometry) args[m_keyPos];
    
    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double tolerance = m_geoParam.getTol();
    if (args.length == 3)
      tolerance = ((Number) args[2]).doubleValue();
    
    return getSearchMbr(key, tolerance);
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
      // Swap geom and key argumemts. Also keep in mind that isInside is different than anyInteract,
      // as the former must not touch the boundaries.
      return Geometry.isInside(key, geom, tolerance, m_geoParam.getSMA(), m_geoParam.getROF(), 
          m_geoParam.isCartesian());
    } catch (Exception e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e);
    }
  }
}

