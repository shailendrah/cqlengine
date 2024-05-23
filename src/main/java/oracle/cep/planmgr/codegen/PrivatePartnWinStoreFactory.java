/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/PrivatePartnWinStoreFactory.java /main/4 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/20/09 - stateless server
    udeshmuk    11/05/08 - rename the class
    hopark      10/19/08 - remove statics
    udeshmuk    10/11/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/PrivatePartnWinStoreFactory.java /main/4 2009/03/30 14:46:02 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.PrivatePartnWindowStoreImpl;
import oracle.cep.exceptions.CEPException;

/**
 * Factory for creation of a partition window store
 *
 * @since 1.0
 */

class PrivatePartnWinStoreFactory extends BasePartnWinStoreFactory 
{
  protected ExecStore newExecStore(StoreGenContext ctx)
  throws CEPException {
    ctx.setInpTuple(ctx.getSecTupleSpec());
    ctx.setDataTuple(ctx.getInpTuple());
    return super.newExecStore(ctx);
  }
  
  public String getStoreClassName()
  {
    return PrivatePartnWindowStoreImpl.class.getName();
  }
}
