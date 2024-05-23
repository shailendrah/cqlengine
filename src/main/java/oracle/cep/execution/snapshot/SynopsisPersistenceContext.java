/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/SynopsisPersistenceContext.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/04/16 - Creation
 */
package oracle.cep.execution.snapshot;

import java.util.HashSet;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/SynopsisPersistenceContext.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SynopsisPersistenceContext implements IPersistenceContext
{
  private int scanId;

  private HashSet cache;
  
  /** Used by ExtensibleIndex */
  private int role;
  
  private boolean validRole;
  
  private boolean isSilent;
  
  public SynopsisPersistenceContext()
  {    
    this.role = -1;
    this.validRole = false;
  }
  
  public SynopsisPersistenceContext(int id)
  {
    this.scanId = id;    
    this.validRole = false;
  }
  
  @Override
  public int getScanId()
  {
    return scanId;
  }
  
  @Override
  public void setScanId(int id)
  {
    this.scanId = id;
  }
  
  @Override
  public HashSet getCache()
  {
    return cache;
  }
  
  @Override
  public void setCache(HashSet cache)
  {
    this.cache = cache;
  }

  public int getRole()
  {
    return role;
  }

  public void setRole(int role)
  {
    this.role = role;
    // Plesae note that range of valid role is 0-17
    // See oracle.cep.execution.internals.IEvalContext
    this.validRole = role > -1 && role < 18;
  }

  @Override
  public boolean isRoleSet()
  {
    return this.validRole;
  }

  public boolean isSilent()
  {
    return isSilent;
  }

  public void setSilent(boolean isSilent)
  {
    this.isSilent = isSilent;
  }
}
