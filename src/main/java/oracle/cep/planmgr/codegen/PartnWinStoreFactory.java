/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PartnWinStoreFactory.java /main/12 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/14/09 - support for bigdecimal
    parujain    03/20/09 - stateless server
    udeshmuk    11/07/08 - make BasePartnWinStoreFactory as the superclass.
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    udeshmuk    09/24/08 - use the provided secTs instead of new dataTupleSpec
    rkomurav    09/17/07 - move instantiation code from opFactory to
                           storeFactory
    najain      03/14/07 - cleanup
    najain      12/04/06 - stores are not storage allocators
    ayalaman    08/03/06 - partition window store factory
    ayalaman    08/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PartnWinStoreFactory.java /main/12 2009/11/09 10:10:59 sborah Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.PartnWindowStoreImpl;
import oracle.cep.exceptions.CEPException;

/**
 * Factory for creation of a partition window store
 *
 * @since 1.0
 */

class PartnWinStoreFactory extends BasePartnWinStoreFactory {

  protected ExecStore newExecStore(StoreGenContext ctx)
  throws CEPException { 
    TupleSpec inpTuple = ctx.getSecTupleSpec(); 
    ctx.setInpTuple(inpTuple);
    TupleSpec dataTuple = inpTuple;
    // a partition store shares its tuples with other operators that have
    // the partition window operator as one of its input operators. Each
    // data tuple has an attribute to store information about the readers
    // that own the tuple.
    int dataUsgPos = dataTuple.addAttr(new AttributeMetadata(Datatype.BYTE,
                                    ExecStoreFactory.BYTES_INITIAL_STUBS, 0, 0));
    ctx.setDataTuple(dataTuple);
    
    ExecStore pwStore = super.newExecStore(ctx);
    Column insCol     = new Column(dataUsgPos);
    ((PartnWindowStoreImpl)pwStore).setColIns(insCol);
    return pwStore;
  }
  
  public String getStoreClassName()
  {
    return PartnWindowStoreImpl.class.getName();
  }
}
