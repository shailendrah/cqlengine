/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */
package com.oracle.cep.cartridge.spatial;

import java.util.List;

import oracle.cep.util.CSVUtil;

/**
 * 
 * @author hoyong
 *
 */
class GeometryParser
{
  public static int getGtype(String s) throws Exception
  {
    List<String> csv = CSVUtil.parseStr(s);
    String v = csv.get(0);
    return Integer.parseInt(v);
  }

  public static int getSRID(String s) throws Exception
  {
    List<String> csv = CSVUtil.parseStr(s);
    String v = csv.get(1);
    return Integer.parseInt(v);
  }

  public static int[] getElemInfo(String s) throws Exception
  {
    List<String> csv = CSVUtil.parseStr(s);
    String v = csv.get(2);
    List<String> elems = CSVUtil.parseStr(v);
    int[] elemInfo = new int[elems.size()];
    for (int i = 0; i < elems.size(); i++)
    {
      String vv = elems.get(i);
      elemInfo[i] = Integer.parseInt(vv);
    }
    return elemInfo;
  }

  public static double[] getOrdinates(String s) throws Exception
  {
    List<String> csv = CSVUtil.parseStr(s);
    String v = csv.get(3);
    List<String> elems = CSVUtil.parseStr(v);
    double[] ords = new double[elems.size()];
    for (int i = 0; i < elems.size(); i++)
    {
      String vv = elems.get(i);
      ords[i] = Double.parseDouble(vv);
    }
    return Geometry.paddTo3D(ords);
  }
}