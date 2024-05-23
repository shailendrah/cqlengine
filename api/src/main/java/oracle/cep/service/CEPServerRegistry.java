/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/CEPServerRegistry.java /main/2 2011/04/27 18:37:35 apiper Exp $ */

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
    hopark      10/20/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/service/CEPServerRegistry.java /main/1 2008/10/24 15:50:24 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CEPServerRegistry extends Remote
{
  CEPServerXface openConnection(String serviceName) throws RemoteException;
  void closeConnection(String serviceName) throws RemoteException;
}
