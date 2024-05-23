/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/SpatialCartridge.java /main/10 2015/09/22 18:57:48 hopark Exp $ */

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
 hopark      09/28/10 - add logging
 hopark      09/11/09 - Creation
 */
package com.oracle.cep.cartridge.spatial;

import com.oracle.cep.cartridge.java.CartridgeContextHolder;
import com.oracle.cep.cartridge.java.JavaTypeSystem;
import com.oracle.cep.cartridge.java.impl.JavaDatatype;
import com.oracle.cep.cartridge.java.impl.JavaTypeSystemImpl;
import com.oracle.cep.cartridge.spatial.functions.FunctionMetadataLocator;
import com.oracle.cep.cartridge.spatial.rtreeindex.IndexInfoLocator;
import com.oracle.cep.cartridge.spatial.rtreeindex.RTreeEntry;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ICartridgeRegistry;
import oracle.cep.snapshot.SnapshotContext;

import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;

public class SpatialCartridge implements ICartridge 
{
  public static final String LOGGER_NAME = "com.oracle.cep.cartridge.spatial";
    
  public static final String CARTRIDGE_NAME = "com.oracle.cep.cartridge.spatial";
  public static final String SERVER_CONTEXT_NAME = "spatial";
  public static final int DEFAULT_SRID = GeodeticParam.LAT_LNG_WGS84_SRID;
  private static final Log log = LogUtil.getLogger(LoggerType.TRACE);

  private static IndexInfoLocator indexInfoLocator = 
    new IndexInfoLocator();

  private static FunctionMetadataLocator funcMetadataLocator =
    new FunctionMetadataLocator();
  
  private static SpatialCartridge s_instance = null;
  
  ICartridgeRegistry m_registry = null;
  Map<Integer, GeodeticParam> m_geodeticParamMap = null;
  
  public static SpatialCartridge createInstance(ICartridgeRegistry registry)
    throws CartridgeException
  {
    s_instance = new SpatialCartridge(registry);
    registry.registerCartridge(CARTRIDGE_NAME, s_instance);
    Map<String, Object> props = new HashMap<String, Object>();
    GeodeticParam gparam = s_instance.getGeoParam(GeodeticParam.LAT_LNG_WGS84_SRID);
    props.put(SpatialContext.GEO_PARAM, gparam);
    registry.registerServerContext(SERVER_CONTEXT_NAME, CARTRIDGE_NAME, props);
    return s_instance;
  }
  
  public static SpatialCartridge getInstance()
  {
    return s_instance;
  }

  public SpatialCartridge(ICartridgeRegistry m_registry) throws CartridgeException 
  {
    this.m_registry = m_registry;
    m_geodeticParamMap = GeodeticParam.getAll();
  }

  @Override
  public IUserFunctionMetadataLocator getFunctionMetadataLocator()
  {
    return funcMetadataLocator;
  }

  @Override
  public ITypeLocator getTypeLocator()
  {
    return m_registry.getJavaTypeSystem();
    //engine complains "Type locator not present for cartridge spatial.", when I return null and use Geometry@spatial.
    //return null;
  }

  @Override
  public IIndexInfoLocator getIndexInfoLocator() 
  {
    return indexInfoLocator;
  }
  
  public ICartridgeRegistry getCartridgeRegistry()
  {
    return m_registry;
  }
  
  public static int getContextSRID(ICartridgeContext cartridgeContext)
  {
    SpatialContext ctx = null;
    if (cartridgeContext == null)
    {
      cartridgeContext = CartridgeContextHolder.get();
      if (cartridgeContext == null) {
          return GeodeticParam.LAT_LNG_WGS84_SRID;
      } else {
          ctx =  new SpatialContext( CartridgeContextHolder.get().getProperties() );
      }
    }
    else
    {
      ctx =  new SpatialContext(cartridgeContext.getProperties());
    }
    
    return ctx.getSrid();
  }
  
  public static GeodeticParam getContextGeodeticParam(ICartridgeContext ctx)
  {
    SpatialCartridge cat = SpatialCartridge.getInstance();
    assert (cat != null);
    int srid = getContextSRID(ctx);
    return cat.getGeoParam(srid);
  }  
  
  public GeodeticParam getGeoParam(int srid)
  {
	return m_geodeticParamMap.get(srid); 
  }  
  
  public void registerGeoParam(int srid, GeodeticParam gparam)
  {
    m_geodeticParamMap.put(srid, gparam);
  }

  public static Datatype getCQLType(Class<?> clz)
  {
    SpatialCartridge spatial = SpatialCartridge.getInstance();
    assert (spatial != null);
    ICartridgeRegistry reg = spatial.getCartridgeRegistry();
    JavaTypeSystem javaTypeLocator = (JavaTypeSystem) reg.getJavaTypeSystem();
    return (Datatype) javaTypeLocator.getCQLType(clz);
  }

  public static Datatype getJavaType(Class<?> clz)
  {
    Datatype retType = null;
    try {
      retType = new JavaDatatype(new JavaTypeSystemImpl(), clz);
    }catch (Exception e){
      log.warn("Error creating the java type for class " + clz , e);
      retType = Datatype.OBJECT;
    }
    return retType;
  }
  
  public static void debugLog(Log log, Object obj, String str)
  {
    log.debug(obj.getClass().getSimpleName()+obj.hashCode() + " : " + str);
  }
  
  static {
      //Register classes used in snapshot
      SnapshotContext.registerClass(10001,RTreeEntry.class);
  }
  
}
