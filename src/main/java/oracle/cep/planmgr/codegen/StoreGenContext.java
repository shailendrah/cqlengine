/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/StoreGenContext.java /main/4 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Context for execution store code generation

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/20/09 - stateless server
    hopark      10/07/08 - use execContext to remove statics
    rkomurav    09/17/07 - add fields
    ayalaman    08/03/06 - additional tuple spec in constructor
    najain      06/15/06 - query deletion support 
    anasrini    03/12/06 - Creation
    anasrini    03/12/06 - Creation
    anasrini    03/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/StoreGenContext.java /main/4 2009/03/30 14:46:02 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Context for code generation passed into all the individual
 * methods. This is an alternative to store specific context,
 * and different signatures at the high-level.
 *
 * @author anasrini
 * @since 1.0
 */

public class StoreGenContext {
  private ExecContext           execContext;
  private PhyStore              store;
  private TupleSpec             ts;
  private TupleSpec             secTs; 
  private TupleSpec             inpTuple;
  private TupleSpec             dataTuple;
  private int                   numPartAttrs;
  private Attr[]                partnAttrs;
  private IAllocator<ITuplePtr> partnTupleFac;
  
  /**
   * Constructor
   * @param store physical layer representation of the store
   */
  public StoreGenContext(ExecContext ec, PhyStore store) {
    execContext       = ec;
    this.store        = store;
    this.ts           = null;
    this.secTs        = null;
    this.numPartAttrs = 0;
    this.partnAttrs   = null;
    this.inpTuple     = null;
    this.dataTuple    = null;
  }

  /**
   * Constructor
   * @param store physical layer representation of the store
   * @param ts TupleSpec for the store
   */
  public StoreGenContext(ExecContext ec, PhyStore store, TupleSpec ts) {
    this.execContext  = ec;
    this.store        = store;
    this.ts           = ts;
    this.secTs        = null;
    this.numPartAttrs = 0;
    this.partnAttrs   = null;
    this.inpTuple     = null;
    this.dataTuple    = null;
  }

  /**
   * Constructor
   * @param store        physical layer representation of the store
   * @param secTs        secondary tuple spec (for those needing it. 
   *                     eg: Partion window)
   * @param numPartAttrs number of partition attributes (for those needing it. 
   *                     eg: Partion window)
   * @param partnAttrs   array of partition attributes (for those needing it. 
   *                     eg: Partion window)
   */
  public StoreGenContext(ExecContext ec, PhyStore store, TupleSpec secTs, int numPartAttrs,
      Attr[] partnAttrs) {
    this.execContext  = ec;
    this.store        = store;
    this.ts           = null;
    this.secTs        = secTs;
    this.numPartAttrs = numPartAttrs;
    this.partnAttrs   = partnAttrs;
    this.inpTuple     = null;
    this.dataTuple    = null;
  }

  // Getter methods

  public ExecContext getExecContext()
  {
    return execContext;
  }
  
  /**
   * Get the physical layer representation of the store
   * @return the physical layer representation of the store
   */
  public PhyStore getPhyStore() {
    return store;
  }

  /**
   * Get the tuple Spec
   * @return tuple Spec
   */
  public TupleSpec getTupleSpec() {
    return ts;
  }

  /**
   * Get the additional tuple Spec
   * @return secondary tuple spec 
   */
  public TupleSpec getSecTupleSpec() {
    return secTs;
  }

  /**
   * Get the number of partition attributes
   * @return the numPartAttrs
   */
  public int getNumPartAttrs() {
    return numPartAttrs;
  }

  /**
   * Get the partition attribtues
   * @return the partnAttrs
   */
  public Attr[] getPartnAttrs() {
    return partnAttrs;
  }

  /**
   * Get partition tuple factory
   * @return the partnTupleFac
   */
  public IAllocator<ITuplePtr> getPartnTupleFac() {
    return partnTupleFac;
  }

  /**
   * Set partition tuple factory
   * @param partnTupleFac the partnTupleFac to set
   */
  public void setPartnTupleFac(IAllocator<ITuplePtr> partnTupleFac) {
    this.partnTupleFac = partnTupleFac;
  }
  
  public void setInpTuple(TupleSpec inp)
  {
    this.inpTuple = inp;
  }
  
  public TupleSpec getInpTuple()
  {
    return this.inpTuple;
  }
  
  public TupleSpec getDataTuple()
  {
    return this.dataTuple;
  }
  
  public void setDataTuple(TupleSpec data)
  {
    this.dataTuple = data;
  }
}