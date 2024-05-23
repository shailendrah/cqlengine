/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/RTreeEntry.java /main/2 2009/12/30 21:49:27 hopark Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/RTreeEntry.java /main/2 2009/12/30 21:49:27 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rtreeindex;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.oracle.cep.cartridge.spatial.Geometry;

import oracle.spatial.geometry.JGeometry;

public class RTreeEntry implements Externalizable
{
  private static final long serialVersionUID = -8494023762427086692L;

  Object      obj;
  JGeometry   geom;
  
  public RTreeEntry(JGeometry geom, Object obj)
  {
    this.geom = geom;
    this.obj = obj;
  }
  
  public boolean equals(Object o)
  {
    if (!(o instanceof RTreeEntry)) return false;
    RTreeEntry other = (RTreeEntry) o;
//    if (!geom.equals(other.geom)) return false;
//    if (obj != null && other.obj != null)
//        return obj.equals(other.obj);
    if (geom != other.geom) return false;
    return (obj == other.obj);
  }
  
  public int hashCode()
  {
     int hash = obj == null ? 0 : obj.hashCode();
     return (geom.hashCode() << 5) + hash;
  }
  
  public JGeometry getGeometry() {return geom;}
  public Object getObject() {return obj;}

  public String toString()
  {
    StringBuilder b = new StringBuilder();
    
    b.append("type=");
    b.append(geom.getType());
    b.append(", dimensions=");
    b.append(geom.getDimensions());
    b.append(", numPoints=");
    b.append(geom.getNumPoints());
    double[] points = geom.getOrdinatesArray();
    if (points != null)
    {
      b.append(", noridinates=");
      b.append(points.length);
      b.append(", oridinates=(");
      for (int i = 0; i < points.length; i++)
      {
        b.append(points[i]);
        if (i != points.length - 1)
          b.append(",");
      }
      b.append(")");
    } else {
      b.append(", points=null");
    }
    
    b.append(obj.toString());
    return b.toString();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(obj);
      Geometry.writeObject(out, geom);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      obj = in.readObject();
      geom = Geometry.readObject(in);
  }
}

