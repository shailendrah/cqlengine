/* $Header: pcbpel/cep/common/src/oracle/cep/service/IDataSourceFinder.java /main/2 2009/01/11 09:33:32 sbishnoi Exp $ */

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
    sbishnoi    01/08/09 - adding support for generic datasource
    parujain    07/03/08 - datasource finder
    parujain    07/03/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/service/IDataSourceFinder.java /main/2 2009/01/11 09:33:32 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.service;

import oracle.cep.extensibility.datasource.IExternalDataSource;

public interface IDataSourceFinder {
	
  public void init();
	
  public void addDataSource(String name, IExternalDataSource ds);
  
  public void removeDataSource(String name);
  
  public IExternalDataSource findDataSource(String name);
}
