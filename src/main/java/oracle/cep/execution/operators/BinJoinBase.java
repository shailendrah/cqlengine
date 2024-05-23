/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinJoinBase.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/03/10 - Creation
 */

package oracle.cep.execution.operators;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.synopses.ExternalSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinJoinBase.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public abstract class BinJoinBase extends ExecOpt
{

  BinJoinBase(ExecOptType typ, MutableState m, ExecContext ec)
  {
    super(typ, m, ec);    
  }

  // TODO: We can improve this class to implement the common part of BinJoin and
  // BinStreamJoin
  public abstract void setInnerExtSyn(ExternalSynopsis eInSyn);

  public abstract void setIsExternal(boolean b);

  public abstract void setOuterScanId(int outerScanId);
  
  public abstract void setOuterFullScanId(int outerFullScanId);

  public abstract void setOuterSyn(RelationSynopsis eOutSyn);

  public abstract void setOuterTupleStorageAlloc(
      IAllocator<ITuplePtr> outerTupleAllocator);

  public abstract void setInnerTupleStorageAlloc(IAllocator<ITuplePtr> ialloc);

  public abstract Queue getOuterInputQueue();  
  
}
