/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/IQueryDestLocator.java /main/3 2010/11/19 07:47:47 udeshmuk Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    IEnvObjRegistry provides a mechanism for an environment to register 
    objects and allow server to reference it by an id.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/23/10 - propagate hb
    sbishnoi    12/09/09 - batching events support
    hopark      01/28/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/IQueryDestLocator.java /main/3 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.service;

import oracle.cep.interfaces.output.QueryOutput;

public interface IQueryDestLocator
{
  QueryOutput find(String id);

  QueryOutput find(String id, boolean isBatchEvents);

  QueryOutput find(String id, boolean isBatchEvents, boolean propagateHb);
}
