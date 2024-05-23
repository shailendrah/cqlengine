/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/JMSDriverContext.java /main/3 2009/02/17 17:42:52 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Context for a JMS Driver to instantiate a JMS Destination connector

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/29/09 - api change
    hopark      10/09/08 - remove statics
    anasrini    09/12/06 - add connFactName and destName
    anasrini    08/18/06 - JMS Driver Context
    anasrini    08/18/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/JMSDriverContext.java /main/3 2009/02/17 17:42:52 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.interfaces;

import oracle.cep.service.ExecContext;

/**
 * Context for a JMS Driver to instantiate a JMS Destination connector
 *
 * @since 1.0
 */

class JMSDriverContext extends InterfaceDriverContext {

  private String jndiConnFactName; 
  private String jndiDestName;
  private int    qid;

  /**
   * Constructor for JMSDriverContext
   * @param jndiConnFactName JNDI name of the connection factory
   * @param jndiDestName JNDI name of the destination (topic or queue)
   * @param qid id of the query for which this is an output
   */
  JMSDriverContext(ExecContext ec, String jndiConnFactName, String jndiDestName, int qid)
  {
    super(ec, InterfaceType.JMS);
    this.jndiConnFactName = jndiConnFactName;
    this.jndiDestName     = jndiDestName;
    this.qid              = qid;
  }

  /**
   * Getter for JNDI connection factory name
   * @return Returns the JNDI name to which the connection factory is bound
   */
  String getConnFactName()
  {
    return jndiConnFactName;
  }

  /**
   * Getter for JNDI destination name
   * @return Returns the JNDI name to which the destination is bound
   */
  String getDestName()
  {
    return jndiDestName;
  }

  /**
   * Getter for query id
   * @return Returns the query id for which this is an output
   */
  int getQueryId()
  {
    return qid;
  }

}
