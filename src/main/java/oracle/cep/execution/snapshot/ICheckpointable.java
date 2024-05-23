/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/ICheckpointable.java hopark_cqlsnapshot/4 2016/02/26 11:55:07 hopark Exp $ */

/* Copyright (c) 2015, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/15/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/ICheckpointable.java hopark_cqlsnapshot/4 2016/02/26 11:55:07 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.snapshot;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import oracle.cep.exceptions.CEPException;


public interface ICheckpointable
{
  /*
   * Create snapshot of operator states
   * It can generate either full snapshot or incremental changes.
   */
  void createSnapshot(ObjectOutputStream output, boolean fullSnapshot) throws CEPException;

  /*
   * Load the operator states from the given snapshot.
   * If fullSnapshot is set, the operator states should be loaded from the full snapshot.
   * Otherwise, the input snapshot is incremental changes and should be applied to current snapshot.
   */
  void loadSnapshot(ObjectInputStream input, boolean fullSnapshot) throws CEPException;
  
  /**
   * Returns true if all stateful objects of operator can be persisted incrementally.
   * <nl><em>
   * Example: RangeWindow is an operator where we can store all stateful objects(synopsis)
   * using append-only journal entries.
   * @return
   */
  boolean usesJournaling();
  
  /**
   * Returns true if the operator has few objects which can be persisted incrementally.
   * In this case, an operator can create a full snapshot of only those objects which are
   * not incrementally updated. 
   * Few objects in operator can persist the state using journal entry which will be
   * saved on every incremental snapshot between two full snapshots.
   * <em>
   * Example: BinJoin is an operator where we can store either left or right synopsis
   * using journal entry if its respective input(left or right) is a silent source.
   * @return
   */
  boolean usesPartialJournaling();
  
  /**
   * Start the batch. Typically initialized the bytearraystream for incremental snapshots for the batch
   */
  void startBatch(boolean fullSnapshot) throws CEPException;
  
  /**
   * End the batch. Typically clean up the bytearraystream used for incremental snapshots for the batch
   */
  void endBatch() throws CEPException;
}
