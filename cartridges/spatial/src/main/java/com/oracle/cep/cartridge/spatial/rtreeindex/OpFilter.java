/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpFilter.java /main/8 2013/03/27 13:00:17 ybedekar Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/OpFilter.java /main/8 2013/03/27 13:00:17 ybedekar Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.Geometry;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.spatial.geometry.JGeometry;

/**
 * This operator identifies all Geometries where its MBRs intersect with the MBR
 * of a specified query geometry. This operator always returns a superset of
 * results for other interaction-based operators. In that sense, this operator
 * is an approximation of other interaction-based operators.
 *
 */
public class OpFilter extends OpBase 
{
  public static final String NAME = "filter";
  
public OpFilter(int keyPos, ICartridgeContext ctx)
  {
    super(NAME, keyPos, ctx);
  }

  public IIndexInfo[] getIndexInfo(int  paramPosition, ICartridgeContext ctx)
  {
    if (paramPosition == 0)
    {
      IndexInfo iinfo2     = new IndexInfo(IndexFactory.getInstance(), new OpFilter(1, ctx), true);
      IndexInfo[] indexInfo1 =  new IndexInfo[] { iinfo2 };
      return indexInfo1;
    }
    else if (paramPosition == 1)
    {
      IndexInfo iinfo     = new IndexInfo(IndexFactory.getInstance(), new OpFilter(0, ctx), true);
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
    
    if (paramTypes.length != 3)
      return null;
    if (!isAllGeometryType(paramTypes[0]))
      return null;
    if (!isAllGeometryType(paramTypes[1]))
      return null;
    if (!paramTypes[2].equals(Datatype.DOUBLE) && !paramTypes[2].equals(Datatype.FLOAT))
      return null;
    
    return new IndexFunctionMetadata(paramTypes, Datatype.BOOLEAN, new OpFilter(0, ctx) );
  }  

  @Override
  protected double[][] getSearchMbr(RuntimeContext ctx, Object[] args)  throws Exception
  {
    JGeometry key = (JGeometry) args[m_keyPos];
    // During compilation, we only accept Double or Float, hence the cast to Number works.
    double tolerance = ((Number) args[2]).doubleValue();
    return getSearchMbr(key, tolerance);
  }  

  //ISingleElementFunction
  @Override
  public Object execute(Object[] args) throws UDFException
  {
    return true;
  }
}
