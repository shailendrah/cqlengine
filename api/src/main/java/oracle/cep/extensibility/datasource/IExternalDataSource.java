/* $Header: pcbpel/cep/common/src/oracle/cep/extensibility/datasource/IExternalDataSource.java /main/2 2009/01/04 04:22:53 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/02/09 - Creation
 */

package oracle.cep.extensibility.datasource;

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/extensibility/datasource/IExternalDataSource.java /main/2 2009/01/04 04:22:53 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public interface IExternalDataSource
{
  /**
   * Attempts to get a connection to the data source
   * @return a connection to the data source
   */  
  public IExternalConnection getConnection() throws Exception;
}
