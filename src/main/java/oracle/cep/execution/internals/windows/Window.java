/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/Window.java /main/4 2011/10/03 01:51:59 sbishnoi Exp $ */

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
    sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/13/11 - cleanup
    sbishnoi    08/01/08 - support for nanosecond timestamp
    parujain    03/23/07 - cleanup
    parujain    03/08/07 - Window Specification
    parujain    03/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/execution/internals/windows/Window.java /main/3 2008/08/18 21:52:48 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.windows;

import oracle.cep.common.EventTimestamp;

public abstract class Window 
{
  public abstract boolean visibleW(EventTimestamp ts, EventTimestamp visTs);
  
  public abstract boolean expiredW(EventTimestamp ts, EventTimestamp expTs); 
}
