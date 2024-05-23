/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PatternStrmClassBState.java /main/13 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/14/09 - pattern re-org
    udeshmuk    04/09/09 - initialize time constants to MIN_EXEC_TIME
    udeshmuk    04/09/09 - total ordering optimization
    udeshmuk    10/28/08 - xmlagg orderby support in pattern
    hopark      10/10/08 - remove statics
    udeshmuk    07/12/08 - 
    rkomurav    07/08/08 - support recurring non event
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    rkomurav    08/07/07 - add readytooutputlist
    anasrini    07/12/07 - support for partition by
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PatternStrmClassBState.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/1 2009/08/28 02:43:22 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.ListIterator;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

public class PatternStrmClassBState extends MutableState
{
  /** current input tuple */
  public ITuplePtr    inputTuple;
  
  /** current input element */
  public QueueElement inputElement;
  
  @DumpDesc(ignore=true) QueueElement inputElementBuf;
  
  /** current output element */
  public QueueElement      outputElement;
  
  /** current output tuple */
  public ITuplePtr         outputTuple;
  
  /** current output kind */
  public QueueElement.Kind outputKind;
  
  /** timestamp of tuple dequeued from inputQueue */
  public long              inputTs;

  /** minimum expeted timestamp of the next input */
  public long              minNextTs;
  
  /** Sequence numbering for the incoming events to pattern */
  public long              sequence;
  
  /** Previous tuple */
  ITuplePtr                prevTuple;
 
   /** Orderby tuple */
  ITuplePtr                orderByTuple;
 
  /** unique final bindng */
  @DumpDesc(ignore=true) public Binding uniqueFinal;
  
  /** output binding */
  @DumpDesc(ignore=true) public Binding outputBinding;
  
  /** binidng B0 */
  @DumpDesc(ignore=true) public Binding bindingB0;
  
  /** activeTemp */
  @DumpDesc(ignore=true) public Binding activeTemp;
  
  /** non event temp1 */
  @DumpDesc(ignore=true) public Binding nonEventTemp1;  
  /** non event temp2 */
  @DumpDesc(ignore=true) public Binding nonEventTemp2;
  
  /** Iterator for current active bindings */
  @DumpDesc(ignore=true) public Iterator<Binding> activeIter;
  
  /** List Iterator for unsure bindings */
  @DumpDesc(ignore=true) public ListIterator<Binding> unsureIter;
  
  /** List Iterator for ready to output bindings */
  @DumpDesc(ignore=true) public ListIterator<Binding> readyOutputIter;
  
  public boolean tempConsumed;

  //total ordering optimization related
  /** Total ordering flag of last input received */
  public boolean lastInputTotalOrderingFlag;

  /** Ordering flag of next output */
  public boolean nextOutputOrderingFlag;
  
  public PatternStrmClassBState()
  {}
  
  public PatternStrmClassBState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs*/
    super(ec);
    inputTs      = Constants.MIN_EXEC_TIME;
    minNextTs    = Constants.MIN_EXEC_TIME;
    state        = ExecState.S_PRE_INIT;
    inputElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    lastInputTotalOrderingFlag = false;
    nextOutputOrderingFlag = false;
  }
  
  public void initialize(ITuplePtr nullInputTuple, Binding bindingB0)
  {
    sequence       = 0;
    this.prevTuple = nullInputTuple;
    this.bindingB0 = bindingB0;
  }
  
  void incrementSeq()
  {
    sequence++;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(minNextTs);
    out.writeLong(sequence);
    out.writeObject(prevTuple);
    out.writeBoolean(tempConsumed);
    out.writeBoolean(lastInputTotalOrderingFlag);
    out.writeBoolean(nextOutputOrderingFlag);
    out.writeObject(uniqueFinal);
    out.writeObject(outputBinding);
    out.writeObject(bindingB0);
    out.writeObject(activeTemp);
    out.writeObject(nonEventTemp1);
    out.writeObject(nonEventTemp2);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);
     this.minNextTs = in.readLong();
     this.sequence = in.readLong();
     this.prevTuple = (ITuplePtr)in.readObject();
     this.tempConsumed = in.readBoolean();
     this.lastInputTotalOrderingFlag = in.readBoolean();
     this.nextOutputOrderingFlag = in.readBoolean();
     this.uniqueFinal = (Binding) in.readObject();
     this.outputBinding = (Binding) in.readObject();
     this.bindingB0 = (Binding) in.readObject();
     this.activeTemp= (Binding) in.readObject();
     this.nonEventTemp1 = (Binding) in.readObject();
     this.nonEventTemp2 = (Binding) in.readObject();
  }
  
  public void copyFrom(PatternStrmClassBState other)
  {
    super.copyFrom(other);
    this.minNextTs = other.minNextTs;
    this.sequence = other.sequence;
    this.prevTuple = other.prevTuple;
    this.tempConsumed = other.tempConsumed;
    this.lastInputTotalOrderingFlag = other.lastInputTotalOrderingFlag;
    this.nextOutputOrderingFlag = other.nextOutputOrderingFlag;
    this.uniqueFinal = other.uniqueFinal;
    this.outputBinding = other.outputBinding;
    this.bindingB0 = other.bindingB0;
    this.activeTemp= other.activeTemp;
    this.nonEventTemp1 = other.nonEventTemp1;
    this.nonEventTemp2 = other.nonEventTemp2;
  }
}
