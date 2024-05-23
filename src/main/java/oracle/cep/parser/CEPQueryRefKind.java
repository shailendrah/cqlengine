/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryRefKind.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    udeshmuk    05/12/11 - support for alter query start time DDL
    sborah      06/16/10 - ordering constraint
    parujain    05/09/07 - monitoring
    parujain    02/28/07 - stop query
    dlenkov     08/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryRefKind.java /main/4 2010/06/17 22:12:24 sborah Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

public enum CEPQueryRefKind {
    START, DROP, ADDDEST, VIEW, STOP, ENABLE_MONITOR, DISABLE_MONITOR,
    ORDERING_CONSTRAINT, SETSTARTTIME;
}
