/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/cache/CacheObjectFactory.java /main/2 2008/09/30 17:08:42 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 Declares CacheObjectFactory in package oracle.cep.metadata.cache.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  09/12/08 - multiple schema support
    skaluska  03/10/06 - Creation
    skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/cache/CacheObjectFactory.java /main/2 2008/09/30 17:08:42 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.metadata.cache;

/**
 * CacheObjectFactory
 *
 * @author skaluska
 */
public interface CacheObjectFactory
{
  public CacheObject newCacheObject(Object key, String schema, CacheObjectType type);
}
