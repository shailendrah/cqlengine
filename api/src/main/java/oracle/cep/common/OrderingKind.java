/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/OrderingKind.java /main/1 2010/06/17 22:12:24 sborah Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/16/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/OrderingKind.java /main/1 2010/06/17 22:12:24 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.common;

/**
 * Enumeration of the types of Ordering contraints allowed for a query.
 * @author sborah
 *
 */
public enum OrderingKind
{
  UNORDERED, PARTITION_ORDERED, TOTAL_ORDER;
}
