/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheObjectHelper.java /main/2 2010/01/06 20:33:12 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/24/09 - synonym
    parujain    09/29/09 - Helper class
    parujain    09/29/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheObjectHelper.java /main/2 2010/01/06 20:33:12 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata.cache;

import oracle.cep.metadata.CacheObjectManager;
import oracle.cep.service.ExecContext;

class CacheObjectHelper
{
  public static CacheObjectManager getCacheObjectManager(ExecContext ec, 
                                                         CacheObject obj)
  {
    switch(obj.getType())
    { 
      case TABLE:
        return ec.getTableMgr();
      case QUERY:
        return ec.getQueryMgr();
      case VIEW:
        return ec.getViewMgr();
      case SINGLE_FUNCTION:
      case AGGR_FUNCTION:
      case SIMPLE_FUNCTION_SET:
	      return ec.getUserFnMgr();
      case WINDOW:
        return ec.getWindowMgr();
      case DEPENDENCY:
        return ec.getDependencyMgr();
      case SYNONYM:
        return ec.getSynonymMgr();
      default:
        //Currently Will be null for ObjectId and SystemObject as well
        return null;
    }
   
  }
}
