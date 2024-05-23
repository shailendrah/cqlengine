/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/execution/snapshot/IPersistenceContext.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/execution/snapshot/IPersistenceContext.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public interface IPersistenceContext
{
  public int getScanId();
  
  public void setScanId(int id);
  
  public HashSet getCache();
  
  public void setCache(HashSet cache);
  
  public int getRole();
  
  public void setRole(int role);
  
  public boolean isRoleSet();
  
  public void setSilent(boolean val);
  
  public boolean isSilent();
}