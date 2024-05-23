/* $Header: pcbpel/cep/server/src/oracle/cep/transaction/ITransaction.java /main/1 2009/02/06 15:51:04 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    02/05/09 - transaction mgmt
    parujain    02/05/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/transaction/ITransaction.java /main/1 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.transaction;

import oracle.cep.service.ExecContext;

public interface ITransaction
{
  /**
   * Commit the changes within the transaction
   */
  public void commit(ExecContext ec);

  /**
   * Rollback the changes within the transaction
   */
  public void rollback(ExecContext ec);
}


