/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/StoreImplIter.java /main/9 2008/12/10 18:55:56 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      06/11/07 - logging - remove ExecContext
    hopark      05/27/07 - logging support
    hopark      04/06/07 - release throws ExecException
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/04/07 - spill-over support
    parujain    11/30/06 - DoublyList mem mgmt
    najain      08/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/StoreImplIter.java /main/9 2008/12/10 18:55:56 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.ILoggable;

/**
 * WinStore Iterator
 *
 * @author najain
 */
public abstract class StoreImplIter implements TupleIterator
{
  public abstract ITuplePtr getNext() throws ExecException;

  protected boolean initialized;
  protected ILoggable logTarget;
  
  public void initialize(ILoggable target)
  {
    initialized = true;
    logTarget = target;
  }

  public void release() throws ExecException
  {
    initialized = false;
  }

  public boolean isInitialized()
  {
    return initialized;
  }
}
