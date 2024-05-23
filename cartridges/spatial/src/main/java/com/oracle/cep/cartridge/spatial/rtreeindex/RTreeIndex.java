/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/RTreeIndex.java /main/7 2015/10/01 22:29:48 hopark Exp $ */

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
package com.oracle.cep.cartridge.spatial.rtreeindex;

import com.oracle.cep.cartridge.spatial.Geometry;
import com.oracle.cep.cartridge.spatial.SpatialCartridge;

import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.indexes.IIndex;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.RTree;

import org.apache.commons.logging.Log;

import java.util.logging.Level;

public class RTreeIndex implements IIndex
{
  protected static Log log = LogUtil.getLogger(LoggerType.CUSTOMER);//LogFactory.getLog(SpatialCartridge.LOGGER_NAME);
  
  ICartridge                cartridge;
  RTree                     rtree;
  int						id;
  static int s_nextId = 0;
  
  ThreadLocal<RuntimeContext> runtimeContext = new ThreadLocal<RuntimeContext>()
  {
    protected synchronized RuntimeContext initialValue()
    {
      return new RuntimeContext(rtree);
    }
  };

  public RTreeIndex()
  {
    // TODO nd, ns mf should be from hint..
    int nd = 2;
    int ns = 6;
    int mf = 1;
    rtree = new RTree(nd, ns, mf);
    id = s_nextId++;
  }

  	public int getId() { return id;}
  	
  public int getEntryCount()
  {
	  return rtree.getEntryCount();
  }
  
  public void insert(Object key, Object val)
  {
    synchronized(rtree)
    {
      JGeometry geom = (JGeometry) key;
      rtree.addEntry(Geometry.get2dMbr(geom), val);
  
      if (log.isDebugEnabled())
      {
        SpatialCartridge.debugLog(log, this, " insert " + geom.toString() + " : count=" + rtree.getEntryCount());
      }
    }
  }

  public void delete(Object key, Object val)
  {
    synchronized(rtree)
    {
      JGeometry geom = (JGeometry) key;
      rtree.removeEntry(Geometry.get2dMbr(geom), val);
      if (log.isDebugEnabled())
      {
        SpatialCartridge.debugLog(log, this, " delete " + geom.toString() + " : count=" + rtree.getEntryCount());
      }
    }
  }

  @Override
  public void update(Object oldkey, Object newkey, Object oldval, Object newval)
  {
    synchronized(rtree)
    {
      delete(oldkey, oldval);
      insert(newkey, newval);
    }
  }

  @Override
  public void startScan(Object cbctx, Object[] args)
  {
    OpBase op = (OpBase) cbctx;

    RuntimeContext rctx = runtimeContext.get();
    rctx.op = op;
    rctx.id = id;
    long startTime = 0;
    try
    {
      if (log.isDebugEnabled()) startTime = System.nanoTime();
      rctx.iterator = op.startScan(rctx, args);
      if (log.isDebugEnabled()) {
    	  long endTime = System.nanoTime();
    	  SpatialCartridge.debugLog(log, this, " startScan took : "+(endTime - startTime));
      }
    }
    catch(Exception e)
    {
      LogUtil.severe(LoggerType.TRACE, e.toString());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }

  @Override
  public Object getNext()
  {
    RuntimeContext rctx = runtimeContext.get();

    if (rctx.iterator == null)
      return null;

    while (rctx.iterator.hasNext())
    {
      Object n = rctx.op.getNext(rctx.iterator);
      //RTreeEntry entry = (RTreeEntry) n;
      //return entry.getObject();
      return n;
    }
    releaseScan();
    return null;
  }

  @Override
  public void releaseScan()
  {
    RuntimeContext rctx = runtimeContext.get();
    if (rctx.op != null)
    {
      rctx.op.releaseScan(rctx);
    }
    rctx.iterator = null;
    rctx.op = null;
  }

}
