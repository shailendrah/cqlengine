/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheObjectType.java /main/6 2010/01/06 20:33:12 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares CacheObjectType in package oracle.cep.metadata.cache.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  03/05/07 - window object
    parujain  02/08/07 - System starup
    dlenkov   11/16/06 - added SIMPLE_FUNCTION_SET
    anasrini  07/06/06 - support for aggregate functions 
    najain    04/28/06 - add view
    skaluska  03/10/06 - Creation
    skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheObjectType.java /main/6 2010/01/06 20:33:12 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.metadata.cache;

/**
 * CacheObjectType
 *
 * @author skaluska
 */
public enum CacheObjectType
{
  OBJECTID,
  TABLE,
  QUERY,
  VIEW,
  SINGLE_FUNCTION,
  AGGR_FUNCTION,
  SIMPLE_FUNCTION_SET,
  SYSTEM_STATE,
  WINDOW,
  SYNONYM,
  DEPENDENCY;
}
