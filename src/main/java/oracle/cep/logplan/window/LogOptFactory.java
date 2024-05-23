/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/window/LogOptFactory.java /main/2 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/29/11 - should throw exception
    najain      03/22/06 - Creation
 */

/**
 *  @version $Header: LogOptFactory.java 22-mar-2006.17:38:26 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogicalPlanException;

/**
 * LogOptFactory
 *
 * @author najain
 */
public abstract class LogOptFactory
{
  public abstract LogOpt newLogOpt(Object ctx) throws LogicalPlanException;
}

