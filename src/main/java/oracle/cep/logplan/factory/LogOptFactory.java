/* $Header: LogOptFactory.java 24-mar-2006.14:23:15 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      03/01/06 - Creation
 */

/**
 *  @version $Header: LogOptFactory.java 24-mar-2006.14:23:15 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.factory;

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


