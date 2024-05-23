/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpInside3d.java /main/2 2015/09/22 18:57:49 hopark Exp $ */

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
    alealves    11/27/09 - Data cartridge context, default package support
    hopark      06/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpInside3d.java /main/2 2015/09/22 18:57:49 hopark Exp $
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
import oracle.spatial.geometry.J3D_Geometry;
import oracle.spatial.geometry.JGeometry;

import com.oracle.cep.cartridge.spatial.Geometry3D;

public class OpInside3d extends OpBase 
{
  public static final String NAME = "INSIDE3D";


  public static IIndexInfo[] getKeyIndexInfo(int keypos, ICartridgeContext ctx)
  {
    if (keypos == 0)
    {
      IndexInfo iinfo     = new IndexInfo(IndexFactory.getInstance(), new OpInside3d(0, ctx), false);
      IIndexInfo[] indexInfo0 =  new IIndexInfo[] { iinfo };
      return indexInfo0;
    }
    else
    {
      IndexInfo iinfo2     = new IndexInfo(IndexFactory.getInstance(), new OpInside3d(1, ctx), false);
      IIndexInfo[] indexInfo1 =  new IIndexInfo[] { iinfo2 };
      return indexInfo1;  
    }
  }
  
  public OpInside3d(int keyPos, ICartridgeContext ctx)
  {
    super(NAME,keyPos, ctx);
  }

  public static IndexFunctionMetadata getMetadata(Datatype[] paramTypes, ICartridgeContext ctx)
  {
    if (paramTypes == null)
    {
      Datatype cqlType = Geometry3D.getGeometry3DType();
      paramTypes = new Datatype[]{ cqlType, cqlType, Datatype.DOUBLE };
    }
    
    // Allow for tolerance to be optional, in which case the one from the context is used.
    if (paramTypes.length != 2 && paramTypes.length != 3)
      return null;
    if (!Geometry3D.isGeometry3DType(paramTypes[0]))
      return null;
    if (!Geometry3D.isGeometry3DType(paramTypes[1]))
      return null;
    if (paramTypes.length == 3 && 
        !paramTypes[2].equals(Datatype.DOUBLE) && !paramTypes[2].equals(Datatype.FLOAT))
      return null;
    
    return new IndexFunctionMetadata(paramTypes, Datatype.BOOLEAN, new OpInside3d(0, ctx) ); 
  }  

  public IIndexInfo[] getIndexInfo(int  paramPosition, ICartridgeContext ctx)
  {
    if (paramPosition == 0)
    {
      IndexInfo iinfo2     = new IndexInfo(IndexFactory.getInstance(), new OpInside3d(1, ctx), false);
      IIndexInfo[] indexInfo1 =  new IIndexInfo[] { iinfo2 };
      return indexInfo1;  
    }
    else if (paramPosition == 1)
      return OpContain.getKeyIndexInfo(0, ctx);
    return null;
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

  @Override
  public Object execute(Object[] args) throws UDFException
  {
    J3D_Geometry geom1 = (J3D_Geometry) args[0];
    J3D_Geometry geom2 = (J3D_Geometry) args[1];

    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double tolerance = m_geoParam.getTol();
    if (args.length == 3)
      tolerance = ((Number) args[2]).doubleValue();
    
    try
    {
      return Geometry3D.inside3d(geom1, geom2, tolerance, m_geoParam.isCartesian());
    } catch (Exception e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, e);
    }
  }  
}

