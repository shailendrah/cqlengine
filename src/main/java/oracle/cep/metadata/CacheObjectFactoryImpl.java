/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/CacheObjectFactoryImpl.java /main/7 2010/01/06 20:33:11 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares CacheObjectFactoryImpl in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  11/24/09 - synonym
    parujain  09/12/08 - multiple schema support
    parujain  03/05/07 - window object
    parujain  02/13/07 - system startup
    dlenkov   11/16/06 - suport of overloads
    anasrini  07/06/06 - support for aggregate functions 
    anasrini  06/12/06 - support for user functions 
    najain    05/14/06 - view support
    skaluska  03/13/06 - Creation
    skaluska  03/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/CacheObjectFactoryImpl.java /main/7 2010/01/06 20:33:11 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectFactory;
import oracle.cep.metadata.cache.CacheObjectType;

/**
 * CacheObjectFactoryImpl
 * 
 * @author skaluska
 */
public class CacheObjectFactoryImpl implements CacheObjectFactory
{
  /**
   * Constructor for CacheObjectFactoryImpl
   */
  public CacheObjectFactoryImpl()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.metadata.cache.CacheObjectFactory#newCacheObject(java.lang.Object,
   *      oracle.cep.metadata.cache.CacheObjectType)
   */
  public CacheObject newCacheObject(Object key, String schema, CacheObjectType type)
  {
    switch (type)
    {
      case OBJECTID:
        return new ObjectId(((Integer) key).intValue());
      case TABLE:
        return new Table((String) key, schema);
      case QUERY:
        return new Query((String)key, schema);
      case VIEW:
        return new View((String)key, schema);
      case SINGLE_FUNCTION:
        return new SimpleFunction((String)key, schema);
      case AGGR_FUNCTION:
        return new AggFunction((String)key, schema);
      case SIMPLE_FUNCTION_SET:
	      return new SimpleFunctionSet((String)key, schema);
      case SYSTEM_STATE:
        return new SystemObject((String)key, schema);
      case WINDOW:
        return new Window((String)key, schema);
      case DEPENDENCY:
        return new Dependency((String)key, schema);
      case SYNONYM:
        return new Synonym((String)key, schema);
      default:
        return null;
    }
  }

}
