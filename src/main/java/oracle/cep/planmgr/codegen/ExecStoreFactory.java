/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/ExecStoreFactory.java /main/6 2009/02/23 06:47:36 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Base Factory class for the execution stores

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      02/17/09 - handle constants
    sborah      11/04/08 - changing value of BYTES_INITIAL_STUBS
    hopark      10/10/08 - remove statics
    najain      12/04/06 - stores are not storage allocators
    najain      07/21/06 - Ref-count tuples 
    anasrini    07/17/06 - return set tupleSpec if one exists 
    najain      06/18/06 - cleanup
    najain      06/16/06 - cleanup
    najain      06/13/06 - bug fix 
    anasrini    03/24/06 - fix bug in getTuplespecSpec 
    anasrini    03/21/06 - add method getTupleSpec 
    anasrini    03/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/ExecStoreFactory.java /main/6 2009/02/23 06:47:36 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.Constants;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;


/**
 * Base Factory class for the execution stores
 *
 * @since 1.0
 */

abstract class ExecStoreFactory {

  static final int BYTES_INITIAL_STUBS 
       = (int)Math.ceil(Constants.INTIAL_NUM_STUBS / Constants.BITS_PER_BYTE);

  abstract ExecStore newExecStore(StoreGenContext ctx)
    throws CEPException;

  /**
   * Get the tuple spec corresponding to a physical operator
   * @return tuple spec corresponding to a physical operator
   */
  protected TupleSpec getTupleSpec(StoreGenContext ctx) throws CEPException 
  {
    ExecContext ec = ctx.getExecContext();
    TupleSpec ts = ctx.getTupleSpec();
    if (ts != null)
      return ts;
    
    PhyOpt    op = ctx.getPhyStore().getOwnOp();
    return CodeGenHelper.getTupleSpec(ec, op);
  }

}
