/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/QueryType.java /main/3 2011/10/11 14:04:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of the types of queries

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    09/27/11 - subquery set operations
    vikshukl    03/14/11 - add N-ary query type
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: QueryType.java 13-feb-2006.06:02:18 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of types of queries
 *
 * @since 1.0
 */

public enum QueryType {
    SFW_QUERY, 
    BINARY_OP,
    NARY_OP,
    SET_SUBQUERY;
}

