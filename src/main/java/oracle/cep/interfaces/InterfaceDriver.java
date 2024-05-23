/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/InterfaceDriver.java /main/6 2009/02/17 17:42:52 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares SourceDriver in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    01/29/09 - add KeyValue
 hopark    10/09/08 - remove statics
 sbishnoi  02/19/08 - added new signature for createDriverContext
 dlenkov   07/17/07 - XML format for EPR
 rkomurav  03/27/07 - add thorws cepexception to createdrivercontext
 najain    03/29/06 - add CreateDriverContext
 skaluska  03/24/06 - Creation
 skaluska  03/24/06 - Creation
 skaluska  03/22/06 - implementation
 skaluska  03/21/06 - Creation
 skaluska  03/21/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/InterfaceDriver.java /main/6 2009/02/17 17:42:52 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import java.net.URI;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;

/**
 * An InterfaceDriver manages interfaces such as ESB or network
 * on the one hand and matches them with the tables that need
 * tuples and queries that need to deliver tuples.
 * 
 * Management of the interfaces involves opening and maintaining 
 * communication channels, depending on the type of the source
 * or destination. For example, a network socket or an ESB adaptor.
 * 
 * Matching tables/queries with sources/destinations requires a notion of subscription, and
 * delivery. Subscription defines an interest in a source or 
 * destination. Subscriptions are defined through the Subscription
 * interface. Tuples are delivered to tables through the
 * TableSource interface. Tuples are delivered from queries through
 * the QueryOutput interface.
 * 
 * @author skaluska
 */
public abstract class InterfaceDriver implements Subscription
{
  /** type of source supported by this driver */
  InterfaceType type;
  ExecContext execContext;

  /**
   * Constructor for InterfaceDriver
   * @param type
   */
  public InterfaceDriver(ExecContext ec, InterfaceType type)
  {
    execContext =ec;
    this.type = type;
  }

  /**
   * Getter for type in InterfaceDriver
   * @return Returns the type
   */
  public InterfaceType getType()
  {
    return type;
  }

  /**
   * Setter for type in InterfaceDriver
   * @param type The type to set.
   */
  public void setType(InterfaceType type)
  {
    this.type = type;
  }

  public ExecContext getExecContext()
  {
    return execContext;
  }
  
  public abstract InterfaceDriverContext CreateDriverContext(URI uri,
    XMLDocument doc, int tableId) throws CEPException;

  public abstract InterfaceDriverContext CreateDriverContext(KeyValue[] vals,
    XMLDocument doc, int tableId) throws CEPException;


  public static class KeyValue
  {
    String key;
    Object value;
    
    public KeyValue(String k, Object v)
    {
      key = k;
      value = v;
    }
    
    public String getKey() {return key;}
    public Object getValue() {return value;}
  }
}
