/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymTableEntryType.java /main/2 2013/12/11 05:32:56 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
   Enumeration of the symbol table entry types

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/09/13 - support of new keyword for pseudo column QUERYNAME
    rkomurav    05/27/07 - 
    anasrini    05/18/07 - Creation
    anasrini    05/18/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymTableEntryType.java /main/2 2013/12/11 05:32:56 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

/**
 * Enumeration of the symbol table entry types supported
 *
 * @since 1.0
 */

enum SymTableEntryType {
  
  SOURCE,
  ATTR,
  CORR,
  PSEUDO;
}

