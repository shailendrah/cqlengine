/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/GeomSeq.java /main/1 2015/06/18 19:14:14 hopark Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/16/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/GeomSeq.java /main/1 2015/06/18 19:14:14 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial;

import java.util.HashMap;
import java.util.Map;

public class GeomSeq
{

  private static final int MAX = 10;
  private static long[] seqNos;
  private static Map<String, Integer> seqIdMap = new HashMap<String, Integer>();
  static
  {
    seqNos = new long[MAX];
    for (int i = 0; i < MAX; i++)
      seqNos[i] = 0;
  }

  public static long next(String idstr)
  {
	  synchronized(seqIdMap)
	  {
		  int id;
		  Integer idv = seqIdMap.get(idstr);
		  if (idv == null)
		  {
			  id = addId(idstr);
		  } else id = idv.intValue();
		  return seqNos[id]++;
	  }
  }

  public static int addId(String idstr)
  {
	  synchronized(seqIdMap)
	  {
		  int id = seqIdMap.size()+1;
		  seqIdMap.put(idstr, id);
		  if (seqNos.length < id)
		  {
			  int len = seqNos.length + MAX;
			  long[] t = new long[len];
			  int i = 0;
			  for (;i < seqNos.length; i++)
				t[i] = seqNos[i];
			  for (;i < len; i++)
			  	t[i] = 0;
			  seqNos = t;
		  }
		  return id;
	  }
  }
}
