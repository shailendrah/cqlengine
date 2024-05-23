/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/TupleKind.java /main/5 2012/10/25 15:53:41 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/11/17 - added UPSERT kind
    pkali       10/22/12 - added START kind
    sbishnoi    10/30/07 - added UPDATE
    mthatte     09/05/07 - Adding toString()
    najain      03/12/07 - bug fix
    najain      05/18/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/TupleKind.java /main/5 2012/10/25 15:53:41 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

/**
 * @author najain
 *
 */



public enum TupleKind
{
  PLUS, MINUS, HEARTBEAT, UPDATE, START, UPSERT
}
// START - kind is used by upstream components to notify 
// the query start event. START is used only in BEAM context.
