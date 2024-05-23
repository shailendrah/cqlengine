/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/PatternExecContext.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

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
    udeshmuk    08/13/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/PatternExecContext.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/3 2010/12/13 01:51:52 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.synopses.BindingSynopsis;

/**
 * This class has the data which will be shared by PatternStrmClassB 
 * execution operator and the PatternProcessor object.
 * @author udeshmuk
 */
public class PatternExecContext implements Externalizable
{
  /** boolean which when set to TRUE will enable collection of functionality
  specific stats of pattern operator */
  public static final boolean trackStats = false;

  /** Binding synopsis */
  public BindingSynopsis      bindSyn;

  /** IEvalContext */
  public IEvalContext         evalContext;
  
  /** Starting Role number for binding role */
  public int                  bindRole;
  
  /** Null Input Tuple */
  public ITuplePtr            nullInputTuple;
  
  /** is there a partition by clause */
  public boolean              hasPartnAttrs;
  
  /** set to TRUE if the input is a heart beat */
  public boolean              isHeartBeat;

  /** true if aggrs are referenced , false otherwise */
  public boolean              aggrsPresent;
  
  /** true if within clause is present */
  public boolean              isWithin;
  
  /** true if within inclusive clause is present */
  public boolean              isWithinInclusive;
  
  /** used for manual profiling */
  public long                 tempTime;

  /**
   * Empty Constructor for Deserialized Objects
   */
  public PatternExecContext()
  {
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(bindSyn);
    out.writeInt(bindRole);
    out.writeObject(nullInputTuple);
    out.writeBoolean(hasPartnAttrs);
    out.writeBoolean(isHeartBeat);
    out.writeBoolean(aggrsPresent);
    out.writeBoolean(isWithin);
    out.writeBoolean(isWithinInclusive);
    out.writeLong(tempTime);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.bindSyn = (BindingSynopsis) in.readObject();
    this.bindRole = in.readInt();
    this.nullInputTuple = (ITuplePtr) in.readObject();
    this.hasPartnAttrs = in.readBoolean();
    this.isHeartBeat = in.readBoolean();
    this.aggrsPresent = in.readBoolean();
    this.isWithin = in.readBoolean();
    this.isWithinInclusive = in.readBoolean();
    this.tempTime = in.readLong();
  }
  
  public void copyFrom(PatternExecContext pec) throws IOException
  {
    this.bindSyn.copyFrom(pec.bindSyn);
    this.bindRole = pec.bindRole;
    this.nullInputTuple = pec.nullInputTuple;
    this.hasPartnAttrs = pec.hasPartnAttrs;
    this.isHeartBeat = pec.isHeartBeat;
    this.aggrsPresent = pec.aggrsPresent;
    this.isWithin = pec.isWithin;
    this.isWithinInclusive = pec.isWithinInclusive;
    this.tempTime = pec.tempTime;
  }
}
