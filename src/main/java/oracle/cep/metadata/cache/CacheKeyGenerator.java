/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheKeyGenerator.java /main/3 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    12/18/08 - fix bug
    parujain    09/24/08 - cache key gen
    parujain    09/24/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheKeyGenerator.java /main/3 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata.cache;

public class CacheKeyGenerator{
	
  public static CacheKey createCacheKey
             (Object key, String schema, NameSpace nameSpace)
  {
    switch(nameSpace)
    {
      case OBJECTID : 
      case DEPENDENCY:
      case SYSTEM   : return new CacheKey(key, nameSpace);
      
      default: return new CacheKey(key, schema, nameSpace);
    }
  }

}
