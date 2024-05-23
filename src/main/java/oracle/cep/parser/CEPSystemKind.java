/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSystemKind.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    sbishnoi    02/21/08 - add CALLOUT
    hopark      05/22/07 - remove debug_level
    parujain    02/09/07 - System Node kinds
    parujain    02/09/07 - Creation
 */

/**
 *  @version $Header: CEPSystemKind.java 21-feb-2008.03:07:41 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

public enum CEPSystemKind {
    RUNTIME, THREADED, SCHEDNAME, TIMESLICE, START_CALLOUT;
}
