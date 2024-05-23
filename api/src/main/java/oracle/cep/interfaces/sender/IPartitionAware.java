/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/interfaces/sender/IPartitionAware.java /main/1 2014/05/21 19:18:48 sbishnoi Exp $ */

/* Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/08/14 - Creation
 */
package oracle.cep.interfaces.sender;
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/interfaces/sender/IPartitionAware.java /main/1 2014/05/21 19:18:48 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public interface IPartitionAware
{
  public IPartitionContext getPartitionContext();
}