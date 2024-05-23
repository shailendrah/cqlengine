/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/TableFunctionRelSource.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/31/09 - Creation
 */

package oracle.cep.execution.operators;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;

/**
 * TableFunctionRelnSrc operator will never compute table function 
 * This will only act as a dummy operator; The table function computation
 * will happen in Join operators.
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/TableFunctionRelSource.java /main/1 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TableFunctionRelSource extends ExecOpt
{
  /**
   * Constructor for BinJoin
   * @param ec TODO
   */
  public TableFunctionRelSource(ExecContext ec)
  {
    super(ExecOptType.EXEC_TABLE_FUNC_SRC, new TableFunctionRelSourceState(ec), ec);
  }
  
  /**
   * Run operator for the specified time.
   * 
   * @param timeslice
   *          The timeslice to run
   * @return Status
   */
  protected int run(int timeslice) 
    throws CEPException
  {
    return 0;
  }
  
  /**
   * Delete operator: this removes references from the operator to any other
   * resources.
   */
  public void deleteOp()
  {
  }

  @Override
  public boolean canBeScheduled()
  {
    return false;
  }

  @Override
  public boolean shouldBeScheduled()
  {
    return false;
  }
  
}
