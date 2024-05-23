/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrKind.java /main/3 2012/05/02 03:05:58 pkali Exp $ */

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
    pkali       03/29/12 - added groupby const
    rkomurav    03/05/07 - add corrattr
    najain      02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrKind.java /main/3 2012/05/02 03:05:58 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.attr;

/**
 * Enumeration of the different logical plan attribute types
 *
 * @since 1.0
 */

public enum AttrKind {
    UNNAMED, NAMED, AGGR, CORR, GROUPBY;
}
