/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/comparator/CacheObjectReverseComparator.java /main/1 2008/10/07 18:26:23 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    10/01/08 - cacheobject comparator
    parujain    10/01/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/comparator/CacheObjectReverseComparator.java /main/1 2008/10/07 18:26:23 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata.comparator;

import java.util.Comparator;

import oracle.cep.metadata.cache.CacheObject;

public class CacheObjectReverseComparator implements Comparator<CacheObject>
{

  public int compare(CacheObject o1, CacheObject o2)
  {
      if(o1.getId() > o2.getId())
        return -1;
      else if(o1.getId() < o2.getId())
        return 1;
      else
        return 0;
  }
	

}
