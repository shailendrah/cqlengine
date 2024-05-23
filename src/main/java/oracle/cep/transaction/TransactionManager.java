/* $Header: pcbpel/cep/server/src/oracle/cep/transaction/TransactionManager.java /main/1 2009/02/06 15:51:04 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    02/05/09 - transaction manager
    parujain    02/05/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/transaction/TransactionManager.java /main/1 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.transaction;

import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;

@DumpDesc(autoFields=true)
public class TransactionManager
{
  ExecContext execContext;

  public TransactionManager(ExecContext ec)
  {
    execContext = ec;
  }

  /**
   * Begins a transaction
   */
  public ITransaction begin()
  {
    return execContext.getCache().beginTrans();
  }

  /**
   *  Commits a transaction
   */
  public void commit(ITransaction txn)
  {
    txn.commit(execContext);
  }

  /**
   *
   */
  public void rollback(ITransaction txn)
  {
    txn.rollback(execContext);
  }
}

