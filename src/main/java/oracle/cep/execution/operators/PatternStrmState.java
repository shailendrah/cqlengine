/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/PatternStrmState.java /main/15 2008/10/24 15:50:21 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      12/06/07 - cleanup spill
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    rkomurav    06/27/07 - add processCurrentFinal
    rkomurav    05/30/07 - add aggrfactory
    rkomurav    05/29/07 - change state init
    rkomurav    04/11/07 - add inputtuple cache
    rkomurav    04/02/07 - add outputTs
    najain      03/29/07 - cleanup
    najain      03/14/07 - cleanup
    rkomurav    03/16/07 - add prev. tuple
    rkomurav    03/10/07 - creation.
    najain      03/12/07 - bug fix
    rkomurav    03/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/PatternStrmState.java /main/15 2008/10/24 15:50:21 hopark Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.logging.DumpDesc;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.ITuplePtr;

public class PatternStrmState extends MutableState
{
  /** timestamp of tuple dequeued from inputQueue */
  long               inputTs;
  
  /** timestamp of output tuple */
  long               outputTs;
  
  /** current output tuple */
  ITuplePtr          outputTuple;
  
  /** current input tuple */
  ITuplePtr          inputTuple;
  
  /** current output kind */
  QueueElement.Kind outputKind;
  
  /** current input element */
  QueueElement      inputElement;
  @DumpDesc(ignore=true) QueueElement       inputElementBuf;
  
  /** current output element */
  QueueElement       outputElement;
  
  /** Binding states - indexed by state numbers*/
  @DumpDesc(ignore=true) Binding[]          states;
  
  /** Boolean array indicating current active binding states */
  boolean[]          activeStates;
  
  /** Sequence numbering for the incoming events to pattern */
  long               sequence;
  
  /** Boolean array indicating current input potential matches in the alphabet set */
  boolean[]          matches;
  
  /** Matched but unsure binding */
  @DumpDesc(ignore=true) Binding            unsureMatch;
  
  /** Unique Final binding in the current run */
  @DumpDesc(ignore=true) Binding            uniqueFinal;
  
  /** unique final binding position in the binding array */
  int                uniqueFinalPos;
  
  /** Output Binding */
  @DumpDesc(ignore=true) Binding            outputBinding;
  
  /** Previous tuple */
  ITuplePtr          prevTuple;
  
  /** To go back and process the current final binding */
  boolean            processCurrentFinal;
  
  public PatternStrmState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    inputTs             = 0;
    outputTs            = 0;
    inputElementBuf     = allocQueueElement();
    outputElement       = allocQueueElement();
    state               = ExecState.S_PRE_INIT;
    processCurrentFinal = false;
  }
  
  public void initialize(int numBindings, int bindLength, int alphabetSize,
      ITuplePtr nullInputTuple, IAllocator<ITuplePtr> aggrTupleFactory) throws ExecException
  {
    states         = new Binding[numBindings];
    activeStates   = new boolean[numBindings];
    sequence       = 0;
    matches        = new boolean[alphabetSize];
    this.prevTuple = nullInputTuple;
    
    //initialise the bindings
    for(int i = 0; i < numBindings; i++)
    {
      states[i]       = new Binding(bindLength, nullInputTuple);
      activeStates[i] = false;
    }
    
    if(aggrTupleFactory != null)
    {
      for(int i = 0; i < numBindings; i++)
      {
        ITuplePtr aggrTuple = (ITuplePtr)aggrTupleFactory.allocate(); //SCRATCH_TUPLE
        states[i].setTuple(aggrTuple, bindLength -1);
      }
    }
    
    //State 0 is always active
    activeStates[0]   = true;
    
    //Initialize state 0 with nullInputTuples
    for(int i = 0; i < bindLength-1; i++)
    {
      states[0].setTuple(nullInputTuple, i);
    }
    
    for(int i = 0; i< alphabetSize; i++)
    {
      matches[i] = false;
    }
  }
  
  void incrementSeq()
  {
    sequence++;
  }
}
