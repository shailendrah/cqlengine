/* $Header: Subscription.java 20-feb-2008.05:09:21 sbishnoi Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares Subscription in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  02/20/08 - modify methods to throw CEPException
 parujain  11/01/07 - unsubscribe output
 parujain  09/28/07 - unsubscribe
 najain    03/29/06 - change SubscriptionDesc to InterfaceDriverContext
 skaluska  03/24/06 - creation
 skaluska  03/24/06 - creation
 skaluska  03/24/06 - Creation
 skaluska  03/24/06 - Creation
 skaluska  03/21/06 - Creation
 skaluska  03/21/06 - Creation
 */

/**
 *  @version $Header: Subscription.java 20-feb-2008.05:09:21 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.exceptions.CEPException;

/**
 * Subscription defines the interface to associate tables or
 * queries with TableSources and QueryOutputs.
 *
 * @author skaluska
 */
public interface Subscription
{
  /**
   * Subscribe to a table source
   * @param desc Subscription description
   * @return TableSource
   */
  public TableSource subscribe_source(InterfaceDriverContext desc) throws CEPException;

  /**
   * Unsubscribe from a table source
   * @param t TableSource
   */
  public void unsubscribe_source(InterfaceDriverContext desc) throws CEPException;

  /**
   * Subscribe to a query output
   * @param desc Subscription description
   * @return QueryOutput
   */
  public QueryOutput subscribe_output(InterfaceDriverContext desc) throws CEPException;

  /**
   * Unsubscribe from a output
   * @param desc Subscription description
   */
  public void unsubscribe_output(InterfaceDriverContext desc) throws CEPException;
}
