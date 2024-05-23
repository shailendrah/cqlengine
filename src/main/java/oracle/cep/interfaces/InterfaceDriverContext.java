/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/InterfaceDriverContext.java /main/5 2010/11/19 07:47:47 udeshmuk Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/21/10 - propagate hb
    sbishnoi    12/09/09 - batching events support
    hopark      01/29/09 - api change
    hopark      10/09/08 - remove statics
    najain      03/29/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/InterfaceDriverContext.java /main/5 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.interfaces;

import oracle.cep.service.ExecContext;

/**
 * An InterfaceDriverContext contains context for each driver. Each driver
 * has a subclass for creating its own driver context.
 * 
 * @author najain
 */
public abstract class InterfaceDriverContext
{
  /** type of source supported by this driver */
  InterfaceType type;
  ExecContext   execContext;
  InterfaceDriver driver;
  boolean isBatchOutput;
  boolean propagateHeartbeat;
  
  /**
   * Constructor for InterfaceDriverContext
   * @param type
   */
  public InterfaceDriverContext(ExecContext ec, InterfaceType type)
  {
    execContext =ec;
    this.type = type;
  }

  /**
   * Getter for type in InterfaceDriverContext
   * @return Returns the type
   */
  public InterfaceType getType()
  {
    return type;
  }

  /**
   * Setter for type in InterfaceDriverContext
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
  
  public void setDriver(InterfaceDriver driver)
  {
    this.driver = driver;
  }
  
  public InterfaceDriver getDriver()
  {
    return driver;
  }

  public void setBatchOutput(boolean isBatchOutput)
  { 
    this.isBatchOutput = isBatchOutput;
  }

  public boolean isBatchOutput()
  {
    return isBatchOutput;
  }

  public void setPropagateHeartbeat(boolean propagateHb)
  { 
    this.propagateHeartbeat = propagateHb;
  }

  public boolean isPropagateHeartbeat()
  {
    return this.propagateHeartbeat;
  }
}
