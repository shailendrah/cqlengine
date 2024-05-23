/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/interfaces/QryDestLocator.java /main/3 2010/11/19 07:47:47 udeshmuk Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/23/10 - propagate hb
    sbishnoi    12/09/09 - api change
    hopark      01/30/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/interfaces/QryDestLocator.java /main/3 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.interfaces;

import java.util.HashMap;

import oracle.cep.interfaces.output.FileDestination;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.service.IQueryDestLocator;

public class QryDestLocator implements IQueryDestLocator
{
  HashMap<String, QueryOutput> m_map;
  String m_path;
  
  public QryDestLocator()
  {
    m_map = new HashMap<String, QueryOutput>();
  }
  
  public void setFileDestination(String path)
  {
    m_path = path;
    m_map.put("TestQueryDest", new FileDestination(null, path));
  }
  
  public QueryOutput find(String id)
  {
    return m_map.get(id);
  }

  public QueryOutput find(String id, boolean isBatchOutput)
  {
    return find(id);
  }
  
  public QueryOutput find(String id, boolean isBatchOutput, boolean propagateHb)
  {
    return find(id);
  }
}
