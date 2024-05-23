/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/DependencyType.java /main/1 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/22/09 - dependent types
    parujain    09/22/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/DependencyType.java /main/1 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

/**
 * DependencyType informs about the type of dependent
 * 
 * @author parujain
 *
 */
public enum DependencyType
{
  TABLE,
  QUERY,
  VIEW,
  FUNCTION,
  WINDOW;
}
