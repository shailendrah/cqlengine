/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/ExternalSynopsis.java /main/2 2011/03/31 18:21:00 alealves Exp $ */

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
    parujain    11/16/07 - External Synopsis
    parujain    11/16/07 - Creation
 */

/**
 *  @version $Header: ExternalSynopsis.java 26-nov-2007.18:13:54 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.synopses;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;

/**
 * ExternalSynopsis is the interface used by operators that use an external
 * synopsis.
 *
 * @author parujain
 */
public interface ExternalSynopsis {

  /**
   * Scan external synopsis using eval context.
   * Eval context is changed, therefore it must be thread-aware.
   * 
   * @param evalCtx 
   * @return
   * @throws ExecException
   */
  public TupleIterator getScan(IEvalContext evalCtx) throws ExecException;

  /**
   * Release scan.
   * 
   * @param iter
   * @throws ExecException
   */
  public void releaseScan(TupleIterator iter) throws ExecException;
}
