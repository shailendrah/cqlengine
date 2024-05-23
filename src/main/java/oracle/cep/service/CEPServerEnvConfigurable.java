/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/CEPServerEnvConfigurable.java /main/4 2011/04/27 18:37:35 apiper Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/26/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/CEPServerEnvConfigurable.java /main/3 2010/02/04 14:33:02 apiper Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.service;

public interface CEPServerEnvConfigurable
{
  void setEnvConfig(IEnvConfig config) throws Exception;
}
