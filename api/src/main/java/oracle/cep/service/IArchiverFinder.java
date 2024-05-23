/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/IArchiverFinder.java /main/1 2011/05/18 04:38:12 udeshmuk Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/26/11 - Creation
 */

package oracle.cep.service;

import oracle.cep.extensibility.datasource.IArchiver;

/**
 *  @version $Header: IArchiverFinder.java 26-apr-2011.05:24:33 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public interface IArchiverFinder
{
  public void init();
  
  public void addArchiver(String name, IArchiver ds);
  
  public void removeArchiver(String name);
  
  public IArchiver findArchiver(String name);
}
