/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IExternalConnection.java /main/5 2010/06/24 06:26:52 sbishnoi Exp $ */

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
    sbishnoi    03/31/10 - adding supportsPredicate
    sbishnoi    02/26/10 - adding getCapabilities API method
    parujain    02/06/09 - bug fix
    sbishnoi    01/02/09 - Creation
 */

package oracle.cep.extensibility.datasource;

import java.util.List;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.extensibility.datasource.ExternalFunctionMetadata;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IExternalConnection.java /main/5 2010/06/24 06:26:52 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public interface IExternalConnection
{
  /**
   * Creates a IExternalPreparedStatement object for sending SQL statements to
   * the database.
   * @param relName name of relation
   * @param pred Predicate Clause
   * @return a new IExternalPreparedStatement object
   */
  public IExternalPreparedStatement prepareStatement(String relName,  
                                                 List<String> relAttrs,
                                                    Predicate pred) 
    throws Exception;
  
  /**
   * Releases all the resources acquired by Connection without any wait.
   */
  public void close() throws Exception;
  
  /**
   * Get array of supported operations in a predicate clause
   * @throws Exception
   */
  public List<ExternalFunctionMetadata> getCapabilities() throws Exception;
  
  /**
   * Check if the connection supports given predicate
   * @param pred
   * @return
   * @throws CEPException
   */
  public boolean supportsPredicate(Predicate pred) throws Exception;
  
  public void validateSchema(int numAttrs,String[] attrNames, AttributeMetadata[] attrMetadata) throws Exception;
}
