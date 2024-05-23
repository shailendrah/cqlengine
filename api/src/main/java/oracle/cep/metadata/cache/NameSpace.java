/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/metadata/cache/NameSpace.java /main/5 2010/01/06 20:33:11 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY) 
    parujain    11/24/09 - synonym
    parujain    09/28/09 - Dependency
    parujain    03/05/07 - window object
    parujain    02/13/07 - system startup
    parujain    07/10/06 - Namespace Implementation 
    parujain    07/10/06 - Namespace Implementation 
    parujain    07/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/metadata/cache/NameSpace.java /main/5 2010/01/06 20:33:11 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata.cache;

/**
 * NameSpace is specific for every ObjectType.  
 * Two object types can have the same NameSpace like View and Table have NameSpace SOURCE
 */
public enum NameSpace{
  OBJECTID,
  SOURCE,
  QUERY,
  USERFUNCTION,
  SYSTEM,
  SYNONYM,
  DEPENDENCY,
  WINDOW;
}